package net.xaethos.tabby.adapter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.xaethos.tabby.halbuilder.impl.representations.ParcelableReadableRepresentation;
import net.xaethos.tabby.view.SimpleRepresentationView;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;

import com.commonsware.cwac.merge.MergeAdapter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.theoryinpractise.halbuilder.api.Link;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;

public class SimpleRepresentationAdapter extends MergeAdapter
{

    private static final String NAME = "NAME";
    private static final String VALUE = "VALUE";

    private static final String[] FROM = { NAME, VALUE };
    private static final int[] TO = { android.R.id.text1, android.R.id.text2 };

    public SimpleRepresentationAdapter(Context context, ParcelableReadableRepresentation representation) {
        super();

        Set<String> rels = Sets.newLinkedHashSet();

        rels.addAll(representation.getResourceMap().keySet());

        for (Link link : representation.getLinks()) {
            rels.add(link.getRel());
        }

        addView(new SimpleRepresentationView(context, representation), false);

        for (String rel : rels) {
            List<ReadableRepresentation> resources = Lists.newArrayList(representation.getResourcesByRel(rel));
            if (resources != null && !resources.isEmpty()) {
                addAdapter(new ResourceAdapter(context, resources));
            }

            List<Link> links = representation.getLinksByRel(rel);
            if (links != null && !links.isEmpty()) {
                addAdapter(new LinkAdapter(context, links));
            }
        }

    }

    // ***** Inner classes

    class PropertyAdapter extends SimpleAdapter
    {

        public PropertyAdapter(Context context, List<Map<String, Object>> data) {
            super(context, data, android.R.layout.simple_list_item_2, FROM, TO);
        }

    }

    class ResourceAdapter extends ArrayAdapter<ReadableRepresentation>
    {

        public ResourceAdapter(Context context, List<ReadableRepresentation> resources) {
            super(context, android.R.layout.simple_list_item_1, resources);
        }

    }

    class LinkAdapter extends ArrayAdapter<Link>
    {

        public LinkAdapter(Context context, List<Link> links) {
            super(context, android.R.layout.simple_list_item_1, links);
        }

    }

}
