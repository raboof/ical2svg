package net.bzzt.ical2svg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Location;

import org.apache.batik.svggen.SVGGraphics2D;
import org.joda.time.Interval;

public class BlockSchemaPainter {
	private final Graphics2D canvas;
	/** line on x-axis where the blocks start */
	private final int blocksleft;
	private final Interval interval;
	private final Template template;
	private float currenty;

	private Set<String> blacklist = new HashSet<String>();

	/**
	 * 
	 * @param canvas the canvas to paint on
	 * @param interval the time interval for which to output events 
	 * @param blocksleft size of the left legend
	 * @param canvaswidth size of the blocks canvas 
	 */
	public BlockSchemaPainter(SVGGraphics2D canvas, Template template, Interval interval) {
		this.canvas = canvas;
		this.interval = interval;

		this.blocksleft = template.getLegendSize();
		
		this.template = template;
//		this.uppery = template.getInitialY();
		this.currenty = template.getInitialY();
	}
	
	public void paintGrid()
	{
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		
		for (org.joda.time.DateTime current = interval.getStart(); interval.contains(current); current = current.plusHours(1))
		{
			AffineTransform Tx = AffineTransform.getQuadrantRotateInstance(3);
			canvas.setTransform(Tx);
			String time = format.format(current.toDate());
			//TextLayout textLayout = new TextLayout(time, getFont(), getFontRenderContext());
			canvas.setFont(getFont());
			
			float xPosition = getX(current.toDate());
			canvas.setColor(Color.BLACK);
			//textLayout.draw(canvas, -template.getInitialY() + 10, xPosition);
			canvas.drawString(time, -template.getInitialY() + 10, xPosition);
			canvas.setTransform(new AffineTransform());
			
			canvas.setColor(Color.GRAY);
			canvas.draw(new Line2D.Double(xPosition, template.getInitialY() - 2,
					xPosition, currenty));
		}
	}

	Font getFont()
	{
		return getFont(template.getFontface(), 7);
	}
	
	static Font getFont(String fontFace, int size) {
		Font font = new Font(fontFace, Font.PLAIN, size);
		return font; 
	}


	/** splits events, then paints them */
	public void paint(String description, Collection<VEvent> events) {
		int margin = 3;
		
		Collection<VEvent> eventsWithoutBlacklist = new ArrayList<VEvent>();
		for (VEvent event : events)
		{
			if (!blacklist.contains(event.getSummary().getValue().trim()))
			{
				eventsWithoutBlacklist.add(event);
			}
		}
		
		//fitText(description, blocksleft - 2 * margin, template.getRowHeight()).draw(canvas, template.getXMargin() + margin, currenty + template.getRowHeight() / 2);
		drawFittedText(description, template.getXMargin(), currenty, blocksleft, template.getRowHeight(), margin);
		
		List<Collection<VEvent>> rows = CalendarUtil.removeOverlap(eventsWithoutBlacklist);
		if (rows.isEmpty())
		{
			// one empty row
			currenty += template.getRowHeight();
		}
		else
		{
			for (Collection<VEvent> row : rows) {
				paintRow(row, currenty);
				currenty += template.getRowHeight();
			}
		}
	}

	/**
	 * paint a row of events at the specified height
	 * 
	 * @param row
	 * @param currentx
	 */
	private void paintRow(Collection<VEvent> row, float currenty) {
		for (VEvent event : row) {
			float startX = Math.max(template.getCanvasX(), getX(event.getStartDate().getDate()));
			float endX = Math.min(template.getCanvasX() + template.getCanvasSize(), getX(event.getEndDate(true).getDate()));
			float width = endX - startX;
			
			String summary = event.getSummary().getValue();

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			format.format(event.getStartDate().getDate());
			
			paintBlock(summary, startX, currenty, width, template.getRowHeight());
		}
	}

	private void paintBlock(String description, float x, float y,
			float width, int height) {
		
		int rectmargin = 1;
		
		canvas.draw(new RoundRectangle2D.Double(x + rectmargin, y + rectmargin, width - 2 * rectmargin, height - 2 * rectmargin, 3, 3));
		
		int margin = 3;
		
		drawFittedText(description, x, y, width, height, margin);
	}

	private void drawFittedText(String text, float x, float y, float width, float height, int margin)
	{
		if (text == null || "".equals(text.trim()))
		{
			return;
		}
		// old:
		//TextLayout textLayout = fitText(text, width - 2 * margin, height - 2 * margin);
		//textLayout.draw(canvas, x + margin, y + new Double(0.5 * (height + textLayout.getBounds().getHeight())).intValue());
		FontRenderContext frc = getFontRenderContext();
		
		int defaultsize = 6;
		int minimumsize = 3;
		
		float widthWithinMargin = width - 2 * margin;
		float heightWithinMargin = height - 2 * margin;
		Font font = getFittingFont(template.getFontface(), frc, defaultsize, minimumsize, text, widthWithinMargin, heightWithinMargin);
		if (font == null)
		{
			// even with the minimum font size it doesn't fit - truncate.
			// later perhaps add word wrapping here.
			font = getFont(template.getFontface(), minimumsize);
			text = getFittingDescription(frc, text, font, widthWithinMargin, heightWithinMargin);
		}

		if (text != null && !text.trim().equalsIgnoreCase(""))
		{
			canvas.setFont(font);
			// unfortunately centering the text doesn't work reliably on openjdk
			// for some reason: the calculation of the height of the bounding box
			// seems to be off in openjdk, I get measurements similar to
			// http://www.mail-archive.com/2d-dev@openjdk.java.net/msg00312.html

			// as a workaround, we make a rough guess by looking at the font point size
			
			//TextLayout textLayout = new TextLayout(text, font, frc);
			//int fontHeight = textLayout.getBounds().getHeight()
			//float fontHeight = FontDesignMetrics.getMetrics(font).getAscent();
			double fontHeight = font.getSize() * 0.7;
			canvas.drawString(text, x + margin, y + new Double(0.5 * (height + fontHeight)).intValue());
		}
	}
	
	private static String getFittingDescription(FontRenderContext frc, String description, Font font,
			float width, float height) {
		for (int characters = description.length(); characters > 0; characters--)
		{
			String truncatedText = description.substring(0, characters) + "..";
			TextLayout layout = new TextLayout(truncatedText, font, frc);
			if (fits(layout, width, height))
			{
				return truncatedText;
			}
		}
		
		// didn't fit?!
		return null;
	}

	static Font getFittingFont(String fontFace, FontRenderContext frc, int defaultsize,
			int minimumsize, String description, float width, float height) {
		for (int size = defaultsize; size > minimumsize; size--)
		{
			Font font = getFont(fontFace, size);
			TextLayout layout = new TextLayout(description, font, frc);
			if (fits(layout, width, height))
			{
				return font;
			}
		}
		return null;
	}

	static FontRenderContext getFontRenderContext() {
		return new FontRenderContext(null, true, true);
		
	}

	private static boolean fits(TextLayout layout, float width, float height) {
		Rectangle2D bounds = layout.getBounds();
		return bounds.getWidth() < width && bounds.getHeight() < height;
	}

	private float getX(Date date) {
		BigDecimal intervalSizeInMillis = BigDecimal.valueOf(interval
				.getEndMillis()
				- interval.getStartMillis());
		BigDecimal dateOffsetInMillis = BigDecimal.valueOf(date.getTime()
				- interval.getStartMillis());

		return template.getCanvasX() + dateOffsetInMillis.multiply(BigDecimal.valueOf(template.getCanvasSize())).divide(intervalSizeInMillis,
				BigDecimal.ROUND_HALF_EVEN).floatValue();
	}

	public void addBlacklist(String... entries) {
		for (String entry : entries)
		{
			blacklist.add(entry);
		}
	}

	public float getCurrentY() {
		return currenty;
	}

	@SuppressWarnings("unchecked")
	public void paint(List<Calendar> arguments, List<String> selection, GroupBy groupBy) {
		Filter filter = new Filter(new PeriodRule(new Period(new DateTime(
				interval.getStartMillis()), new DateTime(interval
				.getEndMillis()))));

		Map<String, Collection<VEvent>> eventsToPaint = new HashMap<String, Collection<VEvent>>();
		for (Calendar calendar : arguments)
		{
			Collection<VEvent> events = filter.filter(calendar.getComponents(
					Component.VEVENT));
			if (groupBy == GroupBy.CALNAME)
			{
				eventsToPaint.put(calendar.getProperty("X-WR-CALNAME").getValue().trim(), events);
			}
			else if (groupBy == GroupBy.LOCATION)
			{
				for (VEvent event : events)
				{
					String key = "";
					Location location = event.getLocation();
					if (location != null && location.getValue() != null)
					{
						key = location.getValue().trim();
					}
					addEvent(eventsToPaint, key, event);
				}
			}
		}
		
		for (Entry<String, Collection<VEvent>> entry : eventsToPaint.entrySet())
		{
			if (selection == null || selection.contains(entry.getKey()))
			{
				paint(entry.getKey(), entry.getValue());
			}
		}
	}

	private static void addEvent(Map<String, Collection<VEvent>> eventsToPaint,
			String key, VEvent event) {
		Collection<VEvent> collection = eventsToPaint.get(key);
		if (collection == null)
		{
			collection = new ArrayList<VEvent>();
			eventsToPaint.put(key, collection);
		}
		collection.add(event);
	}
}
