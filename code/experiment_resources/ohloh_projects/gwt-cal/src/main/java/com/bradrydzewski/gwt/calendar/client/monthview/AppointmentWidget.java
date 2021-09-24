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

import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * A panel used to render an <code>Appointment</code> in a
 * <code>MonthView</code>. <p> Through an association to a domain-model
 * <code>Appointment</code>, <code>AppointmentWidget</code>s allow displaying
 * the appointment details <em>and</em> to capture mouse and keyboard events.
 * </p>
 *
 * @author Brad Rydzewski
 * @author Carlos D. Morales
 */
public class AppointmentWidget extends FocusPanel {
   /**
    * The underlying <code>Appointment</code> represented by this panel.
    */
   private Appointment appointment;

   /**
    * Creates an <code>AppointmentWidget</code> with a reference to the provided
    * <code>appointment</code>.
    *
    * @param appointment The appointment to be displayed through this panel
    *                    widget
    */
   public AppointmentWidget(Appointment appointment) {
      this.appointment = appointment;
      Label titleLabel = new Label();
      titleLabel.getElement().setInnerHTML(this.appointment.getTitle());
      this.add(titleLabel);
   }

   /**
    * Returns the <code>Appointment</code> this panel represents/is associated
    * to.
    *
    * @return The domain model appointment rendered through this panel
    */
   public Appointment getAppointment() {
      return appointment;
   }
}
