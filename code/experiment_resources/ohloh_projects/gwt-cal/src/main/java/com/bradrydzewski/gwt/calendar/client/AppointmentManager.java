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
import java.util.Collections;

/**
 * Manages operations and state for the entire set of appointments displayed by
 * the GWT calendar. <p></p>
 * <p/>
 * The key responsibilities of the <code>AppointmentManager</code> are: <ul>
 * <li>Keep the calendar collection of appointments and provide operations to
 * update it</li> <li>Identify one of the appointments as the &quot;currently
 * selected&quot;</li> <li>Provide &quot;navigation&quot; methods to move the
 * &quot;currently selected&quot; from appointment to appointment over the
 * collection</li> <li>Keep track of changes to the set of appointments to
 * identify when sorting of the appointments -if client code needs them
 * chronologically ordered- should take place, i.e. appointments are not
 * guaranteed to be always in chronological order</li>  </ul>
 *
 * @author Brad Rydzewski
 * @author Carlos D. Morales
 */
public class AppointmentManager {
    /**
     * A reference to the &quot;currently selected appointment&quot;. Will be
     * <code>null</code> when no currently selected appointment has been set.
     */
    private Appointment selectedAppointment = null;
   
    /**
     * A reference to the most recent appointment to receive
     * a mouse over event.
     */
    private Appointment hoveredAppointment = null;
    
    /**
     * A copy of the last appointment that was updated,
     * prior to the update taking place. 
     */
    private Appointment rollbackAppointment = null;
    
    /**
     * A reference to the last appointment that was updated.
     */
    private Appointment committedAppointment = null;

    /**
     * The collection of appointments to be maintained by this
     * <code>AppointmentManager</code>.
     */
    private ArrayList<Appointment> appointments = new ArrayList<Appointment>();

    /**
     * Internal state flag indicating whether the collection of appointments
     * needs to be sorted.
     */
    private boolean sortPending = true;

    /**
     * Returns the collection of appointments that this <code>AppointmentManager</code>
     * maintains. <strong>Warning</strong>: this method returns a modifiable
     * reference to the internally managed list of appointments; client code
     * might break the invariants that this <code>AppointmentManager</code>
     * enforces if it performs any operations that modify the returned
     * collection.
     *
     * @return The appointments managed by this <code>AppointmentManager</code>.
     */
    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    /**
     * Adds an appointment to the collection of appointments maintained by this
     * <code>ApplicationManager</code>.
     *
     * @param appt The appointment to be made part of this manager's managed
     *             collection
     */
    public void addAppointment(Appointment appt) {
        if (appt != null) {
            appointments.add(appt);
            sortPending = true;
        }
    }

    /**
     * Adds multiple appointments to the collection maintained by this
     * <code>ApplicationManager</code>.
     *
     * @param appointments The appointments that will be made part of this
     *                     manager's managed collection
     */
    public void addAppointments(ArrayList<Appointment> appointments) {
        if (appointments != null) {
            for (Appointment appointment : appointments) {
                addAppointment(appointment);
            }
        }
    }

    /**
     * Removes the <code>appointment</code> from this manager's managed
     * collection.
     *
     * @param appointment The appointment to remove
     */
    public void removeAppointment(Appointment appointment) {
        if (appointment != null) {
            boolean wasRemoved = appointments.remove(appointment);
            if (wasRemoved) {
                sortPending = true;
            }
            //I'd rather have the client keep the reference to the selected one if they need it than having here
            //a reference to a thing I no longer should know about...
            if (hasAppointmentSelected() &&
                    getSelectedAppointment().equals(appointment)) {
                selectedAppointment = null;
            }
        }
    }

    /**
     * Removes the &quot;currently selected&quot; appointment from this
     * manager's collection.
     */
    public void removeCurrentlySelectedAppointment() {
        if (hasAppointmentSelected()) {
            removeAppointment(getSelectedAppointment());
            selectedAppointment = null;
        }
    }

    /**
     * Empties the collection of managed appointments.
     */
    public void clearAppointments() {
        appointments.clear();
    }

    /**
     * Sets the appointment that should be considered the &quot;currently
     * selected&quot; appointment.
     *
     * @param selectedAppointment The appointment to consider &quot;currently
     *                            selected&quot;
     */
    public void setSelectedAppointment(Appointment selectedAppointment) {
        if (selectedAppointment != null &&
                appointments.contains(selectedAppointment)) {
            this.selectedAppointment = selectedAppointment;
        }
    }

    /**
     * Indicates whether there is a &quot;currently selected&quot; appointment
     * at the moment.
     *
     * @return <code>true</code> if there is a currently selected appointment
     *         for the collection managed by this component, <code>false</code>
     *         otherwise
     */
    public boolean hasAppointmentSelected() {
        return selectedAppointment != null;
    }

    /**
     * Returns the appointment in this manager's collection that is
     * &quot;currently selected&quot;.
     *
     * @return The currently selected appointment
     */
    public Appointment getSelectedAppointment() {
        return selectedAppointment;
    }

    /**
     * Sorts the collection of appointments by their natural order.
     */
    public void sortAppointments() {
        if (sortPending) {
            Collections.sort(appointments);
            sortPending = false;
        }
    }

    /**
     * Moves the &quot;currently selected&quot; to the previous appointment in
     * the managed collection of this <code>AppointmentManager</code>. <br/> The
     * &quot;previous&quot; appointment will be the appointment before the
     * currently selected appointment in the set of appointments (whether
     * ordered or not).
     * <p/>
     * <br/> Because this operation depends on a &quot;currently selected
     * appointment&quot;, no previous appointment is considered to exist if
     * there is no &quot;currently selected appointment.&quot; or it is the
     * first in the set.
     *
     * @return <code>true</code> if selecting the previous appointment was
     *         successful, <code>false</code> no currently selected appointment
     *         is set or the currently selected appointment is the first in the
     *         collection.
     */
    public boolean selectPreviousAppointment() {
        boolean moveSucceeded = false;
        if (getSelectedAppointment() != null) {
            int selectedApptIndex =
                    getAppointments().indexOf(getSelectedAppointment());
            Appointment prevAppt;
            if (selectedApptIndex > 0 && (prevAppt =
                    getAppointments().get(selectedApptIndex - 1)) != null) {
                selectedAppointment = prevAppt;
                moveSucceeded = true;
            }
        }
        return moveSucceeded;
    }

    /**
     * Moves the &quot;currently selected&quot; to the next appointment in the
     * managed collection of this <code>AppointmentManager</code>. <br/> The
     * &quot;next&quot; appointment will be the appointment after the currently
     * selected appointment in the set of appointments (whether ordered or
     * not).
     * <p/>
     * <br/> Because this operation depends on a &quot;currently selected
     * appointment&quot;, no next appointment is considered to exist if there is
     * no &quot;currently selected appointment or it is the last in the
     * set.&quot;
     *
     * @return <code>true</code> if selecting the previous appointment was
     *         successful, <code>false</code> no currently selected appointment
     *         is set or the currently selected appointment is the last in the
     *         collection.
     */
    public boolean selectNextAppointment() {
        boolean moveSucceeded = false;

        if (getSelectedAppointment() != null) {
            int selectedApptIndex =
                    getAppointments().indexOf(getSelectedAppointment());
            int lastIndex = getAppointments().size() - 1;

            Appointment nextAppt;
            if (selectedApptIndex < lastIndex &&
                    (nextAppt = getAppointments().get(selectedApptIndex + 1)) !=
                            null) {
                selectedAppointment = nextAppt;
                moveSucceeded = true;
            }
        }

        return moveSucceeded;
    }

    /**
     * Resets the &quot;currently selected&quot; appointment of this manager.
     * <p></p>If this manager has a currently selected appointment, the
     * appointment <code>selected</code> property will be set to
     * <code>false</code> and this manager's <code>selectedAppointment</code>
     * property will be set to <code>null</code>.
     */
    public void resetSelectedAppointment() {
        if (hasAppointmentSelected()) {
            //selectedAppointment.setSelected(false);
            selectedAppointment = null;
        }
    }

    /**
     * Tells whether the passed <code>appointment</code> is the same one as the
     * one that is currently selected in this manager.
     *
     * @param appointment The appointment to test to be the same as the
     *                    currently selected
     * @return <code>true</code> if there is a currently selected appointment
     *         and it is equal to the passed <code>appointment</code>
     */
    public boolean isTheSelectedAppointment(Appointment appointment) {
        return hasAppointmentSelected() && selectedAppointment.equals(
                appointment);
    }
    
    public void commit() {
    	rollbackAppointment = null;
    	committedAppointment = null;
    }
    
    public void rollback() {
    	
    	//if the snapshot block is empty, we can do nothing
    	if(rollbackAppointment==null && committedAppointment==null)
    		return;
    	
    	//if there is no committed appointment, we assume
    	// this was a delete operation. We re-add the appointment
    	if(committedAppointment==null) {
    		addAppointment(rollbackAppointment);
    		
        //if there is no rollback appointment, we assume
        // this was an add or update operation. We remove the appointment
    	} else if(rollbackAppointment==null){

    		removeAppointment(committedAppointment);
    		
        //else, we assume this is an update
    	} else {
    		
    		removeAppointment(committedAppointment);
    		addAppointment(rollbackAppointment);
    	}
    	
    	commit();
    }
    
    public void setRollbackAppointment(Appointment appt) {
    	sortPending = true;
    	commit();
    	rollbackAppointment = appt;
    }
    
    public void setCommittedAppointment(Appointment appt) {
    	sortPending = true;
    	committedAppointment = appt;
    }

    public void resetHoveredAppointment() {
    	this.hoveredAppointment = null;
    }
    
	public void setHoveredAppointment(Appointment hoveredAppointment) {
		this.hoveredAppointment = hoveredAppointment;
	}

	public Appointment getHoveredAppointment() {
		return hoveredAppointment;
	}
}