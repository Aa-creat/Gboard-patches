package dev.jason.gboardpatches.extension.font;

import android.content.Context;
import android.os.Bundle;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;
import java.util.stream.Collectors;

import dev.jason.gboardpatches.extension.keyboard.GboardKeyboardLayoutSettingsGroupFeature;
import dev.jason.gboardpatches.extension.settings.GboardPatchesFeatureAvailability;
import dev.jason.gboardpatches.extension.settings.GboardPatchesSettingsContract;
import dev.jason.gboardpatches.extension.settings.GboardPatchesSettingsTestHost;

@RunWith(RobolectricTestRunner.class)
public final class GboardCustomFontSettingsFeatureTest {
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        context.getApplicationInfo().metaData = new Bundle();
        GboardCustomFontSettings.clearAll(context);
    }

    @After
    public void tearDown() {
        context.getApplicationInfo().metaData = new Bundle();
        GboardCustomFontSettings.clearAll(context);
    }

    @Test
    public void unavailableWhenFeatureMarkerMissing() {
        GboardCustomFontSettingsFeature feature = new GboardCustomFontSettingsFeature(context);
        Assert.assertFalse("Should be unavailable when marker missing", feature.isAvailable(context));

        GboardPatchesSettingsTestHost host = new GboardPatchesSettingsTestHost(context);
        List<String> visibleTitles = new GboardKeyboardLayoutSettingsGroupFeature(context)
                .buildScreen(host)
                .getRows()
                .stream()
                .map(row -> row.getTitle().toString())
                .collect(Collectors.toList());
        Assert.assertFalse("Should not be visible in layout group when unavailable",
                visibleTitles.contains(feature.getEntryTitle()));
    }

    @Test
    public void availableWhenFeatureMarkerPresent() {
        Bundle meta = new Bundle();
        meta.putBoolean(GboardPatchesFeatureAvailability.FEATURE_CUSTOM_FONT, true);
        context.getApplicationInfo().metaData = meta;

        GboardCustomFontSettingsFeature feature = new GboardCustomFontSettingsFeature(context);
        Assert.assertTrue("Should be available when marker present", feature.isAvailable(context));

        GboardPatchesSettingsTestHost host = new GboardPatchesSettingsTestHost(context);
        List<String> visibleTitles = new GboardKeyboardLayoutSettingsGroupFeature(context)
                .buildScreen(host)
                .getRows()
                .stream()
                .map(row -> row.getTitle().toString())
                .collect(Collectors.toList());
        Assert.assertTrue("Should be visible in layout group when available",
                visibleTitles.contains(feature.getEntryTitle()));
    }

    @Test
    public void screenBuildsCorrectRowsAndToggles() {
        Bundle meta = new Bundle();
        meta.putBoolean(GboardPatchesFeatureAvailability.FEATURE_CUSTOM_FONT, true);
        context.getApplicationInfo().metaData = meta;

        GboardCustomFontSettingsFeature feature = new GboardCustomFontSettingsFeature(context);
        GboardPatchesSettingsTestHost host = new GboardPatchesSettingsTestHost(context);
        GboardPatchesSettingsContract.Screen screen = feature.buildScreen(host);

        Assert.assertNotNull(screen);
        Assert.assertEquals("自訂字型與現代表情符號", screen.getToolbarTitle());
        Assert.assertEquals(2, screen.getSections().size());

        List<GboardPatchesSettingsContract.Row> rows = screen.getSections().stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.toList());
        Assert.assertTrue("Should contain at least 4 rows", rows.size() >= 4);

        List<String> titles = rows.stream().map(r -> r.getTitle().toString()).collect(Collectors.toList());
        Assert.assertTrue(titles.contains("啟用自訂字型與 Emoji"));
        Assert.assertTrue(titles.contains("字型替換範圍與模式"));
        Assert.assertTrue(titles.contains("Emoji 字型檔案 (核心必選)"));
        Assert.assertTrue(titles.contains("文字字型檔案 (進階可選)"));
        Assert.assertTrue(titles.contains("立即重啟 Gboard 生效"));
    }
}
