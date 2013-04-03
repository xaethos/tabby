package net.xaethos.tabby;

import java.io.IOException;
import java.util.Map;

import net.xaethos.tabby.net.ApiRequest;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.theoryinpractise.halbuilder.api.Link;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        ApiGetRequestTask request = new ApiGetRequestTask();
        request.execute("/");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    // ***** Inner classes

    class ApiGetRequestTask extends AsyncTask<String, Void, ReadableRepresentation> {

        @Override
        protected ReadableRepresentation doInBackground(String... paths) {
            try {
                return ApiRequest.get(paths[0]);
            } catch (IOException e) {
                Log.e(TAG, "Error fetching from " + paths[0], e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ReadableRepresentation result) {
            TextView text = (TextView) MainActivity.this
                    .findViewById(R.id.text);
            
            if (result == null) {
            	text.setText("error");
            	return;
            }
            
            StringBuilder sb = new StringBuilder();
            
            sb.append("Properties\n");
            Map<String, Object> props = result.getProperties();
            for (String key : props.keySet()) {
            	sb.append(key).append(" : ").append(props.get(key)).append("\n");
            }
            
            sb.append("Links\n");
            for (Link link : result.getLinks()) {
            	sb.append(link.getRel()).append(" : ").append(link.getHref()).append("\n");
            }
            
        	text.setText(sb.toString());
        }

    }

}
