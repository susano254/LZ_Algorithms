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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.parser.Packet;
import io.socket.parser.Parser;

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
    private static final String TAG = "MainActivity";
    private SocketHandler socketHandler;

    // all the code for the webview is in onCreate method of MainActivity
    // don't forget to also add the internet permission in the AndroidManifest.xml file and the Webview in the xml layout file
    // the code is a bit long, but it's mostly the JavaScript code that is being executed in the WebView
    // the JavaScript code is responsible for compressing and decompressing the data using lzutf8 (same library of node.js)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize WebView and enable JavaScript
        webView = findViewById(R.id.webview);

        // Initialize the socket handler
        try {
            CustomParser customParser = new CustomParser(webView);
            IO.Options options = new IO.Options();
            options.encoder = customParser.encoder;
            options.decoder = customParser.decoder;
            socketHandler = new SocketHandler(new URI("http://10.0.2.2:3001"), options);
//            socketHandler = new SocketHandler(new URI("https://oddssocketdev.bestlive.io/"), options);
            socketHandler.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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