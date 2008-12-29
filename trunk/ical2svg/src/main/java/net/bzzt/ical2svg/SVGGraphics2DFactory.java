package net.bzzt.ical2svg;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SVGGraphics2DFactory {
	public static SVGGraphics2D newInstance()
	{
		// Get a DOMImplementation.
		DOMImplementation domImpl = GenericDOMImplementation
				.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);
		return new SVGGraphics2D(document);
	}

	public static SVGGraphics2D newInstance(Document document)
	{
		return new TemplatedSVGGraphics2D(document);
	}
	
	public static SVGGraphics2D newInstance(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(inputStream);
		return new TemplatedSVGGraphics2D(document);
	}
}
