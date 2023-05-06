package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.packets.EnterRoomAck;
import com.csc445cowboys.guiwip.packets.GameStart;
import com.csc445cowboys.guiwip.packets.GameState;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketHandler implements Runnable {
    ByteBuffer packet;
    DatagramChannel channel;
    SocketAddress sa;
    AtomicInteger programState;

    public PacketHandler(SocketAddress sa, ByteBuffer packet, AtomicInteger programState) throws IOException {
        try {
            this.packet = packet.flip();  // May not need to actually flip?  TODO Look into this during testing
            this.sa = sa;
            channel = DatagramChannel.open().bind(null);

        } catch (IOException e) {
            System.out.println("Failed to open channel");
        }
    }


    // Heartbeat sends a packet to the server every minute to let it know the client is still connected
    // doesn't really need to do anything else
    @Override
    public void run() {
        try {
            switch (this.programState.get()) {
                case 0 -> MainMenuContext();
                case 1 -> GameRequestedContext();
                case 2 -> InGameContext();
            }
        } catch (GeneralSecurityException | IOException e) {
            System.out.println("Failed to handle packet");
            if(this.programState.get() == 2 | this.programState.get() == 1){
                MainNet.voidGameSession();
            }
        }
    }

    public void GameRequestedContext() throws GeneralSecurityException, IOException {
        switch (this.packet.get(0)) {
            case 6 -> {  // ; GAME ROOMS PACKET received from server
                EnterRoomAck enterRoomAck = new EnterRoomAck(this.packet);
                if (enterRoomAck.getResult()) {
                    MainLobbyController.appendToWriter2("Room is available, waiting for other players and serve to start: " + MainNet.roomID+ "\n");
                }
                if(!enterRoomAck.getResult()){
                    MainLobbyController.appendToWriter2("Failed to enter room: " + MainNet.roomID + "\n");
                    MainNet.voidGameSession();
                }
            }
            case 4 -> {  // GAME START PACKET received from server
                GameStart gameStart = new GameStart(this.packet);
                MainNet.SessionKey = gameStart.cryptoKey;
                MainNet.aead.parseKey(MainNet.SessionKey);
                MainNet.programState.set(2);
                BattleScreenController.setGameStart(gameStart, this.sa);
                MainLobbyController.OpenBattleScreen();
            }
            default -> System.out.printf("Unknown packet type given current context: %d\n", this.packet.get(0));
        }
    }

    public void InGameContext() throws GeneralSecurityException {
        // Decrypt packet
        this.packet = ByteBuffer.wrap(MainNet.aead.decrypt(this.packet.array()));
        switch (this.packet.get(0)) {
            case 9 -> // GAME STATE PACKET
                BattleScreenController.updateFromGameStatePacket(new GameState(this.packet), this.sa);
            default -> System.out.printf("Unknown packet type given current context: %d\n", this.packet.get(0));
        }
    }

    public void MainMenuContext(){}
}
