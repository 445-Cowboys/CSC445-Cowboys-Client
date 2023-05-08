package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.Main;
import com.csc445cowboys.guiwip.packets.EnterRoomAck;
import com.csc445cowboys.guiwip.packets.Factory;
import com.csc445cowboys.guiwip.packets.GameStart;
import com.csc445cowboys.guiwip.packets.GameState;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketHandler implements Runnable {
    ByteBuffer packet;
    DatagramChannel channel;
    SocketAddress sa;
    public BattleScreenController bsc;
    public MainLobbyController mlc;

    public PacketHandler(SocketAddress sa, ByteBuffer packet) throws IOException {
        try {
            this.packet = packet.flip();  // May not need to actually flip?  TODO Look into this during testing
            this.sa = sa;
            channel = DatagramChannel.open().bind(null);

        } catch (IOException e) {
            System.out.println("Failed to open channel");
        }
    }

    /*
    Emty constructor
     */
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
            if(this.packet.get(0) == -1){
                this.packet = new Factory().makeHeartbeatAckPacket();
                channel.send(packet, sa);
                // Time now
                DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
                System.out.printf("Heartbeat sent to %s @ %s\n", sa.toString(),LocalDateTime.now().format(format));
                return;
            }

            switch (MainNet.programState.get()) {
                case 0 -> MainMenuContext();
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

    private void MainMenuContext() {
    }

    public void GameRequestedContext() throws GeneralSecurityException, IOException {
        switch (this.packet.get(0)) {
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

    public void sendActionPacket(int i) throws IOException {
        this.packet = new Factory().makePlayerActionPacket(MainNet.roomID.get(), i,0);
        channel.send(packet, sa);
    }

    public void sendGameRequestPacket(int room) throws IOException {
        this.packet = new Factory().makeEnterRoomPacket(room,MainNet.channel.socket().getLocalPort(),"" );
        channel.send(packet, sa);
    }

    public void sendCourtesyLeave() throws IOException {
        this.packet = new Factory().makeCourtesyLeave();
        channel.send(packet, sa);
    }

}
