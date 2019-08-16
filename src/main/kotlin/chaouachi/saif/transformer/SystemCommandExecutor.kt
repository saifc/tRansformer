package chaouachi.saif.transformer

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SystemCommandExecutor(private val commandInformation: List<String>?) {
    private val adminPassword: String?
    private var inputStreamHandler: ThreadedStreamHandler? = null
    private var errorStreamHandler: ThreadedStreamHandler? = null

    /**
     * Get the standard output (stdout) from the command you just exec'd.
     */
    val standardOutputFromCommand: StringBuilder
        get() = inputStreamHandler!!.outputBuffer

    /**
     * Get the standard error (stderr) from the command you just exec'd.
     */
    val standardErrorFromCommand: StringBuilder
        get() = errorStreamHandler!!.outputBuffer

    init {
        if (commandInformation == null) throw NullPointerException("The commandInformation is required.")
        this.adminPassword = null
    }

    @Throws(IOException::class, InterruptedException::class)
    fun executeCommand(): Int {
        var exitValue = -99

        try {
            val pb = ProcessBuilder(commandInformation)
            val process = pb.start()

            // you need this if you're going to write something to the command's input stream
            // (such as when invoking the 'sudo' command, and it prompts you for a password).
            val stdOutput = process.outputStream

            // i'm currently doing these on a separate line here in case i need to set them to null
            // to get the threads to stop.
            // see http://java.sun.com/j2se/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
            val inputStream = process.inputStream
            val errorStream = process.errorStream

            // these need to run as java threads to get the standard output and error from the command.
            // the inputstream handler gets a reference to our stdOutput in case we need to write
            // something to it, such as with the sudo command
            inputStreamHandler = ThreadedStreamHandler(inputStream, stdOutput, adminPassword)
            errorStreamHandler = ThreadedStreamHandler(errorStream)

            // TODO the inputStreamHandler has a nasty side-effect of hanging if the given password is wrong; fix it
            inputStreamHandler!!.start()
            errorStreamHandler!!.start()

            // TODO a better way to do this?
            exitValue = process.waitFor()

            // TODO a better way to do this?
            inputStreamHandler!!.interrupt()
            errorStreamHandler!!.interrupt()
            inputStreamHandler!!.join()
            errorStreamHandler!!.join()
        } catch (e: IOException) {
            // TODO deal with this here, or just throw it?
            throw e
        } catch (e: InterruptedException) {
            throw e
        } finally {
            return exitValue
        }
    }


}






