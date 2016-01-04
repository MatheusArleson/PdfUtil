package br.com.xavier.pdf.util;

import java.net.MalformedURLException;
import java.net.URL;

public class StringValidator {	
	private StringValidator(){}
	
	public static boolean validateString(String str){
		if(str != null && !str.trim().isEmpty()){
			return true;
		}
		return false;
	}
	
	public static boolean validateNumericString(String numericString){
		if(!validateString(numericString)){
			return false;
		}
		
		if(!numericString.matches("^[0-9]+$")){
			return false;
		}
		
		try {
			Integer.parseInt(numericString);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public static boolean validateBooleanString(String booleanString){
		if(!validateString(booleanString)){
			return false;
		}
		
		if(booleanString.equalsIgnoreCase("true") || booleanString.equalsIgnoreCase("false")){
			return true;
		} else {
			return false;
		}
	}

	public static boolean validateUrlString(String urlString, boolean validateProtocol) {
		if(!validateString(urlString)){
			return false;
		}
		
		try {
			URL url = new URL(urlString);
			if(validateProtocol){
				return validateString(url.getProtocol());
			}
			return true;
		} catch(MalformedURLException e) {
			return false;
		}
	}
}