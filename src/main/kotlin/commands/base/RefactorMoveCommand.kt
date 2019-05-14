package commands.base

import PackageNameFinder
import Usage
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

open class RefactorMoveCommand(
    resType: String,
    projectDir: String,
    baseModule: String,
    packageNameFinder: PackageNameFinder
) : BaseCommand(resType, projectDir, baseModule, packageNameFinder) {

    override operator fun invoke(
        resources: Map<String, MutableList<Usage>>
    ) {
        val affectedFiles = refactorMove(resources)
        fullyQualifyResources(affectedFiles, resources)
    }

    private fun refactorMove(resources: Map<String, MutableList<Usage>>): MutableSet<String> {
        val affectedFiles = mutableSetOf<String>()
        val dirs = File("$projectDir/$baseModule").walk()
            .filter {
                it.isDirectory && it.name.startsWith(resType) && it.parent.contains("res")
            }
        resources.forEach { resource ->

            dirs.flatMap {
                it.walk()
            }.filter {
                !it.isDirectory && resource.key == it.nameWithoutExtension
            }.forEach {
                val module = resource.value[0].module
                val newFile = File(it.path.replace("$projectDir/$baseModule", "$projectDir/$module"))
                    .also {
                        it.parentFile.mkdirs()
                    }
                val newPath = Files.move(it.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                println("$resType ${newFile.name} moved to Module $module under ${newPath.parent}")
            }

            val entry = resource.value[0]
            affectedFiles.addAll(
                findAndReplaceRImports(
                    entry.module,
                    entry.files,
                    resource.key
                )
            )

        }

        return affectedFiles
    }
}