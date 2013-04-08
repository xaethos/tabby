package net.xaethos.tabby.halbuilder.impl.representations;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.theoryinpractise.halbuilder.api.Link;

public class LinkPredicate implements Predicate<Link> {

    private final String rel;

    private LinkPredicate(final String rel) {
        this.rel = Preconditions.checkNotNull(rel, "rel must not be null");
    }

    public static LinkPredicate newLinkPredicate(final String rel) {
        return new LinkPredicate(rel);
    }

    public boolean apply(Link relatable) {
        return relatable.getRel().toLowerCase().contains(rel.toLowerCase());
    }
}
