package org.openslx.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Helper class for loading resources.
 * This should be error safe loaders with a fall back in case the
 * requested resource can't be found, or isn't of the expected type.
 */
public class ResourceLoader
{

	/**
	 * Logger for this class
	 */
	private final static Logger LOGGER = Logger.getLogger( ResourceLoader.class );

	/**
	 * Load the given resource as an ImageIcon.
	 * This is guaranteed to never throw an Exception and always return
	 * an ImageIcon. If the requested resource could not be loaded,
	 * an icon is generated, containing an error message. If even that
	 * fails, an empty icon is returned.
	 *
	 * @param path Resource path to load
	 * @param description Icon description
	 * @return ImageIcon instance
	 */
	public static ImageIcon getIcon( String path, String description )
	{
		URL url = ResourceLoader.class.getResource( path );
		if ( url == null ) {
			LOGGER.error( "Resource not found: " + path );
		} else {
			try {
				return new ImageIcon( url, description );
			} catch ( Exception e ) {
				LOGGER.error( "Resource not loadable: " + path );
			}
		}
		// If we reach here loading failed, create image containing error
		// message
		try {
			return errorIcon( "Invalid Resource: " + path );
		} catch ( Throwable t ) {
			return new ImageIcon();
		}
	}

	public static Icon getIcon( String path, String description, int maxHeight, Component context )
	{
		ImageIcon icon = getIcon( path, description );
		return getScaledIcon( icon, maxHeight, context );
	}

	/**
	 * Load the given resource as an ImageIcon.
	 * This is guaranteed to never throw an Exception and always return
	 * an ImageIcon. If the requested resource could not be loaded,
	 * an icon is generated, containing an error message. If even that
	 * fails, an empty icon is returned.
	 *
	 * @param path Resource path to load
	 * @return ImageIcon instance
	 */
	public static ImageIcon getIcon( String path )
	{
		return getIcon( path, path );
	}

	/**
	 * Helper that will create an icon with given text.
	 *
	 * @param errorText Text to render to icon
	 * @return the icon
	 */
	private static ImageIcon errorIcon( String errorText )
	{
		Font font = new Font( "Tahoma", Font.PLAIN, 20 );

		// get dimensions of text
		FontRenderContext frc = new FontRenderContext( null, true, true );
		Rectangle2D bounds = font.getStringBounds( errorText, frc );
		int w = (int)bounds.getWidth();
		int h = (int)bounds.getHeight();

		// create a BufferedImage object
		BufferedImage image = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB );
		Graphics2D g = image.createGraphics();

		// set color and other parameters
		g.setColor( Color.WHITE );
		g.fillRect( 0, 0, w, h );
		g.setColor( Color.RED );
		g.setFont( font );

		g.drawString( errorText, (float)bounds.getX(), (float) -bounds.getY() );

		g.dispose();
		return new ImageIcon( image, "ERROR" );
	}

	/**
	 * Tries to load the given resource treating it as a text file
	 *
	 * @param path Resource path to load
	 * @return content of the loaded resource as String
	 */
	public static String getTextFile( String path )
	{
		String fileContent = null;
		try ( InputStream stream = ResourceLoader.class.getResourceAsStream( path ) ) {
			fileContent = IOUtils.toString( stream, StandardCharsets.UTF_8 );
		} catch ( Exception e ) {
			LOGGER.error( "IO error while trying to load resource '" + path + "'. See trace: ", e );
		}

		if ( fileContent != null ) {
			return fileContent;
		} else {
			return "Resource '" + path + "' not found.";
		}
	}

	public static InputStream getStream( String path )
	{
		return ResourceLoader.class.getResourceAsStream( path );
	}

	private static final Map<Icon, ImageIcon> iconCache = new HashMap<>();

	public static Icon getScaledIcon( Icon icon, int height, Component context )
	{
		if ( icon == null )
			return null;
		ImageIcon cached = iconCache.get( icon );
		if ( cached != null && cached.getIconHeight() == height )
			return cached;
		// Generate?
		float iHeight = icon.getIconHeight();
		float tHeight = height;
		if ( iHeight <= tHeight )
			return icon; // Small enough
		// Scale down:
		BufferedImage image = new BufferedImage( icon.getIconWidth(), icon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB );
		Graphics2D g = image.createGraphics();
		g.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		icon.paintIcon( context, g, 0, 0 );
		ImageIcon scaledIcon = new ImageIcon( image.getScaledInstance(
				(int) ( icon.getIconWidth() * ( tHeight / iHeight ) ), (int) ( tHeight ), Image.SCALE_SMOOTH ) );
		iconCache.put( icon, scaledIcon );
		return scaledIcon;
	}

}
