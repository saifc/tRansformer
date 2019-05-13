package commands

import PackageNameFinder
import ResourceType
import Usage
import commands.base.RefactorMoveCommand
import java.io.File

class RefactorDimensions(
    projectDir: String,
    packageNameFinder: PackageNameFinder,
    baseModule: String,
    private val valuesDirs: Sequence<File>
) :
    RefactorMoveCommand(ResourceType.string, projectDir, packageNameFinder, baseModule) {


    private val regex = "< *$resType *name *= *\"([a-zA-Z0-9_.]+)\"".toRegex()

    override fun invoke(resources: Map<String, MutableList<Usage>>) {

        val files = valuesDirs.flatMap {
            it.walk()
        }.filter {
            !it.isDirectory && it.nameWithoutExtension.startsWith(resType + "s") && it.extension == "xml"
        }

        val filesToAppendTo = mutableMapOf<String, String>()

        files.forEach { file ->


            val br = file.bufferedReader()


            var writeFile = false
            val newFile = File(file.absolutePath + "_tmp").apply {
                createNewFile()
            }
            newFile.bufferedWriter().use { bw ->
                var line = br.readLine()
                while (line != null) {

                    var writeLine = true
                    val matches = regex.findAll(line)
                    matches.forEach {

                        val (name) = it.destructured
                        val resource = resources[name]
                        if (resource != null) {

                            val module = resource[0].module
                            if (!file.path.contains("$projectDir/$module")) {
                                val path = file.path.replace("$projectDir/$baseModule", "$projectDir/$module")
                                val fileContent = filesToAppendTo[path] ?: ""
                                filesToAppendTo[path] = fileContent + line + "\n"
                                writeFile = true
                                writeLine = false

                            }
                        }
                    }


                    if (writeLine)
                        bw.write(line)

                    line = br.readLine()

                    if (writeLine && line != null)
                        bw.newLine()
                }

                if (writeFile)
                    newFile.renameTo(file)
                else
                    newFile.delete()
            }


        }

        filesToAppendTo.forEach { (key, value) ->
            val file = File(key)
            val fileExists = file.exists()

            val fileContent =
                if (fileExists) {
                    val content = file.readText().substringBeforeLast("</resources >")
                    "$content$value</resources>"
                } else {
                    "<resources xmlns:tools=\"http://schemas.android.com/tools\">\n$value</resources>"
                }

            if (!fileExists) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }

            file.writeText(fileContent)

        }

        val affectedFiles = mutableSetOf<String>()

        resources.forEach { resource ->
            val entry = resource.value[0]
            affectedFiles.addAll(
                findAndReplaceRImports(
                    entry.module,
                    entry.files,
                    resource.key
                )
            )
        }


        fullyQualifyResources(affectedFiles, resources)
    }
}