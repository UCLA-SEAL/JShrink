package com.allen_sauer.gwt.dnd.client.drop;

import java.util.Date;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.bradrydzewski.gwt.calendar.client.dayview.AppointmentWidget;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

public class DayViewDropController extends AbsolutePositionDropController {

	private int gridX;

	private int gridY;
	
	int intervalsPerHour;
	int snapSize;
	int columns;
	int rows;
	Date date;
	private int maxProxyHeight = -1;
	
	public void setColumns(int columns) {
		this.columns = columns;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setSnapSize(int snapSize) {
		this.snapSize = snapSize;
	}
	
	public void setIntervalsPerHour(int intervalsPerHour) {
		this.intervalsPerHour = intervalsPerHour;
		this.rows = intervalsPerHour*24;
	}

	public void setMaxProxyHeight(int maxProxyHeight) {
		this.maxProxyHeight = maxProxyHeight;
	}
	
	public DayViewDropController(AbsolutePanel dropTarget) {
		super(dropTarget);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onDrop(DragContext context) {

		super.onDrop(context);
		
		//get the top and left position and the widget
		int top =draggableList.get(0).desiredY;
		int left=draggableList.get(0).desiredX;
		Widget widget=context.draggable;

		//set the 'snapped' top and left position of the widget
		left = Math.max(0, Math.min(left, dropTarget.getOffsetWidth() - widget.getOffsetWidth()));
		top = Math.max(0, Math.min(top, dropTarget.getOffsetHeight() - widget.getOffsetHeight()));
		left = Math.round((float) left / gridX) * gridX;
		top = Math.round((float) top / gridY) * gridY;
		
		//figure out which row the appointment was dragged to
		int intervalStart = (int) Math.floor(top / gridY);
		int intervalSpan = Math.round(widget.getOffsetHeight() / snapSize);
		
		//figure out which day (column) the appointment was dragged to
		int day = (int) Math.floor(left / gridX);
		day = Math.max(0, day);
		day = Math.min(day, columns-1);

		//get the appointment, create the start & end date
		Appointment appt = ((AppointmentWidget)widget).getAppointment();
		Date start = (Date)date.clone();
		Date end = (Date)date.clone();
		
		start.setDate(start.getDate()+day);
		end.setDate(end.getDate()+day);
		
        start.setHours(0);
        start.setMinutes(0);
        start.setSeconds(0);
        start.setMinutes((intervalStart)*(60/intervalsPerHour));
        end.setHours(0);
        end.setMinutes(0);
        end.setSeconds(0);
        end.setMinutes((intervalStart + intervalSpan)*(60/intervalsPerHour));
		

		appt.setStart(start);
		appt.setEnd(end);
		
		
		
	}

	
//	@Override
//	public void drop(Widget widget, int left, int top) {
//			
//	}
	
//	@Override
//	public void drop(Widget widget, int left, int top) {
//		left = Math.max(0, Math.min(left, dropTarget.getOffsetWidth() - widget.getOffsetWidth()));
//		top = Math.max(0, Math.min(top, dropTarget.getOffsetHeight() - widget.getOffsetHeight()));
//		left = Math.round((float) left / gridX) * gridX;
//		top = Math.round((float) top / gridY) * gridY;
//		
//		System.out.println("on drop");
//		
//		
//		int intervalStart = (int) Math.floor(top / rows);
//		int intervalSpan = 2;
//		int day = (int) Math.floor(left / columns);
//		day = Math.min(0, day);
//		day = Math.min(day, columns);
//		day = day-1; //convert to a 0-based day index
//		
//		Appointment appt = ((AppointmentWidget)widget).getAppointment();
//		Date start = (Date)date.clone();
//		Date end = (Date)date.clone();
//		
//		start.setDate(start.getDate()+day);
//		end.setDate(end.getDate()+day);
//		
//        start.setHours(0);
//        start.setMinutes((intervalStart)*(60/intervalsPerHour));
//        end.setHours(0);
//        end.setMinutes((intervalStart + intervalSpan)*(60/intervalsPerHour));
//		
//        System.out.println("new start: "+start);
//        
//		appt.setStart(start);
//		appt.setEnd(end);
//		
//		
//	}
	
	@Override
	public void onMove(DragContext context) {
		super.onMove(context);

		gridX = (int) Math.floor(dropTarget.getOffsetWidth() / columns);
		gridY = (int) Math.floor(dropTarget.getOffsetHeight() / rows);

		
		for (Draggable draggable : draggableList) {
			draggable.desiredX = context.desiredDraggableX - dropTargetOffsetX + draggable.relativeX;
			draggable.desiredY = context.desiredDraggableY - dropTargetOffsetY + draggable.relativeY;
			
			draggable.desiredX = Math.max(0, Math.min(draggable.desiredX, dropTargetClientWidth - draggable.offsetWidth));
			draggable.desiredY = Math.max(0, Math.min(draggable.desiredY, dropTargetClientHeight - draggable.offsetHeight));
			draggable.desiredX = (int)Math.floor((double) draggable.desiredX / gridX) * gridX;
			draggable.desiredY = (int)Math.round((double) draggable.desiredY / gridY) * gridY;
			
			dropTarget.add(draggable.positioner, draggable.desiredX, draggable.desiredY);
		}
	}

	@Override
	public void onEnter(DragContext context) {
		super.onEnter(context);
		
		for (Draggable draggable : draggableList) {
			int width = draggable.positioner.getOffsetWidth();
			int height = draggable.positioner.getOffsetHeight();
			if (maxProxyHeight > 0 && height > maxProxyHeight) {
				height = maxProxyHeight - 5;
			}
			
			draggable.positioner.setPixelSize(width, height);
		}
	}
}
