package dev.saifc.transformer

import dev.saifc.transformer.commands.Checker
import dev.saifc.transformer.commands.ResourceRemover
import dev.saifc.transformer.commands.Transformer
import dev.saifc.transformer.flags.FlagParser
import dev.saifc.transformer.flags.ParsedFlags

fun main(args: Array<String>) {

    val flags: ParsedFlags
    try {
        flags = FlagParser().parse(*args)
    } catch (e: FlagParser.FlagParseException) {
        System.err.println("Error while parsing the flags: " + e.message)
        return
    }

    when (flags.mainCommand) {
        "remove" -> ResourceRemover(flags).invoke()
        "transform" -> Transformer(flags).invoke()
        "verify" -> Checker(flags).invoke()
        else -> {
            println("transformer <command>")
            println("Commands:")
            println("transform --project=/path/to/project --base-module=baseModule [ --resource-types=${ResourceType.dimen},${ResourceType.drawable},${ResourceType.string},${ResourceType.raw},${ResourceType.color} ]")
            println("Moves resources to the appropriate modules while refactoring the affected code to reflect the changes.")
            println()
            println("remove --project=/path/to/project --base-module=baseModule [ --resource-types=${ResourceType.dimen},${ResourceType.drawable},${ResourceType.string},${ResourceType.raw},${ResourceType.color} ]")
            println("Removes a specific resource type from all modules except for the base module.")
            println()
            println("verify --project=/path/to/project --app-module=appModule")
            println("Verifies the integrity of each module by executing verifyReleaseResources.")
        }
    }

}





