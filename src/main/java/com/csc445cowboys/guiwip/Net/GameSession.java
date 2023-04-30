package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.Alerts;
import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.packets.EnterRoom;
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


// Send a request to join a game and wait for a response from the server
public void requestJoin(int n) {
    try {
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
        GameStart gameStart = futureTask.get(5, TimeUnit.SECONDS);

        // Check for timeout
        if (gameStart == null) {
            throw new TimeoutException("No Response from Server after Join Request");
        }

        // Get Updated Crypto Key
        aead.parseKey(gameStart.cryptoKey);
    } catch (IOException e) {
        // Handle the error
        Alerts.displayAlert(IOException.class.getName(), e.getMessage(), Alert.AlertType.ERROR);
    } catch (GeneralSecurityException e) {
        // Handle the error
        Alerts.displayAlert(GeneralSecurityException.class.getName(), e.getMessage(), Alert.AlertType.ERROR);
    } catch (TimeoutException e) {
        // Handle the timeout
        Alerts.displayAlert("Request timed out", "The server did not respond within 5 seconds", Alert.AlertType.ERROR);
    } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
    }
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
