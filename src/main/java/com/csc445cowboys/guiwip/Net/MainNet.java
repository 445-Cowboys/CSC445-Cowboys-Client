package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.packets.GameRooms;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MainNet implements Runnable {
    // Atomics
    final static AtomicInteger MAX_RETRIES = new AtomicInteger(10);
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
     *
     * @param server Server IP Address
     *               * @param port Server Port Number
     * @throws IOException If the packet fails to send
     */
    public void sendAwake(String server, int port) throws IOException, UnresolvedAddressException {
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
        this.receivedData = futureTask.get(timeout.get(), TimeUnit.MILLISECONDS);

        // Check for timeout
        if (this.receivedData == null) {
            throw new TimeoutException("No Response from Server after Join Request");
        }// Ctr a d lol
    }

    @Override
    public void run() {
        for(;;){
            // Will always fail on first attempt and attempt to round-robin find a server
            while (this.connected.get()){
                // Set Heartbeat to 60 seconds
                this.timeout.set(1000 * 60);
                try {
                    packetReceive();  // Attempt to receive a packet from the server, will time out after 60 seconds
                    Thread thead = new Thread(new PacketHandler(this.sa, this.receivedData));
                    thead.start();
                }catch (TimeoutException e){  // If no packet is received, set connected to false and attempt to connect to a server,
                    // will fall to round robin search to try to connect to a server
                    this.connected.set(false);
                } catch (ExecutionException | InterruptedException | RuntimeException | IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
            // Attempt to connect to a server if connection is lost
            if(!roundRobinServerFind()) System.exit(-5);
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
     */
    public Boolean roundRobinServerFind() {
        // Server Round Robin to attempt to connect to a server
        for (int i = 0; i < ServerConfig.SERVER_NAMES.length; i++) {
            // While retries less than max retries and not connected to a server
            while ((retries.get()-1 < MAX_RETRIES.get())) {
                try {
                    // Send awake packet to server
                    sendAwake(ServerConfig.SERVER_NAMES[i], ServerConfig.SERVER_PORTS[i]);
                    // Attempt to receive a packet from the server
                    packetReceive();
                    // Break out of loop if server is awake upon receipt of @GameRooms packet
                    if (receivedData.get(0) == 5) {
                        mainLobbyController.appendToWriter("Server is awake");
                        mainLobbyController.setGameRooms(new GameRooms(receivedData));
                        this.connected.set(true);
                        this.sa = new InetSocketAddress(ServerConfig.SERVER_NAMES[i], ServerConfig.SERVER_PORTS[i]);
                        this.retries.set(1);
                        return true;
                    }
                    // If no packet is received or other failure occurs, increment retries and backoff time
                } catch (ExecutionException | InterruptedException | TimeoutException | IOException |
                         UnresolvedAddressException e) {
                    // Current retry delay
                    retries.getAndIncrement();
                    timeout.getAndAdd(timeout.get());
                    System.out.println("Error: " + e.getMessage());
                    // Exponential backoff
                    mainLobbyController.appendToWriter("Server: " + ServerConfig.SERVER_NAMES[i] + ". Retrying in " + timeout.get() + "ms. Retry " + retries.get());
                }
            }
            // Reset backoff loop
            retries.set(1);
            timeout.set(1000);
        }
        // If no server is found, set connected to false and return false
        System.out.println("No server found");
        return false;
    }
}
