import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun main(args: Array<String>) {


    val namespaceDimens = true
    val namespaceDrawable = true
    val namespaceStrings = true
    val namespaceRaws = true

    val baseModule = "core"
    val projectDir = "/Users/saif/pot_commun_android"
    val basePackageName = getPackageNameFromModule(projectDir, baseModule)

    val usages = findUsages(projectDir)


    if (namespaceDrawable) {
        refactorDrawables(usages.getMonoModuleDrawables(), projectDir, baseModule, basePackageName)
    }

    val valuesDirs = File("$projectDir/$baseModule").walk()
        .filter {
            it.isDirectory && it.name.startsWith("values") && it.parent.contains("res")
        }

    if (namespaceDimens) {
        refactorDimens(usages.getMonoModuleDimensions(), valuesDirs, projectDir, baseModule, basePackageName)
    }


    if (namespaceStrings) {
        refactorStrings(usages.getMonoModuleStrings(), valuesDirs, projectDir, baseModule, basePackageName)
    }

    if (namespaceRaws) {
        refactorRaws(usages.getMonoModuleRaws(), projectDir, baseModule, basePackageName)
    }


}

private fun refactorStrings(
    strings: Map<String, MutableList<Usage>>,
    valuesDirs: Sequence<File>,
    projectDir: String,
    baseModule: String,
    basePackageName: String
) {
    val affectedFiles = mutableSetOf<String>()


    val stringsFiles = valuesDirs.flatMap {
        it.walk()
    }.filter {
        !it.isDirectory && it.nameWithoutExtension.contains("strings") && it.extension == "xml"
    }


    val stringRegex = "< *string .* *name *= *\"([a-zA-Z0-9_]+)\"".toRegex()

    val filesToAppendTo = mutableMapOf<String, String>()

    stringsFiles.forEach { file ->

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
                val matches = stringRegex.findAll(line)
                matches.forEach {

                    val (stringName) = it.destructured
                    val string = strings[stringName]
                    if (string != null) {

                        val module = string[0].module
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
                newFile.deleteOnExit()
        }


    }

    filesToAppendTo.forEach { key, value ->
        val file = File(key)
        val fileExists = file.exists()

        val fileContent =
            if (fileExists) {
                val content = file.readText().substringBeforeLast("</resources>")
                "$content$value</resources>"
            } else {
                "<resources>\n$value</resources>"
            }

        if (!fileExists) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }

        file.writeText(fileContent)

    }


    strings.forEach { string ->
        val entry = string.value[0]
        val packageName = getPackageNameFromModule(projectDir, entry.module)
        affectedFiles.addAll(
            findAndReplaceRImports(
                basePackageName,
                packageName,
                entry.files,
                string.key,
                ResourceType.string
            )
        )
    }



    fullyQualifyResources(affectedFiles, ResourceType.string, strings.map { it.key }, basePackageName)

    affectedFiles.clear()
}

private fun refactorRaws(
    raws: Map<String, MutableList<Usage>>,
    projectDir: String,
    baseModule: String,
    basePackageName: String
) {
    val affectedFiles = mutableSetOf<String>()
    val dirs = File("$projectDir/$baseModule").walk()
        .filter {
            it.isDirectory && it.name.startsWith(ResourceType.raw) && it.parent.contains("res")
        }
    raws.forEach { raw ->

        dirs.flatMap {
            it.walk()
        }.filter {
            !it.isDirectory && raw.key == it.nameWithoutExtension
        }.forEach {
            val module = raw.value[0].module
            val newFile = File(it.path.replace("$projectDir/$baseModule", "$projectDir/$module"))
                .also {
                    it.parentFile.mkdirs()
                }
            val newPath = Files.move(it.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            println("Raw ${newFile.name} moved to Module $module under ${newPath.parent}")
        }

        val entry = raw.value[0]
        val packageName = getPackageNameFromModule(projectDir, entry.module)
        affectedFiles.addAll(
            findAndReplaceRImports(
                basePackageName,
                packageName,
                entry.files,
                raw.key,
                ResourceType.raw
            )
        )

    }


    fullyQualifyResources(affectedFiles, ResourceType.raw, raws.map { it.key }, basePackageName)

}

private fun refactorDrawables(
    drawables: Map<String, MutableList<Usage>>,
    projectDir: String,
    baseModule: String,
    basePackageName: String
) {
    val affectedFiles = mutableSetOf<String>()
    val drawableDirs = File("$projectDir/$baseModule").walk()
        .filter {
            it.isDirectory && it.name.startsWith(ResourceType.drawable) && it.parent.contains("res")
        }
    drawables.forEach { drawable ->

        drawableDirs.flatMap {
            it.walk()
        }.filter {
            !it.isDirectory && drawable.key == it.nameWithoutExtension
        }.forEach {
            val module = drawable.value[0].module
            val newFile = File(it.path.replace("$projectDir/$baseModule", "$projectDir/$module"))
                .also {
                    it.parentFile.mkdirs()
                }
            val newPath = Files.move(it.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            println("Drawable ${newFile.name} moved to Module $module under ${newPath.parent}")
        }

        val entry = drawable.value[0]
        val packageName = getPackageNameFromModule(projectDir, entry.module)
        affectedFiles.addAll(
            findAndReplaceRImports(
                basePackageName,
                packageName,
                entry.files,
                drawable.key,
                ResourceType.drawable
            )
        )

    }


    fullyQualifyResources(affectedFiles, ResourceType.drawable, drawables.map { it.key }, basePackageName)

    affectedFiles.clear()
}

private fun refactorDimens(
    dimens: Map<String, MutableList<Usage>>,
    valuesDirs: Sequence<File>,
    projectDir: String,
    baseModule: String,
    basePackageName: String
) {


    val affectedFiles = mutableSetOf<String>()


    val dimensFiles = valuesDirs.flatMap {
        it.walk()
    }.filter {
        !it.isDirectory && it.nameWithoutExtension.startsWith("dimens") && it.extension == "xml"
    }


    val dimenRegex = "< *dimen *name *= *\"([a-zA-Z0-9_.]+)\"".toRegex()

    val filesToAppendTo = mutableMapOf<String, String>()

    dimensFiles.forEach { file ->

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
                val matches = dimenRegex.findAll(line)
                matches.forEach {

                    val (dimenName) = it.destructured
                    val dimen = dimens[dimenName]
                    if (dimen != null) {

                        val module = dimen[0].module
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
                newFile.deleteOnExit()
        }


    }

    filesToAppendTo.forEach { key, value ->
        val file = File(key)
        val fileExists = file.exists()

        val fileContent =
            if (fileExists) {
                val content = file.readText().substringBeforeLast("</resources>")
                "$content$value</resources>"
            } else {
                "<resources>\n$value</resources>"
            }

        if (!fileExists) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }

        file.writeText(fileContent)

    }


    dimens.forEach { dimen ->
        val entry = dimen.value[0]
        val packageName = getPackageNameFromModule(projectDir, entry.module)
        affectedFiles.addAll(
            findAndReplaceRImports(
                basePackageName,
                packageName,
                entry.files,
                dimen.key,
                ResourceType.dimen
            )
        )
    }


    fullyQualifyResources(affectedFiles, ResourceType.dimen, dimens.map { it.key }, basePackageName)

    affectedFiles.clear()
}

private fun findUsages(projectDir: String): Usages {

    val resRegex = "([a-zA-Z0-9_.]*)(R.|@)(dimen|drawable|color|string|style|raw|array)[./]([a-zA-Z0-9_]+)".toRegex()
    val usages = Usages()

    val projectFile = File(projectDir)
    projectFile.walk()
        .filter { !it.isDirectory && it.name.endsWith(".xml") && !it.isHidden && it.parent.contains("layout") }
        .forEach { file ->

            val br = file.bufferedReader()

            var line = br.readLine()

            while ((line) != null) {

                val matches = resRegex.findAll(line)
                matches.forEach {
                    val (prefix, _, resourceType, resourceName) = it.destructured


                    if ("android." != prefix) {
                        when (resourceType) {

                            ResourceType.dimen -> {
                                usages.putDimension(
                                    resourceName,
                                    file.toRelativeString(projectFile).substringBefore("/"),
                                    file.path
                                )
                            }
                            ResourceType.drawable -> {
                                usages.putDrawable(
                                    resourceName,
                                    file.toRelativeString(projectFile).substringBefore("/"),
                                    file.path
                                )
                            }

                            ResourceType.color -> {
                                usages.putColor(
                                    resourceName,
                                    file.toRelativeString(projectFile).substringBefore("/"),
                                    file.path
                                )
                            }
                            ResourceType.string -> {
                                usages.putString(
                                    resourceName,
                                    file.toRelativeString(projectFile).substringBefore("/"),
                                    file.path
                                )
                            }

                        }
                    }

                }
                line = br.readLine()
            }

        }

    val codeResRegex =
        "([a-zA-Z0-9_]+[\\n\\r\\s]*\\.[a-zA-Z0-9_.\\n\\r\\s]*\\.[\\n\\r\\s]*|)(R[\\n\\r\\s]*.[\\n\\r\\s]*)(dimen|drawable|color|string|style|raw|array)[\\n\\r\\s]*\\.[\\n\\r\\s]*([a-zA-Z0-9_]+)".toRegex()
    projectFile.walk().filter { !it.isDirectory && (it.name.endsWith(".kt") || it.name.endsWith(".java")) }
        .forEach { file ->
            val text = file.readText()

            val matches = codeResRegex.findAll(text)
            matches.forEach {
                val (prefix, _, resourceType, resourceName) = it.destructured


                if ("android." != prefix.replace("\\s".toRegex(), "")) {
                    when (resourceType) {

                        ResourceType.dimen -> {
                            usages.putDimension(
                                resourceName,
                                file.toRelativeString(projectFile).substringBefore("/"),
                                file.path
                            )
                        }
                        ResourceType.drawable -> {
                            usages.putDrawable(
                                resourceName,
                                file.toRelativeString(projectFile).substringBefore("/"),
                                file.path
                            )
                        }

                        ResourceType.color -> {
                            usages.putColor(
                                resourceName,
                                file.toRelativeString(projectFile).substringBefore("/"),
                                file.path
                            )
                        }
                        ResourceType.string -> {
                            usages.putString(
                                resourceName,
                                file.toRelativeString(projectFile).substringBefore("/"),
                                file.path
                            )
                        }
                        ResourceType.raw -> {
                            usages.putRaw(
                                resourceName,
                                file.toRelativeString(projectFile).substringBefore("/"),
                                file.path
                            )
                        }

                    }
                }

            }
        }

    return usages
}

fun findAndReplaceRImports(
    corePackageName: String,
    packageName: String,
    files: MutableSet<String>,
    res: String,
    resType: String
): MutableSet<String> {
    val modifiedFiles = mutableSetOf<String>()
    if (corePackageName == packageName)
        return modifiedFiles

    val importRRegex = "<import *type *= *\"($corePackageName)\\.R\"".toRegex()
//TODO extract
    val xmlRegex = "([a-zA-Z0-9_.]*)(R.)(dimen|drawable|color|string|style|raw|array)[.]([a-zA-Z0-9_]+)".toRegex()
    val xmlFullyQualifiedRegex =
        "($corePackageName\\.)(R.)(dimen|drawable|color|string|style|raw|array)[.]([a-zA-Z0-9_]+)".toRegex()

    files.filter { it.endsWith(".xml") }.map { File(it) }.forEach { oldFile ->
        val it = oldFile.bufferedReader()

        val newFile = File(oldFile.absolutePath + "_tmp").apply {
            createNewFile()
        }
        var writeFile = false
        newFile.bufferedWriter().use { bw ->
            var skipMatch = false

            var line = it.readLine()
            while ((line) != null) {
                if (!skipMatch) {
                    val result = importRRegex.find(line)
                    if (result != null) {
                        line = line.replace(corePackageName, packageName)
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
            newFile.deleteOnExit()
    }

    val importRCodeRegex =
        "import +($corePackageName)\\.(R([\\n\\r\\s]+as[\\n\\r\\s]+[a-zA-Z0-9_]+)?|R;) *\\n".toRegex()

    val localImportRCodeRegex = "import +$packageName\\.R;?\n".toRegex()
    val importRegex = "import [a-zA-Z0-9_.]+\n".toRegex()


    //TODO extract
    val codeRegex =
        "(([a-zA-Z0-9_]+[\\n\\r\\s]*\\.[\\n\\r\\s]*)+|[a-zA-Z0-9_]*)(R[\\n\\r\\s]*.[\\n\\r\\s]*)(dimen|drawable|color|string|style|raw|array)[\\n\\r\\s]*\\.[\\n\\r\\s]*([a-zA-Z0-9_]+)([\\n\\r\\s]|\\)|;|,)".toRegex()

    files.filter { !it.endsWith(".xml") }.map { File(it) }.forEach { file ->
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


        //TODO new
        /* val fullyMatches = xmlFullyQualifiedRegex.findAll(text).toList().reversed()
         fullyMatches.forEach {
             val (_, _, resourceType, resourceName) = it.destructured

             if (resourceType == resType && resourceName == res) {
                 text = text.replaceRange(it.groups[1]!!.range, "$packageName.")

                 writeFile = true
             }


         }*/
        if (writeFile) {
            file.writeText(text)
        }


    }


    return modifiedFiles
}

val packageRegex = "package *= *\"([a-zA-Z0-9_.]+)\"".toRegex()
val moduleToPackageNameMapping = mutableMapOf<String, String>()
fun getPackageNameFromModule(projectDir: String, module: String): String {
    var packageName = moduleToPackageNameMapping[module] ?: ""
    if (packageName.isEmpty()) {
        File("$projectDir/$module/src/main/AndroidManifest.xml").forEachLine {
            val index = packageRegex.find(it)

            if (index != null) {
                packageName = index.destructured.component1()
                moduleToPackageNameMapping[module] = packageName
                return@forEachLine
            }
        }
    }

    return packageName
}

private fun fullyQualifyResources(
    files: MutableSet<String>,
    resType: String,
    resourcesToExclude: List<String>,
    packageName: String
) {
    val xmlRegex = "([a-zA-Z0-9_.]*)(R.)(dimen|drawable|color|string|style|raw|array)[.]([a-zA-Z0-9_]+)".toRegex()


    files.filter { it.endsWith(".xml") }.map { File(it) }
        .forEach { oldFile ->
            var writeFile = false
            val br = oldFile.bufferedReader()
            val newFile = File(oldFile.absolutePath + "_tmp").apply {
                createNewFile()
            }
            newFile.bufferedWriter().use { bw ->
                var line = br.readLine()

                while ((line) != null) {

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

                            line = line.replaceRange(it.groups[1]!!.range, "$packageName.")
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
                newFile.deleteOnExit()

        }

    val codeRegex =
        "([a-zA-Z0-9_]+[\\n\\r\\s]*\\.[a-zA-Z0-9_.\\n\\r\\s]*\\.[\\n\\r\\s]*|[a-zA-Z0-9_]*\\.?)(R[\\n\\r\\s]*.[\\n\\r\\s]*)(dimen|drawable|color|string|style|raw|array)[\\n\\r\\s]*\\.[\\n\\r\\s]*([a-zA-Z0-9_]+)".toRegex()

    files.filter { !it.endsWith(".xml") }.map { File(it) }
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
                    text = text.replaceRange(it.groups[1]!!.range, "$packageName.")
                    writeFile = true
                }

            }

            if (writeFile) {
                file.writeText(text)
            }

        }
}
