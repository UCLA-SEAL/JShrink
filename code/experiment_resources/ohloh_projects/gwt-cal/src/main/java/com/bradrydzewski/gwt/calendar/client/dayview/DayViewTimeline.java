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

import com.bradrydzewski.gwt.calendar.client.CalendarFormat;
import com.bradrydzewski.gwt.calendar.client.HasSettings;
import com.bradrydzewski.gwt.calendar.client.util.FormattingUtil;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public /**
         * The Timeline Class is a sequential display of the hours in a day. Each
         * hour label should visually line up to a cell in the DayGrid.
         * 
         * @author Brad
         */
        class DayViewTimeline extends Composite {

    //private static final int MINUTES_PER_HOUR = 60;
    private static final int HOURS_PER_DAY = 24;
//	private int intervalsPerHour = settings.getIntervalsPerHour();//2; //30 minute intervals
//	private float intervalSize = settings.getPixelsPerInterval();//25f; //25 pixels per interval
    private AbsolutePanel timelinePanel = new AbsolutePanel();
    private HasSettings settings = null;
    private static final String TIME_LABEL_STYLE = "hour-label";
//    private final String[] HOURS = new String[]{"12 DEFAULT_AM_LABEL", "1 DEFAULT_AM_LABEL", "2 DEFAULT_AM_LABEL", "3 DEFAULT_AM_LABEL",
//        "4 DEFAULT_AM_LABEL", "5 DEFAULT_AM_LABEL", "6 DEFAULT_AM_LABEL", "7 DEFAULT_AM_LABEL", "8 DEFAULT_AM_LABEL", "9 DEFAULT_AM_LABEL", "10 DEFAULT_AM_LABEL",
//        "11 DEFAULT_AM_LABEL", "Noon", "1 DEFAULT_PM_LABEL", "2 DEFAULT_PM_LABEL", "3 DEFAULT_PM_LABEL", "4 DEFAULT_PM_LABEL", "5 DEFAULT_PM_LABEL",
//        "6 DEFAULT_PM_LABEL", "7 DEFAULT_PM_LABEL", "8 DEFAULT_PM_LABEL", "9 DEFAULT_PM_LABEL", "10 DEFAULT_PM_LABEL", "11 DEFAULT_PM_LABEL"
//    };
//    private final String[] HOURS = new String[]{"12", "1", "2", "3",
//        "4", "5", "6", "7", "8", "9", "10",
//        "11", "Noon", "1", "2", "3", "4", "5",
//        "6", "7", "8", "9", "10", "11"};
//    private final String DEFAULT_AM_LABEL = " DEFAULT_AM_LABEL";
//    private final String DEFAULT_PM_LABEL = " DEFAULT_PM_LABEL";

    public DayViewTimeline(HasSettings settings) {
        initWidget(timelinePanel);
        timelinePanel.setStylePrimaryName("time-strip");
        this.settings = settings;
        prepare();
    }

    public void prepare() {
        timelinePanel.clear();
        float labelHeight = 
                settings.getSettings().getIntervalsPerHour() * 
                settings.getSettings().getPixelsPerInterval();
        //float timeineHeight = labelHeight * HOURS_PER_DAY;
        //this.setHeight(timeineHeight+"px");



        int i = 0;
        if (settings.getSettings().isOffsetHourLabels()) {

            i = 1;
            SimplePanel sp = new SimplePanel();
            sp.setHeight((labelHeight / 2) + "px");
            timelinePanel.add(sp);
        }

        //boolean includeAMPM = !CalendarFormat.INSTANCE.getAm().equals("");
        
        while (i < CalendarFormat.HOURS_IN_DAY) {

            String hour = CalendarFormat.INSTANCE.getHourLabels()[i];
            i++;

            //block
            SimplePanel hourWrapper = new SimplePanel();
            hourWrapper.setStylePrimaryName(TIME_LABEL_STYLE);
            hourWrapper.setHeight((labelHeight + FormattingUtil.getBorderOffset()) + "px");

            FlowPanel flowPanel = new FlowPanel();
            flowPanel.setStyleName("hour-layout");
            
            String amPm = " ";
            if (i < 13) {
                amPm += CalendarFormat.INSTANCE.getAm();
            } else if (i > 13) {
                amPm += CalendarFormat.INSTANCE.getPm();
            } else {
				if (CalendarFormat.INSTANCE.isUseNoonLabel()) {
					hour = CalendarFormat.INSTANCE.getNoon();
					amPm = "";
				} else {
					amPm += CalendarFormat.INSTANCE.getPm();
				}
            }
            
            Label hourLabel = new Label(hour);
            hourLabel.setStylePrimaryName("hour-text");
            flowPanel.add(hourLabel);
            
            Label ampmLabel = new Label(amPm);
            ampmLabel.setStylePrimaryName("ampm-text");
            //if(includeAMPM)
            	flowPanel.add(ampmLabel);

            hourWrapper.add(flowPanel);
            
            timelinePanel.add(hourWrapper);
        }
    }
}

