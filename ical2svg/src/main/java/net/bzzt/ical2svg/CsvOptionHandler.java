package net.bzzt.ical2svg;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class CsvOptionHandler extends OptionHandler<String> {

	
	public CsvOptionHandler(CmdLineParser parser, OptionDef option,
			Setter<? super String> setter) {
		super(parser, option, setter);
	}

	@Override
	public String getDefaultMetaVariable() {
		return "VAL";
	}

	@Override
	public int parseArguments(Parameters parameters) throws CmdLineException {
		for (String value : parameters.getParameter(0).split(","))
		{
			setter.addValue(value);
		}
		return 1;
	}

}
