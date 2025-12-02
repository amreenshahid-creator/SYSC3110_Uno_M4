import java.util.EventObject;

public class UnoEvent extends EventObject {

    private final UnoModel model;

    public UnoEvent(UnoModel model) {
        super(model);
        this.model = model;
    }
}
