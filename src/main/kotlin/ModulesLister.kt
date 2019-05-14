import java.io.File

object ModulesLister {

    fun list(projectDir: String): List<String> {

        val modules = mutableListOf<String>()
        //TODO support kts
        val settingsFile = File("$projectDir/settings.gradle")

        settingsFile.forEachLine { line ->
            if (line.trim().startsWith("include")) {
                line.removePrefix("include").split(",").forEach {
                    modules.add(it.trim().replace("'", "").replace("\"", "").removePrefix(":"))
                }
                return@forEachLine
            }
        }

        return modules
    }
}