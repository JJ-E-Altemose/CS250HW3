import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class TCPServer
{
    //todo: CleanUp method invalid number of args clean up parse args clean main
    static DataInputStream dataInput;
    static DataOutputStream dataOut;
    static Scanner userInput = new Scanner(System.in);
    static Client[] clients = new Client[2];
    static ServerSocket serverSocket;
    static Random randomGenerator;

    static int maxNumberOfMessages;

    public static void main(String[] args)
    {
        int[] numberArgs;
        int portNumber, seed;

        numberArgs = tryParseArgsToNumbers(args);

        portNumber = numberArgs[0];
        seed = numberArgs[1];
        maxNumberOfMessages = numberArgs[2];

        initializationMessage(portNumber);

        validatePortAndBind(portNumber);
        randomGenerator.setSeed(seed);

        awaitClientConnections();
        sendConfigToClients();
        clientStatsMessage();

    }

    private static void sendConfigToClients()
    {
        sendClientsMaxNumberOfMessages();
        sendClientSeeds();
    }

    private static void clientStatsMessage()
    {
        for (Client client : clients)
        {
            InetAddress clientInetAddress = client.getSocket().getInetAddress();
            int clientSeed = client.getClientSeed();
            System.out.println(clientInetAddress + " " + clientSeed);
        }
        System.out.println("Finished sending config to clients.");
    }

    private static void sendClientsMaxNumberOfMessages()
    {
        for(int index = 0; index < clients.length; index++)
        {
            sendClientMaxNumberOfMessages(index);
        }
    }

    private static void sendClientMaxNumberOfMessages(int clientIndex)
    {
        Client client = clients[clientIndex];
        client.trySendNumber(maxNumberOfMessages);
    }

    private static void sendClientSeeds()
    {
        for(int index = 0; index < clients.length; index++)
        {
            sendClientSeed(index);
        }
    }

    private static void sendClientSeed(int clientIndex)
    {
        Client client = clients[clientIndex];
        client.setClientSeed(randomGenerator.nextInt());
        client.trySendNumber(client.getClientSeed());
    }

    private static void awaitClientConnections()
    {
        System.out.println("waiting for client...");

        int numberOfClientsConnected = 0;
        while (numberOfClientsConnected != 2)
        {
            Socket connectedSocket = acceptConnection();
            clients[numberOfClientsConnected] = new Client(connectedSocket);
            numberOfClientsConnected++;
        }

        clientsConnectedMessage();
    }
    private static void clientsConnectedMessage()
    {
        String message =
                "Clients Connected!\n" +
                "Sending config to clients...";

        System.out.println(message);
    }

    private static Socket acceptConnection()
    {
        try
        {
            return serverSocket.accept();
        }
        catch (Exception e)
        {
            sendMessageAndClose("A FATAL ERROR HAS OCCURRED 0x616363657074436F6E6E656374696F6E");
        }
        return null;
    }

    private static void initializationMessage(int portNumber)
    {
        InetAddress inetAddress = tryGetIPAddress();
        String hostname = inetAddress.getHostName();
        String ip = inetAddress.getHostAddress();
        String initializationMessage =
                "IP Address: " + hostname + "/" + ip + "\n" +
                "Port Number " + portNumber;

        System.out.println(initializationMessage);
    }

    private static InetAddress tryGetIPAddress()
    {
        try
        {
            return InetAddress.getLocalHost();
        }
        catch (Exception e)
        {
            sendMessageAndClose("A FATAL ERROR HAS OCCURRED 0x747279476574495041646472657373");
        }
        return null;
    }

    //<editor-fold desc="PortValidation">
    private static void validatePortAndBind(int port)
    {
        if(!validPortNumber(port))
        {
            sendMessageAndClose("Error port numbers must be between 1024-65535 not " + port);
        }
        else
        {
            tryBindPort(port);
        }

    }

    private static boolean validPortNumber(int port)
    {
        int portMin = 1024;
        int portMax = 65535;
        return (port > portMin && port <= portMax);
    }

    private static void tryBindPort(int port)
    {
        try
        {
            serverSocket = new ServerSocket(port);
        }
        catch (Exception e)
        {
            sendMessageAndClose("Address already in use (Bind failed)");
        }
    }
    //</editor-fold>

    private static int[] tryParseArgsToNumbers(String[] args)
    {
        try
        {
            return parseArgsToNumbers(args);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error One of the args is not a valid integer.");
        }
    }

    private static int[] parseArgsToNumbers(String[] args)
    {
        int[] numberArgs = new int[args.length];
        for (int index = 0; index < args.length; index++)
        {
            numberArgs[index] = Integer.parseInt(args[index]);
        }
        return numberArgs;
    }

    private static void sendMessageAndClose(String message)
    {
        System.out.println(message);
        System.exit(-1);
    }

}
class Client
{
    Socket socket;
    DataInputStream dataInput;
    DataOutputStream dataOut;
    int clientSeed;

    public Client(Socket socket)
    {
        this.socket = socket;
        this.dataInput = tryGetInputStream();
        this.dataOut = tryGetOutputStream();
    }

    public int getClientSeed()
    {
        return clientSeed;
    }

    public void setClientSeed(int clientSeed)
    {
        this.clientSeed = clientSeed;
    }

    public Socket getSocket()
    {
        return socket;
    }

    public DataInputStream getDataInputStream()
    {
        return dataInput;
    }

    public DataOutputStream getDataOutputStream()
    {
        return dataOut;
    }

    private DataInputStream tryGetInputStream()
    {
        try
        {
            return new DataInputStream(socket.getInputStream());
        }
        catch (Exception e)
        {
            sendMessageAndClose("A FATAL ERROR HAS OCCURRED 0x747279476574496E70757453747265616D");
        }
        return null;
    }

    private DataOutputStream tryGetOutputStream()
    {
        try
        {
            return new DataOutputStream(socket.getOutputStream());
        }
        catch (Exception e)
        {
            sendMessageAndClose("A FATAL ERROR HAS OCCURRED 0x747279476574496E70757453747265616D");
        }
        return null;
    }

    public void trySendNumber(int numberToSend)
    {
        try
        {
            dataOut.writeInt(numberToSend);
            dataOut.flush();
        }
        catch (Exception e)
        {
            sendMessageAndClose(
                    "A FATAL ERROR HAS OCCURRED 0x74727953656E644E756D626572\n" +
                            " make sure the client is still on and connected to the same network");
        }
    }

    private static void sendMessageAndClose(String message)
    {
        System.out.println(message);
        System.exit(-1);
    }
}