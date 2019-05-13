import java.io.File

object PackageNameFinder {

    private val packageRegex = "package *= *\"([a-zA-Z0-9_.]+)\"".toRegex()

    private val moduleToPackageNameMapping = mutableMapOf<String, String>()

    fun getPackageNameFromModule(projectDir: String, module: String): String {
        var packageName = moduleToPackageNameMapping[module] ?: ""
        if (packageName.isEmpty()) {
            File("$projectDir/$module/src/main/AndroidManifest.xml").forEachLine {
                val index = packageRegex.find(it)

                if (index != null) {
                    packageName = index.destructured.component1()
                    moduleToPackageNameMapping[module] = packageName
                    return@forEachLine
                }
            }
        }

        return packageName
    }

}