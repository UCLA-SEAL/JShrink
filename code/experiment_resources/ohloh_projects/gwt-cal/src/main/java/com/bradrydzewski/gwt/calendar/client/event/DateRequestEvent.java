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
 * @author Brad Rydzewski
 */
public class DateRequestEvent<T> extends GwtEvent<DateRequestHandler<T>> {

  /**
   * Handler type.
   */
  private static Type<DateRequestHandler<?>> TYPE;

  /**
   * Fires a open event on all registered handlers in the handler manager.If no
   * such handlers exist, this method will do nothing.
   * 
   * @param <T> the target type
   * @param source the source of the handlers
   * @param target the target
   */
  public static <T> void fire(HasDateRequestHandlers<T> source, T target) {
    fire(source, target, null);
  }

  public static <T> void fire(HasDateRequestHandlers<T> source, T target, Object widget) {
	    if (TYPE != null) {
	      DateRequestEvent<T> event = new DateRequestEvent<T>(target, widget);
	      source.fireEvent(event);
	    }
	  }
  
  /**
   * Gets the type associated with this event.
   * 
   * @return returns the handler type
   */
  public static Type<DateRequestHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<DateRequestHandler<?>>();
    }
    return TYPE;
  }

  private final T target;
  private final Object clicked;

  /**
   * Creates a new delete event.
   * 
   * @param target the ui object being opened
   */
  protected DateRequestEvent(T target, Object clicked) {
    this.target = target;
    this.clicked = clicked;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final Type<DateRequestHandler<T>> getAssociatedType() {
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
  
  public Object getClicked() {
	  return clicked;
  }

  // Because of type erasure, our static type is
  // wild carded, yet the "real" type should use our I param.

  @Override
  protected void dispatch(DateRequestHandler<T> handler) {
        handler.onDateRequested(this);
  }

}
