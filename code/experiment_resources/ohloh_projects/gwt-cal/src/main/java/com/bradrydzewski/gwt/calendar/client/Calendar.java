/*
 * This file is part of gwt-cal
 * Copyright (C) 2009  Scottsdale Software LLC
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

import com.bradrydzewski.gwt.calendar.client.agenda.AgendaView;
import com.bradrydzewski.gwt.calendar.client.dayview.DayView;
import com.bradrydzewski.gwt.calendar.client.monthview.MonthView;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;

public class Calendar extends CalendarWidget implements RequiresResize, ProvidesResize {

    /**
     * The component to manage the presentation of appointments as a list.
     */
    private AgendaView agendaView = null;

	/**
     * The component to manage the presentation of appointments in a single day
     * layout.
     */
    private DayView dayView = null;

    /**
     * The component to manage the presentation of appointments in a month.
     */
    private MonthView monthView = null;

    /**
     * Constructs a <code>Calendar</code> with the DayView currently
     * displayed.
     */
    public Calendar() {
        this(CalendarViews.DAY, CalendarSettings.DEFAULT_SETTINGS);
    }

    public Calendar(CalendarSettings settings) {
    	this(CalendarViews.DAY, settings);
    }
    
    /**
     * Constructs a <code>Calendar</code> with the given
     * CalendarView displayed by default.
     */
    public Calendar(CalendarViews view, CalendarSettings settings) {
        super();
        this.setSettings(settings);
        setView(view);
    }

    /**
     * Constructs a <code>Calendar</code> with the a user-defined
     * CalendarView displayed by default.
     */
    public Calendar(CalendarView view) {
        super();
        setView(view);
    }

    /**
     * Sets the CalendarView that should be used by the Calendar to display
     * the list of appointments.
     * @param view
     */
    public void setView(CalendarViews view) {
        setView(view, getDays());
    }

    /**
     * Sets the current view of this calendar.
     *
     * @param view The ID of a view used to visualize the appointments managed
     *             by the calendar
     * @param days The number of days to display in the view, which can be
     *             ignored by some views.
     */
    public void setView(CalendarViews view, int days) {
        switch (view) {
            case DAY: {
                if (dayView == null)
                    dayView = new DayView();
                dayView.setDisplayedDays(days);
                setView(dayView);
                break;
            }
            case AGENDA: {
                //TODO: need to cache agendaView, but there is a layout bug after a calendar item is deleted.
//                agendaView = new AgendaView();
//                setView(agendaView);
//            	break;
                throw new RuntimeException("Agenda View is not yet supported");
            }
            case MONTH: {
            	if(monthView==null)
            		monthView = new MonthView();
                setView(monthView);
                break;
            }
        }
    }
    

    
    
	public void onResize() {
		resizeTimer.schedule(500);
	}
	
    
    private Timer resizeTimer = new Timer() {
    	/**
    	 * Snapshot of the Calendar's height at the last time
    	 * it was resized.
    	 */
    	private int height;

        @Override
        public void run() {

            int newHeight = getOffsetHeight();
            if (newHeight != height) {
                height = newHeight;
                doSizing();
                if(getView() instanceof MonthView)
                	doLayout();
            }
        }
    };
}
 