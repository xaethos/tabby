package net.xaethos.tabby.fragment;

import net.xaethos.tabby.adapter.SimpleRepresentationAdapter;
import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.theoryinpractise.halbuilder.api.Link;

public class SimpleRepresentationFragment extends ListFragment
{
    private static final String TAG = "SimpleRepresentationFragment";
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

        setListAdapter(new SimpleRepresentationAdapter(getActivity(), getRepresentation()));
    }

    // *** View.OnClickListener implementation

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//        case R.id.link_button:
//            if (mLinkListener != null) {
//                Link link = (Link) v.getTag(R.id.tag_link);
//                mLinkListener.onFollowLink(link);
//            }
//            break;
//        }
//    }

    // ***** Inner classes / interfaces

    public interface OnLinkFollowListener
    {
        void onFollowLink(Link link);
    }

}
