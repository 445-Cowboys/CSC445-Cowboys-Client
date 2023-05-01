package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.Alerts;
import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.packets.Factory;
import com.csc445cowboys.guiwip.packets.GameStart;
import com.csc445cowboys.guiwip.packets.GameState;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.GeneralSecurityException;
import java.util.concurrent.*;

public class GameSession implements Runnable {
    ServerConfig serverConfig = new ServerConfig();
    ByteBuffer buf;
    DatagramChannel client;
    InetSocketAddress serverAddress;
    AEAD aead;
    BattleScreenController battleScreenController;
    GameStart gameStart;
    int lobby;


    public GameSession(BattleScreenController bsc, int lobby) throws IOException, GeneralSecurityException, TimeoutException {
        battleScreenController = bsc;
        client = DatagramChannel.open().bind(null);
        serverAddress = new InetSocketAddress(InetAddress.getLocalHost(), 7086);
        this.lobby = lobby;
        aead = new AEAD();
    }


    // Send a request to join a game and wait for a response from the server
    public Boolean requestJoin(int n) {
        try {
            System.out.println("Sending Join Request ");
            Factory factory = new Factory();
            buf = factory.makeEnterRoomPacket(n, client.getLocalAddress().toString());
            client.send(buf, serverAddress);
            // Create a FutureTask object
            FutureTask<GameStart> futureTask = new FutureTask<>(new Callable<GameStart>() {
                @Override
                public GameStart call() throws Exception {
                    // Receive Game Start Packet
                    client.receive(buf);
                    return new GameStart(buf);
                }
            });

            // Start the long-running operation
            new Thread(futureTask).start();

            // Get the result of the long-running operation
            this.gameStart = futureTask.get(5, TimeUnit.SECONDS);

            // Check for timeout
            if (gameStart == null) {
                throw new TimeoutException("No Response from Server after Join Request");
            }

            // Get Updated Crypt     Key
            aead.parseKey(gameStart.getSymmetricKey().getEncoded());
        } catch (IOException | GeneralSecurityException | TimeoutException | InterruptedException |
                 ExecutionException e) {
            // Handle the error
            e.printStackTrace();
            Alerts.displayAlert("Unable to enter room", "Error during request creation stage", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    public void leaveGame() throws IOException {
        // TODO : Implement LEAVE ROOM COURTESY ACK
        client.close();
    }

    @Override
    public void run() {
        // If game State packet
        if (buf.getInt(0) == 9) {
            try {
                client.receive(buf);
                buf = ByteBuffer.wrap(aead.decrypt(buf.array()));
                GameState gameState = new GameState(buf);
                // Put in a lock here for safety
                battleScreenController.updateFromGameStatePacket(gameState);
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Get Game Start
    public GameStart getGameStart() {
        return gameStart;
    }

    public void fire() {
        buf = new Factory().makePlayerActionPacket(lobby, 1, 0);
    }

    public void reload() {
        buf = new Factory().makePlayerActionPacket(lobby, 2, 0);
    }

    public void useAbility() {
    }
}
