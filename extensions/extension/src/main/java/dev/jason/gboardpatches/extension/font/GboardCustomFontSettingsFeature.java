package dev.jason.gboardpatches.extension.font;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.jason.gboardpatches.extension.R;
import dev.jason.gboardpatches.extension.settings.GboardPatchesFeatureAvailability;
import dev.jason.gboardpatches.extension.settings.GboardPatchesSettingsContract;
import dev.jason.gboardpatches.extension.settings.GboardSettingsText;

public final class GboardCustomFontSettingsFeature implements GboardPatchesSettingsContract.Feature {
    private static final String PREVIEW_ASSET = "settings-previews/customtheme/enabled.png";

    private final String entryTitle;
    private final String entrySummary;

    public GboardCustomFontSettingsFeature(Context context) {
        entryTitle = "自訂字型與現代表情符號";
        entrySummary = "導入外部 Emoji/字型解決缺字與屏蔽，支援應用內隨時匯入並保護中文原生樣式";
    }

    @Override
    public String getEntryTitle() {
        return entryTitle;
    }

    @Override
    public String getEntrySummary() {
        return entrySummary;
    }

    @Override
    public boolean isAvailable(Context context) {
        return GboardPatchesFeatureAvailability.hasFeature(
                context,
                GboardPatchesFeatureAvailability.FEATURE_CUSTOM_FONT
        );
    }

    @Override
    public GboardPatchesSettingsContract.Screen buildScreen(
            GboardPatchesSettingsContract.FeatureHost host) {
        Context context = host.getContext();
        boolean enabled = GboardCustomFontSettings.readEnabled(context);
        boolean hasEmoji = GboardCustomFontSettings.hasEmojiFont(context);
        boolean hasText = GboardCustomFontSettings.hasTextFont(context);
        boolean hasAny = hasEmoji || hasText;
        String mode = GboardCustomFontSettings.readMode(context);
        boolean isEmojiOnly = GboardCustomFontSettings.MODE_EMOJI_ONLY.equals(mode);

        List<GboardPatchesSettingsContract.Row> generalRows = new ArrayList<GboardPatchesSettingsContract.Row>();

        // Row 1: Enable Toggle
        generalRows.add(new GboardPatchesSettingsContract.ToggleRow(
                "啟用自訂字型與 Emoji",
                hasAny
                        ? "在軟鍵盤中套用已匯入的自訂字型或表情符號"
                        : "尚未匯入字型，請先在下方「Emoji 字型檔案」選取字型",
                hasAny,
                enabled,
                value -> {
                    GboardCustomFontSettings.writeEnabled(context, value);
                    GboardPatchesSettingsContract.refresh(host);
                    showToast(context, value ? "已開啟自訂字型，建議重啟鍵盤生效" : "已關閉自訂字型");
                }
        ));

        // Row 2: Mode Selector
        String modeSummary = isEmojiOnly
                ? "僅替換 Emoji，中文、英文、注音 100% 保持系統原生字型排版"
                : "全域替換鍵盤所有文字與字符，適用於特殊手寫或藝術字體";

        generalRows.add(new GboardPatchesSettingsContract.SelectorRow(
                "字型替換範圍與模式",
                modeSummary,
                isEmojiOnly ? "智慧 Emoji 模式 (推薦)" : "全域字型模式",
                enabled && hasAny,
                () -> {
                    String[] labels = new String[]{
                            "智慧 Emoji 模式 (推薦：僅替換 Emoji，完美保護中文原生字型)",
                            "全域字型模式 (替換鍵盤所有文字與字符)"
                    };
                    String[] values = new String[]{
                            GboardCustomFontSettings.MODE_EMOJI_ONLY,
                            GboardCustomFontSettings.MODE_GLOBAL
                    };
                    host.submit(adapter -> adapter.showChoiceDialog(
                            "選擇字型替換範圍",
                            labels,
                            values,
                            mode,
                            null,
                            null,
                            selectedMode -> {
                                GboardCustomFontSettings.writeMode(context, selectedMode);
                                GboardPatchesSettingsContract.refresh(host);
                                showToast(context, "已更新模式，建議重啟鍵盤生效");
                            }
                    ));
                }
        ));

        List<GboardPatchesSettingsContract.Row> slotRows = new ArrayList<GboardPatchesSettingsContract.Row>();

        // Row 3: Emoji Font Slot
        File emojiFile = GboardCustomFontSettings.getEmojiFontFile(context);
        String emojiSummary;
        if (hasEmoji) {
            String name = GboardCustomFontSettings.getEmojiFontDisplayName(context);
            emojiSummary = "已載入：" + name + " (" + GboardCustomFontSettings.formatFileSize(emojiFile.length()) + ")\n點擊可替換新檔案";
        } else {
            emojiSummary = "尚未匯入 Emoji 字型，點擊選取本機 .ttf / .otf 檔案 (推薦 AppleColorEmoji / NotoColorEmoji)";
        }

        slotRows.add(new GboardPatchesSettingsContract.ActionRow(
                "Emoji 字型檔案 (核心必選)",
                emojiSummary,
                true,
                () -> {
                    host.submit(adapter -> adapter.openBinaryDocument(
                            new String[]{"*/*", "font/ttf", "font/otf", "application/x-font-ttf", "application/font-sfnt"},
                            document -> {
                                if (document == null || document.getData() == null) {
                                    showToast(context, "未選取檔案");
                                    return;
                                }
                                boolean success = GboardCustomFontSettings.importEmojiFont(
                                        context,
                                        document.getDisplayName(),
                                        document.getData()
                                );
                                if (success) {
                                    showToast(context, "Emoji 字型匯入成功！點擊下方重啟鍵盤生效");
                                } else {
                                    showToast(context, "字型解析失敗，請確認檔案為有效的 .ttf 或 .otf 格式");
                                }
                                GboardPatchesSettingsContract.refresh(host);
                            }
                    ));
                }
        ));

        // Row 4: Text Font Slot (Optional)
        File textFile = GboardCustomFontSettings.getTextFontFile(context);
        String textSummary;
        if (hasText) {
            String name = GboardCustomFontSettings.getTextFontDisplayName(context);
            textSummary = "已載入：" + name + " (" + GboardCustomFontSettings.formatFileSize(textFile.length()) + ")\n點擊可替換新檔案";
        } else {
            textSummary = "使用 Gboard / 系統原廠字型（推薦），點擊可選取自訂文字字型";
        }

        slotRows.add(new GboardPatchesSettingsContract.ActionRow(
                "文字字型檔案 (進階可選)",
                textSummary,
                true,
                () -> {
                    host.submit(adapter -> adapter.openBinaryDocument(
                            new String[]{"*/*", "font/ttf", "font/otf", "application/x-font-ttf", "application/font-sfnt"},
                            document -> {
                                if (document == null || document.getData() == null) {
                                    showToast(context, "未選取檔案");
                                    return;
                                }
                                boolean success = GboardCustomFontSettings.importTextFont(
                                        context,
                                        document.getDisplayName(),
                                        document.getData()
                                );
                                if (success) {
                                    showToast(context, "文字字型匯入成功！點擊重啟鍵盤生效");
                                } else {
                                    showToast(context, "字型解析失敗，請確認檔案為有效的 .ttf 或 .otf 格式");
                                }
                                GboardPatchesSettingsContract.refresh(host);
                            }
                    ));
                }
        ));

        // Row 5: Clear action
        if (hasAny) {
            slotRows.add(new GboardPatchesSettingsContract.DangerRow(
                    "清除已匯入字型",
                    "刪除本機已匯入的 Emoji 與文字字型，還原為系統原廠字型",
                    true,
                    () -> {
                        GboardCustomFontSettings.clearAll(context);
                        GboardPatchesSettingsContract.refresh(host);
                        showToast(context, "已清除自訂字型並還原為原廠字體");
                    },
                    "確認清除自訂字型？",
                    "這將會刪除 Gboard 私有目錄內儲存的所有外部字型檔案，並恢復為原廠字體。"
            ));
        }

        // Row 6: Restart Gboard
        slotRows.add(new GboardPatchesSettingsContract.ActionRow(
                "立即重啟 Gboard 生效",
                "匯入或切換字型設定後，點擊立即重新載入鍵盤進程",
                true,
                () -> {
                    showToast(context, "正在重啟 Gboard...");
                    host.submit(GboardPatchesSettingsContract.Host::requestTargetRestart);
                }
        ));

        List<GboardPatchesSettingsContract.Section> sections = new ArrayList<GboardPatchesSettingsContract.Section>();
        sections.add(new GboardPatchesSettingsContract.Section("模式與開關", generalRows));
        sections.add(new GboardPatchesSettingsContract.Section("字型檔案管理", slotRows));

        String headerBadge = GboardSettingsText.get(context, R.string.gboard_patches_header_badge);

        return new GboardPatchesSettingsContract.Screen(
                entryTitle,
                headerBadge,
                entryTitle,
                "在應用內隨時匯入或切換 Emoji 與文字字型，智慧回退模式確保中文與排版樣式 100% 完美不變形。",
                Collections.emptyList(),
                sections,
                GboardPatchesSettingsContract.RefreshPolicy.none(),
                GboardPatchesSettingsContract.PanelStyle.FLAT,
                null,
                Collections.emptyList(),
                false
        );
    }

    private static void showToast(Context context, String message) {
        if (context != null && message != null) {
            try {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } catch (Throwable ignored) {
            }
        }
    }
}
