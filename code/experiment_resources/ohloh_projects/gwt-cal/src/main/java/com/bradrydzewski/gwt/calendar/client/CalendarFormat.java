/*
 * This file is part of gwt-cal
 * Copyright (C) 2009-2011  Scottsdale Software LLC
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

import java.util.Date;

import com.bradrydzewski.gwt.calendar.client.i18n.CalendarConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

@SuppressWarnings("deprecation")
public class CalendarFormat {

   public static final CalendarConstants MESSAGES =
      (CalendarConstants) GWT.create(CalendarConstants.class);

   public static final int HOURS_IN_DAY = 24;

   private static final DateTimeFormat DEFAULT_DAY_OF_MONTH_FORMAT =
      DateTimeFormat.getFormat(MESSAGES.dayOfMonthFormat());

   private static final DateTimeFormat DEFAULT_DAY_OF_WEEK_FORMAT =
      DateTimeFormat.getFormat(MESSAGES.weekdayFormat());

   private static final DateTimeFormat DEFAULT_DAY_OF_WEEK_ABBREVIATED_FORMAT =
      DateTimeFormat.getFormat(MESSAGES.weekdayFormat());

   private static final DateTimeFormat DEFAULT_HOUR_FORMAT =
      DateTimeFormat.getFormat(MESSAGES.timeFormat());

   private static final DateTimeFormat DEFAULT_DATE_FORMAT =
      DateTimeFormat.getFormat(MESSAGES.dateFormat());

   private static String DEFAULT_AM_LABEL = MESSAGES.am();
   private static String DEFAULT_PM_LABEL = MESSAGES.pm();
   private static String DEFAULT_NOON_LABEL = MESSAGES.noon();

   private String[] weekDayNames = new String[7];
   private String[] dayOfWeekAbbreviatedNames = new String[7];
   private String[] dayOfMonthNames = new String[32];
   private String[] hours = new String[24];

   private DateTimeFormat dayOfMonthFormat = null;
   private DateTimeFormat dayOfWeekFormat = null;
   private DateTimeFormat dayOfWeekAbbreviatedFormat = null;
   private DateTimeFormat timeFormat = null;
   private DateTimeFormat dateFormat = null;
   private String am = null;
   private String pm = null;
   private String noon = null;

   private boolean useNoonLabel = true;
	
   private int firstDayOfWeek = Integer.valueOf(MESSAGES.firstDayOfWeek());

   public static CalendarFormat INSTANCE = new CalendarFormat();
   
   private CalendarFormat() {
      dayOfMonthFormat = DEFAULT_DAY_OF_MONTH_FORMAT;
      dayOfWeekFormat = DEFAULT_DAY_OF_WEEK_FORMAT;
      dayOfWeekAbbreviatedFormat = DEFAULT_DAY_OF_WEEK_ABBREVIATED_FORMAT;
      timeFormat = DEFAULT_HOUR_FORMAT;
      dateFormat = DEFAULT_DATE_FORMAT;
      am = DEFAULT_AM_LABEL;
      pm = DEFAULT_PM_LABEL;
      noon = DEFAULT_NOON_LABEL;
      refreshWeekDayNames();
      refreshMonthDayNames();
      generateHourLabels();
   }

   /**
    * Configures the formatting pattern to render the days of the week using
    * <code>DateTimeFormat</code>.
    *
    * @param formatPattern The pattern to format day names
    * @see com.google.gwt.i18n.client.DateTimeFormat#getFormat(String)
    */
   public void setDayOfWeekFormat(String formatPattern) {
      dayOfWeekFormat = DateTimeFormat.getFormat(formatPattern);
      refreshWeekDayNames();
   }

   /**
    * Returns the names (labels) of the days of the week.
    *
    * @return The days of the 7 days of the week, formatted with the current
    *         configuration
    */
   public String[] getDayOfWeekNames() {
      return weekDayNames;
   }

   /**
    * Configures the formatting pattern to render the days of the week in an
    * abbreviated manner using <code>DateTimeFormat</code>.
    *
    * @param formatPattern The pattern to format day names
    * @see com.google.gwt.i18n.client.DateTimeFormat#getFormat(String)
    */
   public void setDayOfWeekAbbreviatedFormat(String formatPattern) {
      dayOfWeekAbbreviatedFormat = DateTimeFormat.getFormat(formatPattern);
      refreshWeekDayNames();
   }

   public String[] getDayOfWeekAbbreviatedNames() {
      return dayOfWeekAbbreviatedNames;
   }

   private void refreshWeekDayNames() {
      Date date = new Date();
      for (int i = 1; i <= 7; i++) {
         date.setDate(i);
         int dayOfWeek = date.getDay();
         weekDayNames[dayOfWeek] = dayOfWeekFormat.format(date);
         dayOfWeekAbbreviatedNames[dayOfWeek] =
            dayOfWeekAbbreviatedFormat.format(date);
      }
   }

   /**
    * Configures the formatting pattern to render the days of the month using
    * <code>DateTimeFormat</code>. Most likely, <code>formatPattern</code> will
    * contain at the minimum, the format to render the number of the
    * corresponding day.
    *
    * @param formatPattern The pattern to format days in the month view
    * @see com.google.gwt.i18n.client.DateTimeFormat#getFormat(String)
    */
   public void setDayOfMonthFormat(String formatPattern) {
      dayOfMonthFormat = DateTimeFormat.getFormat(formatPattern);
      refreshMonthDayNames();
   }

   private void refreshMonthDayNames() {
      Date date = new Date();
      date.setMonth(0);
      for (int i = 1; i < 32; ++i) {
         date.setDate(i);
         dayOfMonthNames[i] = dayOfMonthFormat.format(date);
      }
   }

   public void setDateFormat(String formatPattern) {
      dateFormat = DateTimeFormat.getFormat(formatPattern);
   }

   public DateTimeFormat getDateFormat() {
      return dateFormat;
   }

   /**
    * Sets the pattern used to format the displayed hours and re-generates
    * all hour labels.
    *
    * @param formatPattern A legal format following the patterns
    * in {@link com.google.gwt.i18n.client.DateTimeFormat}
    */
   public void setTimeFormat(String formatPattern) {
	  timeFormat = DateTimeFormat.getFormat(formatPattern);
      generateHourLabels();      
   }

   public DateTimeFormat getTimeFormat() {
      return timeFormat;
   }

   /**
    * Allows programmatic configuration of the 24 hour labels in the calendar.
    *
    * @param hourLabels The labels to be used as labels for the hours of the
    *                   day.
    * @throws IllegalArgumentException If the <code>hourLabes</code> array is
    *                                  <code>null</code>, does not have 24
    *                                  elements, or any of the elements is
    *                                  <code>null</code>
    */
   public void setHourLabels(String[] hourLabels) {
      if (hourLabels == null || hourLabels.length != HOURS_IN_DAY) {
         throw new IllegalArgumentException(
            "24 Hour labels expected. Please provide an array with 24 non-null values");
      }
      for (int i = 0; i < HOURS_IN_DAY; i++) {
         if (hourLabels[i] == null) {
            throw new IllegalArgumentException(
               "Hour @ position " + i + " is null.");
         }
         hours[i] = hourLabels[i];
      }
   }

   /**
    * Default logic to generate the labels for the hours.
    */
   private void generateHourLabels() {
//      Date date = new Date();
//      DateTimeFormat shortTimeFormat = DateTimeFormat.getShortTimeFormat();
//      date.setHours(12);
//      date.setMinutes(0);
//      String hour = shortTimeFormat.format(date);
//      String hourFormat = "h";
//
//      if (!hour.equals("12:00 DEFAULT_PM_LABEL")) {
//         noon = hour;
//         am = "";
//         pm = "";
//         hourFormat = shortTimeFormat.getPattern();
//      }
//
//      shortTimeFormat = DateTimeFormat.getFormat(hourFormat);

	   Date date = new Date();
	   date.setHours(0);
	   date.setMinutes(0);
	   String hour;
	   
      for (int i = 0; i < HOURS_IN_DAY; i++) {
         date.setHours(i);
         hour = timeFormat.format(date);//shortTimeFormat.format(date);
         hours[i] = hour;
      }
   }

   /**
    * Returns the currently configured day to start weeks in the
    * <code>MonthView</code>. The default value is read from the
    * <code>CalendarConstants</code> i18n configuration file, but it can be
    * changed through the <code>setFirstDayOfWeek</code> method of this class.
    *
    * @return The currently configured day to start weeks, <code>0</code> for
    *         Sunday, <code>1</code> for Monday, and so on.
    */
   public int getFirstDayOfWeek() {
      return firstDayOfWeek;
   }

   /**
    * Configures the first day in the week when rendering the month view.
    *
    * @param firstDayOfWeek The first day of the week, where Sunday is
    *                       represented by <code>0</code>, Monday by
    *                       <code>1</code>, and so on.
    */
   public void setFirstDayOfWeek(int firstDayOfWeek) {
      this.firstDayOfWeek = Math.abs(firstDayOfWeek % 7);
   }

   public String getAm() {
      return am;
   }

   public void setAm(String am) {
      this.am = am;
   }

   public String getPm() {
      return pm;
   }

   public void setPm(String pm) {
      this.pm = pm;
   }

   /**
    * Returns the currently configured label for the noon (12 p.m.).
    *
    * @return The configured label for the 12 p.m. time either through the
    *         <code>CalendarConstants</code> or the <code>setNoon</code> method
    *         of this class.
    */
   public String getNoon() {
      return noon;
   }

   /**
    * Configures the label to show for the 12 p.m.
    *
    * @param noon A label to show instead of 12 p.m.
    */
   public void setNoon(String noon) {
      this.noon = noon;
   }

   /**
    * Returns the configured labels for the 24 hours of the day. Some labels
    * will vary, depending con configuration, for example &quot;Noon&quot;
    * instead of &quot;12&quot;.
    *
    * @return An array of Strings with the corresponding labels for the hours in
    *         a day
    */
   public String[] getHourLabels() {
      return hours;
   }

   /**
    * Indicates if we want to use the NoonLabel or the 12 p.m.
    * @param use <code>true</true> if we want to use the NoonLabel, <code>false</true> otherwise
    * @since 0.9.4
    */
	public void setUseNoonLabel(boolean use) {
		this.useNoonLabel = use;
	}

	/**
	 * Indicates if we are using the NoonLabel for the 12 p.m.
	 * @since 0.9.4
	 */
	public boolean isUseNoonLabel() {
		return useNoonLabel;
	}
}