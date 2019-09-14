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
        else -> println("transformer --project=/path/to/project --base-module=baseModule [ --resource-types=${ResourceType.dimen},${ResourceType.drawable},${ResourceType.string},${ResourceType.raw},${ResourceType.color} ]")
    }

}





