package net.xaethos.tabby.view;

import java.util.Map.Entry;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.theoryinpractise.halbuilder.api.ReadableRepresentation;

public class SimpleRepresentationView extends LinearLayout
{

    public SimpleRepresentationView(Context context, ReadableRepresentation representation) {
        super(context);

        setOrientation(LinearLayout.VERTICAL);

        for (Entry<String, Object> property : representation.getProperties().entrySet()) {
            View child = inflate(context, android.R.layout.simple_list_item_2, this);
            ((TextView) child.findViewById(android.R.id.text1)).setText(property.getKey());
            ((TextView) child.findViewById(android.R.id.text2)).setText(property.getValue().toString());
        }
    }

}
