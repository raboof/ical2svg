package net.bzzt.ical2svg;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableInstant;

/**
 * Hello world!
 * 
 */
public class Ical2svg {
	private static final Log LOG = LogFactory.getLog(Ical2svg.class);

	public static class ConvertableDate extends MutableDateTime {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ConvertableDate(Date date) {
			super(date);
		}

		private static final Date today = new Date();

		public static ConvertableDate valueOf(String date)
				throws ParseException {
			SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd/HH:mm");

			if (date.length() < 6) {
				SimpleDateFormat dateproducer = new SimpleDateFormat(
						"yyyy-MM-dd/");
				date = dateproducer.format(today) + date;
			}

			return new ConvertableDate(parser.parse(date));
		}
	}

	/**
	 * usage: 'ical2svg *.ics'
	 * 
	 * generates an svg for all shows for today 17:00-24:00
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws ParseException
	 * @throws ParserException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws IOException {
		OptionParser parser = new OptionParser();
		OptionSpec<Float> widthOption = parser.accepts("width",
				"Width of the 'blocks' part of the canvas").withRequiredArg()
				.ofType(Float.class);
		OptionSpec<Integer> legendWidthOption = parser.accepts("legendwidth",
				"Width of the 'legend' part of the canvas").withRequiredArg()
				.ofType(Integer.class);
		OptionSpec<ConvertableDate> fromOption = parser.accepts("from",
				"Start-date and time (HH:mm or yyyy-MM-dd/HH:mm)")
				.withRequiredArg().ofType(ConvertableDate.class);
		OptionSpec<ConvertableDate> toOption = parser.accepts("to",
				"End-date and time (HH:mm or yyyy-MM-dd/HH:mm)")
				.withRequiredArg().ofType(ConvertableDate.class);
		OptionSpec<String> templateOption = parser.accepts("template", "Template SVG file").withRequiredArg().ofType(String.class);
		OptionSpec<String> fontfaceOption = parser.accepts("fontface", "Font face to use").withRequiredArg().ofType(String.class);
		OptionSet options;
		try
		{
			options = parser.parse(args);
		}
		catch (OptionException e)
		{
			System.err.println(e.getMessage());
			parser.printHelpOn(System.err);
			return;
		}

		Template template = new Template(options.valueOf(templateOption));

		SVGGraphics2D graphics;
		if (template.hasDocument())
		{
			graphics = SVGGraphics2DFactory.newInstance(template.getDocument());
		}
		else
		{
			graphics = SVGGraphics2DFactory.newInstance();
		}

		MutableDateTime defaultStart = new MutableDateTime();
		defaultStart.set(DateTimeFieldType.hourOfDay(), 17);
		defaultStart.set(DateTimeFieldType.minuteOfHour(), 0);
		defaultStart.set(DateTimeFieldType.secondOfMinute(), 0);
		ReadableInstant start = option(options.valueOf(fromOption),
				defaultStart);

		// default: midnight after 'start'
		MutableDateTime defaultEnd = new MutableDateTime(start);
		defaultEnd.add(Days.ONE);
		defaultEnd.set(DateTimeFieldType.hourOfDay(), 0);
		defaultEnd.set(DateTimeFieldType.minuteOfHour(), 0);
		defaultEnd.set(DateTimeFieldType.secondOfMinute(), 0);
		ReadableInstant end = option(options.valueOf(toOption), defaultEnd);

		template.setLegendSize(option(options.valueOf(legendWidthOption), template.getLegendSize(), 60));
		template.setCanvasSize(option(options.valueOf(widthOption), template.getCanvasSize(), Float.valueOf(860)));
		template.setFontface(option(options.valueOf(fontfaceOption), template.getFontface()));
		
		BlockSchemaPainter painter = new BlockSchemaPainter(graphics, template,
				new Interval(start, end));

		CalendarBuilder builder = new CalendarBuilder();
		for (String filename : options.nonOptionArguments()) {
			try {
				painter.paint(builder.build(new FileInputStream(filename)));
			} catch (FileNotFoundException e) {
				LOG.error("File " + filename + " not found");
			} catch (IOException e) {
				LOG.error("Error reading file " + filename + ": "
						+ e.getMessage(), e);
			} catch (ParserException e) {
				LOG.error("Error parsing file " + filename + ": "
						+ e.getMessage(), e);
			}
		}
		painter.paintGrid();

		graphics.setSVGCanvasSize(template.getSvgCanvasSize(painter.getCurrentY()));

		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes
		Writer out;
		try {
			out = new OutputStreamWriter(System.out, "UTF-8");
			graphics.stream(out, useCSS);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SVGGraphics2DIOException e) {
			e.printStackTrace();
		}
	}

	/** returns the first non-null option */
	private static <T> T option(T... options) {
		for (T option : options)
		{
			if (option != null)
			{
				return option;
			}
		}
		return null;
	}
}
