package chaouachi.saif.transformer.commands

import chaouachi.saif.transformer.ModulesLister
import chaouachi.saif.transformer.SystemCommandExecutor
import chaouachi.saif.transformer.flags.Flag
import chaouachi.saif.transformer.flags.ParsedFlags
import java.nio.file.Path
import java.nio.file.Paths
import java.util.ArrayList

class Checker(flags: ParsedFlags) {

    private val projectDir =
        (PROJECT_LOCATION_FLAG.getValue(flags) ?: Paths.get(System.getProperty("user.dir"))).toAbsolutePath().toString()
    private val appModule = APP_MODULE_FLAG.getRequiredValue(flags)

    private val modulesLister = ModulesLister

    operator fun invoke() {
        val modules = modulesLister.list(projectDir).apply {
            remove(appModule)
        }


        modules.forEach { module ->
           val cmd = "$projectDir/gradlew -p $projectDir :$module:verifyReleaseResources"
            println(cmd)

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


    }

    companion object {

        private val PROJECT_LOCATION_FLAG: Flag<Path> = Flag.path("project")
        private val APP_MODULE_FLAG: Flag<String> = Flag.string("app-module")
    }
}