#
# Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
#   - Redistributions of source code must retain the above copyright
#     notice, this list of conditions and the following disclaimer.
#
#   - Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
#   - Neither the name of Oracle nor the names of its
#     contributors may be used to endorse or promote products derived
#     from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
# IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
# THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
# PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
# PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
# LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

########################################################################
#
# Sample GNU Makefile for building JVMTI Demo mtrace
#
#  Example uses:    
#       gnumake JDK=<java_home> OSNAME=solaris [OPT=true] [LIBARCH=sparc]
#       gnumake JDK=<java_home> OSNAME=solaris [OPT=true] [LIBARCH=sparcv9]
#       gnumake JDK=<java_home> OSNAME=linux   [OPT=true]
#       gnumake JDK=<java_home> OSNAME=win32   [OPT=true]
#
########################################################################

# Source lists
LIBNAME=jmtrace
SOURCES=../agent_util_jshrink/agent_util.c ../java_crw_jshrink/java_crw_jshrink.c mtrace.c
JAVA_SOURCES=Mtrace.java

BUILD_DIR=../build

# Name of jar file that needs to be created
JARFILE=$(BUILD_DIR)/jmtrace.jar

# Linux GNU C Compiler
# GNU Compiler options needed to build it
COMMON_FLAGS=-fno-strict-aliasing -fPIC -fno-omit-frame-pointer
# Options that help find errors
COMMON_FLAGS+= -W -Wall  -Wno-unused -Wno-parentheses
ifeq ($(OPT), true)
	CFLAGS=-O2 $(COMMON_FLAGS) 
else
        CFLAGS=-g $(COMMON_FLAGS) 
endif
# Object files needed to create library
OBJECTS=$(SOURCES:.c=.o)
# Library name and options needed to build it
LIBRARY=$(BUILD_DIR)/lib$(LIBNAME).so
LDFLAGS=-Wl,-soname=$(LIBRARY) -static-libgcc
# Libraries we are dependent on
LIBRARIES=-L$(JDK)/jre/lib/$(LIBARCH) -lc
#LIBRARIES=-lc
# Building a shared library
LINK_SHARED=$(LINK.c) -shared -o $@

# Common -I options
CFLAGS += -I.
CFLAGS += -I../agent_util_jshrink
CFLAGS += -I../java_crw_jshrink
CFLAGS += -I$(JDK)/include -I$(JDK)/include/$(OSNAME)

# Default rule (build both native library and jar file)
all: $(LIBRARY) $(JARFILE)

# Build native library
$(LIBRARY): $(OBJECTS) $(BUILD_DIR)
	$(LINK_SHARED) $(OBJECTS) $(LIBRARIES)

# Build jar file
$(JARFILE): $(JAVA_SOURCES) $(BUILD_DIR)
	rm -f -r classes
	mkdir -p classes
	$(JDK)/bin/javac -d classes $(JAVA_SOURCES)
	(cd classes; $(JDK)/bin/jar cf ../$@ *)

$(BUILD_DIR):
	mkdir ../build

# Cleanup the built bits
clean:
	rm -f -r classes
	rm -f $(LIBRARY) $(JARFILE) $(OBJECTS) $(BUILD_DIR)

# Simple tester
test: all
	LD_LIBRARY_PATH=. $(JDK)/bin/java -agentlib:$(LIBNAME) -Xbootclasspath/a:./$(JARFILE) -version

