package app.simple.inure.constants

@Suppress("unused")
object Warnings {

    /**
     * InureWarning01: Cannot establish a root connection with the main shell.
     */
    fun getInureWarning01(): String = "0x001: Could not establish a root connection with the main shell."

    /**
     * InureWarning02: Failed to load the bitmap
     */
    fun getInureWarning02(path: String, type: String): String = "0x002: Failed to load type: $type from path: $path"

    /**
     * InureWarning03: Unknown app state detected!
     */
    fun getInureWarning03(): String = "0x003: Unknown app state detected!"

    /**
     * InureWarning04: Invalid unlocker detected
     */
    fun getInureWarning04(): String = "0x004: Invalid unlocker package detected or unlocker integrity has been compromised!"
}