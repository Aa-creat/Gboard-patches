package dev.jason.gboardpatches.extension.toprowswipe;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

final class GboardTopRowSwipeSlotEditorDialog {
    private static final String TAG = "GboardTopRowSwipe";

    interface SaveCallback {
        void onSave(GboardTopRowSwipeSettings.SlotText slot);
    }

    private GboardTopRowSwipeSlotEditorDialog() {
    }

    static boolean show(Activity activity, int slotIndex,
            GboardTopRowSwipeSettings.SlotText currentSlot,
            SaveCallback saveCallback,
            Runnable onDismiss) {
        if (activity == null || activity.isFinishing() || currentSlot == null) {
            return false;
        }

        GboardTopRowSwipeStrings strings = GboardTopRowSwipeStrings.from(activity);
        boolean[] shown = { false };
        runUiActionSafely(activity, "show slot editor dialog", () -> {
            Controller controller = new Controller(activity, currentSlot, strings);
            ScrollView scrollView = new ScrollView(activity);
            scrollView.addView(controller.buildContentView(), new ScrollView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(strings.slotTitle(slotIndex))
                    .setView(scrollView)
                    .setPositiveButton(strings.saveButton, null)
                    .setNegativeButton(strings.cancelButton, null)
                    .setNeutralButton(strings.resetButton, null)
                    .create();
            dialog.setOnShowListener(ignored -> runUiActionSafely(
                    activity,
                    "configure slot editor dialog",
                    () -> {
                        if (dialog.getWindow() != null) {
                            dialog.getWindow().clearFlags(
                                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                            dialog.getWindow().setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        }
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setOnClickListener(view -> runUiActionSafely(
                                        activity,
                                        "save slot editor dialog",
                                        () -> {
                                            GboardTopRowSwipeSettings.SlotText edited =
                                                    controller.editedSlot();
                                            if (edited == null) {
                                                return;
                                            }
                                            if (saveCallback != null) {
                                                saveCallback.onSave(edited);
                                            }
                                            dialog.dismiss();
                                        }));
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                                .setOnClickListener(view -> runUiActionSafely(
                                        activity,
                                        "reset slot editor dialog",
                                        () -> controller.resetToDefaultSlot(slotIndex)));
                    }));
            dialog.setOnDismissListener(ignored -> {
                if (onDismiss != null) {
                    onDismiss.run();
                }
            });
            dialog.show();
            shown[0] = true;
        });
        return shown[0];
    }

    private static final class Controller {
        private final Activity activity;
        private final GboardTopRowSwipeSettings.SlotText currentSlot;
        private final GboardTopRowSwipeStrings strings;
        private boolean commitManuallyEdited;
        private boolean updatingCommitFromDisplay;

        private EditText displayInput;
        private EditText commitInput;
        private TextView commitLabel;

        Controller(Activity activity, GboardTopRowSwipeSettings.SlotText currentSlot,
                GboardTopRowSwipeStrings strings) {
            this.activity = activity;
            this.currentSlot = currentSlot;
            this.strings = strings;
            this.commitManuallyEdited =
                    !safeTrim(currentSlot.displayText).equals(safeTrim(currentSlot.commitText));
        }

        LinearLayout buildContentView() {
            LinearLayout container = new LinearLayout(activity);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(dp(24), dp(8), dp(24), dp(24));

            displayInput = buildInput(strings.editorDisplayHint, currentSlot.displayText);
            commitInput = buildInput(strings.editorCommitHint, currentSlot.commitText);

            container.addView(buildLabel(strings.editorDisplayLabel));
            container.addView(displayInput, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            commitLabel = buildLabel(strings.editorCommitLabel);
            container.addView(commitLabel);
            container.addView(commitInput, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            displayInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence value, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence value, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable value) {
                    if (commitManuallyEdited || commitInput == null) {
                        return;
                    }
                    updatingCommitFromDisplay = true;
                    commitInput.setText(value != null ? value.toString() : "");
                    commitInput.setSelection(commitInput.getText().length());
                    updatingCommitFromDisplay = false;
                }
            });
            commitInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence value, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence value, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable value) {
                    if (!updatingCommitFromDisplay) {
                        commitManuallyEdited = commitManuallyEditedAfterUserEdit(value);
                    }
                }
            });

            displayInput.requestFocus();
            displayInput.setSelection(displayInput.getText().length());
            return container;
        }

        GboardTopRowSwipeSettings.SlotText editedSlot() {
            String display = displayInput != null ? displayInput.getText().toString() : "";
            if (display.trim().isEmpty()) {
                if (displayInput != null) {
                    displayInput.setError(strings.editorDisplayBlankError);
                }
                return null;
            }
            String commit = commitInput != null ? commitInput.getText().toString() : "";
            return slotFromEditorFields(
                    currentSlot,
                    false,
                    display,
                    commit,
                    "",
                    "",
                    commitManuallyEdited);
        }

        void resetToDefaultSlot(int slotIndex) {
            applySlotDraft(GboardTopRowSwipeSettings.defaultSlot(slotIndex));
        }

        private void applySlotDraft(GboardTopRowSwipeSettings.SlotText slot) {
            GboardTopRowSwipeSettings.SlotText safeSlot = slot != null
                    ? slot
                    : new GboardTopRowSwipeSettings.SlotText("", "");
            String display = safeSlot.displayText != null ? safeSlot.displayText : "";
            String commit = safeSlot.commitText != null ? safeSlot.commitText : "";

            updatingCommitFromDisplay = true;
            if (displayInput != null) {
                displayInput.setText(display);
                displayInput.setError(null);
                displayInput.setSelection(displayInput.getText().length());
            }
            if (commitInput != null) {
                commitInput.setText(commit);
                commitInput.setSelection(commitInput.getText().length());
            }
            updatingCommitFromDisplay = false;
            commitManuallyEdited = !safeTrim(display).equals(safeTrim(commit));
        }

        private EditText buildInput(String hint, String value) {
            EditText input = new EditText(activity);
            input.setSingleLine(false);
            input.setMinLines(1);
            input.setMaxLines(3);
            input.setHint(hint);
            input.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            input.setText(value != null ? value : "");
            input.setSelectAllOnFocus(false);
            return input;
        }

        private TextView buildLabel(String text) {
            TextView label = new TextView(activity);
            label.setText(text);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = dp(12);
            label.setLayoutParams(params);
            return label;
        }

        private int dp(int value) {
            float density = activity.getResources().getDisplayMetrics().density;
            return Math.round(value * density);
        }

        private static String safeTrim(String value) {
            return value != null ? value.trim() : "";
        }
    }

    static GboardTopRowSwipeSettings.SlotText slotFromEditorFields(
            GboardTopRowSwipeSettings.SlotText currentSlot,
            boolean useJavaScript,
            String display,
            String commit,
            String script,
            String timeout,
            boolean commitManuallyEdited) {
        return slotFromEditorFields(currentSlot, useJavaScript, display, commit, script,
                timeout, commitManuallyEdited,
                GboardTopRowSwipeSettings.DEFAULT_TIMEOUT_MAX_MS);
    }

    static GboardTopRowSwipeSettings.SlotText slotFromEditorFields(
            GboardTopRowSwipeSettings.SlotText currentSlot,
            boolean useJavaScript,
            String display,
            String commit,
            String script,
            String timeout,
            boolean commitManuallyEdited,
            int timeoutMaxMs) {
        return GboardTopRowSwipeSettings.resolveEditedSlot(
                currentSlot != null ? currentSlot.displayText : "",
                currentSlot != null ? currentSlot.commitText : "",
                display,
                commit,
                commitManuallyEdited);
    }

    static boolean commitManuallyEditedAfterUserEdit(CharSequence value) {
        return value != null && !value.toString().trim().isEmpty();
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
