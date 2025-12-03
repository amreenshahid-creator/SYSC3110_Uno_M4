import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * The main GUI window for the UNO game.
 * <p>
 * UnoFrame is responsible for creating and managing all visible components,
 * including player setup dialogs, scoreboard, hand panel, top card display,
 * and action buttons. It does not contain any game logic; instead, it provides
 * UI elements that the controller can enable, disable, or update.
 * </p>
 */

public class UnoFrame implements UnoView {
    /** Top-level application window. */
    private JFrame frame;

    /** Panel that displays the current top card. */
    private JPanel topCardPanel;

    /** Panel that displays the player's hand as card buttons. */
    private JPanel handPanel;

    /** Container for the scrollable hand panel and the control buttons. */
    private JPanel controlPanel;

    /** Panel that displays player scores. */
    private JPanel scoreBoardPanel;

    /** Label that shows the current player's name. */
    private JLabel currentPlayerLabel;

    /** Label containing the image of the top card. */
    private JLabel topCardLabel;

    /** Status message area for game feedback. */
    private JLabel statusLabel;

    /** Button to draw a new card. */
    private JButton drawButton;

    /** Button to advance to the next player. */
    private JButton nextButton;

    /** Button to save current game. */
    private JButton saveButton;

    /** Button to load a saved game. */
    private JButton loadButton;

    /** Scrollable wrapper for the hand panel. */
    private JScrollPane deckScrollPane;

    /** List of player names obtained during game setup. */
    private java.util.List<String> playerName;

    /** Parallel list indicating whether each player is an AI (true) or human (false). */
    private java.util.List<Boolean> aiPlayers;

    /**
     * Constructs the game window and initializes all graphical components.
     */
    public UnoFrame () {
        initializeGUI();
    }

    /**
     * Builds all GUI panels, prompts for number of players and names,
     * sets up the scoreboard, hand panel, top card panel, and buttons.
     */
    private void initializeGUI() {
        frame = new JFrame ("UNO Game");
        frame.setSize (1000, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // ----- Top info panel: Current player + Status message -----
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout (2, 1));
        currentPlayerLabel = new JLabel("Current Player: ", JLabel.CENTER);
        currentPlayerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        infoPanel.add(currentPlayerLabel);
        statusLabel = new JLabel("Status Message: ", JLabel.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusLabel.setForeground(Color.red);
        infoPanel.add(statusLabel);
        frame.add(infoPanel, BorderLayout.NORTH);

        // ----- Scoreboard Panel -----
        scoreBoardPanel = new JPanel();
        scoreBoardPanel.setLayout(new GridLayout(0, 1, 5, 5));
        scoreBoardPanel.setBorder(BorderFactory.createTitledBorder("Scoreboard"));
        scoreBoardPanel.setPreferredSize(new Dimension(180, 200));
        for (int i = 1; i <= 4; i++){
            scoreBoardPanel.add(new JLabel("Player " + i + ": "));
        }
        frame.add(scoreBoardPanel, BorderLayout.WEST);

        // ----- Top Card Panel -----
        topCardPanel = new JPanel(new GridBagLayout());
        topCardPanel.setBorder(BorderFactory.createTitledBorder("Top Card"));
        topCardPanel.setPreferredSize(new Dimension(200, 200));
        topCardLabel = new JLabel();
        topCardLabel.setHorizontalAlignment((JLabel.CENTER));
        topCardLabel.setVerticalAlignment(JLabel.CENTER);
        //topCardLabel.setPreferredSize(new Dimension(80, 120));
        topCardPanel.add(topCardLabel);
        frame.add(topCardPanel, BorderLayout.CENTER);

        // ----- Player Hand Panel -----
        handPanel = new JPanel();
        handPanel.setLayout(new BoxLayout(handPanel, BoxLayout.X_AXIS));
        handPanel.setBorder(BorderFactory.createTitledBorder("Player's Deck"));
        //handPanel.setPreferredSize(new Dimension(400, 300));
        handPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));


        // ----- Draw + Next Buttons -----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        drawButton = new JButton("Draw Card");
        nextButton = new JButton("Next Player");
        saveButton = new JButton("Save Game");
        loadButton = new JButton("Load Game");
        buttonPanel.add(drawButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);

        // ----- Right-hand control section -----
        controlPanel = new JPanel(new BorderLayout());
        controlPanel.setPreferredSize(new Dimension(400, 300));

        deckScrollPane = new JScrollPane(handPanel);
        deckScrollPane.setPreferredSize(new Dimension(400, 300));
        deckScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        deckScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        controlPanel.add(deckScrollPane, BorderLayout.NORTH);
        controlPanel.add(buttonPanel, BorderLayout.CENTER);

        frame.add(controlPanel, BorderLayout.EAST);

        frame.setVisible (true);

        // ----- Prompt Player Count -----
        String[] playerOptions = {"2", "3", "4"};
        String playerCount = (String) JOptionPane.showInputDialog(
                frame,
                "Select Number of Players:",
                "Player Setup",
                JOptionPane.QUESTION_MESSAGE,
                null,
                playerOptions,
                playerOptions[0]
        );

        // If canceled, exit
        if (playerCount == null){
            System.exit(0);
        }

        // ----- Prompt Player Names + AI/Human -----
        int count = Integer.parseInt(playerCount);
        playerName = new ArrayList<>();
        aiPlayers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String name = JOptionPane.showInputDialog(
                    frame,
                    "Enter name for Player " + i + ":",
                    "Player Setup",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (name == null || name.trim().isEmpty()) {
                name = "Player" + i;
            }
            playerName.add(name);

            Object[] typeOptions = {"Human", "AI"};
            int choice = JOptionPane.showOptionDialog(
                    frame,
                    "Is " + name + " a Human or AI player?",
                    "Player Type",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    typeOptions,
                    typeOptions[0]
            );
            boolean isAI = (choice == 1); // 0 = Human, 1 = AI, anything else defaults to Human
            aiPlayers.add(isAI);
        }

        // ----- Setup Scoreboard for Actual Player Count -----
        scoreBoardPanel.removeAll();
        scoreBoardPanel.setLayout(new GridLayout(playerName.size(), 1, 5, 5));
        for(int i = 0; i < playerName.size(); i++){
            JLabel scores = new JLabel(playerName.get(i) + ": 0");
            scoreBoardPanel.add(scores);
        }
        scoreBoardPanel.revalidate();
        scoreBoardPanel.repaint();

    }

    /** @return the label that shows the top card image. */
    public JLabel getTopCardLabel() {
        return topCardLabel;
    }

    /** @return the label indicating the current player's name. */
    public JLabel getCurrentPlayerLabel() {
        return currentPlayerLabel;
    }

    /** @return the button used to advance to the next player. */
    public JButton getNextButton() {
        return nextButton;
    }


    /** @return the button used to draw a card. */
    public JButton getDrawButton() {
        return drawButton;
    }

    /** @return the scoreboard panel. */
    public JPanel getScoreBoardPanel() {
        return scoreBoardPanel;
    }

    /** @return the status message label. */
    public JLabel getStatusLabel() {
        return statusLabel;
    }

    /** @return the panel holding the top card. */
    public JPanel getTopCardPanel() {
        return topCardPanel;
    }

    /**
     * Opens a dialog to let the user choose the new colour for a WILD card.
     * @return the chosen colour (RED, YELLOW, GREEN, BLUE) or null if cancelled
     */
    public String colourSelectionDialog() {
        String[] colours = {"RED", "YELLOW", "GREEN", "BLUE"};
        String colourSelected = (String) JOptionPane.showInputDialog(
                frame,
                "Choose new colour for Wild Card:",
                "Wild Card Colour",
                JOptionPane.PLAIN_MESSAGE,
                null,
                colours,
                colours[0]
        );
        return colourSelected;
    }

    /**
     * Opens a dialog to let the user choose the new colour for a WILD_STACK card.
     * @return the chosen colour (TEAL, PURPLE, PINK, ORANGE) or null if cancelled
     */
    public String colourSelectionDialogDark() {
        String[] colours = {"TEAL", "PURPLE", "PINK", "ORANGE"};
        String colourSelected = (String) JOptionPane.showInputDialog(
                frame,
                "Choose new colour for Wild Card:",
                "Wild Card Colour",
                JOptionPane.PLAIN_MESSAGE,
                null,
                colours,
                colours[0]
        );
        return colourSelected;
    }

    /**
     * Opens a dialog to let the user choose an option once round is over
     * @return the chosen option (New Round, Quit) or null if cancelled
     */
    public String newRoundSelectionDialog() {
        String[] options = {"New Round", "Quit"};
        String optionSelected = (String) JOptionPane.showInputDialog(
                frame,
                "New Round to Continue playing or Quit",
                "Round Over",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
        return optionSelected;
    }

    /**
     * Opens a dialog to let the user choose an option once game is over
     * @return the chosen option (New Game, Quit) or null if cancelled
     */
    public String newGameSelectionDialog() {
        String[] options = {"New Game", "Quit"};
        String optionSelected = (String) JOptionPane.showInputDialog(frame, "New Game or Quit", "Game Over", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        return optionSelected;
    }

    /**
     * Sets each players scores to 0 and opens dialogs to let the user choose:
     * -Number of players
     * -Names of the players
     * -Human or AI
     */
    public void playerSelectionDialog() {
        // ----- Prompt Player Count -----
        String[] playerOptions = {"2", "3", "4"};
        String playerCount = (String) JOptionPane.showInputDialog(
                frame,
                "Select Number of Players:",
                "Player Setup",
                JOptionPane.QUESTION_MESSAGE,
                null,
                playerOptions,
                playerOptions[0]
        );

        // If canceled, exit
        if (playerCount == null){
            System.exit(0);
        }

        // ----- Prompt Player Names + AI/Human -----
        int count = Integer.parseInt(playerCount);
        playerName = new ArrayList<>();
        aiPlayers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String name = JOptionPane.showInputDialog(
                    frame,
                    "Enter name for Player " + i + ":",
                    "Player Setup",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (name == null || name.trim().isEmpty()) {
                name = "Player" + i;
            }
            playerName.add(name);

            Object[] typeOptions = {"Human", "AI"};
            int choice = JOptionPane.showOptionDialog(
                    frame,
                    "Is " + name + " a Human or AI player?",
                    "Player Type",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    typeOptions,
                    typeOptions[0]
            );
            boolean isAI = (choice == 1); // 0 = Human, 1 = AI, anything else defaults to Human
            aiPlayers.add(isAI);
        }

        // ----- Setup Scoreboard for Actual Player Count -----
        scoreBoardPanel.removeAll();
        scoreBoardPanel.setLayout(new GridLayout(playerName.size(), 1, 5, 5));
        for(int i = 0; i < playerName.size(); i++){
            JLabel scores = new JLabel(playerName.get(i) + ": 0");
            scoreBoardPanel.add(scores);
        }
        scoreBoardPanel.revalidate();
        scoreBoardPanel.repaint();
    }

    /** @return list of player names entered during setup. */
    public List<String> getPlayerName() {
        return playerName;
    }

    /** @return list of AI flags aligned with getPlayerName(): true = AI, false = Human. */
    public List<Boolean> getAiPlayers() {
        return aiPlayers;
    }

    /**
     * Creates a JButton representation of a card with scaling and action command.
     * @param card the model card
     * @return a button containing the card image and correct action command
     */
    public JButton cardButtons(Card card, UnoModel model) {
        JButton cardButton = new JButton(resizeImage(card.getFileName(model.getSide()), 150, 250));
        cardButton.setPreferredSize(new Dimension(150, 250));
        cardButton.setMaximumSize(new Dimension(150, 250));
        cardButton.setMinimumSize(new Dimension(150, 250));

        if(card.getValue().equals(UnoModel.Values.WILD) || card.getValue().equals(UnoModel.Values.WILD_DRAW_TWO)) {
            cardButton.setActionCommand(card.getValue() + "_" + System.identityHashCode(card));
        }
        else {
            cardButton.setActionCommand(card.getColour() + "_" + card.getValue());
        }

        return cardButton;
    }

    /**
     * Rebuilds the hand panel with card buttons for the current player.
     * @param cards list of cards to display
     * @param controller the action listener for card clicks
     */
    public void handPanelButtons(List<Card> cards, UnoController controller, UnoModel model) {
        handPanel.removeAll();

        if (model.isWildStackCard()) {
            disableCardButtons();
        }else {
            enableCards();
        }
        for(Card c: cards) {
            JButton cardButton = cardButtons(c,model);
            cardButton.addActionListener(controller);
            handPanel.add(cardButton);
            handPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        }
        handPanel.revalidate();
        handPanel.repaint();
    }

    /**
     * Scales an image file to create a consistent card display.
     * @param file filename/path of the image
     * @param width target width
     * @param height target height
     * @return ImageIcon resized to the given dimensions
     */
    public ImageIcon resizeImage(String file, int width, int height) {
        ImageIcon image = new ImageIcon(file);
        Image resize = image.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resize);
    }

    /**
     * Adds the controller's ActionListeners to the Draw and Next buttons.
     * @param controller UnoController instance
     */
    public void addController(UnoController controller) {
        nextButton.addActionListener(controller);
        nextButton.setActionCommand("Next Player");
        drawButton.addActionListener(controller);
        drawButton.setActionCommand("Draw Card");
        saveButton.addActionListener(controller);
        saveButton.setActionCommand("Save Game");
        loadButton.addActionListener(controller);
        loadButton.setActionCommand("Load Game");
    }

    /**
     * Enables all card buttons and the Draw button.
     * Disables Next until the player performs an action.
     */
    public void enableCards() {
        drawButton.setEnabled(true);
        nextButton.setEnabled(false); //player can only press once they play or draw a card

        for(Component comp: handPanel.getComponents()) { //goes through all the buttons in hand panel
            if(comp instanceof JButton) {
                comp.setEnabled(true);
            }
        }
    }

    /**
     * Disables card buttons after the player acts and enables Next Player.
     */
    public void disableCards() {
        drawButton.setEnabled(false);
        nextButton.setEnabled(true);

        for(Component comp: handPanel.getComponents()) {
            if(comp instanceof JButton) {
                comp.setEnabled(false);
            }
        }
    }


    /**
     * Disables all card buttons.
     */
    public void disableCardButtons() {
        drawButton.setEnabled(true);
        nextButton.setEnabled(false);

        for(Component comp: handPanel.getComponents()) {
            if(comp instanceof JButton) {
                comp.setEnabled(false);
            }
        }
    }

    /**
     * Disables all interactable buttons (used when the game ends).
     */
    public void disableAllButtons() {
        drawButton.setEnabled(false);
        nextButton.setEnabled(false);

        if (saveButton != null) {
            saveButton.setEnabled(true);
        }
        if (loadButton != null) {
            loadButton.setEnabled(true);
        }

        for(Component comp: handPanel.getComponents()) {
            if(comp instanceof JButton) {
                comp.setEnabled(false);
            }
        }
    }

    /**
     * Prompts the user to choose a file location for saving.
     * @return selected file or null if the dialog was cancelled
     */
    public File chooseSaveFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Prompts the user to choose a previously saved game file.
     * @return selected file or null if the dialog was cancelled
     */
    public File chooseLoadFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Shows an error dialog when persistence fails.
     * @param message text to display
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Save/Load Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Updates stored player metadata to mirror the model contents.
     * @param model the UnoModel being displayed
     */
    public void syncPlayersFromModel(UnoModel model) {
        playerName = new ArrayList<>();
        aiPlayers = new ArrayList<>();
        for (Player player : model.getPlayers()) {
            playerName.add(player.getName());
            aiPlayers.add(player.isAI());
        }
    }

    /**
     * Refreshes the scoreboard labels using the model's current scores.
     * @param model the UnoModel providing score data
     */
    public void refreshScoreboard(UnoModel model) {
        scoreBoardPanel.removeAll();
        scoreBoardPanel.setLayout(new GridLayout(model.getPlayers().size(), 1, 5, 5));
        Map<String, Integer> scores = model.getFinalScores();
        for (Player p : model.getPlayers()) {
            int score = scores.getOrDefault(p.getName(), 0);
            scoreBoardPanel.add(new JLabel(p.getName() + ": " + score));
        }
        scoreBoardPanel.revalidate();
        scoreBoardPanel.repaint();
    }

    // ---------------- Interface Methods ----------------
    /**
     * Updates the top portion of the GUI:
     *  - Displays the current player's name
     *  - Updates the top card image
     *
     * @param event the {@link UnoEvent} made by UnoModel
     */
    @Override
    public void update(UnoEvent event) {
        UnoModel model = (UnoModel) event.getSource();
        currentPlayerLabel.setText("Current Player: " + model.getCurrPlayer().getName());

        // Resize and update the displayed top card image
        Dimension topCardSize = getTopCardPanel().getSize();
        getTopCardLabel().setIcon(
                resizeImage(
                        model.getTopCard().getFileName(model.getSide()),
                        topCardSize.width - 180,
                        topCardSize.height - 250
                )
        );
    }

    /**
     * Updates the player's hand panel by:
     *  - Removing old card buttons
     *  - Creating fresh buttons for the current player's hand
     *  - Adding listeners for each card button
     *
     * @param model the game model containing the player's hand
     * @param controller controller handling card-click events
     */
    @Override
    public void updateHandPanel(UnoModel model, UnoController controller) {
        handPanelButtons(model.getCurrPlayer().getPersonalDeck(), controller, model);
    }

    /**
     * Updates the status message shown at the top of the screen.
     *
     * @param msg text describing what just happened (e.g., "Player drew a card")
     */
    @Override
    public void updateStatusMessage(String msg) {
        statusLabel.setText("Status Message: " + msg);
    }

    /**
     * Updates the scoreboard panel when a player wins.
     * Replaces that player's score label with their new total.
     *
     * @param winner name of the winning player
     * @param score updated score that should be displayed
     */
    @Override
    public void updateWinner(String winner, int score) {

        // Loop through scoreboard labels and replace matching entry
        Component[] scores = scoreBoardPanel.getComponents();
        for (Component comp : scores) {
            if (comp instanceof JLabel label) {
                if (label.getText().startsWith(winner)) {
                    label.setText(winner + ": " + score);
                }
            }
        }
    }

    /**
     * Main method to launch the standalone UNO game window.
     */
    public static void main(String[] args) {
        UnoFrame frame = new UnoFrame();
        UnoModel model = new UnoModel();
        UnoView view = frame;
        UnoController controller = new UnoController(model, view, frame);

        model.addView(view);

        frame.addController(controller);
        controller.play();
    }
}
