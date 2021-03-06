package dev.saifc.transformer

import dev.saifc.transformer.data.Usages
import java.io.File

object UsageFinder {

    private val resRegex =
        "([a-zA-Z0-9_.]*)(R.|@)(dimen|drawable|color|string|style|raw|array)[./]([a-zA-Z0-9_]+)".toRegex()

    private val codeResRegex =
        "(([a-zA-Z0-9_]+[\\n\\r\\s]*\\.[\\n\\r\\s]*)+|[a-zA-Z0-9_]*)(R[\\n\\r\\s]*.[\\n\\r\\s]*)(dimen|drawable|color|string|style|raw|array)[\\n\\r\\s]*\\.[\\n\\r\\s]*([a-zA-Z0-9_]+)([\\n\\r\\s]|\\)|;|,)".toRegex()

    fun findUsages(projectDir: String, baseModule: String): Usages {

        val usages = Usages(baseModule)

        val projectFile = File(projectDir)
        projectFile.walk()
            .filter { !it.isDirectory && it.isXml() && !it.isHidden && (it.parent.contains("layout") || it.parent.contains("menu") || it.parent.contains("xml") || it.name.contains("AndroidManifest.xml") ) }
            .forEach { file ->

                val br = file.bufferedReader()

                var line = br.readLine()

                while (line != null) {

                    val matches = resRegex.findAll(line)
                    matches.forEach {
                        val (prefix, _, resourceType, resourceName) = it.destructured


                        if ("android." != prefix) {
                            putResource(
                                resourceType,
                                usages,
                                resourceName,
                                file,
                                projectFile
                            )
                        }

                    }
                    line = br.readLine()
                }

            }

        projectFile.walk()
            .filter { !it.isDirectory && it.isCode() && !it.path.contains("${File.separator}test${File.separator}") }
            .forEach { file ->
                val text = file.readText()

                val matches = codeResRegex.findAll(text)
                matches.forEach {
                    val (prefix, _, _, resourceType, resourceName) = it.destructured


                    if ("android." != prefix.replace("\\s".toRegex(), "")) {
                        putResource(
                            resourceType,
                            usages,
                            resourceName,
                            file,
                            projectFile
                        )
                    }

                }
            }

        return usages
    }

    private fun putResource(
        resourceType: String,
        usages: Usages,
        resourceName: String,
        file: File,
        projectFile: File
    ) {
        when (resourceType) {

            ResourceType.dimen -> {
                usages.putDimension(
                    resourceName,
                    file.toRelativeString(projectFile).substringBefore(File.separator),
                    file.path
                )
            }
            ResourceType.drawable -> {
                usages.putDrawable(
                    resourceName,
                    file.toRelativeString(projectFile).substringBefore(File.separator),
                    file.path
                )
            }

            ResourceType.color -> {
                usages.putColor(
                    resourceName,
                    file.toRelativeString(projectFile).substringBefore(File.separator),
                    file.path
                )
            }
            ResourceType.string -> {
                usages.putString(
                    resourceName,
                    file.toRelativeString(projectFile).substringBefore(File.separator),
                    file.path
                )
            }

            ResourceType.raw -> {
                usages.putRaw(
                    resourceName,
                    file.toRelativeString(projectFile).substringBefore(File.separator),
                    file.path
                )
            }

        }
    }
}