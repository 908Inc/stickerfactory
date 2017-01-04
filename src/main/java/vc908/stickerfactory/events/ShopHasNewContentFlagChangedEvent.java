package vc908.stickerfactory.events;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */

public class ShopHasNewContentFlagChangedEvent {
    private boolean isHasNewContent;

    public ShopHasNewContentFlagChangedEvent(boolean isHasNewContent) {
        this.isHasNewContent = isHasNewContent;
    }

    public boolean isHasNewContent() {
        return isHasNewContent;
    }
}
