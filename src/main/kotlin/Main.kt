import commands.RefactorColors
import commands.RefactorDimensions
import commands.RefactorDrawables
import commands.RefactorRaws
import commands.RefactorStrings
import java.io.File

fun main(args: Array<String>) {

    val projectDir = if (args.isNotEmpty()) args[0] else "/Users/saif/potcommun_android"
    val baseModule = if (args.size > 1) args[1] else "core"

    val basePackageName = PackageNameFinder.getPackageNameFromModule(projectDir, baseModule)

    refactor(projectDir, baseModule)

    println("Step 1 done")

    val modules = ModulesLister.list(projectDir)

    PackageNameQualifier(projectDir, basePackageName, ResourceFinder).qualify(modules)

    println("Step 2 done")

    val converter = DataBindingResourceConverter(ResourceFinder)
    modules.forEach { module ->
        println("in module $module")
        converter.convertToRIfNeeded(module, projectDir, basePackageName)
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






