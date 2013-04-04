package net.xaethos.tabby.halbuilder.impl.representations;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Ordering.usingToString;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static net.xaethos.tabby.halbuilder.impl.api.Support.WHITESPACE_SPLITTER;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.xaethos.tabby.halbuilder.api.ParcelableLink;
import net.xaethos.tabby.halbuilder.api.ParcelableRepresentationFactory;
import net.xaethos.tabby.halbuilder.impl.api.Support;
import net.xaethos.tabby.halbuilder.impl.bytecode.InterfaceContract;
import net.xaethos.tabby.halbuilder.impl.bytecode.InterfaceRenderer;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import com.theoryinpractise.halbuilder.api.Contract;
import com.theoryinpractise.halbuilder.api.Link;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import com.theoryinpractise.halbuilder.api.RepresentationException;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.api.RepresentationWriter;

public abstract class BaseRepresentation
        implements
        ParcelableReadableRepresentation
{

    public static final Ordering<Link> RELATABLE_ORDERING = Ordering.from(new Comparator<Link>() {
        @Override
        public int compare(Link l1, Link l2) {
            if (l1.getRel().contains("self")) return -1;
            if (l2.getRel().contains("self")) return 1;
            return l1.getRel().compareTo(l2.getRel());
        }
    });

    protected Bundle namespaces = new Bundle();
    protected List<ParcelableLink> links = Lists.newArrayList();
    protected Bundle properties = new Bundle();
    protected Bundle resources = new Bundle();

    protected ParcelableRepresentationFactory representationFactory;
    protected boolean hasNullProperties = false;

    protected BaseRepresentation(ParcelableRepresentationFactory representationFactory)
    {
        this.representationFactory = representationFactory;
    }

    protected BaseRepresentation(Parcel in) {
        this.namespaces.putAll(in.readBundle());
        in.readTypedList(this.links, ParcelableLink.CREATOR);
        this.properties.putAll(in.readBundle());
        this.resources.putAll(in.readBundle());
        this.representationFactory = in.readParcelable(null);
        this.hasNullProperties = in.readInt() != 0;
    }

    @Override
    public Link getResourceLink() {
        return Iterables.find(getLinks(),
                              LinkPredicate.newLinkPredicate(Support.SELF),
                              null);
    }

    @Override
    public Map<String, String> getNamespaces() {
        Bundle namespaces = this.namespaces;
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        for (String key : namespaces.keySet()) {
            builder.put(key, namespaces.getString(key));
        }
        return builder.build();
    }

    @Override
    public List<Link> getCanonicalLinks() {
        return ImmutableList.copyOf(getNaturalLinks());
    }

    @Override
    public Link getLinkByRel(String rel) {
        return Iterables.getFirst(getLinksByRel(rel), null);
    }

    @Override
    public List<Link> getLinksByRel(final String rel) {
        Support.checkRelType(rel);

        final String curiedRel = currieHref(rel);
        final ImmutableList.Builder<Link> linkBuilder = ImmutableList.builder();

        linkBuilder.addAll(getLinksByRel(this, curiedRel));
        // TODO Should this check descendants? Should maybe be an overloaded
        // method with a boolean check
        for (String key : resources.keySet()) {
            for (Parcelable resource : resources.getParcelableArray(key)) {
                linkBuilder.addAll(getLinksByRel((ParcelableReadableRepresentation) resource,
                                                 curiedRel));
            }
        }

        return linkBuilder.build();
    }

    @Override
    public List<? extends ReadableRepresentation>
            getResourcesByRel(final String rel)
    {
        Support.checkRelType(rel);

        return ImmutableList.copyOf((ParcelableReadableRepresentation[]) resources.getParcelableArray(rel));
    }

    @Override
    public Object getValue(String name) {
        if (properties.containsKey(name)) {
            return properties.get(name);
        }
        else {
            throw new RepresentationException("Resource does not contain "
                    + name);
        }
    }

    @Override
    public Object getValue(String name, Object defaultValue) {
        try {
            return getValue(name);
        }
        catch (RepresentationException e) {
            return defaultValue;
        }
    }

    private List<Link> getLinksByRel(ReadableRepresentation representation,
            final String rel)
    {
        Support.checkRelType(rel);
        return ImmutableList.copyOf(Iterables.filter(representation.getCanonicalLinks(),
                                                     new Predicate<Link>() {
                                                         @Override
                                                         public boolean
                                                                 apply(Link relatable)
                                                         {
                                                             return rel.equals(relatable.getRel())
                                                                     || Iterables.contains(WHITESPACE_SPLITTER.split(relatable.getRel()),
                                                                                           rel);
                                                         }
                                                     }));
    }

    @Override
    public List<Link> getLinks() {
        if (representationFactory.getFlags()
                                 .contains(RepresentationFactory.COALESCE_LINKS)) {
            return getCollatedLinks();
        }
        else {
            return getNaturalLinks();
        }
    }

    private List<Link> getNaturalLinks() {
        return FluentIterable.from(links).transform(new Function<Link, Link>() {
            @Override
            public Link apply(Link link) {
                return new Link(representationFactory,
                                currieHref(link.getRel()),
                                currieHref(link.getHref()),
                                link.getName(),
                                link.getTitle(),
                                link.getHreflang(),
                                link.getProfile());
            }
        }).toSortedImmutableList(RELATABLE_ORDERING);

    }

    private List<Link> getCollatedLinks() {
        List<Link> collatedLinks = Lists.newArrayList();

        // href, rel, link
        Table<String, String, Link> linkTable = HashBasedTable.create();

        for (Link link : links) {
            linkTable.put(link.getHref(), link.getRel(), link);
        }

        for (String href : linkTable.rowKeySet()) {
            Set<String> relTypes = linkTable.row(href).keySet();
            Collection<Link> hrefLinks = linkTable.row(href).values();

            String rels = mkSortableJoinerForIterable(" ", relTypes).apply(new Function<String, String>() {
                @Override
                public String apply(String relType) {
                    return currieHref(relType);
                }
            });

            Function<Function<Link, String>, String> nameFunc = mkSortableJoinerForIterable(", ",
                                                                                            hrefLinks);

            String titles = nameFunc.apply(new Function<Link, String>() {
                @Override
                public String apply(Link link) {
                    return link.getTitle();
                }
            });

            String names = nameFunc.apply(new Function<Link, String>() {
                @Override
                public String apply(Link link) {
                    return link.getName();
                }
            });

            String hreflangs = nameFunc.apply(new Function<Link, String>() {
                @Override
                public String apply(Link link) {
                    return link.getHreflang();
                }
            });

            String profile = nameFunc.apply(new Function<Link, String>() {
                @Override
                public String apply(Link link) {
                    return link.getProfile();
                }
            });

            String curiedHref = currieHref(href);

            collatedLinks.add(new Link(representationFactory,
                                       rels,
                                       curiedHref,
                                       emptyToNull(names),
                                       emptyToNull(titles),
                                       emptyToNull(hreflangs),
                                       emptyToNull(profile)));
        }

        return RELATABLE_ORDERING.sortedCopy(collatedLinks);
    }

    private <T>
            Function<Function<T, String>, String>
            mkSortableJoinerForIterable(final String join, final Iterable<T> ts)
    {
        return new Function<Function<T, String>, String>() {
            @Override
            public String apply(Function<T, String> f) {
                return Joiner.on(join)
                             .skipNulls()
                             .join(usingToString().nullsFirst()
                                                  .sortedCopy(newHashSet(transform(ts, f))));
            }
        };
    }

    private String currieHref(String href) {
        for (String key : namespaces.keySet()) {
            if (href.startsWith(namespaces.getString(key))) {
                return href.replace(namespaces.getString(key), key + ":");
            }
        }
        return href;
    }

    @Override
    public Map<String, Object> getProperties() {
        Bundle properties = this.properties;
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        for (String key : properties.keySet()) {
            builder.put(key, properties.get(key));
        }
        return builder.build();
    }

    @Override
    public Collection<Entry<String, ReadableRepresentation>> getResources() {
        return getResourceMultiMap().entries();
    }

    @Override
    public Map<String, Collection<ReadableRepresentation>> getResourceMap() {
        return getResourceMultiMap().asMap();
    }

    public Multimap<String, ReadableRepresentation> getResourceMultiMap() {
        Bundle resources = this.resources;
        Builder<String, ReadableRepresentation> builder = ImmutableMultimap.builder();

        for (String key : resources.keySet()) {
            for (Parcelable resource : resources.getParcelableArray(key)) {
                builder.put(key, (ParcelableReadableRepresentation) resource);
            }
        }
        return builder.build();
    }

    protected void validateNamespaces(ReadableRepresentation representation) {
        for (Link link : representation.getCanonicalLinks()) {
            validateNamespaces(link.getRel());
        }
        for (Map.Entry<String, ReadableRepresentation> aResource : representation.getResources()) {
            validateNamespaces(aResource.getKey());
            validateNamespaces(aResource.getValue());
        }
    }

    private void validateNamespaces(String sourceRel) {
        for (String rel : WHITESPACE_SPLITTER.split(sourceRel)) {
            if (!rel.contains("://") && rel.contains(":")) {
                String[] relPart = rel.split(":");
                if (!namespaces.keySet().contains(relPart[0])) {
                    throw new RepresentationException(format("Undeclared namespace in rel %s for resource",
                                                             rel));
                }
            }
        }
    }

    /**
     * Test whether the Representation in its current state satisfies the
     * provided interface.
     * 
     * @param contract
     *            The interface we wish to check
     * @return Is that Representation satisfied by the supplied contract?
     */
    @Override
    public boolean isSatisfiedBy(Contract contract) {
        return contract.isSatisfiedBy(this);
    }

    @Override
    public boolean hasNullProperties() {
        return hasNullProperties;
    }

    /**
     * Renders the current Representation as a proxy to the provider interface
     * 
     * @param anInterface
     *            The interface we wish to proxy the resource as
     * @return A Guava Optional of the rendered class, this will be absent if
     *         the interface doesn't satisfy the interface
     */
    @Override
    public <T> T toClass(Class<T> anInterface) {
        if (InterfaceContract.newInterfaceContract(anInterface)
                             .isSatisfiedBy(this)) {
            return InterfaceRenderer.newInterfaceRenderer(anInterface)
                                    .render(this);
        }
        else {
            throw new RepresentationException("Unable to write representation to "
                    + anInterface.getName());
        }
    }

    @Override
    public String toString(String contentType) {
        return toString(contentType, Collections.<URI> emptySet());
    }

    @Override
    public String toString(String contentType, final Set<URI> flags) {
        StringWriter sw = new StringWriter();
        toString(contentType, flags, sw);
        return sw.toString();
    }

    @Override
    public void toString(String contentType, Writer writer) {
        toString(contentType, Collections.<URI> emptySet(), writer);
    }

    @Override
    public void toString(String contentType, Set<URI> flags, Writer writer) {
        validateNamespaces(this);
        RepresentationWriter<String> representationWriter = representationFactory.lookupRenderer(contentType);
        ImmutableSet.Builder<URI> uriBuilder = ImmutableSet.<URI> builder()
                                                           .addAll(representationFactory.getFlags());
        if (flags != null) uriBuilder.addAll(flags);
        representationWriter.write(this, uriBuilder.build(), writer);
    }

    @Override
    public int hashCode() {
        int h = namespaces.hashCode();
        h += links.hashCode();
        h += properties.hashCode();
        h += resources.hashCode();
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BaseRepresentation)) {
            return false;
        }
        BaseRepresentation that = (BaseRepresentation) obj;
        boolean e = this.namespaces.equals(that.namespaces);
        e &= this.links.equals(that.links);
        e &= this.properties.equals(that.properties);
        e &= this.resources.equals(that.resources);
        return e;
    }

    @Override
    public String toString() {
        Link href = getLinkByRel("self");
        if (href != null) {
            return "<Representation: " + href.getHref() + ">";
        }
        else {
            return "<Representation: @" + Integer.toHexString(hashCode()) + ">";
        }
    }

    // *** Parcelable */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int parcelFlags) {
        out.writeBundle(namespaces);
        out.writeTypedList(links);
        out.writeBundle(properties);
        out.writeBundle(resources);
        out.writeInt(hasNullProperties ? 1 : 0);
    }

}
