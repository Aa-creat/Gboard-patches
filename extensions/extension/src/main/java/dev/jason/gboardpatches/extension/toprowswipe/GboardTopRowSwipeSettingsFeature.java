package dev.jason.gboardpatches.extension.toprowswipe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.jason.gboardpatches.extension.settings.GboardPatchesFeatureAvailability;
import dev.jason.gboardpatches.extension.settings.GboardPatchesSettingsContract;

public final class GboardTopRowSwipeSettingsFeature
        implements GboardPatchesSettingsContract.Feature {
    private static final String TAG = "GboardPatchesTopRow";
    private static final String PREVIEW_VIDEO_ENABLED_ASSET =
            "settings-previews/keyboard/gboard_top_row_swipe_enabled_preview.mp4";
    private static final GboardTopRowSwipeStrings DEFAULT_STRINGS =
            GboardTopRowSwipeStrings.from(null);
    private static final String EXPORT_FILE_NAME = "gboard-top-row-swipe-slots.json";
    private static final String EXPORT_MIME_TYPE = "application/json";
    private static final String[] IMPORT_MIME_TYPES = {
            "application/json",
            "text/json",
            "text/plain"
    };
    private final GboardTopRowSwipeStrings entryStrings;

    public GboardTopRowSwipeSettingsFeature() {
        this(null);
    }

    public GboardTopRowSwipeSettingsFeature(Context context) {
        entryStrings = GboardTopRowSwipeStrings.from(context);
    }

    @Override
    public String getEntryTitle() {
        return entryStrings.entryTitle;
    }

    @Override
    public String getEntrySummary() {
        return entryStrings.entrySummary;
    }

    @Override
    public boolean isAvailable(Context context) {
        return GboardPatchesFeatureAvailability.hasFeature(
                context,
                GboardPatchesFeatureAvailability.FEATURE_TOP_ROW_SWIPE);
    }

    @Override
    public GboardPatchesSettingsContract.Screen buildScreen(
            GboardPatchesSettingsContract.FeatureHost host) {
        Context context = host != null ? host.getContext() : null;
        GboardTopRowSwipeStrings strings = GboardTopRowSwipeStrings.from(context);
        try {
            if (context == null) {
                return buildErrorScreen(strings);
            }
            GboardTopRowSwipeSettings.ensureDefaults(context);
            SharedPreferences preferences = GboardTopRowSwipeSettings.preferences(context);

            boolean enabled = GboardTopRowSwipeSettings.readEnabled(preferences);
            boolean zhuyinEnabled = GboardTopRowSwipeSettings.readZhuyinEnabled(preferences);
            boolean englishQwertyEnabled =
                    GboardTopRowSwipeSettings.readEnglishQwertyEnabled(preferences);
            List<GboardTopRowSwipeSettings.SlotText> slots =
                    GboardTopRowSwipeSettings.readSlots(preferences);
            boolean subordinateRowsEnabled = enabled;

            List<GboardPatchesSettingsContract.Row> behaviorRows =
                    new ArrayList<GboardPatchesSettingsContract.Row>();
            behaviorRows.add(new GboardPatchesSettingsContract.ToggleRow(
                    strings.enabledTitle,
                    strings.enabledSummary,
                    true,
                    enabled,
                    value -> GboardTopRowSwipeSettings.writeEnabled(context, value),
                    buildEnabledPreview(strings)));

            List<GboardPatchesSettingsContract.Row> layoutRows =
                    new ArrayList<GboardPatchesSettingsContract.Row>();
            layoutRows.add(new GboardPatchesSettingsContract.ToggleRow(
                    strings.zhuyinLayoutTitle,
                    strings.zhuyinLayoutSummary,
                    subordinateRowsEnabled,
                    zhuyinEnabled,
                    value -> GboardTopRowSwipeSettings.writeZhuyinEnabled(
                            context, value)));
            layoutRows.add(new GboardPatchesSettingsContract.ToggleRow(
                    strings.englishQwertyLayoutTitle,
                    strings.englishQwertyLayoutSummary,
                    subordinateRowsEnabled,
                    englishQwertyEnabled,
                    value -> GboardTopRowSwipeSettings.writeEnglishQwertyEnabled(
                            context, value)));

            List<GboardPatchesSettingsContract.Row> slotRows =
                    new ArrayList<GboardPatchesSettingsContract.Row>();
            for (int index = 0; index < GboardTopRowSwipeSettings.SLOT_COUNT; index++) {
                GboardTopRowSwipeSettings.SlotText slot = slots.get(index);
                slotRows.add(new GboardPatchesSettingsContract.CommandRow(
                        strings.slotTitle(index),
                        strings.slotSummary(slot),
                        subordinateRowsEnabled,
                        new ShowSlotEditorAction(host, index, slot)));
            }

            List<GboardPatchesSettingsContract.Row> transferRows =
                    new ArrayList<GboardPatchesSettingsContract.Row>();
            transferRows.add(new GboardPatchesSettingsContract.CommandRow(
                    strings.exportTitle,
                    strings.exportSummary,
                    subordinateRowsEnabled,
                    new ExportSlotsAction(host, strings)));
            transferRows.add(new GboardPatchesSettingsContract.CommandRow(
                    strings.importTitle,
                    strings.importSummary,
                    subordinateRowsEnabled,
                    new ImportSlotsAction(host, strings)));

            List<GboardPatchesSettingsContract.Row> advancedRows =
                    new ArrayList<GboardPatchesSettingsContract.Row>();
            advancedRows.add(new GboardPatchesSettingsContract.DangerRow(
                    strings.resetTitle,
                    strings.resetSummary,
                    subordinateRowsEnabled,
                    new ResetSlotsAction(host),
                    strings.resetConfirmTitle,
                    strings.resetConfirmMessage));

            return new GboardPatchesSettingsContract.Screen(
                    strings.entryTitle,
                    strings.headerBadge,
                    strings.entryTitle,
                    strings.headerSummary,
                    Collections.emptyList(),
                    Arrays.asList(
                            new GboardPatchesSettingsContract.Section(
                                    strings.sectionBehavior,
                                    behaviorRows),
                            new GboardPatchesSettingsContract.Section(
                                    strings.sectionLayouts,
                                    layoutRows),
                            new GboardPatchesSettingsContract.Section(
                                    strings.sectionSlots,
                                    slotRows),
                            new GboardPatchesSettingsContract.Section(
                                    strings.sectionTransfer,
                                    transferRows),
                            new GboardPatchesSettingsContract.Section(
                                    strings.sectionAdvanced,
                                    null,
                                    GboardPatchesSettingsContract.SectionStyle.ADVANCED,
                                    advancedRows)),
                    GboardPatchesSettingsContract.RefreshPolicy.none(),
                    GboardPatchesSettingsContract.PanelStyle.FLAT);
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to render Top Row Swipe settings screen", throwable);
            return buildErrorScreen(strings);
        }
    }

    static String slotSummary(GboardTopRowSwipeSettings.SlotText slot) {
        return DEFAULT_STRINGS.slotSummary(slot);
    }

    private static GboardPatchesSettingsContract.PreviewSpec buildEnabledPreview(
            GboardTopRowSwipeStrings strings) {
        return new GboardPatchesSettingsContract.PreviewSpec(
                strings.entryTitle,
                "",
                GboardPatchesSettingsContract.PreviewLayout.STACKED,
                GboardPatchesSettingsContract.PreviewVideo.fromAsset(
                        PREVIEW_VIDEO_ENABLED_ASSET,
                        ""));
    }

    private GboardPatchesSettingsContract.Screen buildErrorScreen(
            GboardTopRowSwipeStrings strings) {
        List<GboardPatchesSettingsContract.StatusBlock> statusBlocks =
                new ArrayList<GboardPatchesSettingsContract.StatusBlock>();
        statusBlocks.add(new GboardPatchesSettingsContract.StatusBlock(
                strings.errorTitle,
                strings.errorSummary,
                GboardPatchesSettingsContract.StatusTone.WARNING));
        return new GboardPatchesSettingsContract.Screen(
                strings.entryTitle,
                strings.headerBadge,
                strings.entryTitle,
                strings.headerSummary,
                statusBlocks,
                Collections.emptyList());
    }

    private static final class ShowSlotEditorAction implements Runnable {
        private final GboardPatchesSettingsContract.FeatureHost host;
        private final int index;
        private final GboardTopRowSwipeSettings.SlotText slot;

        ShowSlotEditorAction(GboardPatchesSettingsContract.FeatureHost host, int index,
                GboardTopRowSwipeSettings.SlotText slot) {
            this.host = host;
            this.index = index;
            this.slot = slot;
        }

        @Override
        public void run() {
            if (host == null || host.getContext() == null) {
                return;
            }
            Context context = host.getContext();
            if (!(context instanceof Activity activity)) {
                return;
            }
            try {
                GboardPatchesSettingsContract.showManagedDialog(host, onDismiss ->
                        GboardTopRowSwipeSlotEditorDialog.show(
                                activity,
                                index,
                                slot,
                                editedSlot -> {
                                    GboardTopRowSwipeSettings.writeSlot(context, index, editedSlot);
                                    safeRefresh(host);
                                },
                                onDismiss));
            } catch (Throwable throwable) {
                Log.w(TAG, "Failed to show Top Row Swipe slot editor", throwable);
            }
        }
    }

    private static final class ResetSlotsAction implements Runnable {
        private final GboardPatchesSettingsContract.FeatureHost host;

        ResetSlotsAction(GboardPatchesSettingsContract.FeatureHost host) {
            this.host = host;
        }

        @Override
        public void run() {
            if (host == null || host.getContext() == null) {
                return;
            }
            resetAllSlots(host.getContext(), host);
        }
    }

    private static final class ExportSlotsAction implements Runnable {
        private final GboardPatchesSettingsContract.FeatureHost host;
        private final GboardTopRowSwipeStrings strings;

        ExportSlotsAction(GboardPatchesSettingsContract.FeatureHost host,
                GboardTopRowSwipeStrings strings) {
            this.host = host;
            this.strings = strings;
        }

        @Override
        public void run() {
            if (host == null || host.getContext() == null) {
                return;
            }
            Context context = host.getContext();
            try {
                List<GboardTopRowSwipeSettings.SlotText> slots =
                        GboardTopRowSwipeSettings.readSlots(context);
                GboardPatchesSettingsContract.createTextDocument(host,
                        EXPORT_FILE_NAME,
                        EXPORT_MIME_TYPE,
                        GboardTopRowSwipeSettings.exportSettings("",
                                GboardTopRowSwipeSettings.defaultJavaScriptRuntimeLimits(), slots),
                        () -> Toast.makeText(
                                context,
                                strings.exportDoneMessage,
                                Toast.LENGTH_SHORT).show());
            } catch (Throwable throwable) {
                Log.w(TAG, "Failed to export Top Row Swipe slots", throwable);
            }
        }
    }

    private static final class ImportSlotsAction implements Runnable {
        private final GboardPatchesSettingsContract.FeatureHost host;
        private final GboardTopRowSwipeStrings strings;

        ImportSlotsAction(GboardPatchesSettingsContract.FeatureHost host,
                GboardTopRowSwipeStrings strings) {
            this.host = host;
            this.strings = strings;
        }

        @Override
        public void run() {
            if (host == null || host.getContext() == null) {
                return;
            }
            Context context = host.getContext();
            if (!(context instanceof Activity activity)) {
                return;
            }
            try {
                GboardPatchesSettingsContract.openTextDocument(host,
                        IMPORT_MIME_TYPES,
                        text -> importSlotsFromText(activity, context, host, text, strings));
            } catch (Throwable throwable) {
                Log.w(TAG, "Failed to import Top Row Swipe slots", throwable);
            }
        }
    }

    private static void safeRefresh(GboardPatchesSettingsContract.FeatureHost host) {
        if (host == null) {
            return;
        }
        try {
            GboardPatchesSettingsContract.refresh(host);
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to refresh Top Row Swipe settings", throwable);
        }
    }

    private static void resetAllSlots(Context context, GboardPatchesSettingsContract.FeatureHost host) {
        try {
            GboardTopRowSwipeSettings.resetSlots(context);
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to reset Top Row Swipe slots", throwable);
        }
        safeRefresh(host);
    }

    private static void importSlotsFromText(Activity activity, Context context,
            GboardPatchesSettingsContract.FeatureHost host, String text,
            GboardTopRowSwipeStrings strings) {
        try {
            GboardTopRowSwipeSettings.ExportedSettings imported =
                    GboardTopRowSwipeSettings.parseExportedSettings(text);
            GboardTopRowSwipeSettings.writeImportedSettings(context, imported);
            Toast.makeText(context, strings.importDoneMessage, Toast.LENGTH_SHORT).show();
            safeRefresh(host);
        } catch (IllegalArgumentException exception) {
            showImportError(host, activity, strings, strings.importInvalidSummary);
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to apply imported Top Row Swipe slots", throwable);
            showImportError(host, activity, strings, strings.importFailedSummary);
        }
    }

    private static void showImportError(GboardPatchesSettingsContract.FeatureHost host,
            Activity activity,
            GboardTopRowSwipeStrings strings,
            String message) {
        showManagedAlertDialog(host, activity, "show Top Row Swipe import error dialog", () ->
                new AlertDialog.Builder(activity)
                        .setTitle(strings.importFailedTitle)
                        .setMessage(message)
                        .setPositiveButton(strings.closeButton, null)
                        .create());
    }

    private interface ManagedAlertDialogFactory {
        AlertDialog create();
    }

    private static void showManagedAlertDialog(
            GboardPatchesSettingsContract.FeatureHost host,
            Activity activity,
            String operationName,
            ManagedAlertDialogFactory factory) {
        GboardPatchesSettingsContract.showManagedDialog(host, onDismiss -> {
            if (activity == null || activity.isFinishing() || factory == null) {
                return false;
            }
            boolean[] shown = { false };
            runUiActionSafely(activity, operationName, () -> {
                AlertDialog dialog = factory.create();
                if (dialog == null) {
                    return;
                }
                dialog.setOnDismissListener(ignored -> onDismiss.run());
                dialog.show();
                shown[0] = true;
            });
            return shown[0];
        });
    }

    private static void runUiActionSafely(Activity activity, String operationName,
            Runnable action) {
        if (action == null || activity == null || activity.isFinishing()) {
            return;
        }
        try {
            action.run();
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to " + operationName, throwable);
        }
    }
}
