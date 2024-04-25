package cs250.hw3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

public class TCPClient
{
    static Socket socket;
    static int seed;
    static int maxNumberOfMessages;
    static DataInputStream dataInput;
    static DataOutputStream dataOut;
    static Random randomGenerator;
    static long senderSum;
    static int numOfSentMessages;

    static int clientID;
    static int NumberOfForwards = maxNumberOfMessages;
    static ArrayList<Integer> forwardedMessages;
    static long forwardedMessagesSum;

    /**CONFIG INFO**/
    static boolean submission3 = true;
    static boolean submission2 = true || submission3;
    static boolean submission1 = true || submission2;
    /**           **/

    private static void run(String[] args) throws IOException
    {
        if(submission1)
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
        if(submission2)
        {
            wait10Seconds();

            sendMessages();

            messageStatistics();
        }
        if(submission3)
        {
            //listenForForwardInfo();

            receiveForwardedMessages();

            receivedMessageStatistics();
        }
    }

    public static void main(String[] args)// need to make close cleanly
    {
        try
        {
            run(args);
        }
        catch(Exception e)
        {
            e.getMessage();
        }
    }

    private static void receivedMessageStatistics()
    {
        System.out.println("Total messages received: " + forwardedMessages.size());
        System.out.println("Sum of messages received: " + forwardedMessagesSum);
    }

    private static void receiveForwardedMessages() throws IOException
    {
        System.out.println("Starting to listen for messages from server...");
        forwardedMessages = new ArrayList<>();
        forwardedMessagesSum = 0;
        for (int numForwardedMessagesRecived = 0; numForwardedMessagesRecived < NumberOfForwards; numForwardedMessagesRecived++)
        {
            int number = readNumber();
            forwardedMessagesSum += number;
            forwardedMessages.add(number);
        }
        System.out.println("Finished listening for messages from server.");
    }
    /*
    private static void listenForForwardInfo() throws IOException
    {
        NumberOfForwards = readNumber();
    }
     */

    private static void messageStatistics()
    {
        System.out.println("Total messages sent: " + numOfSentMessages);
        System.out.println("Sum of messages sent: " + senderSum);
    }

    private static void sendMessages() throws IOException
    {
        System.out.println("Starting to send messages to server...");
        senderSum = 0;
        for(numOfSentMessages = 0;numOfSentMessages < maxNumberOfMessages;numOfSentMessages++)
        {
            //while(readNumber() != clientID) {}
            //tryFlushOut();
            int randomNumber = randomGenerator.nextInt();
            senderSum += randomNumber;
            trySend(randomNumber);
        }
        System.out.println("Finished sending messages to server.");
    }
    private static void tryFlushOut() throws IOException
    {
        dataOut.flush();
    }

    private static void trySend(int number) throws IOException
    {
        dataOut.writeInt(number);
    }

    private static void wait10Seconds()
    {
        long time  = System.currentTimeMillis();
        while (time - System.currentTimeMillis() > -10000) {}
    }

    private static void configMessage()
    {
        System.out.println(
                "Received config\n" +
                "number of messages = " + maxNumberOfMessages + "\n" +
                "seed = " + seed);
    }

    private static void waitForConfig() throws IOException
    {
        maxNumberOfMessages = readNumber();
        seed = readNumber();
        randomGenerator = new Random(seed);
    }

    private static Integer readNumber() throws IOException
    {
        return dataInput.readInt();
    }

    private static void getSocketStreams() throws IOException//todo: split into 2 methods
    {
        dataInput = new DataInputStream(socket.getInputStream());
        dataOut = new DataOutputStream(socket.getOutputStream());
    }

    private static Socket tryGetSocket(String serverHostName, int port) throws IOException
    {
        InetAddress serverInetAddress = tryGetServer(serverHostName);
        Socket socket = new Socket(serverInetAddress.getHostAddress(), port);
        return socket;
    }

    private static InetAddress tryGetServer(String serverHostName) throws UnknownHostException
    {
        return InetAddress.getByName(serverHostName);
    }

    private static int tryParseArgToNumbers(String arg)
    {
        return parseArgToNumbers(arg);
    }

    private static int parseArgToNumbers(String arg)
    {
        return Integer.parseInt(arg);
    }
}
