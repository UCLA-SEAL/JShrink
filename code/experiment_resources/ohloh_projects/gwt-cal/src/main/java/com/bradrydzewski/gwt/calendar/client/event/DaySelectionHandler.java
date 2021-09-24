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

import com.google.gwt.event.shared.EventHandler;


/**
 * Handler interface for {@link DaySelectionEvent} events.
 * 
 * @param <T> the type being opened
 */
public interface DaySelectionHandler<T> extends EventHandler {

  /**
   * Called when {@link DaySelectionEvent} is fired.
   * 
   * @param event the {@link DaySelectionEvent} that was fired
   */
  void onSelection(DaySelectionEvent<T> event);
}
