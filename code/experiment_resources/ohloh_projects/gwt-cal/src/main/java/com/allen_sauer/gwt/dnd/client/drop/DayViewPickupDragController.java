package com.allen_sauer.gwt.dnd.client.drop;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.util.DragClientBundle;
import com.allen_sauer.gwt.dnd.client.util.WidgetArea;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DayViewPickupDragController extends PickupDragController {

	private int maxProxyHeight = -1;
	
	public DayViewPickupDragController(AbsolutePanel boundaryPanel,
			boolean allowDroppingOnBoundaryPanel) {
		super(boundaryPanel, allowDroppingOnBoundaryPanel);
	}

	@Override
	public void dragMove() {
		try {
			super.dragMove();
		} catch (NullPointerException ex) {
		}
	}

	@Override
	protected Widget newDragProxy(DragContext context) {
		AbsolutePanel container = new AbsolutePanel();
		container.getElement().getStyle().setProperty("overflow", "visible");
		
		WidgetArea draggableArea = new WidgetArea(context.draggable, null);
		for (Widget widget : context.selectedWidgets) {
			WidgetArea widgetArea = new WidgetArea(widget, null);
			Widget proxy = new SimplePanel();
			int height = widget.getOffsetHeight();
			if (maxProxyHeight > 0 && height > maxProxyHeight) {
				height = maxProxyHeight - 5;
			}
			
			proxy.setPixelSize(widget.getOffsetWidth(), height);
			proxy.addStyleName(DragClientBundle.INSTANCE.css().proxy());
			container.add(proxy,
					widgetArea.getLeft() - draggableArea.getLeft(),
					widgetArea.getTop() - draggableArea.getTop());
		}

		return container;
	}

	public void setMaxProxyHeight(int maxProxyHeight) {
		this.maxProxyHeight = maxProxyHeight;
	}
}
