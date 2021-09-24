package com.bradrydzewski.gwt.calendar.client.monthview;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manages the <code>AppointmentLayoutDescription</code> in a stack-like
 * structure arranged in layers. Layers are <code>0</code>-based (1nd. layer has
 * an index of 0, 2nd. has an index of 1).
 *
 * @author Carlos D. Morales
 */
public class AppointmentStackingManager {

   /**
    * The highest layer index that has been allocated by this manager.
    */
   private int highestLayer = 0;

   /**
    * The collection of <code>AppointmentLayoutDescription</code>s grouped by by
    * layer (map key).
    */
   private HashMap<Integer, ArrayList<AppointmentLayoutDescription>>
      layeredDescriptions =
      new HashMap<Integer, ArrayList<AppointmentLayoutDescription>>();

   private int layerOverflowLimit = Integer.MAX_VALUE;

   public void setLayerOverflowLimit(int layerOverflowLimit) {
      this.layerOverflowLimit = layerOverflowLimit;
   }

   /**
    * Associates the provided <code>description</code> to the first available
    * layer in the collection administered by this manager. This manager will
    * look up in the stack of layers (from lowest index to highest index) until
    * a layer that <em>does not have</em> any <code>AppointmentLayoutDescription</code>s
    * that overlap with the days that the description's appointment spans.
    *
    * @param description An appointment description object that can be laid on a
    *                    layer
    */
   public void assignLayer(AppointmentLayoutDescription description) {
      boolean layerAssigned;
      int currentlyInspectedLayer = 0;
      do {
         initLayer(currentlyInspectedLayer);
         layerAssigned = assignLayer(currentlyInspectedLayer, description);
         currentlyInspectedLayer++;
      } while (!layerAssigned);
   }

   /**
    * Returns the lowest layer index that is available on the specified
    * <code>day</code>.
    *
    * @param day The day index for which the lowest layer index will be
    *            attempted to identify
    * @return An integer representing the index of the layer (zero-based) for
    *         which an single day <code>Appointment</code> can be displayed on.
    */
   public int lowestLayerIndex(int day) {
      return (nextLowestLayerIndex(day, 0));
   }

   /**
    * Returns the lowest layer index <em>higher than</em> <code>fromLayer</code>
    * that is available on the specified <code>day</code>.
    *
    * @param day       The day index for which the lowest layer index will be
    *                  attempted to identify found
    * @param fromLayer The layer index <em>after</em> which the search for next
    *                  available layer should be started from
    * @return An integer representing the index of the layer (zero-based) for
    *         which an single day <code>Appointment</code> can be displayed on.
    */
   public int nextLowestLayerIndex(int day, int fromLayer) {
      boolean layerFound = false;
      int currentlyInspectedLayer = fromLayer;
      do {
         if (isLayerAllocated(currentlyInspectedLayer)) {
            if (overlapsWithDescriptionInLayer(
               layeredDescriptions.get(currentlyInspectedLayer), day,
               day)) {
               currentlyInspectedLayer++;
            } else {
               layerFound = true;
            }
         } else {
            layerFound = true;
         }

      } while (!layerFound);
      return currentlyInspectedLayer;
   }

   /**
    * Returns all the <code>AppointmentLayoutDescription</code>s in the
    * specified layer.
    *
    * @param layerIndex The index of a layer for which descriptions will be
    *                   returned
    * @return The collection of appointment descriptions in the layer,
    *         <code>null</code> if no appointment has been allocated for the
    *         layer at all
    */
   public ArrayList<AppointmentLayoutDescription> getDescriptionsInLayer(
      int layerIndex) {
      return layeredDescriptions.get(layerIndex);
   }

   /**
    * Verifies if the range defined by <code>start</code>-<code>end</code>
    * overlaps with any of the appointment descriptions in
    * <code>layerDescriptions</code>.
    *
    * @param layerDescriptions All the <code>AppointmentLayoutDescription</code>
    *                          in a single layer.
    * @param start             The first day of the week in the range to test
    * @param end               The last day of the week in the range to test
    * @return <code>true</code> if any appointment description in
    *         <code>layerDescriptions</code> overlaps with the specified range,
    *         <code>false</code> otherwise.
    */
   private boolean overlapsWithDescriptionInLayer(
      ArrayList<AppointmentLayoutDescription> layerDescriptions, int start,
      int end) {
      if (layerDescriptions != null) {
         for (AppointmentLayoutDescription description : layerDescriptions) {
            if (description.overlapsWithRange(start, end)) {
               return true;
            }
         }
      }
      return false;
   }

   private boolean assignLayer(int layer,
      AppointmentLayoutDescription description) {
      ArrayList<AppointmentLayoutDescription> layerDescriptions =
         layeredDescriptions.get(layer);

      boolean assigned = false;
      if (!overlapsWithDescriptionInLayer(layerDescriptions,
                                          description.getWeekStartDay(),
                                          description.getWeekEndDay())) {
         highestLayer = Math.max(highestLayer, layer);
         if (layer > layerOverflowLimit &&
            description.spansMoreThanADay()) {
            AppointmentLayoutDescription split = description.split();
            layerDescriptions.add(description);
            assignLayer(split);
         } else {
            layerDescriptions.add(description);
         }

         assigned = true;
      }
      return assigned;
   }

   /**
    * Indicates whether a specific layerIndex with index <code>layerIndex</code>
    * has been allocated in the <code>layeredDescriptions</code> map.
    *
    * @param layerIndex The index of a layer to verify
    * @return <code>true</code> if the <code>layeredDescriptions</code> map has
    *         an entry (<code>List&lt;AppointmentLayoutDescription&gt;</code>)
    *         for the <code>layerIndex</code>.
    */
   private boolean isLayerAllocated(int layerIndex) {
      return layeredDescriptions.get(layerIndex) != null;
   }

   /**
    * Initializes the collection of descriptions for the layer with the
    * specified <code>layerIndex</code>.
    *
    * @param layerIndex The index of a layer to initialize
    */
   private void initLayer(int layerIndex) {
      if (!isLayerAllocated(layerIndex)) {
         layeredDescriptions.put(layerIndex,
                                 new ArrayList<AppointmentLayoutDescription>());
      }
   }

   /**
    * Returns the number of appointments (multi-day or all-day, as that's the
    * type of appointment that the stacking manager deals with only) that
    * exceeded the <code>layerOverflowLimit</code> value when they were
    * stacked.
    *
    * @param day The day to perform the count
    * @return The number of days that got a layer higher than the configured
    *         <code>layerOverflowLimit</code> layer, if there were any
    */
   public int multidayAppointmentsOverLimitOn(int day) {
      int count = 0;
      for (int layer = 0; layer <= highestLayer; layer++) {
         ArrayList<AppointmentLayoutDescription> descriptions =
            layeredDescriptions.get(layer);
         if (descriptions != null) {
            for (AppointmentLayoutDescription description : descriptions) {
               if (layer > layerOverflowLimit &&
                  description.overlapsWithRange(day, day)) {
                  count++;
               }
            }
         }
      }
      return count;
   }

   /**
    * Indicates whether there are <em>any</em> appointments that encompass the
    * specified <code>day</code>.
    *
    * @param day The day to test for appointments
    * @return <code>true</code> if there are any descriptions in any layer for
    *         the specified <code>day</code>.
    */
   public boolean areThereAppointmentsOn(int day) {
      boolean thereAre = false;
      for (int layersIndex = 0; layersIndex <= highestLayer; layersIndex++) {
         ArrayList<AppointmentLayoutDescription> layerDescriptions =
            layeredDescriptions.get(layersIndex);
         if (overlapsWithDescriptionInLayer(layerDescriptions, day, day)) {
            thereAre = true;
            break;
         }
      }
      return thereAre;
   }

   @Override public String toString() {
      StringBuilder managerState = new StringBuilder();
      for (int i = 0; i <= highestLayer; i++) {
         ArrayList<AppointmentLayoutDescription> descriptions =
            this.getDescriptionsInLayer(i);
         if (descriptions == null) continue;
         for (AppointmentLayoutDescription desc : descriptions) {
            managerState.append("[").append(i).append("]");
            for (int before = 0; before < desc.getWeekStartDay(); before++) {
               managerState.append("_");
            }
            for (int dur = desc.getWeekStartDay(); dur <= desc.getWeekEndDay();
                 dur++) {
               managerState.append("X");
            }
            for (int after = desc.getWeekEndDay(); after < 6; after++) {
               managerState.append("_");
            }
            managerState.append(" ->").append(desc).append("\n");
         }
      }
      return managerState.toString();
   }
}
