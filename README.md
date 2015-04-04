Take events from one or more iCalendar files and generate a graphical `block schedule' for a given interval.

# Usage

  java -jar ical2svg.jar foo.ics bar.ics baz.ics > schedule.svg

or

  java -jar ical2svg.jar everything.ics --groupby location > schedule.svg

# Example output


See the 'examples' directory

# Printing

Few applications seem to be able to properly print landscape documents. The easiest way seems to be using imagemagick's 'convert' utility to rotate the image:

convert -density 200 -resize 80% -rotate 90 schedule.svg ps:- | lpr
