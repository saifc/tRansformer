package chaouachi.saif.transformer.commands

import chaouachi.saif.transformer.ModulesLister
import chaouachi.saif.transformer.flags.Flag
import chaouachi.saif.transformer.flags.ParsedFlags
import chaouachi.saif.transformer.isXml
import java.io.File
import java.nio.file.Path

class ResourceRemover(flags: ParsedFlags) {
    private val projectDir = PROJECT_LOCATION_FLAG.getRequiredValue(flags).toAbsolutePath().toString()
    private val baseModule = BASE_MODULE_FLAG.getRequiredValue(flags)
    private val resourceTypes = RESOURCE_TYPES_FLAG.getRequiredValue(flags)


    private val modulesLister = ModulesLister


    private fun findResourceTypeFiles(valuesDirs: Sequence<File>, resType: String): Sequence<File> {
        return valuesDirs.flatMap {
            it.walk()
        }.filter {
            !it.isDirectory && it.nameWithoutExtension.contains(resType + "s") && it.isXml()
        }
    }


    operator fun invoke() {
        val modules = modulesLister.list(projectDir).apply {
            remove(baseModule)
        }


        modules.forEach { module ->
            val valuesDirs = File("$projectDir/$module").walk()
                .filter {
                    it.isDirectory && it.name.startsWith("values") && it.parent.contains("res")
                }
            resourceTypes.forEach { resType ->

                findResourceTypeFiles(valuesDirs, resType).forEach { file ->
                    println("removing $file")
                    file.delete()
                }
            }

        }


    }

    companion object {

        private val PROJECT_LOCATION_FLAG: Flag<Path> = Flag.path("project")
        private val BASE_MODULE_FLAG: Flag<String> = Flag.string("base-module")
        private val RESOURCE_TYPES_FLAG: Flag<List<String>> = Flag.stringList("resource-types")
    }
}