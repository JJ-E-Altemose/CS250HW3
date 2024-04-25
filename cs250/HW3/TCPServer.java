package cs250.hw3;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    static Socket server;

    /**CONFIG INFO**/
    static boolean submission3 = false;
    static boolean submission2 = true || submission3;
    static boolean submission1 = true || submission2;
    /**           **/
    private static void run(String[] args)
    {
        if(submission1)
        {
            int[] numberArgs;
            int portNumber, seed;

            numberArgs = tryParseArgsToNumbers(args);

            portNumber = numberArgs[0];
            seed = numberArgs[1];
            maxNumberOfMessages = numberArgs[2];

            initializationMessage(portNumber);

            validatePortAndBind(portNumber);
            randomGenerator = new Random(seed);

            awaitClientConnections();
            sendConfigToClients();
            clientStatsMessage();
        }
        if(submission2)
        {
            receiveMessages();
            printClientMessageStatistics();
        }
        if(submission3)
        {
            //informClientsNumberOfMessagesForwardedToIt();
            forwardMessages();
        }
    }

    public static void main(String[] args)//Client id And close clean
    {
        try
        {
            run(args);
        }
        catch(RuntimeException ignored){}
        catch(Exception e)
        {
            e.getMessage();
        }
    }

    private static void forwardMessages()
    {
        for (int from = 0; from < clients.length; from++)
        {
            for (int to = 0; to < clients.length; to++)
            {
                if(from != to)
                {
                    for(Integer message : clients[from].messages)
                    {
                        clients[to].trySendNumber(message);
                    }
                }
            }
        }
    }
    /*
    private static void informClientsNumberOfMessagesForwardedToIt()
    {
        int number = (clients.length-1)*maxNumberOfMessages;
        for (int clientIndex = 0; clientIndex < clients.length; clientIndex++)
        {
            informClientNumberOfMessagesForwardedToIt(clientIndex, number);
        }
    }

    private static void informClientNumberOfMessagesForwardedToIt(int clientIndex, int number)
    {
        clients[clientIndex].trySendNumber(number);
    }
     */

    private static void printClientMessageStatistics()
    {
        for(Client client : clients)
        {
            String Hostname = client.getSocket().getInetAddress().getHostName();
            System.out.println(Hostname);
            System.out.println("\tMessages received: " + client.messages.size());
            System.out.println("\tSum received: " + getClientSum(client));
        }
    }

    private static long getClientSum(Client client)
    {
        long sum = 0;
        for(int message : client.messages)
        {
            sum += message;
        }
        return sum;
    }

    private static void receiveMessages()
    {
        System.out.println("Starting to listen for client messages...");
        for (int clientID = 0; clientID < clients.length; clientID++)
        {
            for (int numberMessagesReceived = 0; numberMessagesReceived < maxNumberOfMessages; numberMessagesReceived++)
            {
                receiveMessage(clientID);
            }
        }
        System.out.println("Finished listening for client messages.");
    }

    private static void receiveMessage(int clientIndex)
    {
        Client client = clients[clientIndex];
        //client.trySendNumber(clientIndex);
        client.messages.add(tryRead(clientIndex));
    }

    private static Integer tryRead(int clientIndex)
    {
        Integer Output = null;
        try
        {
            Output = clients[clientIndex].dataInput.readInt();
        }
        catch (Exception e)
        {
            sendMessageAndClose(e.getMessage());
        }
        return Output;
    }

    private static void sendConfigToClients()
    {
        sendClientsMaxNumberOfMessages();
        sendClientSeeds();
        //sendClientsIDS();
    }

    private static void clientStatsMessage()
    {
        for (Client client : clients)
        {
            String clientHostName = client.getSocket().getInetAddress().getHostName();
            int clientSeed = client.getClientSeed();
            System.out.println(clientHostName + " " + clientSeed);
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

    private static void sendClientsIDS()
    {
        for(int index = 0; index < clients.length; index++)
        {
            sendClientID(index);
        }
    }

    private static void sendClientID(int clientIndex)
    {
        Client client = clients[clientIndex];
        client.trySendNumber(clientIndex);
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
        while (numberOfClientsConnected != clients.length)
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
            Socket socket = serverSocket.accept();
            if(server == null)
            {
                server = socket;
            }
            return socket;
        }
        catch (Exception e)
        {
            sendMessageAndClose(e.getMessage());
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
            sendMessageAndClose(e.getMessage());
        }
        return null;
    }

    //<editor-fold desc="PortValidation">
    private static void validatePortAndBind(int port)
    {
        tryBindPort(port);
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
            sendMessageAndClose(e.getMessage());
            //sendMessageAndClose("Address already in use (Bind failed)");
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
            throw new RuntimeException(e.getMessage());
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
        throw new RuntimeException();
    }

}
class Client
{
    Socket socket;
    DataInputStream dataInput;
    DataOutputStream dataOut;
    int clientSeed;
    ArrayList<Integer> messages;

    public Client(Socket socket)
    {
        this.socket = socket;
        this.dataInput = tryGetInputStream();
        this.dataOut = tryGetOutputStream();
        messages = new ArrayList<>();
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
            return new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        }
        catch (Exception e)
        {
            sendMessageAndClose(e.getMessage());
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
            sendMessageAndClose(e.getMessage());
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
            sendMessageAndClose(e.getMessage());
        }
    }

    private static void sendMessageAndClose(String message)
    {
        System.out.println(message);
        throw new RuntimeException();
    }
}