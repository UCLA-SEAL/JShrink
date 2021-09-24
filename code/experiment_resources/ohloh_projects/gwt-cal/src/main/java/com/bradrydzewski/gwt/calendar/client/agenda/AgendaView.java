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

package com.bradrydzewski.gwt.calendar.client.agenda;

import java.util.ArrayList;
import java.util.Date;

import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.bradrydzewski.gwt.calendar.client.Attendee;
import com.bradrydzewski.gwt.calendar.client.CalendarView;
import com.bradrydzewski.gwt.calendar.client.CalendarWidget;
import com.bradrydzewski.gwt.calendar.client.DateUtils;
import com.bradrydzewski.gwt.calendar.client.util.AppointmentUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class AgendaView extends CalendarView {

	/**
	 * Adapter class that maps an Appointment to the widgets (DIV's, etc) that represent
	 * it on the screen. This is necessary because a single appointment is represented by
	 * many widgets. For example, an appointment is represented by a title widget,
	 * a description widget, and has a "get more details" label.
	 * 
	 * By mapping an appointment to these widgets we can easily figure out which
	 * appointment the user is interacting with as they click around the AgendaView.
	 * @author Brad Rydzewski
	 * @version 1.0
	 * @since 0.9.0
	 */
	class AgendaViewAppointmentAdapter {
		private Widget titleLabel;
		private Widget dateLabel;
		private Widget detailsLabel;
		private Appointment appointment;
		private Widget detailsPanel;

		public AgendaViewAppointmentAdapter(Widget titleLabel, Widget dateLabel,
				Widget detailsPanel, Widget detailsLabel, Appointment appointment) {
			super();
			this.titleLabel = titleLabel;
			this.dateLabel = dateLabel;
			this.detailsLabel = detailsLabel;
			this.detailsPanel = detailsPanel;
			this.appointment = appointment;
		}

		public Widget getTitleLabel() {
			return titleLabel;
		}

		public Widget getDateLabel() {
			return dateLabel;
		}

		public Widget getDetailsLabel() {
			return detailsLabel;
		}

		public Appointment getAppointment() {
			return appointment;
		}

		public Widget getDetailsPanel() {
			return detailsPanel;
		}
	}

	class AppointmentDetailPanel extends Composite {
		
		private Label moreDetailsRow = new Label();
		
		public AppointmentDetailPanel(SimplePanel detailContainer, Appointment appt) {
			initWidget(detailContainer);

			// add the detail widget
			detailContainer.setStyleName("detailContainer");
			AbsolutePanel detailDecorator = new AbsolutePanel();
			detailDecorator.setStyleName("detailDecorator");
			detailContainer.setVisible(false);
			detailContainer.add(detailDecorator);

			if (appt.getLocation() != null
					&& !appt.getLocation().isEmpty()) {
				AbsolutePanel whereRow = new AbsolutePanel();
				InlineLabel whereHeader = new InlineLabel("Where: ");
				whereHeader.setStyleName("detailHeader");
				whereRow.add(whereHeader);
				whereRow.add(new InlineLabel(appt.getLocation()));
				detailDecorator.add(whereRow);
			}
			if (appt.getCreatedBy() != null
					&& !appt.getCreatedBy().isEmpty()) {
				AbsolutePanel creatorRow = new AbsolutePanel();
				InlineLabel creatorHeader = new InlineLabel("Creator: ");
				creatorHeader.setStyleName("detailHeader");
				creatorRow.add(creatorHeader);
				creatorRow.add(new InlineLabel(appt.getCreatedBy()));
				detailDecorator.add(creatorRow);
			}
			if (appt.getAttendees() != null
					&& !appt.getAttendees().isEmpty()) {
				AbsolutePanel whoRow = new AbsolutePanel();
				InlineLabel whoHeader = new InlineLabel("Who: ");
				whoHeader.setStyleName("detailHeader");
				whoRow.add(whoHeader);
				for (int a = 0; a < appt.getAttendees().size(); a++) {
					Attendee attendee = appt.getAttendees().get(a);
					String comma = (a < appt.getAttendees().size() - 1) ? ", "
							: "";
					String labelText = attendee.getEmail() + comma;
					whoRow.add(new InlineLabel(labelText));
				}
				detailDecorator.add(whoRow);
			}


			
			
			DOM.setInnerHTML(moreDetailsRow.getElement(), 
					"more details&#0187;");
			
			moreDetailsRow.setStyleName("moreDetailsButton");
			//moreDetailsRow.addClickHandler(appointmentClickHandler);
			detailDecorator.add(moreDetailsRow);
		}
		public Label getMoreDetailsLabel() {
			return moreDetailsRow;
		}
	}

	/**
	 * FlexTable used to display a list of appointments.
	 */
	private FlexTable appointmentGrid = new FlexTable();

	/**
	 * List of appointment adapters, used to map widgets to the appointments
	 * they represent.
	 */
	private ArrayList<AgendaViewAppointmentAdapter> appointmentAdapterList = 
		new ArrayList<AgendaViewAppointmentAdapter>();

	/**
	 * DateTime format used to represent a day.
	 */
	private static final DateTimeFormat DEFAULT_DATE_FORMAT =
		DateTimeFormat.getFormat("EEE MMM d");

	/**
	 * DateTime format used when displaying an appointments start and end time.
	 */
	private static final DateTimeFormat DEFAULT_TIME_FORMAT =
		DateTimeFormat.getShortTimeFormat();
	
	/**
	 * Style used to format this view.
	 */
	private String styleName = "gwt-cal-ListView";

	/**
	 * Adds the calendar view to the calendar widget and performs required formatting.
	 */
	public void attach(CalendarWidget widget) {
		super.attach(widget);

		appointmentGrid.setCellPadding(5);
		appointmentGrid.setCellSpacing(0);
		appointmentGrid.setBorderWidth(0);
		appointmentGrid.setWidth("100%");
		//DOM.setStyleAttribute(appointmentGrid.getElement(), "tableLayout", "fixed");
		calendarWidget.getRootPanel().add(appointmentGrid);
		calendarWidget.getRootPanel().add(appointmentGrid);
	}

	/**
	 * Gets the style name associated with this particular view
	 * @return Style name.
	 */
	public String getStyleName() {
		return styleName;
	}

	@Override
	public void doLayout() {

		appointmentAdapterList.clear();
		appointmentGrid.clear();
		for(int i=appointmentGrid.getRowCount()-1;i>=0;i--){
			appointmentGrid.removeRow(i);
		}
		

		//Get the start date, make sure time is 0:00:00 AM
		Date startDate = (Date) calendarWidget.getDate().clone();
		Date today = new Date();
		Date endDate = (Date) calendarWidget.getDate().clone();
		endDate.setDate(endDate.getDate() + 1);
      DateUtils.resetTime(today);
      DateUtils.resetTime(startDate);
      DateUtils.resetTime(endDate);
        
		int row = 0;

		for (int i = 0; i < calendarWidget.getDays(); i++) {

			// Filter the list by date
			ArrayList<Appointment> filteredList = AppointmentUtil
					.filterListByDate(calendarWidget.getAppointments(), startDate, endDate);

			if (filteredList != null && filteredList.size() > 0) {

				appointmentGrid.setText(row, 0, DEFAULT_DATE_FORMAT.format(startDate));

				appointmentGrid.getCellFormatter().setVerticalAlignment(row, 0,
						HasVerticalAlignment.ALIGN_TOP);
				appointmentGrid.getFlexCellFormatter().setRowSpan(row, 0,
						filteredList.size());
				appointmentGrid.getFlexCellFormatter().setStyleName(row, 0,
						"dateCell");
				int startingCell = 1;

				//Row styles will alternate, so we set the style accordingly
				String rowStyle = (i % 2 == 0) ? "row" : "row-alt";

				//If a Row represents the current date (Today) then we style it differently
				if (startDate.equals(today))
					rowStyle += "-today";


				for (Appointment appt : filteredList) {

					// add the time range
					String timeSpanString = DEFAULT_TIME_FORMAT.format(appt.getStart())
							+ " - " + DEFAULT_TIME_FORMAT.format(appt.getEnd());
					Label timeSpanLabel = new Label(timeSpanString.toLowerCase());
					appointmentGrid.setWidget(row, startingCell, timeSpanLabel);

					

					// add the title and description
					FlowPanel titleContainer = new FlowPanel();
					InlineLabel titleLabel = new InlineLabel(appt.getTitle());
					titleContainer.add(titleLabel);
					InlineLabel descLabel = new InlineLabel(" - "
							+ appt.getDescription());
					descLabel.setStyleName("descriptionLabel");
					titleContainer.add(descLabel);
					appointmentGrid.setWidget(row, startingCell + 1,
							titleContainer);



					SimplePanel detailContainerPanel = new SimplePanel();
					AppointmentDetailPanel detailContainer= new AppointmentDetailPanel(detailContainerPanel, appt);
					
					appointmentAdapterList.add(new AgendaViewAppointmentAdapter(
							titleLabel, timeSpanLabel, detailContainerPanel, 
							detailContainer.getMoreDetailsLabel(), appt));

					//add the detail container
					titleContainer.add(detailContainer);

					//add click handlers to title, date and details link
					timeSpanLabel.addClickHandler(appointmentClickHandler);
					titleLabel.addClickHandler(appointmentClickHandler);
					detailContainer.getMoreDetailsLabel().addClickHandler(
							appointmentClickHandler);
					



					// Format the Cells
					appointmentGrid.getCellFormatter().setVerticalAlignment(
							row, startingCell, HasVerticalAlignment.ALIGN_TOP);
					appointmentGrid.getCellFormatter().setVerticalAlignment(
							row, startingCell + 1,
							HasVerticalAlignment.ALIGN_TOP);
					appointmentGrid.getCellFormatter().setStyleName(row,
							startingCell, "timeCell");
					appointmentGrid.getCellFormatter().setStyleName(row,
							startingCell + 1, "titleCell");
					appointmentGrid.getRowFormatter().setStyleName(row,
							rowStyle);

					// increment the row
					// make sure the starting column is reset to 0
					startingCell = 0;
					row++;
				}
			}

			// increment the date
			startDate.setDate(startDate.getDate() + 1);
			endDate.setDate(endDate.getDate() + 1);
		}
	}


	/**
	 * Handles appointments being clicked. Based on the clicked widget will determine
	 * exactly which appointment was clicked and may 1) expand / collaps the appointment
	 * details 2) set the selected appointment or 3) trigger an appointment clicked
	 * event.
	 */
	private ClickHandler appointmentClickHandler = new ClickHandler() {

		public void onClick(ClickEvent event) {

			//get the appointment adapter based on the clicked widget
			AgendaViewAppointmentAdapter adapter =
				getAppointmentFromClickedWidget((Widget)event.getSource());

			if(adapter!=null) {
				if(event.getSource().equals(adapter.getDetailsLabel())) {
					//set the selected appointment
					calendarWidget.setSelectedAppointment(adapter.getAppointment(), true);
					//setSelectedAppointment(adapter.getAppointment(),false);
					//calendarWidget.fireOpenEvent(adapter.getAppointment());
				} else {
					//expand the panel if it is not yet expended
					adapter.getDetailsPanel().setVisible(
							!adapter.getDetailsPanel().isVisible());
				}
			}
		}
	};

	/**
	 * Given Widget w determine which appointment was clicked. This is necessary because
	 * each appointment has 3 widgets that can be clicked - the title, date range and
	 * description.
	 * @param w Widget that was clicked.
	 * @return Appointment mapped to that widget.
	 */
	protected AgendaViewAppointmentAdapter getAppointmentFromClickedWidget(Widget w) {
		for(AgendaViewAppointmentAdapter a : appointmentAdapterList) {
			if(w.equals(a.dateLabel) || w.equals(a.detailsLabel) || 
					w.equals(a.titleLabel) || w.equals(a.getDetailsPanel())) {
				return a;
			}
		}
		return null;
	}

	@Override
	public void onDoubleClick(Element element, Event event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSingleClick(Element element, Event event) {
		// TODO Auto-generated method stub
		
	}
	
	public void onMouseOver(Element element, Event event) {
		
	}

	@Override
	public void onAppointmentSelected(Appointment appt) {
		// TODO Auto-generated method stub
		
	}
}
