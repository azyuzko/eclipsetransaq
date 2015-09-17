package ru.eclipsetrader.transaq.core.server;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;


public class MarketSchedule {
	
	List<Period> tradeSchedule = new ArrayList<>();
	List<Period> connectSchedule = new ArrayList<>();

	static class TimePeriod {
		LocalTime begin;
		LocalTime end;
		
		public static TimePeriod of(String begin, String end) {
			TimePeriod result = new TimePeriod();
			result.begin = LocalTime.parse(begin);
			result.end = LocalTime.parse(end);
			return result;
		}
		
		public boolean inPeriod(LocalTime localTime) {
			return localTime.isAfter(begin) && localTime.isBefore(end);
		}
	}

	static class Period {
		DayOfWeek dayOfWeek;
		List<TimePeriod> periods = new ArrayList<TimePeriod>();
		public Period(DayOfWeek dayOfWeek, TimePeriod...timePeriods) {
			this.dayOfWeek = dayOfWeek;
			periods.addAll(Arrays.asList(timePeriods));
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(StringUtils.rightPad(dayOfWeek.toString(), 10) + " ");
			periods.stream().forEach(h -> { sb.append( periods.indexOf(h) > 0 ? ", " : ""); sb.append(h.begin + "-" + h.end); });
			return sb.toString();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Trade periods: \n");
		tradeSchedule.forEach(m -> sb.append(m + "\n"));
		sb.append("Connect periods: \n");
		connectSchedule.forEach(m -> sb.append(m + "\n"));
		return sb.toString();
	}
	
	public boolean canConnect(LocalDateTime dateTime) {
		Optional<Period> period = connectSchedule.stream().filter(p -> p.dayOfWeek.equals(dateTime.getDayOfWeek())).findFirst();
		if (period.isPresent()) {
			return period.get().periods.stream().filter(p -> p.inPeriod(dateTime.toLocalTime())).findFirst().isPresent();
		} 
		return false;
	}
	
	public boolean canTrade(LocalDateTime dateTime) {
		Optional<Period> period = tradeSchedule.stream().filter(p -> p.dayOfWeek.equals(dateTime.getDayOfWeek())).findFirst();
		if (period.isPresent()) {
			return period.get().periods.stream().filter(p -> p.inPeriod(dateTime.toLocalTime())).findFirst().isPresent();
		} 
		return false;
	}
	
	public static MarketSchedule createTransaqSchedule() {
		MarketSchedule ms = new MarketSchedule();
		for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
			ms.tradeSchedule.add(	new Period(dayOfWeek, TimePeriod.of("10:00", "14:00"), TimePeriod.of("14:05", "18:45"), TimePeriod.of("19:00", "23:45")) );
			ms.connectSchedule.add(	new Period(dayOfWeek, TimePeriod.of("09:45", "23:45")) );
		}
		return ms;
	}
	
	public static void main(String[] args) {
		MarketSchedule ms = createTransaqSchedule();
		System.out.println(ms.canConnect(LocalDateTime.now().plusDays(5)));
		System.out.println(ms.canTrade(LocalDateTime.now().plusDays(4)));
	}
	
}
