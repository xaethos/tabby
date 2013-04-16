package net.xaethos.tabby;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.xaethos.android.halparser.HALLink;
import net.xaethos.android.halparser.HALResource;
import net.xaethos.tabby.fragment.BaseRepresentationFragment;
import net.xaethos.tabby.fragment.RepresentationFragment;
import net.xaethos.tabby.net.ApiRequest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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
        onFollowLink(link, null);
    }

    @Override
    public void onFollowLink(HALLink link, Map<String, Object> map) {
        if (link == null) {
            Toast.makeText(this, "No link to follow", Toast.LENGTH_SHORT).show();
            return;
        }

        if (link.isTemplated()) {
            if (map == null) {
                URITemplateDialogFragment dialog = URITemplateDialogFragment.forLink(link);
                dialog.show(getFragmentManager(), "uritemplate");
            }
            else {
                followURI(link.getURI(map));
            }
            return;
        }

        followURI(link.getURI());
    }

    // *** Helper methods

    protected void followURI(URI target) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(target.toString()), this, this.getClass());
        startActivity(intent);
    }

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

    public static class URITemplateDialogFragment extends DialogFragment implements DialogInterface.OnClickListener
    {
        private static final String ARG_LINK = "link";

        private LinearLayout mLayout;

        public static URITemplateDialogFragment forLink(HALLink link) {
            Bundle args = new Bundle(1);
            args.putParcelable(ARG_LINK, link);
            URITemplateDialogFragment fragment = new URITemplateDialogFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            HALLink link = getArguments().getParcelable(ARG_LINK);

            String title = (String) link.getAttribute("title");
            if (TextUtils.isEmpty(title)) title = link.getRel();

            return new AlertDialog.Builder(getActivity()).setTitle(title)
                                                         .setView(getContentView(link.getVariables()))
                                                         .setPositiveButton(android.R.string.ok, this)
                                                         .setCancelable(true)
                                                         .create();
        }

        private View getContentView(Set<String> variables) {
            if (mLayout == null) {
                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);
                for (String variable : variables) {
                    EditText et = new EditText(getActivity());
                    et.setHint(variable);
                    et.setTag(variable);
                    layout.addView(et);
                }
                mLayout = layout;
            }
            return mLayout;
        }

        // *** DialogInterface.OnClickListener

        @Override
        public void onClick(DialogInterface dialog, int which) {
            LinearLayout layout = mLayout;
            HALLink link = getArguments().getParcelable(ARG_LINK);
            Map<String, Object> map = new HashMap<String, Object>();

            for (String variable : link.getVariables()) {
                EditText et = (EditText) layout.findViewWithTag(variable);
                map.put(variable, et.getText().toString());
            }

            ((RepresentationFragment.OnLinkFollowListener) getActivity()).onFollowLink(link, map);
        }

    }

}
