package com.bradrydzewski.gwt.calendar.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the {@link AppointmentManager} class.
 *
 * @author Brad Rydzewski
 * @author Carlos D. Morales
 */
public class AppointmentManagerTest {

    private AppointmentManager manager = null;

    @Before
    public void prepareAppointmentsAndManager() {
        manager = new AppointmentManager();
    }

    @Test
    public void listNotNullButEmpty() {
        assertNotNull(
                "Newly instantiated AppointmentManager should have a set list of appointments.",
                manager.getAppointments());
        assertTrue(
                "Newly instantiated AppointmentManager should have no appointments.",
                manager.getAppointments().isEmpty());
    }

    @Test
    public void addAppointmentNullAppointment() {
        manager.addAppointment(null);
        assertTrue(
                "Adding null appointment does not cause error: should do nothing!",
                manager.getAppointments().isEmpty());
    }

    @Test
    public void addAppointment() {
        manager.addAppointment(new Appointment());
        manager.addAppointment(new Appointment());
        assertEquals("Appointment not added", 2,
                manager.getAppointments().size());
    }

    @Test
    public void addAppointmentsNullList() {
        manager.addAppointments(null);
        assertTrue(
                "Null appointments list does not cause error: should do nothing!",
                manager.getAppointments().isEmpty());
    }

    @Test
    public void removeAppointmentNullParameter() {
        manager.addAppointment(new Appointment());
        manager.removeAppointment(null);
        assertEquals("Removing null appointment changed the appointments list",
                1, manager.getAppointments().size());
    }

    @Test
    public void removeAppointmentParameterNotInCollection() {
        manager.addAppointment(new Appointment());
        manager.removeAppointment(new Appointment());
        assertEquals(
                "Removing an appointment previously not in the list should not change the list.",
                1, manager.getAppointments().size());
    }

    @Test
    public void removeAppointmentSameAppointment() {
        Appointment toBeRemoved = new Appointment();
        manager.addAppointment(toBeRemoved);
        assertEquals("The appointment was not added", 1,
                manager.getAppointments().size());
        manager.removeAppointment(toBeRemoved);
        assertTrue("The appointment was not removed",
                manager.getAppointments().isEmpty());
    }

    @Test
    public void removeAppointmentThatHappensToBeTheOneSelected() {
        Appointment toBeRemoved = new Appointment();
        manager.addAppointment(toBeRemoved);
        manager.setSelectedAppointment(toBeRemoved);
        assertEquals("Unexpected appointments set size", 1,
                manager.getAppointments().size());
        assertNotNull("Selected appointment should not be null",
                manager.getSelectedAppointment());
        manager.removeAppointment(toBeRemoved);
        assertTrue("Appointments set is not empty",
                manager.getAppointments().isEmpty());
        assertNull(
                "Selected appointment should be null as it was removed from the managed set",
                manager.getSelectedAppointment());
    }

    @Test
    public void removeCurrentlySelectedAppointment() {
        Appointment currentlySelected = new Appointment();
        manager.addAppointment(currentlySelected);
        manager.setSelectedAppointment(currentlySelected);
        assertNotNull("Selected appointment is null",
                manager.getSelectedAppointment());
        manager.removeCurrentlySelectedAppointment();
        assertTrue("The collection of appointments should be empty",
                manager.getAppointments().isEmpty());
        assertNull("The currently selected appointment should be null",
                manager.getSelectedAppointment());
    }

    @Test
    public void removeCurrentlySelectedAppointmentNoAppointmentSelected() {
        manager.addAppointment(new Appointment());
        assertEquals("Unexpected appointments collection size", 1,
                manager.getAppointments().size());
        assertNull("Currently selected appointment should be null",
                manager.getSelectedAppointment());
        manager.removeCurrentlySelectedAppointment();
        assertEquals(
                "There was no selected appointment, size should not change", 1,
                manager.getAppointments().size());
        assertFalse("There should be no appointment selected",
                manager.hasAppointmentSelected());
    }

    @Test
    public void clearAppointments() {
        manager.addAppointment(new Appointment());
        manager.addAppointment(new Appointment());
        assertEquals("Appointments not added", 2,
                manager.getAppointments().size());
        manager.clearAppointments();
        assertTrue("Appointments collection was not cleared",
                manager.getAppointments().isEmpty());
    }

    @Test
    public void setSelectedAppointmentNull() {
        manager.setSelectedAppointment(null);
        assertTrue(
                "Setting the selected appointment to null should have no effect",
                manager.getAppointments().isEmpty());
    }

    @Test
    public void setSelectedAppointmentWontSelectIfAppointmentNotPresent() {
        assertTrue("Appointments should be empty",
                manager.getAppointments().isEmpty());
        assertNull("No selected appointment expected",
                manager.getSelectedAppointment());
        manager.setSelectedAppointment(new Appointment());
        assertNull(
                "No selected appointment expected is not in the collection yet",
                manager.getSelectedAppointment());
    }

    @Test
    public void setSelectedAppointment() {
        Appointment toBeSelected = new Appointment();
        manager.addAppointment(toBeSelected);
        assertNull("Appointment should not be selected yet",
                manager.getSelectedAppointment());
        manager.setSelectedAppointment(toBeSelected);
        assertNotNull("Appointment should be selected now",
                manager.getSelectedAppointment());
        assertSame("Selected appointment is not the one configured",
                toBeSelected, manager.getSelectedAppointment());
    }

    @Test
    public void selectPreviousAppointmentSelectedIsNull() {
        manager.addAppointment(new Appointment());
        assertFalse(
                "Having no selected appointment must make the call not able to move to previous.",
                manager.selectPreviousAppointment());
        assertNull("No selected appointment should be set",
                manager.getSelectedAppointment());
    }

    @Test
    public void selectPreviousAppointmentCurrentIsTheFirst() {
        Appointment firstAppointment = new Appointment();
        manager.addAppointment(firstAppointment);
        manager.setSelectedAppointment(firstAppointment);
        manager.addAppointment(new Appointment());
        assertFalse(
                "Moving previous when the selected is the first should not be successful (true)",
                manager.selectPreviousAppointment());
        assertSame("Currently selected appointment should not have changed",
                firstAppointment, manager.getSelectedAppointment());
    }

    @Test
    public void selectPreviousAppointment() {
        Appointment firstAppointment = new Appointment();
        Appointment secondAppointment = new Appointment();
        manager.addAppointment(firstAppointment);
        manager.addAppointment(secondAppointment);
        manager.setSelectedAppointment(secondAppointment);
        assertTrue("Moving to the previous appointment should succeed.",
                manager.selectPreviousAppointment());
        assertSame("First appointment should now be the currently selected.",
                firstAppointment, manager.getSelectedAppointment());
    }

    @Test
    public void selectNextAppointmentSelectedIsNull() {
        manager.addAppointment(new Appointment());
        assertFalse(
                "Having no selected appointment must make the call not able to move to next.",
                manager.selectNextAppointment());
        assertNull("No selected appointment should be set",
                manager.getSelectedAppointment());
    }

    @Test
    public void selectNextAppointmentCurrentIsTheLast() {
        Appointment lastAppointment = new Appointment();
        manager.addAppointment(new Appointment());
        manager.addAppointment(lastAppointment);
        manager.setSelectedAppointment(lastAppointment);
        assertFalse(
                "Moving to next when the selected is the last should not be successful (true)",
                manager.selectNextAppointment());
        assertSame("Currently selected appointment should not have changed",
                lastAppointment, manager.getSelectedAppointment());
    }

    @Test
    public void selectNextAppointment() {
        Appointment firstAppointment = new Appointment();
        Appointment secondAppointment = new Appointment();
        manager.addAppointment(firstAppointment);
        manager.addAppointment(secondAppointment);
        manager.setSelectedAppointment(firstAppointment);
        assertTrue("Moving to the next appointment should succeed.",
                manager.selectNextAppointment());
        assertSame("Second appointment should now be the currently selected.",
                secondAppointment, manager.getSelectedAppointment());
    }
}