package net.bzzt.ical2svg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableInstant;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Hello world!
 * 
 */
public class Ical2svg {
	private static final Log LOG = LogFactory.getLog(Ical2svg.class);

	@Option(name="-template", usage="Template SVG file")
	private File templateFile;
	
	@Option(name="-start",handler=DateTimeOptionHandler.class,usage="Start of period to show (yyyy-MM-dd/HH:mm or HH:mm)")
	private ReadableInstant start = MutableDateTimes.forTimeToday(17,0,0);
	
	@Option(name="-end",handler=DateTimeOptionHandler.class,usage="End of period to show (yyyy-MM-dd/HH:mm or HH:mm)")
	private ReadableInstant end = MutableDateTimes.forTimeTomorrow(0,0,0);
	
	@Option(name="-legendWidth",usage="Width of legend canvas")
	private Integer legendWidth = 60; 

	@Option(name="-canvasSize",usage="Width of block canvas")
	private Float canvasSize = Float.valueOf(890);

	@Option(name="-fontface",usage="Font face to use")
	private String fontface = "Arial";

	@Option(name="-h",usage="Print this help")
	private boolean printUsage = false;
	
	@Option(name="-groupby",usage="Which property to group events by")
	private GroupBy groupBy = GroupBy.CALNAME;
	
	@Argument(handler=CalendarOptionHandler.class)
	private List<Calendar> arguments = new ArrayList<Calendar>();
	
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
		new Ical2svg().run(args);
	}
	
	public void run (String[] args)
	{
		CmdLineParser cmdLineParser = new CmdLineParser(this);
		try {
			cmdLineParser.parseArgument(args);
		} catch (CmdLineException e1) {
			LOG.error("Error parsing parameters: " + e1.getLocalizedMessage());
			printUsage = true;
		}
		
		if (printUsage)
		{
			cmdLineParser.printUsage(System.err);
			return;
		}
		
//		OptionParser parser = new OptionParser();
//		OptionSpec<Float> widthOption = parser.accepts("width",
//				"Width of the 'blocks' part of the canvas").withRequiredArg()
//				.ofType(Float.class);
//		OptionSpec<Integer> legendWidthOption = parser.accepts("legendwidth",
//				"Width of the 'legend' part of the canvas").withRequiredArg()
//				.ofType(Integer.class);
//		OptionSpec<ConvertableDate> fromOption = parser.accepts("from",
//				"Start-date and time (HH:mm or yyyy-MM-dd/HH:mm)")
//				.withRequiredArg().ofType(ConvertableDate.class);
//		OptionSpec<ConvertableDate> toOption = parser.accepts("to",
//				"End-date and time (HH:mm or yyyy-MM-dd/HH:mm)")
//				.withRequiredArg().ofType(ConvertableDate.class);
//		OptionSpec<String> templateOption = parser.accepts("template", "Template SVG file").withRequiredArg().ofType(String.class);
//		OptionSpec<String> fontfaceOption = parser.accepts("fontface", "Font face to use").withRequiredArg().ofType(String.class);
//		OptionSet options;
//		try
//		{
//			options = parser.parse(args);
//		}
//		catch (OptionException e)
//		{
//			System.err.println(e.getMessage());
//			parser.printHelpOn(System.err);
//			return;
//		}

		Template template = new Template(templateFile);

		SVGGraphics2D graphics;
		if (template.hasDocument())
		{
			graphics = SVGGraphics2DFactory.newInstance(template.getDocument());
		}
		else
		{
			graphics = SVGGraphics2DFactory.newInstance();
		}

		
		// default: midnight after 'start'
//		MutableDateTime defaultEnd = new MutableDateTime(start);
//		defaultEnd.add(Days.ONE);
//		defaultEnd.set(DateTimeFieldType.hourOfDay(), 0);
//		defaultEnd.set(DateTimeFieldType.minuteOfHour(), 0);
//		defaultEnd.set(DateTimeFieldType.secondOfMinute(), 0);
//		ReadableInstant end = option(options.valueOf(toOption), defaultEnd);

		template.setLegendSize(legendWidth);
		template.setCanvasSize(canvasSize);
		template.setFontface(fontface);
		
		BlockSchemaPainter painter = new BlockSchemaPainter(graphics, template,
				new Interval(start, end));

		painter.paint(arguments, groupBy);
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
