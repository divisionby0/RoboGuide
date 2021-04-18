package dev.div0.roboguide.socket;

import dev.div0.roboguide.ISocketStateProvider;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import io.socket.engineio.client.transports.Polling;
import io.socket.engineio.client.transports.WebSocket;

import android.util.Log;
import android.widget.Toast;

import java.net.URISyntaxException;

public class AppSocket {
    private String serverUrl = "https://roboguideserver.divisionby0.ru";
    private Socket socket;
    private ISocketStateProvider socketStateProvider;

    private String tag = "AppSocket";

    public AppSocket(String userId, ISocketStateProvider _socketConnectedProvider){
        Log.d(tag, "AppSocket()");

        socketStateProvider = _socketConnectedProvider;

        try {
            Log.d(tag, "connecting to "+serverUrl);

            IO.Options options = IO.Options.builder()
                    // IO factory options
                    .setForceNew(false)
                    .setMultiplex(true)

                    // low-level engine options
                    .setTransports(new String[] { WebSocket.NAME, Polling.NAME})
                    .setUpgrade(true)
                    .setRememberUpgrade(false)
                    .setPath("/socket.io/")
                    .setQuery("userId="+userId)
                    .setExtraHeaders(null)

                    // Manager options
                    .setReconnection(true)
                    .setReconnectionAttempts(Integer.MAX_VALUE)
                    .setReconnectionDelay(1_000)
                    .setReconnectionDelayMax(5_000)
                    .setRandomizationFactor(0.5)
                    .setTimeout(20_000)

                    // Socket options
                    .setAuth(null)
                    .build();

            socket = IO.socket(serverUrl, options);

            socket.on(Socket.EVENT_CONNECT,onConnect);
            socket.on(Socket.EVENT_DISCONNECT,onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on("hello", onHello);
            socket.connect();
        }
        catch (URISyntaxException e) {
            socketStateProvider.onSocketConnectError(e.getMessage());
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socketStateProvider.onSocketConnected();
        }
    };
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socketStateProvider.onSocketDisconnected();
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Object exception = args[0];

            if((EngineIOException)exception!=null){
                String error = ((EngineIOException) exception).getMessage();
                socketStateProvider.onSocketConnectError(error);
            }
            else{
                socketStateProvider.onSocketConnectError("");
            }
        }
    };

    private Emitter.Listener onHello = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(tag, "server said hello");
        }
    };
}
