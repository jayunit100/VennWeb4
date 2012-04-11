/**
 * 
 */
package command;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import legacy.bioinformatics.BioinformaticsUtilities;
import legacy.bioinformatics.BlastBean;
import legacy.bioinformatics.GPdbUtils;

import org.apache.commons.lang.StringUtils;
import org.bibeault.frontman.Command;
import org.bibeault.frontman.CommandContext;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;

import services.AlignmentUtil;
import services.VennHomologSet;
import services.VennMatrix;
import services.VennStructure;
import visualization.JMolWrapper;

/**
 * 
 * Submit homologs for TITRATION (not for homolog customization, thats another command)..
 * 
 * @author jayunit100
 *
 */
public class C3SubmitHomologsCommand implements Command
{
	
	Logger lg = Logger.getLogger("C3SubmitHomologs");
	int[][] neutralmatrix = new VennMatrix().matrix;

	/**
	 * 
	 */
	public void execute(CommandContext arg0) throws ServletException,
			IOException 
	{
		int penalty = Integer.parseInt(arg0.getRequest().getParameter("gap_penalty"));
		VennStructure v= (VennStructure) arg0.getSession().getAttribute("structure");
		if(v==null)
			lg.info("C3:found null structure.");
		Structure s=v.getStructure();
		
		//If user hasnt already set this, set it for them to the standard white to red conservation coloring scheme.
		//This is on the session so web pages can print conservation map.
		if(arg0.getSession().getAttribute("color") == null)
			arg0.getSession().setAttribute("color", Coloring.getRB());
		
		/////////////////This block is for Conservation Strategies added, May 30, 2011... ////////////////////
		//parse down the equivalent residues. takes a comma separated string.
		HashSet<Character> eqRes = new HashSet<Character>();
		String eqResCommaList=arg0.getRequest().getParameter("equivalentResidues");
		String[] eqr= eqResCommaList.toString().trim().split(",");
		if(eqr==null)
			lg.warning("no equivalent residues on request object !");
		//only makes sense if there is more than one residue in the set.
		if(eqr.length>1)
		{
			for(String rr:eqr)
				eqRes.add(rr.charAt(0));
		}
		//importantly, this is reset every time homologs are added... 
		arg0.getSession().setAttribute("equivalentResidues", eqRes);
		arg0.getSession().setAttribute("equivalentResidueTitle", eqResCommaList);

		//////////////////////////////////////////////////////////////////////////////////
		
		lg.info("Equivalent Residues : " + StringUtils.join(eqr,'+')+ " ; Saved on session !");
		
		Vector<Color> coloring = (Vector<Color>) arg0.getSession().getAttribute("color"); 
		
		//create a default jmol script.
		JMolWrapper j = new JMolWrapper(s);
		j.execute(j.firstCommand);

		//assemble a map of chains to the list of all seq homologs.  This is used by the sequence conservation
		//calculator.  The Sequence alignment steps below access homologs by calling get(Chain), which returns
		//a list of homologs.
		Hashtable<Chain,ArrayList<String>> chainMap=new Hashtable<Chain,ArrayList<String>>() ;
		
		for(Chain c : s.getChains())
		{	
			//only add chains to this map if the user has submitted homologs.
			//other chains should appear as ball and sticks...
			if(arg0.getRequest().getParameterMap().containsKey(c.getName()))
				chainMap.put(c, new ArrayList<String>());
		}
		
	 	//The homolog submission includes  
		//ACCESSION numbers, not sequences.
		for(Chain c : s.getChains())
		{		
				if(arg0.getRequest().getParameterMap().containsKey(c.getName()))
				{
					//for each accession...
					List<BlastBean> beans = findHomologOnSession(arg0.getSession(),arg0.getRequest().getParameterValues(c.getName()));

					//add all sequences that were found, that the user requested to use for
					//this homology threading, to the chain map
					for(BlastBean b1 : beans)
						chainMap.get(c).add(b1.getSequence());
				}
				lg.info("skipping chain " + c.getName() +", " + "no  inputs.");
		}
		
		//clear old alignments from the session.
			Enumeration e = arg0.getSession().getAttributeNames();
			while(e.hasMoreElements())
			{
				String a = e.nextElement().toString();
				if(a.contains("last_alignment"))
				{
					arg0.getSession().removeAttribute(a);
				}
			}
				
		//Step two, calculate the conservation of each residue, from
		// C->N and their respective conservation
		Hashtable<Chain,Vector<Float>> conservation = new Hashtable<Chain,Vector<Float>>();		

		//matrix is a parameter that must be in the incoming page .
		//if the parameter = "DEFAULT", then uses the default null matrix.
		//otherwise, reads from ncbi ftp/blast/matrices.  No USER matrices !!! 
		//They are too error prone and not important enough to support.
		//but maybe we can support user alignments, at some point.
		String matrixName = arg0.getRequest().getParameter("matrix");
		int[][] m = this.neutralmatrix;
		if(! matrixName.equalsIgnoreCase("DEFAULT"))
		{ 
			try
			{
				String matrix = BioinformaticsUtilities.getMatrixFromNCBI(matrixName);
				lg.info(" Matrix found at ncbi " + matrix);
				m = new VennMatrix(matrix).matrix;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				throw new IOException("Alignment matrix " + matrixName + " is unknown .  Go to ftp://ftp.ncbi.nlm.nih.gov/blast/matrices/ to check whats available. ");
			}
		}
		
		
		
		//Now here is where we build the alignments of checked homologs, using alignmentUtil class,
		//coupled with the user supplied matrix.
		for(Chain c : chainMap.keySet())
		{
			//used to use NeedlemanWunsch , homegrown version.  Now using strap so as to be consistent
			//with mnm hiv, venn1, and other software ...
			//back to NW again.  strap sucks.
			//public AlignmentUtil(int[][] ma,  String main, List<? extends BlastBean> cCequences,int p)

			AlignmentUtil a = new AlignmentUtil(m,penalty ,c.getAtomSequence(),eqRes,chainMap.get(c).toArray(new String[] {}));
			
			conservation.put(c, a.getScoresOrderedByResidue( ));
			
			//add alignment to session so pages can see it.
			arg0.getSession().setAttribute("last_alignment"+"m"+c.getName(), a.mainAlignments);
			arg0.getSession().setAttribute("last_alignment"+"t"+c.getName(), a.targetAlignments);
		}

		
		
		//if the user supplied labcutoffs, then record it. 
		Float cutoff = 1f;
		if(arg0.getRequest().getParameter("label_cutoff")!= null)
			cutoff=Float.parseFloat(arg0.getRequest().getParameter("label_cutoff"));
			
		//Now finally, for each chain from step two, 
		//get the corresponding pdb group and, via the jmol wrapper,
		//call the "color" command.
		for(Chain c : conservation.keySet())
		{
			//for histograms, the color vector has an index for each amino acid in the struture chain.
			//this is encoded as "color_conservation_vector+chainNAme" see below.
			Vector<Color> colors = new Vector<Color>();
			
			//the process of building the color vector is separate
			//from building the jmol commands.
			//this is because the color conservation vector has dual use.
			//one, to be used in alignments, two, in structural visualization.
			//thus, for clarity, its construction is in an independant loop.
			
			//first, build the linear array of colors
			for(int i = 0 ; i < c.getAtomSequence().length() ; i++)
			{
				Float cons = conservation.get(c).get(i);
				Color rgb = Coloring.getColoring(cons, coloring);
				colors.add(rgb);
			}
			
			
			arg0.getSession().setAttribute("color_conservation_vector"+c.getName(), colors);
			
			//now, build the jmol commands.
			for(int i = 0 ; i < c.getAtomSequence().length(); i++)
			{	
				Group g = GPdbUtils.getGroupAt(c,i);
				char ch = c.getName().charAt(0);
				j.highlight(ch, g, colors.get(i));
				j.spacefill(650, ch, g);
				//if highly conserved w/ respect to cutoff add a special label. 
				if( conservation.get(c).get(i) >= cutoff)
				{
					j.label("CA", g, GPdbUtils.getChar(g)+""+g.getPDBCode());
				}
			}
		}

		//marty likes his amino acids spacefilled !
		j.martyStyle();
		//also, add the color map !
		arg0.getSession().setAttribute("script", j.getScript());
		arg0.forwardToView("titrateH");
	
	}
	
	public List<BlastBean> findHomologOnSession(HttpSession s,String[] ids)
	{
		Enumeration names = s.getAttributeNames();
		ArrayList<BlastBean> beans=new ArrayList<BlastBean>();
		
		while(names.hasMoreElements())
		{
			Object name = names.nextElement();

			
			if(s.getAttribute(name.toString()) instanceof VennHomologSet)
			{
				VennHomologSet set = (VennHomologSet) s.getAttribute(name.toString());
				Hashtable<String,BlastBean> map = set.getMap();
				for(String id : ids )
				{
					//search for the ids, this could be more efficient
					BlastBean b1 = map.get(id);
					if(b1 != null)
						beans.add(b1);
				}
			}

			
		}
	
		return beans;
	}

	public static void main(String[] args)
	{
		System.out.println("Hellop world");
	}
}
