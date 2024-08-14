package com.susano.LZ_Algorithms;

import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URISyntaxException;

// convert the above code to Java
public class SocketHandler {
    private static Socket mSocket;

    public static synchronized void setSocket() {
        try {
//            mSocket = IO.socket("http://192.168.1.3:3001");
            mSocket = IO.socket("oddssocketdev.bestlive.io");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Socket getSocket() {
        return mSocket;
    }

    public static synchronized void establishConnection() {
        mSocket.connect();
        Log.d("SocketHandler", "Connection established");
    }

    public static synchronized void closeConnection() {
        mSocket.disconnect();
    }
}



