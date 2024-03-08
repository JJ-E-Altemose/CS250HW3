import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class SubmissionOneClient
{
    static Socket socket;
    static int seed;
    static int maxNumberOfMessages;
    static DataInputStream dataInput;
    static DataOutputStream dataOut;

    public static void main(String[] args)
    {
        String serverHostName;
        int port;
        int[] numArgs;
        InetAddress serverInetAddress;

        serverHostName = args[0];
        port = tryParseArgToNumbers(args[1]);

        socket = tryGetSocket(serverHostName, port);
        getSocketStreams();

        waitForConfig();
        configMessage();
    }

    private static void configMessage()
    {
        System.out.println(
                "Received config\n" +
                "number of messages = " + maxNumberOfMessages +
                "seed = " + seed);
    }

    private static void waitForConfig()
    {
        int numConfigsRecived = 0;
        while (numConfigsRecived != 1)
        {
            if(numConfigsRecived == 0)
            {
                maxNumberOfMessages = readNumber();
                numConfigsRecived++;
            }
            if (numConfigsRecived == 1)
            {
                seed = readNumber();
                numConfigsRecived++;
            }

        }
    }

    private static Integer readNumber()
    {
        try
        {
            return dataInput.readInt();
        }
        catch (Exception e)
        {
            sendMessageAndClose("A FATAL ERROR HAS OCCURRED 0x726561644E756D626572");
        }
        return null;
    }

    private static void getSocketStreams()//todo: split into 2 methods
    {
        try
        {
            dataInput = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
        }
        catch (Exception e)
        {
            sendMessageAndClose("A FATAL ERROR HAS OCCURRED 0x676574536F636B");
        }
    }

    private static Socket tryGetSocket(String serverHostName, int port)
    {

        try
        {
            InetAddress serverInetAddress = tryGetServer(serverHostName);
            return new Socket(serverInetAddress.getHostAddress(), port);
        }
        catch (Exception e)
        {
            sendMessageAndClose("A FATAL ERROR HAS OCCURRED 0x747279476574536F636B6574\n" +
                    "Unable to bind to port " + port);
        }
        return null;
    }

    private static InetAddress tryGetServer(String serverHostName)
    {
        try
        {
            return InetAddress.getByName(serverHostName);
        }
        catch (Exception e)
        {
            sendMessageAndClose("A FATAL ERROR HAS OCCURRED 0x747279476574536572766572 \n" +
                    "Error " + serverHostName + "can not be found");
        }
        return null;
    }

    private static int tryParseArgToNumbers(String arg)
    {
        try
        {
            return parseArgToNumbers(arg);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error One of the args is not a valid integer.");
        }
    }

    private static int parseArgToNumbers(String arg)
    {
        return Integer.parseInt(arg);
    }

    private static void sendMessageAndClose(String message)
    {
        System.out.println(message);
        System.exit(-1);
    }
}
