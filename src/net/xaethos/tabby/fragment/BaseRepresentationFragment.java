package net.xaethos.tabby.fragment;

import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class BaseRepresentationFragment extends Fragment implements RepresentationFragment
{
    protected static final String ARG_REPRESENTATION = "representation";

    // ***** Instance fields

    private ParcelableReadableRepresentation mRepresentation;
    protected OnLinkFollowListener mLinkListener;

    // ***** Instance methods

    protected ParcelableReadableRepresentation getRepresentation() {
        if (mRepresentation == null) {
            Bundle args = getArguments();
            if (args != null) {
                mRepresentation = args.getParcelable(ARG_REPRESENTATION);
            }
        }
        return mRepresentation;
    }

    protected void setRepresentation(ParcelableReadableRepresentation representation) {
        mRepresentation = representation;
    }

    // *** Fragment lifecycle

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnLinkFollowListener) mLinkListener = (OnLinkFollowListener) activity;
    }

}
