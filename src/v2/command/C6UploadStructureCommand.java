/**
 * 
 */
package command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;

import legacy.bioinformatics.GPdbUtils;

import org.bibeault.frontman.Command;
import org.bibeault.frontman.CommandContext;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;

import services.ChainEditorInterfaceDetector;
import services.VennStructureDAO;

/**
 * This command supports structure uploads. 
 * 
 * Upload a novel or custom structure to PDB.
 * If an interface check box is on the parameter inputs, 
 * 3 interfaces are calculated.
 * 
 * The PDBTEXT field can either be a pdb id OR the full structure's text.
 * 
 * for new structure, no interface
 * just the new id is printed out.
 * 
 * for interface
 * This page is a dead end, all interface ids are printed out.
 * 
 * @author jayunit100
 *
 */
public class C6UploadStructureCommand implements Command
{

	@Override
	public void execute(CommandContext arg0) throws ServletException,IOException 
	{
		System.out.println("C6 Upload Structure" );
		String pdbText = arg0.getRequest().getParameter("pdbfile");
		VennStructureDAO vsd = new VennStructureDAO();
		System.out.println("About to commit.");
		
		//a table of id's to structures is maintained for the web pages
		//so that they can display each structure, along with its id.
		//Then the user can go into the venn workflow module 
		//input the apropriate venn id, and view their interface structure.
		Hashtable<Long,Structure> structureIds = new Hashtable<Long,Structure>();
		
		Structure s ;
		if(pdbText.length()<7)
			s = GPdbUtils.getStructure(pdbText);
		else
			s = new org.biojava.bio.structure.io.PDBFileParser() .parsePDBFile(new BufferedReader(new StringReader(pdbText)));

		s.setPDBCode("VENN");
		//if user wants an interface.
		if(arg0.getRequest().getParameter("interface") != null)
		{
			//create 3 interface files
			for(Integer i : new Integer[] {6,8,10})
			{
				try
				{
					ChainEditorInterfaceDetector c = new ChainEditorInterfaceDetector(s, i) ;
					if(c.newStructure.getChains().size()>1)
					{
						System.out.println("interface detected, saving structure to venn");
						c.newStructure.setName(c.newStructure.getName()+"(venn iface ("+i+")");
						Long id = vsd.importStructure(c.newStructure);
						structureIds.put(id,c.newStructure);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}

			
			arg0.getResponse().getWriter().println("<HTML><BODY>"); 
			for(Long l : structureIds.keySet())
			{
				Structure sL = structureIds.get(l);
				arg0.getResponse().getWriter().println("venn id <B>" +l+"</B> " + sL.getName() + "  <BR>");
				for(Chain c : sL.getChains())
				{
					arg0.getResponse().getWriter().println(c.getName() + " : " +c.getAtomSequence() + " / " + c.getAtomLength()+"<BR>");
				}
			}
			arg0.getResponse().getWriter().println("</BODY></HTML>");
			
			arg0.getResponse().getWriter().close();

		}
		//if no interface... create one file and dump the id out.
		else
		{
			vsd.importStructure(s);
			Long id = vsd.importStructure(s);
			arg0.getResponse().getWriter().println("<HTML><BODY>The Venn ID is <b>"+id + " " + "; This structure has " + s.getChains() + " chains " +"</BODY></HTML>");
			arg0.getResponse().getWriter().close();
		}
	}

	
}
