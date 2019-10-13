package dev.saifc.transformer

import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilderFactory

object ResourceFinder {
    fun findModuleResources(
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

                        if (File(file).isHidden)
                            continue
                        val dbFactory = DocumentBuilderFactory.newInstance()
                        val dBuilder = dbFactory.newDocumentBuilder()
                        val xmlInput = FileInputStream("${it.absolutePath}/$file")
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
}