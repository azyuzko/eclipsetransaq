package ru.eclipsetrader.transaq.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Test {
	   public static void main(String args[]) { 
		  
		   List<String> sl1 = Arrays.asList("one", "two", "three");
		   List<String> sl2 = Arrays.asList("nine", "ten", "eleven");
		   Map<Integer, List<String>> map = new HashMap<>();
		   map.put(1, sl1);
		   // map.put(2, sl2);
		   
		   map.merge(2, sl2, (a,b) -> b);

		   String result = new String();
		   result = map.entrySet().stream().map( p -> p.getValue()).reduce(new ArrayList<String>(), (a, b) -> { a.addAll(b); return a;}).stream().reduce(result, (a,b) -> a.concat(b) ) ;
		   
		   System.out.println(result);
		   
		   LinkedList<Integer> li = Arrays.asList(50, 49, 51).stream().collect(Collectors.toCollection(LinkedList::new));
		   
		   int first = 50;
		   int li_res = li.stream().reduce(first, (a,b) -> { System.out.println(" a = " + a + " b = " + b ); return a + b; });
		  // System.out.println("Li = " + li_res);
		   
		   System.out.println(String.format("%.5f", 9.900000e-1));
		   
		   /*Map<Integer, Holder<Integer, String>> map2 = new HashMap<>();
		   
		   Holder<Integer, String> h1 = new Holder<Integer, String>(1, "test");
		   Holder<Integer, String> h2 = new Holder<Integer, String>(2, "test2");
		  
		   List<Holder<Integer, String>> hl = new ArrayList<>();
		   hl.add(h1);
		   hl.add(h2);
		  
		   hl.forEach(h -> map2.merge(h.getFirst(), h, (a,b) -> b));

		   System.out.println(map2);*/
	   }
	}
