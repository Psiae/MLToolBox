package dev.psiae.mltoolbox.utilskt

fun String.removePrefix(
    prefix: String,
    ignoreCase: Boolean = false
) = if (startsWith(prefix, ignoreCase = ignoreCase)) drop(prefix.length) else this

fun String.removeSuffix(
    suffix: String,
    ignoreCase: Boolean = false
) = if (endsWith(suffix, ignoreCase = ignoreCase)) dropLast(suffix.length) else this

fun String.endsWithLineSeparator(): Boolean {
    // empty
    if (isEmpty())
        return false
    // Unix and Windows
    if (endsWith("\n"))
        return true
    // Mac
    if (endsWith("\r"))
        return true
    return false
}

fun String.uppercaseFirstChar(): String =
    transformFirstCharIfNeeded(
        shouldTransform = { it.isLowerCase() },
        transform = { it.uppercaseChar() }
    )

fun String.lowercaseFirstChar(): String =
    transformFirstCharIfNeeded(
        shouldTransform = { it.isUpperCase() },
        transform = { it.lowercaseChar() }
    )

private inline fun String.transformFirstCharIfNeeded(
    shouldTransform: (Char) -> Boolean,
    transform: (Char) -> Char
): String {
    if (isNotEmpty()) {
        val firstChar = this[0]
        if (shouldTransform(firstChar)) {
            val sb = java.lang.StringBuilder(length)
            sb.append(transform(firstChar))
            sb.append(this, 1, length)
            return sb.toString()
        }
    }
    return this
}

