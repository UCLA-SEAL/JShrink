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
package com.bradrydzewski.gwt.calendar.client.dayview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a block of time that contains one or many Appointments. An example
 * of a timeblock could be 10am - 10:30 am, where a block of time is 30 minutes.
 * <p> A block of time can contain or be interested by one or many appointments.
 * Using the above example, a time block from 10am - 10:30 am could contain the
 * following appointments:
 * <ul>
 * <li>10:00 - 10:30, where start / end are same as time block</li>
 * <li>9:00 - 10:30, where appointment start before and ends at
 * same time as tiem block</li>
 * <li>9:00 - 10:20, where appointment start before
 * and ends after start of time block</li>
 * <li>9:00 - 11:00, where appointment starts before and ends after the time
 * block</li>
 * <li>10:05 - 10:25, where appointment starts after but ends before the
 * time block</li> </ul></p> 
 * Above
 * are example use cases of a time block and how it relates to an appointment.
 * Note that an appointment can intersect with one ore many time blocks. This
 * class holds a number of parameters allowing a time block to manage its
 * appointments and determine where the appointments should be arranged within
 * itself.
 *
 * @author Brad Rydzewski
 * @version 1.0 6/07/09
 * @since 1.0
 */
public class TimeBlock {

   private List<AppointmentAdapter> appointments =
      new ArrayList<AppointmentAdapter>();
   private Map<Integer, Integer> occupiedColumns = new HashMap<Integer, Integer>();
   private int totalColumns = 1;
   private int order;
   private String name;
   private int start;
   private int end;
   private float top;
   private float bottom;

   public List<AppointmentAdapter> getAppointments() {
      return appointments;
   }

   public Map<Integer, Integer> getOccupiedColumns() {
      return occupiedColumns;
   }

   public int getFirstAvailableColumn() {

      int col = 0;
      while (true) {
         if (occupiedColumns.containsKey(col))
            col++;
         else return col;
      }
   }

   public int getTotalColumns() {
      return totalColumns;
   }

   public void setTotalColumns(int totalColumns) {
      this.totalColumns = totalColumns;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getOrder() {
      return order;
   }

   public void setOrder(int order) {
      this.order = order;
   }

   public int getEnd() {
      return end;
   }

   public void setEnd(int end) {
      this.end = end;
   }

   public int getStart() {
      return start;
   }

   public void setStart(int start) {
      this.start = start;
   }

   public float getBottom() {
      return bottom;
   }

   public void setBottom(float bottom) {
      this.bottom = bottom;
   }

   public float getTop() {
      return top;
   }

   public void setTop(float top) {
      this.top = top;
   }

   public boolean intersectsWith(int apptStart, int apptEnd) {

      //scenario 1: start date of appt between start and end of block
      if (apptStart >= this.getStart() && apptStart < this.getEnd())
         return true;

      //scenario 2: end date of appt > block start date &
      //  start date of appt before block start date
      return apptEnd > this.getStart() && apptStart < this.getStart();
   }

   public boolean intersectsWith(AppointmentAdapter appt) {
      return intersectsWith(appt.getAppointmentStart(),
                            appt.getAppointmentEnd());
   }
}
