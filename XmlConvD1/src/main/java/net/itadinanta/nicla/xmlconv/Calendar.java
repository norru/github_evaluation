package net.itadinanta.nicla.xmlconv;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class Calendar {
	private List<Day> days;
	private DateTime from;
	private DateTime to;
	private Days duration;

	public Days getDuration() {
		return duration;
	}

	public Calendar(DateTime from, DateTime to) {
		this.from = from;
		this.to = to;
		this.duration = Days.daysBetween(from, to);
		int count = 7;
		this.days = new ArrayList<Day>(7);
		for (int i = 0; i < count; i++) {
			this.days.add(new Day(i));
		}
	}

	public Day getDay(int i) {
		return this.days.get(i);
	}

	public List<Day> getDays() {
		return this.days;
	}

	public DateTime getFrom() {
		return from;
	}

	public DateTime getTo() {
		return to;
	}
	
	public void addLesson(Lesson lesson) {
		days.get(lesson.getDayOfWeek()).addLesson(lesson);
	}
}
