import PackageNameFinder.getPackageNameFromModule
import ResourceFinder.findModuleResources
import commands.RefactorColors
import commands.RefactorDimensions
import commands.RefactorDrawables
import commands.RefactorRaws
import commands.RefactorStrings
import java.io.File


fun main(args: Array<String>) {

    val projectDir = if (args.isNotEmpty()) args[0] else "/Users/saif/potcommun_android"
    val baseModule = if (args.size > 1) args[1] else "core"
    val basePackageName = getPackageNameFromModule(projectDir, baseModule)

    refactor(projectDir, baseModule)

    println("Step 1 done")

    val modules = arrayOf(
        "app",
        "materialsearchview",
        "auth",
        "hiw",
        "about",
        "createpot",
        "contribute",
        "giftcard",
        "notifications",
        "contributors",
        "thankcontributors",
        "pots",
        "potdetails",
        "potoffer",
        "potuse",
        "account",
        "wiretransfer",
        "partners",
        "prices",
        "confirmation",
        "marketingcampaign",
        "materialstepperview",
        "actionmenu"
    )

    val resRegex =
        "([a-zA-Z0-9_.]*)R\\.(dimen|drawable|color|string|style|raw|array|anim|layout|bool)\\.([a-zA-Z0-9_]+)".toRegex()



    modules.forEach { module ->
        println("$module:")
        val moduleResources = findModuleResources(projectDir, module)
        //val packageName = getPackageNameFromModule(projectDir, module)

        File("$projectDir/$module").walk()
            .filter {
                !it.isDirectory && (it.name.endsWith(".xml") || it.name.endsWith(".java") || it.name.endsWith(".kt")) && !it.path.contains(
                    "/test/"
                )
            }
            .forEach { file ->

                val br = file.bufferedReader()
                var writeFile = false

                val newFile = File(file.absolutePath + "_tmp").apply {
                    createNewFile()
                }

                newFile.bufferedWriter().use { bw ->
                    var line = br.readLine()
                    while (line != null) {
                        val matches = resRegex.findAll(line).toList().reversed()
                        matches.forEach {
                            val (prefix, resourceType, resourceName) = it.destructured
                            val localResources = moduleResources[resourceType]
                            var isLocalResource = localResources?.contains(resourceName)
                            if (isLocalResource != true && resourceType == ResourceType.style) {
                                isLocalResource = localResources?.contains(resourceName.replace("_", "."))
                            }
                            //TODO make parameterizable
                            if (prefix.isBlank() && isLocalResource != true) {
                                line = line.replaceRange(
                                    it.groups[1]!!.range,
                                    "$basePackageName."
                                )

                                writeFile = true
                            }
                        }

                        bw.write(line)
                        line = br.readLine()
                        if (line != null) {
                            bw.newLine()
                        }
                    }
                    if (writeFile)
                        newFile.renameTo(file)
                    else
                        newFile.delete()
                }
            }

    }

    println("Step 2 done")

    modules.forEach { module ->
        println("in module $module")
        DataBindingResourceConverter(ResourceFinder).convertToRIfNeeded(module, projectDir, basePackageName)
    }
}


private fun refactor(projectDir: String, baseModule: String) {
    val namespaceDimens = true
    val namespaceDrawable = true
    val namespaceStrings = true
    val namespaceRaws = true
    val namespaceColors = true


    val usages = UsageFinder.findUsages(projectDir, baseModule)


    if (namespaceDrawable) {
        refactorDrawables(usages.getMonoModuleDrawables(), projectDir, baseModule)
    }

    val valuesDirs = File("$projectDir/$baseModule").walk()
        .filter {
            it.isDirectory && it.name.startsWith("values") && it.parent.contains("res")
        }

    if (namespaceDimens) {
        refactorDimens(usages.getMonoModuleDimensions(), valuesDirs, projectDir, baseModule)
    }

    if (namespaceRaws) {
        refactorRaws(usages.getMonoModuleRaws(), projectDir, baseModule)
    }

    if (namespaceStrings) {
        refactorStrings(usages.getMonoModuleStrings(), valuesDirs, projectDir, baseModule)
    }

    if (namespaceColors) {
        refactorColors(usages.getMonoModuleColors(), valuesDirs, projectDir, baseModule)
    }
}

fun refactorColors(
    colors: Map<String, MutableList<Usage>>,
    valuesDirs: Sequence<File>,
    projectDir: String,
    baseModule: String
) {
    val command = RefactorColors(projectDir, PackageNameFinder, baseModule, valuesDirs)
    command(colors)
}

private fun refactorStrings(
    strings: Map<String, MutableList<Usage>>,
    valuesDirs: Sequence<File>,
    projectDir: String,
    baseModule: String
) {
    val command = RefactorStrings(projectDir, PackageNameFinder, baseModule, valuesDirs)
    command(strings)


}

private fun refactorRaws(
    raws: Map<String, MutableList<Usage>>,
    projectDir: String,
    baseModule: String
) {

    val command = RefactorRaws(projectDir, PackageNameFinder, baseModule)
    command(raws)

}

private fun refactorDrawables(
    drawables: Map<String, MutableList<Usage>>,
    projectDir: String,
    baseModule: String
) {

    val command = RefactorDrawables(projectDir, PackageNameFinder, baseModule)
    command(drawables)

}


private fun refactorDimens(
    dimens: Map<String, MutableList<Usage>>,
    valuesDirs: Sequence<File>,
    projectDir: String,
    baseModule: String
) {

    val command = RefactorDimensions(projectDir, PackageNameFinder, baseModule, valuesDirs)
    command(dimens)

}






