/*
 * This file is part of gwt-cal
 * Copyright (C) 2011  Scottsdale Software LLC
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
 * Defines the style attribute values that will vary with a theme
 * when a particular theme+style is applied. The currently active
 * {@link com.bradrydzewski.gwt.calendar.client.monthview.MonthViewStyleManager} or
 * {@link com.bradrydzewski.gwt.calendar.client.dayview.DayViewStyleManager} will use the
 * strings in the theme style to style the elements in the view.
 *
 * @author Brad Rydzewski
 * @author Carlos D. Morales
 *
 * @see com.bradrydzewski.gwt.calendar.theme.google.client.GoogleAppointmentStyle
 * @see com.bradrydzewski.gwt.calendar.theme.ical.client.ICalAppointmentStyle
 */
public interface ThemeAppointmentStyle {

    public String getBackgroundHeader();

    public String getBackground();

    public String getSelectedBorder();

    public String getHeaderText();

    public String getSelectedBackgroundImage();

    public String getBorder();
}
