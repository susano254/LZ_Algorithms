package com.susano.LZ_Algorithms;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnection extends AsyncTask<Void, Void, String> {

    @Override
    protected String doInBackground(Void... voids) {
        String response = "";
        try {
            // The URL you want to send the GET request to
            URL url = new URL("https://oddssocketdev.bestlive.io/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Set some optional properties
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000);    // 10 seconds

            // Get the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder responseBuilder = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    responseBuilder.append(inputLine);
                }
                in.close();

                // Convert the response to a string
                response = responseBuilder.toString();
            } else {
                response = "GET request failed. Response Code: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = "Exception: " + e.getMessage();
        }

        return response;
    }

    // turns out this wasn't the data to be decoded (as specificed in the requirements) this was just the epoch time not the socket connection
    @Override
    protected void onPostExecute(String result) {
        // Here you can update the UI with the result
        Log.d("HttpGetExample", "Response: " + result);

        // this the response from the server:  Hello Bestlive Development World!-1723549115409
        // extract the number from the response
        String[] parts = result.split("!");
        String part1 = parts[0]; // Hello Bestlive Development World
        String part2 = parts[1]; // -1723549115409

        Log.d("HttpGetExample", "part1: " + part1);
        Log.d("HttpGetExample", "part2: " + part2);

        // convert the number to a long
        long number = Long.parseLong(part2);
        Log.d("HttpGetExample", "number: " + number);

    }
}
