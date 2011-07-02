package net.itadinanta.nicla.xmlconv;

import java.io.IOException;
import java.io.InputStream;

import nu.xom.Document;
import nu.xom.Serializer;

import org.junit.Assert;
import org.junit.Test;

public class XmlConvTest {
	XmlConv xmlConv = new XmlConv();

	public Document loadAndParse() {
		return xmlConv.load(loadTestResource());
	}

	private InputStream loadTestResource() {
		return getClass().getClassLoader().getResourceAsStream("Orario07042011.xml");
	}

	@Test
	public void testLoadAndPrettyPrint() throws IOException {
		Document doc = loadAndParse();
		prettyPrint(doc);
	}

	private void prettyPrint(Document doc) throws IOException {
		Assert.assertNotNull(doc);
		Serializer serializer = new Serializer(System.out);
		serializer.setIndent(4);
		serializer.setMaxLength(64);
		serializer.write(doc);
		serializer.flush();
	}

	@Test
	public void testFindTables() throws IOException {
		xmlConv.parse(loadTestResource());
	}
	
	@Test
	public void testParsing() throws IOException {
		Calendar calendar = xmlConv.parse(loadTestResource());
		Document doc = xmlConv.toXom(calendar);
		prettyPrint(doc);
	}
	
	@Test
	public void testMain() throws IOException {
		xmlConv.runWithArgs("-i src/main/samples/Orario07042011.xml -o target/TimeTable_test_07042011.xml".split(" "));
	}
	
}
