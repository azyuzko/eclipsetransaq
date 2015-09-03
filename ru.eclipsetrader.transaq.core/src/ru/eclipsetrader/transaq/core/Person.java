package ru.eclipsetrader.transaq.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

public class Person implements Comparable<Person> {

	  private String lastName;
	  private String middleName;
	  private String firstName;
	  private int zipCode;
	  
	  // constructor, getters and setters are omitted
	  
	  @Override
	  public int compareTo(Person other) {
	    return ComparisonChain.start()
	        .compare(lastName, other.lastName)
	        .compare(firstName, other.firstName)
	        .compare(middleName, other.middleName, Ordering.natural().nullsLast())
	        .compare(zipCode, other.zipCode)
	        .result();
	  }
	  
	  @Override
	  public boolean equals(Object obj) {
	    if (obj == null || getClass() != obj.getClass()) {
	      return false;
	    }
	    Person other = (Person) obj;
	    return Objects.equals(lastName, other.lastName)
	        && Objects.equals(middleName, other.middleName)
	        && Objects.equals(firstName, other.firstName)
	        && zipCode == other.zipCode;
	  }
	  
	  @Override
	  public int hashCode() {
	    return Objects.hash(lastName, middleName, firstName, zipCode);
	  }
	  
	  @Override
	  public String toString() {		
	    return MoreObjects.toStringHelper(this)
	        .omitNullValues()
	        .addValue(true)
	        .add("lastName", lastName)
	        .add("middleName", middleName)
	        .add("firstName", firstName)
	        .add("zipCode", zipCode)
	        .toString();
	  }

	public static void main(String[] args) {
		Person p = new Person();
		p.firstName = "1A";
		p.lastName = "Z";
		p.middleName = ".";
		p.zipCode = 102;

		Person p2 = new Person();
		p2.firstName = "A";
		p2.lastName = "Z";
		p2.middleName = ".";
		p2.zipCode = 102;

		Map<Integer, List<String>> map = new HashMap<>();
		
		//map.computeIfAbsent(1, () -> new ArrayList<String>()).add("test");
		
		System.out.println(MoreObjects.firstNonNull(p2, p));
	}
}
