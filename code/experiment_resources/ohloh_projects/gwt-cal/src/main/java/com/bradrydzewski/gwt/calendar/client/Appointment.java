/*
 * This file is part of gwt-cal
 * Copyright (C) 2010-2011  Scottsdale Software LLC
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents an event that Calendar Views display and manipulate through the
 * gwt-cal provided user interface elements.
 * <p>
 * The <code>Appointment</code> class provides a set of text-based properties to
 * describe it, including a <em>title, description, location, createdBy</em>,
 * etc. Additional to these, there is a set of properties that exist to provide
 * the gwt-cal components with information useful during the
 * <code>Appointment</code> rendering in the widget views (<code>allDay</code>,
 * <code>recurring</code>, etc.)
 * </p>
 * <p>
 * All <code>Appointment</code> properties are ultimately used by the gwt-cal
 * views and it is up to these components to decide how to render (if at all)
 * them as well as to provide appropriate runtime features to modify them.
 * </p>
 * 
 * @author Brad Rydzewski
 * @author Carlos D. Morales
 */
@SuppressWarnings("serial")
public class Appointment implements Comparable<Appointment>, Serializable {

	private String id;
	private String title;
	private String description;
	private Date start;
	private Date end;
	private String location;
	private String createdBy;
	private List<Attendee> attendees = new ArrayList<Attendee>();
	private boolean allDay = false;
	private AppointmentStyle style = AppointmentStyle.DEFAULT;
	private String customStyle;
	private boolean readOnly = false;

	/**
	 * <p>
	 * Creates an <code>Appointment</code> with the following attributes set to
	 * <code>null</code>
	 * 
	 * <ul>
	 * <li><code>title</code></li>
	 * <li><code>description</code></li>
	 * <li><code>start</code></li>
	 * <li><code>end</code></li>
	 * <li><code><code>location</code></li>
	 * <li><code>createdBy</code></li>
	 * </ul>
	 * the <code>attendees</code> collection empty and the <code>allDay</code>
	 * property <code>false</code>.
	 * </p>
	 * 
	 */
	public Appointment() {

	}

	/**
	 * Returns the unique identifier for this <code>Appointment</code>.
	 * The field is optional (and not used by gwt-cal) and therefore
	 * may be null.
	 * 
	 * @return A unique identifier for this Appointment (optional).
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the unique identifier of this <code>Appointment</code>.
	 * This identifier is optional.
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the configured start time-stamp of this <code>Appointment</code>.
	 * 
	 * @return A date object with the date and time this appointment starts on
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * Sets the start time-stamp of this <code>Appointment</code>.
	 * 
	 * @param start
	 *            A date object with the date and time this appointment starts
	 */
	public void setStart(Date start) {
		this.start = start;
	}

	/**
	 * Returns the configured end time-stamp of this <code>Appointment</code>.
	 * 
	 * @return A date object with the date and time this appointment ends on
	 */
	public Date getEnd() {
		return end;
	}

	/**
	 * Sets the end time-stamp of this <code>Appointment</code>.
	 * 
	 * @param end
	 *            A date object with the date and time this appointment starts
	 */
	public void setEnd(Date end) {
		this.end = end;
	}

	/**
	 * Returns the identifying title of this <code>Appointment</code>.
	 * 
	 * @return The title's short text
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the identifying title of this <code>Appointment</code>.
	 * 
	 * @param title
	 *            The title's short text
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Returns a description for this <code>Appointment</code>.
	 * 
	 * @return The <code>appointment</code>'s description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this <code>Appointment</code>.
	 * 
	 * @param description
	 *            The title's short text
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns a location of this <code>Appointment</code>.
	 * 
	 * @return The <code>appointment</code> location.
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Sets the location of this <code>Appointment</code>.
	 * 
	 * @param location
	 *            The <code>appointment</code> location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Returns a creator of this <code>Appointment</code>.
	 * 
	 * @return The <code>appointment</code> creator description.
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * Sets the creator of this <code>Appointment</code>.
	 * 
	 * @param createdBy
	 *            The <code>appointment</code> creator description.
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * Returns the collection of associated attendees.
	 * 
	 * @return The currently configured list of attendees
	 */
	public List<Attendee> getAttendees() {
		return attendees;
	}

	/**
	 * Sets the attendees associated to this <code>Appointment</code>.
	 * 
	 * @param attendees
	 *            The entities associated (<em>attending</em>) this
	 *            <code>Appointment</code>
	 */
	public void setAttendees(List<Attendee> attendees) {
		this.attendees = attendees;
	}

	/**
	 * Compares this <code>Appointment</code> with the specified
	 * <code>appointment</code> based first on the <code>start</code> dates of
	 * each appointment and then (if they happen to be the same), on the
	 * <code>end</code> dates.
	 * 
	 * @param appointment
	 *            The appointment to compare this one to
	 * @return a negative integer if <code>this</code> appointment
	 *         <em>is before</em> <code>appointment</code>, <code>zero</code> if
	 *         both appointments have the same <code>start</code>/
	 *         <code>end</code> dates, and a positive integer if
	 *         <code>this</code> appointment <em>is after</em>
	 *         <code>appointment</code>.
	 */
	public int compareTo(Appointment appointment) {
		int compare = this.getStart().compareTo(appointment.getStart());

		if (compare == 0) {
			compare = appointment.getEnd().compareTo(this.getEnd());
		}

		return compare;
	}

	/**
	 * Tells whether this <code>Appointment</code> spans more than a single day,
	 * based on its <code>start</code> and <code>end</code> properties.
	 * 
	 * @return <code>true</code> if the <code>start</code> and <code>end</code>
	 *         dates fall on different dates, <code>false</code> otherwise.
	 */
	public boolean isMultiDay() {
		if (getEnd() != null && getStart() != null) {
			return !DateUtils.areOnTheSameDay(getEnd(), getStart());
		}
		throw new IllegalStateException(
				"Calculating isMultiDay with no start/end dates set");
	}

	/**
	 * Returns the configured value of the <code>allDay</code> property, which
	 * indicates if this <code>Appointment</code> should be considered as
	 * spanning all day. It is left to the view rendering this
	 * <code>Appointment</code> to decide how to render an appointment based on
	 * this property value. For instance, the month view, will display the
	 * <code>Appointment</code> at the top of the days in a week.
	 * 
	 * @return The current value of the <code>allDay</code> property
	 */
	public boolean isAllDay() {
		return allDay;
	}

	/**
	 * Configures the the <code>allDay</code> property, which indicates if this
	 * <code>Appointment</code> should be considered as spanning all day. It is
	 * left to the view rendering this <code>Appointment</code> to decide how to
	 * render an appointment based on this property value. For instance, the
	 * month view, will display the <code>Appointment</code> at the top of the
	 * days in a week.
	 * 
	 * @param allDay
	 *            The current value of the <code>allDay</code> property
	 */
	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	public Appointment clone() {
		Appointment clone = new Appointment();
        clone.setId(this.id);
		clone.setAllDay(this.allDay);
        clone.setAttendees(this.attendees);
		clone.setCreatedBy(this.createdBy);
		clone.setDescription(this.description);
		clone.setEnd(DateUtils.newDate(this.end));
		clone.setLocation(this.location);
		clone.setStart(DateUtils.newDate(this.start));
		clone.setTitle(this.title);
		clone.setStyle(this.style);
        clone.setCustomStyle(this.customStyle);
        clone.setReadOnly(this.readOnly);

		return clone;
	}

	public AppointmentStyle getStyle() {
		return style;
	}

	public void setStyle(AppointmentStyle style) {
		this.style = style;
	}

	public String getCustomStyle() {
		return customStyle;
	}

	public void setCustomStyle(String customStyle) {
		this.customStyle = customStyle;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
}
