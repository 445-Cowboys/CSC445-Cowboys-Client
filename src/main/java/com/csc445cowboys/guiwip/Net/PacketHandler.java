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
import java.util.concurrent.atomic.AtomicInteger;

public class PacketHandler implements Runnable {
    ByteBuffer packet;
    DatagramChannel channel;
    SocketAddress sa;
    MainNet mainNet;
    ActionEvent event;
    AtomicInteger room;

    public PacketHandler(SocketAddress sa, ByteBuffer packet, MainNet mainNet) throws IOException {
        try {
            this.mainNet = mainNet;
            this.packet = packet;
            packet.flip();
            this.sa = sa;
            channel = DatagramChannel.open().bind(null);
        } catch (IOException e) {
            System.out.println("Issue creating heartbeat");
        }
    }

    public void AddEvent(ActionEvent event){
        this.event = event;
    }

    public void AddRoom(AtomicInteger room){
        this.room = room;
    }

    // Heartbeat sends a packet to the server every minute to let it know the client is still connected
    // doesn't really need to do anything else
    @Override
    public void run() {
        switch (this.packet.get()) {
            case 4 -> // GAME START PACKET
                    BattleScreenController.setGameStart(new GameStart(this.packet), this.sa);
            case 5 -> {  // GAME ROOMS PACKET
                try {
                    MainLobbyController.OpenBattleScreen(event, room);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case 6 -> // ENTER ROOM ACK PACKET
                    mainNet.setRoomLock(new EnterRoomAck(this.packet));
            case 9 -> // GAME STATE PACKET
                    BattleScreenController.updateFromGameStatePacket(new GameState(this.packet), this.sa);
        }
    }
}
