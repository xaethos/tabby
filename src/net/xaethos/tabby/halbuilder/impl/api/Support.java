package net.xaethos.tabby.halbuilder.impl.api;

import android.text.TextUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

public class Support {

    public static final Splitter WHITESPACE_SPLITTER = Splitter.onPattern("\\s")
            .omitEmptyStrings();

    public static final String REL = "rel";
    public static final String SELF = "self";
    public static final String LINK = "link";
    public static final String HREF = "href";
    public static final String LINKS = "_links";
    public static final String EMBEDDED = "_embedded";
    public static final String NAME = "name";
    public static final String CURIE = "curie";
    public static final String TITLE = "title";
    public static final String HREFLANG = "hreflang";
    public static final String TEMPLATED = "templated";
    public static final String PROFILE = "profile";

    public static void checkRelType(String rel) {
        Preconditions.checkArgument(!TextUtils.isEmpty(rel), "Provided rel should not be null nor empty.");
        Preconditions.checkArgument(!rel.contains(" "), "Provided rel value should be a single rel type, as defined by http://tools.ietf.org/html/rfc5988");
    }

}
