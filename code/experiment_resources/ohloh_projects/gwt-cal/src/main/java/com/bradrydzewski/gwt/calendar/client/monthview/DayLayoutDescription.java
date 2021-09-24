package com.bradrydzewski.gwt.calendar.client.monthview;

import java.util.ArrayList;

import com.bradrydzewski.gwt.calendar.client.Appointment;

/**
 * Contains the calculated layout description of all <code>Appointment</code>s
 * in single day part of a week row in a <code>MonthView</code>.
 * <p></p><strong>Note:</strong> A <code>DayLayoutDescription</code> is not
 * aware of <em>multi-day</em> <code>Appointment</code>s that might span the day
 * represented by this description.
 *
 * @author Carlos D. Morales
 */
public class DayLayoutDescription {
    /**
     * The list of <em>simple</em> appointments (not multiday, not all-day) in
     * this single day.
     */
    private ArrayList<Appointment> appointments
            = new java.util.ArrayList<Appointment>();

    /**
     * The index of the represented day in the corresponding parent week.
     */
    private int dayIndex = -1;

    public DayLayoutDescription(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    public int getTotalAppointmentCount() {
        return appointments.size();
    }

    public void addAppointment(Appointment appointment) {
        if (!appointment.isMultiDay()) {
            appointments.add(appointment);
        } else {
            throw new IllegalArgumentException(
                    "Attempted to add a multiday appointment to a single day description");
        }
    }

    public int getDayIndex() {
        return dayIndex;
    }
}