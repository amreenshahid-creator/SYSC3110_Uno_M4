/**
 * View interface for the UNO game.
 * Responsible for:
 *  - Displaying the current player
 *  - Showing the top card image
 *  - Updating the player's hand panel
 *  - Updating status messages
 *  - Updating the scoreboard when a player wins
 * The UnoView does NOT contain game logic. It only updates visible elements
 * inside the UnoFrame based on changes in the UnoModel.
 */
public interface UnoView {

    /**
     * Updates the main display according to the state of the model
     * @param event the {@link UnoEvent} made by UnoModel
     */
    void update(UnoEvent event);

    /**
     * Updates the hand panel to display current players cards
     * @param model the UnoModel with the current players hand
     * @param controller the UnoController handling the card clicks
     */
    void updateHandPanel(UnoModel model, UnoController controller);

    /**
     * Updates the status message based on the current state of the game
     * @param msg the message that will be displayed in the statusLabel
     */
    void updateStatusMessage(String msg);

    /**
     * Updates the current winners scoreboard
     * @param winner the name of the player who won the round or game
     * @param score the score of the player who won the round or game
     */
    void updateWinner(String winner, int score);

}