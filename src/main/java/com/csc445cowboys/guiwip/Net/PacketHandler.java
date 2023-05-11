package com.csc445cowboys.guiwip.Net;
import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.Main;
import com.csc445cowboys.guiwip.packets.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.*;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PacketHandler implements Runnable {
    ByteBuffer packet;
    DatagramChannel channel;
    SocketAddress sa;
    public BattleScreenController bsc;
    public MainLobbyController mlc;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public PacketHandler(SocketAddress sa, ByteBuffer packet, MainLobbyController mlc, BattleScreenController bsc) throws IOException {
        try {
            this.packet = packet;
            this.sa = sa;
            channel = DatagramChannel.open().bind(null);
            this.mlc = mlc;
            this.bsc = bsc;

        } catch (IOException e) {
            System.out.println("Failed to open channel");
        }
    }

    public PacketHandler(SocketAddress sa) throws IOException {
        this.packet = ByteBuffer.allocate(1024);
        this.sa = sa;
        this.channel = DatagramChannel.open().bind(null);
    }

    // Heartbeat sends a packet to the server every minute to let it know the client is still connected
    // doesn't really need to do anything else
    @Override
    public void run() {
        try {
            //this clause is in case the server sends an empty buffer
            if(this.packet.limit() == 0){return;}
            if(this.packet.get(0) == -1){
                this.packet = new Factory().makeHeartbeatAckPacket();
                channel.send(packet, sa);
                // Time now
                DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
                System.out.printf("Heartbeat sent to %s @ %s\n", sa.toString(),LocalDateTime.now().format(format));
                return;
            }

            switch (MainNet.programState.get()) {
                case 0 -> InLobbyContext();
                case 1 -> GameRequestedContext();
                case 2 -> InGameContext();
            }
        } catch (GeneralSecurityException | IOException e) {
            System.out.println("Failed to handle packet");
            System.out.println("code has "+(int) this.packet.get(0));
        }
    }

    private void InLobbyContext() throws IOException {
        switch (this.packet.get(0)) {
            case 10 -> {
                System.out.println("Got player count update...");
                ByteBuffer ackBuf = ByteBuffer.allocate(1);
                ackBuf.put((byte) 0x0A);
                ackBuf.flip();
                channel.send(ackBuf, sa);
                //update player count
                mlc.updatePlayerCount(new PlayerCount(this.packet));
            }

            case 11 -> {
                //send back an ack, for ease of use, acks for GameRooms packets will just be the opcode 11 to
                //coincide with the opcode of the GameRooms packets themselves
                ByteBuffer ackBuf = ByteBuffer.allocate(1);
                ackBuf.put((byte) 0x0B);
                ackBuf.flip();
                channel.send(ackBuf, sa);
                //update game rooms data
                mlc.updateGameRooms(new GameRoomsUpdate(this.packet));
            }
            default -> System.out.printf("Unknown packet type given current context: %d\n", this.packet.get(0));
        }
    }

    public void GameRequestedContext() throws GeneralSecurityException, IOException {
        switch (this.packet.get(0)) {
            case 10 -> {
                System.out.println("Received player count update");
                ByteBuffer ackBuf = ByteBuffer.allocate(1);
                ackBuf.put((byte) 0x0A);
                ackBuf.flip();
                channel.send(ackBuf, sa);
                //update player count
                mlc.updatePlayerCount(new PlayerCount(this.packet));
            }

            case 11 -> {
                //send back an ack, for ease of use, acks for GameRooms packets will just be the opcode 5 to
                //coincide with the opcode of the GameRooms packets themselves
                ByteBuffer ackBuf = ByteBuffer.allocate(1);
                ackBuf.put((byte) 0x0B);
                ackBuf.flip();
                channel.send(ackBuf, sa);
                //update game rooms data
                mlc.updateGameRooms(new GameRoomsUpdate(this.packet));
            }

            case 6 -> {  // ; GAME ROOMS PACKET received from server
                EnterRoomAck enterRoomAck = new EnterRoomAck(this.packet);
                if (enterRoomAck.getResult()) {
                    mlc.appendToMainLobbyWriter("Entered room: " + MainNet.roomID.get() + "\n");
                    System.out.printf("Entered room: %d\n", MainNet.roomID.get());
                }else{
                    mlc.appendToMainLobbyWriter("Failed to enter room: " + MainNet.roomID.get() + "\n");
                    MainNet.voidGameSession();
                    System.out.printf("Failed to enter room: %d\n", MainNet.roomID.get());
                }
            }
            case 4 -> {  // GAME START PACKET received from server
                //send an ack back letting the server know we got it
                mlc.appendToMainLobbyWriter("Game starting...\n");
                ByteBuffer ackBuf = ByteBuffer.allocate(1);
                ackBuf.put((byte) 0x04);
                ackBuf.flip();
                channel.send(ackBuf, sa);
                //set the initial game start values
                GameStart gameStart = new GameStart(this.packet);
                MainNet.SessionKey = gameStart.getSymmetricKey();
                MainNet.aead.parseKey(MainNet.SessionKey);
                MainNet.programState.set(2);
                MainNet.roomID.set(gameStart.getGameRoom());
                bsc.setClientPlayerNumber(gameStart.getCharacter());
                bsc.setServerNameAndRoundLabel();
                mlc.OpenBattleScreen();
            }
            default -> System.out.printf("Unknown packet type given current context: %d\n", this.packet.get(0));
        }
    }

    public void InGameContext() throws GeneralSecurityException, IOException {
        // Decrypt packet
        this.packet = ByteBuffer.wrap(MainNet.aead.decrypt(Arrays.copyOfRange(packet.array(), 0, packet.limit())));
        // GAME STATE PACKET
        ByteBuffer ackBuf = ByteBuffer.allocate(1);
        if (this.packet.get(0) == 9) {
            ackBuf.put((byte) 9);
            ackBuf.flip();
            channel.send(ackBuf, sa);
            bsc.updateFromGameStatePacket(new GameState(this.packet), this.sa);
        } else {
            System.out.printf("Unknown packet type given current context: %d\n", this.packet.get(0));
        }
    }

    public void sendActionPacket(int action, int playerNum) throws IOException, GeneralSecurityException {
        Factory factory = new Factory();

        this.packet = factory.makePlayerActionPacket(MainNet.roomID.get(), action, playerNum);
        //encrypt action packet now
        this.packet = ByteBuffer.wrap(MainNet.aead.encrypt(this.packet.array()));
        //now send the action
        ByteBuffer ackBuf = ByteBuffer.allocate(1);
        Callable<Void> Callable = () -> {
            try {
                channel.receive(ackBuf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        };
        int retryNum = 0;
        //try it with mainnet sa first
        channel.send(packet, MainNet.sa);
        while(retryNum < 10) {
            Future<Void> task = executorService.submit(Callable);
            try {
                task.get(100, TimeUnit.MILLISECONDS);
                ackBuf.flip();
                //if we get to this point then we get our ack back
                if ((int) ackBuf.get(0) == -1) {
                    //Shouldn't get here so if you see this message there is a problem.
                    System.out.println("Action invalid");
                    return;
                }
                return;
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                retryNum++;
                channel.close();
                channel = DatagramChannel.open().bind(null);
                packet.rewind();
                channel.send(packet, MainNet.sa);
            }
        }

        retryNum = 0;
        while (retryNum < 10) {
            for (String server : ServerConfig.SERVER_NAMES) {
            SocketAddress sa = new InetSocketAddress(server, 7086);
            channel.send(packet, sa);
                Future<Void> task = executorService.submit(Callable);
                try {
                    task.get(500, TimeUnit.MILLISECONDS);
                    //make the new sa the one we just successfully got an ack from
                    MainNet.sa = sa;
                    //set the bsc server name to tne new server we prioritize
                    ackBuf.flip();
                    //if we get to this point then we get our ack back
                    if ((int) ackBuf.get(0) == -1) {
                        //Shouldn't get here so if you see this message there is a problem.
                        System.out.println("Action invalid");
                        return;
                    }
                    bsc.appendToBattleWriter("Action Received!");
                    return;
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    retryNum++;
                    channel.close();
                    channel = DatagramChannel.open().bind(null);
                    packet.rewind();
                    channel.send(packet, MainNet.sa);
                }
            }
        }
    }

    public void sendGameRequestPacket(int room) throws IOException {
        MainNet.programState.set(1);
        this.packet = new Factory().makeEnterRoomPacket(room,MainNet.channel.socket().getLocalPort());
        ByteBuffer ackBuf = ByteBuffer.allocate(3);
        Callable<Void> Callable = () -> {
            try {
                channel.receive(ackBuf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        };
        int retryNum = 0;
        //try it with mainnet sa first
        while (retryNum < 10) {
            channel.send(packet, MainNet.sa);
            Future<Void> task = executorService.submit(Callable);
            try {
                task.get(100, TimeUnit.MILLISECONDS);
                ackBuf.flip();
                //if we get to this point then we get our ack back
                EnterRoomAck enterRoomAck = new EnterRoomAck(ackBuf);
                if (enterRoomAck.getResult()) {
                    System.out.printf("Entered room: %d\n", MainNet.roomID.get());
                }else{
                    System.out.printf("Failed to enter room: %d\n", MainNet.roomID.get());
                    MainNet.voidGameSession();
                }
                MainNet.sa = sa;
                //set the bsc sa value to the new server
                return;
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                retryNum++;
                channel.close();
                channel = DatagramChannel.open().bind(null);
            }
        }

        retryNum = 0;
        while (retryNum < 10) {
            for (String server : ServerConfig.SERVER_NAMES) {
                SocketAddress sa = new InetSocketAddress(server, 7086);
                channel.send(packet, sa);
                Future<Void> task = executorService.submit(Callable);
                try {
                    task.get(500, TimeUnit.MILLISECONDS);
                    ackBuf.flip();
                    //if we get to this point then we get our ack back
                    EnterRoomAck enterRoomAck = new EnterRoomAck(ackBuf);
                    if (enterRoomAck.getResult()) {
                        System.out.printf("Entered room: %d\n", MainNet.roomID.get());
                    } else {
                        System.out.printf("Failed to enter room: %d\n", MainNet.roomID.get());
                        MainNet.voidGameSession();
                    }
                    return;
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    retryNum++;
                    channel.close();
                    channel = DatagramChannel.open().bind(null);
                }
            }
        }
    }

    public void sendCourtesyLeave() throws IOException {
        this.packet = new Factory().makeCourtesyLeave(Integer.parseInt(MainNet.channel.getLocalAddress().toString().split("]:")[1]));
        channel.send(packet, sa);
    }

}
