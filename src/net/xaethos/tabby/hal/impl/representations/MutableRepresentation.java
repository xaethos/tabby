package net.xaethos.tabby.hal.impl.representations;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;

import net.xaethos.tabby.hal.impl.api.Support;

import com.theoryinpractise.halbuilder.api.Link;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import com.theoryinpractise.halbuilder.api.Representable;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationException;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;

public class MutableRepresentation extends BaseRepresentation implements Representation {

    public MutableRepresentation(RepresentationFactory representationFactory, String href) {
        super(representationFactory);
        if (href != null) {
            this.links.add(new Link(representationFactory, "self", href));
        }
    }

    public MutableRepresentation(RepresentationFactory representationFactory) {
        super(representationFactory);
    }

    /**
     * Add a link to this resource
     *
     * @param rel
     * @param href The target href for the link, relative to the href of this resource.
     * @return
     */
    public MutableRepresentation withLink(String rel, String href) {
        withLink(rel, href, null, null, null, null);
        return this;
    }

    /**
     * Add a link to this resource
     *
     * @param rel
     * @param href The target href for the link, relative to the href of this resource.
     */
    public MutableRepresentation withLink(String rel, String href, String name, String title, String hreflang, String profile) {
        Support.checkRelType(rel);
        links.add(new Link(representationFactory, rel, href, name, title, hreflang, profile));
        return this;
    }

    /**
     * Add a link to this resource
     *
     * @param rel
     * @param uri The target URI for the link, possibly relative to the href of
     *            this resource.
     * @return
     */
    public MutableRepresentation withLink(String rel, URI uri) {
        return withLink(rel, uri.toASCIIString());
    }

    public Representation withProperty(String name, Object value) {
        if (properties.containsKey(name)) {
            throw new RepresentationException(format("Duplicate property '%s' found for resource", name));
        }
        if (null == value) {
            this.hasNullProperties = true;
        }
        properties.put(name, value);
        return this;
    }

    public Representation withBean(Object value) {
        throw new UnsupportedOperationException();
    }

    public Representation withFields(Object value) {
        try {
            for (Field field : value.getClass().getDeclaredFields()) {
                if (Modifier.isPublic(field.getModifiers())) {
                    withProperty(field.getName(), field.get(value));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return this;

    }

    public Representation withRepresentable(Representable representable) {
        representable.representResource(this);
        return this;
    }

    public Representation withFieldBasedRepresentation(String rel, String href, Object o) {
        return withRepresentation(rel, representationFactory.newRepresentation(href).withFields(o));
    }

    public Representation withBeanBasedRepresentation(String rel, String href, Object o) {
        return withRepresentation(rel, representationFactory.newRepresentation(href).withBean(o));
    }

    /**
     * Adds a new namespace
     *
     * @param namespace
     * @param href      The target href of the namespace being added. This may be relative to the resourceFactories baseref
     * @return
     */
    public Representation withNamespace(String namespace, String href) {
        if (namespaces.containsKey(namespace)) {
            throw new RepresentationException(format("Duplicate namespace '%s' found for resource", namespace));
        }
        namespaces.put(namespace, href);
        return this;
    }

    public MutableRepresentation withRepresentation(String rel, ReadableRepresentation resource) {
        Support.checkRelType(rel);
        resources.put(rel, resource);
        // Propagate null property flag to parent.
        if (resource.hasNullProperties()) {
            hasNullProperties = true;
        }
        return this;
    }

}
