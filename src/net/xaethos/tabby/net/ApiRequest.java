package net.xaethos.tabby.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

import net.xaethos.android.halparser.HALJsonParser;
import net.xaethos.android.halparser.HALResource;
import android.util.Log;

public class ApiRequest
{
    private static final String TAG = "ApiRequest";

    private static final URI API_ROOT = URI.create("http://enigmatic-plateau-6595.herokuapp.com/");

    public static HALResource get(URI uri) throws IOException {
        Log.i(TAG, "Starting GET " + uri);

        if (!uri.isAbsolute()) {
            uri = API_ROOT.resolve(uri);
        }

        HALResource resource;

        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestProperty("Accept", "application/hal+json, application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            Log.e(TAG, "GET " + uri + " failed with: " + responseCode + " " + conn.getResponseMessage());
            return null;
        }

        HALJsonParser parser = new HALJsonParser(API_ROOT);
        InputStream in = conn.getInputStream();

        try {
            resource = parser.parse(new InputStreamReader(in));
        }
        finally {
            in.close();
        }

        return resource;
    }

    public static HALResource get(String path) throws IOException {
        return get(URI.create(path));
    }

}
