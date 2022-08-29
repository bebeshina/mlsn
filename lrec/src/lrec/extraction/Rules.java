package lrec.extraction;

import java.util.Arrays;

public enum Rules 
{
	subject(new String[] {"V&Ncmpnn","V&Ncfpnn"},new Integer[] {13}),//prémisse, conclusion
	object(new String[] {"Ncmpan","Ncmpan"},new Integer[] {14}),
	instrument(new String[] {"",""},new Integer[] {}),
	location(new String[] {"",""},new Integer[] {}),
	manner(new String[] {"",""},new Integer[] {}),
	predicate(new String[] {"",""},new Integer[] {});
	
	private String[] premisses;
	private Integer[] conclusion;
	
	
	private Rules(String[] premisses, Integer[] conclusion)
	{
		this.premisses=premisses;
		this.conclusion=conclusion;
	}
	
	public Integer[] getConclusion()
	{
		return this.conclusion;
	}
	
	public String[] getPremisses()
	{
		return this.premisses;
	}
	
	/**
	 * calcul proba associé
	 */
	
	
	public Integer[] check(String prem)
	{
		Integer[] res=null;
		for(Rules r:Rules.values())	
		{
			if(Arrays.deepToString(r.getPremisses()).contains(prem))
			{
				res=r.getConclusion();
			}
		}
		return res;
	}

}
