package com.bradrydzewski.gwt.calendar.client.monthview;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test cases to verify logic in the <code>AppointmentLayoutDescription</code>.
 *
 * @author Carlos D. Morales
 */
public class AppointmentLayoutDescriptionTest {

   private AppointmentLayoutDescription appointmentDescription = null;

   @Test
   public void spansMoreThanADay_SameStartAndEndDay() {
      appointmentDescription = new AppointmentLayoutDescription(0, 0, null);
      assertFalse(appointmentDescription.spansMoreThanADay());
   }

   @Test
   public void spansMoreThanADay_DifferentStartAndEndDay() {
      appointmentDescription = new AppointmentLayoutDescription(0, 1, null);
      assertTrue(appointmentDescription.spansMoreThanADay());
   }

   @Test
   public void splitSingleDayDescription() {
      appointmentDescription = new AppointmentLayoutDescription(0, 0, null);
      assertSame(appointmentDescription, appointmentDescription.split());
   }


   @Test
   public void splitTwoDayDescription() {
      appointmentDescription = new AppointmentLayoutDescription(0, 1, null);
      assertTrue(appointmentDescription.spansMoreThanADay());

      AppointmentLayoutDescription secondDay = appointmentDescription.split();

      assertFalse(appointmentDescription.spansMoreThanADay());
      assertStartEndDay(0,0, appointmentDescription);
      assertSame(appointmentDescription, appointmentDescription.split());
      assertStartEndDay(1, 1, secondDay);
   }

   private void assertStartEndDay(int start, int end,
      AppointmentLayoutDescription description) {
      assertEquals("Start day", start, description.getWeekStartDay());
      assertEquals("End day", end, description.getWeekEndDay());
   }

}
