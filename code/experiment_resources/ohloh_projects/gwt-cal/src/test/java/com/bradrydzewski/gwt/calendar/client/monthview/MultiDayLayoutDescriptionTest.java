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
package com.bradrydzewski.gwt.calendar.client.monthview;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test cases for the {@link com.bradrydzewski.gwt.calendar.client.monthview.AppointmentLayoutDescription}
 * class. These test cases focus on verifying logic for appointments that
 * span more than a day (hence multi-day in the name).
 *
 * @author Carlos D. Morales
 */
public class MultiDayLayoutDescriptionTest {

    private AppointmentLayoutDescription multidayDescription = null;

    @Test
    public void singleDayDescriptionOverlap() {
        multidayDescription = new AppointmentLayoutDescription(0, 0, null);
        assertTrue("Overlap of single day expected to be true",
                multidayDescription.overlapsWithRange(0, 0));
    }

    @Test
    public void singleDayDescriptionWithOverlappingTwoDays() {
        multidayDescription = new AppointmentLayoutDescription(0, 0, null);
        assertTrue(
                "Overlap of description on index 0 with description 0-1 expected to be true",
                multidayDescription.overlapsWithRange(0, 1));
    }

    @Test
    public void singleDayDescriptionWithOverlappingEnclosingTotally() {
        multidayDescription = new AppointmentLayoutDescription(1, 1, null);
        assertTrue(
                "Overlap of description on index 1 with description 0-2 expected to be true",
                multidayDescription.overlapsWithRange(0, 2));
    }

    @Test
    public void twoDayDescriptionCompleteOverlap() {
        multidayDescription = new AppointmentLayoutDescription(1, 2, null);
        assertTrue(
                "Overlap of description on index 1-2 with description 1-2 expected to be true",
                multidayDescription.overlapsWithRange(1, 2));
    }

    @Test
    public void twoDayDescriptionOtherOverlappingOnLeft() {
        multidayDescription = new AppointmentLayoutDescription(1, 2, null);
        assertTrue(
                "Overlap of description on index 1-2 with description 0-1 expected to be true",
                multidayDescription.overlapsWithRange(0, 1));
    }

    @Test
    public void twoDayDescriptionOtherOverlappingOnRight() {
        multidayDescription = new AppointmentLayoutDescription(1, 2, null);
        assertTrue(
                "Overlap of description on index 1-2 with description 2-3 expected to be true",
                multidayDescription.overlapsWithRange(2, 3));
    }
}
