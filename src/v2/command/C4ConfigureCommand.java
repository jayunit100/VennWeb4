/**
 * 
 */
package command;

import java.awt.Color;
import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.bibeault.frontman.Command;
import org.bibeault.frontman.CommandContext;

/**
 * @author jayunit100
 *
 */
public class C4ConfigureCommand implements Command
{

	/* (non-Javadoc)
	 * @see org.bibeault.frontman.Command#execute(org.bibeault.frontman.CommandContext)
	 */
	@Override
	public void execute(CommandContext arg0) throws ServletException,IOException 
		{

		Vector<Color> colors=new Vector<Color>();

		//parse the color vectors from the colors field.
		String[] pv = arg0.getRequest().getParameterValues("color");
		colors = new Vector<Color>();
		for(String array : pv)
		{
			System.out.print("C4-Color:"+array);
			String values = StringUtils.substringBetween(array, "[","]");
			String[] rgb = values.split(",");
			Integer[] rgbI = new Integer[3];
			for(int i = 0 ; i  <  rgbI.length; i++)
			{
				rgbI[i]=Integer.parseInt(rgb[i]);
			}
			colors.add(new Color(rgbI[0],rgbI[1],rgbI[2]));
		}
		
		arg0.getSession().setAttribute("color", colors);
		
		//cutoff, preselects cutoffs.  Used for bings problem where she wanted 
		//all sequences above a cutoff to be preselected . Here, we add that
		//value to the session for pages to access, so users dont have to click 
		//a zillion good sequences.
		Float cutoff = Float.parseFloat(arg0.getRequest().getParameter("cutoff"));
		arg0.getSession().setAttribute("cutoff", cutoff);
		
		arg0.forwardToView("getPDBFromUser");
	}
	
}
