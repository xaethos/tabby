package net.xaethos.tabby.fragment;

import java.util.Map;

import net.xaethos.tabby.R;
import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BaseRepresentationFragment extends Fragment implements RepresentationFragment
{
    // ***** Constants

    // *** Argument/State keys
    protected static final String ARG_REPRESENTATION = "representation";
    protected static final String ARG_FRAGMENT_LAYOUT = "fragment_layout";

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
        return R.layout.default_property_item;
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
        if (view == null) {
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

    // *** Helpers

    protected String propertyTag(String name) {
        return "property:" + name;
    }

    // ***** Inner classes

    public static class ArgumentsBuilder
    {
        private final Bundle mArgs;

        public ArgumentsBuilder() {
            Bundle args = new Bundle();
            // Set defaults...
            args.putInt(ARG_FRAGMENT_LAYOUT, R.layout.default_representation_view);
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

        public Bundle build() {
            return new Bundle(mArgs);
        }
    }

}
