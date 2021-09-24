package com.bradrydzewski.gwt.calendar.client.util;

import com.google.gwt.user.client.Window.Navigator;

/**
 * Provides a set of re-usable methods related to the client's
 * browser window.
 *
 * @author Brad Rydzewski
 */
public class WindowUtils {

    /**
     * Width in pixels of Client's scroll bar.
     */
    private static int scrollBarWidth;

    /**
     * Gets the width of the client's scroll bar.
     *
     * @param useCachedValue Indicates if cached value should be used, or refreshed.
     * @return Width, in pixels, of Client's scroll bar
     */
    public static int getScrollBarWidth(boolean useCachedValue) {
    	/*
    	 * OSX Lion doesn't show the scrollbars (ala iOS) which causes cosmetic problems: issue 143
    	 */
    	boolean isOSXLion = Navigator.getUserAgent().contains("Mac OS X 10.7") || Navigator.getUserAgent().contains("Mac OS X 10_7");
    	if (isOSXLion && Navigator.getUserAgent().contains("Safari")) {
    		// 0 seems to cause weird effects in Chrome
    		return 1;
    	}

        if (useCachedValue && scrollBarWidth > 0) {
            return scrollBarWidth;
        }

        /* sometimes getScrollBarWidth() temporarily returns a negative
        * number. So when this happens we will return "17"
        * which seems to be default on many systems...
        * but we won't save it as a cached value.
        */
        int tmpScrollBarWidth = getScrollBarWidth();
        if (tmpScrollBarWidth <= 0) {
            return 17;
        }

        scrollBarWidth = tmpScrollBarWidth;

        return scrollBarWidth;
    }

    /**
     * Calculates the width of the clients scroll bar, which can vary among operating systems,
     * browsers and themes. Based on code from: http://www.alexandre-gomes.com/?p=115
     *
     * @return The width of the browser scrollbar in pixels
     */
    private static native int getScrollBarWidth() /*-{
	
		var inner = document.createElement("p");
		inner.style.width = "100%";
		inner.style.height = "200px";
		
		var outer = document.createElement("div");
		outer.style.position = "absolute";
		outer.style.top = "0px";
		outer.style.left = "0px";
		outer.style.visibility = "hidden";
		outer.style.width = "200px";
		outer.style.height = "150px";
		outer.style.overflow = "hidden";
		outer.appendChild (inner);
		
		document.body.appendChild (outer);
		var w1 = inner.offsetWidth;
		outer.style.overflow = "scroll";
		var w2 = inner.offsetWidth;
		if (w1 == w2) w2 = outer.clientWidth;
		
		document.body.removeChild (outer);
		 
		return (w1 - w2);
	}-*/;
}
