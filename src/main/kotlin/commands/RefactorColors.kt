package commands

import PackageNameFinder
import ResourceType
import Usage
import commands.base.RefactorMoveCommand
import java.io.File

class RefactorColors(
    projectDir: String,
    packageNameFinder: PackageNameFinder,
    baseModule: String,
    private val valuesDirs: Sequence<File>
) :
    RefactorMoveCommand(ResourceType.color, projectDir, packageNameFinder, baseModule) {

    private val regex = "< *$resType .*name *= *\"([a-zA-Z0-9_]+)\"".toRegex()

    override fun invoke(resources: Map<String, MutableList<Usage>>) {

        val affectedFiles = refactorMove(resources)
        val files = valuesDirs.flatMap {
            it.walk()
        }.filter {
            !it.isDirectory && it.nameWithoutExtension.contains(resType + "s") && it.extension == "xml"
        }


        val filesToAppendTo = mutableMapOf<String, String>()

        files.forEach { file ->

            println("in ${file.path}")

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

                        val (resName) = it.destructured
                        val res = resources[resName]
                        if (res != null) {

                            val module = res[0].module
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
                    val content = file.readText().substringBeforeLast("</resources>")
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


        resources.forEach { string ->
            val entry = string.value[0]
            affectedFiles.addAll(
                findAndReplaceRImports(
                    entry.module,
                    entry.files,
                    string.key
                )
            )
        }


        fullyQualifyResources(affectedFiles, resources)

    }


}