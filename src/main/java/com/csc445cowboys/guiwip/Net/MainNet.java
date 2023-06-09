package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.Alerts;
import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.packets.GameRooms;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.UnresolvedAddressException;
import java.security.GeneralSecurityException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MainNet implements Runnable {
    // Atomics
    final static AtomicInteger MAX_RETRIES = new AtomicInteger(3);
    static public DatagramChannel channel;
    static public SocketAddress sa;
    static public AEAD aead;
    final static int PORT = 7086;
    ByteBuffer receivedData;

    // Triggered when user attempts to enter a room
    /*
    0 - Main Lobby Context
    1 - Game Requested Context
    2 - In Game Context
     */
    static public byte[] SessionKey;
    static public AtomicInteger roomID = new AtomicInteger(-1);
    static public AtomicInteger programState = new AtomicInteger(0);
    AtomicInteger timeout = new AtomicInteger(500);
    AtomicInteger retries = new AtomicInteger(1);
    AtomicBoolean connected = new AtomicBoolean(false);
    public static AtomicInteger playerNumber = new AtomicInteger(-1);
    MainLobbyController mainLobbyController;
    BattleScreenController battleScreenController;

    // Main Menu Net Constructor, set the mainLobbyController reference, binds a reception channel to a random port
    // and sets the channel to non-blocking
    public MainNet(MainLobbyController mainLobbyController, BattleScreenController battleScreenController) throws IOException, GeneralSecurityException {
        receivedData = ByteBuffer.allocate(1024);
        channel = DatagramChannel.open().bind(null);
        channel.configureBlocking(true);
        aead = new AEAD();
        this.mainLobbyController = mainLobbyController;
        this.battleScreenController = battleScreenController;
    }

    public static void voidGameSession() {
        programState.set(0);
        roomID.set(-1);
        playerNumber.set(-1);
        SessionKey = null;
    }


    public DatagramChannel sendAwake(String server, int port) throws IOException, UnresolvedAddressException {
        ByteBuffer buf = ByteBuffer.allocate(5);
        buf.put((byte) 20);
        buf.putInt(port);
        buf.flip();
        SocketAddress sa = new InetSocketAddress(server, PORT);
        channel.send(buf, sa);
        System.out.println("Sent awake packet to server");
        return channel;
    }

    /**
     * General receive method for receiving packets from the server
     */
    public SocketAddress packetReceive() throws ExecutionException, InterruptedException, TimeoutException {
        final SocketAddress[] serverAdd = new SocketAddress[1];
        FutureTask<ByteBuffer> futureTask = new FutureTask<>(() -> {
            // Receive Game Start Packet
            serverAdd[0] = channel.receive(receivedData);
            // Flip
            receivedData.flip();
            return receivedData;
        });
        // Start the long-running operation
        new Thread(futureTask).start();

        // Exp backoff
        this.receivedData = futureTask.get(timeout.get(), TimeUnit.MILLISECONDS);

        // Check for timeout
        if (this.receivedData == null) {
            throw new TimeoutException("No Response from Server after Join Request");
        }// Ctr a d lol
        return serverAdd[0];
    }

    public SocketAddress packetReceiveRoundRobin(DatagramChannel channel) throws ExecutionException, InterruptedException, TimeoutException {
        final SocketAddress[] serverAdd = new SocketAddress[1];
        FutureTask<ByteBuffer> futureTask = new FutureTask<>(() -> {
            // Receive Game Start Packet
            serverAdd[0] = channel.receive(receivedData);
            // Flip
            receivedData.flip();
            return receivedData;
        });
        // Start the long-running operation
        new Thread(futureTask).start();

        // Exp backoff
        this.receivedData = futureTask.get(timeout.get(), TimeUnit.MILLISECONDS);

        // Check for timeout
        if (this.receivedData == null) {
            throw new TimeoutException("No Response from Server after Join Request");
        }// Ctr a d lol
        return serverAdd[0];
    }

    @Override
    public void run() {
        for (;;) {
            // Will always fail on first attempt and attempt to round-robin find a server
            while (this.connected.get()) {
                this.receivedData = ByteBuffer.allocate(1024);
                // Set Heartbeat to 60 seconds
                this.timeout.set(1000 * 60);
                try {
                    SocketAddress serverAdd = packetReceive();  // Attempt to receive a packet from the server, will time out after 60 seconds
                    Thread thead = new Thread(new PacketHandler(serverAdd, this.receivedData, mainLobbyController, battleScreenController));  // start a new thread to handle the packet
                    thead.start();
                } catch (TimeoutException e) {  // If no packet is received, set connected to false and attempt to connect to a server,
                    // will fall to round robin search to try to connect to a server
                    this.connected.set(false);
                    voidGameSession();
                } catch (ExecutionException | InterruptedException | RuntimeException | IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (!roundRobinServerFind()) {
                    Alerts.displayAlert("Servers down", "All servers are currently down. Please try connecting later.", Alert.AlertType.INFORMATION,false);
                    //noinspection BusyWait
                    Thread.sleep(5000);
                    System.exit(0);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * Round Robin Server Find goes in a circle of defined possible servers and attempts to connect to each one
     * If a server is found, the server is set to connected and the game rooms are update and connected is set to true
     * and also returns true
     * At the end of the loop, if no server is found, connected is set to false and returns false, which should be handled
     * by the caller
     *  For Each Server in ServerConfig.SERVER_NAMES
     * -> Send Awake Packet
     * -> Attempt to Receive Packet
     * -> If GAME_ROOM Packet is received, break out of loop
     * --> If no packet is received, increment retries
     * --> Increment backoff time
     * --> If retries > MAX_RETRIES, attempt to connect to next server
     * --> If no servers are available, exit the program
     * ---> If GAME_ROOM Packet is received, break out of loop, set the server to connected, and update the game rooms
     * TODO Make it so that it tries each server before attempting a longer delay*/
    public Boolean roundRobinServerFind() throws IOException {
        // Server Round Robin to attempt to connect to a server
        // While retries less than max retries and not connected to a server
        while ((retries.get() <= MAX_RETRIES.get())) {
            for (int i = 0; i < ServerConfig.SERVER_NAMES.length; i++) {
                System.out.println(i);
                try {
                    // Send awake packet to server
                    sendAwake(ServerConfig.SERVER_NAMES[i], Integer.parseInt(channel.getLocalAddress().toString().split("]:")[1]));
                    // Attempt to receive a packet from the server
                    receivedData = ByteBuffer.allocate(1024);
                    packetReceiveRoundRobin(channel);
                    // Break out of loop if server is awake upon receipt of @GameRooms packet
                    if (receivedData.get(0) == 5) {
                        mainLobbyController.appendToMainLobbyWriter("Connected to Server: " + ServerConfig.SERVER_NAMES[i] + "\n");
                        mainLobbyController.setGameRooms(new GameRooms(receivedData));
                        sa = new InetSocketAddress(ServerConfig.SERVER_NAMES[i], MainNet.PORT);
                        this.connected.set(true);
                        this.retries.set(1);
                        this.timeout.set(500);
                        return true;
                    }
                    // If no packet is received or other failure occurs, increment retries and backoff time
                } catch (ExecutionException | InterruptedException | TimeoutException | IOException |
                         UnresolvedAddressException e) {
                    channel.close();
                    channel = DatagramChannel.open().bind(null);
                    // Exponential backoff
                    mainLobbyController.appendToMainLobbyWriter("No response from server: " + ServerConfig.SERVER_NAMES[i] + ".\nRetrying in " + timeout.get() + "ms. Retry " + retries.get());
                }
            }
            // Current retry delay
            retries.getAndIncrement();
            timeout.getAndAdd(timeout.get());
        }

        // If no server is found, set connected to false and return false
        return false;
    }

    public void setPlayerNumber(int num){
        playerNumber.set(num);
        //set it for the battle screen controller as well
        battleScreenController.setClientPlayerNumber(num);
    }

}
