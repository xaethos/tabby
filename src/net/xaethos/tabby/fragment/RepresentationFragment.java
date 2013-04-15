package net.xaethos.tabby.fragment;

import net.xaethos.android.halparser.HALLink;
import net.xaethos.android.halparser.HALResource;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface RepresentationFragment
{

    public HALResource getRepresentation();

    public void setRepresentation(HALResource representation);

    public void bindRepresentation(View view, HALResource representation);

    public View getPropertyView(LayoutInflater inflater,
            View rootView,
            ViewGroup container,
            HALResource representation,
            String name);

    public void bindPropertyView(View propertyView,
            HALResource representation,
            String name,
            Object value);

    public View getLinkView(LayoutInflater inflater,
            View rootView,
            ViewGroup container,
            HALResource representation,
            HALLink link);

    public void bindLinkView(View propertyView, HALResource representation, HALLink link);

    public View getResourceView(LayoutInflater inflater,
            View rootView,
            ViewGroup container,
            HALResource representation,
            String rel,
            HALResource resource);

    public void bindResourceView(View propertyView,
            HALResource representation,
            String rel,
            HALResource resource);

    // ***** Inner classes

    public interface OnLinkFollowListener
    {
        void onFollowLink(HALLink link);
    }

}
