package com.bradrydzewski.gwt.calendar.client.dayview;

import com.bradrydzewski.gwt.calendar.client.Appointment;
import com.bradrydzewski.gwt.calendar.client.ThemeAppointmentStyle;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

public abstract class DayViewStyleManager {

    protected static final String APPOINTMENT_STYLE = "dv-appointment";

    protected static final String APPOINTMENT_STYLE_SELECTED = "-selected";

    protected static final String APPOINTMENT_STYLE_MULTIDAY = "-multiday";

    protected static final String BACKGROUND_COLOR_STYLE_ATTRIBUTE = "backgroundColor";

    protected static final String BACKGROUND_IMAGE_STYLE_ATTRIBUTE = "backgroundImage";

    protected static final String BORDER_COLOR_STYLE_ATTRIBUTE = "borderColor";

    protected static final String COLOR_STYLE_ATTRIBUTE = "color";

	public void applyStyle(AppointmentWidget widget, boolean selected){
        doApplyStyleInternal(widget, selected);
    }

    protected abstract ThemeAppointmentStyle getViewAppointmentStyleForTheme(Appointment appointment);

    protected abstract ThemeAppointmentStyle getDefaultViewAppointmentStyleForTheme();

    private void doApplyStyleInternal(AppointmentWidget widget, boolean selected) {

		// Extract the Appointment for later reference
		Appointment appointment = widget.getAppointment();
		// Extract the DOM Element for later reference
		Element elem = widget.getElement();
		Element bodyElem = widget.getBody().getElement();
		Element headerElem = widget.getHeader().getElement();
		// Is MultiDay?
		boolean multiDay = appointment.isMultiDay() || appointment.isAllDay();


		//Lookup the style from the map
		ThemeAppointmentStyle style = getViewAppointmentStyleForTheme(appointment);

		//Determine Style Name
		String styleName = APPOINTMENT_STYLE;
		if(multiDay) styleName+=APPOINTMENT_STYLE_MULTIDAY;
		if(selected) styleName+=APPOINTMENT_STYLE_SELECTED;
		widget.setStylePrimaryName(styleName);

		//If no style is found, apply the default blue style
		//TODO: need to check for a custom style
		if(style==null)
			style =  getDefaultViewAppointmentStyleForTheme();


		if (multiDay)
			DOM.setStyleAttribute(elem, BACKGROUND_COLOR_STYLE_ATTRIBUTE, style.getBackgroundHeader());
		else
			DOM.setStyleAttribute(elem, BACKGROUND_COLOR_STYLE_ATTRIBUTE, style.getBackground());

		DOM.setStyleAttribute(elem, BORDER_COLOR_STYLE_ATTRIBUTE, style.getBackgroundHeader());

		DOM.setStyleAttribute(bodyElem, COLOR_STYLE_ATTRIBUTE, style.getSelectedBorder());

		DOM.setStyleAttribute(headerElem, COLOR_STYLE_ATTRIBUTE, style.getHeaderText());

		DOM.setStyleAttribute(headerElem, BACKGROUND_COLOR_STYLE_ATTRIBUTE,style.getBackgroundHeader());

		if (multiDay)
			return;

		if (selected && style.getSelectedBackgroundImage() != null) {
			DOM.setStyleAttribute(elem, BACKGROUND_IMAGE_STYLE_ATTRIBUTE, "url("+ style.getSelectedBackgroundImage() + ")");
		} else {
			DOM.setStyleAttribute(elem, BACKGROUND_IMAGE_STYLE_ATTRIBUTE, "none");
		}

	}
}
