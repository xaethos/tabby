package net.xaethos.tabby.halbuilder.api;

import android.os.Parcel;
import android.os.Parcelable;

import com.theoryinpractise.halbuilder.api.Link;

public class ParcelableLink extends Link implements Parcelable {

	public static final Parcelable.Creator<ParcelableLink> CREATOR = new Parcelable.Creator<ParcelableLink>() {
		public ParcelableLink createFromParcel(Parcel in) {
			return new ParcelableLink(in);
		}

		public ParcelableLink[] newArray(int size) {
			return new ParcelableLink[size];
		}
	};

	private ParcelableRepresentationFactory mRepresentationFactory;

	public ParcelableLink(
			ParcelableRepresentationFactory representationFactory, String rel,
			String href) {
		super(representationFactory, rel, href);
		mRepresentationFactory = representationFactory;
	}

	public ParcelableLink(
			ParcelableRepresentationFactory representationFactory, String rel,
			String href, String name, String title, String hreflang,
			String profile) {
		super(representationFactory, rel, href, name, title, hreflang, profile);
		mRepresentationFactory = representationFactory;
	}

	public ParcelableLink(Parcel in) {
		this((ParcelableRepresentationFactory) in.readParcelable(null),
				in.readString(), in.readString(), in.readString(),
				in.readString(), in.readString(), in.readString());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(mRepresentationFactory, flags);
		out.writeString(getRel());
		out.writeString(getHref());
		out.writeString(getName());
		out.writeString(getTitle());
		out.writeString(getHreflang());
		out.writeString(getProfile());
	}

}
