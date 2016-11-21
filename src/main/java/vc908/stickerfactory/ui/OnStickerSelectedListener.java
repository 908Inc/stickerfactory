package vc908.stickerfactory.ui;

/**
 * Listener interface for user interaction with stickers
 *
 * @author Dmitry Nezhydenko
 */
public interface OnStickerSelectedListener {
    void onStickerSelected(String code);

    void onEmojiSelected(String emoji);
}
