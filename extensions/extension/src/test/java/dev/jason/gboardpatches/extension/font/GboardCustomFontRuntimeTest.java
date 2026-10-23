package dev.jason.gboardpatches.extension.font;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public final class GboardCustomFontRuntimeTest {
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        GboardCustomFontRuntime.resetForTesting();
    }

    @After
    public void tearDown() {
        GboardCustomFontRuntime.resetForTesting();
    }

    @Test
    public void detectsEmojiCodePointsAccurately() {
        Assert.assertTrue(GboardCustomFontRuntime.containsEmoji("😀"));
        Assert.assertTrue(GboardCustomFontRuntime.containsEmoji("Hello 😂 World"));
        Assert.assertTrue(GboardCustomFontRuntime.containsEmoji("🇹🇼"));
        Assert.assertTrue(GboardCustomFontRuntime.containsEmoji("❤️"));
        Assert.assertTrue(GboardCustomFontRuntime.containsEmoji("🚀"));

        Assert.assertFalse(GboardCustomFontRuntime.containsEmoji("Hello"));
        Assert.assertFalse(GboardCustomFontRuntime.containsEmoji("12345"));
        Assert.assertFalse(GboardCustomFontRuntime.containsEmoji("你好世界"));
        Assert.assertFalse(GboardCustomFontRuntime.containsEmoji("注音 ㄅㄆㄇㄈ"));
        Assert.assertFalse(GboardCustomFontRuntime.containsEmoji(""));
        Assert.assertFalse(GboardCustomFontRuntime.containsEmoji(null));
    }

    @Test
    public void emojiOnlyModeKeepsOriginalTypefaceForPlainAlphaNumericKeys() {
        TextView textView = new TextView(context);
        textView.setText("Q");
        Typeface stockTypeface = Typeface.DEFAULT_BOLD;
        Typeface customMockTypeface = Typeface.MONOSPACE;

        GboardCustomFontRuntime.setCustomTypefaceForTesting(customMockTypeface);
        GboardCustomFontRuntime.sEmojiOnlyMode = true;

        Typeface resolved = GboardCustomFontRuntime.resolveTypeface(textView, stockTypeface);
        Assert.assertSame("Should keep stock typeface for alphanumeric key", stockTypeface, resolved);
    }

    @Test
    public void emojiOnlyModeKeepsOriginalTypefaceForEmptyOrUnsetText() {
        TextView textView = new TextView(context);
        textView.setText("");
        Typeface stockTypeface = Typeface.DEFAULT_BOLD;
        Typeface customMockTypeface = Typeface.MONOSPACE;

        GboardCustomFontRuntime.setCustomTypefaceForTesting(customMockTypeface);
        GboardCustomFontRuntime.sEmojiOnlyMode = true;

        Typeface resolved = GboardCustomFontRuntime.resolveTypeface(textView, stockTypeface);
        Assert.assertSame("Should keep stock typeface for empty/unset text", stockTypeface, resolved);
    }

    @Test
    public void emojiOnlyModeAppliesCustomFontWhenEmojiPresent() {
        TextView textView = new TextView(context);
        textView.setText("🎉");
        Typeface stockTypeface = Typeface.DEFAULT_BOLD;
        Typeface customMockTypeface = Typeface.MONOSPACE;

        GboardCustomFontRuntime.setCustomTypefaceForTesting(customMockTypeface);
        GboardCustomFontRuntime.sEmojiOnlyMode = true;

        Typeface resolved = GboardCustomFontRuntime.resolveTypeface(textView, stockTypeface);
        Assert.assertSame("Should apply custom typeface for emoji", customMockTypeface, resolved);
    }

    @Test
    public void globalModeAppliesCustomFontToAllText() {
        TextView textView = new TextView(context);
        textView.setText("W");
        Typeface stockTypeface = Typeface.DEFAULT_BOLD;
        Typeface customMockTypeface = Typeface.MONOSPACE;

        GboardCustomFontRuntime.setCustomTypefaceForTesting(customMockTypeface);
        GboardCustomFontRuntime.sEmojiOnlyMode = false;

        Typeface resolved = GboardCustomFontRuntime.resolveTypeface(textView, stockTypeface);
        Assert.assertSame("Should apply custom typeface when emojiOnly is disabled", customMockTypeface, resolved);
    }

    @Test
    public void applyTypefaceToPaintSetsTypeface() {
        android.graphics.Paint paint = new android.graphics.Paint();
        Typeface customMockTypeface = Typeface.MONOSPACE;
        GboardCustomFontRuntime.setCustomTypefaceForTesting(customMockTypeface);

        GboardCustomFontRuntime.applyTypefaceToPaint(paint);
        Assert.assertSame("applyTypefaceToPaint should configure typeface on paint",
                customMockTypeface, paint.getTypeface());
    }

    @Test
    public void applyTypefaceToKeyInEmojiModeAppliesOnlyToEmoji() {
        TextView emojiKey = new TextView(context);
        TextView letterKey = new TextView(context);
        Typeface stockTypeface = Typeface.DEFAULT_BOLD;
        Typeface customMockTypeface = Typeface.MONOSPACE;

        emojiKey.setTypeface(stockTypeface);
        letterKey.setTypeface(stockTypeface);

        GboardCustomFontRuntime.setCustomTypefaceForTesting(customMockTypeface);
        GboardCustomFontRuntime.sEmojiOnlyMode = true;

        // Simulate active font
        GboardCustomFontSettings.writeEnabled(context, true);

        GboardCustomFontRuntime.applyTypefaceToKey(emojiKey, "😀");
        Assert.assertSame("Emoji key should receive custom emoji typeface",
                customMockTypeface, emojiKey.getTypeface());

        GboardCustomFontRuntime.applyTypefaceToKey(letterKey, "A");
        Assert.assertSame("Letter key should retain stock typeface in emoji-only mode",
                stockTypeface, letterKey.getTypeface());
    }
}
