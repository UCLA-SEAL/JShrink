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

package org.jboss.logmanager;

/**
 * Log4j-like levels.
 */
public final class Level extends java.util.logging.Level {
    private static final long serialVersionUID = 491981186783136939L;

    protected Level(final String name, final int value) {
        super(name, value);
    }

    protected Level(final String name, final int value, final String resourceBundleName) {
        super(name, value, resourceBundleName);
    }

    public static final Level FATAL = new Level("FATAL", 1100);
    public static final Level ERROR = new Level("ERROR", 1000);
    public static final Level WARN = new Level("WARN", 900);
    public static final Level INFO = new Level("INFO", 800);
    public static final Level DEBUG = new Level("DEBUG", 500);
    public static final Level TRACE = new Level("TRACE", 400);
}
