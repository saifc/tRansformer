package chaouachi.saif.transformer

import java.io.File

fun File.isXml() : Boolean = name.endsWith(".xml")
fun File.isCode() : Boolean = name.endsWith(".java") || name.endsWith(".kt")
fun String.isCode() : Boolean = this.endsWith(".java") || this.endsWith(".kt")
fun String.isXml() : Boolean = this.endsWith(".xml")
