package net.bzzt.ical2svg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.fortuna.ical4j.model.component.VEvent;

public class CalendarUtil {
	/**
	 * Split a list of possibly-overlapping events into separate lists of
	 * non-overlapping events.
	 * 
	 * @param events
	 * @return
	 */
	public static List<Collection<VEvent>> removeOverlap(Collection<VEvent> events)
	{
		List<Collection<VEvent>> result = new ArrayList<Collection<VEvent>>();
		for (VEvent event : events)
		{
			addEvent(event, result);
		}
		return result;
	}

	/**
	 * add event to list of non-overlapping events
	 *
	 * @param event
	 * @param result
	 */
	private static void addEvent(VEvent event, List<Collection<VEvent>> result) {
		for (Collection<VEvent> row : result)
		{
			if (!overlaps(event, row))
			{
				row.add(event);
				return;
			}
		}
		// adding to the existing rows didn't work out, so we need to add a new one
		List<VEvent> newRow = new ArrayList<VEvent>();
		newRow.add(event);
		result.add(newRow);
	}

	private static boolean overlaps(VEvent newEvent, Collection<VEvent> row) {
		for (VEvent event : row)
		{
			if (overlaps(event, newEvent))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean overlaps(VEvent one, VEvent other) {
		return 
			one.getStartDate().getDate().before(other.getEndDate(true).getDate())
			&& one.getEndDate(true).getDate().after(other.getStartDate().getDate());
	}
}
