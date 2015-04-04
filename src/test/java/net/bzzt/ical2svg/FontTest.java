package net.bzzt.ical2svg;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import junit.framework.TestCase;

import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Under openjdk, the calculation of boundingbox heights seems to be enourmously
 * inaccurately, I'm getting measurements similar to 
 *  http://www.mail-archive.com/2d-dev@openjdk.java.net/msg00312.html
 *  
 * assertions are commented out, because for now we have worked around the issue
 * by looking at the font point size instead.
 * 
 * @author arnouten
 *
 */
public class FontTest extends TestCase {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(FontTest.class);
	
	public void testOpenJdkText() throws SVGGraphics2DIOException
	{
//		String text = "Test";
//		
//		//Font
//		FontRenderContext frc = BlockSchemaPainter.getFontRenderContext();
//		Font font = BlockSchemaPainter.getFont("Arial", 7);
//		assertNotNull(font);
//
//		// openjdk for some reason reports '6.3125', while sun reports '5.09375'.
//		// sun seems to be (more) correct: in inkscape, 'Test' in Arial 7pt has
//		// a height of 5.202
//		assertTrue(new TextLayout(text, font, frc).getBounds().getHeight() < 5.3);
	}
	
//	public void testOpenJdkTextIsolated() throws SVGGraphics2DIOException
//	{
//		String text = "The Quick Brown Fox Jumped Over The Lazy Dog.";
//		
//		FontRenderContext frc = new FontRenderContext(null, false, false);
//		Font font = new Font("Tahoma", Font.PLAIN, 11);
//		assertNotNull(font);
//
////		FontDesignMetrics.getMetrics(font).getHeight();
//		
//		// openjdk reports '18.234375', while sun reports '10.734375'.
//		// This is roughly equivalent to the measurements at
//		// http://www.mail-archive.com/2d-dev@openjdk.java.net/msg00306.html ,
//		// which might suggest (further in the thread) absence of byte code 
//		// hinting in the freetype found by OpenJDK
//		double height = new TextLayout(text, font, frc).getBounds().getHeight();
//		assertEquals("Height is " + height, 10, height);
//	}
}
