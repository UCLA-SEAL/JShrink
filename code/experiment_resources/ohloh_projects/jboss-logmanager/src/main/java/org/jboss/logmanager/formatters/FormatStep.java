/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logmanager.formatters;

import org.jboss.logmanager.ExtLogRecord;

/**
 * A single format step which handles some part of rendering a log record.
 */
public interface FormatStep {

    /**
     * Render a part of the log record.
     *
     * @param builder the string builder to append to
     * @param record the record being rendered
     */
    void render(StringBuilder builder, ExtLogRecord record);

    /**
     * Emit an estimate of the length of data which this step will produce.  The more accurate the estimate, the
     * more likely the format operation will be performant.
     *
     * @return an estimate
     */
    int estimateLength();
}
