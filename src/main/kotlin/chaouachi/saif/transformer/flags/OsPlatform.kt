package chaouachi.saif.transformer.flags

enum class OsPlatform {
    WINDOWS,
    MACOS,
    LINUX,
    OTHER;

    companion object {

        /** Returns the OS platform this JVM is running on, relying on the "os.name" property name.  */
        val currentPlatform: OsPlatform
            get() {
                val os = System.getProperty("os.name")
                return if (os.startsWith("Mac OS")) {
                    MACOS
                } else if (os.startsWith("Windows")) {
                    WINDOWS
                } else if (os.startsWith("Linux")) {
                    LINUX
                } else {
                    OTHER
                }
            }
    }
}