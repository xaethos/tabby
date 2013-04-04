package net.xaethos.tabby.fragment;

import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class RepresentationFragment extends Fragment {

	public static RepresentationFragment withRepresentation(
	        ParcelableReadableRepresentation representation) {
		RepresentationFragment fragment = new RepresentationFragment();
		Bundle args = new Bundle();
		args.putParcelable("representation", representation);
		fragment.setArguments(args);
		return fragment;
	}

}
