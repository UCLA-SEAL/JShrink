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

/**
 * Indicates whether or not an Attendee will be attending an
 * {@link Appointment}.
 * 
 * @author Brad Rydzewski
 * @since 0.9.0
 */
public enum Attending {
	/**
	 * Indicates an Attendee will be attending.
	 */
	Yes,
	/**
	 * Indicates an Attendee will not be attending.
	 */
	No,
	/**
	 * Indicates an Attendee has not yet made up their mind
	 * whether or not they will be attending.
	 */
	Maybe
}
