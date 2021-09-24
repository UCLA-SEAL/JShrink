package com.bradrydzewski.gwt.calendar.client.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.bradrydzewski.gwt.calendar.client.Appointment;

/**
 * Test cases for the logic in the <code>AppointmentUtil</code>
 * utilities class.
 *
 * @author Carlos D. Morales
 */
public class AppointmentUtilTest {

    private DateFormat dateFormatter = null;
    private Date rangeStart = null;
    private Date rangeEnd = null;
    private Appointment appointment = null;

    @Before
    public void init() throws Exception {
        dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
        rangeStart = dateFormatter.parse("10/20/2009");
        rangeEnd = dateFormatter.parse("10/21/2009");
        appointment = new Appointment();
    }

    /**
     * Tests that an appointment completely out of the range
     * is not contained in the range.
     */
    @Test
    public void testRangeContains_AppointmentOutOfRange() throws Exception {
        appointment.setStart(dateFormatter.parse("11/17/2009"));
        appointment.setEnd(dateFormatter.parse("10/19/2009"));
        assertFalse(
                AppointmentUtil.rangeContains(appointment, rangeStart, rangeEnd));
    }

    /**
     * Tests that an appointment whose start and end dates &quot;overlap&quot;
     * with a specified range on the left side is considered contained in the
     * range.
     */
    @Test
    public void testRangeContains_AppointmentOverlapsOnLeft() throws Exception {
        appointment.setStart(dateFormatter.parse("10/19/2009"));
        appointment.setEnd(dateFormatter.parse("10/25/2009"));
        assertTrue(
                AppointmentUtil.rangeContains(appointment, rangeStart, rangeEnd));
    }

    /**
     * Tests that an appointment completely contained in the range
     * is actually considered contained.
     */
    @Test
    public void testRangeContains_AppointmentContained() throws Exception {
        appointment.setStart(dateFormatter.parse("10/20/2009"));
        appointment.setEnd(dateFormatter.parse("10/20/2009"));
        assertTrue(AppointmentUtil.rangeContains(appointment, rangeStart, rangeEnd));
    }

    /**
     * Tests that an appointment whose start and end date exactly
     * overlap on the range is considered contained.
     */
    @Test
    public void testRangeContains_AppointmenEqualsRange() throws Exception {
        appointment.setStart(dateFormatter.parse("10/20/2009"));
        appointment.setEnd(dateFormatter.parse("10/21/2009"));
        assertTrue(AppointmentUtil.rangeContains(appointment, rangeStart, rangeEnd));
    }

}