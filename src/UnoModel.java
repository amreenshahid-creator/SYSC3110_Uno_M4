import java.io.*;
import java.util.*;

/**
 * Core game model for a UNO Flip game (Milestone-ready).
 * <p>
 * Responsibilities:
 * - Holds game state (players, direction, current/next player).
 * - Manages deck-less random card generation for draws (stubbed RNG).
 * - Enforces legal-play checks (colour/value match; wilds always playable).
 * - Applies card effects for the light side (FlIP, DRAW_ONE, REVERSE, SKIP, WILD, WILD_DRAW_TWO).
 * - Applies card effects for the dark side (FLIP, DRAW_FIVE, SKIP_ALL, WILD_STACK).
 * - Computes per-round score for the winner and tracks cumulative scores based on the current side of the deck.
 * - Notifies registered views (observer-style hooks via {@link #addView(UnoView)}).
 * <p>
 * Notes:
 * - This class is not thread-safe (single-threaded Swing usage assumed).
 * - Random draws use {@link java.util.Random}; no persistence of a physical deck in this version.
 */

public class UnoModel implements Serializable {
    /** Available light card colours. Wilds use null colour. */
    public enum Colours {RED, YELLOW, GREEN, BLUE}

    /** Available dark card colours. Wilds use null colour. */
    public enum ColoursDark {ORANGE, PINK, PURPLE, TEAL}

    /** Available card values, including action/wild cards. */
    public enum Values {ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, DRAW_ONE, REVERSE, SKIP, WILD, WILD_DRAW_TWO, FLIP}

    /** Available card values for dark side. */
    public enum ValuesDark {ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, FLIP, DRAW_FIVE, SKIP_ALL, WILD_STACK}

    /** What side the deck is being played on. */
    public enum Side {LIGHT, DARK};

    // Players in turn order (clockwise or counterclockwise based on 'direction').
    private final List<Player> players = new ArrayList<>();

    // Index of the current player within 'players'.
    private int currPlayerIndex = 0;

    // Turn direction: +1 for clockwise, -1 for counterclockwise.
    int direction = 1;

    // The card currently on top of the discard pile (sets legal-play constraints).
    private Card topCard;

    // Cumulative (match) scores per player name.
    private final Map<String, Integer> finalScores = new HashMap<>();

    // Registered views to be notified on model changes.
    private final transient List<UnoView> views = new ArrayList<>();

    //Holds the current side
    private Side side = Side.LIGHT;

    //Indicates whether a Wild Stack card is currently in play
    private boolean isWildStackCard = false;

    //Holds the new chosen colour for the Wild Stack card
    private ColoursDark newColour;

    //For serialization
    private static final long serialVersionUID = 1L;

    /**
     * Generates a random card. In this milestone, there is no physical deck;
     * draws are random and infinite.
     * @return a pseudo-random {@link Card}
     */
    public Card getRandomCard() {
        Random rand = new Random();

        Values[] valuesLight = Values.values();
        Values valueLight = valuesLight[rand.nextInt(valuesLight.length)];

        Colours colourLight = null;
        // Wilds have no colour until set; all other cards need a colour.
        if (valueLight != Values.WILD && valueLight != Values.WILD_DRAW_TWO) {
            Colours[] coloursLight = Colours.values();
            colourLight = coloursLight[rand.nextInt(coloursLight.length)];
        }

        ValuesDark[] valuesDark = ValuesDark.values();
        ValuesDark valueDark = valuesDark[rand.nextInt(valuesDark.length)];

        ColoursDark colourDark = null;
        if(valueDark != ValuesDark.WILD_STACK) {
            ColoursDark[] coloursDark = ColoursDark.values();
            colourDark = coloursDark[rand.nextInt(coloursDark.length)];
        }

        return new Card(colourLight, valueLight, colourDark, valueDark);
    }

    /**
     * Plays a card from the current player's hand and sets it as the top card.
     * Assumes caller checked {@link #isPlayable(Card)} beforehand.
     * @param card the card to play
     */
    public void playCard(Card card) {
        getCurrPlayer().getPersonalDeck().remove(card);
        topCard = card;
        notifyViews();
    }

    /**
     * Current player draws one random card.
     */
    public void drawCard() {
        Player currPlayer = getCurrPlayer();
        currPlayer.addCard(getRandomCard());
        notifyViews();
    }


    //--------------- ACTION CARDS ----------------//

    /**
     * Makes the next player (relative to current direction) draw exactly one card.
     * @return the drawn {@link Card}
     */
    public Card drawOne() {
        Card drawnCard = getRandomCard();
        int nextPlayerIndex = (currPlayerIndex + 1) % players.size();
        Player nextPlayer = players.get(nextPlayerIndex);
        nextPlayer.addCard(drawnCard);
        notifyViews();
        return drawnCard;
    }

    /**
     * Reverses play direction (clockwise ↔ counterclockwise).
     */
    public void reverse() {
        direction = -direction;
        notifyViews();
    }

    /**
     * Skips the next player's turn by advancing two steps in current direction.
     */
    public void skip() {
        currPlayerIndex = (currPlayerIndex + 2 * direction + players.size()) % players.size();
        notifyViews();
    }

    /**
     * Applies a wild colour choice to the current top card.
     * @param newColour chosen colour (cannot be null)
     */
    public void wild(Colours newColour) {
        topCard.setColour(newColour);
        notifyViews();
    }

    /**
     * Wild Draw Two: set colour and make next player draw 2, then skip them.
     * @param newColour chosen colour for the wild
     * @return list containing the two drawn cards
     */
    public List<Card> wildDrawTwo(Colours newColour) {
        topCard.setColour(newColour);
        Card drawnCard1 = getRandomCard();
        Card drawnCard2 = getRandomCard();
        int nextPlayerIndex = (currPlayerIndex + direction + players.size()) % players.size();
        Player nextPlayer = players.get(nextPlayerIndex);
        nextPlayer.addCard(drawnCard1);
        nextPlayer.addCard(drawnCard2);
        notifyViews();

        List<Card> drawnCards = new ArrayList<>();
        drawnCards.add(drawnCard1);
        drawnCards.add(drawnCard2);

        skip();

        return drawnCards;
    }

    /**
     * Flips the deck between light and dark sides
     * When flipping, ensures that the top card s not a wild card
     */
    public void flip() {
        if(side == Side.DARK) {
            side = Side.LIGHT;

            while(topCard != null && (topCard.getValue()).equals(Values.WILD) ||(topCard.getValue()).equals(Values.WILD_DRAW_TWO) ) {
                topCard = getRandomCard();
            }

        } else {
            side = Side.DARK;

            while(topCard != null && (topCard.getValueDark()).equals(ValuesDark.WILD_STACK)) {
                topCard = getRandomCard();
            }
        }

        notifyViews();
    }


    /**
     * Adds five cards to the next players deck and skips their turn.
     */
    public void drawFive() {
        int nextPlayerIndex = (currPlayerIndex + 1) % players.size();
        Player nextPlayer = players.get(nextPlayerIndex);

        for(int i = 0; i < 5; i++) {
            nextPlayer.addCard(getRandomCard());
        }

        notifyViews();
        skip();
    }


    /**
     * Skips every players turn and current player plays again by not advancing
     */
    public void skipAll() {
        notifyViews();
    }


    /**
     * Initiates the Wild Stack card by setting the chosen colour for the stack,
     * setting the top card as a wild stack card and advancing to next player.
     *
     * @param newColour colour chosen for the Wild Stack
     */
    public void setInitWildStack(ColoursDark newColour) {
        topCard.setColourDark(newColour);
        isWildStackCard = true;
        this.newColour = newColour;
        currPlayerIndex = (currPlayerIndex + direction + players.size()) % players.size();
        notifyViews();
    }

    /**
     * Executes logic for Wild Stack card by drawing cards for the current player.
     * - Draw cards until it matches the chosen colour
     * - When matching card is drawn, end the stack
     *
     * @return true if the player drew the chosen colour, false otherwise
     */
    public boolean wildStack() {
        if(!isWildStackCard) {
            return false;
        }

        Card drawnCard = getRandomCard();
        getCurrPlayer().addCard(drawnCard);
        notifyViews();

        if(drawnCard.getColourDark() != null && drawnCard.getColourDark().equals(newColour)) {
            isWildStackCard = false;
            newColour = null;
            //currPlayerIndex = (currPlayerIndex + direction + players.size()) % players.size();
            notifyViews();
            return true;
        }

        return false;
    }

    /**
     * Checks if Wild Stack card is being played
     *
     * @return true if the Wild Stack card is currently being played, false otherwise.
     */
    public boolean isWildStackCard() {
        return isWildStackCard;

    }

    //------------------------------------------//

    /**
     * Starts a new round:
     * - Clears each player's hand and deals 7 random cards.
     * - Chooses a non-wild top card to begin play.
     * - Resets current player and direction.
     */
    public void newRound() {
        for(Player player: players) {
            player.getPersonalDeck().clear();
            for(int i = 0; i < 7; i++) {
                player.addCard(getRandomCard());
            }
        }
        do {
            topCard = getRandomCard();
        }while (topCard.getValue() == Values.WILD || topCard.getValue() == Values.WILD_DRAW_TWO);

        side = Side.LIGHT;
        currPlayerIndex = 0;
        direction = 1;
        notifyViews();
    }

    /**
     * Starts a new game:
     * - Clears each all players and their scores.
     * - Resets current player and direction.
     */
    public void newGame(List<String> player, List<Boolean> isAI) {
        players.clear();
        finalScores.clear();

        for(int i = 0; i < player.size(); i++) {
            players.add(new Player(player.get(i), isAI.get(i)));     //Add new players
            finalScores.put(player.get(i), 0);                       //Set each players score to 0
        }

        side = Side.LIGHT;
        currPlayerIndex = 0;
        direction = 1;
        notifyViews();
    }

    /**
     * Computes the round score earned by the winner:
     * Sum of point values of all other players' remaining cards.
     * @param winner player who emptied their hand
     * @return numeric score for this round
     */
    public int getScore(Player winner) {
        int score = 0;
        for(Player player : players) {
            if (player == winner){
                continue;
            }
            List<Card> deck = player.getPersonalDeck();
            if(side == Side.LIGHT) {
                for (Card card : deck) {
                    switch (card.getValue()) {
                        case ONE -> score += 1;
                        case TWO -> score += 2;
                        case THREE -> score += 3;
                        case FOUR -> score += 4;
                        case FIVE -> score += 5;
                        case SIX -> score += 6;
                        case SEVEN -> score += 7;
                        case EIGHT -> score += 8;
                        case NINE -> score += 9;
                        case DRAW_ONE -> score += 10;
                        case SKIP, REVERSE -> score += 20;
                        case WILD -> score += 40;
                        case WILD_DRAW_TWO -> score += 50;

                    }
                }
            } else {
                for (Card card : deck) {
                    switch (card.getValueDark()) {
                        case ONE -> score += 1;
                        case TWO -> score += 2;
                        case THREE -> score += 3;
                        case FOUR -> score += 4;
                        case FIVE -> score += 5;
                        case SIX -> score += 6;
                        case SEVEN -> score += 7;
                        case EIGHT -> score += 8;
                        case NINE -> score += 9;
                        case DRAW_FIVE, FLIP -> score += 20;
                        case SKIP_ALL -> score += 30;
                        case WILD_STACK -> score += 60;
                    }
                }
            }
        }
        return score;
    }

    /**
     * Advances to the next player's turn using current direction.
     */
    public void advance() {
        currPlayerIndex = (currPlayerIndex + direction + players.size()) % players.size();
        notifyViews();
    }

    /**
     * Updates cumulative scores and checks if any player reached the winning threshold.
     * @param winner player who just won the round
     * @return true if someone (possibly the winner) reached the match target (e.g., 500)
     */
    public boolean checkWinner(Player winner) {
        int winnerScore = getScore(winner);
        finalScores.put(winner.getName(), finalScores.get(winner.getName()) + winnerScore);

        for(Player p: players) {
            int SCORE_TO_WIN = 500;
            if(finalScores.get(p.getName()) >= SCORE_TO_WIN) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return copy of the cumulative scores keyed by player name.
     */
    public Map<String, Integer> getFinalScores() {
        return new HashMap<>(finalScores);
    }


    /**
     * Legal play check:
     * - Wilds are always playable.
     * - Otherwise, either colour matches top card's colour or value matches top card's value.
     * @param card card to evaluate
     * @return true if the card can be legally played now
     */
    public boolean isPlayable(Card card){

        //wild cards can always be played
        if(side == Side.LIGHT) {
            if(card.getValue() == Values.WILD || card.getValue() == Values. WILD_DRAW_TWO) {
                return true;
            }
        } else {
            if(card.getValueDark() == ValuesDark.WILD_STACK) {
                return true;
            }
        }

        if(side == Side.LIGHT) {
            boolean sameColour = topCard.getColour() != null && card.getColour() != null && card.getColour().equals(topCard.getColour());
            boolean sameValue = (card.getValue() == topCard.getValue());
            return sameColour || sameValue;
        }

        else {
            boolean sameColour = topCard.getColourDark() != null && card.getColourDark() != null && card.getColourDark().equals(topCard.getColourDark());
            boolean sameValue = (card.getValueDark() == topCard.getValueDark());
            return sameColour || sameValue;
        }
    }

    /**
     * Adds a new player by name and initializes their cumulative score to 0.
     * @param playerName display name
     * @param isAI whether the player is computer-controlled
     */
    public void addPlayer(String playerName, boolean isAI) {
        players.add(new Player(playerName, isAI));
        finalScores.put(playerName, 0);
    }

    /**
     * Convenience overload for adding a human player.
     * @param playerName display name
     */
    public void addPlayer(String playerName) {
        addPlayer(playerName, false);
    }

    /** @return current player object */
    public Player getCurrPlayer() {
        return players.get(currPlayerIndex);
    }

    /**
     * @return the next player considering current direction
     */
    public Player getNextPlayer() {
        return players.get((currPlayerIndex + direction + players.size()) % players.size());
    }

    /**
     * @return immutable list of players in seating order.
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /** @return the current top (discard) card */
    public Card getTopCard() {
        return topCard;
    }

    /**
     * Sets the current top (discard) card.
     * @param card card to become the new top
     */
    public void setTopCard(Card card) {
        topCard = card;
    }

    /**
     * @return true if the current player has emptied their hand
     */
    public boolean isDeckEmpty() {
        return getCurrPlayer().getPersonalDeck().isEmpty();
    }

    public Side getSide() {
        return side;
    }

    /**
     * Registers a view to receive {@link #notifyViews()} updates.
     * @param view a view to add
     */
    public void addView(UnoView view) {
        if(!views.contains(view)) {
            views.add(view);
        }
    }

    /**
     * Unregisters a previously added view.
     * @param view view to remove
     */
    public void removeView(UnoView view){
        views.remove(view);
    }

    /**
     * Notifies all registered views to refresh from model state.
     * (Simple observer-style callback.)
     */
    public void notifyViews() {
        UnoEvent event = new UnoEvent(this);

        for(UnoView v: views) {
            v.update(event);
        }
    }

    // ---------- AI helpers ----------

    /**
     * Returns a list of all cards in players hand that are playable.
     *
     * @param player the player whose hand is being checked
     * @return list of playable cards, null if none are playable
     */
    public List<Card> getPlayableCards(Player player) {
        List<Card> playable = new ArrayList<>();
        for(Card card : player.getPersonalDeck()) {
            if(isPlayable(card)) {
                playable.add(card);
            }
        }
        return playable;
    }


    /**
     * Checks if the player has playable cards in their deck
     *
     * @param player the player whose deck is being checked
     * @return true if the player has playable cards, otherwise false
     */
    public boolean hasPlayableCard(Player player) {
        return !getPlayableCards(player).isEmpty();
    }


    /**
     * Checks if the current player has any playable cards.
     *
     * @return true if the current player has playable cards, otherwise false
     */
    public boolean currPlayerHasPlayableCard() {
        return hasPlayableCard(getCurrPlayer());
    }

    /**
     * Persists the current game state to the provided file path.
     *
     * @param file destination file for the serialized state
     * @throws IOException if writing to disk fails
     */
    public void saveGame(File file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(new GameState(this));
        }
    }

    /**
     * Loads game state from disk and replaces the current model data.
     *
     * @param file saved-game file to load
     * @throws IOException if file access fails or contents are invalid
     * @throws ClassNotFoundException if the stored classes cannot be resolved
     */
    public void loadGame(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object data = in.readObject();
            if (!(data instanceof GameState state)) {
                throw new IOException("Invalid save file");
            }
            applyState(state);
        }
    }

    private void applyState(GameState state) {
        players.clear();
        players.addAll(state.players);

        finalScores.clear();
        finalScores.putAll(state.finalScores);

        currPlayerIndex = state.currPlayerIndex;
        direction = state.direction;
        topCard = state.topCard;
        side = state.side;
        isWildStackCard = state.isWildStackCard;
        newColour = state.newColour;

        notifyViews();
    }

    private static class GameState implements Serializable {
        private static final long serialVersionUID = 1L;

        private final List<Player> players;
        private final Map<String, Integer> finalScores;
        private final int currPlayerIndex;
        private final int direction;
        private final Card topCard;
        private final Side side;
        private final boolean isWildStackCard;
        private final ColoursDark newColour;

        GameState(UnoModel model) {
            this.players = new ArrayList<>();
            for (Player p : model.players) {
                Player copy = new Player(p.getName(), p.isAI());
                copy.getPersonalDeck().addAll(p.getPersonalDeck());
                this.players.add(copy);
            }

            this.finalScores = new HashMap<>(model.finalScores);
            this.currPlayerIndex = model.currPlayerIndex;
            this.direction = model.direction;
            this.topCard = model.topCard;
            this.side = model.side;
            this.isWildStackCard = model.isWildStackCard;
            this.newColour = model.newColour;
        }
    }


    /**
     * Chooses a card from the AIs list of playable cards to play by
     * playing non number cards if multiple playable cards exist.
     *
     * @param player the AI player whose deck will be checked to determine the best card to play
     * @return the chosen card to play or null if player has no playable cards
     */
    public Card chooseAICard(Player player) {
        List<Card> playable = getPlayableCards(player);
        if(playable.isEmpty()) {
            return null;
        }

        Card best = null;
        for(Card card : playable) {
            if(best == null) {
                best = card;
                continue;
            }

            boolean bestIsNumber = isNumberCard(best);
            boolean cardIsNumber = isNumberCard(card);

            if(bestIsNumber && !cardIsNumber) {
                best = card;
            }
        }

        return best;
    }

    /**
     * Chooses a card from the current AIs list of playable cards to play
     * using the same method as {@link #chooseAICard(Player)}
     *
     * @return the chosen card to play or null if player has no playable cards
     */
    public Card chooseAICardForCurrPlayer() {
        return chooseAICard(getCurrPlayer());
    }


    /**
     * Checks whether the given card is number card (1-9)
     *
     * @param card the card that is being checked
     * @return true if the card is a number card, false otherwise
     */
    private boolean isNumberCard(Card card) {
        if(side == Side.LIGHT) {
            Values v = card.getValue();
            return v == Values.ONE || v == Values.TWO || v == Values.THREE || v == Values.FOUR
                    || v == Values.FIVE || v == Values.SIX || v == Values.SEVEN
                    || v == Values.EIGHT || v == Values.NINE;
        } else {
            ValuesDark v = card.getValueDark();
            return v == ValuesDark.ONE || v == ValuesDark.TWO || v == ValuesDark.THREE || v == ValuesDark.FOUR
                    || v == ValuesDark.FIVE || v == ValuesDark.SIX || v == ValuesDark.SEVEN
                    || v == ValuesDark.EIGHT || v == ValuesDark.NINE;
        }
    }
}