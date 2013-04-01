package net.xaethos.tabby.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class ApiRequest {
    private static final String TAG = "ApiRequest";

    private static final String API_ROOT = "http://haltalk.herokuapp.com";

    public static String get(String path) throws IOException {
        Log.i(TAG, "Starting GET " + path);

        URL url;
        String result;

        try {
            url = new URL(API_ROOT + path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        conn.setRequestProperty("Accept",
                "application/hal+json, application/json, */*; q=0.01");

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            switch (responseCode) {
            case 401:
                result = "Invalid username or password";
                break;

            default:
                result = conn.getResponseMessage();
                break;
            }
            Log.e(TAG, "GET " + path + " failed with: " + responseCode);
            return result;
        }

        InputStream in = conn.getInputStream();

        try {
            InputStreamReader is = new InputStreamReader(in);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(is);
            String read = br.readLine();

            while (read != null) {
                sb.append(read);
                read = br.readLine();
            }

            result = sb.toString();
        } finally {
            in.close();
        }

        return result;
    }

}
