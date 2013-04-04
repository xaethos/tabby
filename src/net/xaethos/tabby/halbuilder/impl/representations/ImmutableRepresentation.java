package net.xaethos.tabby.halbuilder.impl.representations;

import java.util.List;

import net.xaethos.tabby.halbuilder.api.ParcelableLink;
import net.xaethos.tabby.halbuilder.api.ParcelableRepresentationFactory;
import android.os.Bundle;

import com.theoryinpractise.halbuilder.api.Link;

public class ImmutableRepresentation extends BaseRepresentation
{

    private final Link resourceLink;

    public ImmutableRepresentation(ParcelableRepresentationFactory representationFactory,
            Bundle namespaces,
            List<ParcelableLink> links,
            Bundle properties,
            Bundle resources,
            boolean hasNullProperties)
    {
        super(representationFactory);
        this.namespaces = namespaces;
        this.links = links;
        this.properties = properties;
        this.resources = resources;
        this.hasNullProperties = hasNullProperties;

        this.resourceLink = super.getResourceLink();
    }

    public Link getResourceLink() {
        return resourceLink;
    }

}
