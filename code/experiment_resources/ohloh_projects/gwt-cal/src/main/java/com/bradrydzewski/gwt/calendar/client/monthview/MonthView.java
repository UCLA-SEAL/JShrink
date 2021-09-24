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

package com.bradrydzewski.gwt.calendar.client.monthview;

import static com.bradrydzewski.gwt.calendar.client.DateUtils.moveOneDayForward;
import static com.bradrydzewski.gwt.calendar.client.monthview.MonthViewDateUtils.firstDateShownInAMonthView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.MonthViewDropController;
import com.allen_sauer.gwt.dnd.client.drop.MonthViewPickupDragController;
import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.bradrydzewski.gwt.calendar.client.CalendarFormat;
import com.bradrydzewski.gwt.calendar.client.CalendarSettings.Click;
import com.bradrydzewski.gwt.calendar.client.CalendarView;
import com.bradrydzewski.gwt.calendar.client.CalendarWidget;
import com.bradrydzewski.gwt.calendar.client.DateUtils;
import com.bradrydzewski.gwt.calendar.client.event.HasDaySelectionHandlers;
import com.bradrydzewski.gwt.calendar.client.event.HasWeekSelectionHandlers;
import com.bradrydzewski.gwt.calendar.client.util.FormattingUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>
 * A CalendarView that displays appointments for a given month. The Month is
 * displayed in a grid-style view where cells represents days, columns
 * represents days of the week (i.e. Monday, Tuesday, etc.) and rows represent a
 * full week (Sunday through Saturday).
 * <p/>
 * <p/>
 * <h3>CSS Style Rules</h3> <ul class='css'>
 * <li>.gwt-cal-MonthView { }</li>
 * <li>.dayCell { cell that represents a day }</li>
 * <li>.dayCell-today { cell that represents today }</li>
 * <li>.dayCell-disabled { cell's day falls outside the month }</li>
 * <li>.dayCell-today-disabled { cell represents today, falls outside the month
 * }</li>
 * <li>.dayCellLabel { header for the cell }</li>
 * <li>.dayCellLabel-today { cell represents today }</li>
 * <li>.dayCellLabel-disabled { cell's day falls outside the month }</li>
 * <li>.dayCellLabel-today-disabled { cell represents today, falls outside the
 * month }</li>
 * <li>.weekDayLabel { label for the days of the week }</li> </ul>
 *
 * @author Brad Rydzewski
 * @since 0.9.0
 */
public class MonthView extends CalendarView implements HasWeekSelectionHandlers<Date>, HasDaySelectionHandlers<Date> {

	public static final Comparator<Appointment> APPOINTMENT_COMPARATOR = new Comparator<Appointment>() {

		public int compare(Appointment a1, Appointment a2) {
			int compare = Boolean.valueOf(a2.isMultiDay()).compareTo(
					a1.isMultiDay());

			if (compare == 0) {
				compare = a1.getStart().compareTo(a2.getStart());
			}

			if (compare == 0) {
				compare = a2.getEnd().compareTo(a1.getEnd());
			}

			return compare;
		}
	};

	private static final int DAYS_IN_A_WEEK = 7;
	private final static String MONTH_VIEW = "gwt-cal-MonthView";
	private final static String CANVAS_STYLE = "canvas";
	private final static String GRID_STYLE = "grid";
	private final static String CELL_STYLE = "dayCell";
	private final static String MORE_LABEL_STYLE = "moreAppointments";
	private final static String CELL_HEADER_STYLE = "dayCellLabel";
	private final static String WEEKDAY_LABEL_STYLE = "weekDayLabel";
	private final static String WEEKNUMBER_LABEL_STYLE = "weekNumberLabel";


	/**
	 * List of appointment panels drawn on the month view canvas.
	 */
	private ArrayList<AppointmentWidget> appointmentsWidgets = new ArrayList<AppointmentWidget>();

	/**
	 * All appointments are placed on this canvas and arranged.
	 */
	private AbsolutePanel appointmentCanvas = new AbsolutePanel();

	/**
	 * All "+ n more" Labels, mapped to its cell in the MonthView Grid.
	 */
	private HashMap<Element, Integer> moreLabels = new HashMap<Element, Integer>();
	private ArrayList<Label> dayLabels = new ArrayList<Label>();
	private ArrayList<Widget> dayPanels = new ArrayList<Widget>();
	
	/**
	 * The first date displayed on the MonthView (1st cell.) This date is not
	 * necessarily the first date of the month as the month view will sometimes
	 * display days from the adjacent months because of the number of days
	 * fitting in the visible grid.
	 */
	private Date firstDateDisplayed;

	/**
	 * Grid that makes up the days and weeks of the MonthView.
	 */
	private FlexTable monthCalendarGrid = new FlexTable();

	/**
	 * The number of rows required to display the entire month in grid format.
	 * Although most months span a total of five weeks, there are some months
	 * that span six weeks.
	 */
	private int monthViewRequiredRows = 5;

	/**
	 * List of <code>AppointmentWidget</code>s that are associated to the
	 * currently selected <code>Appointment</code> appointment.
	 */
	private ArrayList<AppointmentWidget> selectedAppointmentWidgets = new ArrayList<AppointmentWidget>();

	private PickupDragController dragController = null;

	private MonthViewDropController monthViewDropController = null;
	
	private MonthViewStyleManager styleManager = GWT.create(MonthViewStyleManager.class);

	/**
	 * This method is called when the MonthView is attached to the Calendar and
	 * displayed. This is where all components are configured and added to the
	 * RootPanel.
	 */
	public void attach(CalendarWidget widget) {
		super.attach(widget);

		calendarWidget.addToRootPanel(monthCalendarGrid);
		monthCalendarGrid.setCellPadding(0);
		monthCalendarGrid.setBorderWidth(0);
		monthCalendarGrid.setCellSpacing(0);
		monthCalendarGrid.setStyleName(GRID_STYLE);

		calendarWidget.addToRootPanel(appointmentCanvas);
		appointmentCanvas.setStyleName(CANVAS_STYLE);

		selectedAppointmentWidgets.clear();

		if (dragController == null) {
			dragController = new MonthViewPickupDragController(appointmentCanvas, true);
			dragController.addDragHandler(new DragHandler() {

				public void onDragEnd(DragEndEvent event) {
                    Appointment appt = ((AppointmentWidget) event.getContext().draggable).getAppointment();

                    calendarWidget.setCommittedAppointment(appt);
                    calendarWidget.fireUpdateEvent(appt);
				}

				public void onDragStart(DragStartEvent event) {
					Appointment appt = ((AppointmentWidget) event.getContext().draggable).getAppointment();
					calendarWidget.setRollbackAppointment(appt.clone());
				}

				public void onPreviewDragEnd(DragEndEvent event)
						throws VetoDragException {
					// do nothing
				}

				public void onPreviewDragStart(DragStartEvent event)
						throws VetoDragException {
					// do nothing
				}
			});
		}

		/*
		 * Need to re-set appointmentCanvas to position:absolute because gwt-dnd
		 * will set it to relative, but then the layout gets f***ed up
		 */
		DOM.setStyleAttribute(appointmentCanvas.getElement(), "position",
				"absolute");

		dragController.setBehaviorDragStartSensitivity(5);
		dragController.setBehaviorDragProxy(true);

		// instantiate our drop controller
		monthViewDropController = new MonthViewDropController(
				appointmentCanvas, monthCalendarGrid);
		dragController.registerDropController(monthViewDropController);

	}

	/**
	 * Performs a Layout and arranges all appointments on the MonthView's
	 * appointment canvas.
	 */
	@Override
	public void doLayout() {
		// Clear all existing appointments
		appointmentCanvas.clear();
		monthCalendarGrid.clear();
		appointmentsWidgets.clear();
		moreLabels.clear();
		dayLabels.clear();
		dayPanels.clear();
		selectedAppointmentWidgets.clear();
		while (monthCalendarGrid.getRowCount() > 0) {
			monthCalendarGrid.removeRow(0);
		}

		// Rebuild the month grid
		buildCalendarGrid();

		// (Re)calculate some variables
		calculateCellHeight();
		calculateCellAppointments();

		// set variables needed by the drop controller
		// monthViewDropController.setDayHeaderHeight(calculatedDayHeaderHeight);
		monthViewDropController.setDaysPerWeek(DAYS_IN_A_WEEK);
		// monthViewDropController.setWeekdayHeaderHeight(calculatedWeekDayHeaderHeight);
		monthViewDropController.setWeeksPerMonth(monthViewRequiredRows);
		monthViewDropController.setFirstDateDisplayed(firstDateDisplayed);

		// Sort the appointments
		// TODO: don't re-sort the appointment unless necessary
		Collections.sort(calendarWidget.getAppointments(),
				APPOINTMENT_COMPARATOR);
      // Distribute appointments
		MonthLayoutDescription monthLayoutDescription = new
            MonthLayoutDescription( 
				   firstDateDisplayed, monthViewRequiredRows, 
				   calendarWidget.getAppointments(),
				   calculatedCellAppointments - 1);

		int dayIndex = 0;
		for (int row = 0; row < monthCalendarGrid.getRowCount() - 1; row++) {
			for (int col = 0; col < DAYS_IN_A_WEEK; col++) {
				Widget lbl = dayPanels.get(dayIndex);
				placeDayLabelInGrid(lbl, col, row);
				dayIndex++;
			}
		}
		
		// Get the layouts for each week in the month
		WeekLayoutDescription[] weeks = monthLayoutDescription
				.getWeekDescriptions();
		for (int weekOfMonth = 0; weekOfMonth < weeks.length
				&& weekOfMonth < monthViewRequiredRows; weekOfMonth++) {
			WeekLayoutDescription weekDescription = weeks[weekOfMonth];
			if (weekDescription != null) {
				layOnTopOfTheWeekHangingAppointments(
                    weekDescription, weekOfMonth);
				layOnWeekDaysAppointments(weekDescription, weekOfMonth);
			}
		}
	}

	private void layOnTopOfTheWeekHangingAppointments(
      WeekLayoutDescription weekDescription, int weekOfMonth) {
      AppointmentStackingManager weekTopElements
          = weekDescription.getTopAppointmentsManager();
		for (int layer = 0; layer < calculatedCellAppointments; layer++) {

			ArrayList<AppointmentLayoutDescription> descriptionsInLayer
            = weekTopElements.getDescriptionsInLayer(layer);

			if (descriptionsInLayer == null) {
				break;
			}

			for (AppointmentLayoutDescription weekTopElement : descriptionsInLayer) {
				layOnAppointment(weekTopElement.getAppointment(),
						weekTopElement.getWeekStartDay(), weekTopElement
								.getWeekEndDay(), weekOfMonth, layer);
			}
		}
	}

    private void layOnWeekDaysAppointments(WeekLayoutDescription week,
        int weekOfMonth) {

        AppointmentStackingManager topAppointmentManager = week
            .getTopAppointmentsManager();

        for (int dayOfWeek = 0; dayOfWeek < DAYS_IN_A_WEEK; dayOfWeek++) {
            DayLayoutDescription dayAppointments = week
                .getDayLayoutDescription(dayOfWeek);

            int appointmentLayer =
                topAppointmentManager.lowestLayerIndex(dayOfWeek);
            
            if (dayAppointments != null) {
                int count = dayAppointments.getAppointments().size();
                for (int i = 0; i < count; i++) {
                    Appointment appointment
                       = dayAppointments.getAppointments().get(i);
                    appointmentLayer = topAppointmentManager
                        .nextLowestLayerIndex(dayOfWeek,
                                              appointmentLayer);
                    if (appointmentLayer > calculatedCellAppointments - 1) {
                        int remaining = count + topAppointmentManager.multidayAppointmentsOverLimitOn(dayOfWeek) - i;
                        if ( remaining == 1 ) {
                           layOnAppointment(appointment, dayOfWeek, dayOfWeek,
                                            weekOfMonth, appointmentLayer);
                        } else {
                           layOnNMoreLabel(remaining, dayOfWeek, weekOfMonth);
                        }
                       break;
                    }
                    layOnAppointment(appointment, dayOfWeek, dayOfWeek,
                                     weekOfMonth, appointmentLayer);
                    appointmentLayer++;
                }
            } else if ( topAppointmentManager.multidayAppointmentsOverLimitOn(dayOfWeek) > 0 ) {
               layOnNMoreLabel(topAppointmentManager
                  .multidayAppointmentsOverLimitOn(dayOfWeek),
                               dayOfWeek, weekOfMonth);               
            }
        }
    }

   private void layOnNMoreLabel(int moreCount, int dayOfWeek, int weekOfMonth){
      Label more = new Label(CalendarFormat.MESSAGES.more(moreCount));
      more.setStyleName(MORE_LABEL_STYLE);
      placeItemInGrid(more, dayOfWeek, dayOfWeek,weekOfMonth,
                      calculatedCellAppointments);
      appointmentCanvas.add(more);
      moreLabels.put(more.getElement(),(dayOfWeek)+(weekOfMonth*7));
   }


	private void layOnAppointment(Appointment appointment, int colStart,
			int colEnd, int row, int cellPosition) {
		AppointmentWidget panel = new AppointmentWidget(appointment);

		placeItemInGrid(panel, colStart, colEnd, row, cellPosition);

		boolean selected = calendarWidget.isTheSelectedAppointment(appointment);
		styleManager.applyStyle(panel, selected);
		
		if(calendarWidget.getSettings().isEnableDragDrop() && !appointment.isReadOnly())
			dragController.makeDraggable(panel);

		if(selected)
			selectedAppointmentWidgets.add(panel);
		
		appointmentsWidgets.add(panel);
		appointmentCanvas.add(panel);
	}
   
	/**
	 * Gets the Month View's primary style name.
	 */
	public String getStyleName() {
		return MONTH_VIEW;
	}

	/**
	 * Handles the DoubleClick event to determine if an Appointment has been
	 * selected. If an appointment has been double clicked the OpenEvent will
	 * get fired for that appointment.
	 */
	public void onDoubleClick(Element clickedElement, Event event) {
		if (clickedElement.equals(appointmentCanvas.getElement())) {
			if (calendarWidget.getSettings().getTimeBlockClickNumber() == Click.Double) {
				dayClicked(event);
			}
		} else {
			ArrayList<AppointmentWidget> list = findAppointmentWidgetsByElement(clickedElement);
			if (!list.isEmpty()) {
				calendarWidget.fireOpenEvent(list.get(0).getAppointment());
			}
		}
	}

	/**
	 * Handles the a single click to determine if an appointment has been
	 * selected. If an appointment is clicked it's selected status will be set
	 * to true and a SelectionEvent will be fired.
	 */
	@Override
	public void onSingleClick(Element clickedElement, Event event) {
		if (clickedElement.equals(appointmentCanvas.getElement())) {
			if (calendarWidget.getSettings().getTimeBlockClickNumber() == Click.Single) {
				dayClicked(event);
			}
		} else {
			Appointment appointment = findAppointmentByElement(clickedElement);
			if (appointment != null) {
				selectAppointment(appointment);
			} else {
				// else, lets see if a "+ n more" label was clicked
				if (moreLabels.containsKey(clickedElement)) {
					calendarWidget.fireDateRequestEvent(
							cellDate(moreLabels.get(clickedElement)),
							clickedElement);
				}
			}
		}
	}

	public void onMouseOver(Element element, Event event) {
		Appointment appointment = findAppointmentByElement(element);
		calendarWidget.fireMouseOverEvent(appointment, element);
	}

    /**
     * Returns the date corresponding to the <code>cell</code> (as if the
     * month view grid was a big linear sequence of cells) in the month view
     * grid.
     * @param cell The cell number in the month view grid
     * @return The date that corresponds to the given <code>cell</code>
     */
	private Date cellDate(int cell) {
		return DateUtils.shiftDate(firstDateDisplayed, cell);
	}

	private void dayClicked(Event event) {
		int y = event.getClientY() - DOM.getAbsoluteTop(appointmentCanvas.getElement());
		int x = event.getClientX() - DOM.getAbsoluteLeft(appointmentCanvas.getElement());

		int row = (int) Math.floor(y / (appointmentCanvas.getOffsetHeight() / monthViewRequiredRows));
		int col = (int) Math.floor(x / (appointmentCanvas.getOffsetWidth() / DAYS_IN_A_WEEK));
        calendarWidget.fireTimeBlockClickEvent(
            cellDate(row * DAYS_IN_A_WEEK + col));
	}

	private ArrayList<AppointmentWidget> findAppointmentWidgetsByElement(
			Element element) {
		return findAppointmentWidgets(findAppointmentByElement(element));
	}

	/**
	 * Builds and formats the Calendar Grid. No appointments are included when
	 * building the grid.
	 */
	@SuppressWarnings("deprecation")
	private void buildCalendarGrid() {
		int firstDayOfWeek = CalendarFormat.INSTANCE.getFirstDayOfWeek();
		int month = calendarWidget.getDate().getMonth();
		firstDateDisplayed = firstDateShownInAMonthView(
				calendarWidget.getDate(), firstDayOfWeek);

		Date today = new Date();
		DateUtils.resetTime(today);

		/* Add the calendar weekday heading */
		for (int i = 0; i < DAYS_IN_A_WEEK; i++) {
			monthCalendarGrid
					.setText(
							0,
							i,
							CalendarFormat.INSTANCE
									.getDayOfWeekAbbreviatedNames()[(i + firstDayOfWeek) % 7]);
			monthCalendarGrid.getCellFormatter().setVerticalAlignment(0, i,
					HasVerticalAlignment.ALIGN_TOP);
			monthCalendarGrid.getCellFormatter().setStyleName(0, i,
					WEEKDAY_LABEL_STYLE);
		}
      Date date = (Date)firstDateDisplayed.clone();
		monthViewRequiredRows = MonthViewDateUtils.monthViewRequiredRows(
				calendarWidget.getDate(), firstDayOfWeek);
		int weekNumber = DateUtils.calendarWeekIso(date);
		
		for (int monthGridRowIndex = 1; monthGridRowIndex <= monthViewRequiredRows; monthGridRowIndex++) {
			for (int dayOfWeekIndex = 0; dayOfWeekIndex < DAYS_IN_A_WEEK; dayOfWeekIndex++) {

				if (monthGridRowIndex != 1 || dayOfWeekIndex != 0) {
					moveOneDayForward(date);
					weekNumber = DateUtils.calendarWeekIso(date);
				}
				
				configureDayInGrid(monthGridRowIndex, dayOfWeekIndex,
						date, date.equals(today),
						date.getMonth() != month, weekNumber);
			}
		}
	}

	/**
	 * Configures a single day in the month grid of this <code>MonthView</code>.
	 *
	 * @param row
	 *            The row in the grid on which the day will be set
	 * @param col
	 *            The col in the grid on which the day will be set
	 * @param date
	 *            The Date in the grid
	 * @param isToday
	 *            Indicates whether the day corresponds to today in the month
	 *            view
	 * @param notInCurrentMonth
	 *            Indicates whether the day is in the current visualized month
	 *            or belongs to any of the two adjacent months of the current
	 *            month
	 * @param weekNumber
	 *            The weekNumber to show in the cell, only appears in the first col.
	 */
	private void configureDayInGrid(int row, int col, Date date,
			boolean isToday, boolean notInCurrentMonth, int weekNumber) {
		HorizontalPanel panel = new HorizontalPanel();
		String text = String.valueOf(date.getDate());
		Label label = new Label(text);

		StringBuilder headerStyle = new StringBuilder(CELL_HEADER_STYLE);
		StringBuilder cellStyle = new StringBuilder(CELL_STYLE);
		boolean found = false;
		
		for (Date day : getSettings().getHolidays()) {
			if (DateUtils.areOnTheSameDay(day, date)) {
				headerStyle.append("-holiday");
				cellStyle.append("-holiday");
				found = true;
				break;
			}
		}

		if (isToday) {
			headerStyle.append("-today");
			cellStyle.append("-today");
	    } else if(!found && DateUtils.isWeekend(date)) {
			headerStyle.append("-weekend");
			cellStyle.append("-weekend");
		}

		if (notInCurrentMonth) {
			headerStyle.append("-disabled");
		}

		label.setStyleName(headerStyle.toString());
		addDayClickHandler(label, (Date)date.clone());

		if (col == 0 && getSettings().isShowingWeekNumbers()) {
			Label weekLabel = new Label(String.valueOf(weekNumber));
			weekLabel.setStyleName(WEEKNUMBER_LABEL_STYLE);
			
			panel.add(weekLabel);
			panel.setCellWidth(weekLabel, "25px");
			DOM.setStyleAttribute(label.getElement(), "paddingLeft", "5px");
			addWeekClickHandler(weekLabel, (Date)date.clone());
		}
		panel.add(label);
		
		appointmentCanvas.add(panel);
		dayLabels.add(label);
		dayPanels.add(panel);

		//monthCalendarGrid.setWidget(row, col, panel);
		monthCalendarGrid.getCellFormatter().setVerticalAlignment(row, col,
				HasVerticalAlignment.ALIGN_TOP);
		monthCalendarGrid.getCellFormatter().setStyleName(row, col,
				cellStyle.toString());
	}

	/**
	 * Returns the {@link Appointment} indirectly associated to the passed
	 * <code>element</code>. Each Appointment drawn on the CalendarView maps to
	 * a Widget and therefore an Element. This method attempts to find an
	 * Appointment based on the provided Element. If no match is found a null
	 * value is returned.
	 *
	 * @param element Element to look up.
	 * @return Appointment matching the element.
	 */
	private Appointment findAppointmentByElement(Element element) {
		Appointment appointmentAtElement = null;
		for (AppointmentWidget widget : appointmentsWidgets) {
			if (DOM.isOrHasChild(widget.getElement(), element)) {
				appointmentAtElement = widget.getAppointment();
				break;
			}
		}
		return appointmentAtElement;
	}

	/**
	 * Finds any related <code>AppointmentWidgets</code> associated to the
	 * passed Appointment, <code>appt</code>.
	 *
	 * @param appt
	 *            Appointment to match.
	 * @return List of related AppointmentWidget objects.
	 */
	private ArrayList<AppointmentWidget> findAppointmentWidgets(Appointment appt) {
		ArrayList<AppointmentWidget> appointmentWidgets = new ArrayList<AppointmentWidget>();
		if (appt != null) {
			for (AppointmentWidget widget : appointmentsWidgets) {
				if (widget.getAppointment().equals(appt)) {
					appointmentWidgets.add(widget);
				}
			}
		}
		return appointmentWidgets;
	}

	public void onDeleteKeyPressed() {
		if (calendarWidget.getSelectedAppointment() != null)
			calendarWidget.fireDeleteEvent(calendarWidget
					.getSelectedAppointment());
	}

	@Override
	public void onAppointmentSelected(Appointment appt) {
		ArrayList<AppointmentWidget> clickedAppointmentWidgets = findAppointmentWidgets(appt);

		if (!clickedAppointmentWidgets.isEmpty()) {
			for (AppointmentWidget widget : selectedAppointmentWidgets) {
				//widget.removeStyleDependentName("selected");
				//DOM.setStyleAttribute(widget.getElement(),
                //       "borderColor", widget.getAppointment().getAppointmentStyle().getBorder());
				styleManager.applyStyle(widget, false);
			}

			for (AppointmentWidget widget : clickedAppointmentWidgets) {
				//widget.addStyleDependentName("selected");
				//DOM.setStyleAttribute(widget.getElement(),
                //       "borderColor", appt.getAppointmentStyle().getSelectedBorder());
				styleManager.applyStyle(widget, true);
			}

			selectedAppointmentWidgets.clear();
			selectedAppointmentWidgets = clickedAppointmentWidgets;
		}
	}

    /**
     * Multiple calculated (&quot;cached&quot;) values reused during
     * laying out the month view elements.
     */

	private int calculatedWeekDayHeaderHeight;
	private int calculatedDayHeaderHeight;

	/**
	 * Maximum appointments per cell (day).
	 */
	private int calculatedCellAppointments;

	/**
	 * Height of each Cell (day), including the day's header.
	 */
	private float calculatedCellOffsetHeight;

	/**
	 * Height of each Cell (day), excluding the day's header.
	 */
	private float calculatedCellHeight;

	/**
	 * Calculates the height of each day cell in the Month grid. It excludes the
	 * height of each day's header, as well as the overall header that shows the
	 * weekday labels.
	 */
	private void calculateCellHeight() {

		int gridHeight = monthCalendarGrid.getOffsetHeight();
		int weekdayRowHeight = monthCalendarGrid.getRowFormatter()
				.getElement(0).getOffsetHeight();
		int dayHeaderHeight = dayLabels.get(0).getOffsetHeight(); 

		calculatedCellOffsetHeight = (float) (gridHeight - weekdayRowHeight)
				/ monthViewRequiredRows;
		calculatedCellHeight = calculatedCellOffsetHeight - dayHeaderHeight;
		calculatedWeekDayHeaderHeight = weekdayRowHeight;
		calculatedDayHeaderHeight = dayHeaderHeight;
	}

	/**
	 * Calculates the maximum number of appointments that can be displayed in a
	 * given &quot;day cell&quot;.
	 */
	private void calculateCellAppointments() {
		int paddingTop = appointmentPaddingTop();
		int height = appointmentHeight();

		calculatedCellAppointments = (int)
            Math.floor((float) (calculatedCellHeight - paddingTop)
						/ (float) (height + paddingTop)) - 1;
	}

	private static int appointmentPaddingTop() {
		return 1 + (Math.abs(FormattingUtil.getBorderOffset()) * 3);
	}

	private static int appointmentHeight() {
		// TODO: calculate appointment height dynamically
		return 20;
	}

	private void placeItemInGrid(Widget panel, int colStart, int colEnd,
			int row, int cellPosition) {
		int paddingTop = appointmentPaddingTop() + 3;
		int height = appointmentHeight();

		float left = (float) colStart / (float) DAYS_IN_A_WEEK * 100f + .5f;

		float width = ((float) (colEnd - colStart + 1) / (float) DAYS_IN_A_WEEK) * 100f - 1f;

		float top = calculatedWeekDayHeaderHeight
				+ (row * calculatedCellOffsetHeight)
				+ calculatedDayHeaderHeight + paddingTop
				+ (cellPosition * (height + paddingTop));
//		 System.out.println( "\t" + calculatedWeekDayHeaderHeight + " + (" + row +
//		 " * " + calculatedCellOffsetHeight + ") + " +
//		 calculatedDayHeaderHeight + " + " + paddingTop + " + (" +
//		 cellPosition+"*("+height+"+"+paddingTop + "));");

		DOM.setStyleAttribute(panel.getElement(), "position", "absolute");
		DOM.setStyleAttribute(panel.getElement(), "top", top + "px");
		DOM.setStyleAttribute(panel.getElement(), "left", left + "%");
		DOM.setStyleAttribute(panel.getElement(), "width", width + "%");
	}

	private void placeDayLabelInGrid(Widget panel, int col, int row) {
		int paddingTop = appointmentPaddingTop();

		float left = (float) col / (float) DAYS_IN_A_WEEK * 100f + .5f;

		float width = (1f / (float) DAYS_IN_A_WEEK) * 100f - 1f;

		float top = calculatedWeekDayHeaderHeight
				+ (row * calculatedCellOffsetHeight)
				+ paddingTop;
		DOM.setStyleAttribute(panel.getElement(), "position", "absolute");
		DOM.setStyleAttribute(panel.getElement(), "top", top + "px");
		DOM.setStyleAttribute(panel.getElement(), "left", left + "%");
		DOM.setStyleAttribute(panel.getElement(), "width", width + "%");
	}
}
