package net.xaethos.tabby.fragment;

import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.theoryinpractise.halbuilder.api.Link;

public interface RepresentationFragment
{

    public ParcelableReadableRepresentation getRepresentation();

    public void setRepresentation(ParcelableReadableRepresentation representation);

    public void bindRepresentation(View view, ParcelableReadableRepresentation representation);

    public View getPropertyView(LayoutInflater inflater,
            ViewGroup container,
            ParcelableReadableRepresentation representation,
            String name);

    public void bindPropertyView(View propertyView,
            ParcelableReadableRepresentation representation,
            String name,
            Object value);

    public View getLinkView(LayoutInflater inflater,
            ViewGroup container,
            ParcelableReadableRepresentation representation,
            Link link);

    public void bindLinkView(View propertyView,
            ParcelableReadableRepresentation representation,
            Link link);

    public View getResourceView(LayoutInflater inflater,
            ViewGroup container,
            ParcelableReadableRepresentation representation,
            String rel,
            ParcelableReadableRepresentation resource);

    public void bindResourceView(View propertyView,
            ParcelableReadableRepresentation representation,
            String rel,
            ParcelableReadableRepresentation resource);

    // ***** Inner classes

    public interface OnLinkFollowListener
    {
        void onFollowLink(Link link);
    }

}
