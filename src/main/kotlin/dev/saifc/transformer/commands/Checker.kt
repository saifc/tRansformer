package dev.saifc.transformer.commands

import dev.saifc.transformer.ModulesLister
import dev.saifc.transformer.flags.Flag
import dev.saifc.transformer.flags.OsPlatform
import dev.saifc.transformer.flags.ParsedFlags
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths


class Checker(flags: ParsedFlags) {

    private val projectDir =
        (PROJECT_LOCATION_FLAG.getValue(flags) ?: Paths.get(System.getProperty("user.dir"))).toAbsolutePath().toString()
    private val appModule = APP_MODULE_FLAG.getRequiredValue(flags)

    private val modulesLister = ModulesLister

    operator fun invoke() {
        val modules = modulesLister.list(projectDir).apply {
            remove(appModule)
        }

        var cmd = "$projectDir${File.separator}gradlew -p $projectDir "

        modules.forEach { module ->
            cmd += ":$module:verifyReleaseResources "

        }
        if (OsPlatform.currentPlatform == OsPlatform.WINDOWS)
            println("Operation unsupported on Windows. Execute the following in the command line:")
        println(cmd)
        if (OsPlatform.currentPlatform == OsPlatform.WINDOWS)
            return
        val run = Runtime.getRuntime()
        val pr = run.exec(cmd)
        pr.waitFor()
        val buf = BufferedReader(InputStreamReader(pr.inputStream))
        var line = buf.readLine()
        while (line != null) {
            println(line)
            line = buf.readLine()
        }


    }

    companion object {

        private val PROJECT_LOCATION_FLAG: Flag<Path> = Flag.path("project")
        private val APP_MODULE_FLAG: Flag<String> = Flag.string("app-module")
    }
}