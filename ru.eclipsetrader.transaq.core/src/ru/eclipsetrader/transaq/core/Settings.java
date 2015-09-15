package ru.eclipsetrader.transaq.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;


public class Settings {

	public static boolean SHOW_CONSOLE_TRACE = false;
	
	
	public static void main(String[] args) throws InvalidPropertiesFormatException, IOException {
		
		Properties props = new Properties();
		
		InputStream is = new FileInputStream("c:/email-configuration.xml");
		//load the xml file into properties format
		props.loadFromXML(is);
		
		String email = props.getProperty("email.support");
		
		System.out.println(email);
	}
}
