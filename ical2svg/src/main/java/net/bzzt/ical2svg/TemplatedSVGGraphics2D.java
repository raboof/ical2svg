package net.bzzt.ical2svg;

import java.io.Writer;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An SVGGraphics2D that keeps the original template intact.
 * 
 * For some reason, we need to re-add the template's nodes
 * when streaming out the document
 * 
 * @author arnouten
 */
public class TemplatedSVGGraphics2D extends SVGGraphics2D {

	private final Document template;
	
	public TemplatedSVGGraphics2D(Document template) {
		super(template);
		this.template = template;
	}

	@Override
	public void stream(Element svgRoot, Writer writer, boolean useCss,
			boolean escaped) throws SVGGraphics2DIOException {
		NodeList templateNodes = template.getChildNodes();
		for (int i = 0; i < templateNodes.getLength(); i++)
		{
			Node templateNode = templateNodes.item(i).cloneNode(true);
			svgRoot.insertBefore(templateNode, svgRoot.getFirstChild());
		}
		
		super.stream(svgRoot, writer, useCss, escaped);
	}
	
}
