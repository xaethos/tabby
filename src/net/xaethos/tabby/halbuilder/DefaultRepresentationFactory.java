package net.xaethos.tabby.halbuilder;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.xaethos.tabby.halbuilder.api.ParcelableLink;
import net.xaethos.tabby.halbuilder.api.ParcelableRepresentationFactory;
import net.xaethos.tabby.halbuilder.impl.json.JsonRepresentationReader;
import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationException;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.api.RepresentationWriter;

public class DefaultRepresentationFactory extends ParcelableRepresentationFactory {

    private TreeMap<String, String> namespaces = Maps.newTreeMap(Ordering.usingToString());
    private List<ParcelableLink> links = Lists.newArrayList();
    private Set<URI> flags = Sets.newHashSet();

    public DefaultRepresentationFactory() {
        super();
    }

    public DefaultRepresentationFactory(Parcel in) {
        super();

        // Must parallel order from writeToParcel

        // TreeMap<String, String> namespaces
        Bundle ns = in.readBundle();
        for (String key : ns.keySet()) {
            namespaces.put(key, ns.getString(key));
        }

        // private List<ParcelableLink> links
        in.readTypedList(links, ParcelableLink.CREATOR);

        // Set<URI> flags # URI implements Serializable
        for (URI flag : (URI[]) in.readArray(null)) {
            flags.add(flag);
        }
    }

    @Override
    public DefaultRepresentationFactory withNamespace(String namespace, String href) {
        if (namespaces.containsKey(namespace)) {
            throw new RepresentationException(format("Duplicate namespace '%s' found for representation factory", namespace));
        }
        namespaces.put(namespace, href);
        return this;
    }

    @Override
    public DefaultRepresentationFactory withLink(String rel, String href) {
        links.add(new ParcelableLink(this, rel, href));
        return this;
    }

    @Override
    public RepresentationFactory withFlag(URI flag) {
        flags.add(flag);
        return this;
    }

    @Override
    public Representation newRepresentation(URI uri) {
        return newRepresentation(uri.toString());
    }

    @Override
    public Representation newRepresentation() {
        return newRepresentation((String) null);
    }

    @Override
    public Representation newRepresentation(String href) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParcelableReadableRepresentation readRepresentation(Reader reader) {
        try {
            Reader bufferedReader = new BufferedReader(reader);
            bufferedReader.mark(1);
            char firstChar = (char) bufferedReader.read();
            bufferedReader.reset();

            switch (firstChar) {
            case '{':
                // All is good... for now
                break;
            case '<':
                throw new IllegalArgumentException("Unhandled ContentType: " + HAL_XML);
            default:
                throw new RepresentationException("unrecognized initial character in stream: " + firstChar);
            }
            return new JsonRepresentationReader(this).read(bufferedReader);
        }
        catch (Exception e) {
            throw new RepresentationException(e);
        }
    }

    @Override
    public RepresentationWriter<String> lookupRenderer(String contentType) {
    	throw new UnsupportedOperationException();
    }

    public Set<URI> getFlags() {
        return ImmutableSet.copyOf(flags);
    }

    // *** Parcelable ***

    public static final Parcelable.Creator<DefaultRepresentationFactory> CREATOR;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int parcelFlags) {
        // TreeMap<String, String> namespaces
        Bundle ns = new Bundle(namespaces.size());
        for (Entry<String, String> entry : namespaces.entrySet()) {
            ns.putString(entry.getKey(), entry.getValue());
        }
        out.writeBundle(ns);

        // private List<ParcelableLink> links
        out.writeTypedList(links);

        // Set<URI> flags # URI implements Serializable
        URI[] flagArray = new URI[flags.size()];
        out.writeArray(flags.toArray(flagArray));
    }

    static {
        CREATOR = new Parcelable.Creator<DefaultRepresentationFactory>() {
            public DefaultRepresentationFactory createFromParcel(Parcel in) {
                return new DefaultRepresentationFactory(in);
            }

            public DefaultRepresentationFactory[] newArray(int size) {
                return new DefaultRepresentationFactory[size];
            }
        };
    }

}
