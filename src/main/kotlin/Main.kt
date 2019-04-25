import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.xml.parsers.DocumentBuilderFactory


fun main(args: Array<String>) {
    refactor()

    println("Step 1 done")
    readLine()
    val modules = arrayOf(
        "app",
        "materialsearchview",
        "auth",
        "hiw",
        "about",
        "createpot",
        "contribute",
        "giftcard",
        "notifications",
        "contributors",
        "thankcontributors",
        "pots",
        "potdetails",
        "potoffer",
        "potuse",
        "account",
        "wiretransfer",
        "partners",
        "prices",
        "confirmation",
        "marketingcampaign",
        "materialstepperview",
        "actionmenu"
    )
    val baseModule = "core"
    val projectDir = "/Users/saif/pot_commun_android"
    val basePackageName = getPackageNameFromModule(projectDir, baseModule)


    val resRegex =
        "([a-zA-Z0-9_.]*)R\\.(dimen|drawable|color|string|style|raw|array|anim|layout|bool)\\.([a-zA-Z0-9_]+)".toRegex()
    val types = mutableSetOf<String>()
    modules.forEach { module ->
        println("$module:")
        val moduleResources = findModuleResources(projectDir, module)
        //val packageName = getPackageNameFromModule(projectDir, module)

        types.addAll(moduleResources.keys)

        File("$projectDir/$module").walk()
            .filter { !it.isDirectory && (it.name.endsWith(".xml") || it.name.endsWith(".java") || it.name.endsWith(".kt")) }
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
                            if (isLocalResource != true && resourceType == "style") {
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

        //  convertToRIfNeeded(module)
    }

    //refactor()
    println("Step 2 done")
    readLine()
    modules.forEach { module ->
        println("in module $module")
        convertToRIfNeeded(module)
    }
    println(types)
}

private fun convertToRIfNeeded(module: String) {
    val baseModule = "core"
    val projectDir = "/Users/saif/pot_commun_android"
    val basePackageName = getPackageNameFromModule(projectDir, baseModule)


    val map = findModuleResources(projectDir, module)

    if (module == "contribute") {
        println("convertToRIfNeeded:")
        println(map)
    }


    val resRegex = "(@)(dimen|drawable|color|string)(/)([a-zA-Z0-9_]+)[:\\s},)]".toRegex()

    val projectFile = File("$projectDir/$module")
    projectFile.walk()
        .filter { !it.isDirectory && it.name.endsWith(".xml") && !it.isHidden && it.parent.contains("layout") }
        .forEach { file ->

            val br = file.bufferedReader()

            var line = br.readLine()

            var writeFile = false

            val newFile = File(file.absolutePath + "_tmp").apply {
                createNewFile()
            }

            newFile.bufferedWriter().use { bw ->

                while ((line) != null) {

                    val matches = resRegex.findAll(line).toList().reversed()
                    matches.forEach {
                        val (_, resourceType, _, resourceName) = it.destructured

                        if (file.nameWithoutExtension == "contribute_pot_activity") {
                            println("bufferedWriter:")
                            println(module)
                            println(map)
                        }
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

private fun findModuleResources(
    projectDir: String,
    module: String
): MutableMap<String, MutableSet<String>> {
    val map = mutableMapOf<String, MutableSet<String>>()
    File("$projectDir/$module").walk()
        .filter {
            it.isDirectory && it.parent.contains("res")
        }.forEach {
            if (it.name.startsWith("values")) {
                for (file in it.list()) {
                    //TODO windowsify
                    if (file.substringBefore(".") == "")
                        continue
                    val dbFactory = DocumentBuilderFactory.newInstance()
                    val dBuilder = dbFactory.newDocumentBuilder()
                    val xmlInput = FileInputStream(it.absolutePath + "/" + file)
                    val doc = dBuilder.parse(xmlInput)
                    doc.documentElement.normalize()
                    val nodes = doc.getElementsByTagName("resources").item(0).childNodes
                    for (i in 0 until nodes.length) {
                        val child = nodes.item(i)
                        val name = child.nodeName
                        if (!name.startsWith("#")) {
                            val element =
                                if (name != "item")
                                    name
                                else
                                    (child as Element).getAttribute("type")

                            map.putElement(element, (child as Element).getAttribute("name"))
                        }

                    }
                }


            } else {

                val name = if (it.name.contains("-")) {
                    it.name.substringBefore("-")
                } else {
                    it.name
                }
                for (file in it.list()) {
                    map.putElement(name, file.substringBeforeLast("."))
                }
            }

        }
    return map
}

private fun MutableMap<String, MutableSet<String>>.putElement(key: String, element: String) {
    var value = this[key]


    if (value == null) {
        value = mutableSetOf(element)
        this[key] = value
    } else {
        value.add(element)
    }
}


private fun refactor() {
    val namespaceDimens = true
    val namespaceDrawable = true
    val namespaceStrings = true
    val namespaceRaws = true
    val namespaceColors = true

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

    if (namespaceRaws) {
        refactorRaws(usages.getMonoModuleRaws(), projectDir, baseModule, basePackageName)
    }

    if (namespaceStrings) {
        refactorStrings(usages.getMonoModuleStrings(), valuesDirs, projectDir, baseModule, basePackageName)
    }

    if (namespaceColors) {
        refactorColors(usages.getMonoModuleColors(), valuesDirs, projectDir, baseModule, basePackageName)
    }
}

fun refactorColors(
    colors: Map<String, MutableList<Usage>>,
    valuesDirs: Sequence<File>,
    projectDir: String,
    baseModule: String,
    basePackageName: String
) {
    val affectedFiles = refactorMove(projectDir, baseModule, colors, ResourceType.color, basePackageName)
    val files = valuesDirs.flatMap {
        it.walk()
    }.filter {
        !it.isDirectory && it.nameWithoutExtension.contains("colors") && it.extension == "xml"
    }


    val regex = "< *color .*name *= *\"([a-zA-Z0-9_]+)\"".toRegex()

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
                    val res = colors[resName]
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


    colors.forEach { string ->
        val entry = string.value[0]
        val packageName = getPackageNameFromModule(projectDir, entry.module)
        affectedFiles.addAll(
            findAndReplaceRImports(
                basePackageName,
                packageName,
                entry.files,
                string.key,
                ResourceType.color
            )
        )
    }


    fullyQualifyResources(affectedFiles, ResourceType.color, colors.map { it.key }, basePackageName)
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
                newFile.delete()
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
    val affectedFiles = refactorMove(projectDir, baseModule, raws, ResourceType.raw, basePackageName)
    fullyQualifyResources(affectedFiles, ResourceType.raw, raws.map { it.key }, basePackageName)

}

private fun refactorDrawables(
    drawables: Map<String, MutableList<Usage>>,
    projectDir: String,
    baseModule: String,
    basePackageName: String
) {
    val affectedFiles = refactorMove(projectDir, baseModule, drawables, ResourceType.drawable, basePackageName)
    fullyQualifyResources(affectedFiles, ResourceType.drawable, drawables.map { it.key }, basePackageName)
}

private fun refactorMove(
    projectDir: String,
    baseModule: String,
    resources: Map<String, MutableList<Usage>>,
    resType: String,
    basePackageName: String
): MutableSet<String> {
    val affectedFiles = mutableSetOf<String>()
    val dirs = File("$projectDir/$baseModule").walk()
        .filter {
            it.isDirectory && it.name.startsWith(resType) && it.parent.contains("res")
        }
    resources.forEach { raw ->

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
                resType
            )
        )

    }

    return affectedFiles

}


private fun refactorDimens(
    dimens: Map<String, MutableList<Usage>>,
    valuesDirs: Sequence<File>,
    projectDir: String,
    baseModule: String,
    basePackageName: String
) {


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
                newFile.delete()
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

    val affectedFiles = mutableSetOf<String>()

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
    projectFile.walk()
        .filter { !it.isDirectory && (it.name.endsWith(".kt") || it.name.endsWith(".java")) && !it.path.contains("/test/") }
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
            newFile.delete()
    }

    val importRCodeRegex =
        "import +($corePackageName)\\.(R([\\n\\r\\s]+as[\\n\\r\\s]+[a-zA-Z0-9_]+)?|R;) *\\n".toRegex()

    val localImportRCodeRegex = "import +$packageName\\.R;?\n".toRegex()
    val importRegex = "import [a-zA-Z0-9_.]+\n".toRegex()


    //TODO extract
    val codeRegex =
        "(([a-zA-Z0-9_]+[\\n\\r\\s]*\\.[\\n\\r\\s]*)+|[a-zA-Z0-9_]*)(R[\\n\\r\\s]*.[\\n\\r\\s]*)(dimen|drawable|color|string|style|raw|array)[\\n\\r\\s]*\\.[\\n\\r\\s]*([a-zA-Z0-9_]+)([\\n\\r\\s]|\\)|;|,)".toRegex()

    files.filter { !it.endsWith(".xml") && !it.contains("/test/") }.map { File(it) }.forEach { file ->
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
                newFile.delete()

        }

    val codeRegex =
        "([a-zA-Z0-9_]+[\\n\\r\\s]*\\.[a-zA-Z0-9_.\\n\\r\\s]*\\.[\\n\\r\\s]*|[a-zA-Z0-9_]*\\.?)(R[\\n\\r\\s]*.[\\n\\r\\s]*)(dimen|drawable|color|string|style|raw|array)[\\n\\r\\s]*\\.[\\n\\r\\s]*([a-zA-Z0-9_]+)".toRegex()

    files.filter { !it.endsWith(".xml") && !it.contains("/test/") }.map { File(it) }
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
