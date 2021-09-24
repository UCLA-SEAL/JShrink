package com.bradrydzewski.gwt.calendar.theme.ical.client;

import java.util.HashMap;
import java.util.Map;

import com.bradrydzewski.gwt.calendar.client.AppointmentStyle;
import com.google.gwt.core.client.GWT;

public class ICalAppointmentTheme {

	private static final String URL = GWT.getModuleBaseURL()+"/";
    private static final ICalAppointmentStyle BLUE    = new ICalAppointmentStyle("#8DAFEA", "#0D50D5", URL+"blue-appt-gradient.gif");
    private static final ICalAppointmentStyle RED     = new ICalAppointmentStyle("#f76260", "#e3231f", URL+"red-appt-gradient.gif");
    private static final ICalAppointmentStyle PURPLE  = new ICalAppointmentStyle("#aa92ea", "#4b2ca0", URL+"purple-appt-gradient.gif");
    private static final ICalAppointmentStyle GREEN   = new ICalAppointmentStyle("#8EED7F", "#12A300", URL+"green-appt-gradient.gif");
    private static final ICalAppointmentStyle ORANGE  = new ICalAppointmentStyle("#fca550", "#f37b14", URL+"orange-appt-gradient.gif");
    private static final ICalAppointmentStyle FUCHSIA = new ICalAppointmentStyle("#c45cc3", "#b02cae", URL+"fuschia-appt-gradient.gif");
    public static final ICalAppointmentStyle DEFAULT = ORANGE;
    public static Map<AppointmentStyle, ICalAppointmentStyle> STYLES;
    
    static {
		STYLES = new HashMap<AppointmentStyle, ICalAppointmentStyle>();
		STYLES.put(AppointmentStyle.BLUE, BLUE);
		STYLES.put(AppointmentStyle.GREEN, GREEN);
		STYLES.put(AppointmentStyle.LIGHT_PURPLE, FUCHSIA);
		STYLES.put(AppointmentStyle.ORANGE, ORANGE);
		STYLES.put(AppointmentStyle.PURPLE, PURPLE);
		STYLES.put(AppointmentStyle.RED, RED);
		STYLES.put(AppointmentStyle.DEFAULT, DEFAULT);
    }
}

