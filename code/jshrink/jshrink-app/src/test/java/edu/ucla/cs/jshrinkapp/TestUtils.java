package edu.ucla.cs.jshrinkapp;

import edu.ucla.cs.jshrinklib.reachability.MethodData;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtils {
	public static MethodData getMethodDataFromSignature(String signature) throws IOException {
		signature = signature.trim();
		if(!signature.startsWith("<") || !signature.endsWith(">")){
			throw new IOException("Signature must start with with '<' and end with '>'");
		}

		signature = signature.substring(1,signature.length()-1);
		String[] signatureSplit = signature.split(":");

		if(signatureSplit.length != 2){
			throw new IOException("Method signature must be in format of " +
				"'<[classname]:[public?] [static?] [returnType] [methodName]([args...?])>'");
		}

		String clName = signatureSplit[0];
		String methodString = signatureSplit[1];

		boolean publicMethod;
		if (methodString.toLowerCase().contains("public")) publicMethod = true;
		else publicMethod = false;
		boolean staticMethod = methodString.toLowerCase().contains("static");

		Pattern pattern = Pattern.compile("<?([a-zA-Z][a-zA-Z0-9_]*>?)(\\(.*\\))");
		Matcher matcher = pattern.matcher(methodString);

		if(!matcher.find()){
			throw new IOException("Could not find a method matching our regex pattern ('" + pattern.toString() + "')");
		}

		String method = matcher.group();
		String methodName = method.substring(0,method.indexOf('('));
		String[] methodArgs = method.substring(method.indexOf('(')+1, method.lastIndexOf(')'))
			.split(",");

		for(int i=0; i<methodArgs.length; i++){
			methodArgs[i] = methodArgs[i].trim();
		}

		if(methodArgs.length == 1 && methodArgs[0].isEmpty()){ //For case "... method();
			methodArgs = new String[0];
		}

		String[] temp = methodString.substring(0, methodString.indexOf(methodName)).trim().split("\\s+");
		String methodReturnType = temp[temp.length-1];

		return new MethodData(methodName,clName,methodReturnType, methodArgs, publicMethod, staticMethod);
	}
}
