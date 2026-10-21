package dev.jason.gboardpatches.extension.toprowswipe;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GboardTopRowSwipeDialogSafetySourceTest {
    @Test
    public void settingsFeatureWrapsDialogAndClipboardCallbacks() throws Exception {
        String source = readSource(
                "src/main/java/dev/jason/gboardpatches/extension/toprowswipe/"
                        + "GboardTopRowSwipeSettingsFeature.java");

        Assert.assertTrue(source.contains("private static void runUiActionSafely("));
        Assert.assertTrue(source.contains("\"show Top Row Swipe import error dialog\""));
    }

    @Test
    public void slotEditorWrapsOnShowAndEditorCallbacks() throws Exception {
        String source = readSource(
                "src/main/java/dev/jason/gboardpatches/extension/toprowswipe/"
                        + "GboardTopRowSwipeSlotEditorDialog.java");

        Assert.assertTrue(source.contains("private static void runUiActionSafely("));
        Assert.assertTrue(source.contains("\"show slot editor dialog\""));
        Assert.assertTrue(source.contains("\"save slot editor dialog\""));
        Assert.assertTrue(source.contains("\"reset slot editor dialog\""));
    }

    private static String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Path.of(path)), StandardCharsets.UTF_8);
    }
}
