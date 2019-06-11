package chaouachi.saif.transformer.subcommands.base

import chaouachi.saif.transformer.PackageNameFinder
import chaouachi.saif.transformer.data.Usage
import chaouachi.saif.transformer.isXml
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

open class RefactorRemoveAndAppend(
    resType: String,
    projectDir: String,
    baseModule: String,
    private val valuesDirs: Sequence<File>,
    packageNameFinder: PackageNameFinder
) : BaseCommand(resType, projectDir, baseModule, packageNameFinder) {

    private val regex = "< *$resType .* *name *= *\"([a-zA-Z0-9_]+)\"".toRegex()

    override operator fun invoke(
        resources: Map<String, MutableList<Usage>>
    ) {
        val files = findResourceTypeFiles()

        val filesToAppendTo = findFilesToAppendTo(files, resources)

        appendToFiles(filesToAppendTo)

        fixInconsistencies(filesToAppendTo.keys)

        findAndReplace(resources)
    }

    private fun fixInconsistencies(files: Set<String>) {

        files.map { File(it) }.forEach { file ->
            val keys = HashSet<String>()
            var writeFile = false

            val builderFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = builderFactory.newDocumentBuilder()
            val doc = docBuilder.parse(file)
            val entries = doc.getElementsByTagName("*")

            for (index in entries.length downTo 0) {
                val item = entries.item(index) as Element?
                val name = item?.getAttribute("name")
                if (!name.isNullOrEmpty()) {
                    if (keys.contains(name)) {

                        val prevElem = item.previousSibling
                        if (prevElem != null &&
                            prevElem.nodeType == Node.TEXT_NODE &&
                            prevElem.nodeValue.trim().isEmpty()
                        ) {
                            item.parentNode.removeChild(prevElem)
                        }
                        item.parentNode.removeChild(item)

                        println("item: $name")
                        writeFile = true
                    } else {
                        keys.add(name)
                    }
                }
            }
            if (writeFile) {

                val transformerFactory = TransformerFactory.newInstance()
                val transformer = transformerFactory.newTransformer()
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
                transformer.setOutputProperty(OutputKeys.METHOD, "xml")
                transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

                val source = DOMSource(doc)
                val result = StreamResult(file)

                transformer.transform(source, result)
            }
        }
    }

    private fun findResourceTypeFiles(): Sequence<File> {
        return valuesDirs.flatMap {
            it.walk()
        }.filter {
            !it.isDirectory && it.nameWithoutExtension.contains(resType + "s") && it.isXml()
        }
    }

    private fun findFilesToAppendTo(
        files: Sequence<File>,
        resources: Map<String, MutableList<Usage>>
    ): MutableMap<String, String> {


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
        return filesToAppendTo
    }

    private fun appendToFiles(filesToAppendTo: MutableMap<String, String>) {
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
    }

    private fun findAndReplace(resources: Map<String, MutableList<Usage>>) {
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