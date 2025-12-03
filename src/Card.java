/**
 * Immutable-ish card record (mutable colour for wild recolouring).
 * <p>
 * For WILD/WILD_DRAW_TWO, colour may be null until chosen by a player.
 */
import java.io.Serializable;

public class Card implements Serializable {
    private static final long serialVersionUID = 1L;
    private UnoModel.Colours colour;             // can be reassigned for wilds via wild()/wildDrawTwo()
    private final UnoModel.Values value;
    private UnoModel.ColoursDark colourDark;
    private final UnoModel.ValuesDark valueDark;

    /**
     * Creates a card with the given colour and value.
     * For wilds, pass null for colour.
     */
    public Card(UnoModel.Colours colour, UnoModel.Values value, UnoModel.ColoursDark colourDark, UnoModel.ValuesDark valueDark) {
        this.colour = colour;
        this.value = value;
        this.colourDark = colourDark;
        this.valueDark = valueDark;
    }

    /** @return current colour on the light side; may be null for wilds until chosen */
    public UnoModel.Colours getColour() {
        return colour;
    }

    /** @return current colour on the dark side; may be null for wilds until chosen */
    public UnoModel.ColoursDark getColourDark() {
        return colourDark;
    }

    /** @return value of the card on the light side (number/action/wild) */
    public UnoModel.Values getValue() {
        return value;
    }

    /** @return value of the card on the dark side (number/action/wild) */
    public UnoModel.ValuesDark getValueDark() {
        return valueDark;
    }

    /** Assigns a new colour for the light side (used when playing a wild). */
    public void setColour(UnoModel.Colours colour) {
        this.colour = colour;
    }

    /** Assigns a new colour for the dark side (used when playing a wild stack card). */
    public void setColourDark(UnoModel.ColoursDark colourDark) {
        this.colourDark = colourDark;
    }

    /**
     * @return image file name for this card (assumes resources in /light_cards or /dark_cards depending on side).
     * Wilds do not include colour in the file name.
     */
    public String getFileName(UnoModel.Side side) {

        if(side == UnoModel.Side.LIGHT) {
            if(value == UnoModel.Values.WILD || value == UnoModel.Values.WILD_DRAW_TWO) {
                return "light_cards/" + value + ".png";
            }
            return "light_cards/" + colour.toString() + "_" + value.toString() + ".png";
        }

        else if(side == UnoModel.Side.DARK) {
            if(valueDark == UnoModel.ValuesDark.WILD_STACK) {
                return "dark_cards/" + valueDark + ".png";
            }
            return "dark_cards/" + colourDark.toString() + "_" + valueDark.toString() + ".png";
        }

        return "";
    }

    /**
     * Logical equality: same colour and value.
     * (Note: no hashCode override—avoid using as hash keys unless added.)
     */
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if (!(o instanceof Card other)) {
            return false;
        }
        return this.colour == other.colour && this.value == other.value && this.colourDark == other.colourDark && this.valueDark == other.valueDark;
    }
}