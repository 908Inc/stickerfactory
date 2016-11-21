package vc908.stickerfactory.events;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class PackMarkStatusChangedEvent {
    private final String packName;
    private final boolean isMarked;

    public PackMarkStatusChangedEvent(String packName, boolean isMarked) {
        this.packName = packName;
        this.isMarked = isMarked;
    }

    public String getPackName() {
        return packName;
    }

    public boolean isMarked() {
        return isMarked;
    }
}
