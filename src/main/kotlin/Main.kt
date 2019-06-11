import flags.FlagParser
import flags.ParsedFlags

fun main(args: Array<String>) {

    val flags: ParsedFlags
    try {
        flags = FlagParser().parse(*args)
    } catch (e: FlagParser.FlagParseException) {
        System.err.println("Error while parsing the flags: " + e.message)
        return
    }

    if (flags.mainCommand == "help")
        println("transformer --project=/path/to/project --base-module=baseModule [ resource-types=${ResourceType.dimen},${ResourceType.drawable},${ResourceType.string},${ResourceType.raw},${ResourceType.color} ]")
    else
        Transformer(flags).invoke()
}





