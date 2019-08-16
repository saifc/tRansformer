package chaouachi.saif.transformer;


import java.io.*;

class ThreadedStreamHandler extends Thread {
    private InputStream inputStream;
    private String adminPassword;
    private OutputStream outputStream;
    private PrintWriter printWriter;
    private StringBuilder outputBuffer = new StringBuilder();
    private boolean sudoIsRequested = false;

    /**
     * A simple constructor for when the sudo command is not necessary.
     * This constructor will just run the command you provide, without
     * running sudo before the command, and without expecting a password.
     *
     * @param inputStream
     */
    ThreadedStreamHandler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Use this constructor when you want to invoke the 'sudo' command.
     * The outputStream must not be null. If it is, you'll regret it. :)
     * <p>
     * TODO this currently hangs if the admin password given for the sudo command is wrong.
     *
     * @param inputStream
     * @param outputStream
     * @param adminPassword
     */
    ThreadedStreamHandler(InputStream inputStream, OutputStream outputStream, String adminPassword) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.printWriter = new PrintWriter(outputStream);
        this.adminPassword = adminPassword;
        this.sudoIsRequested = true;
    }

    public void run() {
        // on mac os x 10.5.x, when i run a 'sudo' command, i need to write
        // the admin password out immediately; that's why this code is
        // here.
        if (sudoIsRequested) {
            printWriter.println(adminPassword);
            printWriter.flush();
        }

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outputBuffer.append(line).append("\n");
            }
        } catch (Throwable ioe) {
            // TODO handle this better
            ioe.printStackTrace();
        }
        // ignore this one
    }

    public StringBuilder getOutputBuffer() {
        return outputBuffer;
    }

}








