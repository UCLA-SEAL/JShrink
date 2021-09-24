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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.bradrydzewski.gwt.calendar.client.DateUtils;
import com.bradrydzewski.gwt.calendar.client.HasSettings;
import com.bradrydzewski.gwt.calendar.client.util.AppointmentUtil;

/**
 * Responsible for arranging all Appointments, visually, on a screen in a manner
 * similar to the Microsoft Outlook / Windows Vista calendar. 
 * See: <img src='http://www.microsoft.com/library/media/1033/athome/images/moredone/calendar.gif'/>
 * <p>
 * Note how overlapping appointments are displayed in the provided image
 * 
 * @author Brad Rydzewski
 * @version 1.0 6/07/09
 * @since 1.0
 */
public class DayViewLayoutStrategy {

    private static final int MINUTES_PER_HOUR = 60;
    private static final int HOURS_PER_DAY = 24;
    private HasSettings settings = null;

    public DayViewLayoutStrategy(HasSettings settings) {
        this.settings = settings;
    }

    public ArrayList<AppointmentAdapter> doLayout(List<Appointment> appointments, int dayIndex, int dayCount) {


        int intervalsPerHour = settings.getSettings().getIntervalsPerHour(); //30 minute intervals
        float intervalSize = settings.getSettings().getPixelsPerInterval(); //25 pixels per interval

        /*
         * Note: it is important that all appointments are sorted by Start date
         * (asc) and Duration (desc) for this algorithm to work. If that is not
         * the case, it won't work, at all!! Maybe this is a problem that needs
         * to be addressed
         */

        // set to 30 minutes. this means there will be 48 cells. 60min / 30min
        // interval * 24
        //int minutesPerInterval = 30;
        // interval size, set to 100px
        //float sizeOfInterval = 25f;

        // a calendar can view multiple days at a time. sets number of visible
        // days
        // TODO: use this later, not currently implemented
        // float numberOfDays = dates.size();

        int minutesPerInterval = MINUTES_PER_HOUR / intervalsPerHour;

        // get number of cells (time blocks)
        int numberOfTimeBlocks = MINUTES_PER_HOUR / minutesPerInterval * HOURS_PER_DAY;
        TimeBlock[] timeBlocks = new TimeBlock[numberOfTimeBlocks];

        for (int i = 0; i < numberOfTimeBlocks; i++) {
            TimeBlock t = new TimeBlock();
            t.setStart(i * minutesPerInterval);
            t.setEnd(t.getStart() + minutesPerInterval);
            t.setOrder(i);
            t.setTop((float) i * intervalSize);
            t.setBottom(t.getTop() + intervalSize);
            timeBlocks[i] = t;
        }

        // each appointment will get "wrapped" in an appoinetment cell object,
        // so that we can assign it a location in the grid, row and
        // column span, etc.
        ArrayList<AppointmentAdapter> appointmentCells = new ArrayList<AppointmentAdapter>();
        // Map<TimeBlock,TimeBlock> blockGroup = new
        // HashMap<TimeBlock,TimeBlock>();
        int groupMaxColumn = 0; // track total columns here! this will reset
        // when a group completes
        int groupStartIndex = -1;
        int groupEndIndex = -2;

        // Question: how to distinguish start / finish of a new group?
        // Answer: when endCell of previous appointments < startCell of new
        // appointment

        // for each appointments, we need to see if it intersects with each time
        // block
        for (Appointment appointment : appointments) {

            TimeBlock startBlock = null;
            TimeBlock endBlock = null;

            // if(blockGroupEndCell)

            // wrap appointment with AppointmentInterface Cell and add to list
            AppointmentAdapter apptCell = new AppointmentAdapter(appointment);
            appointmentCells.add(apptCell);

            // get the first time block in which the appointment should appear
            // TODO: since appointments are sorted, we should never need to
            // re-evaluate a time block that had zero matches...
            // store the index of the currently evaluated time block, if no
            // match, increment
            // that will prevent the same block from ever being re-evaluated
            // after no match found
            for (TimeBlock block : timeBlocks) {
                // does the appointment intersect w/ the block???
                if (block.intersectsWith(apptCell)) {

                    // we found one! set as start block and exit loop
                    startBlock = block;
                    // blockGroup.put(block, block);

                    if (groupEndIndex < startBlock.getOrder()) {

                        //System.out.println("   prior group max cols: "
                        //		+ groupMaxColumn);

                        for (int i = groupStartIndex; i <= groupEndIndex; i++) {

                            TimeBlock tb = timeBlocks[i];
                            tb.setTotalColumns(groupMaxColumn + 1);
                        //System.out.println("     total col set for block: "
                        //		+ i);
                        }
                        groupStartIndex = startBlock.getOrder();
                        //System.out.println("new group at: " + groupStartIndex);
                        groupMaxColumn = 0;
                    }

                    break;
                } else {
                    // here is where I would increment, as per above to-do
                }
            }

            // add the appointment to the start block
            startBlock.getAppointments().add(apptCell);
            // add block to appointment
            apptCell.getIntersectingBlocks().add(startBlock);

            // set the appointments column, if it has not already been set
            // if it has been set, we need to get it for reference later on in
            // this method
            int column = startBlock.getFirstAvailableColumn();
            apptCell.setColumnStart(column);
            apptCell.setColumnSpan(1); // hard-code to 1, for now

            // we track the max column for a time block
            // if a column get's added make sure we increment
            // if (startBlock.getTotalColumns() <= column) {
            // startBlock.setTotalColumns(column+1);
            // }

            // add column to block's list of occupied columns, so that the
            // column cannot be given to another appointment
            startBlock.getOccupiedColumns().put(column, column);

            // sets the start cell of the appt to the current block
            // we can do this since the blocks are ordered ascending
            apptCell.setCellStart(startBlock.getOrder());

            // go through all subsequent blocks...
            // find intersections
            for (int i = startBlock.getOrder() + 1; i < timeBlocks.length; i++) {

                // get the nextTimeBlock
                TimeBlock nextBlock = timeBlocks[i];

                // exit look if end date > block start, since no subsequent
                // blocks will ever intersect
                // if (apptCell.getAppointmentEnd() > nextBlock.getStart()) {
                // break; //does appt intersect with this block?
                // }
                if (nextBlock.intersectsWith(apptCell)) {

                    // yes! add appointment to the block
                    // register start column
                    nextBlock.getAppointments().add(apptCell);
                    nextBlock.getOccupiedColumns().put(column, column);
                    endBlock = nextBlock; // this may change if intersects with
                    // next block

                    // add block to appointments list of intersecting blocks
                    apptCell.getIntersectingBlocks().add(nextBlock);

                // we track the max column for a time block
                // if a column get's added make sure we increment
                // if (nextBlock.getTotalColumns() <= column) {
                // nextBlock.setTotalColumns(column+1);
                // }

                // blockGroup.put(nextBlock, nextBlock);
                }
            }

            // if end block was never set, use the start block
            endBlock = (endBlock == null) ? startBlock : endBlock;
            // maybe here is the "end" of a group, where we then evaluate max
            // column

            if (column > groupMaxColumn) {
                groupMaxColumn = column;
            // System.out.println("  max col: " + groupMaxColumn);
            }

            if (groupEndIndex < endBlock.getOrder()) {
                groupEndIndex = endBlock.getOrder();
            //System.out.println("  end index (re)set: " + groupEndIndex);
            }
            // for(int i = groupStartIndex; i<=groupEndIndex; i++) {
            // timeBlocks[i].setTotalColumns(groupMaxColumn);
            // }
            // groupMaxColumn=1;
            // }

            // for(TimeBlock timeBlock : blockGroup.values()) {
            //    
            // }

            // blockGroup = new HashMap<TimeBlock,TimeBlock>();

            // set the appointments cell span (top to bottom)
            apptCell.setCellSpan(endBlock.getOrder() - startBlock.getOrder() + 1);

        }
        for (int i = groupStartIndex; i <= groupEndIndex; i++) {

            TimeBlock tb = timeBlocks[i];
            tb.setTotalColumns(groupMaxColumn + 1);
        //System.out.println("     total col set for block: " + i);
        }
        // we need to know the MAX number of cells for each time block.
        // so unfortunately we have to go back through the list to find this out
		/*
         * for(AppointmentCell apptCell : appointmentCells) {
         * 
         * for (TimeBlock block : apptCell.getIntersectingBlocks()) {
         * 
         * int maxCol = 0;
         * 
         * //find the max cell for (AppointmentCell apptCell :
         * block.getAppointments()) { int col = apptCell.getColumnStart(); if
         * (col > maxCol) { maxCol = col; } }
         * 
         * block.setTotalColumns(maxCol+1); } }
         */


        //last step is to calculate the adjustment reuired for 'multi-day' / multi-column
        float widthAdj = 1f / dayCount;

        float paddingLeft = .5f;
        float paddingRight = .5f;
        float paddingBottom = 2;

        // now that everything has been assigned a cell, column and spans
        // we can calculate layout
        // Note: this can only be done after every single appointment has
        // been assigned a position in the grid
        for (AppointmentAdapter apptCell : appointmentCells) {

            float width = 1f / (float) apptCell.getIntersectingBlocks().get(0).getTotalColumns() * 100;
            float left = (float) apptCell.getColumnStart() / (float) apptCell.getIntersectingBlocks().get(0).getTotalColumns() * 100;

            //AppointmentInterface appt = apptCell.getAppointment();
            apptCell.setTop((float) apptCell.getCellStart() * intervalSize); // ok!
            apptCell.setLeft((widthAdj * 100 * dayIndex) + (left * widthAdj) + paddingLeft); // ok
            apptCell.setWidth(width * widthAdj - paddingLeft - paddingRight); // ok!
            apptCell.setHeight((float) apptCell.getIntersectingBlocks().size() * ((float) intervalSize) - paddingBottom); // ok!

            float apptStart = apptCell.getAppointmentStart();
            float apptEnd = apptCell.getAppointmentEnd();
            float blockStart = timeBlocks[apptCell.getCellStart()].getStart();
            float blockEnd = timeBlocks[apptCell.getCellStart() + apptCell.getCellSpan() - 1].getEnd();
            float blockDuration = blockEnd - blockStart;
            float apptDuration = apptEnd - apptStart;
            float timeFillHeight = apptDuration / blockDuration * 100f;
            float timeFillStart = (apptStart - blockStart) / blockDuration * 100f;
//			System.out.println("apptStart: "+apptStart);
//			System.out.println("apptEnd: "+apptEnd);
//			System.out.println("blockStart: "+blockStart);
//			System.out.println("blockEnd: "+blockEnd);
//			System.out.println("timeFillHeight: "+timeFillHeight);
            //System.out.println("timeFillStart: "+timeFillStart);
            //System.out.println("------------");
            apptCell.setCellPercentFill(timeFillHeight);
            apptCell.setCellPercentStart(timeFillStart);
            //appt.formatTimeline(apptCell.getCellPercentStart(), apptCell.getCellPercentFill());
        }

        return appointmentCells;
    }

    public int doMultiDayLayout(ArrayList<Appointment> appointments, ArrayList<AppointmentAdapter> adapters, Date start, int days) {


        //create array to hold all appointments for a particular day
        //HashMap<Date, ArrayList<AppointmentAdapter>> appointmentDayMap 
        //        = new HashMap<Date, ArrayList<AppointmentAdapter>>();

        //for a particular day need to track all used rows
        HashMap<Integer, HashMap<Integer, Integer>> daySlotMap = new HashMap<Integer, HashMap<Integer, Integer>>();

        int minHeight = 30;
        int maxRow = 0;


        //convert appointment to adapter
        for (Appointment appointment : appointments) {
            adapters.add(new AppointmentAdapter(appointment));
        }

        //create array of dates
        ArrayList<Date> dateList = new ArrayList<Date>();
        Date tempStartDate = (Date)start.clone();
        
//        tempStartDate.setHours(0);
//        tempStartDate.setMinutes(0);
//        tempStartDate.setSeconds(0);
        for (int i = 0; i < days; i++) {
            Date d = (Date) tempStartDate.clone();
            DateUtils.resetTime(d);
            //appointmentDayMap.put(d, new ArrayList<AppointmentAdapter>());
            daySlotMap.put(i, new HashMap<Integer, Integer>());
            dateList.add(d);
            DateUtils.moveOneDayForward(tempStartDate);
        }


        //add appointments to each day
        for (AppointmentAdapter adapter : adapters) {

            int columnSpan = 0; //number of columns spanned
            boolean isStart = true; //indicates if current column is appointment start column

            //set column & span
            for (int i = 0; i < dateList.size(); i++) {
                Date date = dateList.get(i);
                boolean isWithinRange =
                        AppointmentUtil.rangeContains(
                        adapter.getAppointment(), date);

                //System.out.println("    isWithinRange == " + isWithinRange + " for start: " + adapter.getAppointment().getStart() + " end: " + adapter.getAppointment().getEnd() + " date: "+date);

                //while we are at it, we can set the adapters start column
                // and colun span
                if (isWithinRange) {
                    //appointmentDayMap.get(date).add(adapter);

                    if (isStart) {
                        adapter.setColumnStart(i);
                        isStart = false;
                    }

                    adapter.setColumnSpan(columnSpan);
                    columnSpan++;
                }
            }

            //now we set the row, which cannot be more than total # of appointments
            for (int x = 0; x < adapters.size(); x++) {

                boolean isRowOccupied = false;
                for (int y = adapter.getColumnStart(); y <= adapter.getColumnStart() + adapter.getColumnSpan(); y++) {
                    try{
                	HashMap<Integer, Integer> rowMap = daySlotMap.get(y);
                    if (rowMap.containsKey(x)) {
                        isRowOccupied = true;
                    //System.out.println("    row [" + x+"] is occupied for day [" + y+"]");
                    } else {
                        //isRowOccupied = false;
                        break; //break out of loop, nothing found in row slot
                    }
                    }catch(Exception ex) {
                    	System.out.println("Exception: y=" + y + " x=" + x + " adapters.size=" + adapters.size() + " start="+adapter.getAppointment().getStart() + " end="+adapter.getAppointment().getEnd().toString());
                    }
                }

                if (!isRowOccupied) {
                    //add row to maps
                    for (int y = adapter.getColumnStart(); y <= adapter.getColumnStart() + adapter.getColumnSpan(); y++) {
                        HashMap<Integer, Integer> rowMap = daySlotMap.get(y);
                        rowMap.put(x, x);
                        if (x > maxRow) {
                            maxRow = x;
                        }
                    //System.out.println("    > added "+ x + " to row list for column " + y);
                    }
                    //set the row (also named cell)
                    adapter.setCellStart(x);
                    //break loop


                    //now we set the appointment's location
                    //Appointment appt = adapter.getAppointment();
                    float top = adapter.getCellStart() * 25f + 5f;
                    float width = ((float) adapter.getColumnSpan() + 1f) / days * 100f - 1f;//10f=padding
                    float left = ((float) adapter.getColumnStart()) / days * 100f + .5f;//10f=padding
                    //float left = (float) adapter.getColumnStart() / (float) apptCell.getIntersectingBlocks().get(0).getTotalColumns() * 100;
                    adapter.setWidth(width);
                    adapter.setLeft(left);
                    adapter.setTop(top);
                    adapter.setHeight(20);
                    //System.out.println("set appointment [" + appt.getTitle() + "]  layout left: " + left + " top: " + top + " width: " + width);
                    break;
                }
            }

        //System.out.println("multi-day layout -- title: \"" + adapter.getAppointment().getTitle() + "\" | col start: " + adapter.getColumnStart() + " | colspan: " + adapter.getColumnSpan() + " | row: " + adapter.getCellStart() + " | start date: " + adapter.getAppointment().getStart() + " | end date: " + adapter.getAppointment().getEnd());


        }

        int height = (maxRow+1) * 25 + 5;
        return Math.max(height, minHeight);
    }
    

    
}
