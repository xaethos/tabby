package net.xaethos.tabby.fragment;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.xaethos.tabby.R;
import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import net.xaethos.tabby.net.ApiRequest;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.theoryinpractise.halbuilder.api.Link;

public class RepresentationFragment extends Fragment implements View.OnClickListener
{
    private static final String TAG = "RepresentationFragment";
    private static final String ARG_REPRESENTATION = "representation";
    private static final String ARG_SELF_URI = "self_uri";

    // ***** Class methods

    public static RepresentationFragment withRepresentation(ParcelableReadableRepresentation representation) {
        RepresentationFragment fragment = new RepresentationFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_REPRESENTATION, representation);
        fragment.setArguments(args);
        return fragment;
    }

    public static RepresentationFragment withURI(String uriString) {
        return withURI(URI.create(uriString));
    }

    public static RepresentationFragment withURI(URI uri) {
        RepresentationFragment fragment = new RepresentationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SELF_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    // ***** Instance fields

    private ParcelableReadableRepresentation mRepresentation;
    private OnLinkFollowListener mLinkListener;

    // ***** Instance methods

    private ParcelableReadableRepresentation getRepresentation() {
        if (mRepresentation == null) {
            Bundle args = getArguments();
            if (args != null) {
                mRepresentation = args.getParcelable(ARG_REPRESENTATION);
            }
        }
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

        Bundle args = getArguments();
        if (args != null) {
            return (URI) args.getSerializable(ARG_SELF_URI);
        }

        return null;
    }

    // *** Fragment lifecycle

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnLinkFollowListener) mLinkListener = (OnLinkFollowListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        restoreState(savedInstanceState);

        loadRepresentation(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_resource, container, false);
        populate(getRepresentation(), root);
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mRepresentation != null) outState.putParcelable(ARG_REPRESENTATION, mRepresentation);
    }

    // *** Helpers

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;

        Parcelable parcelable;

        if ((parcelable = savedInstanceState.getParcelable(ARG_REPRESENTATION)) != null) {
            setRepresentation((ParcelableReadableRepresentation) parcelable);
        }
    }

    private void loadRepresentation(Bundle savedInstanceState) {
        ParcelableReadableRepresentation representation = getRepresentation();
        if (representation == null) {
            ApiGetRequestTask request = new ApiGetRequestTask();
            request.execute(getSelfURI());
        }
    }

    private void populate(ParcelableReadableRepresentation representation, View root) {
        if (representation == null || root == null) return;
        populateProperties(representation, root);
        populateLinks(representation, root);
    }

    private void populateProperties(ParcelableReadableRepresentation representation, View root) {
        ViewGroup layout = (ViewGroup) root.findViewById(R.id.representation_properties_layout);
        Activity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();

        layout.removeAllViews();

        Map<String, Object> properties = representation.getProperties();
        for (Entry<String, Object> property : properties.entrySet()) {
            View propertyView = inflater.inflate(R.layout.view_representation_property, layout, false);
            ((TextView) propertyView.findViewById(R.id.property_name)).setText(property.getKey());
            ((TextView) propertyView.findViewById(R.id.property_value)).setText(property.getValue().toString());
            layout.addView(propertyView);
        }
    }

    private void populateLinks(ParcelableReadableRepresentation representation, View root) {
        ViewGroup layout = (ViewGroup) root.findViewById(R.id.representation_links_layout);
        Activity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();

        layout.removeAllViews();

        List<Link> links = representation.getLinks();
        for (Link link : links) {
            View linkView = inflater.inflate(R.layout.view_representation_link, layout, false);
            Button linkButton = (Button) linkView.findViewById(R.id.link_button);

            String rel = link.getRel();
            String title = link.getTitle();
            if (TextUtils.isEmpty(title)) title = rel;

            linkButton.setText(title);
            linkButton.setOnClickListener(this);
            linkButton.setTag(R.id.tag_link, link);

            layout.addView(linkView);
        }
    }

    // *** View.OnClickListener implementation

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.link_button:
            if (mLinkListener != null) {
                Link link = (Link) v.getTag(R.id.tag_link);
                mLinkListener.onFollowLink(link);
            }
            break;
        }
    }

    // ***** Inner classes

    public interface OnLinkFollowListener
    {
        void onFollowLink(Link link);
    }

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
                populate(result, getView());
            }
        }

    }

}
