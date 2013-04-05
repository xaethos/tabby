package net.xaethos.tabby.halbuilder.impl.json;

//FIXME: Re-write this with Android shit instead of Jackson

import static net.xaethos.tabby.halbuilder.impl.api.Support.CURIE;
import static net.xaethos.tabby.halbuilder.impl.api.Support.EMBEDDED;
import static net.xaethos.tabby.halbuilder.impl.api.Support.HREF;
import static net.xaethos.tabby.halbuilder.impl.api.Support.HREFLANG;
import static net.xaethos.tabby.halbuilder.impl.api.Support.LINKS;
import static net.xaethos.tabby.halbuilder.impl.api.Support.NAME;
import static net.xaethos.tabby.halbuilder.impl.api.Support.PROFILE;
import static net.xaethos.tabby.halbuilder.impl.api.Support.TITLE;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.xaethos.tabby.halbuilder.api.ParcelableLink;
import net.xaethos.tabby.halbuilder.api.ParcelableRepresentationFactory;
import net.xaethos.tabby.halbuilder.impl.representations.ImmutableRepresentation;
import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import android.os.Bundle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.theoryinpractise.halbuilder.api.RepresentationException;
import com.theoryinpractise.halbuilder.api.RepresentationReader;

public class JsonRepresentationReader implements RepresentationReader
{
    private ParcelableRepresentationFactory representationFactory;

    public JsonRepresentationReader(ParcelableRepresentationFactory representationFactory) {
        this.representationFactory = representationFactory;
    }

    public ParcelableReadableRepresentation read(Reader reader) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.readValue(reader, JsonNode.class);

            return readResource(rootNode);
        }
        catch (Exception e) {
            throw new RepresentationException(e);
        }

    }

    private ImmutableRepresentation readResource(JsonNode rootNode) {
        Bundle namespaces = new Bundle();
        Bundle properties = new Bundle();
        Bundle resources = new Bundle();
        List<ParcelableLink> links = Lists.newArrayList();
        boolean hasNullProperties;

        readNamespaces(namespaces, rootNode);
        readLinks(links, rootNode);
        hasNullProperties = readProperties(properties, rootNode);
        readResources(resources, rootNode);

        return new ImmutableRepresentation(representationFactory,
                                           namespaces,
                                           links,
                                           properties,
                                           resources,
                                           hasNullProperties);
    }

    private void readNamespaces(Bundle namespaces, JsonNode rootNode) {
        if (rootNode.has(LINKS)) {
            JsonNode linksNode = rootNode.get(LINKS);
            if (linksNode.has(CURIE)) {
                JsonNode curieNode = linksNode.get(CURIE);

                if (curieNode.isArray()) {
                    Iterator<JsonNode> values = curieNode.elements();
                    while (values.hasNext()) {
                        JsonNode valueNode = values.next();
                        namespaces.putString(valueNode.get(NAME).asText(), valueNode.get(HREF).asText());
                    }
                }
                else {
                    namespaces.putString(curieNode.get(NAME).asText(), curieNode.get(HREF).asText());
                }
            }
        }
    }

    private void readLinks(List<ParcelableLink> links, JsonNode rootNode) {
        if (rootNode.has(LINKS)) {
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.get(LINKS).fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> keyNode = fields.next();
                if (!CURIE.equals((keyNode.getKey()))) {
                    if (keyNode.getValue().isArray()) {
                        Iterator<JsonNode> values = keyNode.getValue().elements();
                        while (values.hasNext()) {
                            JsonNode valueNode = values.next();
                            withJsonLink(links, keyNode, valueNode);
                        }
                    }
                    else {
                        withJsonLink(links, keyNode, keyNode.getValue());
                    }
                }
            }
        }
    }

    private void withJsonLink(List<ParcelableLink> links, Map.Entry<String, JsonNode> keyNode, JsonNode valueNode) {
        String rel = keyNode.getKey();
        String href = valueNode.get(HREF).asText();
        String name = optionalNodeValueAsText(valueNode, NAME);
        String title = optionalNodeValueAsText(valueNode, TITLE);
        String hreflang = optionalNodeValueAsText(valueNode, HREFLANG);
        String profile = optionalNodeValueAsText(valueNode, PROFILE);

        links.add(new ParcelableLink(representationFactory, rel, href, name, title, hreflang, profile));
    }

    String optionalNodeValueAsText(JsonNode node, String key) {
        JsonNode value = node.get(key);
        return value != null ? value.asText() : null;
    }

    private boolean readProperties(Bundle properties, JsonNode rootNode) {
        boolean hasNullProperties = false;

        Iterator<String> fieldNames = rootNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (!fieldName.startsWith("_")) {
                JsonNode field = rootNode.get(fieldName);
                if (field.isNull()) {
                    hasNullProperties = true;
                    properties.putString(fieldName, null);
                }
                else {
                    properties.putString(fieldName, field.asText());
                }
            }
        }

        return hasNullProperties;
    }

    private void readResources(Bundle resources, JsonNode rootNode) {
        if (rootNode.has(EMBEDDED)) {
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.get(EMBEDDED).fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> keyNode = fields.next();
                ParcelableReadableRepresentation[] subresources;
                if (keyNode.getValue().isArray()) {
                    Iterator<JsonNode> values = keyNode.getValue().elements();
                    List<ParcelableReadableRepresentation> subresourceList = new ArrayList<ParcelableReadableRepresentation>();

                    while (values.hasNext()) {
                        JsonNode valueNode = values.next();
                        subresourceList.add(readResource(valueNode));
                    }

                    subresources = new ParcelableReadableRepresentation[subresourceList.size()];
                    subresourceList.toArray(subresources);
                }
                else {
                    subresources = new ParcelableReadableRepresentation[1];
                    subresources[0] = readResource(keyNode.getValue());
                }

                resources.putParcelableArray(keyNode.getKey(), subresources);
            }
        }
    }
}
