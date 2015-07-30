package ru.eclipsetrader.transaq.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Id;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Utils {
	
	private static Logger logger = LogManager.getFormatterLogger(Utils.class);

	private static SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
	private static SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
	private static SAXParserFactory fb = SAXParserFactory.newInstance();
	
	public static Date truncDate(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
	
	public static Date parseDate(String value) {
		try {
			if (value != null) {
				return sdfDateTime.parse(value);
			} else {
				return null;
			}
		} catch (ParseException e) {
			logger.error("Cannot parse <%s> date", value);
			return null;
		}
	}
	
	private static void appendProperties(Object object, Class<?> class_, StringBuilder sb) throws IllegalArgumentException, IllegalAccessException {
		for (Field f : class_.getDeclaredFields()) {
			f.setAccessible(true);
			String fName = f.getName();
			if (!fName.toLowerCase().equals("owner")) {
				Object temp_obj = f.get(object);
				String value = (temp_obj != null ? temp_obj.toString() : "");
				sb.append("\t<"+ fName +">" + value + "<" + fName + "/>\n");
			}
		}
	}
	
	public static String toString(Object o) {
		
		if (o == null) {
			return "null";
		}
		
		StringBuilder sb = new StringBuilder();
		String className = o.getClass().getSimpleName().toLowerCase();
		sb.append("<"+className+">\n");
		try {
			Class<?> class_ = o.getClass();
			do {
				appendProperties(o, class_, sb);
				class_ = class_.getSuperclass();
			} while (!class_.equals(Object.class));
		} catch (Exception e) {
			sb.append(e.getMessage());
		}
		sb.append("</"+className+">\n");
		return sb.toString();
	}
	
	public static Date parseTime(String value) {
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.set(Calendar.HOUR, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			return new Date( c.getTimeInMillis() +  sdfTime.parse(value).getTime());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String formatDate(Date date) {
		if (date != null) {
			return sdfDateTime.format(date);
		} else {
			return null;
		}
	}
	
	public static String formatTime(Date date) {
		if (date != null) {
			return new SimpleDateFormat("HH:mm:ss.SSS").format(date);
		} else {
			return null;
		}
	}
	
	public static SAXParserFactory getSAXParserFactory(){
		return fb;
	}

	public static <T extends Serializable> T readObjectFromFile(String filename) { 
		try {
			InputStream file = new FileInputStream(filename);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			
			@SuppressWarnings("unchecked")
			T t = (T) input.readObject();
			input.close();
			buffer.close();
			file.close();
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException();
	}
	
	public static <T extends Serializable> void saveObjectToFile(T t, String filename) {
		try {
			OutputStream file = new FileOutputStream(filename);
		    OutputStream buffer = new BufferedOutputStream(file);
		    ObjectOutput output = new ObjectOutputStream(buffer);
		    output.writeObject(t);
		    output.close();
		    buffer.close();
		    file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static <T> T generateInternalClass(T t, Class<?> class_) throws IllegalArgumentException, IllegalAccessException {
		for (Field f : class_.getDeclaredFields()) {
			
			if (Modifier.isFinal(f.getModifiers())) {
				continue;
			}
			
			f.setAccessible(true);
			
			String value = f.getName();
			if (f.isAnnotationPresent(Id.class)) {
				value = UUID.randomUUID().toString();
			}
			
			if (f.getType().equals(String.class)) {
				f.set(t, value);
			} else if (f.getType().equals(Integer.class)) {
				f.set(t, (int)(Math.random() * 100000));
			} else if (f.getType().equals(Double.class)) {
				f.set(t, Math.random() * 1000);
			} else if (f.getType().equals(Long.class)) {
				f.set(t, Math.round(Math.random() * 100000));
			} else if (f.getType().equals(Date.class)) {
				f.set(t, new Date(System.currentTimeMillis() - Math.round(Math.random() * 10000)));
			} else if (f.getType().equals(Timestamp.class)) {
				f.set(t, new Timestamp(System.currentTimeMillis() - Math.round(Math.random() * 10000)));
			} else if (f.getType().isEnum()) {
				Object[] constants=f.getType().getEnumConstants();
				f.set(t, constants[(int)Math.round((constants.length-1) * Math.random())]);
			}
		}
		return t;
	}
	
	public static <T> T generateStub(T t) {
		try {
			Class<?> class_ = t.getClass();
			do {
				t = generateInternalClass(t, class_);
				class_ = class_.getSuperclass();
			} while (!class_.equals(Object.class));
			return t;
		} catch (Exception e) {
			e.printStackTrace();
			return t;
		}
	}

	public static String printArray(double[] data) {
		return printArray(data, null);
	}
	
	public static String printArray(double[] data, String format) {
		return printArray(ArrayUtils.toObject(data), format);
	}

	public static String printArray(Object[] data) {
		return printArray(data);
	}
	
	public static String printArray(Object[] data, String format) {
		StringBuilder sb = new StringBuilder();
		if (data != null) {
			for (Object d : data) {
				if (format != null) {
					sb.append(String.format(format, d));
				} else {
					sb.append(d);
				}
				sb.append(" ");
			}
		} else {
			sb.append("empty");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		/*Order o = new Order();
		System.out.println(generateStub(o));*/
		System.out.println(truncDate(new Date()));
	}
}
