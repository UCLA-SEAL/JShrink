package com.bradrydzewski.gwt.calendar.client;

import java.util.ArrayList;

public interface HasAppointments {

	public void removeAppointment(Appointment appointment);
	public void removeAppointment(Appointment appointment, boolean fireEvents);
	public void addAppointment(Appointment appointment);
	public void addAppointments(ArrayList<Appointment> appointments);
	public void clearAppointments();
	public Appointment getSelectedAppointment();
	public void setSelectedAppointment(Appointment appointment);
	public void setSelectedAppointment(Appointment appointment,
		      boolean fireEvents);
	public boolean hasAppointmentSelected();
}
