package net.xaethos.tabby;

import java.io.IOException;
import java.net.URI;

import net.xaethos.tabby.fragment.RepresentationFragment;
import net.xaethos.tabby.fragment.SimpleRepresentationFragment;
import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import net.xaethos.tabby.net.ApiRequest;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.theoryinpractise.halbuilder.api.Link;

public class MainActivity extends FragmentActivity implements RepresentationFragment.OnLinkFollowListener
{
    private static final String TAG = "MainActivity";

    // State keys
    private static final String ARG_REPRESENTATION = "representation";

    // ***** Instance fields

    private ParcelableReadableRepresentation mRepresentation;

    // ***** Instance methods

    private ParcelableReadableRepresentation getRepresentation() {
        return mRepresentation;
    }

    private void setRepresentation(ParcelableReadableRepresentation representation) {
        mRepresentation = representation;
    }

    private URI getSelfURI() {
        ParcelableReadableRepresentation representation = getRepresentation();

        if (representation != null) {
            Link self = representation.getResourceLink();
            if (self != null) return URI.create(self.getHref());
        }

        Uri uri = getIntent().getData();
        if (uri != null) {
            return URI.create(uri.toString());
        }

        return URI.create("/");
    }

    // *** Activity life-cycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParcelableReadableRepresentation representation = loadRepresentation(savedInstanceState);
        if (representation != null) {
            loadRepresentationFragment(representation);
        }
        else {
            setContentView(R.layout.view_loading);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mRepresentation != null) outState.putParcelable(ARG_REPRESENTATION, mRepresentation);
    }

    // *** RepresentationFragment.OnLinkFollowListener implementation

    @Override
    public void onFollowLink(Link link) {
        if (link == null) {
            Toast.makeText(this, "No link to follow", Toast.LENGTH_SHORT).show();
            return;
        }

        if (link.hasTemplate()) {
            Toast.makeText(this, "Can't follow templated links", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.getHref()), this, this.getClass());
        startActivity(intent);
    }

    // *** Helper methods

    private ParcelableReadableRepresentation loadRepresentation(Bundle savedInstanceState) {
        ParcelableReadableRepresentation representation = getRepresentation();

        if (representation == null && savedInstanceState != null) {
            representation = savedInstanceState.getParcelable(ARG_REPRESENTATION);
            if ((representation = savedInstanceState.getParcelable(ARG_REPRESENTATION)) != null) {
                setRepresentation(representation);
            }
        }

        if (representation == null) {
            ApiGetRequestTask request = new ApiGetRequestTask();
            request.execute(getSelfURI());
        }

        return representation;
    }

    private void loadRepresentationFragment(ParcelableReadableRepresentation representation) {
        FragmentManager manager = getSupportFragmentManager();

        if (manager.findFragmentById(android.R.id.content) == null) {
            ((ViewGroup) findViewById(android.R.id.content)).removeAllViews();

            Fragment fragment = SimpleRepresentationFragment.withRepresentation(representation);

            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(android.R.id.content, fragment);
            transaction.commit();
        }
    }

    // ***** Inner classes

    class ApiGetRequestTask extends AsyncTask<URI, Void, ParcelableReadableRepresentation>
    {

        @Override
        protected ParcelableReadableRepresentation doInBackground(URI... paths) {
            try {
                return ApiRequest.get(paths[0]);
            }
            catch (IOException e) {
                Log.e(TAG, "Error fetching from " + paths[0], e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ParcelableReadableRepresentation result) {
            if (result != null) {
                setRepresentation(result);
                loadRepresentationFragment(result);
            }
        }

    }

}
