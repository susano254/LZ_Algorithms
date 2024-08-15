package com.susano.LZ_Algorithms;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.parser.Packet;
import io.socket.parser.Parser;

public class CustomParser {
    private boolean isPageLoaded = false;
    private static final String TAG = "CustomParser";
    WebView webView;
    Packet packet;

    public Parser.Encoder encoder = new Parser.Encoder() {
        @Override
        public void encode(Packet obj, Callback callback) {
            String packetJSON = packetToJSON(obj);
            // convert to byte array
            byte[] encoded = packetJSON.getBytes(StandardCharsets.UTF_8);
            Log.d(TAG, "Encoded: " + Arrays.toString(encoded));
            Log.d(TAG, Arrays.toString(new Object[]{encoded}));
            callback.call(new Object[]{encoded});
        }
    };


    public Parser.Decoder decoder = new Parser.Decoder() {
        @Override
        public void add(String obj) {
            Log.d(TAG, "Decoded: " + obj);
        }

        @Override
        public void add(byte[] obj) {
            CountDownLatch latch = new CountDownLatch(1);
            Log.d(TAG, "Decoded: " + Arrays.toString(obj));
            new Handler(Looper.getMainLooper()).post(() -> {
                webView.evaluateJavascript("decompressData(new Uint8Array(" + Arrays.toString(obj) + "))", value -> {
                    Log.d(TAG, "Decoded Packet: " + value);
                    lzCallBack(value);
                    latch.countDown();
                });
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Finished Decoding Packet");

            if(isPacketValid(packet)) {
                Emitter emitter = new Emitter();
                emitter.emit("decoded", packet);
                onDecoded(packet1 -> {
                    Log.d(TAG, "Packet: " + packet1.type + " " + packet1.data + " " + packet1.nsp);
                });
            }
        }

        void lzCallBack(String json) {
            // remove starting and ending double quotes
            json = json.substring(1, json.length() - 1);
            // Unescape JSON string using org.json.JSONObject unescaping
            json = json.replace("\\\"", "\"")
                    .replace("\\\\", "\\");

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                packet = objectMapper.readValue(json, Packet.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void destroy() {
            Log.d(TAG, "Decoder Destroyed");
        }

        @Override
        public void onDecoded(Callback callback) {
            Log.d(TAG, "onDecoded Callback");
//            if(packet != null) {
//                Log.d(TAG, "onDecoded Packet: " + packet.type + " " + packet.data + " " + packet.nsp);
//                callback.call(packet);
//            }
        }
    };

    // Load a simple HTML page that includes lzutf8
    String htmlData = "<html><body>" +
            "<script src='https://cdn.jsdelivr.net/npm/lzutf8/build/production/lzutf8.min.js'></script>" +
            "<script>" +
            "console.log('WebView JavaScript is running!');" +
            "function compressData(data) {" +
            "   try {" +
            "       console.log('Compressing data: ' + data);" +
            "       var compressed = LZUTF8.compress(data, { outputEncoding: 'ByteArray' });" +
            "       console.log('Compressed data variable: ' + compressed);" +
            "       console.log('Compressed data JSON: ' + JSON.stringify(compressed));" +
            "       return compressed;" +
            "   } catch (e) {" +
            "       console.log('Error during compression: ' + e.message);" + // Log errors
            "       return 'Error: ' + e.message;" +
            "   }" +
            "}" +
            "var compressedData = compressData('This is a string that needs to be compressed');" +
            "console.log('Compressed data: ' + compressedData);" +
            "function decompressData(base64Data) {" +
            "   try {" +
            "       console.log('Decompressing data: ' + base64Data);" +
            "       var decompressed = LZUTF8.decompress(base64Data, { inputEncoding: 'ByteArray' });" +
            "       return decompressed;" +
//            "       return JSON.stringify(decompressed);" +
            "   } catch (e) {" +
            "       console.log('Error during decompression: ' + e.message);" + // Log errors
            "       return JSON.stringify('Error: ' + e.message);" +
            "   }" +
            "}" +
            "var result = decompressData(compressedData);" +
            "console.log('Decompressed data: ' + result);" +
            "</script>" +
            "</body></html>";

    CustomParser(WebView webView) {
        this.webView = webView;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isPageLoaded = true;
                Log.d(TAG, "Page finished loading");
            }
        });

        webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
    }

    public void close() {
        webView.destroy();
    }

    public String packetToJSON(Packet packet){
        ObjectMapper objectMapper = new ObjectMapper();
        // don't include null values
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        String packetJSON = null;
        try {
            packetJSON = objectMapper.writeValueAsString(packet);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return packetJSON;
    }

    // convert the isPacketValid function to Java
    private static boolean isPacketValid(Packet packet) {
        boolean isNamespaceValid = packet.nsp != null && packet.nsp instanceof String;
        if (!isNamespaceValid) {
            return false;
        }
        switch (packet.type) {
            case 0: // CONNECT
                return packet.data == null || packet.data instanceof Object;
            case 1: // DISCONNECT
                return packet.data == null;
            case 2: // EVENT
                return packet.data instanceof ArrayList && ((ArrayList) packet.data).size() > 0;
            case 3: // ACK
                return packet.data instanceof ArrayList;
            case 4: // CONNECT_ERROR
                return packet.data instanceof Object;
            default:
                return false;
        }
    }
}
