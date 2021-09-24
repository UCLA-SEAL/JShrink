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

import static com.bradrydzewski.gwt.calendar.client.monthview.MonthViewDateUtils.firstDateShownInAMonthView;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

/**
 * Test cases for the {@link com.bradrydzewski.gwt.calendar.client.monthview.MonthViewDateUtils}
 * utilities. This test case checks with the whole 2009 year months.
 * <p/>
 * Also, many of the test cases in this suite are meant to verify that the
 * appropriate <code>java.util.Date</code> cloning occurs to prevent undesired
 * effects of the date manipulations.
 *
 * @author Carlos D. Morales
 */
public class MonthViewDateUtilsTest {

   private DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

   @Test
   public void testMonthViewRequires5Rows() throws Exception {
      assertMonthRequiredRows(5, "01/01/2009", "Jan 09");
      assertMonthRequiredRows(5, "02/01/2009", "Feb 09");
      assertMonthRequiredRows(5, "03/01/2009", "Mar 09");
      assertMonthRequiredRows(5, "04/01/2009", "Apr 09");
      assertMonthRequiredRows(5, "06/01/2009", "Jun 09");
      assertMonthRequiredRows(5, "07/01/2009", "Jul 09");
      assertMonthRequiredRows(5, "09/01/2009", "Sep 09");
      assertMonthRequiredRows(5, "10/01/2009", "Oct 09");
      assertMonthRequiredRows(5, "11/01/2009", "Nov 09");
      assertMonthRequiredRows(5, "12/01/2009", "Dec 09");
   }

   @Test
   public void firstDateShownInAMonthView_CreatesClone() throws Exception {
      Date dayInMonth = date("02/01/2010");
      Date firstDateShownInMonthView =
         firstDateShownInAMonthView(dayInMonth, 0);
      assertEquals("First date in month grid", date("01/31/2010"),
                   firstDateShownInMonthView);
      assertNotSame("Date shown in a month view should be a brand new one",
                    dayInMonth, firstDateShownInMonthView);
   }

   @Test
   public void january2010() throws Exception {
      assertMonthRequiredRows(6, "01/31/2010", "Jan 10");
   }


   @Test
   public void february2010() throws Exception {
      assertMonthRequiredRows(5, "02/01/2010", "Feb 10");
   }

   @Test
   public void testMonthViewRequires6Rows_May09AndAug09() throws Exception {
      assertMonthRequiredRows(6, "05/01/2009", "May 09");
      assertMonthRequiredRows(6, "08/01/2009", "Aug 09");
   }

   private static final String[] YEAR_2010_DATES =
      {"01/01/2010", "02/01/2010", "03/01/2010", "04/01/2010", "05/01/2010",
         "06/01/2010", "07/01/2010", "08/01/2010", "09/01/2010", "10/01/2010",
         "11/01/2010", "12/01/2010"};

   @Test
   public void modifyFirstDayOfWeek_Sunday_2010() throws Exception {
      String[] firstDaysWhenWeekStartsOnMonday =
         {
            "12/27/2009", "01/31/2010", "02/28/2010", "03/28/2010",
            "04/25/2010", "05/30/2010", "06/27/2010", "08/01/2010",
            "08/29/2010", "09/26/2010", "10/31/2010", "11/28/2010"};
      assertFirstDateInMonthView(firstDaysWhenWeekStartsOnMonday, 0);
   }

   @Test
   public void modifyFirstDayOfWeek_Monday_2010() throws Exception {
      String[] firstDaysWhenWeekStartsOnMonday =
         {
            "12/28/2009", "02/01/2010", "03/01/2010", "03/29/2010",
            "04/26/2010", "05/31/2010", "06/28/2010", "07/26/2010",
            "08/30/2010", "09/27/2010", "11/01/2010", "11/29/2010"};
      assertFirstDateInMonthView(firstDaysWhenWeekStartsOnMonday, 1);
   }

   @Test
   public void modifyFirstDayOfWeek_Tuesday_2010() throws Exception {
      String[] firstDaysWhenWeekStartsOnTuesday =
         {
            "12/29/2009", "01/26/2010", "02/23/2010", "03/30/2010",
            "04/27/2010", "06/01/2010", "06/29/2010", "07/27/2010",
            "08/31/2010", "09/28/2010", "10/26/2010", "11/30/2010"
         };
      assertFirstDateInMonthView(firstDaysWhenWeekStartsOnTuesday, 2);
   }

   @Test
   public void modifyFirstDayOfWeek_Wednesday_2010() throws Exception {
      String[] firstDaysWhenWeekStartsOnWednesday =
         {
            "12/30/2009", "01/27/2010", "02/24/2010", "03/31/2010",
            "04/28/2010", "05/26/2010", "06/30/2010", "07/28/2010",
            "09/01/2010", "09/29/2010", "10/27/2010", "12/01/2010"

         };
      assertFirstDateInMonthView(firstDaysWhenWeekStartsOnWednesday, 3);
   }

   @Test
   public void modifyFirstDayOfWeek_Thursday_2010() throws Exception {
      String[] firstDaysWhenWeekStartsOnWednesday =
         {
            "12/31/2009", "01/28/2010", "02/25/2010", "04/01/2010",
            "04/29/2010", "05/27/2010", "07/01/2010", "07/29/2010",
            "08/26/2010", "09/30/2010", "10/28/2010", "11/25/2010"
         };
      assertFirstDateInMonthView(firstDaysWhenWeekStartsOnWednesday, 4);
   }

   @Test
   public void modifyFirstDayOfWeek_Friday_2010() throws Exception {
      String[] firstDaysWhenWeekStartsOnWednesday =
         {
            "01/01/2010", "01/29/2010", "02/26/2010", "03/26/2010",
            "04/30/2010", "05/28/2010", "06/25/2010", "07/30/2010",
            "08/27/2010", "10/01/2010", "10/29/2010", "11/26/2010"
         };
      assertFirstDateInMonthView(firstDaysWhenWeekStartsOnWednesday, 5);
   }

   @Test
   public void modifyFirstDayOfWeek_Saturday_2010() throws Exception {
      String[] firstDaysWhenWeekStartsOnWednesday =
         {
            "12/26/2009", "01/30/2010", "02/27/2010", "03/27/2010",
            "05/01/2010", "05/29/2010", "06/26/2010", "07/31/2010",
            "08/28/2010", "09/25/2010", "10/30/2010", "11/27/2010"

         };
      assertFirstDateInMonthView(firstDaysWhenWeekStartsOnWednesday, 6);
   }

   private void assertFirstDateInMonthView(String[] expected, int startOn)
      throws Exception {
      for (int i = 0; i < 12; i++) {
         assertEquals(date(expected[i]),
                      MonthViewDateUtils.firstDateShownInAMonthView(
                         date(YEAR_2010_DATES[i]), startOn));
      }
   }

   private void assertMonthRequiredRows(int expectedRows, String dateString,
      String monthYear) throws Exception {
      assertEquals(String.format("%s required %d rows", monthYear,
                                 expectedRows), expectedRows,
                   MonthViewDateUtils.monthViewRequiredRows(date(dateString),
                                                            0));
   }

   public Date date(String dateString) throws Exception {
      return dateFormatter.parse(dateString);
   }


}
