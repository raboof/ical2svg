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
import java.util.HashSet;
import java.util.Set;

import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;

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
			TextLayout textLayout = new TextLayout(time, getFont(), getFontRenderContext());
			float xPosition = getX(current.toDate());
			canvas.setColor(Color.BLACK);
			textLayout.draw(canvas, -template.getInitialY() + 10, xPosition);
			canvas.setTransform(new AffineTransform());
			
			canvas.setColor(Color.GRAY);
			canvas.draw(new Line2D.Double(xPosition, template.getInitialY() - 2,
					xPosition, currenty));
		}
	}

	private Font getFont() {
		Font font = new Font("Dialog", Font.PLAIN, 7);
		return font; 
	}

	/** filters events from the calendar and paints them */
	public void paint(Calendar calendar) {
		Filter filter = new Filter(new PeriodRule(new Period(new DateTime(
				interval.getStartMillis()), new DateTime(interval
				.getEndMillis()))));

		Collection<VEvent> events = filter.filter(calendar.getComponents(
				Component.VEVENT));

		paint(calendar.getProperty("X-WR-CALNAME").getValue().trim(), events);
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
		
		fitText(description, blocksleft - 2 * margin, template.getRowHeight()).draw(canvas, template.getXMargin() + margin, currenty + template.getRowHeight() / 2);
		for (Collection<VEvent> row : CalendarUtil.removeOverlap(eventsWithoutBlacklist)) {
			paintRow(row, currenty);
			currenty += template.getRowHeight();
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
		
		TextLayout textLayout = fitText(description, width - 2 * margin, height - 2 * margin);
		
		textLayout.draw(canvas, x + margin, y + new Double(0.5 * (height + textLayout.getBounds().getHeight())).intValue());
		
	}

	/** fit the text into the specified width and height */
	private TextLayout fitText(String description, float width, float height) {
		FontRenderContext frc = getFontRenderContext();
		int defaultsize = 6;
		int minimumsize = 3;
		
		for (int size = defaultsize; size > minimumsize; size--)
		{
			Font font = new Font("Dialog", Font.PLAIN, size);
			TextLayout layout = new TextLayout(description, font, frc);
			if (fits(layout, width, height))
			{
				return layout;
			}
		}
		
		// even with the minimum font size it doesn't fit - truncate.
		// later perhaps add word wrapping here.
		Font font = new Font("Dialog", Font.PLAIN, minimumsize);
		for (int characters = description.length(); characters > 0; characters--)
		{
			TextLayout layout = new TextLayout(description.substring(0, characters) + "...", font, frc);
			if (fits(layout, width, height))
			{
				return layout;
			}
		}

		// didn't fit?!
		return new TextLayout("?", font, frc);
	}

	private FontRenderContext getFontRenderContext() {
		return new FontRenderContext(null, true, true);
		
	}

	private boolean fits(TextLayout layout, float width, float height) {
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
}
