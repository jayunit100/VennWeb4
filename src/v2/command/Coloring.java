/**
 * 
 */
package command;

import java.awt.Color;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author jayunit100
 * Utility class for coloring the amino acids according to float
 * conservation on scale from 0 to 1. Methods return an RGB color array 
 * of size 3.
 */
public class Coloring 
{
	static Logger lg = Logger.getLogger(Coloring.class);
	/**
	 * This is the coloring type.
	 * @author jayunit100
	 *
	 */
	public static enum COLORING{RB}
;	/**
	 * Only color type right now is red->white.
	 * @param f1
	 * @param type
	 * @deprecated
	 * @return
	 */
	public static Color getColoring(Float f1,COLORING type)
	{
		if(type==COLORING.RB)
		{
			int rb= Math.round(255 - f1*255);
			Color c = new Color(255,rb,rb);
			return c;
		}
		lg.warn("Unknown coloring option " +type.name());
		return null;
	}
	
	public static Vector<Color> getRB ()
	{
		Vector<Color> rr = new Vector<Color>();
		for(float i = 0; i < 1.0; i+=.1)
		{
			int rb = Math.round(255 - i*255);
			Color c = new Color(255,rb,rb);
			rr.add(c);
		}
		System.out.println("Default coloring \n" + StringUtils.join(rr.toArray(),"\n"));
		return rr;
	}
	/**
	 * Takes an ordered array of colors [100,200,400], [100,550,100], ...
	 * The index of the array will correspond to percentile conservation
	 * that gets colored.  
	 * @param f1
	 * @param colorNames
	 * @return
	 */
	public static Color getColoring(Float f1,Vector<Color> colorNames)
	{
		if(colorNames==null)
			lg.error("Color Names vector was null.");
		//neon yellow (255,255,30) signifies a bug !
		Color color = Color.yellow;
		boolean changed=false;
		
		//for each color in the vector
		//keep incrementing the color till you pass the threshold.
		for(int i = 0 ; i < colorNames.size(); i++)
		{
			float percentile = (float) i / (float) colorNames.size();
 			if(f1 >=  percentile)
			{
 				Color c = colorNames.get(i);
 				color=colorNames.get(i);
 				changed=true;
			}
		}
		if(! changed)
			lg.warn("Color not changed from the default ! in getColoring method (uses colorNames array of size   " +colorNames.size() + " ) ");
		return color;
	}

	public static void main(String[] args)
	{
		Coloring.getRB();
	}
}
