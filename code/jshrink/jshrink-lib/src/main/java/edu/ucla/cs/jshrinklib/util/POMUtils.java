package edu.ucla.cs.jshrinklib.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class POMUtils {
	public static String getArtifactId(String path) {
		File pom_file = new File(path);
		String artifact_id = null;
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
			XPathExpression expr = xpath.compile("/project/artifactId");
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            if(nodes.getLength() != 1) {
            	// the pom file should not contain more than one artifact id node
            	System.err.println("There are zero or multiple artifact ids in " + path);
            } else {
            	artifact_id = nodes.item(0).getTextContent();
            }
		} catch (SAXException | ParserConfigurationException | IOException | XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return artifact_id;
	}
	public static boolean addTestExclusionsToPOM(ArrayList<String> testClasses, File pom_file){
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
				Node arg_node = doc.createElement("excludes");
				for(String testClass:testClasses){
					Node test_node = doc.createElement("exclude");
					test_node.setTextContent(testClass);
					arg_node.appendChild(test_node);
				}

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
							System.err.println("[POMUtils] There are zero or multiple project nodes in the POM file. "
									+ "Please double check if it is correct.");
						}
					} else if (nodes3.getLength() == 1){
						// found the build node
						Node build_node = nodes3.item(0);
						build_node.appendChild(plugins_node);
					} else {
						System.err.println("[POMUtils] There are multiple build nodes within the project node in the POM file. "
								+ "Please double check if it is correct.");
					}
				} else if (nodes2.getLength() == 1) {
					// found the plugins node
					Node plugins_node = nodes2.item(0);
					plugins_node.appendChild(plugin_node);
				} else {
					System.err.println("[POMUtils] There are multiple plugins nodes within the build node in the POM file. "
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
						if(config_child.getNodeName().equals("excludes")) {
							arg_node = config_child;
							config_node.removeChild(arg_node);
							break;
						}
					}

					if(arg_node == null) {
						// no argLine option, insert it
						arg_node = doc.createElement("excludes");
					}
					for(String testClass:testClasses){
						Node test_node = doc.createElement("exclude");
						test_node.setTextContent(testClass);
						arg_node.appendChild(test_node);
					}
					config_node.appendChild(arg_node);

				} else {
					// no configuration node, insert it
					config_node = doc.createElement("configuration");
					Element arg_node = doc.createElement("excludes");
					for(String testClass:testClasses){
						Node test_node = doc.createElement("exclude");
						test_node.setTextContent(testClass);
						arg_node.appendChild(test_node);
					}
					config_node.appendChild(arg_node);
					config_node.appendChild(arg_node);
					plugin_node.appendChild(config_node);
				}
			} else {
				// is it possible to have two sunfire plugins？
				System.err.println("[POMUtils] There are more than one sunfire plugin in the POM file. "
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
			return false;
		}
		return true;
	}
	public static boolean addAgentToPOM(String injection, File pom_file){
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
				arg_node.setTextContent(injection);
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
							System.err.println("[POMUtils] There are zero or multiple project nodes in the POM file. "
									+ "Please double check if it is correct.");
						}
					} else if (nodes3.getLength() == 1){
						// found the build node
						Node build_node = nodes3.item(0);
						build_node.appendChild(plugins_node);
					} else {
						System.err.println("[POMUtils] There are multiple build nodes within the project node in the POM file. "
								+ "Please double check if it is correct.");
					}
				} else if (nodes2.getLength() == 1) {
					// found the plugins node
					Node plugins_node = nodes2.item(0);
					plugins_node.appendChild(plugin_node);
				} else {
					System.err.println("[POMUtils] There are multiple plugins nodes within the build node in the POM file. "
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
						arg_node.setTextContent(injection);
						config_node.appendChild(arg_node);
					} else {
						// already have argLine option, append the java agent after the existing options
						String arg_option = arg_node.getTextContent();
						// ignore the case where tamiflex has been added as a java agent
						if(!arg_option.contains(injection)) {
							if(arg_option.isEmpty()) {
								// add the java agent option directly
								arg_node.setTextContent(injection);
							} else {
								// append the new java agent option
								arg_option += " "+injection;
								arg_node.setTextContent(arg_option);
							}
						}
					}
				} else {
					// no configuration node, insert it
					config_node = doc.createElement("configuration");
					Element arg_node = doc.createElement("argLine");
					arg_node.setTextContent(injection);
					config_node.appendChild(arg_node);
					plugin_node.appendChild(config_node);
				}
			} else {
				// is it possible to have two sunfire plugins？
				System.err.println("[POMUtils] There are more than one sunfire plugin in the POM file. "
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
			return false;
		}
		return true;
	}
}
