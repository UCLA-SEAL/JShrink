/*
 * Copyright (C) 2009 Leandro de Oliveira Aparecido <lehphyro@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.configprocessor.maven;

import java.io.*;
import java.util.*;

import org.apache.maven.artifact.factory.*;
import org.apache.maven.artifact.repository.*;
import org.apache.maven.artifact.resolver.*;
import org.apache.maven.project.*;
import org.apache.maven.shared.io.location.*;

import com.google.code.configprocessor.io.*;
import com.google.code.configprocessor.log.*;

public class MavenFileResolver implements FileResolver {

	private Locator locator;
	private LogAdapter logAdapter;
	
	public MavenFileResolver(MavenProject mavenProject, ArtifactFactory artifactFactory, ArtifactResolver artifactResolver, ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories, LogAdapter logAdapter) {
		this.logAdapter = logAdapter;
		locator = new Locator();
		List<LocatorStrategy> strategies = new ArrayList<LocatorStrategy>();
		strategies.add(new RelativeFileLocatorStrategy(mavenProject));
		strategies.add(new ClasspathResourceLocatorStrategy());
		strategies.add(new ArtifactLocatorStrategy(artifactFactory, artifactResolver, localRepository, remoteRepositories));
		strategies.add(new URLLocatorStrategy());
		locator.setStrategies(strategies);
	}

	public File resolve(String name) throws IOException {
		Location location = locator.resolve(name);
		if (location == null) {
			throw new IOException("File not found [" + name + "]\n" + locator.getMessageHolder().render());
		}
		
		try {
			File file = location.getFile();
			logAdapter.debug("Resolved [" + name + "] to file [" + file + "]");
			return file;
		} catch (IOException e) {
			throw new IOException("Failed to load file [" + name + "]\n" + locator.getMessageHolder().render());
		}
	}

}
