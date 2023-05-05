package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.packets.EnterRoomAck;
import com.csc445cowboys.guiwip.packets.GameRooms;
import com.csc445cowboys.guiwip.packets.GameStart;
import com.csc445cowboys.guiwip.packets.GameState;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class PacketHandler implements Runnable {
    ByteBuffer packet;
    DatagramChannel channel;
    SocketAddress sa;
    MainNet mainNet;

    public PacketHandler(SocketAddress sa, ByteBuffer packet,MainNet mainNet) throws IOException {
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

    // Heatbeat sends a packet to the server every minute to let it know the client is still connected
    // doesn't really need to do anything else
    @Override
    public void run() {
        switch (this.packet.get()) {
            case 4 -> // GAME START PACKET
                    BattleScreenController.setGameStart(new GameStart(this.packet), this.sa);
            case 5 ->  // GAME ROOMS PACKET
                    MainLobbyController.setGameRooms(new GameRooms(this.packet));
            case 6 -> // ENTER ROOM ACK PACKET
                    mainNet.setRoomLock(new EnterRoomAck(this.packet));
            case 9 -> // GAME STATE PACKET
                    BattleScreenController.updateFromGameStatePacket(new GameState(this.packet), this.sa);
        }
    }
}
