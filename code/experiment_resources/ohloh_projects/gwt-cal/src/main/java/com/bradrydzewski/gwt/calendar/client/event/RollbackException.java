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

/**
 * <code>RollbackException</code> can be thrown to rollback or cancel any
 * changes made and not yet committed at the time of an Event. <p></p> An
 * example is when an {@link com.bradrydzewski.gwt.calendar.client.Appointment}
 * is deleted by the end-user. A DeleteEvent is raised and the change can be
 * reversed by throwing the RollbackException.
 *
 * @author Brad Rydzewski
 */
public class RollbackException extends Exception {
   /**
    * Default empty constructor.
    */
   public RollbackException() {
   }
}
