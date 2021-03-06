package com.example.bulgota;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DataGetServer extends AsyncTask<Integer, Void, Void> {

    String url;
    public DataGetServer(String url) {
        this.url = url;
    }

    @Override
    protected Void doInBackground(Integer... integers) {

        HttpsURLConnection httpsURLConnection;
        String result = null;

        try {
            // Open the connection
            URL url = new URL(this.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();

            // Get the stream
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            // Set the result
            result = builder.toString();
            Log.e("데이터수신 값 : ", result);
        }
        catch (Exception e) {
            Log.e("서버 전송 여부 : ","실패");
            e.printStackTrace();
        }
        return null;
    }

}
