/**
 * 
 */
package command;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import legacy.bioinformatics.BioinformaticsUtilities;

import org.apache.commons.lang.NumberUtils;
import org.bibeault.frontman.Command;
import org.bibeault.frontman.CommandContext;
import org.biojava.bio.structure.Chain;

import services.VennHomologSet;
import services.VennStructure;
import services.VennStructureDAO;
import visualization.JMolWrapper;

/**
 * @author jayunit100
 * TODO remove strap from maven dependencies.
 * To prevent DB4o from crashing, 
 * permission java.lang.reflect.ReflectPermission "suppressAccessChecks"; 
 * must be granted in the policy file.
 */
public class C1SubmitStructureCommand implements Command
{
	static Logger lg = java.util.logging.Logger.getLogger("c1 submit structure");
	public static boolean running=true;
	//not used currently, but is updated
	//in the thread below.  status
	//might help web pages decide what message to display.
	public void execute(final CommandContext arg0) throws ServletException,
			IOException 
	{
		final VennStructureDAO dao = new VennStructureDAO();
		final String pdbid=arg0.getRequest().getParameter("pdbId");
		
		//create a venn structure that is db serialized.
		VennStructure s;
		//integer codes : typically, the idea is that users can have their own structures
		// which have integer ids in venn.
		if(NumberUtils.isDigits(pdbid))
		{
			lg.info("c1: User structure ; digit recieved");
			Long vid = Long.parseLong(pdbid);
			s = dao.getStructure(vid);
			if(s==null)
			{
				arg0.getSession().setAttribute("error", "vid " + vid + " does not exist");
				arg0.forwardToView("error");
			}
		}
		//its a PDBID ! go get it from rcsb if we dont already have it.
		else
		{
			lg.info("c1: PDB Structure ; pdb recieved");

			s = dao.getStructure(pdbid);
			if(s==null)
			{
				dao.importStructure(pdbid);
			}
			s = dao.getStructure(pdbid);
			if(s==null)
			{
				arg0.getSession().setAttribute("error", "pdbid  " + pdbid + " does not exist at rcsb");
				arg0.forwardToView("error");
			}
		}
		//necessary for thread.
		final VennStructure sFinal = s;
		
		//the below line should never throw an exception, since at this point, s is non null (there are checks in both if/else above)
		lg.info("c2: structure pdb code is "+s.getStructure().getPDBCode());

		arg0.getSession().setAttribute("structure", s);
									/**
									 * This method adds homolog sets for each chain to the session.
									 * The results is session attributes with chain names, such as homologsA homologsB ....
									 * These area COPIED from the session_homologs if user has added them, and in that case,
									 * no blast homologs are used.
									 */
									int ch=0;
									for(Chain c : sFinal.getStructure().getChains())
									{
										ch++;
							//			status = "requests " + r +" Starting chain " + c.getName() + " " +ch+"/"+r+" length = "+ c.getAtomLength() +" of " + sFinal.getStructure().getChains().size();
							//			lg.info("Status requests " +r);
										lg.info("C3a: chain " + c.getName() + " len " + c.getAtomLength());
										
										//ONLY blast chains that are protein sequences with at least some real aminos.
										//this came about because bing gave me a pdb chain with a million X's in it that was 
										//really tripping up the uniprot blast server.
										if(c.getAtomSequence() != null && c.getAtomSequence().matches("[QWERTYUIOPASDFGHJKLZCVBNM]{1,10000}"))
										{
											lg.info("  " + c.getName());
											VennHomologSet homosetc = (VennHomologSet) arg0.getSession().getAttribute("session_homologs");
							
											lg.info("Venn homolog set on the session is : " + homosetc + " ");
											
											//this parameter, if checked, will induce a full blast search.
											//hopefully it will usually be unchecked.
											String fullChecked = arg0.getRequest().getParameter("full");
											boolean full = fullChecked != null && fullChecked.equals("true") ;
											lg.info("full value is : " + fullChecked + " (full blast only runs if true)");
											if(homosetc == null)
											{
												//record the amount of available homologs for debugging.
												//this is to control for the "unecessary blast" bug.
							
												//if the user clicked the full check box, automatically import from EBI... regardless.
												//if there are no homologs in the database, try to EBI import them.	
												if(
												   full || 
												   dao.getHomologs(c.getAtomSequence())==null || 
												   dao.getHomologs(c.getAtomSequence()).getChildren().size()==0
												   )
													{	
													
														if(! full)
														{
															lg.info("DAO returned no homologs for " + c.getAtomSequence());
														}
														dao.importHomologsFromEBI(c.getAtomSequence(),full);
													}
												homosetc =dao.getHomologs(c.getAtomSequence());
											}
											//one way or another, we now have homologs.
											//now we have homologs.  Lets add them to the session using the homologA homologB homologC convention.
											lg.info("Adding homolog set for chain " + c.getName() + " to session " + homosetc);
											arg0.getSession().setAttribute("homologs"+c.getName(),homosetc);
										}
										//some chains, you just cant blast...
										else
											lg.info("Not blastable : " + c.getAtomSequence() + ", not adding homologs for chain " + c.getName());
										//status = "done with chain " + c.getName() + " ( " + c.getAtomLength() + " ) ";
									lg.info("Done with chain " + c.getName());
									}//done looping through chains
									running=false;
		
		JMolWrapper w =new JMolWrapper(s.getStructure());
		w.execute(w.firstCommand);
		arg0.getSession().setAttribute("script", w.getScript());
		lg.info("Forwarding to titration view");
		arg0.forwardToView("titrate");
	}
	public static void main(String[] args)
	{ 
		lg.info(""+"AIIAIAPPALYPLPY".matches("[QWERRTYUIOPASDFGHJKLZXCVBNM]*"));;
		lg.info("start");;
		BioinformaticsUtilities.getHomologousProteinsViaEBI("jay", BioinformaticsUtilities.NEF);
	}
}
