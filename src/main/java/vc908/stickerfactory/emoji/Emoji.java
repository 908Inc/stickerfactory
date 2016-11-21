package vc908.stickerfactory.emoji;

/**
 * Class-helper for creating emoji strings from codepoints, char and chars
 *
 * @author Dmytro Nezhydenko
 */
public class Emoji {
    private String emojiString;

    private Emoji() {
    }

    public static Emoji fromCodePoint(int codePoint) {
        Emoji emoji = new Emoji();
        emoji.emojiString = newString(codePoint);
        return emoji;
    }

    public static Emoji fromChar(char ch) {
        Emoji emoji = new Emoji();
        emoji.emojiString = Character.toString(ch);
        return emoji;
    }

    public static Emoji fromChars(String chars) {
        Emoji emoji = new Emoji();
        emoji.emojiString = chars;
        return emoji;
    }

    public Emoji(String emoji) {
        this.emojiString = emoji;
    }

    public String getEmoji() {
        return emojiString;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Emoji && emojiString.equals(((Emoji) o).emojiString);
    }

    @Override
    public int hashCode() {
        return emojiString.hashCode();
    }

    public static String newString(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(Character.toChars(codePoint));
        }
    }
}
