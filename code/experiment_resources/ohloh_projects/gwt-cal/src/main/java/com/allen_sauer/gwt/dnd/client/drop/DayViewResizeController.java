package com.allen_sauer.gwt.dnd.client.drop;

import java.util.Date;

import com.allen_sauer.gwt.dnd.client.AbstractDragController;
import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.bradrydzewski.gwt.calendar.client.dayview.AppointmentWidget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

public class DayViewResizeController extends AbstractDragController {

	public DayViewResizeController(AbsolutePanel boundaryPanel) {
		super(boundaryPanel);
	}

	int snapSize;
	int intervalsPerHour;

	public void setSnapSize(int snapSize) {
		this.snapSize = snapSize;
	}
	
	public void setIntervalsPerHour(int intervalsPerHour) {
		this.intervalsPerHour = intervalsPerHour;
	}

	public void dragMove() {

		Widget appointment = context.draggable.getParent();

		//calculates difference between elements position on screen
		// and how many pixels the user is trying to drag it
		int delta = context.draggable.getAbsoluteTop()
				- context.desiredDraggableY;
		
		//get the height of the widget
		int contentHeight = appointment.getOffsetHeight();
		
		//make sure the height of the widget is not < the minimum size
		int newHeight = Math.max(contentHeight - delta, snapSize);

		//get the 'snapped' height. basically it gets the rounded
		// intervals spanned, then multiples it by the snapSize
		int snapHeight = Math.round(
				(float) newHeight / snapSize) * snapSize;

		appointment.setHeight(snapHeight + "px");
	}
	

	@SuppressWarnings("deprecation")
	public void dragEnd() {
		AppointmentWidget apptWidget = (AppointmentWidget)context.draggable.getParent();
		int apptHeight = apptWidget.getOffsetHeight();
		Appointment appt = apptWidget.getAppointment();
		
		
        //get the start date
        Date end = (Date)appt.getStart().clone();
        
        //get the "top" location of the appointment widget
        float topFloat = DOM.getIntStyleAttribute(apptWidget.getElement(), "top");
        
        //get the grid span
        int intervalStart = Math.round(topFloat / snapSize);
        int intervalSpan = Math.round(apptHeight / snapSize);
        
        //set the end based on the new dragged value
        end.setHours(0);
        end.setMinutes((intervalStart + intervalSpan)*(60/intervalsPerHour));
        
        //update the end
        appt.setEnd(end);
		
		super.dragEnd();
	}

}
