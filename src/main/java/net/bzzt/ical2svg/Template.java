package net.bzzt.ical2svg;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Template {
	private static final Log LOG = LogFactory.getLog(Template.class);
	
	/**
	 * The template Document. All elements in this document should
	 * later also be added to the final SVG.
	 */
	private Document document;

	/**
	 * left and right page margins
	 */
	private Integer xmargin = 50;
	
	/**
	 * upper and lower page margins
	 */
	private Integer ymargin = 20;
	
	/**
	 * width of the 'legend'
	 */
	private Integer legendSize = 60;
	
	/**
	 * height of one row of information
	 */
	private int rowHeight = 20;
	
	/**
	 * Canvas on which the blocks are painted
	 */
	private Rectangle2D.Float canvas = new Rectangle2D.Float(xmargin + legendSize, 60, 1050 - 2*xmargin - legendSize, 10);

	/**
	 * Block in which the heading (usually the current date) is printed
	 */
	private Rectangle2D.Float heading = new Rectangle2D.Float(xmargin, canvas.y - rowHeight, legendSize, rowHeight);

	private String fontface = "Arial";
	
	public float getInitialY() {
		return canvas.y;
	}

	public Template(String filename)
	{
		init(filename == null ? null : new File(filename));
	}

	public Template(File file) {
		init(file);
	}

	private void init(File file)
	{
		if (file != null)
		{
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			Document templateDocument;
			try {
				builder = builderFactory.newDocumentBuilder();
				templateDocument = builder.parse(file);
			} catch (Exception e) {
				LOG.error("Error parsing template: " + e.getMessage(), e);
				templateDocument = null;
			}
			document = templateDocument;
		}
		else
		{
			document = null;
		}
		
		if (document != null)
		{
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			XPathExpression expr;
			try {
				expr = path.compile("//rect[@id='content']");
				Element content = (Element) expr.evaluate(document, XPathConstants.NODE);
				canvas = new Rectangle2D.Float(Float.valueOf(content.getAttribute("x")), Float.valueOf(content.getAttribute("y")),
						Float.valueOf(content.getAttribute("width")), Float.valueOf(content.getAttribute("height")));
				
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	public boolean hasDocument()
	{
		return document != null;
	}
	
	public Document getDocument()
	{
		return document;
	}

	public Float getCanvasSize() {
		return canvas.width;
	}

	public Integer getLegendSize() {
		return legendSize;
	}

	public void setCanvasSize(Float canvasSize) {
		this.canvas.width = canvasSize;
	}

	public void setLegendSize(Integer option) {
		this.legendSize = option;
	}

	public Dimension getSvgCanvasSize(float currentY) {
		return new Dimension(Float.valueOf(2 * xmargin + legendSize + canvas.width).intValue(),
				2 * ymargin + Float.valueOf(currentY).intValue());
	}

	public float getCanvasX() {
		return canvas.x;
	}

	public int getXMargin() {
		return xmargin;
	}

	public int getRowHeight() {
		return rowHeight;
	}

	public Rectangle2D.Float getHeading() {
		return heading;
	}

	public String getFontface() {
		return fontface;
	}

	public void setFontface(String fontface) {
		this.fontface = fontface;
	}
	
	
}
