package edu.ucla.cs.jshrinklib.reachability;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.ucla.cs.jshrinklib.util.MavenUtils;

public class TamiFlexRunner {
	private String tamiflex_path;
	private String project_path;
	private boolean rerun;
	
	// classes that are referenced or instantiated via Java reflection
	public HashMap<String, HashSet<String>> accessed_classes;
	// fields that are referenced or accessed via Java reflection
	public HashMap<String, HashSet<String>> accessed_fields;
	// methods that are referenced or invoked via Java reflection
	public HashMap<String, Map<String, Set<String>>> used_methods;
	// methods that contain the invocation of the methods in used_methods
	public HashMap<String, Set<String>> used_methods_callers;
	
	public TamiFlexRunner(String tamiflexJarPath, String mavenProjectPath, boolean rerunTamiFlex) {
		this.tamiflex_path = tamiflexJarPath;
		this.project_path = mavenProjectPath;
		this.rerun = rerunTamiFlex;
		accessed_classes = new HashMap<String, HashSet<String>>();
		accessed_fields = new HashMap<String, HashSet<String>>();
		used_methods = new HashMap<String, Map<String, Set<String>>>();
		used_methods_callers = new HashMap<String, Set<String>>();
	}
	
	public void run() throws IOException {
		// double check if the tamiflex jar exists
		File tamiflex_jar = new File(tamiflex_path);
		if(tamiflex_jar.exists()) {
			// update the tamiflex jar path with the absolute path
			// because 'mvn test' is run in the root directory of the given project
			// a relative path will not work
			this.tamiflex_path = tamiflex_jar.getAbsolutePath();
		} else {
			System.err.println("[TamiFlexRunner] Error: the TamiFlex jar does not exist in " + tamiflex_path);
			return;
		}
		
		checkTamiFlexConfig();
		 
		// find all submodules if any
		HashMap<String, File> modules = new HashMap<String, File>();
		MavenUtils.getModules(new File(project_path), modules);
		
		boolean hasTamiFlexOutput = false;
		for(String artifact_id : modules.keySet()) {
			File dir = modules.get(artifact_id);
			File pom_file = new File(dir.getAbsolutePath() + File.separator + "pom.xml");
			
			// check if a tamiflex output folder exists
			File tamiflex_output = new File(dir.getAbsolutePath() + File.separator + "out");
			if(tamiflex_output.exists()) {
				// TamiFlex output exists
				if(rerun) {
					// delete existing tamiflex output if we want to rerun TamiFlex
					tamiflex_output.delete();
				} else {
					hasTamiFlexOutput = true;
				}
			}
			
			// save a copy of the pom file
			File copy = new File(pom_file.getAbsolutePath() + ".tmp");
			FileUtils.copyFile(pom_file, copy);
			
			// inject TamiFlex as the java agent in the POM file
			injectTamiFlex(pom_file.getAbsolutePath());
		}
		
		// run TamiFlex in the root
		try {
			if(!hasTamiFlexOutput) {
				// run 'mvn test'
				boolean testResult = runMavenTest();
				/*if(Application.isDebugMode() || Application.isVerboseMode()) {
					if(testResult) {
						System.out.println("[TamiFlexRunner] mvn test succeeds.");
					} else {
						System.out.println("[TamiFlexRunner] mvn test fails.");
					}
				}*/
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			// restore the modified pom files
			for(String artifact_id : modules.keySet()) {
				File dir = modules.get(artifact_id);
				File pom_file = new File(dir.getAbsolutePath() + File.separator + "pom.xml");
				File copy = new File(pom_file.getAbsolutePath() + ".tmp");
				FileUtils.copyFile(copy, pom_file);
			    copy.delete();
			}
		}
		
		// analyze the result
		for(String artifact_id : modules.keySet()) {
			File dir = modules.get(artifact_id);
			File tamiflex_output = new File(dir.getAbsolutePath() + File.separator + "out");
			if(tamiflex_output.exists()) {
				String log = tamiflex_output.getAbsolutePath() + File.separator + "refl.log";
				analyze(artifact_id, log);
			} else {
				// avoid false alarms since some modules are not built or do not have java classes
				// (e.g., resource module)

				/*
				[Bobby]: I've removed this. I found this code to be pretty secure. The only times this error was
				triggered was when the project has a "skipTests" flag in the POM file.
				 */

				/*File target_folder = new File(dir.getAbsolutePath() + File.separator + "target");
				File test_folder = new File(target_folder.getAbsolutePath() + File.separator + "test-classes");
				if(target_folder.exists() && test_folder.exists()) {
					System.err.println("[TamiFlexRunner] Error: TamiFlex does not run successfully. "
							+ "No output folder exists in " + dir.getAbsolutePath());
				}*/
			}
		}
	}
	
	public void checkTamiFlexConfig() throws IOException {
		// make sure the TamiFlex property file has the right configuration---dontDumpClasses set to true
		// and dontNormalize set to true
		String propFilePath = 
				System.getProperty("user.home") + File.separator + ".tamiflex" + File.separator + "poa.properties";
		File propFile = new File(propFilePath);
		if(!propFile.exists()) {
			// copy the default TamiFlex property file
			ClassLoader classLoader = TamiFlexRunner.class.getClassLoader();
			if(!propFile.getParentFile().exists()) {
				Files.createDirectory(propFile.getParentFile().toPath());
			}

			InputStream in = classLoader.getResourceAsStream("poa.properties");
			Files.copy(in, propFile.toPath());
		} else {
			// double check if the existing TamiFlex property file has the right configuration
			List<String> lines = FileUtils.readLines(propFile, Charset.defaultCharset());
			boolean isRewritten  = false;
			for(int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if(line.startsWith("dontDumpClasses")) {
					String value = line.substring(line.indexOf("=") + 1).trim();
					if(value.equals("false")) {
						lines.set(i, "dontDumpClasses = true");
						isRewritten = true;
					}
				} else if(line.startsWith("dontNormalize")) {
					String value = line.substring(line.indexOf("=") + 1).trim();
					if(value.equals("false")) {
						lines.set(i, "dontNormalize = true");
						isRewritten = true;
					}
				}
			}
			
			if(isRewritten) {
				FileUtils.writeLines(propFile, lines, false);
			}
		}
	}
	
	public void analyze(String module, String log) {
		File tamiflex_log = new File(log);
		if(tamiflex_log.exists()) {
			try {
				List<String> lines = FileUtils.readLines(tamiflex_log, Charset.defaultCharset());

				for(String line : lines) {
					String[] ss = line.split(";");
					String containing_method = ss[2];
					if(containing_method.equals("java.lang.Class.searchMethods") 
							|| containing_method.equals("java.lang.Class.searchFields")) {
						// ignore the java reflection calls in java.lang.Class.searchMethods
						// and java.lang.Class.searchFields, because these two methods iterate 
						// all method or field members in a class and find the one that match the given
						// name. Therefore, all members in that class will be marked as used. But
						// only the needed one is actually used after search.
						continue;
					}
					
					String reference = ss[1];
					if(reference.startsWith("[L")) {
						// sometimes it starts with [L, seems like a formating issue in TamiFlex
						reference = reference.substring(2);
					}
					
					if(reference.endsWith("[]")) {
						// this is an array type
						String base_type = reference.substring(0, reference.length() - 2);
						if(!isPrimitiveType(base_type)) {
							HashSet<String> set;
							if(accessed_classes.containsKey(module)) {
								set = accessed_classes.get(module);
							} else {
								set = new HashSet<String>();
							}
							set.add(base_type);
							accessed_classes.put(module, set);
						}
					} else if (reference.startsWith("<") && reference.endsWith(">")) {
						reference = reference.substring(1, reference.length() - 1);
						// this is either a field or a method 
//						String class_name = reference.split(": ")[0];
						String class_member = reference.split(": ")[1];
						if(class_member.contains("(") && class_member.contains(")")) {
							// this is a method in the format of "return_type method_subsignature"
							Map<String, Set<String>> set;
							if(used_methods.containsKey(module)) {
								set = used_methods.get(module);
							} else {
								set = new HashMap<String, Set<String>>();
							}

							if(!set.containsKey(reference)){
								set.put(reference, new HashSet<String>());
							}

							if(!containing_method.isEmpty()){
								set.get(reference).add(containing_method);


								if(!used_methods_callers.containsKey(module)){
									used_methods_callers.put(module, new HashSet<String>());
								}

								used_methods_callers.get(module).add(containing_method);
							}
							used_methods.put(module, set);

						} else {
							// this is a field in the format of "field_type field_name"
							HashSet<String> set;
							if(accessed_fields.containsKey(module)) {
								set = accessed_fields.get(module);
							} else {
								set = new HashSet<String>();
							}
							set.add(reference);
							accessed_fields.put(module, set);
						}
					} else {
						// this is a class type
						HashSet<String> set;
						if(accessed_classes.containsKey(module)) {
							set = accessed_classes.get(module);
						} else {
							set = new HashSet<String>();
						}
						set.add(reference);
						accessed_classes.put(module, set);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("[TamiFlexRunner] Error: There is no TamiFlex log file - " + log);
		}
	}
	
	private boolean isPrimitiveType(String t) {
		if(t.equals("boolean") || t.equals("byte") || t.equals("char") || t.equals("short")
				|| t.equals("int") || t.equals("long") || t.equals("float") || t.equals("double")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean runMavenTest() throws IOException, InterruptedException {
		String[] cmd = {"mvn", "-f" , (new File(project_path)).getAbsolutePath() ,"test", "-fn", "--batch-mode"};
		ProcessBuilder processBuilder = new ProcessBuilder(cmd);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		InputStream stdout = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdout);
		BufferedReader br = new BufferedReader(isr);

		boolean testResult = false;
		String output = null;
		while ((output = br.readLine()) != null) {
			if(output.contains("BUILD SUCCESS")) {
				testResult = true;
			} else if (output.contains("BUILD FAILURE")) {
				testResult = false;
			}
		}
		process.waitFor();
		
		return testResult;
	}
	
	/**
	 * 
	 * Inject TamiFlex as a java agent in the test plugin in the given POM file
	 * 
	 * @param path
	 */
	public void injectTamiFlex(String path) {
		File pom_file = new File(path);
		try {
			// parse the pom file as xml
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(pom_file);
			doc.getDocumentElement().normalize();
			
			// build the xpath to locate the artifact id
			// note that the parent node also contains a artifactId node
			// so we need to specify that the artifactId node we are looking 
			// for is under the project node
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("/project/build/plugins/plugin/artifactId[text()=\"maven-surefire-plugin\"]");
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            if(nodes.getLength() == 0) {
            	// This POM does not declare the sunfire plugin
            	// We must declare one explicitly together with the java agent argument
            	Node plugin_node = doc.createElement("plugin");
            	
            	Node groupId_node = doc.createElement("groupId");
            	groupId_node.setTextContent("org.apache.maven.plugins");
            	
            	Node artifactId_node = doc.createElement("artifactId");
            	artifactId_node.setTextContent("maven-surefire-plugin");
            	
            	Node version_node = doc.createElement("version");
            	version_node.setTextContent("2.20.1");
            	
            	Node config_node = doc.createElement("configuration");
        		Node arg_node = doc.createElement("argLine");
        		arg_node.setTextContent("-javaagent:" + tamiflex_path);
        		config_node.appendChild(arg_node);
        		
        		plugin_node.appendChild(groupId_node);
        		plugin_node.appendChild(artifactId_node);
        		plugin_node.appendChild(version_node);
        		plugin_node.appendChild(config_node);
        		
        		XPathExpression expr2 = xpath.compile("/project/build/plugins");
        		NodeList nodes2 = (NodeList) expr2.evaluate(doc, XPathConstants.NODESET);
        		if(nodes2.getLength() == 0) {
        			// no plugins node, must inject one
        			Node plugins_node = doc.createElement("plugins");
        			plugins_node.appendChild(plugin_node);
        			
        			XPathExpression expr3 = xpath.compile("/project/build");
        			NodeList nodes3 = (NodeList) expr3.evaluate(doc, XPathConstants.NODESET);
        			if(nodes3.getLength() == 0) {
        				// no build node, must inject one
        				Node build_node = doc.createElement("build");
        				build_node.appendChild(plugins_node);
        				
        				XPathExpression expr4 = xpath.compile("/project");
        				NodeList nodes4 = (NodeList) expr4.evaluate(doc, XPathConstants.NODESET);
        				if(nodes4.getLength() == 1) {
        					Node project_node = nodes4.item(0);
        					project_node.appendChild(build_node);
        				} else {
        					System.err.println("[TamiFlexRunner] There are zero or multiple project nodes in the POM file. "
        							+ "Please double check if it is correct.");
        				}
        			} else if (nodes3.getLength() == 1){
        				// found the build node
        				Node build_node = nodes3.item(0);
        				build_node.appendChild(plugins_node);
        			} else {
        				System.err.println("[TamiFlexRunner] There are multiple build nodes within the project node in the POM file. "
    							+ "Please double check if it is correct.");
        			}
        		} else if (nodes2.getLength() == 1) {
        			// found the plugins node
        			Node plugins_node = nodes2.item(0);
        			plugins_node.appendChild(plugin_node);
        		} else {
        			System.err.println("[TamiFlexRunner] There are multiple plugins nodes within the build node in the POM file. "
							+ "Please double check if the POM file is correct.");
        		}
            } else if (nodes.getLength() == 1) {
            	// This POM declares the sunfire plugin explicitly
            	// We only need to inject the java agent argument
            	Node plugin_node = nodes.item(0).getParentNode();
            	NodeList children = plugin_node.getChildNodes();
            	Node config_node = null;
            	for(int i = 0; i < children.getLength(); i++) {
            		Node child = children.item(i);
            		if(child.getNodeName().equals("configuration")) {
            			// configuration node exists
            			config_node = child;
            			break;
            		}
            	}
            	
            	if(config_node != null) {
            		NodeList config_children = config_node.getChildNodes();
        			Node arg_node = null;
        			for(int j = 0; j < config_children.getLength(); j++) {
        				Node config_child = config_children.item(j);
        				if(config_child.getNodeName().equals("argLine")) {
        					arg_node = config_child;
        					break;
        				}
        			}
        			
        			if(arg_node == null) {
        				// no argLine option, insert it
        				arg_node = doc.createElement("argLine");
        				arg_node.setTextContent("-javaagent:" + tamiflex_path);
        				config_node.appendChild(arg_node);
        			} else {
        				// already have argLine option, append the java agent after the existing options
        				String arg_option = arg_node.getTextContent();
        				// ignore the case where tamiflex has been added as a java agent
        				if(!arg_option.contains(tamiflex_path)) {
        					if(arg_option.isEmpty()) {
        						// add the java agent option directly
        						arg_node.setTextContent("-javaagent:" + tamiflex_path);
        					} else {
        						// append the new java agent option
        						arg_option += " -javaagent:" + tamiflex_path;
        						arg_node.setTextContent(arg_option);
        					}
        				}
        			}
            	} else {
            		// no configuration node, insert it
            		config_node = doc.createElement("configuration");
            		Element arg_node = doc.createElement("argLine");
            		arg_node.setTextContent("-javaagent:" + tamiflex_path);
            		config_node.appendChild(arg_node);
            		plugin_node.appendChild(config_node);
            	}
            } else {
            	// is it possible to have two sunfire plugins？
            	System.err.println("[TamiFlexRunner] There are more than one sunfire plugin in the POM file. "
            			+ "Please double check if it is correct.");
            }
            
            // rewrite the POM file
    		TransformerFactory transformerFactory = TransformerFactory.newInstance();
    		Transformer transformer = transformerFactory.newTransformer();
    		DOMSource source = new DOMSource(doc);
    		StreamResult result = new StreamResult(pom_file);
    		transformer.transform(source, result);
		} catch (SAXException | ParserConfigurationException 
				| IOException | XPathExpressionException | TransformerException e) {
			e.printStackTrace();
		}
	}
}
