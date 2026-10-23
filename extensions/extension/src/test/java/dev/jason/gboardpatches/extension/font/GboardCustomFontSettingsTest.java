package dev.jason.gboardpatches.extension.font;

import android.content.Context;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public final class GboardCustomFontSettingsTest {
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        GboardCustomFontSettings.clearAll(context);
    }

    @After
    public void tearDown() {
        GboardCustomFontSettings.clearAll(context);
    }

    @Test
    public void defaultValuesAreClean() {
        Assert.assertFalse("Should be disabled by default", GboardCustomFontSettings.readEnabled(context));
        Assert.assertEquals("Default mode should be emoji_only",
                GboardCustomFontSettings.MODE_EMOJI_ONLY, GboardCustomFontSettings.readMode(context));
        Assert.assertFalse("Should have no emoji font initially", GboardCustomFontSettings.hasEmojiFont(context));
        Assert.assertFalse("Should have no text font initially", GboardCustomFontSettings.hasTextFont(context));
        Assert.assertFalse("Should not be active initially", GboardCustomFontSettings.isCustomFontActive(context));
    }

    @Test
    public void modePersistenceWorks() {
        GboardCustomFontSettings.writeMode(context, GboardCustomFontSettings.MODE_GLOBAL);
        Assert.assertEquals(GboardCustomFontSettings.MODE_GLOBAL, GboardCustomFontSettings.readMode(context));

        GboardCustomFontSettings.writeMode(context, GboardCustomFontSettings.MODE_EMOJI_ONLY);
        Assert.assertEquals(GboardCustomFontSettings.MODE_EMOJI_ONLY, GboardCustomFontSettings.readMode(context));

        // Invalid mode falls back to emoji_only
        GboardCustomFontSettings.writeMode(context, "invalid_mode");
        Assert.assertEquals(GboardCustomFontSettings.MODE_EMOJI_ONLY, GboardCustomFontSettings.readMode(context));
    }

    @Test
    public void enablePersistenceWorks() {
        GboardCustomFontSettings.writeEnabled(context, true);
        Assert.assertTrue(GboardCustomFontSettings.readEnabled(context));

        GboardCustomFontSettings.writeEnabled(context, false);
        Assert.assertFalse(GboardCustomFontSettings.readEnabled(context));
    }

    @Test
    public void formatFileSizeFormatsAccurately() {
        Assert.assertEquals("0 B", GboardCustomFontSettings.formatFileSize(0));
        Assert.assertEquals("500 B", GboardCustomFontSettings.formatFileSize(500));
        Assert.assertEquals("1.5 KB", GboardCustomFontSettings.formatFileSize(1536));
        Assert.assertEquals("2.0 MB", GboardCustomFontSettings.formatFileSize(2 * 1024 * 1024));
    }

    @Test
    public void importRejectsCorruptBytes() {
        byte[] badBytes = new byte[]{1, 2, 3, 4, 5};
        boolean ok = GboardCustomFontSettings.importEmojiFont(context, "bad.ttf", badBytes);
        Assert.assertFalse("Should reject corrupt font data", ok);
        Assert.assertFalse("Should not have emoji font file", GboardCustomFontSettings.hasEmojiFont(context));
    }
}
