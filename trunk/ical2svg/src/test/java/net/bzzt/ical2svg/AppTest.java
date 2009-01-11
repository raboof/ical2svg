package net.bzzt.ical2svg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtStart;

import org.apache.batik.svggen.SVGGraphics2D;
import org.joda.time.Interval;
import org.xml.sax.SAXException;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}

	public void testParseCalendar() throws Exception {
		assertTrue(getTestCal().getComponents().size() > 0);

	}

	private Calendar getTestCal() throws IOException, ParserException {
		return getTestCal("rtl4.nl.ics");
	}

	private Calendar getTestCal(String filename) throws IOException,
			ParserException {
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(this.getClass().getResourceAsStream(
				"/" + filename));
		return calendar;
	}

	public void testFilterCalendar() throws Exception {
		assertEquals(10, getFilteredTestEvents().size());
	}

	private Collection<VEvent> getFilteredTestEvents() throws ParseException,
			IOException, ParserException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		DateTime start = new DateTime(format.parse("2008-12-20 17:00"));

		DateTime end = new DateTime(format.parse("2008-12-20 23:00"));

		Filter filter = new Filter(new PeriodRule(new Period(start, end)));

		@SuppressWarnings("unchecked")
		Collection<VEvent> events = filter.filter(getTestCal().getComponents(
				Component.VEVENT));
		return events;
	}

	public void testTimezone() throws ParseException
	{
		// 18:05 UTC
		DtStart dtstart = new DtStart("20081221T180500Z");
		// and again...
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss ZZZ");
		Date formatteddate = sdf.parse("20081221 180500 UTC");
		
		assertEquals(dtstart.getDate().getTime(), formatteddate.getTime());
		
		// here (holland), it's 19:05 though:
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd HHmmss");
		Date formatteddate2 = sdf2.parse("20081221 190500");
		assertEquals(formatteddate2.getTime(), formatteddate.getTime());
		
		sdf.format(formatteddate2);
	}
	
	public void testSplitOverlap() throws Exception {
		@SuppressWarnings("unchecked")
		List<Collection<VEvent>> result = CalendarUtil
				.removeOverlap(getTestCal().getComponents(Component.VEVENT));
		assertEquals(3, result.size());

		result = CalendarUtil.removeOverlap(getFilteredTestEvents());
		assertEquals(1, result.size());
	}

	public void testGenerateBlocks() throws ParseException, IOException,
			ParserException, ParserConfigurationException {
		
		SVGGraphics2D graphics = SVGGraphics2DFactory.newInstance();

		testPainting(graphics);

		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes
		Writer out = new OutputStreamWriter(new FileOutputStream(
				"/tmp/test.svg"), "UTF-8");
		graphics.stream(out, useCSS);
	}
	
	private void testPainting(SVGGraphics2D graphics) throws ParseException, IOException, ParserException
	{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date start = format.parse("2008-12-21 17:00");
		Date end = format.parse("2008-12-21 24:00");

		Template template = new Template((File) null);
		template.setCanvasSize(Float.valueOf(860));
		template.setLegendSize(60);
		
		BlockSchemaPainter painter = new BlockSchemaPainter(graphics, template,
				new Interval(start.getTime(), end.getTime()));

		painter.addBlacklist("Z@pp", "Nederland 3", "KETNET", "CANVAS");
    	
		List<Calendar> calendars = new ArrayList<Calendar>();
		for (String filename : new String[] { "nederland1.omroep.nl.ics",
				"nederland2.omroep.nl.ics", "nederland3.omroep.nl.ics",
				"rtl4.nl.ics", "rtl5.nl.ics", "net5.nl.ics", "sbs6.nl.ics",
				"rtl7.nl.ics", "veronica.nl.ics", "nick.com.ics",
				"omroepgelderland.nl.ics", "cartoonnetwork.nl.ics",
				"bbc1.bbc.co.uk.ics", "bbc2.bbc.co.uk.ics" }) {
			calendars.add(getTestCal(filename));
		}
		painter.paint(calendars, null, GroupBy.CALNAME);
		painter.paintGrid();
	}
	
	public void testTemplateBased() throws ParserConfigurationException, SAXException, IOException, ParseException, ParserException
	{
//		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		InputStream inputStream = this.getClass().getResourceAsStream("/template.svg");
//		Document template = builder.parse(inputStream);
		
//		SVGGraphics2D canvas = new TemplatedSVGGraphics2D(template);
		SVGGraphics2D canvas = SVGGraphics2DFactory.newInstance(inputStream);
		testPainting(canvas);
//		Element root = canvas.getRoot();
//		NodeList templateNodes = template.getChildNodes();
//		for (int i = 0; i < templateNodes.getLength(); i++)
//		{
//			Node templateNode = templateNodes.item(i).cloneNode(true);
//			root.insertBefore(templateNode, root.getFirstChild());
//		}

		
//		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("/tmp/test2.svg"));
//		canvas.stream(root, writer, true, false);
//		writer.flush();
//		writer.close();
		canvas.stream("/tmp/test2.svg");
	}
}
