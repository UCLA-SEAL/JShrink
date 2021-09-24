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

package com.bradrydzewski.gwt.calendar.theme.google.client;

import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.bradrydzewski.gwt.calendar.client.ThemeAppointmentStyle;
import com.bradrydzewski.gwt.calendar.client.dayview.DayViewStyleManager;

/**
 * Google theme style manager for the day view.
 *
 * @author Brad Rydzewski
 * @author Carlos D. Morales
 */
public class GoogleDayViewStyleManager extends DayViewStyleManager {

    @Override
    protected ThemeAppointmentStyle getDefaultViewAppointmentStyleForTheme() {
        return GoogleAppointmentTheme.DEFAULT;
    }

    @Override
    protected ThemeAppointmentStyle getViewAppointmentStyleForTheme(Appointment appointment) {
        return GoogleAppointmentTheme.STYLES.get(appointment.getStyle());
    }

}
