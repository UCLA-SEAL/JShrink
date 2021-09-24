/*
 * This file is part of gwt-cal
 * Copyright (C) 2010  Scottsdale Software LLC
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
package com.bradrydzewski.gwt.calendar.client.util;

import java.util.ArrayList;
import java.util.Date;

import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.bradrydzewski.gwt.calendar.client.DateUtils;

/**
 * Utility class for several operations involving time and {@link Appointment}
 * objects.
 *
 * @author Brad Rydzewski
 * @author Carlos D. Morales
 */
public class AppointmentUtil {

    public static ArrayList<Appointment> filterListByDateRange(ArrayList<Appointment> fullList, Date date,
                                                  int days) {
        ArrayList<Appointment> group = new ArrayList<Appointment>();
        Date startDate = (Date) date.clone();
        DateUtils.resetTime(startDate);
        Date endDate = DateUtils.shiftDate(date, days);

        for (Appointment appointment : fullList) {
            if ((appointment.isMultiDay() || appointment.isAllDay()) &&
            		rangeContains(appointment, startDate, endDate)) {
                group.add(appointment);
            }
        }

        return group;
    }

    @SuppressWarnings("deprecation")
    public static boolean rangeContains(Appointment appt, Date date) {
        Date rangeEnd = (Date) date.clone();
        rangeEnd.setDate(rangeEnd.getDate() + 1);
        DateUtils.resetTime(rangeEnd);
        return rangeContains(appt, date, rangeEnd);
    }

    /**
     * Indicates whether the specified <code>appointment</code> falls within the
     * date range defined by <code>rangeStart</code> and <code>rangeEnd</code>.
     *
     * @param appointment The appointment to test
     * @param rangeStart  The range lower limit
     * @param rangeEnd    The range upper limit
     * @return <code>true</code> if the appointment's date falls within the
     *         range, <code>false</code> otherwise.
     */
    public static boolean rangeContains(Appointment appointment,
                                        Date rangeStart, Date rangeEnd) {
        long apptStartMillis = appointment.getStart().getTime();
        long apptEndMillis = appointment.getEnd().getTime();
        long rangeStartMillis = rangeStart.getTime();
        long rangeEndMillis = rangeEnd.getTime();

        return apptStartMillis >= rangeStartMillis
                && apptStartMillis < rangeEndMillis
                || apptStartMillis <= rangeStartMillis
                && apptEndMillis >= rangeStartMillis;
    }

    /**
     * Filters a list of appointments and returns only appointments with a start
     * date equal to the date provided. FYI - I hate everything about this
     * method and am pissed off I have to use it. May be able to avoid it in the
     * future
     *
     * @param fullList A full set of <code>Appointment</code>s, that will be filtered
     * with the above described rule
     * @param startDate The start date
     * @return A list with all appointments whose start date is on or after the
     * passed <code>startDate</code>
     */
    @SuppressWarnings("deprecation")
    public static ArrayList<Appointment> filterListByDate(ArrayList<Appointment> fullList, Date startDate, Date endDate) {

        ArrayList<Appointment> group = new ArrayList<Appointment>();

        for (Appointment appointment : fullList) {
           if (!appointment.isMultiDay() && !appointment.isAllDay() &&
                appointment.getEnd().before(endDate)) {
                //TODO: probably can shorten this by using the compareTo method
                if (appointment.getStart().after(startDate) ||
                    appointment.getStart().equals(startDate)) {
                    group.add(appointment);
                }
            }
        }

        return group;
    }
}

