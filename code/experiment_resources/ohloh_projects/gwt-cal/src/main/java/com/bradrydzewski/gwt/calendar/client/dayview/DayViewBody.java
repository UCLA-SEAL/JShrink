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

import com.bradrydzewski.gwt.calendar.client.HasSettings;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class DayViewBody extends Composite {
   private FlexTable layout = new FlexTable();
   private ScrollPanel scrollPanel = new ScrollPanel();
   private DayViewTimeline timeline = null;
   private DayViewGrid grid = null;
   private HasSettings settings = null;

   public void add(Widget w) {
      scrollPanel.add(w);
   }

   public ScrollPanel getScrollPanel() {
      return scrollPanel;
   }

   public DayViewGrid getGrid() {
      return grid;
   }

   public DayViewTimeline getTimeline() {
      return timeline;
   }

   public DayViewGrid getDayViewGrid() {
      return grid;
   }

   public DayViewTimeline getDayViewTimeline() {
      return timeline;
   }

   public DayViewBody(HasSettings settings) {
      initWidget(scrollPanel);
      this.settings = settings;
      this.timeline = new DayViewTimeline(settings);
      this.grid = new DayViewGrid(settings);
      scrollPanel.setStylePrimaryName("scroll-area");
      DOM.setStyleAttribute(scrollPanel.getElement(), "overflowX",
                            "hidden");
      DOM.setStyleAttribute(scrollPanel.getElement(), "overflowY",
                            "scroll");

      // create the calendar body layout table
      // calendarBodyLayoutTable.setStyleName("scroll-area");
      layout.setCellPadding(0);
      layout.setBorderWidth(0);
      layout.setCellSpacing(0);
      layout.getColumnFormatter().setWidth(1, "99%");
      // set vertical alignment
      VerticalAlignmentConstant valign = HasVerticalAlignment.ALIGN_TOP;
      layout.getCellFormatter().setVerticalAlignment(0, 0, valign);
      layout.getCellFormatter().setVerticalAlignment(0, 1, valign);

      // grid.build(8, 17, 1);
      grid.setStyleName("gwt-appointment-panel");
      // timeline.prepare();


      //TODO: use CSS to set table layout
      layout.getCellFormatter().setWidth(0, 0, "50px");
      DOM.setStyleAttribute(layout.getElement(), "tableLayout", "fixed");

      layout.setWidget(0, 0, timeline);
      layout.setWidget(0, 1, grid);
      scrollPanel.add(layout);

   }

   public void setDays(Date date, int days) {
      grid.build(settings.getSettings().getWorkingHourStart(),
                 settings.getSettings().getWorkingHourEnd(), days);
   }
}
