package dev.saifc.transformer

import dev.saifc.transformer.commands.Checker
import dev.saifc.transformer.commands.ResourceRemover
import dev.saifc.transformer.commands.Transformer
import dev.saifc.transformer.flags.Flag
import dev.saifc.transformer.flags.FlagParser
import dev.saifc.transformer.flags.ParsedFlags

fun main(args: Array<String>) {
    val commandsHelp= mapOf(
        Transformer.NAME to "${Transformer.NAME} --project=/path/to/project --base-module=baseModule [ --resource-types=${ResourceType.dimen},${ResourceType.drawable},${ResourceType.string},${ResourceType.raw},${ResourceType.color} ]",
        ResourceRemover.NAME to "${ResourceRemover.NAME} --project=/path/to/project --base-module=baseModule [ --resource-types=${ResourceType.dimen},${ResourceType.drawable},${ResourceType.string},${ResourceType.raw},${ResourceType.color} ]",
        Checker.NAME to "${Checker.NAME}  --project=/path/to/project --app-module=appModule"
    )
    val flags: ParsedFlags
    try {
        flags = FlagParser().parse(*args)
    } catch (e: FlagParser.FlagParseException) {
        System.err.println("Error while parsing the flags: " + e.message)
        return
    }

    try {
        when (flags.mainCommand) {
            ResourceRemover.NAME -> ResourceRemover(flags).invoke()
            Transformer.NAME -> Transformer(flags).invoke()
            Checker.NAME -> Checker(flags).invoke()
            else -> {
                println("Version 1.0.0")
                println("transformer <command>")
                println("Commands:")
                println(commandsHelp[Transformer.NAME])
                println("Moves resources to the appropriate modules while refactoring the affected code to reflect the changes.")
                println()
                println(commandsHelp[ResourceRemover.NAME])
                println("Removes a specific resource type from all modules except for the base module.")
                println()
                println(commandsHelp[Checker.NAME])
                println("Verifies the integrity of each module by executing verifyReleaseResources.")
            }
        }
    } catch (exception: Flag.RequiredFlagNotSetException) {
        println(exception.localizedMessage)
        if (commandsHelp.containsKey(flags.mainCommand)) {
            print("Usage: ")
            println("transformer ${commandsHelp[flags.mainCommand]}")
        }

    }
}





