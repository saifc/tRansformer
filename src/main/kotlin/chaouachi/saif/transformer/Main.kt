package chaouachi.saif.transformer

import chaouachi.saif.transformer.commands.ResourceRemover
import chaouachi.saif.transformer.commands.Transformer
import chaouachi.saif.transformer.flags.FlagParser
import chaouachi.saif.transformer.flags.ParsedFlags

fun main(args: Array<String>) {

    val flags: ParsedFlags
    try {
        flags = FlagParser().parse(*args)
    } catch (e: FlagParser.FlagParseException) {
        System.err.println("Error while parsing the flags: " + e.message)
        return
    }

    when (flags.mainCommand) {
        "help" -> println("transformer --project=/path/to/project --base-module=baseModule [ resource-types=${ResourceType.dimen},${ResourceType.drawable},${ResourceType.string},${ResourceType.raw},${ResourceType.color} ]")
        "remove" -> ResourceRemover(flags).invoke()
        else -> Transformer(flags).invoke()
    }

}





