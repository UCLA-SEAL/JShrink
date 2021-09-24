/*
 * This file is part of gwt-cal
 * Copyright (C) 2009-2011  Scottsdale Software LLC
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.Date;

import org.junit.Test;

/**
 * Test cases for the domain model class {@link Appointment}.
 *
 * @author Carlos D. Morales
 */
public class AppointmentTest {

    @Test
    public void cloneClonesStartAndDateDates_notAttendeesThough(){
        Appointment original = new Appointment();
        original.setId("ID");
        original.setTitle("TITLE");
        original.setDescription("DESCRIPTION");
        original.setStart(new Date());
        original.setEnd(new Date());
        original.setLocation("LOCATION");
        original.setCreatedBy("CREATED BY");
        original.setAttendees(Collections.<Attendee>emptyList());
        original.setAllDay(true);
        original.setStyle(AppointmentStyle.DARK_PURPLE);
        original.setCustomStyle("FUNNY CUSTOM STYLE");

        Appointment clone = original.clone();
        assertEquals(original.getId(), clone.getId());
        assertEquals(original.getId(), clone.getId());
        assertEquals(original.getTitle(), clone.getTitle());
        assertEquals(original.getDescription(), clone.getDescription());

        assertEquals(original.getStart(), clone.getStart());
        assertNotSame(original.getStart(), clone.getStart());

        assertEquals(original.getEnd(), clone.getEnd());
        assertNotSame(original.getEnd(), clone.getEnd());

        assertEquals(original.getLocation(), clone.getLocation());
        assertEquals(original.getCreatedBy(), clone.getCreatedBy());

        assertSame(original.getAttendees(), clone.getAttendees());

        assertEquals(original.isAllDay(), clone.isAllDay());
        assertEquals(original.getStyle(), clone.getStyle());
        assertEquals(original.getCustomStyle(), clone.getCustomStyle());
    }
}
