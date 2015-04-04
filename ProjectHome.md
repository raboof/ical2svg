This tool takes events from one or more iCalendar files and generates a graphical `block schedule' for a given interval.

This can, for example, be used to generate a daily TV schedule suitable for printing from some iCalendar files from [xmltv2ical](http://xmltv2ical.googlecode.com).

# Usage #

> java -jar ical2svg.jar foo.ics bar.ics baz.ics > schedule.svg

or

> java -jar ical2svg.jar everything.ics --groupby location > schedule.svg


# Example output #

An example: [epgdata.svg](http://ical2svg.googlecode.com/files/epgdata.svg) ([ps version](http://ical2svg.googlecode.com/files/epgdata.ps))

# Printing #

Few applications seem to be able to properly print landscape documents. The easiest way seems to be using [imagemagick](http://www.imagemagick.org)'s 'convert' utility to rotate the image:

> convert -density 200 -resize 80% -rotate 90 schedule.svg ps:- | lpr