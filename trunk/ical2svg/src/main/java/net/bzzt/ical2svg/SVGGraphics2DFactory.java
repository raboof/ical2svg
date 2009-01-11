package net.bzzt.ical2svg;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SVGGraphics2DFactory {
	public static SVGGraphics2D newInstance() throws ParserConfigurationException
	{
//		// Get a DOMImplementation.
//		DOMImplementation domImpl = GenericDOMImplementation
//				.getDOMImplementation();
//
//		// Create an instance of org.w3c.dom.Document.
//		String svgNS = "http://www.w3.org/2000/svg";
//		Document document = domImpl.createDocument(svgNS, "svg", null);
//		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.newDocument();
		
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
		builder.newDocument();
		Document document = builder.parse(inputStream);
		return new TemplatedSVGGraphics2D(document);
	}
}
