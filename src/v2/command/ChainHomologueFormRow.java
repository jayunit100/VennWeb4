/**
 * 
 */
package command;

import legacy.bioinformatics.BlastBean;
import legacy.util.Utilities;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.biojava.bio.structure.Chain;

/**
 * This class is a utility for the homologues titration page.
 * @author jayunit100
 *
 */
public class ChainHomologueFormRow extends BlastBean {
	
	Logger lg = Logger.getLogger(ChainHomologueFormRow.class);
	public String formField;
	public ChainHomologueFormRow(Chain h,BlastBean s, float cutoff) 	
	{
		lg.info("new form row for protein , sequence bean = "+ h.getAtomSequence());
		try
		{
			BeanUtils.copyProperties(this,s);
			System.out.println("h fields:"+BeanUtils.describe(h));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		//if the score is high enought, check the box !!!!!
		String sel =  (s.getScore().floatValue() > cutoff) ? " CHECKED":" ";
		
		formField = "<input name=\"<0>\" value=\"<1>\" type=\"checkbox\"" +sel+"></input>";
		formField = Utilities.rp(formField, h.getName(),s.getId());
	}
	/**
	 * @return the formField
	 */
	public String getFormField() {
		return this.formField;
	}
	/**
	 * @param formField the formField to set
	 */
	public void setFormField(String formField) {
		this.formField = formField;
	}
}
