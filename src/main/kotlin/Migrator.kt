import commands.RefactorColors
import commands.RefactorDimensions
import commands.RefactorDrawables
import commands.RefactorRaws
import commands.RefactorStrings
import java.io.File

class Migrator(private val projectDir: String, private val baseModule: String) {

    private val packageNameFinder = PackageNameFinder(projectDir)

    private val basePackageName = packageNameFinder.getPackageNameFromModule(baseModule)

    private val valuesDirs by lazy(LazyThreadSafetyMode.NONE) {
        File("$projectDir/$baseModule").walk()
            .filter {
                it.isDirectory && it.name.startsWith("values") && it.parent.contains("res")
            }
    }

    private val resourceFinder = ResourceFinder

    operator fun invoke() {

        refactor(
            namespaceDimens = true,
            namespaceDrawable = true,
            namespaceStrings = true,
            namespaceRaws = true,
            namespaceColors = true
        )

        println("Moving done")

        val modules = ModulesLister.list(projectDir)

        PackageNameQualifier(projectDir, basePackageName, resourceFinder).qualify(modules)

        println("Package name qualification done")

        val converter = DataBindingResourceConverter(projectDir, basePackageName, resourceFinder)
        modules.forEach { module ->
            println("in module $module")
            converter.convertToRIfNeeded(module)
        }

        println("Data binding resource conversion done")
    }

    private fun refactor(
        namespaceDimens: Boolean,
        namespaceDrawable: Boolean,
        namespaceStrings: Boolean,
        namespaceRaws: Boolean,
        namespaceColors: Boolean
    ) {

        val usages = UsageFinder.findUsages(projectDir, baseModule)


        if (namespaceDrawable) {
            refactorDrawables(usages.getMonoModuleDrawables())
        }



        if (namespaceDimens) {
            refactorDimens(usages.getMonoModuleDimensions())
        }

        if (namespaceRaws) {
            refactorRaws(usages.getMonoModuleRaws())
        }

        if (namespaceStrings) {
            refactorStrings(usages.getMonoModuleStrings())
        }

        if (namespaceColors) {
            refactorColors(usages.getMonoModuleColors())
        }
    }

    fun refactorColors(
        colors: Map<String, MutableList<Usage>>
    ) {
        val command = RefactorColors(projectDir, baseModule, valuesDirs, packageNameFinder)
        command(colors)
    }

    private fun refactorStrings(
        strings: Map<String, MutableList<Usage>>
    ) {
        val command = RefactorStrings(projectDir, baseModule, valuesDirs, packageNameFinder)
        command(strings)
    }

    private fun refactorRaws(
        raws: Map<String, MutableList<Usage>>
    ) {

        val command = RefactorRaws(projectDir, baseModule, packageNameFinder)
        command(raws)
    }

    private fun refactorDrawables(
        drawables: Map<String, MutableList<Usage>>
    ) {

        val command = RefactorDrawables(projectDir, baseModule, packageNameFinder)
        command(drawables)
    }

    private fun refactorDimens(
        dimens: Map<String, MutableList<Usage>>
    ) {

        val command = RefactorDimensions(projectDir, baseModule, valuesDirs, packageNameFinder)
        command(dimens)
    }
}