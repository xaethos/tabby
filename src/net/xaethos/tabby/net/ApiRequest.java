package net.xaethos.tabby.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

import net.xaethos.tabby.halbuilder.DefaultRepresentationFactory;
import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import android.util.Log;

public class ApiRequest
{
    private static final String TAG = "ApiRequest";

    private static final URI API_ROOT = URI.create("http://haltalk.herokuapp.com");

    public static ParcelableReadableRepresentation get(URI uri) throws IOException {
        Log.i(TAG, "Starting GET " + uri);

        if (!uri.isAbsolute()) {
            uri = API_ROOT.resolve(uri);
        }

        ParcelableReadableRepresentation representation;

        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        conn.setRequestProperty("Accept", "application/hal+json, application/json, */*; q=0.01");

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            Log.e(TAG, "GET " + uri + " failed with: " + responseCode + " " + conn.getResponseMessage());
            return null;
        }

        DefaultRepresentationFactory factory = new DefaultRepresentationFactory();
        InputStream in = conn.getInputStream();

        try {
            representation = factory.readRepresentation(new InputStreamReader(in));
        }
        finally {
            in.close();
        }

        return representation;
    }

    public static ParcelableReadableRepresentation get(String path) throws IOException {
        return get(URI.create(path));
    }

}
