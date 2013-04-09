package net.xaethos.tabby.fragment;

import java.util.Map;
import java.util.Map.Entry;

import net.xaethos.tabby.R;
import net.xaethos.tabby.halbuilder.impl.api.Support;
import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theoryinpractise.halbuilder.api.Link;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;

public class BaseRepresentationFragment extends Fragment implements RepresentationFragment, View.OnClickListener
{
    // ***** Constants

    // *** Argument/State keys
    protected static final String ARG_REPRESENTATION = "representation";

    protected static final String ARG_FRAGMENT_LAYOUT = "fragment_layout";
    protected static final String ARG_PROPERTY_LAYOUT = "property_layout";
    protected static final String ARG_LINK_LAYOUT = "link_layout";

    // ***** Instance fields

    private ParcelableReadableRepresentation mRepresentation;
    protected OnLinkFollowListener mLinkListener;

    // ***** Instance methods

    public ParcelableReadableRepresentation getRepresentation() {
        if (mRepresentation == null) {
            Bundle args = getArguments();
            if (args != null) {
                mRepresentation = args.getParcelable(ARG_REPRESENTATION);
            }
        }
        return mRepresentation;
    }

    public void setRepresentation(ParcelableReadableRepresentation representation) {
        mRepresentation = representation;
    }

    protected int getFragmentLayoutRes() {
        return getArguments().getInt(ARG_FRAGMENT_LAYOUT);
    }

    protected int getPropertyItemRes() {
        return getArguments().getInt(ARG_PROPERTY_LAYOUT);
    }

    protected int getLinkItemRes() {
        return getArguments().getInt(ARG_LINK_LAYOUT);
    }

    protected boolean isViewableRel(String rel) {
        return !(Support.SELF.equals(rel) || Support.CURIE.equals(rel) || Support.PROFILE.equals(rel));
    }

    // *** Fragment lifecycle

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnLinkFollowListener) mLinkListener = (OnLinkFollowListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getFragmentLayoutRes(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        bindRepresentation(view, getRepresentation());
    }

    // *** View binding

    public void bindRepresentation(View view, ParcelableReadableRepresentation representation) {
        bindProperties(view, representation);
        bindLinks(view, representation);
    }

    private void bindProperties(View view, ParcelableReadableRepresentation representation) {
        Map<String, Object> properties = representation.getProperties();

        ViewGroup propertiesLayout = (ViewGroup) view.findViewById(R.id.properties_layout);
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        if (propertiesLayout != null) {
            for (String name : properties.keySet()) {
                View propertyView = getPropertyView(inflater, propertiesLayout, representation, name);
                bindPropertyView(propertyView, representation, name, properties.get(name));
                if (propertyView.getParent() == null) {
                    propertiesLayout.addView(propertyView);
                }
            }
        }
    }

    public View getPropertyView(LayoutInflater inflater,
            ViewGroup container,
            ParcelableReadableRepresentation representation,
            String name)
    {
        View view = getView().findViewWithTag(propertyTag(name));
        if (view == null && container != null) {
            view = inflater.inflate(getPropertyItemRes(), container, false);
        }
        return view;
    }

    public void bindPropertyView(View propertyView,
            ParcelableReadableRepresentation representation,
            String name,
            Object value)
    {
        View childView;

        childView = propertyView.findViewById(R.id.property_name);
        if (childView instanceof TextView) {
            ((TextView) childView).setText(name);
        }

        childView = propertyView.findViewById(R.id.property_value);
        if (childView instanceof TextView) {
            ((TextView) childView).setText(value.toString());
        }
    }

    private void bindLinks(View view, ParcelableReadableRepresentation representation) {
        ViewGroup layout = (ViewGroup) view.findViewById(R.id.links_layout);
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        // Resources
        for (Entry<String, ReadableRepresentation> entry : representation.getResources()) {
            String rel = entry.getKey();
            if (!isViewableRel(rel)) continue;
            ParcelableReadableRepresentation resource = (ParcelableReadableRepresentation) entry.getValue();
            View resourceView = getResourceView(inflater, layout, representation, rel, resource);
            if (resourceView != null) {
                bindResourceView(resourceView, representation, rel, resource);
                if (resourceView.getParent() == null && layout != null) {
                    layout.addView(resourceView);
                }
            }
        }

        // Links
        for (Link link : representation.getLinks()) {
            if (!isViewableRel(link.getRel())) continue;
            View linkView = getLinkView(inflater, layout, representation, link);
            if (linkView != null) {
                bindLinkView(linkView, representation, link);
                if (linkView.getParent() == null && layout != null) {
                    layout.addView(linkView);
                }
            }
        }
    }

    public View getLinkView(LayoutInflater inflater,
            ViewGroup container,
            ParcelableReadableRepresentation representation,
            Link link)
    {
        View view = getView().findViewWithTag(linkTag(link.getRel()));
        if (view == null && container != null) {
            view = inflater.inflate(getLinkItemRes(), container, false);
        }
        return view;
    }

    public void bindLinkView(View propertyView, ParcelableReadableRepresentation representation, Link link) {
        View childView;

        childView = propertyView.findViewById(R.id.link_title);
        if (childView instanceof TextView) {
            String title = link.getTitle();
            if (TextUtils.isEmpty(title)) title = link.getRel();
            ((TextView) childView).setText(title);
        }

        childView.setOnClickListener(this);
        childView.setTag(R.id.tag_link, link);
    }

    public View getResourceView(LayoutInflater inflater,
            ViewGroup container,
            ParcelableReadableRepresentation representation,
            String rel,
            ParcelableReadableRepresentation resource)
    {
        View view = getView().findViewWithTag(linkTag(rel));
        if (view == null && container != null) {
            view = inflater.inflate(getLinkItemRes(), container, false);
            ((TextView) view).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
        return view;
    }

    public void bindResourceView(View propertyView,
            ParcelableReadableRepresentation representation,
            String rel,
            ParcelableReadableRepresentation resource)
    {
        View childView;
        Link link = resource.getResourceLink();

        childView = propertyView.findViewById(R.id.link_title);
        if (childView instanceof TextView) {
            String title = link.getTitle();
            if (TextUtils.isEmpty(title)) title = rel;
            ((TextView) childView).setText(title);
        }

        if (link != null) {
            childView.setOnClickListener(this);
            childView.setTag(R.id.tag_link, link);
        }
    }

    // *** View.OnClickListener implementation

    @Override
    public void onClick(View v) {
        if (mLinkListener != null) {
            Link link = (Link) v.getTag(R.id.tag_link);
            if (link != null) mLinkListener.onFollowLink(link);
        }
    }

    // *** Helpers

    protected String propertyTag(String name) {
        return "property:" + name;
    }

    protected String linkTag(String name) {
        return "rel:" + name;
    }

    // ***** Inner classes

    public static class ArgumentsBuilder
    {
        private final Bundle mArgs;

        public ArgumentsBuilder() {
            Bundle args = new Bundle();
            // Set defaults...
            args.putInt(ARG_FRAGMENT_LAYOUT, R.layout.default_representation_view);
            args.putInt(ARG_PROPERTY_LAYOUT, R.layout.default_property_item);
            args.putInt(ARG_LINK_LAYOUT, R.layout.default_link_item);

            mArgs = args;
        }

        public ArgumentsBuilder setRepresentation(ParcelableReadableRepresentation representation) {
            mArgs.putParcelable(ARG_REPRESENTATION, representation);
            return this;
        }

        public ArgumentsBuilder setFragmentView(int resId) {
            mArgs.putInt(ARG_FRAGMENT_LAYOUT, resId);
            return this;
        }

        public ArgumentsBuilder setPropertyView(int resId) {
            mArgs.putInt(ARG_PROPERTY_LAYOUT, resId);
            return this;
        }

        public ArgumentsBuilder setLinkView(int resId) {
            mArgs.putInt(ARG_LINK_LAYOUT, resId);
            return this;
        }

        public Bundle build() {
            return new Bundle(mArgs);
        }
    }

}
