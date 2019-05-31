import commands.RefactorColors
import commands.RefactorDimensions
import commands.RefactorDrawables
import commands.RefactorRaws
import commands.RefactorStrings
import flags.Flag
import flags.ParsedFlags
import java.io.File
import java.nio.file.Path

class Migrator(flags: ParsedFlags) {

    private val projectDir = PROJECT_LOCATION_FLAG.getRequiredValue(flags).toAbsolutePath().toString()
    private val baseModule = BASE_MODULE_FLAG.getRequiredValue(flags)
    private val resourceTypes = RESOURCE_TYPES_FLAG.getValue(flags)

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

        refactor()

        println("Moving done")

        val modules: List<String> = ModulesLister.list(projectDir).apply {
            remove(baseModule)
        }
        PackageNameQualifier(projectDir, basePackageName, resourceFinder).qualify(modules)

        println("Package name qualification done")

        val converter = DataBindingResourceConverter(projectDir, basePackageName, resourceFinder)
        modules.forEach { module ->
            println("in module $module")
            converter.convertToRIfNeeded(module)
        }

        println("Data binding resource conversion done")
    }

    private fun refactor() {

        var namespaceDimens = resourceTypes?.contains(ResourceType.dimen) ?: true
        var namespaceDrawable = resourceTypes?.contains(ResourceType.drawable) ?: true
        var namespaceStrings = resourceTypes?.contains(ResourceType.string) ?: true
        var namespaceRaws = resourceTypes?.contains(ResourceType.raw) ?: true
        var namespaceColors = resourceTypes?.contains(ResourceType.color) ?: true


        if (!namespaceDimens && !namespaceDrawable && !namespaceStrings && !namespaceRaws && !namespaceColors) {
            namespaceDimens = true
            namespaceDrawable = true
            namespaceStrings = true
            namespaceRaws = true
            namespaceColors = true
        }

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

    private fun refactorColors(
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

    companion object {

        val PROJECT_LOCATION_FLAG: Flag<Path> = Flag.path("project")
        val BASE_MODULE_FLAG: Flag<String> = Flag.string("base-module")
        val RESOURCE_TYPES_FLAG: Flag<List<String>> = Flag.stringList("resource-types")
    }
}