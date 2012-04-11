/**
 * 
 */
package command;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import legacy.bioinformatics.BioinformaticsUtilities;
import legacy.bioinformatics.BlastBean;
import legacy.util.Utilities;

import org.bibeault.frontman.Command;
import org.bibeault.frontman.CommandContext;

import services.VennHomologSet;

/**
 * TODO rename this class to ManageHomologsCommand
 * 
 * Adds homologs in fasta format to the session.
 * Example fasta format http://www.uniprot.org/help/fasta-headers
 * 
 * Also, removes them if clear=true is a parameter on the request.
 * 
 * @author jayunit100
 *
 */
public class C5AddHomologCommand implements Command
{
	
	/**
	 * 
	 * @see org.bibeault.frontman.Command#execute(org.bibeault.frontman.CommandContext)
	 */
	@Override
	public void execute(CommandContext arg0) throws ServletException,
			IOException 
	{
		//Bing needed a reference sequence, or parent, when uploading homologs, so scores would be present.
		//thus, on the request we look for a parent parameter which can be null
		String parent = "A";
		if(arg0.getRequest().getParameter("parent") != null && arg0.getRequest().getParameter("parent").length()>0)
		{
			parent = arg0.getRequest().getParameter("parent");
		}
		
		//User can clear homologs, rather than add them, which would 
		//cause venn to default .
		if(arg0.getRequest().getParameter("clear") != null)
		{
			arg0.getSession().removeAttribute("session_homologs");
		}
		else
		{	
			Enumeration e = arg0.getSession().getAttributeNames();
			while(e.hasMoreElements())
			{
				System.out.println(e.nextElement());
			}
			//remove new lines by reading through the fasta, one by one !
			String fasta=arg0.getRequest().getParameter("fasta");
			System.out.println(fasta.split("\n").length + " are the number of new line chars in the fasta string input ");
			List<BlastBean> b1=BioinformaticsUtilities.parseBeansFromFasta(fasta);
			
			for(BlastBean bb : b1)
			{
				if(bb.getSequence()==null || bb.getSequence().length()==0)
				{
					System.out.println("WARNING null sequence " );
				}
			}
			
			for(int i = 0 ; i < b1.size(); i++)
			{
				
				//if the user supplied a parent... use it to set the blastbeans score.
				if(parent.length()>0)
				{
					double similarity = Utilities.StringUt.similarity(b1.get(i).getSequence(), parent);
					System.out.println("similarity is " + similarity + " for " + parent + " / " + b1.get(i).getSequence());
					b1.get(i).setScore(similarity);
				}
				
				//set the unique identifier.  This is usually going to be done
				//since, unless the user enters uniref formatted fasta headers, no ids will be
				//calcualted.
				if(b1.get(i).getId()==null )
				{
					System.out.println("Null id found . Generating venn id ");
					b1.get(i).setId("usr_"+i);
				}
			}
			System.out.println("Done reading fasta found " + b1.size());
	
			VennHomologSet s = new VennHomologSet("",b1);
			System.out.println(" Adding venn homolog set to session " + s + " with homolog count : " + s.getChildren().size());
			
			arg0.getSession().setAttribute("session_homologs", s);
	
			System.out.println(s.debug());
			
			System.out.println(" Done adding venn homologs ");
		}
		
		String resp = arg0.getSession().getAttribute("session_homologs")==null? "No homologs on session. ":  arg0.getSession().getAttribute("session_homologs") +"" ;

		arg0.getResponse().getWriter().print("<HTML><HEADER></HEDAER><TITLE>Done editing homologs</TITLE><BODY>Done , Now the homolog status is : <b> " + resp + " </b> </BODY></HTML>");
		arg0.getResponse().getWriter().close();
	}
	
}
