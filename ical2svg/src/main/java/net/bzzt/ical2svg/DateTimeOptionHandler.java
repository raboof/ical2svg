package net.bzzt.ical2svg;

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
		String time = arg0.getParameter(0);
		// TODO parse and use 'setter' to set the value
		return 1;
	}

}
