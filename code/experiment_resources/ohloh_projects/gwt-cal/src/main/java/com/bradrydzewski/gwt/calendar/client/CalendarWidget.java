/*
 * This file is part of gwt-cal
 * Copyright (C) 2009  Scottsdale Software LLC
 * 
 * gwt-cal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/
 */

package com.bradrydzewski.gwt.calendar.client;

import java.util.ArrayList;
import java.util.Date;

import com.bradrydzewski.gwt.calendar.client.event.CreateEvent;
import com.bradrydzewski.gwt.calendar.client.event.CreateHandler;
import com.bradrydzewski.gwt.calendar.client.event.DateRequestEvent;
import com.bradrydzewski.gwt.calendar.client.event.DateRequestHandler;
import com.bradrydzewski.gwt.calendar.client.event.DeleteEvent;
import com.bradrydzewski.gwt.calendar.client.event.DeleteHandler;
import com.bradrydzewski.gwt.calendar.client.event.HasDateRequestHandlers;
import com.bradrydzewski.gwt.calendar.client.event.HasDeleteHandlers;
import com.bradrydzewski.gwt.calendar.client.event.HasMouseOverHandlers;
import com.bradrydzewski.gwt.calendar.client.event.HasTimeBlockClickHandlers;
import com.bradrydzewski.gwt.calendar.client.event.HasUpdateHandlers;
import com.bradrydzewski.gwt.calendar.client.event.MouseOverEvent;
import com.bradrydzewski.gwt.calendar.client.event.MouseOverHandler;
import com.bradrydzewski.gwt.calendar.client.event.TimeBlockClickEvent;
import com.bradrydzewski.gwt.calendar.client.event.TimeBlockClickHandler;
import com.bradrydzewski.gwt.calendar.client.event.UpdateEvent;
import com.bradrydzewski.gwt.calendar.client.event.UpdateHandler;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

/**
 * <code>CalendarWidget</code> is an {@link com.bradrydzewski.gwt.calendar.client.InteractiveWidget}
 * that maintains a calendar model (a set of {@link Appointment} objects)
 * managed through an {@link AppointmentManager}.
 * <p/>
 * TODO: Need Calendar "View" - CHECK TODO: Need CalendarSettings TODO: Need
 * LayoutStrategy - CHECK TODO: Need DragDropStrategy TODO: Need ResizeStrategy
 * ??? or is this same as DragDrop TODO: Add AppointmentBuilder ??? downside is
 * that if the Appointment object is updated, need to refersh widget
 *
 * @author Brad Rydzewski
 * @see com.bradrydzewski.gwt.calendar.client.InteractiveWidget
 */
public class CalendarWidget extends InteractiveWidget implements
   HasSelectionHandlers<Appointment>, HasDeleteHandlers<Appointment>,
   HasOpenHandlers<Appointment>, HasTimeBlockClickHandlers<Date>,
   HasUpdateHandlers<Appointment>, HasDateRequestHandlers<Date>,
   HasMouseOverHandlers<Appointment>,
   HasLayout, HasAppointments {

   /**
    * Set to <code>true</code> if the calendar layout is suspended and cannot
    * be triggered.
    */
   private boolean layoutSuspended = false;

   /**
    * Set to <code>true</code> if the calendar is pending the layout of its
    * appointments.
    */
   private boolean layoutPending = false;

   /**
    * The date currently displayed by the calendar. Set to current system date
    * by default.
    */
   private Date date;

   /**
    * Calendar settings, set to default.
    */
   private CalendarSettings settings = CalendarSettings.DEFAULT_SETTINGS;

   /**
    * The component to manage the set of appointments displayed by this
    * <code>CalendarWidget</code>.
    */
   private AppointmentManager appointmentManager = null;

   private CalendarView view = null;

   /**
    * Creates a <code>CalendarWidget</code> with an empty set of appointments
    * and the current system date as the date currently displayed by the
    * calendar.
    */
   public CalendarWidget() {
      this(new Date());
   }

   public CalendarWidget(Date date) {
      super();
      appointmentManager = new AppointmentManager();
      this.date = date;
      DateUtils.resetTime(this.date);
   }

   /**
    * Changes the current view of this calendar widget to the specified
    * <code>view</code>. By setting this widget's current view the whole widget
    * panel is cleared.
    *
    * @param view The {@link CalendarView} implementation to render this
    *             widget's underlying calendar
    */
   public final void setView(CalendarView view) {
      this.getRootPanel().clear();
      this.view = view;
      this.view.attach(this);
      this.setStyleName(this.view.getStyleName());
      this.refresh();
   }
   
   public final CalendarView getView() {
	   return view;
   }

   public Date getDate() {
      return (Date) date.clone();
   }

   public void setDate(Date date, int days) {
      Date dateCopy = (Date)date.clone();
      DateUtils.resetTime(dateCopy);
      this.date = dateCopy;
      view.setDisplayedDays(days);
      refresh();
   }

   public void setDate(Date date) {
      setDate(date, getDays());
   }

   /**
    * Moves this calendar widget current <code>date</code> as many days as
    * specified by the <code>numOfDays</code> parameter.
    *
    * @param numOfDays The number of days to change the calendar date forward
    *                  (positive number) or backwards.
    */
   @SuppressWarnings("deprecation")
   public void addDaysToDate(int numOfDays) {
      this.date.setDate(this.date.getDate() + numOfDays);
   }

   public int getDays() {
      return view == null ? 3 : view.getDisplayedDays();
   }

   public void setDays(int days) {
      view.setDisplayedDays(days);
      refresh();
   }

   /**
    * Returns the collection of appointments in the underlying in-memory model
    * of this calendar widget. <strong>Warning</strong>: the returned
    * collection of apointments can be modified by client code, possibly
    * breaking the system model invariants.
    *
    * @return The set of appointments to be displayed by this calendar widget
    * @see AppointmentManager#getAppointments()
    */
   public ArrayList<Appointment> getAppointments() {
      return appointmentManager.getAppointments();
   }

   /**
    * Removes an appointment from the calendar.
    *
    * @param appointment the item to be removed.
    */
   public void removeAppointment(Appointment appointment) {
      removeAppointment(appointment, false);
   }

   /**
    * Removes the currently selected appointment from the model, if such
    * appointment is set.
    */
   public void removeCurrentlySelectedAppointment() {
      appointmentManager.removeCurrentlySelectedAppointment();
   }

   /**
    * Removes an appointment from the calendar.
    *
    * @param appointment the item to be removed.
    * @param fireEvents  <code>true</code> to allow deletion events to be
    *                    fired
    */
   public void removeAppointment(Appointment appointment, boolean fireEvents) {
      boolean commitChange = true;

      if (fireEvents) {
         commitChange = DeleteEvent.fire(this, getSelectedAppointment());
      }

      if (commitChange) {
         appointmentManager.removeAppointment(appointment);
         refresh();
      }
   }

   /**
    * Resets the &quot;currently selected&quot; appointment of this calendar.
    *
    * @see com.bradrydzewski.gwt.calendar.client.AppointmentManager
    */
   public void resetSelectedAppointment() {
      appointmentManager.resetSelectedAppointment();
   }

   /**
    * Adds an appointment to the calendar.
    *
    * @param appointment item to be added
    */
   public void addAppointment(Appointment appointment) {
      if ( appointment == null )
      {
         throw new NullPointerException("Added appointment cannot be null.");
      }
      appointmentManager.addAppointment(appointment);
      refresh();
   }

   /**
    * Adds each appointment in the list to the calendar.
    *
    * @param appointments items to be added.
    */
   public void addAppointments(ArrayList<Appointment> appointments) {
      appointmentManager.addAppointments(appointments);
      refresh();
   }

   /**
    * Clears all appointment items.
    */
   public void clearAppointments() {
      appointmentManager.clearAppointments();
      refresh();
   }

   /**
    * Sets the currently selected item.
    *
    * @param appointment the item to be selected, or <code>null</code> to
    *                    de-select all items.
    */
   public void setSelectedAppointment(Appointment appointment) {
      setSelectedAppointment(appointment, true);
   }

   public void setSelectedAppointment(Appointment appointment,
      boolean fireEvents) {
      appointmentManager.setSelectedAppointment(appointment);
      if (fireEvents) {
         fireSelectionEvent(appointment);
      }
   }

   /**
    * Indicates whether there is a &quot;currently selected&quot; appointment
    * at the moment.
    *
    * @return <code>true</code> if there is an appointment currently selected,
    *         <code>false</code> if it is <code>null</code>.
    * @see com.bradrydzewski.gwt.calendar.client.AppointmentManager#hasAppointmentSelected()
    */
   public boolean hasAppointmentSelected() {
      return appointmentManager.hasAppointmentSelected();
   }

   /**
    * Gets the currently selected item.
    *
    * @return the selected item.
    */
   public Appointment getSelectedAppointment() {
      return appointmentManager.getSelectedAppointment();
   }

   /**
    * Tells whether the passed <code>appointment</code> is the currently
    * selected appointment.
    *
    * @param appointment The appointment to test to be the currently selected
    * @return <code>true</code> if there is a currently selected appointment
    *         and happens to be equal to the passed <code>appointment</code>
    * @see com.bradrydzewski.gwt.calendar.client.AppointmentManager#isTheSelectedAppointment(Appointment)
    */
   public boolean isTheSelectedAppointment(Appointment appointment) {
      return appointmentManager.isTheSelectedAppointment(appointment);
   }

   /**
    * Performs all layout calculations for the list of appointments and resizes
    * the Calendar View appropriately.
    */
   protected void refresh() {
      if (layoutSuspended) {
         layoutPending = true;
         return;
      }
      
      appointmentManager.resetHoveredAppointment();
      appointmentManager.sortAppointments();
      
      doLayout();
      doSizing();
   }

   public void doLayout() {
      view.doLayout();
   }

   public void doSizing() {
      view.doSizing();
   }

   public void onLoad() {
	   Scheduler.get().scheduleDeferred(new ScheduledCommand() {
		public void execute() {
			doSizing();
			}
	   });
   }

   /**
    * Suspends the calendar from performing a layout. This can be useful when
    * adding a large number of appointments at a time, since a layout is
    * performed each time an appointment is added.
    */
   public void suspendLayout() {
      layoutSuspended = true;
   }

   /**
    * Allows the calendar to perform a layout, sizing the component and placing
    * all appointments. If a layout is pending it will get executed when this
    * method is called.
    */
   public void resumeLayout() {
      layoutSuspended = false;

      if (layoutPending) {
         refresh();
      }
   }

   public CalendarSettings getSettings() {
      return this.settings;
   }

   public void setSettings(CalendarSettings settings) {
      this.settings = settings;
   }

   public void scrollToHour(int hour) {
      view.scrollToHour(hour);
   }

   public boolean selectPreviousAppointment() {

      boolean selected = appointmentManager.selectPreviousAppointment();
      if (selected) {
         fireSelectionEvent(getSelectedAppointment());
      }

      return selected;
   }

   public boolean selectNextAppointment() {
      boolean selected = appointmentManager.selectNextAppointment();
      if (selected)
         fireSelectionEvent(getSelectedAppointment());
      return selected;
   }

   @Override
   public void onDeleteKeyPressed() {
      view.onDeleteKeyPressed();
   }

   @Override
   public void onDoubleClick(Element element, Event event) {
      view.onDoubleClick(element, event);
   }

   @Override
   public void onDownArrowKeyPressed() {
      view.onDownArrowKeyPressed();
   }

   @Override
   public void onLeftArrowKeyPressed() {
      view.onLeftArrowKeyPressed();
   }

   @Override
   public void onMouseDown(Element element, Event event) {
      view.onSingleClick(element, event);
   }
   
   public void onMouseOver(Element element, Event event) {
	   view.onMouseOver(element, event);
   }

   @Override
   public void onRightArrowKeyPressed() {
      view.onRightArrowKeyPressed();
   }

   @Override
   public void onUpArrowKeyPressed() {
      view.onUpArrowKeyPressed();
   }

   public void fireOpenEvent(Appointment appointment) {
      OpenEvent.fire(this, appointment);
   }

   public void fireDeleteEvent(Appointment appointment) {

      //fire the event to notify the client
      boolean allow = DeleteEvent.fire(this, appointment);

      if (allow) {
         appointmentManager.removeAppointment(appointment);
         refresh();
      }
   }

   public void fireSelectionEvent(Appointment appointment) {
      view.onAppointmentSelected(appointment);
      SelectionEvent.fire(this, appointment);
   }
   
   public void fireMouseOverEvent(
		   Appointment appointment, Element element) {
	   //we need to make sure we aren't re-firing the event
	   // for the same appointment. This is a bit problematic,
	   // because the mouse over event will fire for an appointment's
	   // child elements (title label, footer, body, for example)
	   // and will cause this method to be called with a null
	   // appointment. this is a temp workaround, but basically
	   // an appointment cannot be hovered twice in a row
	   if(appointment!=null && !appointment.equals(
			   appointmentManager.getHoveredAppointment())) {
		   appointmentManager.setHoveredAppointment(appointment);
		   MouseOverEvent.fire(this, appointment, element);
	   }
   }

   public void fireTimeBlockClickEvent(Date date) {
      TimeBlockClickEvent.fire(this, date);
   }

   public void fireCreateEvent(Appointment appointment) {
	   boolean allow = CreateEvent.fire(this, appointment);
	   if (!allow) {
		   appointmentManager.rollback();
		   refresh();
	   }
	}

   public void fireDateRequestEvent(Date date) {
      DateRequestEvent.fire(this, date);
   }

   public void fireDateRequestEvent(Date date, Element clicked) {
	      DateRequestEvent.fire(this, date, clicked);
   }

   public void fireUpdateEvent(Appointment appointment) {
      //refresh the appointment
      refresh();
      //fire the event to notify the client
      boolean allow = UpdateEvent.fire(this, appointment);

      if (!allow) {
         appointmentManager.rollback();
         refresh();
      }
   }

   public HandlerRegistration addSelectionHandler(
      SelectionHandler<Appointment> handler) {
      return addHandler(handler, SelectionEvent.getType());
   }

   public HandlerRegistration addDeleteHandler(
      DeleteHandler<Appointment> handler) {
      return addHandler(handler, DeleteEvent.getType());
   }
   
   public HandlerRegistration addMouseOverHandler(
		      MouseOverHandler<Appointment> handler) {
	   return addHandler(handler, MouseOverEvent.getType());
   }

   public HandlerRegistration addTimeBlockClickHandler(
      TimeBlockClickHandler<Date> handler) {
      return addHandler(handler, TimeBlockClickEvent.getType());
   }

	public HandlerRegistration addUpdateHandler(UpdateHandler<Appointment> handler) {
		return addHandler(handler, UpdateEvent.getType());
	}

   public HandlerRegistration addCreateHandler(
		      CreateHandler<Appointment> handler) {
		      return addHandler(handler, CreateEvent.getType());
   }

   public HandlerRegistration addOpenHandler(
      OpenHandler<Appointment> handler) {
      return addHandler(handler, OpenEvent.getType());
   }

   public HandlerRegistration addDateRequestHandler(
      DateRequestHandler<Date> handler) {
      return addHandler(handler, DateRequestEvent.getType());
   }

   public void addToRootPanel(Widget widget) {
      getRootPanel().add(widget);
   }

   public void setRollbackAppointment(Appointment appt) {
      appointmentManager.setRollbackAppointment(appt);
   }

   public void setCommittedAppointment(Appointment appt) {
      appointmentManager.setCommittedAppointment(appt);
   }
}