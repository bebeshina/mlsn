package builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lrec.extraction.Extractor;

public enum Foncteurs 
{
	
	p1("в","r_lieu"),
	p2("в","r_lieu_action"),
	p3("в","r_conseq"),
	p4("в","r_manner"),
	
//	c1(":CasPrepositionnel","r_lieu::à l'intérieur",p1),
//	c2(":CasAccusatif","r_lieu::vers l'intérieur", p1),
	c3("CasPrepositionnel:","r_lieu_action::à l'intérieur",p2),
	c4("CasAccusatif:","r_lieu_action::vers l'intérieur", p2),
	c5("CasAccusatif:","r_manner::modalité", p4), //comme dans "rouler en boule"; "acheter à crédit"
	
//	p5("внутрь","r_lieu::vers l'intérieur"),
//	p6("внутри","r_lieu::à l'intérieur"),
	
	p7("до","r_predesseur-time"),
	p8("до","r_manner"),
	
	c6("CasGenitif:","r_predesseur-time",p7),
	c7("CasGenitif:","r_manner::limite",p8),
	
	p9("на","r_lieu"),
	p10("на","r_lieu_action"),
	p11("на","r_has_part"),
	p12("на","r_object_mater"),
	p13("на","r_action_lieu"),
	
	c8(":CasPrepositionnel","r_lieu::surface",p9), //le livre est sur l'étagère 
	c9("CasAccusatif:","r_lieu::surface",p10),//mettre un livre sur l'étagère
	c10("CasPrepositionnel:","r_has_part::composant essentiel",p11),//système à base de transistors 
	c11("CasPrepositionnel:","r_object>matter::composant essentiel",p12),//confiture à base de miel
	c12("CasPrepositionnel:","r_action_lieu::moyen essentiel",p13),//faire sauter à l'huile + substance
	c16("CasPrepositionnel:","r_action_lieu",p10),//faire sauter à l'huile + substance
	
	p14("с","r_predesseur-space"),
	p15("с","r_cohypo"),
	p16("с","r_quantificateur"),
	p17("c","r_foaf"),
	
	c13("CasGenitif:","r_predesseur-space",p14),
	c14("CasInstrumental:","r_cohypo::association",p15),
	c15("CasAccusatif:","r_quanitifcateur:approximation",p16),

	v1("Ver:","r_predicat"),
	c50("CasAccusatif:","r_patient"),
	c51("CasNominatif:","r_agent"),
	c52("CasDatif:","r_beneficiaire"),
	c53("CasInstrumental:","r_instr"),
	c54("CasGenitif:","r_object>mater"),
	;
	
	private String trait;
	private String type;
	private Foncteurs condition;
	
	private Foncteurs(String trait,String type)
	{
		this.setTrait(trait);
		this.setType(type);
	}
	private Foncteurs(String trait,String type, Foncteurs condition)
	{
		this.setTrait(trait);
		this.setType(type);
		this.setCondition(condition);
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTrait() {
		return trait;
	}

	public void setTrait(String trait) {
		this.trait = trait;
	}
	public Foncteurs getCondition() {
		return condition;
	}
	public void setCondition(Foncteurs condition) {
		this.condition = condition;
	}
	public static void generateRules()
	{
		for(Foncteurs f:Foncteurs.values())
		{
			if(f.toString().startsWith("c"))
			{
				if(f.getCondition()!=null)
				{
					System.out.println("P	$X r_pos Ver:");
					System.out.println("P	$Y r_pos Nom:"+f.getTrait());
					String s=f.getCondition().getTrait();
					System.out.println("P	$Z == "+s);
					System.out.println("C	$X	"+f.getType()+"	$Y");
					System.out.println("\n");
				}
				else 
				{
					System.out.println("P	$X r_pos Ver:");
					System.out.println("P	$Y r_pos Nom:"+f.getTrait());
					System.out.println("C	$X	"+f.getType()+"	$Y");
					System.out.println("\n");
				}
			}
		}
	}

	public static void getRulesForFeature(String trait, LinkedList<Map<String,String>> list)
	{
		boolean flag=false;
		Map<String,Set<String>> lmap=new HashMap<String,Set<String>>();
		String verb="";
	
		for(Foncteurs f:Foncteurs.values())
		{
			if(f.getTrait().equals(trait))
			{
				/**
				 * get verb first 
				 */
				
//				for(int i=0;i<list.size()-1;i++)
//				{
//					for(Entry<String,String>e:list.get(i).entrySet())
//					{
//						
//						if(e.getValue()!=null)
//						{
//							if(!e.getValue().equals("null"))
//							{
//								for(String feat:e.getValue().split("\\,"))
//								{
//									if(feat.equals("Ver:"))
//									{
//										if(!(e.getValue().contains("Participle:")))
//										{
//											verb=e.getKey();
//											System.out.println("verb::::"+verb);
//											
//										}
//										else 
//										{
////											verb=Extractor.lemma;
////											System.out.println("previous action ::::"+verb);
//										}
//									}
//								}
//							}
//						}
//					}		
//				}
				
				
				System.out.println("processing feature "+f.getTrait());	
				for(int i=0;i<list.size()-1;i++)
				{
					for(Entry<String,String>e:list.get(i).entrySet())
					{
						if(f.getCondition()!=null)
						{
							String s=f.getCondition().getTrait();
							System.out.println(e.getKey()+"-->"+e.getValue());
							System.out.println("condition expected "+s);
							if(e.getValue()!=null)
							{
								if(!e.getValue().equals("null"))
								{
									for(String feat:e.getValue().split("\\,"))
									{
										if(feat.equals(s))
										{
											flag=true;
											System.out.println("need condition "+f.getCondition()+" for "+f.getType());
										}
										if(feat.equals("Ver:"))
										{
											verb=e.getKey();
											System.out.println("verb::::"+verb);
										}
										
									}
								}
							}
						}
						else 
						{
							System.out.println("no condition");
//							System.out.println(Foncteurs.valueOf(f.getTrait())+" --> ?"+f.getType()+"::"+e.getKey());
							if(lmap.isEmpty())
							{
								Set<String>lset=new HashSet<String>();
								
								lset.add(f.getTrait()+" --> ?"+f.getType()+"::"+e.getKey());
								
								for(String s:lset)
								{
									System.out.println("expected type : "+verb+" "+s);
								}
								
								lmap.put(verb, lset);
							}
							
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
//		generateRules();
//		getRulesForFeature("CasAccusatif:");
	}
	
}
