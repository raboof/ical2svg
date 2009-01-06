package net.bzzt.ical2svg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class CalendarOptionHandler extends OptionHandler<Calendar> {

	private CalendarBuilder builder = new CalendarBuilder();
	
	public CalendarOptionHandler(CmdLineParser parser, OptionDef option,
			Setter<? super Calendar> setter) {
		super(parser, option, setter);
	}

	@Override
	public String getDefaultMetaVariable() {
		return "CALENDAR";
	}

	@Override
	public int parseArguments(Parameters arg0) throws CmdLineException {
		String name = arg0.getParameter(0);
		try {
			setter.addValue(builder.build(new FileInputStream(name)));
			return 1;
		} catch (FileNotFoundException e) {
			throw new CmdLineException("File " + name + " not found", e);
		} catch (IOException e) {
			throw new CmdLineException("Could not open " + name + ": " + e.getMessage(), e);
		} catch (ParserException e) {
			throw new CmdLineException("Error parsing file " + name + ": " + e.getMessage(), e);
		}
	}

}
