package chaouachi.saif.transformer.flags


import java.util.ArrayList
import java.util.Arrays

/**
 * Utility for flag parsing, specific to the Bundle Tool.
 *
 *
 * The flags follow the below convention:
 *
 *
 * [bundle-tool] [command1] [command2] .. [command-n] [--flag1] [--flag2=v2] [--flag3] [v3]..
 * [--flagn] where:
 *
 *
 *  * commands: cannot start with "-".
 *  * flags: have to start with "--". They can have the format "--flag=value" or "--flag value",
 * but when "=" is omitted, values cannot start with "--". A value does not have to be set
 * and is empty string by default.
 *
 */
class FlagParser {

    /**
     * Parses the given arguments populating the structures.
     *
     * @throws FlagParseException if the input does not represent parsable command line arguments
     */
    fun parse(vararg args: String): ParsedFlags {
        val commands = ArrayList<String>()
        // Need to wrap it into a proper list implementation to be able to remove elements.
        val argsToProcess = ArrayList(Arrays.asList(*args))
        while (argsToProcess.size > 0 && !argsToProcess[0].startsWith("-")) {
            commands.add(argsToProcess[0])
            argsToProcess.removeAt(0)
        }
        return ParsedFlags(commands, parseFlags(argsToProcess))
    }

    private fun HashMap<String, List<String>>.put(key: String, element: String) {
        var list = this[key]
        if (list == null) {
            list = mutableListOf(element)
            this[key] = list
        } else {
            (list as MutableList).add(element)
        }
    }

    private fun parseFlags(args: List<String>): ImmutableListMultimap<String, String> {
        val flagMap = HashMap<String, List<String>>()
        var lastFlag: String? = null
        for (arg in args) {
            if (arg.startsWith("--")) {
                if (lastFlag != null) {
                    flagMap.put(lastFlag, "")
                    lastFlag = null
                }
                if (arg.contains(KEY_VALUE_SEPARATOR)) {
                    val segments = arg.split(KEY_VALUE_SEPARATOR)
                    flagMap.put(segments[0].substring(2), segments[1])
                } else {
                    lastFlag = arg.substring(2)
                }
            } else {
                if (lastFlag == null) {
                    throw FlagParseException(
                        String.format("Syntax error: flags should start with -- (%s)", arg)
                    )
                } else {
                    flagMap.put(lastFlag, arg)
                    lastFlag = null
                }
            }
        }
        if (lastFlag != null) {
            flagMap.put(lastFlag, "")
        }
        return flagMap
    }

    /** Exception encapsulating any flag parsing errors.  */
    open class FlagParseException(message: String) : IllegalStateException(message)

    companion object {

        private val KEY_VALUE_SEPARATOR = "="
    }
}