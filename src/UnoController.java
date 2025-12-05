import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The UnoController connects the UnoModel, UnoView, and UnoFrame.
 * It handles all user interactions (button presses and card selections)
 * and updates both the model and the view accordingly.
 */
public class UnoController implements ActionListener {
    /** The game model holding players, decks, and game logic. */
    private final UnoModel model;
    /** The view responsible for rendering the state of the game. */
    private final UnoView view;
    /** The main game window with GUI components. */
    private final UnoFrame frame;
    /** Flag indicating if the current player's turn has already advanced. */
    private boolean isAdvanced;
    /** Guard flag to avoid recursively entering the AI turn loop. */
    private boolean handlingAITurn;
    /** Holds the last status message from an AI action so it can be shown alongside turn handoff text. */
    private String pendingAIStatusMessage;

    /**
     * Updates the status message for AI players.
     *
     * @param status the message to display in the view
     */
    private void updateStatusWithPending(String status) {
        if (pendingAIStatusMessage != null && handlingAITurn) {
            status = pendingAIStatusMessage + " " + status;
            pendingAIStatusMessage = null;
        }
        view.updateStatusMessage(status);
    }

    /**
     * Constructs the controller with references to the model, view, and frame.
     *
     * @param model game model containing the core logic
     * @param view view interface for displaying updates
     * @param frame main application window
     */
    public UnoController(UnoModel model, UnoView view, UnoFrame frame) {
        this.model = model;
        this.view = view;
        this.frame = frame;
        isAdvanced = false;
        handlingAITurn = false;
        pendingAIStatusMessage = null;
    }

    /**
     * Builds a unique action command string for the given card
     * - Wild Cards: the command is a unique identifier to differentiate it from other wild cards in the deck
     * - Others: command is based on the colour and value
     *
     * @param card the card to generate an action command
     * @return a unique string representing the card
     */
    private String buildActionCommand(Card card) {
        if (card.getValue().equals(UnoModel.Values.WILD) || card.getValue().equals(UnoModel.Values.WILD_DRAW_TWO)) {
            return card.getValue() + "_" + System.identityHashCode(card);
        }
        return card.getColour() + "_" + card.getValue();
    }

    /**
     * Chooses colour for the AI player when Wild Cards are being played
     * - Picks colour based on the most common colour in the players deck
     *
     * @return the {@link UnoModel.Colours} with the highest count in the players deck
     */
    private UnoModel.Colours chooseColourForAI() {
        Player player = model.getCurrPlayer();
        int[] counts = new int[UnoModel.Colours.values().length];
        for (Card card : player.getPersonalDeck()) {
            UnoModel.Colours colour = card.getColour();
            if (colour != null) {
                counts[colour.ordinal()]++;
            }
        }
        return UnoModel.Colours.values()[indexOfMax(counts)];
    }

    /**
     * Chooses colour for the AI player when Wild Stack card is being played
     * - Picks colour based on the most common colour in the players deck
     *
     * @return the {@link UnoModel.ColoursDark} with the highest count in the players deck
     */
    private UnoModel.ColoursDark chooseDarkColourForAI() {
        Player player = model.getCurrPlayer();
        int[] counts = new int[UnoModel.ColoursDark.values().length];
        for (Card card : player.getPersonalDeck()) {
            UnoModel.ColoursDark colour = card.getColourDark();
            if (colour != null) {
                counts[colour.ordinal()]++;
            }
        }
        return UnoModel.ColoursDark.values()[indexOfMax(counts)];
    }

    /**
     * Returns the index of the maximum value in a list
     *
     * @param values list of integers
     * @return index of the maximum value
     */
    private int indexOfMax(int[] values) {
        int bestIndex = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] > values[bestIndex]) {
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    /**
     * Starts the game by:
     * - Adding players from the frame
     * - Dealing initial hands
     * - Enabling the first player's actions
     */
    public void play() {
        // Add players from the frame's setup
        List<Boolean> aiPlayers = frame.getAiPlayers();
        List<String> playerNames = frame.getPlayerName();
        for (int i = 0; i < playerNames.size(); i++) {
            String player = playerNames.get(i);
            boolean isAI = aiPlayers.get(i);
            model.addPlayer(player, isAI);
        }

        // Start a new round (deal cards, choose initial top card)
        model.newRound();

        // Update the initial hand display and enable cards
        view.updateHandPanel(model, this);
        frame.enableCards();

        // Update initial status message
        view.updateStatusMessage("Game started. It is " + model.getCurrPlayer().getName() + "'s turn.");

        maybeRunAITurn();
    }

    /**
     * Handles button clicks and card selections:
     * - "Next Player" button: advances to next player, checks round/match end.
     * - "Draw Card" button: draws a new card for the current player.
     * - Card buttons: attempts to play the selected card if legal, and applies card effects.
     *
     * @param e the action event triggered by user interaction
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        // ---------- SERIALIZATION: Save Game ----------
        if (command.equals("Save Game")) {
            File saveTarget = frame.chooseSaveFile();
            if (saveTarget != null) {
                try {
                    model.saveGame(saveTarget);
                    view.updateStatusMessage("Game saved to " + saveTarget.getName());
                } catch (IOException ex) {
                    frame.showError("Unable to save game: " + ex.getMessage());
                }
            }
            return;
        }

        // ---------- SERIALIZATION: Load Game ----------
        else if (command.equals("Load Game")) {
            File saveTarget = frame.chooseLoadFile();
            if (saveTarget != null) {
                try {
                    model.loadGame(saveTarget);
                    frame.syncPlayersFromModel(model);
                    frame.refreshScoreboard(model);
                    view.updateHandPanel(model, this);
                    frame.enableCards();
                    updateStatusWithPending("Loaded game. It is " + model.getCurrPlayer().getName() + "'s turn.");
                    maybeRunAITurn();
                } catch (IOException | ClassNotFoundException ex) {
                    frame.showError("Unable to load game: " + ex.getMessage());
                }
            }
            return;
        }

        // Handle "Next Player" button presses
        else if (command.equals("Next Player")) {
            if (!isAdvanced) {
                model.advance();
            }
            Player currPlayer = model.getCurrPlayer();
            view.updateHandPanel(model, this);
            frame.enableCards();
            isAdvanced = false;
            updateStatusWithPending("Turn passed to " + currPlayer.getName() + ".");
            maybeRunAITurn();
        }

        // Handle "Draw Card" button presses
        else if (command.equals("Draw Card")) {
            boolean canDraw = false;
            for (Card card : model.getCurrPlayer().getPersonalDeck()) {
                if (model.isPlayable(card)) {
                    canDraw = true;
                    break;
                }
            }
            if (model.isWildStackCard()) {
                boolean chosen = model.wildStack();
                view.updateHandPanel(model, this);
                frame.disableCardButtons();

                if (chosen) {
                    view.updateHandPanel(model, this);
                    frame.disableCards();
                    view.updateStatusMessage(model.getCurrPlayer().getName() + " drew the colour");
                } else {
                    view.updateStatusMessage("Keep drawing");
                }
            } else if (!canDraw) {
                model.drawCard();
                view.updateHandPanel(model, this);
                frame.disableCards();
                view.updateStatusMessage(model.getCurrPlayer().getName() + " draws one card.");
                isAdvanced = false;
            } else {
                view.updateStatusMessage("You have a card that can be played. Please play it instead of drawing.");
            }
        }

        //Handle "Undo" button presses
        else if(command.equals("Undo")) {
            model.undo();
            view.updateStatusMessage("Undo Performed");
            view.updateHandPanel(model, this);
            view.updateTopCard(model.getTopCard(), model);

            //Will disable cards and draw button if the current player is AI while undoing
            if(model.getCurrPlayer().isAI()) {
                frame.disableCards();
            } else {
                frame.enableCards();
            }
        }

        //Handle "Redo" button presses
        else if(command.equals("Redo")) {
            model.redo();
            view.updateStatusMessage("Redo Performed");
            view.updateHandPanel(model, this);
            view.updateTopCard(model.getTopCard(), model);

            //Will disable cards and draw button if the current player is AI while redoing
            if(model.getCurrPlayer().isAI()) {
                frame.disableCards();
            } else {
                frame.enableCards();
            }
        }

        // Handle card selections
        else {
            Card cardPicked = null;
            String cmd;
            // Identify which card was clicked by matching command strings
            for (Card card : model.getCurrPlayer().getPersonalDeck()) {
                // Find the card that was picked
                if (card.getValue().equals(UnoModel.Values.WILD) || card.getValue().equals(UnoModel.Values.WILD_DRAW_TWO)) {
                    cmd = card.getValue() + "_" + System.identityHashCode(card); // Unique per instance
                } else {
                    cmd = card.getColour() + "_" + card.getValue();
                }
                if (cmd.equals(command)) {
                    cardPicked = card;
                    break;
                }
            }

            if (cardPicked != null && !(model.isPlayable(cardPicked))) {
                view.updateStatusMessage("Placing that card is not a valid move. Try again.");
                return;
            }

            model.playCard(cardPicked);
            isAdvanced = cardPicked.getValue() == UnoModel.Values.SKIP
                    || cardPicked.getValueDark() == UnoModel.ValuesDark.DRAW_FIVE
                    || cardPicked.getValue() == UnoModel.Values.WILD_DRAW_TWO
                    || cardPicked.getValueDark() == UnoModel.ValuesDark.SKIP_ALL;

            if (model.getSide() == UnoModel.Side.LIGHT && cardPicked.getValue() == UnoModel.Values.WILD) {
                String colour;
                if (model.getCurrPlayer().isAI()) {
                    colour = chooseColourForAI().toString();
                } else {
                    colour = frame.colourSelectionDialog();
                }
                view.updateStatusMessage("New colour chosen, " + colour);
                model.wild(UnoModel.Colours.valueOf(colour));
            }

            if (model.getSide() == UnoModel.Side.LIGHT && cardPicked.getValue() == UnoModel.Values.WILD_DRAW_TWO) {
                String colour;
                if (model.getCurrPlayer().isAI()) {
                    colour = chooseColourForAI().toString();
                } else {
                    colour = frame.colourSelectionDialog();
                }
                if (colour != null) {
                    model.wildDrawTwo(UnoModel.Colours.valueOf(colour));
                    view.updateStatusMessage("New colour chosen, " + colour + " and " + model.getNextPlayer().getName() + " skips their turn");
                }
            }

            if (model.getSide() == UnoModel.Side.DARK && cardPicked.getValueDark() == UnoModel.ValuesDark.WILD_STACK) {
                String colour;
                if (model.getCurrPlayer().isAI()) {
                    colour = chooseDarkColourForAI().toString();
                } else {
                    colour = frame.colourSelectionDialogDark();
                }

                model.setInitWildStack(UnoModel.ColoursDark.valueOf(colour));
                view.updateStatusMessage("New colour chosen, " + colour + " and " + model.getNextPlayer().getName()
                        + " keeps drawing until the new colour is chosen");

                isAdvanced = false;
            }

            view.updateHandPanel(model, this);
            frame.disableCards();

            if (model.getSide() == UnoModel.Side.LIGHT
                    && cardPicked.getValue() != UnoModel.Values.WILD
                    && cardPicked.getValue() != UnoModel.Values.WILD_DRAW_TWO) {
                switch (cardPicked.getValue()) {
                    case SKIP -> view.updateStatusMessage(model.getNextPlayer().getName()
                            + " is skipped, turn passes to " + model.getCurrPlayer().getName());
                    case REVERSE -> view.updateStatusMessage("Reversed order");
                    case DRAW_ONE -> view.updateStatusMessage(model.getNextPlayer().getName() + " draws one card");
                    case FLIP -> view.updateStatusMessage("Deck is flipped");
                    default -> view.updateStatusMessage(model.getCurrPlayer().getName() + " played a card");
                }
            } else if (model.getSide() == UnoModel.Side.DARK
                    && cardPicked.getValueDark() != UnoModel.ValuesDark.WILD_STACK) {
                switch (cardPicked.getValueDark()) {
                    case DRAW_FIVE -> view.updateStatusMessage(model.getNextPlayer().getName()
                            + " draws five cards and skips their turn");
                    case SKIP_ALL -> view.updateStatusMessage(model.getCurrPlayer().getName() + "'s turn again");
                    case FLIP -> view.updateStatusMessage("Deck is flipped");
                    default -> view.updateStatusMessage(model.getCurrPlayer().getName() + " played a card");
                }
            }

            // Check if the current player has emptied their hand after playing
            if (model.isDeckEmpty()) {
                Player winner = model.getCurrPlayer();
                int score = model.getScore(winner);
                view.updateStatusMessage("Round over: " + winner.getName()
                        + " wins this round and gets " + score + " points.");
                view.updateWinner(winner.getName(), score);
                boolean matchOver = model.checkWinner(winner);
                if (matchOver) {
                    view.updateStatusMessage("Game over: " + winner.getName()
                            + " wins the game, reaching 500 or more points.");
                    frame.disableAllButtons();

                    String option = frame.newGameSelectionDialog();
                    if (option == null || option.equals("Quit")) {
                        System.exit(0);
                    } else {
                        frame.playerSelectionDialog();
                        model.newGame(frame.getPlayerName(), frame.getAiPlayers());
                        model.newRound();
                        view.updateHandPanel(model, this);
                        frame.enableCards();
                        view.updateStatusMessage("New game started. It is "
                                + model.getCurrPlayer().getName() + "'s turn.");
                    }
                } else {
                    String option = frame.newRoundSelectionDialog();
                    if (option == null || option.equals("Quit")) {
                        System.exit(0);
                    } else {
                        model.newRound();
                        view.updateHandPanel(model, this);
                        frame.enableCards();
                        isAdvanced = false;
                        view.updateStatusMessage("New round started. It is "
                                + model.getCurrPlayer().getName() + "'s turn.");
                        maybeRunAITurn();
                    }
                }
            }
        }
    }

    /**
     * Executes the turn for the AI player until a human player is active
     * - Will stop if a wild stack card is being played to resolve in {@link #actionPerformed(ActionEvent)}
     */
    private void maybeRunAITurn() {
        if (handlingAITurn) {
            return;
        }
        handlingAITurn = true;
        try {
            while (model.getCurrPlayer().isAI()) {
                frame.disableAllButtons();
                view.updateStatusMessage(model.getCurrPlayer().getName() + " (AI) is playing.");
                Card aiChoice = model.chooseAICardForCurrPlayer();
                if (aiChoice != null) {
                    actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, buildActionCommand(aiChoice)));
                    // When AI plays a Wild Stack
                    if (model.isWildStackCard()) {
                        view.updateStatusMessage("Wild stack card played. Chosen colour "
                                + model.getTopCard().getColourDark() + " and " + model.getCurrPlayer().getName()
                                + " draws until " + model.getTopCard().getColourDark() + " is chosen.");
                        break;
                    }
                } else {
                    actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Draw Card"));
                }
                // Only go to next player if wild stack card isn't played
                if (!model.isWildStackCard()) {
                    actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Next Player"));
                } else {
                    break;
                }
            }
        } finally {
            handlingAITurn = false;
        }
    }
}
