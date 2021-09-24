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

import com.bradrydzewski.gwt.calendar.client.agenda.AgendaView;
import com.bradrydzewski.gwt.calendar.client.dayview.DayView;
import com.bradrydzewski.gwt.calendar.client.monthview.MonthView;

/**
 * Enumeration that represents each standard {@link CalendarView}.
 * @author Brad Rydzewski
 * @since 0.9.0
 */
public enum CalendarViews {
	/**
	 * Represents the {@link DayView}, which presents a set of
	 * Appointments a single day at a time.
	 */
	DAY,
	/**
	 * Represents the {@link MonthView}, which presents a set of
	 * Appointments for a whole month.
	 */
	MONTH,
	/**
	 * Represents the {@link AgendaView}, which presents a set of
	 * Appointments as a list.
	 */
	AGENDA
}
