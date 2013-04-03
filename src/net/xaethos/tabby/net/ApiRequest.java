package net.xaethos.tabby.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.xaethos.tabby.hal.DefaultRepresentationFactory;

import android.util.Log;

import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;

public class ApiRequest {
	private static final String TAG = "ApiRequest";

	private static final String API_ROOT = "http://haltalk.herokuapp.com";

	public static ReadableRepresentation get(String path) throws IOException {
		Log.i(TAG, "Starting GET " + path);

		URL url;
		ReadableRepresentation representation;

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
			Log.e(TAG, "GET " + path + " failed with: " + responseCode + " "
					+ conn.getResponseMessage());
			return null;
		}

		RepresentationFactory factory = new DefaultRepresentationFactory();
		InputStream in = conn.getInputStream();

		try {
			representation = factory.readRepresentation(new InputStreamReader(in));
		} finally {
			in.close();
		}

		return representation;
	}

}
