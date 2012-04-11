/**
 * 
 */
package command;

import java.text.DateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import javax.servlet.ServletException;

import legacy.bioinformatics.BioinformaticsUtilities;
import legacy.util.Utilities;


/**
 * @author jayunit100
 * 
 * This servlet has a custom initialization method, but otherwise 
 * is the same as the parent class.
 */
public class CWServlet extends org.bibeault.frontman.CommandBroker{
	public static String blastTest="-";
	private ScheduledExecutorService scheduler;

	public void contextInitialized( ) 
	{
		scheduler = Executors.newSingleThreadScheduledExecutor();
	    scheduler.scheduleAtFixedRate(
	    new Runnable()
	    {
	    	public synchronized void run()
	    	{
	    		System.out.println("Periodic blast test starting " +System.currentTimeMillis());
				String connection = BioinformaticsUtilities.testUniprotConnection();
				blastTest= Utilities.getTimestamp()+" "+connection;
	    		System.out.println("Periodic blast test done " + System.currentTimeMillis() );
	    		System.out.println("Result : "  + blastTest);
	    	}
	    }, 0, 30, TimeUnit.MINUTES);
	    
	}

	public void init()
	{
		System.out.println("venn web 4 ; init.");
		try {
			super.init();
			this.contextInitialized( );
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args)
	{
		System.out.println("00");
	}
}
