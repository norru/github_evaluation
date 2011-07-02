package net.itadinanta.nicla.xmlconv;

import java.util.ArrayList;
import java.util.List;

public class Day {
	private int dayOfWeek;
	private List<Lesson> lessons;

	public Day(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
		this.lessons = new ArrayList<Lesson>();
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public List<Lesson> getLessons() {
		return lessons;
	}

	public void addLesson(Lesson lesson) {
		this.lessons.add(lesson);
	}
}
