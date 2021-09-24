package com.bradrydzewski.gwt.calendar.client.monthview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.bradrydzewski.gwt.calendar.client.Appointment;

/**
 * Test cases for the {@link WeekLayoutDescription} class.
 *
 * @author Carlos D. Morales
 */
public class WeekLayoutDescriptionTest {

    private DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
    private Date NOV_1_2009 = null;
    private Date DEC_5_2009 = null;
    private Appointment appointment = null;

    private WeekLayoutDescription weekDescription = null;

    @Before
    public void init() throws Exception {
        NOV_1_2009 = dateFormatter.parse("11/01/2009");
        DEC_5_2009 = dateFormatter.parse("12/05/2009");
        weekDescription = new WeekLayoutDescription(NOV_1_2009, DEC_5_2009);
        appointment = new Appointment();
    }

    @Test
    public void areThereAppointmentsOnDay() throws Exception {
        assertFalse("No appointment has been added on the 1st. of Nov 09",
                weekDescription.areThereAppointmentsOnDay(0));
        appointment.setStart(NOV_1_2009);
        appointment.setEnd(NOV_1_2009);
        weekDescription.addAppointment(appointment);
        assertTrue(
                "An appointment for the first day of this week has been added",
                weekDescription.areThereAppointmentsOnDay(0));
    }

    /**
     * Tests and shows that a single week is not aware of any other thing than
     * placing an appointment <em>horizontally</em>, i.e., without considering
     * the exact week the appointment belongs to.
     *
     * @throws Exception If a date cannot be parsed
     */
    @Test
    public void areThereAppointmentsOnDayWeekIgnoresExactWeek()
            throws Exception {
        assertFalse("No appointment has been added on the 1st. of Nov 09",
                weekDescription.areThereAppointmentsOnDay(0));

        appointment.setStart(NOV_1_2009);
        appointment.setEnd(NOV_1_2009);
        weekDescription.addAppointment(appointment);

        appointment = new Appointment();
        appointment.setStart(dateFormatter.parse("11/08/2009"));
        appointment.setEnd(dateFormatter.parse("11/08/2009"));
        weekDescription.addAppointment(appointment);

        assertThereAreAppointmentsOn(0);

        assertEquals(
                "Two Appointments for the first day of the week have been added",
                2,
                weekDescription
                        .getDayLayoutDescription(0).getTotalAppointmentCount());
    }

    @Test
    public void addOneMultiDaySingleWeek() throws Exception {
        assertFalse("No appointment has been added on the 1st. of Nov 09",
                weekDescription.areThereAppointmentsOnDay(0));

        appointment.setStart(NOV_1_2009);
        appointment.setEnd(dateFormatter.parse("11/03/2009"));
        weekDescription.addMultiDayAppointment(appointment);

        assertThereAreAppointmentsOn(0);

        assertNull(
                "Appointment should not be added to the day layout because its multi day",
                weekDescription.getDayLayoutDescription(0));

        assertMultiDayAppointmentsNotEmpty(0);
        assertMultiDayAppointmentsNotEmpty(1);
        assertMultiDayAppointmentsNotEmpty(2);


        assertStackOrderForDay(0, 1);
        assertStackOrderForDay(1, 1);
        assertStackOrderForDay(2, 1);
    }

    private void addMultiDayAppointment(String start, String end) throws Exception {
        appointment = new Appointment();
        appointment.setStart(dateFormatter.parse(start));
        appointment.setEnd(dateFormatter.parse(end));
        weekDescription.addMultiDayAppointment(appointment);
    }

    @Test
    public void addTwoMultiDaySingleWeekAndNoOverlapBetweenThem()
            throws Exception {

        assertFalse("No appointment has been added on the 1st. of Nov 09",
                weekDescription.areThereAppointmentsOnDay(0));

        addMultiDayAppointment("11/01/2009", "11/03/2009");
        addMultiDayAppointment("11/04/2009", "11/05/2009");

        assertThereAreAppointmentsOn(0);

        assertNull(
                "Appointment should not be added to the day layout because its multi day",
                weekDescription.getDayLayoutDescription(0));

        assertMultiDayAppointmentsNotEmpty(0);
        assertMultiDayAppointmentsNotEmpty(1);
        assertMultiDayAppointmentsNotEmpty(2);
        assertMultiDayAppointmentsNotEmpty(3);
        assertMultiDayAppointmentsNotEmpty(4);

        assertStackOrderForDay(0, 1);
        assertStackOrderForDay(1, 1);
        assertStackOrderForDay(2, 1);
        assertStackOrderForDay(3, 1);
        assertStackOrderForDay(4, 1);
        assertStackOrderForDay(5, 0);
        assertStackOrderForDay(6, 0);
    }

    @Test
    public void addTwoMultiDaySingleWeekWithOverlap()
            throws Exception {

        assertFalse("No appointment has been added on the 1st. of Nov 09",
                weekDescription.areThereAppointmentsOnDay(0));

        addMultiDayAppointment("11/01/2009", "11/03/2009");
        addMultiDayAppointment("11/03/2009", "11/05/2009");

        assertThereAreAppointmentsOn(0);
        assertThereAreAppointmentsOn(1);
        assertThereAreAppointmentsOn(2);
        assertThereAreAppointmentsOn(3);
        assertThereAreAppointmentsOn(4);

        assertMultiDayAppointmentsNotEmpty(0);
        assertMultiDayAppointmentsNotEmpty(1);
        assertMultiDayAppointmentsNotEmpty(2);
        assertMultiDayAppointmentsNotEmpty(3);
        assertMultiDayAppointmentsNotEmpty(4);

        assertStackOrderForDay(0, 1);//11/1
        assertStackOrderForDay(1, 1);//11/2
        assertStackOrderForDay(2, 2);//11/3
        assertStackOrderForDay(3, 0);//11/4
        assertStackOrderForDay(4, 0);//11/5
        assertStackOrderForDay(5, 0);//11/6
        assertStackOrderForDay(6, 0);//11/7
    }

    private void assertThereAreAppointmentsOn(int day) {
        assertTrue(
                "Appointments for the day " + day + " of the week have been added",
                weekDescription.areThereAppointmentsOnDay(day));
    }

    private void assertMultiDayAppointmentsNotEmpty(int day) {
        assertTrue("List of multiday appointments should not be empty",
                weekDescription.areThereAppointmentsOnDay(day));
    }

    private void assertStackOrderForDay(int day, int expected) {
        assertEquals(
                "The stack order for the " + day + " day should be " + expected,
                expected, weekDescription.currentStackOrderInDay(day));
    }
}
