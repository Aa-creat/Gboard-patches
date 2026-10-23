package dev.jason.gboardpatches.extension.longpressquickactions;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.util.TypedValue;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Future;

public final class GboardLongPressQuickActions1803ReflectionHandles {
    private static final String SOFT_KEY_VIEW_CLASS =
            "com.google.android.libraries.inputmethod.widgets.SoftKeyView";
    private static final String ACTION_SET_CLASS =
            "com.google.android.libraries.inputmethod.metadata.SoftKeyDef";
    private static final String ACTION_TYPE_CLASS = "pmy";
    private static final String ACTION_DEF_CLASS =
            "com.google.android.libraries.inputmethod.metadata.ActionDef";
    private static final String ACTION_ENTRY_CLASS = "pnu";
    private static final String ACTION_BUILDER_CLASS = "pmz";
    private static final String METADATA_BUILDER_CLASS = "ppo";
    private static final String INTENTION_CLASS = "pnt";
    private static final String EVENT_CLASS = "nur";
    private static final String INPUT_METHOD_SERVICE_CLASS = "oup";
    private static final String POINTER_TRACKER_CLASS = "pvi";

    private static final int SINGLE_ICON_POPUP_LAYOUT_ATTR_ID = 0x7f040288;

    public final Method softKeyBindMethod;
    public final Method inputEventMethod;
    public final Method pointerOwnerMethod;

    private final Field softKeyMetadataField;
    private final Field keyIdField;
    private final Method exactActionLookupMethod;
    private final Field actionEntriesField;
    private final Field popupLabelsField;
    private final Field popupIconsField;
    private final Field entryKeycodeField;
    private final Field entryPayloadField;
    private final Constructor<?> entryConstructor;
    private final Constructor<?> actionBuilderConstructor;
    private final Field actionBuilderTypeField;
    private final Field actionBuilderEntriesField;
    private final Method actionBuilderEntriesMethod;
    private final Field actionBuilderLabelsField;
    private final Method actionBuilderLabelsMethod;
    private final Field actionBuilderIconsField;
    private final Method actionBuilderIconsMethod;
    private final Field actionBuilderPopupLayoutField;
    private final Method copyActionMethod;
    private final Method buildActionMethod;
    private final Method scheduleLongPressMethod;
    private volatile Field longPressFutureField;
    private final Constructor<?> metadataBuilderConstructor;
    private final Method copyMetadataMethod;
    private final Method putActionMethod;
    private final Method buildMetadataMethod;
    private final Field eventActionTypeField;
    private final Field eventEntriesField;
    private final Field eventMetadataField;
    private volatile Field eventSubtypeField;
    private final Class<?> actionEntryClass;
    private final Object pressActionType;
    private final Object longPressActionType;

    public GboardLongPressQuickActions1803ReflectionHandles(ClassLoader classLoader)
            throws Throwable {
        boolean is1854 = false;
        try {
            classLoader.loadClass("agrt");
            is1854 = true;
        } catch (ClassNotFoundException ignored) {
        }

        if (is1854) {
            Class<?> softKeyViewClass = resolve(classLoader, SOFT_KEY_VIEW_CLASS);
            Class<?> actionSetClass = resolve(classLoader, ACTION_SET_CLASS);
            Class<?> actionTypeClass = resolve(classLoader, "agcu");
            Class<?> actionDefClass = resolve(classLoader, ACTION_DEF_CLASS);
            actionEntryClass = resolve(classLoader, "agen");
            Class<?> actionBuilderClass = resolve(classLoader, "agcy");
            Class<?> metadataBuilderClass = resolve(classLoader, "agih");
            Class<?> intentionClass = resolve(classLoader, "agem");
            Class<?> eventClass = resolve(classLoader, "acwt");
            Class<?> inputMethodServiceClass = resolve(classLoader,
                    "com.google.android.apps.inputmethod.libs.latin5.LatinIme");
            Class<?> pointerTrackerClass = resolve(classLoader, "agrt");

            softKeyBindMethod = softKeyViewClass.getDeclaredMethod("s", actionSetClass, long.class);
            inputEventMethod = inputMethodServiceClass.getDeclaredMethod("p", eventClass);
            pointerOwnerMethod = pointerTrackerClass.getDeclaredMethod(
                    "B", softKeyViewClass, float.class, float.class, long.class, int.class);
            validateInputEventBinding(inputMethodServiceClass, inputEventMethod);

            softKeyMetadataField = softKeyViewClass.getDeclaredField("e");
            keyIdField = actionSetClass.getDeclaredField("g");
            exactActionLookupMethod = actionSetClass.getDeclaredMethod("h", actionTypeClass);

            actionEntriesField = actionDefClass.getDeclaredField("g");
            popupLabelsField = actionDefClass.getDeclaredField("q");
            popupIconsField = actionDefClass.getDeclaredField("r");
            entryKeycodeField = actionEntryClass.getDeclaredField("d");
            entryPayloadField = actionEntryClass.getDeclaredField("f");
            entryConstructor = actionEntryClass.getDeclaredConstructor(
                    int.class, intentionClass, Object.class, int.class);

            actionBuilderConstructor = actionBuilderClass.getDeclaredConstructor();
            actionBuilderTypeField = actionBuilderClass.getDeclaredField("b");
            actionBuilderEntriesField = null;
            actionBuilderEntriesMethod = actionBuilderClass.getDeclaredMethod(
                    "n", Array.newInstance(actionEntryClass, 0).getClass());
            actionBuilderLabelsField = null;
            actionBuilderLabelsMethod = actionBuilderClass.getDeclaredMethod("t", String[].class);
            actionBuilderIconsField = null;
            actionBuilderIconsMethod = actionBuilderClass.getDeclaredMethod("o", int[].class);
            actionBuilderPopupLayoutField = actionBuilderClass.getDeclaredField("h");
            copyActionMethod = actionBuilderClass.getDeclaredMethod("j", actionDefClass);
            buildActionMethod = actionBuilderClass.getDeclaredMethod("c");
            scheduleLongPressMethod = pointerTrackerClass.getDeclaredMethod("y");

            metadataBuilderConstructor = metadataBuilderClass.getDeclaredConstructor();
            copyMetadataMethod = metadataBuilderClass.getDeclaredMethod("m", actionSetClass);
            putActionMethod = metadataBuilderClass.getDeclaredMethod("v", actionDefClass);
            buildMetadataMethod = metadataBuilderClass.getDeclaredMethod("b");

            eventActionTypeField = eventClass.getDeclaredField("a");
            eventEntriesField = eventClass.getDeclaredField("b");
            eventMetadataField = eventClass.getDeclaredField("c");

            pressActionType = enumValue(actionTypeClass, "PRESS");
            longPressActionType = enumValue(actionTypeClass, "LONG_PRESS");

            setAccessible(
                    softKeyBindMethod,
                    inputEventMethod,
                    pointerOwnerMethod,
                    softKeyMetadataField,
                    keyIdField,
                    exactActionLookupMethod,
                    actionEntriesField,
                    popupLabelsField,
                    popupIconsField,
                    entryKeycodeField,
                    entryPayloadField,
                    entryConstructor,
                    actionBuilderConstructor,
                    actionBuilderTypeField,
                    actionBuilderEntriesMethod,
                    actionBuilderLabelsMethod,
                    actionBuilderIconsMethod,
                    actionBuilderPopupLayoutField,
                    copyActionMethod,
                    buildActionMethod,
                    scheduleLongPressMethod,
                    metadataBuilderConstructor,
                    copyMetadataMethod,
                    putActionMethod,
                    buildMetadataMethod,
                    eventActionTypeField,
                    eventEntriesField,
                    eventMetadataField);
        } else {
            Class<?> softKeyViewClass = resolve(classLoader, SOFT_KEY_VIEW_CLASS);
            Class<?> actionSetClass = resolve(classLoader, ACTION_SET_CLASS);
            Class<?> actionTypeClass = resolve(classLoader, ACTION_TYPE_CLASS);
            Class<?> actionDefClass = resolve(classLoader, ACTION_DEF_CLASS);
            actionEntryClass = resolve(classLoader, ACTION_ENTRY_CLASS);
            Class<?> actionBuilderClass = resolve(classLoader, ACTION_BUILDER_CLASS);
            Class<?> metadataBuilderClass = resolve(classLoader, METADATA_BUILDER_CLASS);
            Class<?> intentionClass = resolve(classLoader, INTENTION_CLASS);
            Class<?> eventClass = resolve(classLoader, EVENT_CLASS);
            Class<?> inputMethodServiceClass = resolve(classLoader, INPUT_METHOD_SERVICE_CLASS);
            Class<?> pointerTrackerClass = resolve(classLoader, POINTER_TRACKER_CLASS);

            softKeyBindMethod = softKeyViewClass.getDeclaredMethod(
                    "r", actionSetClass, long.class);
            inputEventMethod = inputMethodServiceClass.getDeclaredMethod("au", eventClass);
            pointerOwnerMethod = pointerTrackerClass.getDeclaredMethod(
                    "B", softKeyViewClass, float.class, float.class, long.class, int.class);
            validateInputEventBinding(inputMethodServiceClass, inputEventMethod);

            softKeyMetadataField = softKeyViewClass.getDeclaredField("e");
            keyIdField = actionSetClass.getDeclaredField("d");
            actionSetClass.getDeclaredField("f").setAccessible(true);
            actionSetClass.getDeclaredField("g").setAccessible(true);
            exactActionLookupMethod = actionSetClass.getDeclaredMethod("h", actionTypeClass);

            actionDefClass.getDeclaredField("c").setAccessible(true);
            actionEntriesField = actionDefClass.getDeclaredField("d");
            actionDefClass.getDeclaredField("g").setAccessible(true);
            popupLabelsField = actionDefClass.getDeclaredField("n");
            popupIconsField = actionDefClass.getDeclaredField("o");
            entryKeycodeField = actionEntryClass.getDeclaredField("c");
            entryPayloadField = actionEntryClass.getDeclaredField("e");
            entryConstructor = actionEntryClass.getDeclaredConstructor(
                    int.class, intentionClass, Object.class, int.class);

            actionBuilderConstructor = actionBuilderClass.getDeclaredConstructor();
            actionBuilderTypeField = actionBuilderClass.getDeclaredField("a");
            actionBuilderEntriesField = actionBuilderClass.getDeclaredField("b");
            actionBuilderEntriesMethod = null;
            actionBuilderLabelsField = actionBuilderClass.getDeclaredField("c");
            actionBuilderLabelsMethod = null;
            actionBuilderIconsField = actionBuilderClass.getDeclaredField("d");
            actionBuilderIconsMethod = null;
            actionBuilderPopupLayoutField = actionBuilderClass.getDeclaredField("g");
            copyActionMethod = actionBuilderClass.getDeclaredMethod("j", actionDefClass);
            buildActionMethod = actionBuilderClass.getDeclaredMethod("c");
            scheduleLongPressMethod = pointerTrackerClass.getDeclaredMethod("y");

            metadataBuilderConstructor = metadataBuilderClass.getDeclaredConstructor();
            copyMetadataMethod = metadataBuilderClass.getDeclaredMethod("j", actionSetClass);
            putActionMethod = metadataBuilderClass.getDeclaredMethod("t", actionDefClass);
            buildMetadataMethod = metadataBuilderClass.getDeclaredMethod("d");

            eventActionTypeField = eventClass.getDeclaredField("a");
            eventEntriesField = eventClass.getDeclaredField("b");
            eventMetadataField = eventClass.getDeclaredField("c");

            pressActionType = enumValue(actionTypeClass, "PRESS");
            longPressActionType = enumValue(actionTypeClass, "LONG_PRESS");

            setAccessible(
                    softKeyBindMethod,
                    inputEventMethod,
                    pointerOwnerMethod,
                    softKeyMetadataField,
                    keyIdField,
                    exactActionLookupMethod,
                    actionEntriesField,
                    popupLabelsField,
                    popupIconsField,
                    entryKeycodeField,
                    entryPayloadField,
                    entryConstructor,
                    actionBuilderConstructor,
                    actionBuilderTypeField,
                    actionBuilderEntriesField,
                    actionBuilderLabelsField,
                    actionBuilderIconsField,
                    actionBuilderPopupLayoutField,
                    copyActionMethod,
                    buildActionMethod,
                    scheduleLongPressMethod,
                    metadataBuilderConstructor,
                    copyMetadataMethod,
                    putActionMethod,
                    buildMetadataMethod,
                    eventActionTypeField,
                    eventEntriesField,
                    eventMetadataField);
        }
    }

    public Object extractSoftKeyMetadata(Object softKeyView) throws IllegalAccessException {
        return softKeyView == null ? null : softKeyMetadataField.get(softKeyView);
    }

    public int extractKeyId(Object metadata) throws IllegalAccessException {
        return metadata == null ? 0 : keyIdField.getInt(metadata);
    }

    public String extractPressText(Object metadata) throws Throwable {
        Object pressAction = findExactAction(metadata, pressActionType);
        Object[] entries = extractEntries(pressAction);
        if (entries.length == 0 || entries[0] == null) {
            return null;
        }
        Object payload = entryPayloadField.get(entries[0]);
        return payload instanceof CharSequence ? payload.toString() : null;
    }

    public int extractPressCarrierCode(Object metadata) throws Throwable {
        return firstEntryCode(findExactAction(metadata, pressActionType));
    }

    public int[] extractLongPressCodes(Object metadata) throws Throwable {
        Object longPressAction = findExactAction(metadata, longPressActionType);
        Object[] entries = extractEntries(longPressAction);
        int[] result = new int[entries.length];
        for (int index = 0; index < entries.length; index++) {
            result[index] = entries[index] == null ? 0 : entryKeycodeField.getInt(entries[index]);
        }
        return result;
    }

    public Object appendShiftChordActions(Object metadata, int downKeycode,
            Object downPayload, int upKeycode) throws Throwable {
        Object downActionType = enumValue(pressActionType.getClass(), "DOWN");
        Object upActionType = enumValue(pressActionType.getClass(), "UP");
        if (metadata == null
                || findExactAction(metadata, downActionType) != null
                || findExactAction(metadata, upActionType) != null) {
            return null;
        }
        Object downAction = buildSingleEntryAction(downActionType, downKeycode, downPayload);
        Object upAction = buildSingleEntryAction(upActionType, upKeycode, null);
        if (downAction == null || upAction == null) {
            return null;
        }
        Object metadataBuilder = metadataBuilderConstructor.newInstance();
        copyMetadataMethod.invoke(metadataBuilder, metadata);
        putActionMethod.invoke(metadataBuilder, downAction);
        putActionMethod.invoke(metadataBuilder, upAction);
        return buildMetadataMethod.invoke(metadataBuilder);
    }

    private Object buildSingleEntryAction(Object actionType, int keycode, Object payload)
            throws Throwable {
        Object actionBuilder = actionBuilderConstructor.newInstance();
        actionBuilderTypeField.set(actionBuilder, actionType);
        Object entries = Array.newInstance(actionEntryClass, 1);
        Array.set(entries, 0, entryConstructor.newInstance(
                keycode, null, payload, Integer.MAX_VALUE));
        setActionBuilderEntries(actionBuilder, entries);
        return buildActionMethod.invoke(actionBuilder);
    }

    public boolean isStockGlobeMetadata(Object metadata) throws Throwable {
        Object pressAction = findExactAction(metadata, pressActionType);
        Object longPressAction = findExactAction(metadata, longPressActionType);
        return firstEntryCode(pressAction) == -0x271b
                && "globe".equals(firstEntryPayload(pressAction))
                && firstEntryCode(longPressAction) == -0x2726
                && "globe".equals(firstEntryPayload(longPressAction));
    }

    public Object appendLongPressAction(Context context, Object metadata,
            GboardLongPressQuickActions1803Policy.QuickAction action)
            throws Throwable {
        Object existingAction = findExactAction(metadata, longPressActionType);
        Object actionBuilder = actionBuilderConstructor.newInstance();
        Object[] existingEntries = extractEntries(existingAction);
        String[] existingLabels = popupLabels(existingAction);
        int[] existingIcons = popupIcons(existingAction);

        if (existingAction != null) {
            copyActionMethod.invoke(actionBuilder, existingAction);
        } else {
            int resolvedPopupLayoutResId = resolveSingleIconPopupLayoutResId(context);
            if (resolvedPopupLayoutResId == 0) {
                return null;
            }
            actionBuilderTypeField.set(actionBuilder, longPressActionType);
            actionBuilderPopupLayoutField.setInt(
                    actionBuilder, resolvedPopupLayoutResId);
        }

        Object updatedEntries = Array.newInstance(actionEntryClass, existingEntries.length + 1);
        System.arraycopy(existingEntries, 0, updatedEntries, 0, existingEntries.length);
        Array.set(updatedEntries, existingEntries.length,
                entryConstructor.newInstance(
                        action.actionCode, null, null, Integer.MAX_VALUE));
        setActionBuilderEntries(actionBuilder, updatedEntries);

        String[] labels = normalizeLabels(existingLabels, existingEntries.length + 1);
        labels[existingEntries.length] = null;
        setActionBuilderLabels(actionBuilder, labels);

        int[] icons = normalizeIcons(existingIcons, existingEntries.length + 1);
        icons[existingEntries.length] = action.iconResId;
        setActionBuilderIcons(actionBuilder, icons);

        Object patchedAction = buildActionMethod.invoke(actionBuilder);
        if (patchedAction == null) {
            return null;
        }
        Object metadataBuilder = metadataBuilderConstructor.newInstance();
        copyMetadataMethod.invoke(metadataBuilder, metadata);
        putActionMethod.invoke(metadataBuilder, patchedAction);
        return buildMetadataMethod.invoke(metadataBuilder);
    }

    private void setActionBuilderEntries(Object actionBuilder, Object entries) throws Throwable {
        if (actionBuilderEntriesMethod != null) {
            actionBuilderEntriesMethod.invoke(actionBuilder, entries);
        } else if (actionBuilderEntriesField != null) {
            actionBuilderEntriesField.set(actionBuilder, entries);
        }
    }

    private void setActionBuilderLabels(Object actionBuilder, String[] labels) throws Throwable {
        if (actionBuilderLabelsMethod != null) {
            actionBuilderLabelsMethod.invoke(actionBuilder, (Object) labels);
        } else if (actionBuilderLabelsField != null) {
            actionBuilderLabelsField.set(actionBuilder, labels);
        }
    }

    private void setActionBuilderIcons(Object actionBuilder, int[] icons) throws Throwable {
        if (actionBuilderIconsMethod != null) {
            actionBuilderIconsMethod.invoke(actionBuilder, (Object) icons);
        } else if (actionBuilderIconsField != null) {
            actionBuilderIconsField.set(actionBuilder, icons);
        }
    }

    private static int resolveSingleIconPopupLayoutResId(Context context) {
        if (context == null) {
            return 0;
        }
        TypedValue typedValue = new TypedValue();
        boolean resolved = context.getTheme().resolveAttribute(
                SINGLE_ICON_POPUP_LAYOUT_ATTR_ID, typedValue, true);
        return resolved
                && typedValue.resourceId != 0
                && typedValue.resourceId != SINGLE_ICON_POPUP_LAYOUT_ATTR_ID
                ? typedValue.resourceId
                : 0;
    }

    public void scheduleLongPress(Object pointerTracker) throws Throwable {
        if (pointerTracker != null) {
            scheduleLongPressMethod.invoke(pointerTracker);
        }
    }

    public boolean cancelScheduledLongPress(Object pointerTracker)
            throws IllegalAccessException {
        if (pointerTracker == null) {
            return false;
        }
        Field futureField = resolveLongPressFutureField(pointerTracker.getClass());
        Object future = futureField.get(pointerTracker);
        return future instanceof Future<?> && ((Future<?>) future).cancel(false);
    }

    public String extractEventActionTypeName(Object event) throws IllegalAccessException {
        Object actionType = event == null ? null : eventActionTypeField.get(event);
        return actionType instanceof Enum<?> ? ((Enum<?>) actionType).name() : null;
    }

    public Object extractEventMetadata(Object event) throws IllegalAccessException {
        return event == null ? null : eventMetadataField.get(event);
    }

    public int extractSelectedEventCode(Object event) throws IllegalAccessException {
        Object value = event == null ? null : eventEntriesField.get(event);
        if (!(value instanceof Object[] entries) || entries.length == 0 || entries[0] == null) {
            return 0;
        }
        return entryKeycodeField.getInt(entries[0]);
    }

    public String extractActionTypeName(Object actionType) {
        return actionType instanceof Enum<?> ? ((Enum<?>) actionType).name() : null;
    }

    public int extractEntryCode(Object entry) throws IllegalAccessException {
        return entry == null ? 0 : entryKeycodeField.getInt(entry);
    }

    public Object extractEntryPayload(Object entry) throws IllegalAccessException {
        return entry == null ? null : entryPayloadField.get(entry);
    }

    public Object extractSelectedEventPayload(Object event) throws IllegalAccessException {
        Object value = event == null ? null : eventEntriesField.get(event);
        if (!(value instanceof Object[] entries) || entries.length == 0 || entries[0] == null) {
            return null;
        }
        return entryPayloadField.get(entries[0]);
    }

    public int extractEventSubtype(Object event) throws IllegalAccessException {
        if (event == null) {
            return 0;
        }
        return resolveEventSubtypeField(event.getClass()).getInt(event);
    }

    private Field resolveLongPressFutureField(Class<?> owner) throws IllegalAccessException {
        Field cached = longPressFutureField;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            cached = longPressFutureField;
            if (cached == null) {
                try {
                    cached = owner.getDeclaredField("A");
                    if (!Future.class.isAssignableFrom(cached.getType())) {
                        throw new NoSuchFieldException("18.0.3 long-press future type drift");
                    }
                    cached.setAccessible(true);
                    longPressFutureField = cached;
                } catch (ReflectiveOperationException failure) {
                    throw new IllegalAccessException(failure.toString());
                }
            }
            return cached;
        }
    }

    private Field resolveEventSubtypeField(Class<?> owner) throws IllegalAccessException {
        Field cached = eventSubtypeField;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            cached = eventSubtypeField;
            if (cached == null) {
                try {
                    cached = owner.getDeclaredField("w");
                    if (cached.getType() != int.class) {
                        throw new NoSuchFieldException("18.0.3 event subtype type drift");
                    }
                    cached.setAccessible(true);
                    eventSubtypeField = cached;
                } catch (ReflectiveOperationException failure) {
                    throw new IllegalAccessException(failure.toString());
                }
            }
            return cached;
        }
    }

    private int firstEntryCode(Object action) throws IllegalAccessException {
        Object[] entries = extractEntries(action);
        return entries.length == 0 || entries[0] == null
                ? 0 : entryKeycodeField.getInt(entries[0]);
    }

    private Object firstEntryPayload(Object action) throws IllegalAccessException {
        Object[] entries = extractEntries(action);
        return entries.length == 0 || entries[0] == null
                ? null : entryPayloadField.get(entries[0]);
    }

    private Object findExactAction(Object metadata, Object actionType) throws Throwable {
        if (metadata == null || actionType == null) {
            return null;
        }
        return exactActionLookupMethod.invoke(metadata, actionType);
    }

    private Object[] extractEntries(Object action) throws IllegalAccessException {
        Object value = action == null ? null : actionEntriesField.get(action);
        if (value instanceof java.util.List<?>) {
            return ((java.util.List<?>) value).toArray();
        }
        return value instanceof Object[] ? (Object[]) value : new Object[0];
    }

    private String[] popupLabels(Object action) throws IllegalAccessException {
        Object value = action == null ? null : popupLabelsField.get(action);
        if (value instanceof java.util.List<?>) {
            java.util.List<?> list = (java.util.List<?>) value;
            String[] array = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                array[i] = item != null ? item.toString() : null;
            }
            return array;
        }
        return value instanceof String[] ? (String[]) value : new String[0];
    }

    private int[] popupIcons(Object action) throws IllegalAccessException {
        Object value = action == null ? null : popupIconsField.get(action);
        return value instanceof int[] ? (int[]) value : new int[0];
    }

    private static String[] normalizeLabels(String[] source, int size) {
        String[] result = new String[size];
        if (source != null && source.length == 1 && size > 1) {
            for (int index = 0; index < size - 1; index++) {
                result[index] = source[0];
            }
        } else if (source != null) {
            System.arraycopy(source, 0, result, 0, Math.min(source.length, size));
        }
        return result;
    }

    private static int[] normalizeIcons(int[] source, int size) {
        int[] result = new int[size];
        if (source != null && source.length == 1 && size > 1) {
            for (int index = 0; index < size - 1; index++) {
                result[index] = source[0];
            }
        } else if (source != null) {
            System.arraycopy(source, 0, result, 0, Math.min(source.length, size));
        }
        return result;
    }

    private static void validateInputEventBinding(Class<?> owner, Method method) {
        if (!InputMethodService.class.isAssignableFrom(owner)
                || method.getDeclaringClass() != owner
                || method.getReturnType() != boolean.class
                || method.getParameterCount() != 1
                || !Modifier.isPublic(method.getModifiers())
                || !Modifier.isFinal(method.getModifiers())) {
            throw new IllegalArgumentException("oup#au(nur) binding shape mismatch");
        }
    }

    private static Class<?> resolve(ClassLoader classLoader, String name)
            throws ClassNotFoundException {
        return Class.forName(name, false, classLoader);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object enumValue(Class<?> enumClass, String name) {
        return Enum.valueOf((Class<? extends Enum>) enumClass.asSubclass(Enum.class), name);
    }

    private static void setAccessible(Object... members) {
        for (Object member : members) {
            if (member instanceof Method method) {
                method.setAccessible(true);
            } else if (member instanceof Field field) {
                field.setAccessible(true);
            } else if (member instanceof Constructor<?> constructor) {
                constructor.setAccessible(true);
            }
        }
    }
}
