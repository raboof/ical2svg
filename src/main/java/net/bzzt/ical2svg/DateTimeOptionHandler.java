package net.bzzt.ical2svg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.Instant;
import org.joda.time.ReadableInstant;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class DateTimeOptionHandler extends OptionHandler<ReadableInstant> {

	public DateTimeOptionHandler(CmdLineParser parser, OptionDef option, Setter<ReadableInstant> setter)
	{
		super(parser, option, setter);
	}
	
	@Override
	public String getDefaultMetaVariable() {
		return "DATETIME";
	}

	@Override
	public int parseArguments(Parameters arg0) throws CmdLineException {
		String date = arg0.getParameter(0);
		Date today = new Date();
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd/HH:mm");

		if (date.length() < 6) {
			SimpleDateFormat dateproducer = new SimpleDateFormat(
					"yyyy-MM-dd/");
			date = dateproducer.format(today) + date;
		}
		try {
			setter.addValue(new Instant(parser.parse(date)));
		} catch (ParseException e) {
			throw new CmdLineException("Could not parse " + date + ": " + e.getMessage(), e);
		}
		
		return 1;
	}

}
