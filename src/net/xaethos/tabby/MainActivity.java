package net.xaethos.tabby;

import java.net.URI;
import java.util.Map;

import net.xaethos.android.halbrowser.APIClient;
import net.xaethos.android.halbrowser.fragment.BaseResourceFragment;
import net.xaethos.android.halbrowser.fragment.ResourceFragment;
import net.xaethos.android.halbrowser.fragment.URITemplateDialogFragment;
import net.xaethos.android.halparser.HALLink;
import net.xaethos.android.halparser.HALResource;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends FragmentActivity
        implements
        ResourceFragment.OnLinkFollowListener,
        LoaderManager.LoaderCallbacks<HALResource>
{
    // State keys
    private static final String ARG_RESOURCE = "resource";

    // ***** Instance fields

    private HALResource mResource;

    // ***** Instance methods

    // *** Activity life-cycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HALResource resource = loadResource(savedInstanceState);
        if (resource != null) {
            loadResourceFragment(resource);
        }
        else {
            setContentView(R.layout.view_loading);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mResource != null) outState.putParcelable(ARG_RESOURCE, mResource);
    }

    // *** ResourceFragment.OnLinkFollowListener implementation

    @Override
    public void onFollowLink(HALLink link, Map<String, Object> map) {
        if (link == null) {
            Toast.makeText(this, "No link to follow", Toast.LENGTH_SHORT).show();
            return;
        }

        if (link.isTemplated()) {
            if (map == null) {
                URITemplateDialogFragment.forLink(link).show(getSupportFragmentManager(), "uritemplate");
            }
            else {
                followURI(link.getURI(map));
            }
            return;
        }

        if (link.getRel() == "external") {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.getHref()));
            startActivity(intent);
            return;
        }

        followURI(link.getURI());
    }

    // *** Helper methods

    protected void followURI(URI target) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(target.toString()), this, this.getClass());
        startActivity(intent);
    }

    private HALResource loadResource(Bundle savedInstanceState) {
        if (mResource == null && savedInstanceState != null) {
            mResource = savedInstanceState.getParcelable(ARG_RESOURCE);
        }

        if (mResource == null) {
            getLoaderManager().initLoader(0, null, this);
        }

        return mResource;
    }

    private void loadResourceFragment(HALResource resource) {
        FragmentManager manager = getSupportFragmentManager();

        if (manager.findFragmentById(android.R.id.content) == null) {
            ((ViewGroup) findViewById(android.R.id.content)).removeAllViews();

            Fragment fragment = getResourceFragment(resource);

            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(android.R.id.content, fragment);
            transaction.commit();
        }
    }

    private BaseResourceFragment getResourceFragment(HALResource resource) {
        BaseResourceFragment.Builder builder = new BaseResourceFragment.Builder();
        builder.setResource(resource);

        return builder.buildFragment(BaseResourceFragment.class);
    }

    // *** LoaderManager.LoaderCallbacks<HALResource>

    @Override
    public Loader<HALResource> onCreateLoader(int id, Bundle args) {
        APIClient client = new APIClient.Builder("http://enigmatic-plateau-6595.herokuapp.com/").setEntryPath("/articles")
                                                                                                .build();
        Uri uri = getIntent().getData();
        if (uri != null) {
            return client.getLoaderForURI(this, uri.toString());
        }
        else {
            return client.getLoader(this);
        }
    }

    @Override
    public void onLoadFinished(Loader<HALResource> loader, HALResource resource) {
        if (resource != null) {
            mResource = resource;
            loadResourceFragment(resource);
        }
        else {
            Toast.makeText(MainActivity.this, "Couldn't GET relation :(", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<HALResource> loader) {
        mResource = null;
    }

}
