import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;

public class UnoModelTest {
    @Test
    void undoSimpleCard() {
        UnoModel model = new UnoModel();
        model.addPlayer("A", false);
        model.addPlayer("B", false);
        Player curr = model.getCurrPlayer();
        Card card = new Card(
                UnoModel.Colours.RED,
                UnoModel.Values.THREE,
                UnoModel.ColoursDark.ORANGE,
                UnoModel.ValuesDark.THREE
        );
        curr.addCard(card);
        model.playCard(card);
        model.undo();
        assertTrue(curr.getPersonalDeck().contains(card));
        assertNull(model.getTopCard());
    }
    @Test
    void redoSimpleCard() {
        UnoModel model = new UnoModel();
        model.addPlayer("A", false);
        model.addPlayer("B", false);
        Player curr = model.getCurrPlayer();
        Card card = new Card(
                UnoModel.Colours.GREEN,
                UnoModel.Values.FOUR,
                UnoModel.ColoursDark.TEAL,
                UnoModel.ValuesDark.FOUR
        );
        curr.addCard(card);
        model.playCard(card);
        model.undo();
        model.redo();
        assertFalse(curr.getPersonalDeck().contains(card));
        assertEquals(card, model.getTopCard());
    }

    @Test
    void undoDrawOneCard() {
        UnoModel model = new UnoModel();
        model.addPlayer("A", false);
        model.addPlayer("B", false);
        Player curr = model.getCurrPlayer();
        Player next = model.getNextPlayer();
        Card drawOne = new Card(
                UnoModel.Colours.YELLOW,
                UnoModel.Values.DRAW_ONE,
                UnoModel.ColoursDark.PINK,
                UnoModel.ValuesDark.ONE
        );
        curr.addCard(drawOne);
        int before = next.getPersonalDeck().size();
        model.playCard(drawOne);
        model.undo();
        assertEquals(before, next.getPersonalDeck().size());
        assertTrue(curr.getPersonalDeck().contains(drawOne));
    }
    @Test
    void undoFlipSide() {
        UnoModel model = new UnoModel();
        model.addPlayer("A", false);
        model.addPlayer("B", false);
        Player curr = model.getCurrPlayer();
        Card flip = new Card(
                UnoModel.Colours.BLUE,
                UnoModel.Values.FLIP,
                UnoModel.ColoursDark.PURPLE,
                UnoModel.ValuesDark.FLIP
        );
        curr.addCard(flip);
        UnoModel.Side start = model.getSide();
        model.playCard(flip);
        model.undo();
        assertEquals(start, model.getSide());
        assertTrue(curr.getPersonalDeck().contains(flip));
    }

    @Test
    void redoFlipSide() {
        UnoModel model = new UnoModel();
        model.addPlayer("A", false);
        model.addPlayer("B", false);
        Player curr = model.getCurrPlayer();
        Card flip = new Card(
                UnoModel.Colours.GREEN,
                UnoModel.Values.FLIP,
                UnoModel.ColoursDark.TEAL,
                UnoModel.ValuesDark.FLIP
        );
        curr.addCard(flip);
        UnoModel.Side start = model.getSide();
        model.playCard(flip);
        model.undo();
        model.redo();
        assertNotEquals(start, model.getSide());
    }

    @Test
    void saveAndLoadKeepsState() throws Exception {
        UnoModel m1 = new UnoModel();
        m1.addPlayer("A", false);
        m1.addPlayer("B", true);
        m1.newRound();
        m1.flip();
        Player winner = m1.getCurrPlayer();
        m1.checkWinner(winner);
        File f = File.createTempFile("uno_save_test", ".dat");
        try {
            m1.saveGame(f);
            UnoModel m2 = new UnoModel();
            m2.loadGame(f);
            assertEquals(m1.getPlayers().size(), m2.getPlayers().size());
            for (int i = 0; i < m1.getPlayers().size(); i++) {
                assertEquals(
                        m1.getPlayers().get(i).getName(),
                        m2.getPlayers().get(i).getName()
                );
            }
            assertEquals(m1.getCurrPlayer().getName(), m2.getCurrPlayer().getName());
            assertEquals(m1.getSide(), m2.getSide());
            assertEquals(m1.getTopCard(), m2.getTopCard());
            assertEquals(m1.getFinalScores(), m2.getFinalScores());
        } finally {
            f.delete();
        }
    }
}
