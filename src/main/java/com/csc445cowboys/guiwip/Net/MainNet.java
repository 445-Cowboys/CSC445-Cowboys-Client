package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.packets.GameRooms;

import java.io.IOException;
import java.net.InetAddress;
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
    final static AtomicInteger MAX_RETRIES = new AtomicInteger(10);
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
    AtomicInteger timeout = new AtomicInteger(1000);
    AtomicInteger retries = new AtomicInteger(1);
    AtomicBoolean connected = new AtomicBoolean(false);
    static AtomicInteger playerNumber = new AtomicInteger(-1);
    MainLobbyController mainLobbyController;
    BattleScreenController battleScreenController;

    // Main Menu Net Constructor, set the mainLobbyController reference, binds a reception channel to a random port
    // and sets the channel to non-blocking
    public MainNet(MainLobbyController mainLobbyController, BattleScreenController battleScreenController) throws IOException, GeneralSecurityException {
        receivedData = ByteBuffer.allocate(1024);
        channel = DatagramChannel.open().bind(null);  // TOO Still need to ask Dom how he wants to handle client ports
        channel.configureBlocking(true);
        System.out.println("Client bound to port: " + channel.getLocalAddress());
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


    public void sendAwake(String server, int port) throws IOException, UnresolvedAddressException {
        ByteBuffer buf = ByteBuffer.allocate(5);
        buf.put((byte) 20);
        buf.putInt(port);
        buf.flip();
        SocketAddress sa = new InetSocketAddress(server, PORT);
        channel.send(buf, sa);
        System.out.println("Sent awake packet to server");
    }

    /**
     * General receive method for receiving packets from the server
     */
    public SocketAddress packetReceive() throws ExecutionException, InterruptedException, TimeoutException {
        final SocketAddress[] serverAdd = new SocketAddress[1];
        FutureTask<ByteBuffer> futureTask = new FutureTask<>(new Callable<ByteBuffer>() {
            @Override
            public ByteBuffer call() throws Exception {
                // Receive Game Start Packet
                serverAdd[0] = channel.receive(receivedData);
                // Flip
                receivedData.flip();
                return receivedData;
            }
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
        for (; ; ) {
            // Will always fail on first attempt and attempt to round-robin find a server
            while (this.connected.get()) {
                // Set Heartbeat to 60 seconds
                this.timeout.set(1000 * 60);
                try {
                    SocketAddress serverAdd = packetReceive();  // Attempt to receive a packet from the server, will time out after 60 seconds
                    Thread thead = new Thread(new PacketHandler(serverAdd, this.receivedData));  // start a new thread to handle the packet
                    thead.start();
                } catch (TimeoutException e) {  // If no packet is received, set connected to false and attempt to connect to a server,
                    // will fall to round robin search to try to connect to a server
                    this.connected.set(false);
                    voidGameSession();
                } catch (ExecutionException | InterruptedException | RuntimeException | IOException e) {
                    e.printStackTrace();
                }
            }
            if (!roundRobinServerFind()) {
                System.exit(-5);
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
    public Boolean roundRobinServerFind() {
        // Server Round Robin to attempt to connect to a server
        // While retries less than max retries and not connected to a server
        int init = 0;
        while ((retries.get() <= MAX_RETRIES.get())) {
            for (int i = init; i < ServerConfig.SERVER_NAMES.length; i++) {
                try {
                    // Send awake packet to server
                    sendAwake(ServerConfig.SERVER_NAMES[i], Integer.parseInt(channel.getLocalAddress().toString().split("]:")[1]));
                    // Attempt to receive a packet from the server
                    packetReceive();
                    // Break out of loop if server is awake upon receipt of @GameRooms packet
                    if (receivedData.get(0) == 5) {
                        mainLobbyController.appendToMainLobbyWriter("Connected to Server: " + ServerConfig.SERVER_NAMES[i] + "\n");
                        mainLobbyController.setGameRooms(new GameRooms(receivedData));
                        this.connected.set(true);
                        sa = new InetSocketAddress(ServerConfig.SERVER_NAMES[i], MainNet.PORT);
                        this.retries.set(1);
                        return true;
                    }
                    // If no packet is received or other failure occurs, increment retries and backoff time
                } catch (ExecutionException | InterruptedException | TimeoutException | IOException |
                         UnresolvedAddressException e) {
                    // Exponential backoff
                    mainLobbyController.appendToMainLobbyWriter("Server: " + ServerConfig.SERVER_NAMES[i] + ". Retrying in " + timeout.get() + "ms. Retry " + retries.get());
                }
            }
            // Current retry delay
            retries.getAndIncrement();
            timeout.getAndAdd(timeout.get());

        }

        // Reset backoff loop
        retries.set(1);
        timeout.set(1000);
        // If no server is found, set connected to false and return false
        System.out.println("No server found");
        return false;
    }

}
