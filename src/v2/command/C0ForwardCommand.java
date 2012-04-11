/**
 * 
 */
package command;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;

import org.bibeault.frontman.Command;
import org.bibeault.frontman.CommandContext;

import services.VennStructure;
import services.VennStructureDAO;

/**
 * @author jayunit100
 *
 */
public class C0ForwardCommand implements Command {

	/* (non-Javadoc)
	 * @see org.bibeault.frontman.Command#execute(org.bibeault.frontman.CommandContext)
	 */
	@Override
	public void execute(CommandContext arg0) throws ServletException, IOException 
	{
		System.out.println( "Forward commmand , to page " + arg0.getRequest().getParameter("page") );
		arg0.forwardToView( arg0.getRequest().getParameter("page") );
		
	}

}
