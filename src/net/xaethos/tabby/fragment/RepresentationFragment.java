package net.xaethos.tabby.fragment;

import com.theoryinpractise.halbuilder.api.Link;

public interface RepresentationFragment
{

    // ***** Inner classes

    public interface OnLinkFollowListener
    {
        void onFollowLink(Link link);
    }

}
