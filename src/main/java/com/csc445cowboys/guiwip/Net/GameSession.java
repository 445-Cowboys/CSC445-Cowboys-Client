package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.packets.GameStart;
import com.csc445cowboys.guiwip.packets.GameState;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.GeneralSecurityException;
import java.util.concurrent.Callable;

public class GameSession implements Runnable {
    ByteBuffer buf;
    DatagramChannel client;
    InetSocketAddress serverAddress;
    AEAD aead;
    BattleScreenController battleScreenController;
    int lobby;


    public GameSession(BattleScreenController bsc, int lobby) throws IOException, GeneralSecurityException {
        battleScreenController = bsc;
        client = DatagramChannel.open().bind(null);
        serverAddress = new InetSocketAddress(InetAddress.getLocalHost(), 7086);
        this.lobby = lobby;
        aead = new AEAD();
    }


    public void requestJoin() throws IOException, GeneralSecurityException {
        // TODO : Implement ENTER ROOM REQUEST
        client.connect(serverAddress);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("ENTER ROOM REQUEST".getBytes());
        buffer.flip();
        // TODO: ACTUALLY ENCRYPT THIS
        client.write(buffer);
        // TODO : Implement ENTER ROOM RESPONSE (initial game state)
        buffer.clear();
        client.read(buffer);
        buffer.flip();
        GameStart gameStart = new GameStart(buffer);
        aead.parseKey(gameStart.cryptoKey);
        battleScreenController.initializeGameScreen(gameStart);

    }

    public void startGame() {
        //our task we will time
        Callable<Void> Callable = () -> {
            ByteBuffer receivedData = ByteBuffer.allocate(1024);
            //                new Thread(new BatNet(battleScreenController,receivedData, serverAddress)).start();
            return null;
        };
    }

    public void leaveGame() throws IOException {
        // TODO : Implement LEAVE ROOM COURTESY ACK
        client.close();
    }

    @Override
    public void run() {

        // If game State packet
        if (buf.get(1) == 2) {
            GameState gameState = new GameState(buf);
            battleScreenController.updateFromGameStatePacket(gameState);
        }
    }
}
