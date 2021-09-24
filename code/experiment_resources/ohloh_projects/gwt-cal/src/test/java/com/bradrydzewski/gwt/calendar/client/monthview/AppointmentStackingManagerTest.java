package com.bradrydzewski.gwt.calendar.client.monthview;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test cases for the {@link com.bradrydzewski.gwt.calendar.client.monthview.AppointmentStackingManager}
 * component.
 *
 * @author Carlos D. Morales
 */
public class AppointmentStackingManagerTest {

   private AppointmentStackingManager stackManager =
      new AppointmentStackingManager();

   @Test
   public void assignFullWeekDescriptions() {
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 6, null));
      assertEquals("Expected 1 description in the first layer", 1,
                   stackManager.getDescriptionsInLayer(0).size());
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 6, null));
      assertEquals("Expected 1 description in the first layer", 1,
                   stackManager.getDescriptionsInLayer(0).size());
      assertEquals("Expected 1 description in the second layer", 1,
                   stackManager.getDescriptionsInLayer(1).size());
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 6, null));
      assertEquals("Expected 1 description in the second layer", 1,
                   stackManager.getDescriptionsInLayer(2).size());
   }

   @Test
   public void assignTwoDescriptionsInLayer() {
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 2, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(3, 6, null));
      assertEquals("Expected 2 descriptions in the first layer", 2,
                   stackManager.getDescriptionsInLayer(0).size());
   }

   @Test
   public void assignSevenDescriptionsPerLayer() {
      stackManager.assignLayer(new AppointmentLayoutDescription(0, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(1, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(2, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(3, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(4, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(5, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(6, null));

      assertEquals("Expected 7 descriptions in the first layer", 7,
                   stackManager.getDescriptionsInLayer(0).size());
   }

   @Test
   public void assignPartiallyOverlappingDescriptionsResultsInTwoLayers() {
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 3, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(3, 6, null));
      assertEquals("Expected 1 descriptions in the first layer", 1,
                   stackManager.getDescriptionsInLayer(0).size());
      assertEquals("Expected 1 descriptions in the second layer", 1,
                   stackManager.getDescriptionsInLayer(1).size());
   }

   @Test
   public void oneInLayerOneAndTwoInLayerTwo() {
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 6, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 3, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(4, 6, null));
      assertEquals("Expected 1 descriptions in the first layer", 1,
                   stackManager.getDescriptionsInLayer(0).size());
      assertEquals("Expected 2 descriptions in the second layer", 2,
                   stackManager.getDescriptionsInLayer(1).size());
   }

   @Test
   public void buildBrickWall() {
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 1, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(1, 2, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(3, 4, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(4, 5, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(6, 6, null));

      assertCurrentlyAvailableStackingOrder(0, 1);
      assertCurrentlyAvailableStackingOrder(1, 2);
      assertCurrentlyAvailableStackingOrder(2, 0);
      assertCurrentlyAvailableStackingOrder(3, 1);
      assertCurrentlyAvailableStackingOrder(4, 2);
      assertCurrentlyAvailableStackingOrder(5, 0);
      assertCurrentlyAvailableStackingOrder(6, 1);

      assertEquals("Expected 3 descriptions in the first layer", 3,
                   stackManager.getDescriptionsInLayer(0).size());
      assertEquals("Expected 2 descriptions in the second layer", 2,
                   stackManager.getDescriptionsInLayer(1).size());
   }

   @Test
   public void singleDaysSlipThroughMultidays() {
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 1, null));

      assertCurrentlyAvailableStackingOrder(0, 2);
      assertCurrentlyAvailableStackingOrder(1, 0);

      assertEquals("Next available layer should be after the 2nd. multi-day",
                   2, stackManager.nextLowestLayerIndex(1, 1));
   }

   @Test
   public void multidayAboveMaxLayerGetsSplit() {
      stackManager.setLayerOverflowLimit(2);
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 5, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 5, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 5, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 6, null));

      assertCurrentlyAvailableStackingOrder(0, 4);
      assertCurrentlyAvailableStackingOrder(1, 4);
      assertCurrentlyAvailableStackingOrder(2, 4);
      assertCurrentlyAvailableStackingOrder(3, 4);
      assertCurrentlyAvailableStackingOrder(4, 4);
      assertCurrentlyAvailableStackingOrder(5, 4);
      assertCurrentlyAvailableStackingOrder(6, 1);

   }

   @Test
   public void multidayLandsPike() {

      stackManager.setLayerOverflowLimit(2);

      stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));

      stackManager.assignLayer(new AppointmentLayoutDescription(1, 1, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(1, 1, null));

      stackManager.assignLayer(new AppointmentLayoutDescription(0, 1, null));

      assertCurrentlyAvailableStackingOrder(0, 4);
      assertCurrentlyAvailableStackingOrder(1, 3);
   }

   @Test
   public void multidayAppointmentsOverLimitOn_OneOverflown() {
      stackManager.setLayerOverflowLimit(2);

      stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 1, null));

      assertEquals("One appointment is above limit", 1,
                   stackManager.multidayAppointmentsOverLimitOn(0));
   }

   @Test
   public void multidayAppointmentsOverLimitOn_NoOverflown() {
      stackManager.setLayerOverflowLimit(2);

      stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));

      assertEquals("One appointment is above limit", 0,
                   stackManager.multidayAppointmentsOverLimitOn(0));
   }

   @Test
   public void multidayAppointmentsOverLimitOn_LimitNotSet() {

      for(int i = 0; i < 100; i++){
         stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));
      }

      assertEquals("One appointment is above limit", 0,
                   stackManager.multidayAppointmentsOverLimitOn(0));
   }



   @Test
   public void nextLowestLayerIndex() {
      // Assume no multi-day are on the day
      assertEquals("Def. Lowest Layer", 0, stackManager.lowestLayerIndex(0));
      assertEquals("1st Layer", 0, stackManager.nextLowestLayerIndex(0, 0));
      assertEquals("2nd Layer", 1, stackManager.nextLowestLayerIndex(0, 1));
      assertEquals("3rd Layer", 2, stackManager.nextLowestLayerIndex(0, 2));
      assertEquals("4th Layer", 3, stackManager.nextLowestLayerIndex(0, 3));
   }

   @Test
   public void nextLowestLayerIndexInterspersedWithMultidayAppointments() {

      stackManager.assignLayer(new AppointmentLayoutDescription(0, 1, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 0, null));
      stackManager.assignLayer(new AppointmentLayoutDescription(0, 1, null));

      assertCurrentlyAvailableStackingOrder(0, 3);
      assertCurrentlyAvailableStackingOrder(1, 1);

      assertEquals("Def. Lowest Layer", 1, stackManager.lowestLayerIndex(1));
      assertEquals("Third multi-day leaves next available layer 3",
                   3, stackManager.nextLowestLayerIndex(0, 1));
   }

   private void assertCurrentlyAvailableStackingOrder(int day, int expected) {
      assertEquals("Unexpected lowest stack order for day " + day, expected,
                   stackManager.lowestLayerIndex(day));
   }
}
