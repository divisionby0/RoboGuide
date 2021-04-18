package dev.div0.roboguide;

public interface ISocketStateProvider {
    void onSocketConnected();
    void onSocketDisconnected();
    void onSocketConnectError(String error);
}
