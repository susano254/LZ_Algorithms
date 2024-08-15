package com.susano.LZ_Algorithms;

import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URI;

// convert the above code to Java
public class SocketHandler {
    private Socket mSocket;


    SocketHandler(URI url, IO.Options options) {
        mSocket = IO.socket(url, options);

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on("message", onMessage);

    }

    // create a method to connect to the server
    public void connect() {
        Log.d("Socket.io", "Connecting to server...");
        mSocket.connect();
    }

    public Socket getSocket() {
        return mSocket;
    }

    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("Socket.io", "Connected to server");
            // print the socket id
            Log.d("Socket.io", "Socket ID: " + mSocket.id());
            // Example: Join a room
            mSocket.emit("sub", "{ room: 'some_room' }");
        }
    };

    private final Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("Socket.io", "Disconnected from server");
        }
    };

    private final Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("Socket.io", "Connection error: " + args[0]);
        }
    };

    private final Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("Socket.io", "Received message: " + args[0]);
        }
    };

    // create a destructor to disconnect the socket
    public void close() {
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off("message", onMessage);
    }
}



