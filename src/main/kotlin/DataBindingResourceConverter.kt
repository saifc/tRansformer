import java.io.File

class DataBindingResourceConverter(private val resourceFinder: ResourceFinder) {
    private val resRegex = "(@)(dimen|drawable|color|string)(/)([a-zA-Z0-9_]+)[:\\s},)]".toRegex()

    fun convertToRIfNeeded(module: String, projectDir: String, basePackageName: String) {

        val map = resourceFinder.findModuleResources(projectDir, module)

        val projectFile = File("$projectDir/$module")
        projectFile.walk()
            .filter { !it.isDirectory && it.extension.endsWith(".xml") && !it.isHidden && it.parent.contains("layout") }
            .forEach { file ->

                val br = file.bufferedReader()

                var line = br.readLine()

                var writeFile = false

                val newFile = File(file.absolutePath + "_tmp").apply {
                    createNewFile()
                }

                newFile.bufferedWriter().use { bw ->

                    while (line != null) {

                        val matches = resRegex.findAll(line).toList().reversed()
                        matches.forEach {
                            val (_, resourceType, _, resourceName) = it.destructured

                            val localResources = map[resourceType]
                            val isLocalResource = localResources?.contains(resourceName) ?: false

                            if (!isLocalResource) {
                                writeFile = true

                                when (resourceType) {

                                    ResourceType.dimen -> {
                                        line = changeToRResource(
                                            line,
                                            it,
                                            "context.resources.getDimension($basePackageName.R."
                                        )
                                    }

                                    ResourceType.drawable -> {
                                        line = changeToRResource(
                                            line,
                                            it,
                                            "androidx.appcompat.content.res.AppCompatResources.getDrawable(context, $basePackageName.R."
                                        )
                                    }

                                    ResourceType.color -> {
                                        line = changeToRResource(
                                            line,
                                            it,
                                            "androidx.core.content.ContextCompat.getColor(context, $basePackageName.R."
                                        )
                                    }

                                    ResourceType.string -> {
                                        line = changeToRResource(
                                            line,
                                            it,
                                            "context.getString($basePackageName.R."
                                        )
                                    }

                                }
                            }
                        }


                        bw.write(line)
                        line = br.readLine()
                        if (line != null) {
                            bw.newLine()
                        }
                    }
                }
                if (writeFile)
                    newFile.renameTo(file)
                else
                    newFile.delete()
            }
    }

    private fun changeToRResource(line: String, it: MatchResult, prefix: String): String {
        return line.replaceRange(it.groups[4]!!.range, "${it.groupValues[4]})")
            .replaceRange(it.groups[3]!!.range, ".")
            .replaceRange(it.groups[1]!!.range, prefix)
    }
}