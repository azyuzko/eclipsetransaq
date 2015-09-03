package ru.eclipsetrader.transaq.core.orders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class ChangeList {

	List<Holder<Date, DiffMap>> changes = new ArrayList<>();

	public void putChange(Date onDate, DiffMap diff) {
		changes.add(new Holder<Date, DiffMap>(onDate, diff));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Holder<Date, DiffMap> dm : changes) {
			sb.append(Utils.formatDate(dm.getFirst()) + ":\n");
			sb.append("\n");
		}
		return sb.toString();
	}
}
