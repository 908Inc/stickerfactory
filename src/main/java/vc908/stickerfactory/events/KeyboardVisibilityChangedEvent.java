package vc908.stickerfactory.events;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class KeyboardVisibilityChangedEvent {
    private boolean isVisible;

    public KeyboardVisibilityChangedEvent(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }
}
