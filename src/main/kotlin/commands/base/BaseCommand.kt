package commands.base

import PackageNameFinder
import Usage
import java.io.File

abstract class BaseCommand(
    protected val resType: String,
    protected val projectDir: String,
    private val packageNameFinder: PackageNameFinder,
    protected val baseModule: String
) : Command {
    private val basePackageName by lazy(LazyThreadSafetyMode.NONE) {
        packageNameFinder.getPackageNameFromModule(
            projectDir,
            baseModule
        )
    }
    private val xmlRegex =
        "([a-zA-Z0-9_.]*)(R.)(dimen|drawable|color|string|style|raw|array)[.]([a-zA-Z0-9_]+)".toRegex()
    private val codeRegex =
        "([a-zA-Z0-9_]+[\\n\\r\\s]*\\.[a-zA-Z0-9_.\\n\\r\\s]*\\.[\\n\\r\\s]*|[a-zA-Z0-9_]*\\.?)(R[\\n\\r\\s]*.[\\n\\r\\s]*)(dimen|drawable|color|string|style|raw|array)[\\n\\r\\s]*\\.[\\n\\r\\s]*([a-zA-Z0-9_]+)".toRegex()

    private val importRRegex by lazy(LazyThreadSafetyMode.NONE) { "<import *type *= *\"($basePackageName)\\.R\"".toRegex() }

    protected fun fullyQualifyResources(
        files: MutableSet<String>,
        resources: Map<String, MutableList<Usage>>
    ) {

        val resourcesToExclude = resources.map { it.key }

        files.filter { it.endsWith(".xml") }.map { File(it) }
            .forEach { oldFile ->
                var writeFile = false
                val br = oldFile.bufferedReader()
                val newFile = File(oldFile.absolutePath + "_tmp").apply {
                    createNewFile()
                }
                newFile.bufferedWriter().use { bw ->
                    var line = br.readLine()

                    while (line != null) {

                        val matches = xmlRegex.findAll(line).toList().reversed()
                        matches.forEach {
                            val (prefix, _, resourceType, resourceName) = it.destructured



                            if (prefix.isBlank() && ((resourceType != resType) || (resourceType == resType && !resourcesToExclude.contains(
                                    resourceName
                                )))
                            ) {
                                println("-----")
                                println(line)
                                println("$resourceType==$resType")

                                line = line.replaceRange(it.groups[1]!!.range, "$basePackageName.")
                                println("became")
                                println(line)

                                writeFile = true
                            }

                        }
                        bw.write(line)
                        line = br.readLine()
                        if (line != null)
                            bw.newLine()
                    }

                }

                if (writeFile)
                    newFile.renameTo(oldFile)
                else
                    newFile.delete()

            }


        files.filter { (it.endsWith(".kt") || it.endsWith(".java")) && !it.contains("/test/") }.map { File(it) }
            .forEach { file ->
                var writeFile = false
                var text = file.readText()

                val matches = codeRegex.findAll(text).toList().reversed()
                matches.forEach {
                    val (prefix, _, resourceType, resourceName) = it.destructured

                    if ((prefix.isBlank() || !prefix.endsWith(".")) && ((resourceType != resType) || (resourceType == resType && !resourcesToExclude.contains(
                            resourceName
                        )))
                    ) {
                        text = text.replaceRange(it.groups[1]!!.range, "$basePackageName.")
                        writeFile = true
                    }

                }

                if (writeFile) {
                    file.writeText(text)
                }

            }
    }

    protected fun findAndReplaceRImports(
        module: String,
        files: MutableSet<String>,
        res: String
    ): MutableSet<String> {
        val modifiedFiles = mutableSetOf<String>()

        val packageName = packageNameFinder.getPackageNameFromModule(projectDir, module)

        if (basePackageName == packageName)
            return modifiedFiles

//TODO extract
        val xmlRegex = "([a-zA-Z0-9_.]*)(R.)(dimen|drawable|color|string|style|raw|array)[.]([a-zA-Z0-9_]+)".toRegex()
        val xmlFullyQualifiedRegex =
            "($basePackageName\\.)(R.)(dimen|drawable|color|string|style|raw|array)[.]([a-zA-Z0-9_]+)".toRegex()

        files.filter { it.endsWith(".xml") }.map { File(it) }.forEach { oldFile ->
            val it = oldFile.bufferedReader()

            val newFile = File(oldFile.absolutePath + "_tmp").apply {
                createNewFile()
            }
            var writeFile = false
            newFile.bufferedWriter().use { bw ->
                var skipMatch = false

                var line = it.readLine()
                while (line != null) {
                    if (!skipMatch) {
                        val result = importRRegex.find(line)
                        if (result != null) {
                            line = line.replace(basePackageName, packageName)
                            skipMatch = true

                            writeFile = true
                            modifiedFiles.add(oldFile.path)
                        }
                    }

                    if (skipMatch) {
                        val matches = xmlRegex.findAll(line).toList().reversed()
                        matches.forEach {
                            val (prefix, _, resourceType, resourceName) = it.destructured

                            if (!prefix.isBlank() && prefix != "android." && resourceType == resType && resourceName == res) {
                                line = line.replaceRange(it.groups[1]!!.range, "$packageName.")

                                writeFile = true
                            }

                        }
                    } else {
                        val matches = xmlFullyQualifiedRegex.findAll(line).toList().reversed()
                        matches.forEach {
                            val (_, _, resourceType, resourceName) = it.destructured

                            if (resourceType == resType && resourceName == res) {
                                line = line.replaceRange(it.groups[1]!!.range, "$packageName.")

                                writeFile = true
                            }

                        }
                    }
                    bw.write(line)
                    line = it.readLine()
                    if (line != null)
                        bw.newLine()
                }
            }


            if (writeFile)
                newFile.renameTo(oldFile)
            else
                newFile.delete()
        }

        val importRCodeRegex =
            "import +($basePackageName)\\.(R([\\n\\r\\s]+as[\\n\\r\\s]+[a-zA-Z0-9_]+)?|R;) *\\n".toRegex()

        val localImportRCodeRegex = "import +$packageName\\.R;?\n".toRegex()
        val importRegex = "import [a-zA-Z0-9_.]+\n".toRegex()

        //TODO extract
        val codeRegex =
            "(([a-zA-Z0-9_]+[\\n\\r\\s]*\\.[\\n\\r\\s]*)+|[a-zA-Z0-9_]*)(R[\\n\\r\\s]*.[\\n\\r\\s]*)(dimen|drawable|color|string|style|raw|array)[\\n\\r\\s]*\\.[\\n\\r\\s]*([a-zA-Z0-9_]+)([\\n\\r\\s]|\\)|;|,)".toRegex()

        files.filter { (it.endsWith(".kt") || it.endsWith(".java")) && !it.contains("/test/") }.map { File(it) }
            .forEach { file ->
                var writeFile = false
                var text = file.readText()


                importRCodeRegex.findAll(text).toList().reversed().forEach {
                    text = text.replaceRange(it.groups[1]!!.range, packageName)
                    writeFile = true
                }
                if (writeFile) {
                    modifiedFiles.add(file.path)
                    if (localImportRCodeRegex.find(text) == null) {
                        val match = importRegex.find(text)
                        if (match != null) {
                            val group = match.groups[0]!!
                            text = text.replaceRange(group.range, "${group.value}import $packageName.R;\n")
                        }
                    }
                }
                val matches = codeRegex.findAll(text).toList().reversed()
                matches.forEach {
                    val (prefix, _, _, resourceType, resourceName) = it.destructured

                    if (!prefix.isBlank() && prefix != "android." && resourceType == resType && resourceName == res) {
                        text = text.replaceRange(it.groups[1]!!.range, "$packageName.")
                        writeFile = true
                    }

                }



                if (writeFile) {
                    file.writeText(text)
                }

            }


        return modifiedFiles
    }
}