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
    ResourceFragment mFragment;

    // *** Activity life-cycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadResourceFragment(null);
        getLoaderManager().initLoader(0, null, this);
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

    private void loadResourceFragment(HALResource resource) {
        FragmentManager manager = getSupportFragmentManager();

        mFragment = (ResourceFragment) manager.findFragmentById(android.R.id.content);
        if (mFragment == null) {
            ((ViewGroup) findViewById(android.R.id.content)).removeAllViews();

            mFragment = getResourceFragment(resource);

            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(android.R.id.content, (Fragment) mFragment);
            transaction.commit();
        }
    }

    private ResourceFragment getResourceFragment(HALResource resource) {
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
            mFragment.setResource(resource);
        }
        else {
            Toast.makeText(MainActivity.this, "Couldn't GET relation :(", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<HALResource> loader) {
    }

}
