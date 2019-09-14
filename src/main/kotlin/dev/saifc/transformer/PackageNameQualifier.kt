package dev.saifc.transformer

import java.io.File

class PackageNameQualifier(
    private val projectDir: String,
    private val basePackageName: String,
    private val resourceFinder: ResourceFinder
) {

    private val resRegex =
        "([a-zA-Z0-9_.]*)R\\.(dimen|drawable|color|string|style|raw|array|anim|layout|bool)\\.([a-zA-Z0-9_]+)".toRegex()

    fun qualify(modules: List<String>) {
        modules.forEach { module ->
            println("$module:")
            val moduleResources = resourceFinder.findModuleResources(projectDir, module)
            //val packageName = getPackageNameFromModule(projectDir, module)

            File("$projectDir/$module").walk()
                .filter {
                    !it.isDirectory && (it.isXml() || it.isCode()) && !it.path.contains(
                        "/test/"
                    )
                }
                .forEach { file ->

                    val br = file.bufferedReader()
                    var writeFile = false

                    val newFile = File(file.absolutePath + "_tmp").apply {
                        createNewFile()
                    }

                    newFile.bufferedWriter().use { bw ->
                        var line = br.readLine()
                        while (line != null) {
                            val matches = resRegex.findAll(line).toList().reversed()
                            matches.forEach {
                                val (prefix, resourceType, resourceName) = it.destructured
                                val localResources = moduleResources[resourceType]
                                var isLocalResource = localResources?.contains(resourceName)
                                if (isLocalResource != true && resourceType == ResourceType.style) {
                                    isLocalResource = localResources?.contains(resourceName.replace("_", "."))
                                }
                                //TODO make parameterizable
                                if (prefix.isBlank() && isLocalResource != true) {
                                    line = line.replaceRange(
                                        it.groups[1]!!.range,
                                        "$basePackageName."
                                    )

                                    writeFile = true
                                }
                            }

                            bw.write(line)
                            line = br.readLine()
                            if (line != null) {
                                bw.newLine()
                            }
                        }
                        if (writeFile)
                            newFile.renameTo(file)
                        else
                            newFile.delete()
                    }
                }

        }
    }
}