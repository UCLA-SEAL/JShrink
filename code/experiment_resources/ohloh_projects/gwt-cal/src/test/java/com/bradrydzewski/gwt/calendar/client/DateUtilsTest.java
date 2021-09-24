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
package com.bradrydzewski.gwt.calendar.client;

import static com.bradrydzewski.gwt.calendar.client.DateUtils.areOnTheSameMonth;
import static com.bradrydzewski.gwt.calendar.client.DateUtils.firstOfNextMonth;
import static com.bradrydzewski.gwt.calendar.client.DateUtils.moveOneDayForward;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

/**
 * Test cases for the set of utilities to work with <code>java.util.Date</code>
 * objects.
 *
 * @author Carlos D. Morales
 */
public class DateUtilsTest {

   private DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

   @Test
   public void moveOneDayForward_WithinSameMonth() throws Exception {
      assertEquals(date("01/31/2010"), moveOneDayForward(date("01/30/2010")));
   }

   @Test
   public void moveOneDayForward_AcrossMonths() throws Exception {
      assertEquals(date("02/01/2010"), moveOneDayForward(date("01/31/2010")));
   }

   @Test
   public void moveOneDayForward_AcrossYears() throws Exception {
      assertEquals(date("01/01/2011"), moveOneDayForward(date("12/31/2010")));
   }

   @Test
   public void firstOfTheMonth_LastOfJan2010() throws Exception {
      Date someDayInMonth = date("01/31/2010");
      Date first = DateUtils.firstOfTheMonth(someDayInMonth);
      assertEquals(date("01/01/2010"), first);
      assertNotSame(first, someDayInMonth);
   }

   @Test
   public void firstOfTheMonth_LastOfFeb2010() throws Exception {
      Date someDayInMonth = date("02/28/2010");
      Date first = DateUtils.firstOfTheMonth(someDayInMonth);
      assertEquals(date("02/01/2010"), first);
      assertNotSame(first, someDayInMonth);
   }

   @Test
   public void differenceInDays_SunDec_20101226_MonJan_20110103()
      throws Exception {
      assertEquals(8, DateUtils.differenceInDays(date("01/03/2011"),
                                                 date("12/26/2010")));
   }

   @Test
   public void differenceInDays_SunApr_20100425_MonMay_20100503()
      throws Exception {
      assertEquals(8, DateUtils.differenceInDays(date("05/03/2010"),
                                                 date("04/25/2010")));
   }


   @Test
   public void differenceInDays_SunDec_20101226_MonMay_20110103()
      throws Exception {
      assertEquals(8, DateUtils.differenceInDays(date("01/03/2011"),
                                                 date("12/26/2010")));
   }

   @Test
   public void differenceInDays_consecutive_but_swapped() throws Exception {
      assertEquals(1,
                   DateUtils.differenceInDays(date("05/25/1981"),
                                              date("05/26/1981")));
   }

   @Test
   public void differenceInDays_endDateWithNoHours() throws Exception {
      assertEquals(1, DateUtils.differenceInDays(date("02/01/2010"),
                                                 date("01/31/2010")));
   }

   @Test
   @SuppressWarnings(value = "deprecation")
   public void differenceInDays_endDateWithHours() throws Exception {

      Date endDate = date("02/01/2010");
      endDate.setHours(9);
      endDate.setMinutes(0);
      Date startDateFirstInstant = date("01/31/2010");
      DateUtils.resetTime(startDateFirstInstant);
      assertEquals(1, DateUtils.differenceInDays(endDate,
                                                 startDateFirstInstant));
   }

   @Test
   @SuppressWarnings(value = "deprecation")
   public void differenceInDays_startCloseToEOD_endRightAfterBeginningOfDay()
      throws Exception {
      Date endDate = date("02/01/2010");
      endDate.setHours(0);
      endDate.setMinutes(2);
      Date startDate = date("01/31/2010");
      startDate.setHours(23);
      startDate.setMinutes(58);
      assertEquals(1, DateUtils.differenceInDays(endDate, startDate));
   }

   @Test
   public void differenceInDays_startOnFirstInstant_endOnLastInstant_differentDays()
      throws Exception {
      Date endDate = new Date(date("02/02/2010").getTime() - 1);
      Date startDate = date("01/31/2010");
      DateUtils.resetTime(startDate);
      assertEquals(1, DateUtils.differenceInDays(endDate, startDate));
   }


   @Test
   @SuppressWarnings("deprecation")
   public void differenceInDays_bothSameDate_DifferentTimes()
      throws Exception {
      Date endDate = date("01/31/2010");
      endDate.setHours(0);
      endDate.setMinutes(2);
      Date startDate = date("01/31/2010");
      startDate.setHours(23);
      startDate.setMinutes(58);
      assertEquals(0, DateUtils.differenceInDays(endDate, startDate));
   }

   @Test
   @SuppressWarnings("deprecation")
   public void testFirstOfNextMonthChangeYear() throws Exception {
      Date dec012009 = date("12/01/2009");
      Date jan012010 = firstOfNextMonth(dec012009);
      assertEquals("Day should be the first", 1, jan012010.getDate());
      assertEquals("Year should have changed", 0, jan012010.getMonth());
   }

   @Test
   @SuppressWarnings("deprecation")
   public void testFirstOfNextMonthFromFebOnLeapYear() throws Exception {
      Date feb012009 = date("02/01/2009");
      Date mar012009 = firstOfNextMonth(feb012009);
      assertEquals("Day should be the first", 1, mar012009.getDate());
      assertEquals("Month should be march", 2, mar012009.getMonth());
      assertEquals("Year should be the same", 2009 - 1900, mar012009.getYear());
   }

   @Test
   public void testAreOnTheSameMonth() throws Exception {
      assertTrue(areOnTheSameMonth(date("02/28/2010"), date("02/01/2010")));
      assertTrue(areOnTheSameMonth(date("12/31/2010"), date("12/01/2010")));
      assertFalse(areOnTheSameMonth(date("10/28/2009"), date("10/28/2010")));
      assertFalse(areOnTheSameMonth(date("01/01/2009"), date("02/01/2009")));
   }

   @Test
   public void newDateSourceIsNull(){
       assertNull(DateUtils.newDate(null));
   }

    @Test
    public void newDateReturnsSemanticallyEqualObject(){
        Date original = new Date();
        Date copy = DateUtils.newDate(original);
        assertEquals(original, copy);
    }

   public Date date(String dateString) throws Exception {
      return dateFormatter.parse(dateString);
   }

}
