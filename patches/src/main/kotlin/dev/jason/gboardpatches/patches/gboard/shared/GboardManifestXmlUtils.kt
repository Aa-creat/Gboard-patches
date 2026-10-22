package dev.jason.gboardpatches.patches.gboard.shared

import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element

internal fun ensureManifestUsesPermission(
    document: Document,
    manifest: Element,
    permissionName: String,
) {
    val existing = manifest.childElements("uses-permission").firstOrNull {
        it.manifestAndroidAttribute("name") == permissionName
    }
    if (existing != null) {
        return
    }
    val createdPermission = document.createElement("uses-permission").also { created ->
        val application = manifest.childElements("application").firstOrNull()
        if (application == null) {
            manifest.appendChild(created)
        } else {
            manifest.insertBefore(created, application)
        }
    }
    createdPermission.setManifestAndroidAttribute("name", permissionName)
}

internal fun ensureManifestComponent(
    document: Document,
    application: Element,
    tagName: String,
    className: String,
): Element {
    val component = application.childElements(tagName).firstOrNull {
        it.manifestAndroidAttribute("name") == className
    } ?: document.createElement(tagName).also(application::appendChild)
    component.setManifestAndroidAttribute("name", className)
    return component
}

internal fun ensureManifestIntentFilter(
    document: Document,
    component: Element,
    actionName: String,
    categoryName: String? = null,
) {
    val intentFilter = component.childElements("intent-filter").firstOrNull {
        it.childElements("action").any { action ->
            action.manifestAndroidAttribute("name") == actionName
        }
    } ?: document.createElement("intent-filter").also(component::appendChild)
    ensureNamedManifestChild(document, intentFilter, "action", actionName)
    categoryName?.let { category ->
        ensureNamedManifestChild(document, intentFilter, "category", category)
    }
}

internal fun ensureManifestMetaData(
    document: Document,
    component: Element,
    name: String,
    value: String,
) {
    val metaData = component.childElements("meta-data").firstOrNull {
        it.manifestAndroidAttribute("name") == name
    } ?: document.createElement("meta-data").also(component::appendChild)
    metaData.setManifestAndroidAttribute("name", name)
    metaData.setManifestAndroidAttribute("value", value)
}

internal fun Element.manifestAndroidAttribute(localName: String): String? {
    val namespaced = getAttributeNS(ANDROID_NS, localName)
    if (namespaced.isNotBlank()) return namespaced
    return getAttribute("android:$localName").takeIf { it.isNotBlank() }
}

internal fun Element.setManifestAndroidAttribute(localName: String, value: String) {
    val existingAttr = (0 until attributes.length)
        .mapNotNull { attributes.item(it) as? Attr }
        .firstOrNull { attr ->
            (attr.namespaceURI == ANDROID_NS && attr.localName == localName) ||
                attr.nodeName == "android:$localName"
        }
    if (existingAttr != null) {
        existingAttr.value = value
    } else {
        if (ownerDocument?.documentElement?.namespaceURI == ANDROID_NS || namespaceURI == ANDROID_NS) {
            setAttributeNS(ANDROID_NS, "android:$localName", value)
        } else {
            setAttribute("android:$localName", value)
        }
    }
}

internal fun Element.removeManifestAndroidAttribute(localName: String) {
    val existingAttr = (0 until attributes.length)
        .mapNotNull { attributes.item(it) as? Attr }
        .firstOrNull { attr ->
            (attr.namespaceURI == ANDROID_NS && attr.localName == localName) ||
                attr.nodeName == "android:$localName"
        }
    if (existingAttr != null) {
        removeAttributeNode(existingAttr)
    }
    removeAttributeNS(ANDROID_NS, localName)
    removeAttribute("android:$localName")
}

private fun ensureNamedManifestChild(
    document: Document,
    parent: Element,
    tagName: String,
    name: String,
) {
    val child = parent.childElements(tagName).firstOrNull {
        it.manifestAndroidAttribute("name") == name
    } ?: document.createElement(tagName).also(parent::appendChild)
    child.setManifestAndroidAttribute("name", name)
}
