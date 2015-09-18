package ru.eclipsetrader.transaq.core.server;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

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
			assert result.begin.isBefore(result.end);
			return result;
		}
		
		public boolean inPeriod(LocalTime localTime) {
			return (localTime.isAfter(begin) || localTime.equals(begin)) && localTime.isBefore(end);
		}
		
		/**
		 * Внутри периода 0
		 * До начала периода -1
		 * После окончания периода 1
		 * @param localTime
		 * @return
		 */
		public int compareWith(LocalTime localTime) {
			if ((localTime.isAfter(begin) || localTime.equals(begin)) && localTime.isBefore(end)) {
				return 0;
			} 
			if (localTime.isBefore(begin)) {
				return -1;
			}
			if (localTime.isAfter(end) || localTime.equals(end)) {
				return 1;
			}
			throw new RuntimeException(); 
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
	
	public static LocalDateTime weekStart(LocalDateTime localDateTime) {
		return localDateTime.minusDays(localDateTime.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue()).truncatedTo(ChronoUnit.DAYS);
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
	
	/**
	 * Возвращает ближайшую дату, когда можно соединиться с сервером
	 * @param dateTime
	 * @return
	 */
	public LocalDateTime closestConnectDateTime(LocalDateTime dateTime) {
		LocalDateTime weekStartDate = weekStart(dateTime);
		TreeMap<LocalDateTime, LocalDateTime> map = new TreeMap<>();
		for (Period period : connectSchedule) {
			for (TimePeriod timePeriod : period.periods) {
				LocalDateTime dayStart = weekStartDate.plusDays(period.dayOfWeek.getValue() - weekStartDate.getDayOfWeek().getValue());
				LocalDateTime beginDateTime = LocalDateTime.of(dayStart.toLocalDate(), timePeriod.begin);
				LocalDateTime endDateTime = LocalDateTime.of(dayStart.toLocalDate(), timePeriod.end);
				if ( (dateTime.isAfter(beginDateTime) || dateTime.equals(beginDateTime)) && dateTime.isBefore(endDateTime) ) {
					// если попали в текущий рабочий период, вернем входную дату
					return dateTime;
				}
				map.put(beginDateTime, endDateTime);
			}
		}
		assert map.size() > 0;
		Entry<LocalDateTime, LocalDateTime> entry = map.ceilingEntry(dateTime);
		if (entry != null) {
			return entry.getKey();
		} else {
			// после окончания всех рабочих периодов на этой неделе - увеличим первый период на неделю
			return map.firstEntry().getKey().plusWeeks(1);
		}
	}
	
	public LocalDateTime closestDisconnectDateTime(LocalDateTime dateTime) {
		LocalDateTime weekStartDate = weekStart(dateTime);
		TreeMap<LocalDateTime, LocalDateTime> map = new TreeMap<>();
		for (Period period : connectSchedule) {
			for (TimePeriod timePeriod : period.periods) {
				LocalDateTime dayStart = weekStartDate.plusDays(period.dayOfWeek.getValue() - weekStartDate.getDayOfWeek().getValue());
				LocalDateTime endDateTime = LocalDateTime.of(dayStart.toLocalDate(), timePeriod.end);
				if ( dateTime.equals(endDateTime) ) {
					// если попали в дату окончания, вернем входную дату
					return dateTime;
				}
				map.put(endDateTime, endDateTime);
			}
		}
		assert map.size() > 0;
		Entry<LocalDateTime, LocalDateTime> entry = map.ceilingEntry(dateTime);
		if (entry != null) {
			return entry.getKey();
		} else {
			// после окончания всех рабочих периодов на этой неделе - увеличим первый период на неделю
			return map.firstEntry().getKey().plusWeeks(1);
		}
	}
	
	public static MarketSchedule createTransaqSchedule() {
	/*	MarketSchedule ms = new MarketSchedule();
		for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
			ms.tradeSchedule.add(	new Period(dayOfWeek, TimePeriod.of("10:00", "14:00"), TimePeriod.of("14:05", "18:45"), TimePeriod.of("19:00", "23:45")) );
			ms.connectSchedule.add(	new Period(dayOfWeek, TimePeriod.of("09:45", "23:45")) );
		}
		return ms;
	}
	
	public static MarketSchedule createTestSchedule() {*/
		MarketSchedule ms = new MarketSchedule();
		ms.tradeSchedule.add(	new Period(DayOfWeek.FRIDAY, TimePeriod.of("17:25", "17:25")) );
		ms.connectSchedule.add(	new Period(DayOfWeek.FRIDAY, TimePeriod.of("17:32", "17:33"), TimePeriod.of("17:34", "17:35")) );
		return ms;
	}
	
	public static void main(String[] args) {
		MarketSchedule ms = createTransaqSchedule();
		System.out.println(ms.canConnect(LocalDateTime.now().plusDays(5)));
		System.out.println(ms.canTrade(LocalDateTime.now().plusDays(4)));
		LocalDateTime now = LocalDateTime.now();
		System.out.println(ChronoUnit.MINUTES.between(weekStart(now), now));
		System.out.println(ms.closestConnectDateTime(LocalDateTime.of(2015, Month.SEPTEMBER, 17, 23, 44, 00)));
		System.out.println(ms.closestDisconnectDateTime(LocalDateTime.of(2015, Month.SEPTEMBER, 17, 23, 45, 00)));
	}
	
}
