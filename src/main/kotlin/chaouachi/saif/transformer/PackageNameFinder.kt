package chaouachi.saif.transformer

import java.io.File

class PackageNameFinder(private val projectDir: String) {

    private val packageRegex = "package *= *\"([a-zA-Z0-9_.]+)\"".toRegex()

    private val moduleToPackageNameMapping = mutableMapOf<String, String>()

    fun getPackageNameFromModule(module: String): String {
        var packageName = moduleToPackageNameMapping[module] ?: ""
        if (packageName.isEmpty()) {
            File("$projectDir/$module/src/chaouachi.saif.transformer.main/AndroidManifest.xml").forEachLine {
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