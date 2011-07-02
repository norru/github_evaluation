package net.itadinanta.nicla.xmlconv;

public class Lesson {
	private int dayOfWeek;
	private String time;
	private String activity;
	private String instructor;
	private String location;

	public Lesson(int dayOfWeek, String time, String activity, String instructor, String location) {
		super();
		this.dayOfWeek = dayOfWeek;
		this.time = time;
		this.activity = activity;
		this.instructor = instructor;
		this.location = location;
	}

	public Lesson(int dayOfWeek, String time, String activity, String instructor, String location, String resources) {
		super();
		this.dayOfWeek = dayOfWeek;
		this.time = time;
		this.activity = activity;
		this.instructor = instructor;
		this.location = location;
		this.resources = resources;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getInstructor() {
		return instructor;
	}

	public void setInstructor(String instructor) {
		this.instructor = instructor;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getResources() {
		return resources;
	}

	public void setResources(String resources) {
		this.resources = resources;
	}

	private String resources;
}
