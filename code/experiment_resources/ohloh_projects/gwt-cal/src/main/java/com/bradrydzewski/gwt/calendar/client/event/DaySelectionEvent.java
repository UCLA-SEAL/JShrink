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
 * Represents a selection event.
 * 
 * @param <T>
 *            the type being selected
 */
public class DaySelectionEvent<T> extends GwtEvent<DaySelectionHandler<T>> {

	/**
	 * Handler type.
	 */
	private static Type<DaySelectionHandler<?>> TYPE;

	/**
	 * Fires a selection event on all registered handlers in the handler
	 * manager.If no such handlers exist, this method will do nothing.
	 * 
	 * @param <T>
	 *            the selected item type
	 * @param source
	 *            the source of the handlers
	 * @param selectedItem
	 *            the selected item
	 */
	public static <T> void fire(HasDaySelectionHandlers<T> source, T selectedItem) {
		if (TYPE != null) {
			DaySelectionEvent<T> event = new DaySelectionEvent<T>(
					selectedItem);
			source.fireEvent(event);
		}
	}

	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<DaySelectionHandler<?>> getType() {
		if (TYPE == null) {
			TYPE = new Type<DaySelectionHandler<?>>();
		}
		return TYPE;
	}

	private final T selectedItem;

	/**
	 * Creates a new selection event.
	 * 
	 * @param selectedItem
	 *            selected item
	 */
	protected DaySelectionEvent(T selectedItem) {
		this.selectedItem = selectedItem;
	}

	// The instance knows its BeforeSelectionHandler is of type I, but the TYPE
	// field itself does not, so we have to do an unsafe cast here.
	@SuppressWarnings("unchecked")
	@Override
	public final Type<DaySelectionHandler<T>> getAssociatedType() {
		return (Type) TYPE;
	}

	/**
	 * Gets the selected item.
	 * 
	 * @return the selected item
	 */
	public T getSelectedItem() {
		return selectedItem;
	}

	@Override
	protected void dispatch(DaySelectionHandler<T> handler) {
		handler.onSelection(this);
	}
}
