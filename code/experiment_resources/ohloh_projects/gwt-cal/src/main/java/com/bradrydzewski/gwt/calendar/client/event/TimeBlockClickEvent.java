/*
 * This file is part of gwt-cal
 * Copyright (C) 2010  Scottsdale Software LLC
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
package com.bradrydzewski.gwt.calendar.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * TODO: Complete JavaDoc comments.
 *
 * @author Brad Rydzewski
 */
public class TimeBlockClickEvent<T> extends GwtEvent<TimeBlockClickHandler<T>> {

   /**
    * Handler type.
    */
   private static Type<TimeBlockClickHandler<?>> TYPE;


   /**
    * Fires a open event on all registered handlers in the handler manager.If no
    * such handlers exist, this method will do nothing.
    *
    * @param <T>    the target type
    * @param source the source of the handlers
    * @param target the target
    */
   public static <T> void fire(HasTimeBlockClickHandlers<T> source, T target) {
      if (TYPE != null) {
         TimeBlockClickEvent<T> event = new TimeBlockClickEvent<T>(target);
         source.fireEvent(event);
      }
   }

   /**
    * Gets the type associated with this event.
    *
    * @return returns the handler type
    */
   public static Type<TimeBlockClickHandler<?>> getType() {
      if (TYPE == null) {
         TYPE = new Type<TimeBlockClickHandler<?>>();
      }
      return TYPE;
   }

   private final T target;

   /**
    * Creates a new delete event.
    *
    * @param target the ui object being opened
    */
   protected TimeBlockClickEvent(T target) {
      this.target = target;
   }

   @SuppressWarnings("unchecked")
   @Override
   public final Type<TimeBlockClickHandler<T>> getAssociatedType() {
      return (Type) TYPE;
   }

   /**
    * Gets the target.
    *
    * @return the target
    */
   public T getTarget() {
      return target;
   }

   // Because of type erasure, our static type is
   // wild carded, yet the "real" type should use our I param.

   @Override
   protected void dispatch(TimeBlockClickHandler<T> handler) {
      handler.onTimeBlockClick(this);
   }

}
