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

    Migrator(flags).invoke()

}





