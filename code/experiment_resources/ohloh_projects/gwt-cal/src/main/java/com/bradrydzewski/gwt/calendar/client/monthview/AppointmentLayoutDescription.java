package com.bradrydzewski.gwt.calendar.client.monthview;

import com.bradrydzewski.gwt.calendar.client.Appointment;

/**
 * Contains common properties and behavior for layout descriptions that can be
 * &quot;stacked&quot; on top of each month view's week.
 *
 * @author Carlos D. Morales
 */
public class AppointmentLayoutDescription {
   /**
    * The layer order (in a stack-like arrangement)assigned to this description
    * by the <code>AppointmentStackingManager</code> when arranging the
    * descriptions on the top of a week.
    */
   private int stackOrder = 0;

   /**
    * The start day of the described <code>Appointment</code> when laid on the
    * top of one of the weeks while visualizing a month through the
    * <code>MonthView</code>.
    */
   private int fromWeekDay = 0;

   /**
    * The end day of the described <code>Appointment</code> when laid on the top
    * of one of the weeks while visualizing a month through the
    * <code>MonthView</code>.
    */
   private int toWeekDay = 0;

   /**
    * The underlying <code>Appointment</code> whose details will be displayed
    * through the <code>MonthView</code>.
    */
   private Appointment appointment = null;


   public AppointmentLayoutDescription(int fromWeekDay, int toWeekDay,
      Appointment appointment) {
      this.toWeekDay = toWeekDay;
      this.fromWeekDay = fromWeekDay;
      this.appointment = appointment;
   }

   public AppointmentLayoutDescription(int weekDay,
      Appointment appointment) {
      this(weekDay, weekDay, appointment);
   }

   public boolean overlapsWithRange(int from, int to) {
      return fromWeekDay >= from && fromWeekDay <= to ||
         fromWeekDay <= from && toWeekDay >= from;
   }

   public void setStackOrder(int stackOrder) {
      this.stackOrder = stackOrder;
   }

   public int getStackOrder() {
      return stackOrder;
   }

   public int getWeekStartDay() {
      return fromWeekDay;
   }

   public int getWeekEndDay() {
      return toWeekDay;
   }

   public boolean spansMoreThanADay() {
      return fromWeekDay != toWeekDay;
   }

   public AppointmentLayoutDescription split() {
      AppointmentLayoutDescription secondPart = null;
      if ( spansMoreThanADay() )
      {
         secondPart =
            new AppointmentLayoutDescription(fromWeekDay + 1, toWeekDay,
                                                 appointment);
         this.toWeekDay = this.fromWeekDay;
      }
      else
      {
         secondPart = this;
      }
      return secondPart;
   }


   public Appointment getAppointment() {
      return appointment;
   }

   public void setAppointment(Appointment appointment) {
      this.appointment = appointment;
   }

   @Override public String toString() {
      return "AppointmentLayoutDescription{" +
         "stackOrder=" + stackOrder +
         ", fromWeekDay=" + fromWeekDay +
         ", toWeekDay=" + toWeekDay +
         ", appointment=[" + appointment.getTitle() + "]@" + appointment.hashCode() + 
         '}';
   }
}
