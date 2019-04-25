import java.io.File
import java.util.ArrayList


fun main() {
    val baseModule = "core"
    val projectDir = "/Users/saif/pot_commun_android"
    val valuesDirs = File("$projectDir/$baseModule").walk()
        .filter {
            it.isDirectory && it.name.startsWith("values") && it.parent.contains("res")
        }

    refactorDimens(valuesDirs, projectDir)
}

private fun refactorDimens(
    valuesDirs: Sequence<File>,
    projectDir: String
) {


    val dimensFiles = valuesDirs.flatMap {
        it.walk()
    }.filter {
        !it.isDirectory && it.nameWithoutExtension.startsWith("dimens") && it.extension == "xml"
    }


    val dimenRegex = "< *dimen *name *= *\"([a-zA-Z0-9_.]+)\"".toRegex()

    val folder = "$projectDir/app/build/generated/not_namespaced_r_class_sources/debug/processDebugResources/r"
    dimensFiles.forEach { file ->

        val br = file.bufferedReader()


        var line = br.readLine()
        while (line != null) {

            val matches = dimenRegex.findAll(line)
            matches.forEach {

                val (dimenName) = it.destructured
                val cmd = "grep -r -s \"$dimenName\" $folder  | grep  -v \"fr/lepotcommun/lpc\""


                // you need a shell to execute a command pipeline
                val commands = ArrayList<String>()
                commands.add("/bin/sh")
                commands.add("-c")
                commands.add(cmd)

                val commandExecutor = SystemCommandExecutor(commands)
                val result = commandExecutor.executeCommand()

                if (result == 0) {
                    val stdout = commandExecutor.standardOutputFromCommand
                    println(stdout)
                }else {
                    val stderr = commandExecutor.standardErrorFromCommand
                    if (!stderr.isBlank()) {
                        println("STDERR")
                        println(stderr)
                    }
                }

            }

            line = br.readLine()


        }


    }


}