package com.investing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class InvestingUtils {
	
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.dd.MM HH:mm");
	
	static String URL = "http://ru.investing.com/common/technical_summary/api.php?action=TSB_updatePairs&pairs=$1&timeframe=$2";


	public static Map<InvestingSymbol, InvestingRequest> parse(String data) {
		JSONObject obj = new JSONObject(data);
		// InvestingSecurity.get(symbol).getCode()
		Map<InvestingSymbol, InvestingRequest> result = new HashMap<InvestingSymbol, InvestingRequest>();
		for (String key : obj.keySet()) {
			InvestingSymbol invSymbol = InvestingSymbol.getInvSymbol(Integer.valueOf(key));
			if (invSymbol == null) continue;
			JSONObject symObj = obj.getJSONObject(key);
			JSONObject row = symObj.getJSONObject("row");
			String last = row.getString("last");
			String updateTime = symObj.getString("updateTime");
			double price = Double.valueOf(last.replace(".", "").replace(",", "."));
			String hint = row.getString("ma");
			InvestingRequest hr = new InvestingRequest();
			hr.date = new Date();
			hr.price = price;
			hr.signal = InvestingState.fromString(hint);
			try {
				Calendar c = Calendar.getInstance(); c.setTime(new Date()); 
				hr.lastUpdate = sdf.parse(c.get(Calendar.YEAR) + "." + updateTime);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			result.put(invSymbol, hr);
		}
		return result;
	}
	
	public static String callRest(List<InvestingSymbol> symbols, int period) {
		StringBuilder sb = new StringBuilder();
		for (InvestingSymbol symbol : symbols) {
			if (!sb.toString().isEmpty()) {
				sb.append(",");
			}
			sb.append(symbol.getCode());
		}
		String url = URL.replace("$1", sb.toString()).replace("$2", String.valueOf(period));
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
	
			int responseCode = con.getResponseCode();

			if (responseCode == 200) {
	
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				return response.toString();
			} else {
				return "Response code :" + responseCode;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String readFile( String file ) {
	    try (BufferedReader reader = new BufferedReader( new FileReader (file))) {
		    String         line = null;
		    StringBuilder  stringBuilder = new StringBuilder();
		    String         ls = System.getProperty("line.separator");
	
		    while( ( line = reader.readLine() ) != null ) {
		        stringBuilder.append( line );
		        stringBuilder.append( ls );
		    }
		    return stringBuilder.toString();
	    } catch (Exception e) {
	    	throw new RuntimeException(e);
	    }
	    
	}
	
	public static void main(String[] args) {
		// String data = readFile("C:\\1\\test.data");
		String data = callRest(Arrays.asList(InvestingSymbol.PALLADIUM, InvestingSymbol.CUPRUM, InvestingSymbol.PLATINUM), 900);
		System.out.println(data);
		
		System.out.println(parse(data));
		
	}
}
