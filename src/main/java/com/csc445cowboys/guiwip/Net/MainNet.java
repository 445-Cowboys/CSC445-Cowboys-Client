package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.packets.GameRooms;
import com.csc445cowboys.guiwip.packets.GameStart;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MainNet implements Runnable {
    // Atomics
    final static AtomicInteger MAX_RETRIES = new AtomicInteger(3);
    MainLobbyController mainLobbyController;  // Reference to Main Menu Controller
    ByteBuffer receivedData;
    DatagramChannel channel;
    SocketAddress sa;
    AtomicInteger timeout = new AtomicInteger(1000);
    AtomicInteger retries = new AtomicInteger(1);
    AtomicBoolean inGame = new AtomicBoolean(false);
    AtomicBoolean connected = new AtomicBoolean(false);

    // Main Menu Net Constructor, set the mainLobbyController reference, binds a reception channel to a random port
    // and sets the channel to non-blocking
    public MainNet(MainLobbyController mainLobbyController) throws IOException {
        this.mainLobbyController = mainLobbyController;
        receivedData = ByteBuffer.allocate(1024);
        channel = DatagramChannel.open().bind(null);  // TOO Still need to ask Dom how he wants to handle client ports
        channel.configureBlocking(false);
        System.out.println("Client bound to port: " + channel.getLocalAddress());
    }

    /**
     * Sends a packet to the server upon client startup to wake the server up
     * @param server Server IP Address
*    * @param port Server Port Number
     * @throws IOException If the packet fails to send
     */
    public void sendAwake(String server, int port) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.put((byte) 20);
        buf.flip();
        SocketAddress sa = new InetSocketAddress(server, port);
        channel.send(buf, sa);
        System.out.println("Sent awake packet to server");
    }

    /**
     * General receive method for receiving packets from the server
     */
    public void packetReceive() throws ExecutionException, InterruptedException, TimeoutException {
        FutureTask<ByteBuffer> futureTask = new FutureTask<>(new Callable<ByteBuffer>() {
        @Override
        public ByteBuffer call() throws Exception {
            // Receive Game Start Packet
            channel.receive(receivedData);
            // Flip
            receivedData.flip();
            return receivedData;
        }
        });
        // Start the long-running operation
        new Thread(futureTask).start();

        // Exp backoff
        this.receivedData = futureTask.get((long) timeout.get() * retries.get(), TimeUnit.MILLISECONDS);

        // Check for timeout
        if (this.receivedData == null) {
            throw new TimeoutException("No Response from Server after Join Request");
        }// Ctr a d lol
    }


    /*
    * Main Menu Net Thread.
    *  Server Initialization
    *  For Each Server in ServerConfig.SERVER_NAMES
    * -> Send Awake Packet
    * -> Attempt to Receive Packet
    * -> If GAME_ROOM Packet is received, break out of loop
    * --> If no packet is received, increment retries
    * --> Increment backoff time
    * --> If retries > MAX_RETRIES, attempt to connect to next server
    * --> If no servers are available, exit the program
    * ---> If GAME_ROOM Packet is received, break out of loop, set the server to connected, and update the game rooms
    *
    * --> Then Enter Main Run State
     */
    @Override
    public void run() {
        // Server Round Robin to attempt to connect to a server
        for (int i = 0; i < ServerConfig.SERVER_NAMES.length; i++) {
            while ((retries.get() < MAX_RETRIES.get()) | !this.connected.get()) {
                try {
                    // Send awake packet to server
                    sendAwake(ServerConfig.SERVER_NAMES[i], ServerConfig.SERVER_PORTS[i]);
                    // Attempt to receive a packet from the server
                    packetReceive();
                    // Break out of loop if server is awake
                    if (receivedData.get(0) == 5) {
                        mainLobbyController.appendToWriter("Server is awake");
                        mainLobbyController.setGameRooms(new GameRooms(receivedData));
                        this.connected.set(true);
                        break;
                    }
                } catch (ExecutionException | InterruptedException | TimeoutException | IOException e) {
                    System.out.println("Error: " + e.getMessage());
                    mainLobbyController.appendToWriter("Server: " + ServerConfig.SERVER_NAMES[i]  + ". Retrying in " + timeout.get() + "ms. Retry " + retries.get());
                    retries.getAndIncrement();
                }
            }
            if (this.connected.get()) {
                this.sa = new InetSocketAddress(ServerConfig.SERVER_NAMES[i], ServerConfig.SERVER_PORTS[i]);
                break;
            }
            if (i == ServerConfig.SERVER_NAMES.length - 1) {  // If no servers are available, exit the program
                mainLobbyController.appendToWriter("No servers available. Exiting...");
                System.exit(0);
            }
            retries.set(1);
        }
    }

    public void mainMenuRun(){
        while (this.connected.get(){
            try {
                packetReceive();
                if (receivedData.get(0) == 6) {
                    mainLobbyController.appendToWriter("Game Start Packet Received");
                    mainLobbyController.setGameStart(new GameStart(receivedData));
                    this.inGame.set(true);
                    break;
                }
            } catch (ExecutionException | InterruptedException | TimeoutException | IOException e) {
                System.out.println("Error: " + e.getMessage());
                mainLobbyController.appendToWriter("Server: " + ServerConfig.SERVER_NAMES[i]  + ". Retrying in " + timeout.get() + "ms. Retry " + retries.get());
                retries.getAndIncrement();
            }
        }
    }
}
