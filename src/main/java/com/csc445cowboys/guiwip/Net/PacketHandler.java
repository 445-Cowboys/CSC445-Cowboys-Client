package com.csc445cowboys.guiwip.Net;
import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.packets.*;

import java.io.IOException;
import java.net.InetSocketAddress;
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


    public PacketHandler(SocketAddress sa, ByteBuffer packet, MainLobbyController mlc) throws IOException {
        try {
            this.packet = packet;  // May not need to actually flip?  TODO Look into this during testing
            this.sa = sa;
            channel = DatagramChannel.open().bind(null);
            this.mlc = mlc;

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
            if(MainNet.programState.get() == 2 | MainNet.programState.get() == 1){
                MainNet.voidGameSession();
            }
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
                //send back an ack, for ease of use, acks for GameRooms packets will just be the opcode 5 to
                //coincide with the opcode of the GameRooms packets themselves
                ByteBuffer ackBuf = ByteBuffer.allocate(1);
                ackBuf.put((byte) 0x0B);
                ackBuf.flip();
                channel.send(ackBuf, sa);
                //update game rooms data
                this.packet.flip();
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
                    System.out.printf("Entered room: %d\n", MainNet.roomID.get());
                }else{
                    MainNet.voidGameSession();
                    System.out.printf("Failed to enter room: %d\n", MainNet.roomID.get());
                }
            }
            case 4 -> {  // GAME START PACKET received from server
                GameStart gameStart = new GameStart(this.packet);
                MainNet.SessionKey = gameStart.getSymmetricKey().getKeySetAsJSON();
                MainNet.aead.parseKey(MainNet.SessionKey);
                MainNet.programState.set(2);
                MainNet.playerNumber.set(gameStart.getCharacter());
                mlc.OpenBattleScreen();
            }
            default -> System.out.printf("Unknown packet type given current context: %d\n", this.packet.get(0));
        }
    }

    public void InGameContext() throws GeneralSecurityException {
        // Decrypt packet
        this.packet = ByteBuffer.wrap(MainNet.aead.decrypt(this.packet.array()));
        // TODO May need to flip after encryption/decryption??
        // GAME STATE PACKET
        if (this.packet.get(0) == 9) {
            bsc.updateFromGameStatePacket(new GameState(this.packet), this.sa);
        } else {
            System.out.printf("Unknown packet type given current context: %d\n", this.packet.get(0));
        }
    }

    public void sendActionPacket(int action, int playerNum) throws IOException {
        Factory factory = new Factory();

        this.packet = factory.makePlayerActionPacket(MainNet.roomID.get(), action, playerNum);

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
        for (String server : ServerConfig.SERVER_NAMES) {
            SocketAddress sa = new InetSocketAddress(server, 7086);
            while (retryNum < 10) {
                channel.send(packet, sa);
                Future<Void> task = executorService.submit(Callable);

                try {
                    task.get(500, TimeUnit.MILLISECONDS);
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    retryNum++;
                    continue;
                }
                if ((int) ackBuf.get(0) == -1) {
                    //Shouldn't get here so if you see this message there is a problem.
                    System.out.println("Action invalid");
                }
            }
        }
    }

    public void sendGameRequestPacket(int room) throws IOException {
        this.packet = new Factory().makeEnterRoomPacket(room,MainNet.channel.socket().getLocalPort());
        channel.send(packet, sa);
//        channel.receive(packet);
//        System.out.println("Received packet");
    }

    public void sendCourtesyLeave() throws IOException {
        this.packet = new Factory().makeCourtesyLeave();
        channel.send(packet, sa);
    }

}
