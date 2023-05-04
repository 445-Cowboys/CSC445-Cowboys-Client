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
    AtomicInteger retries = new AtomicInteger(0);
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
     */
    public void sendAwake() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.put((byte) 20);
        buf.flip();
        SocketAddress sa = new InetSocketAddress(ServerConfig.SERVER_NAMES[0], ServerConfig.SERVER_PORTS[0]);
        channel.send(buf, sa);
        System.out.println("Sent awake packet to server");
    }

    /**
     * Attempts to receive a packet from the server to get the initial game room data
     */
    public void initServResp() {
        GameRooms gameRooms = null;
        try {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            FutureTask<GameRooms> futureTask = new FutureTask<>(() -> {
                // Receive Game Start Packet
                channel.receive(buf);
                // Check if the packet is game room packet
                if (buf.get(0) != 5) {
                    throw new RuntimeException();
                }
                buf.flip();
                return new GameRooms(buf);
            });
            // Start the long-running operation
            new Thread(futureTask).start();
            // Get the result of the long-running operation
            gameRooms = futureTask.get(1, TimeUnit.SECONDS);
            if (gameRooms == null) {
                throw new TimeoutException();
            }
        } catch (TimeoutException e) {
            System.out.println("Server is not awake: Timeout Exception");
        } catch (RuntimeException e) {
            System.out.println("Server is not awake: Runtime Exception");
        } catch (InterruptedException e) {
            System.out.println("Server is not awake: Interrupted Exception");
        } catch (ExecutionException e) {
            System.out.println("Server is not awake: Execution Exception");
        }
        if (gameRooms != null) {
            System.out.println("Server is awake");
            mainLobbyController.appendToWriter("Server is awake");
            mainLobbyController.setGameRooms(gameRooms);
        }
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
            return receivedData;
        }
        });
        // Start the long-running operation
        new Thread(futureTask).start();

        // Get the result of the long-running operation
        this.receivedData = futureTask.get((long) this.timeout.get() *this.timeout.get(), TimeUnit.MILLISECONDS);

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
     */
    @Override
    public void run() {
        // Ctr a d lol
        for (int i = 0; i < ServerConfig.SERVER_NAMES.length; i++) {
            while ((retries.get() < MAX_RETRIES.get()) | !this.connected.get()) {
                try {
                    // Send awake packet to server
                    sendAwake();
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
                    mainLobbyController.appendToWriter("Server: " + ServerConfig.SERVER_NAMES[i]  + ". Retrying in " + timeout.get() + "ms. Retry " + retries.get());
                    retries.getAndIncrement();
                }
            }
            if (this.connected.get()) break;
            if (i == ServerConfig.SERVER_NAMES.length - 1) {
                mainLobbyController.appendToWriter("No servers available. Exiting...");
                System.exit(0);
            }
        }
    }
}
