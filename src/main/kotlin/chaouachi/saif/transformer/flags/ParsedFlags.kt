package chaouachi.saif.transformer.flags

import java.util.HashSet

typealias  ImmutableListMultimap<T, V> = Map<T, List<V>>

/**
 * Represents the result of parsing the command line arguments of this invocation of the tool.
 *
 *
 * You should not need to use this class directly, flag values should be accessed via the `Flag` class.
 *
 * <pre>
 * static Flag<String> MY_FLAG = Flag.string("myFlag")
 *
 * void execute(chaouachi.saif.transformer.ParsedFlags flags) {
 * Optional<String> flagValue = MY_FLAG.value(flags);
 * ...
 * }
</String></String></pre> *
 */
class ParsedFlags internal constructor(
    val commands: List<String>,
    private val flags: ImmutableListMultimap<String, String>
) {

    private val accessedFlags = HashSet<String>()

    /**
     * Returns the first command provided on the command line if provided.
     *
     */
    val mainCommand: String?
        get() = getSubCommand(0)

    /**
     * Returns the second command provided on the command line if provided.
     *
     */
    val subCommand: String?
        get() = getSubCommand(1)

    private fun getSubCommand(index: Int): String? {
        return if (index < commands.size) commands[index] else null
    }

    /**
     * Gets value of the flag, if it has been set.
     *
     * @throws FlagParser.FlagParseException if the flag has been set multiple times
     */
    fun getFlagValue(name: String): String? {
        val values = getFlagValues(name)
        return when (values.size) {
            0 -> null
            1 -> values[0]
            else -> throw FlagParser.FlagParseException(
                String.format(
                    "Flag --%s has been set more than once.",
                    name
                )
            )
        }
    }

    private fun getFlagValues(name: String): List<String> {
        accessedFlags.add(name)
        return flags[name] ?: emptyList()
    }
}