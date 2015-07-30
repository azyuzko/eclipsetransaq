package ru.eclipsetrader.transaq.core.model.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import ru.eclipsetrader.transaq.core.model.BoardType;

/**
 * Gap для котировки по бумаге
 * @author Zyuzko-AA
 *
 */
public class SymbolGapMap {

	
	BoardType board;
	String seccode;
	Date time;
	
	HashMap<String, String> gaps = new HashMap<String, String>();
	
	public SymbolGapMap(Date time) {
		this.time = time;
	}

	public BoardType getBoard() {
		return board;
	}

	public void setBoard(BoardType board) {
		this.board = board;
	}

	public String getSeccode() {
		return seccode;
	}

	public void setSeccode(String seccode) {
		this.seccode = seccode;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public void put(String key, String value) {
		gaps.put(key, value);
	}

	public Set<String> keySet() {
		return gaps.keySet();
	}

	public String get(String key) {
		return gaps.get(key);
	}
	
	public Map<String, String> getGaps() {
		return gaps;
	}
	
	public byte[] getGapData() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(gaps);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return bos.toByteArray();
	}
	

	public static String mapToString(Map<String, String> map) {
		StringBuilder sb = new StringBuilder();

		for (String key : map.keySet()) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			String value = map.get(key);
			sb.append(key);
			sb.append("=");
			sb.append(value);
		}

		return sb.toString();
	}

	public static Map<String, String> stringToMap(String input) {
		Map<String, String> map = new HashMap<String, String>();

		if (input == null) {
			return map;
		}
		
		String[] nameValuePairs = input.split("\n");
		for (String nameValuePair : nameValuePairs) {
			String[] nameValue = nameValuePair.split("=");
			map.put(nameValue[0], nameValue[1]);

		}

		return map;
	}
	
	public static void main(String[] args) {
		SymbolGapMap s = new SymbolGapMap(new Date());
		
		for (int i = 0; i < 1000; i++) {
			s.put("test"+i, String.valueOf(i));
		}
			
		long start = System.currentTimeMillis();
		String data = mapToString(s.getGaps());
		long end1 = System.currentTimeMillis();
		System.out.println(data.length());
		
		Map<String, String> result = stringToMap(data);
		long end2 = System.currentTimeMillis();
		for (String key : result.keySet()) {
			//System.out.println(key + " " + result.get(key));
		}
		
		System.out.println("end1 = " + (end1-start));
		System.out.println("end2 = " + (end2-end1));
	}

}
