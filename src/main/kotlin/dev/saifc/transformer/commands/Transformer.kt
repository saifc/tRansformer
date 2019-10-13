package dev.saifc.transformer.commands

import dev.saifc.transformer.*
import dev.saifc.transformer.data.Usage
import dev.saifc.transformer.flags.Flag
import dev.saifc.transformer.flags.ParsedFlags
import dev.saifc.transformer.subcommands.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class Transformer(flags: ParsedFlags) {

    private val projectDir =
        (PROJECT_LOCATION_FLAG.getValue(flags) ?: Paths.get(System.getProperty("user.dir"))).toAbsolutePath().toString()
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

    private val modulesLister = ModulesLister

    private val usageFinder = UsageFinder

    private val packageNameQualifier = PackageNameQualifier(projectDir, basePackageName, resourceFinder)

    operator fun invoke() {

        refactor()

        println("Moving done")

        val modules: List<String> = modulesLister.list(projectDir).apply {
            remove(baseModule)
        }
        packageNameQualifier.qualify(modules)

        println("Package name qualification done")

        val converter =
            DataBindingResourceConverter(projectDir, basePackageName, resourceFinder)
        modules.forEach { module ->
            println("in module $module")
            converter.convertToRIfNeeded(module)
        }

        println("Data binding resource conversion done")
    }

    private fun refactor() {

        var namespaceDimens = resourceTypes?.contains(ResourceType.dimen) ?: false
        var namespaceDrawable = resourceTypes?.contains(ResourceType.drawable) ?: false
        var namespaceStrings = resourceTypes?.contains(ResourceType.string) ?: false
        var namespaceRaws = resourceTypes?.contains(ResourceType.raw) ?: false
        var namespaceColors = resourceTypes?.contains(ResourceType.color) ?: false


        if (!namespaceDimens && !namespaceDrawable && !namespaceStrings && !namespaceRaws && !namespaceColors) {
            namespaceDimens = true
            namespaceDrawable = true
            namespaceStrings = true
            namespaceRaws = true
            namespaceColors = true
        }

        val usages = usageFinder.findUsages(projectDir, baseModule)


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
        val command =
            RefactorColors(projectDir, baseModule, valuesDirs, packageNameFinder)
        command(colors)
    }

    private fun refactorStrings(
        strings: Map<String, MutableList<Usage>>
    ) {
        val command =
            RefactorStrings(projectDir, baseModule, valuesDirs, packageNameFinder)
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

        val command = RefactorDimensions(
            projectDir,
            baseModule,
            valuesDirs,
            packageNameFinder
        )
        command(dimens)
    }

    companion object {

        private val PROJECT_LOCATION_FLAG: Flag<Path> = Flag.path("project")
        private val BASE_MODULE_FLAG: Flag<String> = Flag.string("base-module")
        private val RESOURCE_TYPES_FLAG: Flag<List<String>> = Flag.stringList("resource-types")

        const val NAME="transform"
    }
}