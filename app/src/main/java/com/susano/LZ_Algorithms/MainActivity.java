package com.susano.LZ_Algorithms;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {
    ConstraintLayout progressBar;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    DrawerLayout drawer;
    NavigationView navView;
    Toast toast;
    TextInputEditText streamEditText;
    ArrayList<Tag> tags;

    private WebView webView;
    private boolean isPageLoaded = false;
    private Socket mSocket;
    private static final String TAG = "MainActivity";


    {
        try {
            mSocket = IO.socket("https://oddssocketdev.bestlive.io/");
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSocket.connect();

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
                "       return JSON.stringify(decompressed);" +
                "   } catch (e) {" +
                "       console.log('Error during decompression: ' + e.message);" + // Log errors
                "       return JSON.stringify('Error: ' + e.message);" +
                "   }" +
                "}" +
                "var result = decompressData(compressedData);" +
                "console.log('Decompressed data: ' + result);" +
                "</script>" +
                "</body></html>";

        // Initialize WebView and enable JavaScript
        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isPageLoaded = true;
                Log.d(TAG, "Page finished loading");

                String compressedData = "This is a string that needs to be compressed";
                String script = "compressData('" + compressedData + "').toString()";
                Log.d(TAG, "Script: " + script);

                webView.evaluateJavascript(script, value -> {
                    // 'value' will contain the compressed data
                    Log.d(TAG, "Compressed data: " + value);

                    // convert the string to array of bytes
                    String[] parts = value.split(",");
                    Log.d(TAG, "Compressed data parts: " + parts.length);

                    ArrayList<Byte> compressedDataArray = new ArrayList<>();
                    for (int i = 0; i < parts.length; i++) {
                        // trim away double quotes
                        parts[i] = parts[i].replace("\"", "");
                        Log.d(TAG, "Compressed data part: " + parts[i]);
                        compressedDataArray.add(Byte.parseByte(parts[i]));
                    }
                    Log.d(TAG, "Compressed data array: " + compressedDataArray);

//                    String compressedScript = "console.log(new Uint8Array(" + compressedDataArray + ").length)";
                    String decompressScript = "console.log(decompressData(new Uint8Array(" + compressedDataArray + ")))";
                    Log.d(TAG, "Compressed script: " + decompressScript);
                    webView.evaluateJavascript(decompressScript, value1 -> {
                        Log.d(TAG, "Received value: " + value1);
                    });

                });

            }
        });


        webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);

        progressBar = findViewById(R.id.compressingScreen);
        recyclerView = findViewById(R.id.recyclerView);
        drawer = findViewById(R.id.drawer);
        navView = findViewById(R.id.navView);
        streamEditText = findViewById(R.id.streamEditText);
        streamEditText.setText("ABAABABAABBBBBBBBBBBBA");

        tags = new ArrayList<>();
        tags.add( new Tag(0, 0, 'A') );
        tags.add( new Tag(0, 0, 'B') );
        tags.add( new Tag(2, 1, 'C') );


        setupDrawer();
        recyclerAdapter = new RecyclerAdapter(this, tags);
        recyclerView.setAdapter(recyclerAdapter);
    }


    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Connected to server");
            // Example: Join a room
            mSocket.emit("sub", "{ room: 'some_room' }");
        }
    };

    private final Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Disconnected from server");
        }
    };

    private final Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG, "Connection error: " + args[0]);
        }
    };

    private final Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Received message: " + args[0]);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off("message", onMessage);
    }


    public void openDrawer(View view) {
        drawer.openDrawer(GravityCompat.START);
    }

    private void setupDrawer(){
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                int id = item.getItemId();
                switch(id){
                    case R.id.home:
                        showToast("Home");
                        break;
                    case R.id.about:
                        intent = new Intent(MainActivity.this, about.class);
                        intent.putExtra("about",true);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });
    }
    private void showToast(String msg){
        if(toast != null) toast.cancel();
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void compress(View view) {
        //progressBar.setVisibility(View.VISIBLE);
        String stream = streamEditText.getText().toString();

        Compressor compressor = new Compressor(stream, tags);
        compressor.lz77(stream, 12);
        recyclerAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(tags.size());
    }

}