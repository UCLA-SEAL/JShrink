/*
 * This file is part of gwt-cal
 * Copyright (C) 2009  Brad Rydzewski
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarSettings {

    public static CalendarSettings DEFAULT_SETTINGS = new CalendarSettings();
    private int pixelsPerInterval = 30; //IE6 cannot be less than 20!!!!! 
    private int intervalsPerHour = 2;
    private int workingHourStart = 8;
    private int workingHourEnd = 17;
    private int scrollToHour = 8; //default hour that gets scrolled to
    private boolean enableDragDrop = true;
    private boolean offsetHourLabels = false;
    private boolean showWeekNumbers = false;
    private boolean dragDropCreation = true;
    private boolean showMultiDay = true;
    private List<Date> holidays = new ArrayList<Date>();

    /*
     * Clicks required to fire TimeBlockClickEvent.
     */
    private Click timeBlockClickNumber = Click.Single;

    public CalendarSettings() {
    }

    public int getPixelsPerInterval() {
        return pixelsPerInterval;
    }

    public void setPixelsPerInterval(int px) {
        pixelsPerInterval = px;
    }

    public int getIntervalsPerHour() {
        return intervalsPerHour;
    }

    public void setIntervalsPerHour(int intervals) {
        intervalsPerHour = intervals;
    }

    public int getWorkingHourStart() {
        return workingHourStart;
    }

    public void setWorkingHourStart(int start) {
        workingHourStart = start;
    }

    public int getWorkingHourEnd() {
        return workingHourEnd;
    }

    public void setWorkingHourEnd(int end) {
        workingHourEnd = end;
    }

    public int getScrollToHour() {
        return scrollToHour;
    }

    public void setScrollToHour(int hour) {
        scrollToHour = hour;
    }

    public boolean isEnableDragDrop() {
        return enableDragDrop;
    }

    public void setEnableDragDrop(boolean enableDragDrop) {
        this.enableDragDrop = enableDragDrop;
    }

    public boolean isOffsetHourLabels() {
        return offsetHourLabels;
    }

    public void setOffsetHourLabels(boolean offsetHourLabels) {
        this.offsetHourLabels = offsetHourLabels;
    }

    public Click getTimeBlockClickNumber() {
        return timeBlockClickNumber;
    }

    public void setTimeBlockClickNumber(Click timeBlockClickNumber) {
        this.timeBlockClickNumber = timeBlockClickNumber;
    }
    
    public void setEnableDragDropCreation(boolean dragDropCreation) {
    	this.dragDropCreation = dragDropCreation;
    }

    public boolean getEnableDragDropCreation() {
    	return dragDropCreation;
    }
    
    /**
     * 
     * @param showWeekNumbers
     * @since 0.9.4
     */
    public void setShowWeekNumbers(boolean showWeekNumbers) {
    	this.showWeekNumbers = showWeekNumbers;
    }
    
    /**
     * 
     * @since 0.9.4
     */
    public boolean isShowingWeekNumbers() {
    	return showWeekNumbers;
    }
    
    /**
     * 
     * @since 0.9.4
     */
    public void setHolidays(List<Date> holidays) {
    	this.holidays = holidays;
    }
    
    /**
     * 
     * @since 0.9.4
     */
    public List<Date> getHolidays() {
    	return holidays;
    }
    
    /**
     * 
     * @since 0.9.4
     */
    public void setShowMultiday(boolean visible) {
    	this.showMultiDay = visible;
    }
    
    /**
     * 
     * @since 0.9.4
     */
    public boolean isMultidayVisible() {
    	return showMultiDay;
    }
    
    public enum Click {
        Double, Single, Drag
    }
}
