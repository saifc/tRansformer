package dev.saifc.transformer.flags

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * Represents a command-line flag of a specified type.
 *
 * @param <T> the type that the flag should be parsed as (e.g. string, file path etc).
</T> */
abstract class Flag<T> internal constructor(val name: String) {

    override fun toString(): String {
        return name
    }

    /**
     * Returns the flag value. Throws if the flag absent.
     *
     * @return the value of the flag
     * @throws RequiredFlagNotSetException if the flag was not set
     */
    fun getRequiredValue(flags: ParsedFlags): T {
        return getValue(flags) ?: throw RequiredFlagNotSetException(name)
    }

    /**
     * Returns the flag value wrapped in the [Optional] class.
     *
     *
     * Empty [Optional] means that the flag was not specified.
     */
    abstract fun getValue(flags: ParsedFlags): T?

    /**
     * Abstract class for flags that can hold only a single value.
     */
    internal abstract class SingleValueFlag<T>(name: String) : Flag<T>(name) {

        override fun getValue(flags: ParsedFlags): T? {
            val value = flags.getFlagValue(name)
            return if (value != null)
                parse(value)
            else null
        }

        abstract fun parse(value: String): T
    }

    internal class PathFlag(name: String) : SingleValueFlag<Path>(name) {

        override fun parse(value: String): Path {
            var value = value

            if (OsPlatform.currentPlatform != OsPlatform.WINDOWS) {
                value = HOME_DIRECTORY_ALIAS
                    .matcher(value)
                    .replaceFirst(Matcher.quoteReplacement(System.getProperty("user.home")))
            }

            value= PARENT_DIRECTORY_ALIAS
                .matcher(value)
                .replaceFirst(Matcher.quoteReplacement(File(System.getProperty("user.dir")).parent))

            value = LOCAL_DIRECTORY_ALIAS
                .matcher(value)
                .replaceFirst(Matcher.quoteReplacement(System.getProperty("user.dir")))

            return Paths.get(value)
        }
    }

    internal class StringFlag(name: String) : SingleValueFlag<String>(name) {

        override fun parse(value: String): String {
            return value
        }
    }

    /**
     * Flag that can contain multiple comma-separated values.
     */
    internal class ListFlag<T>(private val singleFlag: SingleValueFlag<T>) : SingleValueFlag<List<T>>(singleFlag.name) {

        override fun parse(value: String): List<T> {
            return if (value.isEmpty()) {
                ArrayList()
            } else value.split(',')
                .stream()
                .map<T> { singleFlag.parse(it) }
                .collect(Collectors.toList())
        }
    }

    /**
     * Exception thrown when a required flag value is attempted to be read.
     *
     * @see Flag.getRequiredValue
     */
    class RequiredFlagNotSetException internal constructor(flagName: String) :
        FlagParser.FlagParseException(String.format("Missing the required --%s flag.", flagName))

    companion object {

        private val HOME_DIRECTORY_ALIAS = Pattern.compile("^~")
        private val PARENT_DIRECTORY_ALIAS = Pattern.compile("^\\.\\.")
        private val LOCAL_DIRECTORY_ALIAS = Pattern.compile("^\\.")

        /**
         * Path flag holding a single value.
         */
        fun path(name: String): Flag<Path> {
            return PathFlag(name)
        }

        /**
         * String flag holding a single value.
         */
        fun string(name: String): Flag<String> {
            return StringFlag(name)
        }

        /**
         * String flag holding a list of comma-delimited values.
         */
        fun stringList(name: String): Flag<List<String>> {
            return ListFlag(StringFlag(name))
        }
    }
}