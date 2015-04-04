package net.bzzt.ical2svg;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Days;
import org.joda.time.MutableDateTime;

public class MutableDateTimes {

	public static MutableDateTime forTimeToday(int hourOfDay, int minuteOfHour, int secondOfMinute) {
		MutableDateTime defaultStart = new MutableDateTime();
		defaultStart.set(DateTimeFieldType.hourOfDay(), hourOfDay);
		defaultStart.set(DateTimeFieldType.minuteOfHour(), minuteOfHour);
		defaultStart.set(DateTimeFieldType.secondOfMinute(), secondOfMinute);
		return defaultStart;
	}

	public static MutableDateTime forTimeTomorrow(int hourOfDay, int minuteOfHour, int secondOfMinute) {
		MutableDateTime result = forTimeToday(hourOfDay, minuteOfHour, secondOfMinute);
		result.add(Days.ONE);
		return result;
	}

}
