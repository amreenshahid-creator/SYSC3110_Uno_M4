// Player.java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Player entity: holds a name, an AI flag, and the personal hand.
 */
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The cards currently in this player's hand. */
    private final List<Card> personalDeck = new ArrayList<>();

    /** Display name of the player (for UI / scoreboard). */
    private final String name;

    /** True if this player is controlled by the computer (AI). */
    private final boolean ai;

    /**
     * Creates a human-controlled player with the given display name.
     */
    public Player(String name) {
        this(name, false);
    }

    /**
     * Creates a player with the given display name and control type.
     *
     * @param name display name
     * @param isAI true if this player is controlled by the AI, false for a human
     */
    public Player(String name, boolean isAI) {
        this.name = name;
        this.ai = isAI;
    }

    /**
     * @return live list of cards held by the player.
     */
    public List<Card> getPersonalDeck() {
        return personalDeck;
    }

    /**
     * Adds a single card to the player's hand.
     */
    public void addCard(Card c) {
        if (c != null) {
            personalDeck.add(c);
        }
    }

    /**
     * @return player display name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return true if this player is controlled by the AI.
     */
    public boolean isAI() {
        return ai;
    }
}
