package com.bradrydzewski.gwt.calendar.client.util;

import com.bradrydzewski.gwt.calendar.client.util.impl.FormattingImpl;
import com.google.gwt.core.client.GWT;

/**
 * This utility provides access to data to help format widgets
 * correctly across browsers.
 * @author Brad Rydzewski
 */
public class FormattingUtil {

    /**
     * Implementation of formatting class. Holds browser-specific
     * values, loaded by GWT deferred binding.
     */
    private static FormattingImpl impl = GWT.create(FormattingImpl.class);
    
    /**
     * All CSS2 compliant browsers count the border height in the
     * overall height of an Element. This method returns an offset
     * value that should be added to the height or width of an item
     * before setting its size. This will ensure consistent sizing
     * across compliant and non-compliant browsers.
     * @return
     */
    public static int getBorderOffset() {
        return impl.getBorderOffset();
    }
}
