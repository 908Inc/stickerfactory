package vc908.stickerfactory.events;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class PackTabImageDownloadedEvent {
    private String packName;

    public PackTabImageDownloadedEvent(String packName) {
        this.packName = packName;
    }

    public String getPackName() {
        return packName;
    }
}
