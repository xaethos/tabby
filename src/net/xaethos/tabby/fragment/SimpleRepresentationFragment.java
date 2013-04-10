package net.xaethos.tabby.fragment;

import net.xaethos.tabby.adapter.SimpleRepresentationAdapter;
import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.theoryinpractise.halbuilder.api.Link;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;

public class SimpleRepresentationFragment extends ListFragment
        implements
        RepresentationFragment,
        AdapterView.OnItemClickListener
{
    private static final String ARG_REPRESENTATION = "representation";

    // ***** Class methods

    public static SimpleRepresentationFragment withRepresentation(ParcelableReadableRepresentation representation) {
        SimpleRepresentationFragment fragment = new SimpleRepresentationFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_REPRESENTATION, representation);
        fragment.setArguments(args);
        return fragment;
    }

    // ***** Instance fields

    private ParcelableReadableRepresentation mRepresentation;
    private RepresentationFragment.OnLinkFollowListener mLinkListener;
    private SimpleRepresentationAdapter mAdapter;

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

    // *** Fragment lifecycle

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getRepresentation() == null) {
            throw new IllegalStateException("must have a representation");
        }

        if (activity instanceof OnLinkFollowListener) mLinkListener = (OnLinkFollowListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new SimpleRepresentationAdapter(getActivity(), getRepresentation());
        setListAdapter(mAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setOnItemClickListener(this);
    }

    // *** AdapterView.OnItemClickListener implementation

    @Override
    public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
        Object o = mAdapter.getItem(position);
        Link link = null;

        if (o instanceof Link) {
            link = (Link) o;
        }
        else if (o instanceof ReadableRepresentation) {
            link = ((ReadableRepresentation) o).getResourceLink();
        }

        if (link != null && mLinkListener != null) {
            mLinkListener.onFollowLink(link);
        }
    }

    @Override
    public void bindRepresentation(View view, ParcelableReadableRepresentation representation) {
        // TODO Auto-generated method stub

    }

    @Override
    public View getPropertyView(LayoutInflater inflater,
            View rootView,
            ViewGroup container,
            ParcelableReadableRepresentation representation,
            String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void bindPropertyView(View propertyView,
            ParcelableReadableRepresentation representation,
            String name,
            Object value)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public View getLinkView(LayoutInflater inflater,
            View rootView,
            ViewGroup container,
            ParcelableReadableRepresentation representation,
            Link link)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void bindLinkView(View propertyView, ParcelableReadableRepresentation representation, Link link) {
        // TODO Auto-generated method stub

    }

    @Override
    public View getResourceView(LayoutInflater inflater,
            View rootView,
            ViewGroup container,
            ParcelableReadableRepresentation representation,
            String rel,
            ParcelableReadableRepresentation resource)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void bindResourceView(View propertyView,
            ParcelableReadableRepresentation representation,
            String rel,
            ParcelableReadableRepresentation resource)
    {
        // TODO Auto-generated method stub

    }

}
