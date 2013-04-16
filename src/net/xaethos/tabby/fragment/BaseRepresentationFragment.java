package net.xaethos.tabby.fragment;

import java.util.Map;

import net.xaethos.android.halparser.HALLink;
import net.xaethos.android.halparser.HALResource;
import net.xaethos.tabby.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BaseRepresentationFragment extends Fragment implements RepresentationFragment, View.OnClickListener
{
    // ***** Constants

    // *** Argument/State keys
    protected static final String ARG_REPRESENTATION = "representation";

    protected static final String ARG_FRAGMENT_LAYOUT = "fragment_layout";
    protected static final String ARG_PROPERTY_LAYOUT = "property_layout";
    protected static final String ARG_LINK_LAYOUT = "link_layout";
    protected static final String ARG_PROPERTY_LAYOUT_MAP = "property_layout_map";
    protected static final String ARG_LINK_LAYOUT_MAP = "link_layout_map";
    protected static final String ARG_REL_SHOW_MAP = "rel_show_map";

    // ***** Instance fields

    private HALResource mRepresentation;
    protected OnLinkFollowListener mLinkListener;

    // ***** Instance methods

    @Override
    public HALResource getRepresentation() {
        if (mRepresentation == null) {
            Bundle args = getArguments();
            if (args != null) {
                mRepresentation = args.getParcelable(ARG_REPRESENTATION);
            }
        }
        return mRepresentation;
    }

    @Override
    public void setRepresentation(HALResource representation) {
        mRepresentation = representation;
    }

    protected int getFragmentLayoutRes() {
        return getArguments().getInt(ARG_FRAGMENT_LAYOUT);
    }

    protected int getPropertyItemRes(String name) {
        Bundle args = getArguments();
        Bundle map = args.getBundle(ARG_PROPERTY_LAYOUT_MAP);

        if (map.containsKey(name)) return map.getInt(name);
        return args.getInt(ARG_PROPERTY_LAYOUT);
    }

    protected int getLinkItemRes(String rel) {
        Bundle args = getArguments();
        Bundle map = args.getBundle(ARG_LINK_LAYOUT_MAP);

        if (map.containsKey(rel)) return map.getInt(rel);

        return args.getInt(ARG_LINK_LAYOUT);
    }

    protected boolean isViewableRel(String rel) {
        return getArguments().getBundle(ARG_REL_SHOW_MAP).getBoolean(rel, true);
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

    @Override
    public void bindRepresentation(View view, HALResource representation) {
        bindProperties(view, representation);
        bindLinks(view, representation);
    }

    private void bindProperties(View view, HALResource representation) {
        Map<String, Object> properties = representation.getProperties();

        ViewGroup propertiesLayout = (ViewGroup) view.findViewById(R.id.properties_layout);
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        for (String name : properties.keySet()) {
            View propertyView = getPropertyView(inflater, view, propertiesLayout, representation, name);
            if (propertyView != null) {
                bindPropertyView(propertyView, representation, name, properties.get(name));
                if (propertyView.getParent() == null && propertiesLayout != null) {
                    propertiesLayout.addView(propertyView);
                }
            }
        }

    }

    @Override
    public View getPropertyView(LayoutInflater inflater,
            View rootView,
            ViewGroup container,
            HALResource representation,
            String name)
    {
        View view = rootView.findViewWithTag(propertyTag(name));
        if (view == null && container != null) {
            view = inflater.inflate(getPropertyItemRes(name), container, false);
        }
        return view;
    }

    @Override
    public void bindPropertyView(View propertyView, HALResource representation, String name, Object value) {
        View childView;

        childView = propertyView.findViewById(R.id.property_name);
        if (childView instanceof TextView) {
            ((TextView) childView).setText(name);
        }

        childView = propertyView.findViewById(R.id.property_value);
        if (childView instanceof TextView) {
            ((TextView) childView).setText(value == null ? "" : value.toString());
        }
    }

    private void bindLinks(View view, HALResource representation) {
        ViewGroup layout = (ViewGroup) view.findViewById(R.id.links_layout);
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        // Links
        for (String rel : representation.getLinkRels()) {
            if (!isViewableRel(rel)) continue;
            for (HALLink link : representation.getLinks(rel)) {
                View linkView = getLinkView(inflater, view, layout, representation, link);
                if (linkView != null) {
                    bindLinkView(linkView, representation, link);
                    if (linkView.getParent() == null && layout != null) {
                        layout.addView(linkView);
                    }
                }
            }
        }

        // Resources
        for (String rel : representation.getResourceRels()) {
            if (!isViewableRel(rel)) continue;
            for (HALResource resource : representation.getResources(rel)) {
                View resourceView = getResourceView(inflater, view, layout, representation, rel, resource);
                if (resourceView != null) {
                    bindResourceView(resourceView, representation, rel, resource);
                    if (resourceView.getParent() == null && layout != null) {
                        layout.addView(resourceView);
                    }
                }
            }
        }
    }

    @Override
    public View getLinkView(LayoutInflater inflater,
            View rootView,
            ViewGroup container,
            HALResource representation,
            HALLink link)
    {
        String rel = link.getRel();
        View view = rootView.findViewWithTag(linkTag(rel));
        if (view == null && container != null) {
            view = inflater.inflate(getLinkItemRes(rel), container, false);
        }
        return view;
    }

    @Override
    public void bindLinkView(View linkView, HALResource representation, HALLink link) {
        View childView;

        childView = linkView.findViewById(R.id.link_title);
        if (childView instanceof TextView) {
            String title = (String) link.getAttribute("title");
            if (TextUtils.isEmpty(title)) title = link.getRel();
            ((TextView) childView).setText(title);
        }

        linkView.setOnClickListener(this);
        linkView.setTag(R.id.tag_link, link);
    }

    @Override
    public View getResourceView(LayoutInflater inflater,
            View rootView,
            ViewGroup container,
            HALResource representation,
            String rel,
            HALResource resource)
    {
        View view = getView().findViewWithTag(linkTag(rel));
        if (view == null && container != null) {
            view = inflater.inflate(getLinkItemRes(rel), container, false);
        }
        return view;
    }

    @Override
    public void bindResourceView(View resourceView, HALResource representation, String rel, HALResource resource) {
        View childView;
        HALLink link = resource.getLink("self");

        childView = resourceView.findViewById(R.id.link_title);
        if (childView instanceof TextView) {
            String title = (String) link.getAttribute("title");
            if (TextUtils.isEmpty(title)) title = rel;
            ((TextView) childView).setText(title);
        }

        if (link != null) {
            resourceView.setOnClickListener(this);
            resourceView.setTag(R.id.tag_link, link);
        }

        bindProperties(resourceView, resource);
        bindLinks(resourceView, resource);
    }

    // *** View.OnClickListener implementation

    @Override
    public void onClick(View v) {
        if (mLinkListener != null) {
            HALLink link = (HALLink) v.getTag(R.id.tag_link);
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

    public static class Builder
    {
        private static final String[] BASIC_RELS = { "self", "curie", "profile" };

        private final Bundle mArgs;

        public Builder() {
            Bundle args = new Bundle();
            Bundle subArgs;
            // Set defaults...
            args.putInt(ARG_FRAGMENT_LAYOUT, R.layout.default_representation_view);
            args.putInt(ARG_PROPERTY_LAYOUT, R.layout.default_property_item);
            args.putBundle(ARG_PROPERTY_LAYOUT_MAP, new Bundle());
            args.putInt(ARG_LINK_LAYOUT, R.layout.default_link_item);
            args.putBundle(ARG_LINK_LAYOUT_MAP, new Bundle());

            subArgs = new Bundle();
            for (String rel : BASIC_RELS) {
                subArgs.putBoolean(rel, false);
            }
            args.putBundle(ARG_REL_SHOW_MAP, subArgs);

            mArgs = args;
        }

        public Builder setRepresentation(HALResource representation) {
            mArgs.putParcelable(ARG_REPRESENTATION, representation);
            return this;
        }

        public Builder setFragmentView(int resId) {
            mArgs.putInt(ARG_FRAGMENT_LAYOUT, resId);
            return this;
        }

        public Builder setDefaultPropertyView(int resId) {
            mArgs.putInt(ARG_PROPERTY_LAYOUT, resId);
            return this;
        }

        public Builder setPropertyView(String name, int resId) {
            mArgs.getBundle(ARG_PROPERTY_LAYOUT_MAP).putInt(name, resId);
            return this;
        }

        public Builder setDefaultLinkView(int resId) {
            mArgs.putInt(ARG_LINK_LAYOUT, resId);
            return this;
        }

        public Builder setLinkView(String rel, int resId) {
            mArgs.getBundle(ARG_LINK_LAYOUT_MAP).putInt(rel, resId);
            return this;
        }

        public Builder showBasicRels(boolean show) {
            Bundle showMap = mArgs.getBundle(ARG_REL_SHOW_MAP);
            for (String rel : BASIC_RELS) {
                showMap.putBoolean(rel, show);
            }
            return this;
        }

        public Builder showRel(String rel, boolean show) {
            mArgs.getBundle(ARG_REL_SHOW_MAP).putBoolean(rel, show);
            return this;
        }

        public <T extends BaseRepresentationFragment> T buildFragment(Class<T> klass) {
            T fragment;
            try {
                fragment = klass.newInstance();
            }
            catch (java.lang.InstantiationException e) {
                throw new IllegalArgumentException(klass.getName() + " must implement the zero-argument contructor.");
            }
            catch (IllegalAccessException e) {
                throw new IllegalArgumentException(klass.getName() + " must have a public zero-argument contructor.");
            }

            fragment.setArguments(mArgs);
            return fragment;
        }

        public Bundle buildArguments() {
            return new Bundle(mArgs);
        }
    }

}
