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

import java.util.Date;

import com.bradrydzewski.gwt.calendar.client.CalendarFormat;
import com.bradrydzewski.gwt.calendar.client.DateUtils;
import com.bradrydzewski.gwt.calendar.client.HasSettings;
import com.bradrydzewski.gwt.calendar.client.event.DaySelectionEvent;
import com.bradrydzewski.gwt.calendar.client.event.DaySelectionHandler;
import com.bradrydzewski.gwt.calendar.client.event.HasDaySelectionHandlers;
import com.bradrydzewski.gwt.calendar.client.event.HasWeekSelectionHandlers;
import com.bradrydzewski.gwt.calendar.client.event.WeekSelectionEvent;
import com.bradrydzewski.gwt.calendar.client.event.WeekSelectionHandler;
import com.bradrydzewski.gwt.calendar.client.util.WindowUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DayViewHeader extends Composite implements HasWeekSelectionHandlers<Date>, HasDaySelectionHandlers<Date> {
   private FlexTable header = new FlexTable();
   private VerticalPanel timePanel = new VerticalPanel();
   private AbsolutePanel dayPanel = new AbsolutePanel();
   private AbsolutePanel weekPanel = new AbsolutePanel();
   private AbsolutePanel splitter = new AbsolutePanel();
   //private static final DateTimeFormat DAY_FORMAT = DateTimeFormat.getFormat("EEE, MMM d");
   private static final String GWT_CALENDAR_HEADER_STYLE =
      "gwt-calendar-header";
   private static final String DAY_CELL_CONTAINER_STYLE = "day-cell-container";
   private static final String WEEK_CELL_CONTAINER_STYLE = "week-cell-container";
   private static final String YEAR_CELL_STYLE = "year-cell";
   private static final String SPLITTER_STYLE = "splitter";
   private final boolean showWeekNumbers;
   private final HasSettings settings;

   public DayViewHeader(HasSettings settings) {
      initWidget(header);
      
      this.settings = settings;
      
      header.setStyleName(GWT_CALENDAR_HEADER_STYLE);
      dayPanel.setStyleName(DAY_CELL_CONTAINER_STYLE);
      weekPanel.setStyleName(WEEK_CELL_CONTAINER_STYLE);
      timePanel.setWidth("100%");
      
      showWeekNumbers = settings.getSettings().isShowingWeekNumbers();
      
      header.insertRow(0);
      header.insertRow(0);
      header.insertCell(0, 0);
      header.insertCell(0, 0);
      header.insertCell(0, 0);
      header.setWidget(0, 1, timePanel);
      header.getCellFormatter().setStyleName(0, 0, YEAR_CELL_STYLE);
      header.getCellFormatter().setWidth(0, 2,
                                         WindowUtils.getScrollBarWidth(true) +
                                            "px");
      // header.getCellFormatter().setStyleName(1, 0,SPLITTER_STYLE);

      header.getFlexCellFormatter().setColSpan(1, 0, 3);
      header.setCellPadding(0);
      header.setBorderWidth(0);
      header.setCellSpacing(0);
      
      if (showWeekNumbers) {
    	  timePanel.add(weekPanel);
      }
      timePanel.add(dayPanel);

      splitter.setStylePrimaryName(SPLITTER_STYLE);
      header.setWidget(1, 0, splitter);
   }

   public void setDays(Date date, int days) {

		dayPanel.clear();
		weekPanel.clear();

		float dayWidth = 100f / days;
		float dayLeft;
		int week = DateUtils.calendarWeekIso(date);
		int previousDayWeek = week;
		Date previousDate = date;
		float weekWidth = 0f;
		float weekLeft = 0f;

		for (int i = 0; i < days; i++) {

			// set the left position of the day splitter to
			// the width * incremented value
			dayLeft = dayWidth * i;

			// increment the date by 1
			if (i > 0) {
				DateUtils.moveOneDayForward(date);
			} else {
				// initialize the week values
				weekLeft = dayLeft;
				weekWidth = dayWidth;
			}

			// String headerTitle = DAY_LIST[date.getDay()] + ", "
			// + MONTH_LIST[date.getMonth()] + " " + date.getDate();

			String headerTitle = CalendarFormat.INSTANCE.getDateFormat()
					.format(date);

			Label dayLabel = new Label();
			dayLabel.setStylePrimaryName("day-cell");
			dayLabel.setWidth(dayWidth + "%");
			dayLabel.setText(headerTitle);
			DOM.setStyleAttribute(dayLabel.getElement(), "left", dayLeft + "%");
			
			addDayClickHandler(dayLabel, (Date) date.clone());

			boolean found = false;
			for (Date day : settings.getSettings().getHolidays()) {
				if (DateUtils.areOnTheSameDay(day, date)) {
					dayLabel.setStyleName("day-cell-holiday");
					found = true;
					break;
				}
			}
			
			// set the style of the header to show that it is today
			if (DateUtils.areOnTheSameDay(new Date(), date)) {
				dayLabel.setStyleName("day-cell-today");
			} else if (!found && DateUtils.isWeekend(date)) {
				dayLabel.setStyleName("day-cell-weekend");
			}
			
			if (showWeekNumbers) {
				week = DateUtils.calendarWeekIso(date);
				boolean lastDay = i + 1 == days;
				if ((previousDayWeek != week) || lastDay) {
					if (lastDay) {
						previousDayWeek = week;
						previousDate = date;
					}
					String weekTitle = "W " + previousDayWeek;

					Label weekLabel = new Label();
					weekLabel.setStylePrimaryName("week-cell");
					weekLabel.setWidth(weekWidth + "%");
					weekLabel.setText(weekTitle);
					DOM.setStyleAttribute(weekLabel.getElement(), "left",
							weekLeft + "%");
					
					addWeekClickHandler(weekLabel, previousDate);

					weekPanel.add(weekLabel);

					weekWidth = dayWidth;
					weekLeft = dayLeft + dayWidth;
				} else {
					weekWidth += dayWidth;
				}
				previousDayWeek = week;
				previousDate = date;
			}

			dayPanel.add(dayLabel);
		}
	}

	public void setYear(Date date) {
		setYear(DateUtils.year(date));
	}

	public void setYear(int year) {
		header.setText(0, 0, String.valueOf(year));
	}

	private void addDayClickHandler(final Label dayLabel, final Date day) {
		dayLabel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				fireSelectedDay(day);
			}
		});
	}
	
	private void addWeekClickHandler(final Label weekLabel, final Date day) {
		weekLabel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				fireSelectedWeek(day);
			}
		});
	}
	
	private void fireSelectedDay(final Date day) {
		DaySelectionEvent.fire(this, day);
	}
	
	private void fireSelectedWeek(final Date day) {
		WeekSelectionEvent.fire(this, day);
	}

	public HandlerRegistration addWeekSelectionHandler(
			WeekSelectionHandler<Date> handler) {
		return addHandler(handler, WeekSelectionEvent.getType());
	}

	public HandlerRegistration addDaySelectionHandler(
			DaySelectionHandler<Date> handler) {
		return addHandler(handler, DaySelectionEvent.getType());
	}
}


