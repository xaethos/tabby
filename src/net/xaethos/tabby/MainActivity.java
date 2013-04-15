package net.xaethos.tabby;

import java.io.IOException;
import java.net.URI;

import net.xaethos.android.halparser.HALLink;
import net.xaethos.android.halparser.HALResource;
import net.xaethos.tabby.fragment.BaseRepresentationFragment;
import net.xaethos.tabby.fragment.RepresentationFragment;
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

public class MainActivity extends FragmentActivity implements RepresentationFragment.OnLinkFollowListener
{
    // ***** Constants

    private static final String TAG = "MainActivity";

    // State keys
    private static final String ARG_REPRESENTATION = "representation";

    // ***** Instance fields

    private HALResource mRepresentation;

    // ***** Instance methods

    private URI getSelfURI() {
        if (mRepresentation != null) {
            HALLink self = mRepresentation.getLink("self");
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

        HALResource representation = loadRepresentation(savedInstanceState);
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
    public void onFollowLink(HALLink link) {
        if (link == null) {
            Toast.makeText(this, "No link to follow", Toast.LENGTH_SHORT).show();
            return;
        }

//        if ((Boolean) link.getAttribute("templated")) {
//            Toast.makeText(this, "Can't follow templated links", Toast.LENGTH_SHORT).show();
//            return;
//        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.getHref()), this, this.getClass());
        startActivity(intent);
    }

    // *** Helper methods

    private HALResource loadRepresentation(Bundle savedInstanceState) {
        if (mRepresentation == null && savedInstanceState != null) {
            mRepresentation = savedInstanceState.getParcelable(ARG_REPRESENTATION);
        }

        if (mRepresentation == null) {
            ApiGetRequestTask request = new ApiGetRequestTask();
            request.execute(getSelfURI());
        }

        return mRepresentation;
    }

    private void loadRepresentationFragment(HALResource representation) {
        FragmentManager manager = getSupportFragmentManager();

        if (manager.findFragmentById(android.R.id.content) == null) {
            ((ViewGroup) findViewById(android.R.id.content)).removeAllViews();

            Fragment fragment = getRepresentationFragment(representation);

            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(android.R.id.content, fragment);
            transaction.commit();
        }
    }

    private BaseRepresentationFragment getRepresentationFragment(HALResource representation) {
        BaseRepresentationFragment.Builder builder = new BaseRepresentationFragment.Builder();
        builder.setRepresentation(representation);

        // builder.showBasicRels(true);
        builder.showRel("curies", false);

        builder.setLinkView("ht:post", R.layout.post_link_item);

        String href = representation.getLink("self").getHref();

        if ("/".equals(href)) {
            builder.setFragmentView(R.layout.root_representation_view);
        }

        return builder.buildFragment(BaseRepresentationFragment.class);
    }

    // ***** Inner classes

    class ApiGetRequestTask extends AsyncTask<URI, Void, HALResource>
    {

        @Override
        protected HALResource doInBackground(URI... paths) {
            try {
                return ApiRequest.get(paths[0]);
            }
            catch (IOException e) {
                Log.e(TAG, "Error fetching from " + paths[0], e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(HALResource result) {
            if (result != null) {
                mRepresentation = result;
                loadRepresentationFragment(result);
            }
            else {
                Toast.makeText(MainActivity.this, "Couldn't GET relation :(", Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }

}
