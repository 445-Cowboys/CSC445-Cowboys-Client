package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.packets.GameRooms;
import com.csc445cowboys.guiwip.packets.GameStart;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
// import server status enum

public class MainNet implements Runnable {

    ServerConfig serverConfig = new ServerConfig();
    Boolean serverStatus = false;
    MainLobbyController mainLobbyController;
    ByteBuffer receivedData;
    DatagramChannel channel;
    SocketAddress sa;
    int timeout = 5000;

    public MainNet(MainLobbyController mainLobbyController) throws IOException {
        this.mainLobbyController = mainLobbyController;
        receivedData = ByteBuffer.allocate(1024);
        channel = DatagramChannel.open().bind(null);
        sa = new InetSocketAddress(ServerConfig.SERVER_NAMES[0], ServerConfig.SERVER_PORTS[0]);
    }

    public void sendAwakeLoop() {
        int failCount = 0;
        for (int i = 0; i < ServerConfig.SERVER_NAMES.length; i++) {
            try {
                ByteBuffer buf = ByteBuffer.allocate(4);
                buf.putInt(20);  // 20 is arbitrary for
                buf.flip();
                channel.send(buf, new InetSocketAddress(ServerConfig.SERVER_NAMES[i], ServerConfig.SERVER_PORTS[i]));
                initServResp(); // throws exception if no response
            } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
                System.out.println("Server " + ServerConfig.SERVER_NAMES[i] + " is not awake");
                mainLobbyController.appendToWriter("Server " + ServerConfig.SERVER_NAMES[i] + " is not awake");
                failCount++;
            } finally {
                if (failCount == 9 && i==2) {
                    System.out.println("No servers are awake");
                    mainLobbyController.appendToWriter("No servers are awake");
                    System.exit(1);
                }
            }
        }
    }

    public void initServResp() throws ExecutionException, InterruptedException, TimeoutException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        GameRooms gameRooms;
        FutureTask<GameRooms> futureTask = new FutureTask<>(() -> {
            // Receive Game Start Packet
            channel.receive(buf);
            // Check if the packet is game room packet
            if (buf.getInt() != 6) {
                throw new RuntimeException();
            }
            return new GameRooms(buf);
        });

        // Start the long-running operation
        new Thread(futureTask).start();

        // Get the result of the long-running operation
        gameRooms = futureTask.get(5, TimeUnit.SECONDS);

        if (gameRooms == null) {
            throw new TimeoutException();
        }
    }

    // Listens for updates on game rooms
    @Override
    public void run() {
        // for
        try {
            for (; ; ) {
                channel.receive(receivedData);
                receivedData.flip();
                int opcode = receivedData.getInt();
                // Opcode 6 is for game rooms
                if (opcode == 6) {
                    GameRooms gameRooms = new GameRooms(receivedData);
                    mainLobbyController.setGameRooms(gameRooms);
                }
                Thread.sleep(1000);
            }

        } catch (RuntimeException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
