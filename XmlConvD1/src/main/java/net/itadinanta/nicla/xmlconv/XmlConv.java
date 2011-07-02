package net.itadinanta.nicla.xmlconv;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlConv {
	private static final Logger LOG = LoggerFactory.getLogger(XmlConv.class);
	private Element aquagymElement;
	private Element cardiofitnessElement;
	private Element circuitTrainingElement;
	private List<Element> dailyElement = new ArrayList<Element>();
	private DateTime from;
	private Element miniClassiElement;
	private List<Element> poolDomeElement = new ArrayList<Element>();
	private List<Element> poolIndoorsElement = new ArrayList<Element>();
	private Element spinningElement;
	private DateTime to;
	private Pattern instructorPattern = Pattern
			.compile("^(.*)\\s(Carlo.*?|(Princ. )?Pamela.*?|Andrea.*?|Gianluca.*?|Daniele.*?|Mauro.*?|Staff.*?|Laura.*?|Marco.*?|Alfred.*?|Tony.*?|Luigi.*|Adriana.*|Gabriele.*|Fab.*?|Claudio.*?|Guido.*?|Nicoletta.*?|Lucia.*?|Aless.*?|Ciro.*?|Giorgi.*?|Silv.*?)$");

	public void findMetadata(Element root) {
		Elements elements = root.getChildElements();
		for (int i = 0; i < elements.size(); i++) {
			Element element = elements.get(i);
			String localName = element.getLocalName();
			if ("P".equals(localName)) {
				String p = element.getValue().trim();
				Matcher matcher = Pattern.compile("Orario in vigore dal (.*) al (.*) -Rev.*").matcher(p);
				if (matcher.matches()) {
					String fromString = matcher.group(1);
					String toString = matcher.group(2);
					from = DateTimeFormat.forPattern("d/M/y").parseDateTime(fromString);
					to = DateTimeFormat.forPattern("d/M/y").parseDateTime(toString);
				}
			}
		}
	}

	public void findTables(Element root) {
		Elements elements = root.getChildElements();
		String tableName = null;
		for (int i = 0; i < elements.size(); i++) {
			Element element = elements.get(i);
			String localName = element.getLocalName();
			if ("P".equals(localName)) {
				tableName = element.getValue().trim();
			} else if ("Table".equals(localName)) {
				if (tableName != null) {
					if (tableName.matches("ISOTONICA.*CARDIOFITNESS.*")) {
						cardiofitnessElement = element;
						LOG.info("Found CARDIOFITNESS");
					} else if (tableName.matches("^AQUAG.*")) {
						aquagymElement = element;
						LOG.info("Found AQUAGYM");
					} else if (tableName.matches("^SPINNING.*")) {
						spinningElement = element;
						LOG.info("Found SPINNING");
					} else if (tableName.matches("^LUNED.*")) {
						dailyElement.add(element);
						LOG.info("Found day {}", dailyElement.size());
					} else if (tableName.matches("^CIRCUIT.*")) {
						circuitTrainingElement = element;
						LOG.info("Found CIRCUIT TRAINING");
					} else if (tableName.matches("^MINI.*")) {
						miniClassiElement = element;
						LOG.info("Found MINI CLASSI");
					} else {
						LOG.warn("Unknown table {}", tableName);
					}
				}
			} else if ("Part".equals(localName)) {
				Elements sects = element.getChildElements("Sect");
				for (int j = 0; j < sects.size(); j++) {
					Elements poolTableElements = sects.get(j).getChildElements("Table");
					for (int k = 0; k < poolTableElements.size(); k++) {
						Element poolTableElement = poolTableElements.get(k);
						String poolTableHeader = poolTableElement.getFirstChildElement("TR").getFirstChildElement("TH")
								.getValue();
						if (poolTableHeader.matches(".*INTERNA.*")) {
							poolIndoorsElement.add(poolTableElement);
							LOG.info("Found poolIndoorsElement {} {}", poolIndoorsElement.size(), poolTableHeader);
						} else if (poolTableHeader.matches(".*PALLONE.*")) {
							poolDomeElement.add(poolTableElement);
							LOG.info("Found poolDomeElement {} {}", poolDomeElement.size(), poolTableHeader);
						}
					}
				}
			}
		}
	}

	public Element getAquagymElement() {
		return aquagymElement;
	}

	public Element getCardiofitnessElement() {
		return cardiofitnessElement;
	}

	public Element getCircuitTrainingElement() {
		return circuitTrainingElement;
	}

	public List<Element> getDailyElement() {
		return dailyElement;
	}

	public DateTime getFrom() {
		return from;
	}

	public Element getMiniClassiElement() {
		return miniClassiElement;
	}

	public List<Element> getPoolDomeElement() {
		return poolDomeElement;
	}

	public List<Element> getPoolIndoorsElement() {
		return poolIndoorsElement;
	}

	public Element getSpinningElement() {
		return spinningElement;
	}

	public DateTime getTo() {
		return to;
	}

	public Document load(InputStream inputStream) {
		try {
			Builder builder = new Builder();
			return builder.build(inputStream, "UTF-8");
		} catch (Exception ex) {
			LOG.error("Could not parse Xml", ex);
		}
		return null;
	}

	public Document load(String path) {
		try {
			return new Builder().build(new File(path));
		} catch (Exception ex) {
			LOG.error("Could not parse Xml from {}!", path, ex);
		}
		return null;
	}

	public Calendar parse(InputStream inputStream) {
		Document document = load(inputStream);
		findMetadata(document.getRootElement());
		findTables(document.getRootElement());
		Calendar calendar = new Calendar(from, to);
		populateCardioFitness(calendar, cardiofitnessElement);
		populateAquagym(calendar, aquagymElement);
		populateSpinning(calendar, spinningElement);
		populateCourses(calendar, dailyElement);
		populateCircuitTraining(calendar, circuitTrainingElement);
		populateMiniClasses(calendar, miniClassiElement);
		populatePoolIndoors(calendar, poolIndoorsElement);
		populatePoolDome(calendar, poolDomeElement);
		return calendar;
	}

	private enum DAYS_LETTERS {
		LUN, MAR, MER, GIO, VEN, SAB, DOM;
	}

	private static int[] stringToDays(String days) {
		int count = 0;
		String[] items = days.split("/");
		int[] daysOfWeek = new int[items.length];
		for (String item : items) {
			daysOfWeek[count++] = DAYS_LETTERS.valueOf(item).ordinal();
		}
		return daysOfWeek;
	}

	private void populateActivity(Calendar calendar, Element table, String defaultActivity) {
		String[][] cell = tableToStringArray(table);
		for (int i = 1; i < cell.length; i++) {
			int[] days = stringToDays(cell[i][0]);
			for (int dayOfWeek : days) {
				String activityInstructor = cell[i][1];
				if (activityInstructor != null && !activityInstructor.isEmpty()) {
					String activity = defaultActivity;
					String instructor = activityInstructor;
					Matcher matcher = instructorPattern.matcher(activityInstructor);
					if (matcher.matches()) {
						if (matcher.group(1).length() > 3) {
							activity = matcher.group(1);
						}
						instructor = matcher.group(2);
					} else {
						LOG.warn("Unknown instructor: {}", activityInstructor);
					}
					calendar.addLesson(new Lesson(dayOfWeek, cell[i][3], activity, instructor, cell[i][2]));
				}
			}
		}
	}

	private void populatePool(Calendar calendar, String pool, List<Element> tables) {
		int dayOfWeek = 0;
		for (Element dayElement : tables) {
			String[][] cell = tableToStringArray(dayElement);
			for (int i = 1; i < cell.length; i++) {
				String time = cell[i][0];
				if (time != null && !time.isEmpty()) {
					calendar.addLesson(new Lesson(dayOfWeek, time, "Nuoto libero soci", null, pool, cell[i][1]));
				}
			}
			dayOfWeek++;
		}
	}

	private void populatePoolDome(Calendar calendar, List<Element> tables) {
		populatePool(calendar, "Piscina coperta pallone", tables);
	}

	private void populatePoolIndoors(Calendar calendar, List<Element> tables) {
		populatePool(calendar, "Piscina interna", tables);
	}

	private void populateMiniClasses(Calendar calendar, Element table) {
		populateActivity(calendar, miniClassiElement, "Mini classi (30minuti)");
	}

	private void populateCircuitTraining(Calendar calendar, Element table) {
		populateActivity(calendar, circuitTrainingElement, "Circuit training");
	}

	private void populateCourses(Calendar calendar, List<Element> table) {
		int dayOfWeek = 0;
		for (Element dayElement : table) {
			String[][] cell = tableToStringArray(dayElement);
			for (int i = 1; i < cell.length; i++) {
				for (int j = 1; j < cell[i].length; j++) {
					String activityInstructor = cell[i][j];
					if (activityInstructor != null && !activityInstructor.isEmpty()) {
						Matcher matcher = instructorPattern.matcher(activityInstructor);
						String room = cell[0][j];
						if (matcher.matches()) {
							String activity = matcher.group(1);
							String instructor = matcher.group(2);
							calendar.addLesson(new Lesson(dayOfWeek, cell[i][0], activity, instructor, room));
						} else {
							LOG.warn("Unknown instructor: {}", activityInstructor);
							String activity = activityInstructor;
							calendar.addLesson(new Lesson(dayOfWeek, cell[i][0], activity, null, room));
						}
					}
				}
			}
			dayOfWeek++;
		}
	}

	private void populateFromDayTable(Calendar calendar, Element table, String activity, String location) {
		String[][] cf = tableToStringArray(table);
		for (int i = 1; i < cf.length; i++) {
			String time = cf[i][0];
			for (int j = 1; j < 7; j++) {
				String description = cf[i][j];
				if (description != null && !description.isEmpty()) {
					calendar.addLesson(new Lesson(j - 1, time, activity, description, location));
				}
			}
		}
	}

	private void populateSpinning(Calendar calendar, Element table) {
		populateFromDayTable(calendar, table, "Spinning", "2º Piano");
	}

	private void populateAquagym(Calendar calendar, Element table) {
		populateFromDayTable(calendar, table, "Aquagym", null);
	}

	private void populateCardioFitness(Calendar calendar, Element table) {
		String[][] cf = tableToStringArray(table);
		for (int j = 0; j < 7; j++) {
			switch (j) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				calendar.addLesson(new Lesson(j, cf[1][1], "Isotonica/Cardiofitness", null, null, "Insegnanti"));
				calendar.addLesson(new Lesson(j, cf[2][1], "Isotonica/Cardiofitness", null, null, "Accesso libero"));
				break;
			case 6:
				calendar.addLesson(new Lesson(j, cf[1][2], "Isotonica/Cardiofitness", null, null, "Insegnanti"));
				calendar.addLesson(new Lesson(j, cf[2][2], "Isotonica/Cardiofitness", null, null, "Accesso libero"));
				break;
			case 7:
				calendar.addLesson(new Lesson(j, cf[1][3], "Isotonica", null, null, "Insegnanti"));
				calendar.addLesson(new Lesson(j, cf[2][3], "Isotonica", null, null, "Accesso libero"));
				calendar.addLesson(new Lesson(j, cf[1][4], "Cardiofitness", null, null, "Insegnanti"));
				calendar.addLesson(new Lesson(j, cf[2][4], "Cardiofitness", null, null, "Accesso libero"));
				break;
			}
		}
	}

	private String[][] tableToStringArray(Element elementTable) {
		int width;
		int height;
		Elements rows = elementTable.getChildElements();
		height = rows.size();
		width = 0;
		for (int i = 0; i < height; ++i) {
			Elements columns = rows.get(i).getChildElements();
			if (width < columns.size()) {
				width = columns.size();
			}
		}
		String[][] result = new String[height][width];
		for (int i = 0; i < height; ++i) {
			Elements columns = rows.get(i).getChildElements();
			for (int j = 0; j < columns.size(); j++) {
				result[i][j] = columns.get(j).getValue().trim();
			}
		}
		return result;
	}

	private Element attribute(Element parent, String attributeName, Object attributeValue) {
		if (attributeValue != null && parent != null) {
			parent.addAttribute(new Attribute(attributeName, attributeValue.toString()));
		}
		return parent;
	}

	private Element element(Element parent, String childName, Object childValue) {
		if (parent != null && childValue != null) {
			Element childElement = new Element(childName);
			childElement.appendChild(childValue.toString());
			parent.appendChild(childElement);
		}
		return parent;
	}

	private static final String[] DAYS_OF_WEEK = { "Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato",
			"Domenica" };

	public Document toXom(Calendar calendar) {
		Element root = new Element("Calendario");
		attribute(root, "da", calendar.getFrom());
		attribute(root, "a", calendar.getTo());
		for (Day day : calendar.getDays()) {
			Element dayElement = new Element(DAYS_OF_WEEK[day.getDayOfWeek()]);
			for (Lesson lesson : day.getLessons()) {
				Element lessonElement = new Element("Insegnamento");
				element(lessonElement, "Ora", lesson.getTime());
				element(lessonElement, "Attività", lesson.getActivity());
				element(lessonElement, "Insegnante", lesson.getInstructor());
				element(lessonElement, "Location", lesson.getLocation());
				element(lessonElement, "Risorsa", lesson.getResources());
				dayElement.appendChild(lessonElement);
			}
			root.appendChild(dayElement);
		}
		return new Document(root);
	}

	public void runWithArgs(String[] args) throws IOException {
		String infile = null;
		String outfile = null;
		Getopt g = new Getopt("XmlConv", args, "i:o:h");
		int c;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'h':
				System.out.println("XmlConv infile [-o outfile]");
				break;
			case 'i':
				infile = g.getOptarg();
				break;
			case 'o':
				outfile = g.getOptarg();
				break;
			default:
				infile = g.getOptarg();
			}
		}
		InputStream instream = System.in;
		OutputStream outstream = System.out;
		if (infile != null) {
			instream = new FileInputStream(infile);
		}
		if (outfile != null) {
			outstream = new FileOutputStream(outfile);
		}
		try {
			Calendar calendar = parse(instream);
			Document doc = toXom(calendar);
			Serializer serializer = new Serializer(outstream);
			serializer.setIndent(4);
			serializer.setMaxLength(256);
			serializer.write(doc);
			serializer.flush();
		} finally {
			try {
				if (outstream != System.out) {
					outstream.close();
				}
			} finally {
				if (instream != System.in) {
					instream.close();
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new XmlConv().runWithArgs(args);
	}
}
