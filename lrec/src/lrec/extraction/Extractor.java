package lrec.extraction;

import java.io.ByteArrayInputStream;
//import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
//import java.util.HashSet;
//import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
//import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import builder.Foncteurs;


public class Extractor 
{
	public static final String start="\\<s\\>";
	public static final String end="\\<\\/s\\>";
	public static final String dir="data";
	
	final Logger logger = LoggerFactory.getLogger(Extractor.class);
	
	public Extractor() {}
	
	public LinkedList <InputStream> list;
	/**
	 * on remplit la lste des phrases annotées
	 */
	public String getFileContents(File f) throws IOException
	{
		String c="";
		byte[] encoded = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
		c=new String(encoded, Charset.forName("UTF-8"));
		return c;
	}
	
	public List<String> split(String str)
	{
	    return Stream.of(str.replaceAll(start+"\n","").trim().split(end))//start+"\n"+
	      .map (elem -> new String(elem))
	      .collect(Collectors.toList());
	}
	public Sentence sent;
	public Set<String>inferred_rels=new HashSet<String>();
	public void execute() throws IOException
	{
		File[] directory=new File(dir).listFiles();
		for(File f:directory)
		{
			String s=this.getFileContents(f);
			this.logger.info("FILE CONTENTS : \n {}",s);
			for(String l:this.split(s))
			{
				this.logger.info("SENTENCE : \n {}",l);
				InputStream is=  new ByteArrayInputStream(l.getBytes());
				InputStreamReader isr=new InputStreamReader(is);
				sent=new Sentence(isr);
				this.convert();
				this.getSequence();
//				this.getManner();
//				this.getCarac();
//				this.getPatient();
//				this.getLieu();
//				this.getConseq();
//				this.inferLocally();
			fl.clear();
			}
			
			
//			this.applyRules();
		}
		System.out.println("printing the contents of the graph " +local_graph.size());
		
		PrintWriter pw=new PrintWriter(new FileWriter(new File("rels/sequence_1.txt")));
		for(String s:local_graph)
		{
			System.out.println(s);
			pw.println(s);
			pw.flush();
		}
		pw.close();
		System.out.println("printed the contents of the graph " +local_graph.size());
	}
	public boolean isPunctiation(String s)
	{
		Pattern p =Pattern.compile("[.;?!:,-]+");//ajouté -
		Matcher m=p.matcher(s);
		if(m.find())
		{
			return true;
		}
		return false;
	}
	public Map<String,String> conversion_map=new HashMap<String,String>();
	
	public InputStream params;
	
	@SuppressWarnings("resource")
	public static String readFileAsString(String file) throws FileNotFoundException 
	{
		   return new Scanner(new File(file)).useDelimiter("\\Z").next();
	}
	
	public void getParams() throws IOException
	{
			String ruler=readFileAsString("resources/convert.params");
			String[]rules=ruler.split("\\n[\\n]+");
			for(String rule:rules)
			{
//				System.err.println("printing rule :");
//				System.out.println(rule);
				this.params = new ByteArrayInputStream(rule.getBytes(StandardCharsets.UTF_8.name()));
				Properties properties=new Properties();
				properties.load(params);
//				String propName=properties.getProperty("category");
//				System.out.println("propName="+propName);
//				if(propName.equals(propertyName))
//				{
//					this.chooseMap(propName);
					for (String key : properties.stringPropertyNames()) 
					{
						if(!key.equals("category"))
						{
							
						    String value = properties.getProperty(key);
						    conversion_map.put(key, value);
						}
					}
//				}
			}
//			for(Entry<String,String> e:this.conversion_map.entrySet())
//			{
////				this.logger.info("{} equals {}",e.getKey(),e.getValue());
//			}

	}
	/**
	 * conversion en graphe de travail sous forme de liste des relations
	 * @param Sentence s (phrase annotée avec un outil externe, Russian Malt)
	 * @throws IOException 
	 *
	 */	
	public Map<String,String> fs;
	public LinkedList<Map<String,String>>fl=new LinkedList<Map<String,String>>();
	/**
	 * Мелко	мелко	R	R	R
1	нашинкованный	нашинковать	V	V	Vmps-smpfpa
1	лук	лук	N	N	Ncmsan
1	обжарить	обжарить	V	V	Vmn----a-p
1	на	на	S	S	Sp-l
1	сковороде	сковорода	N	N	Ncfsln
1	с	с	S	S	Sp-i
1	растительным	растительный	A	A	Afpnsif
1	маслом	масло	N	N	Ncnsin
1	.	.	S	S	SENT
	 * @param s
	 * @throws IOException
	 */
	public String lemma;
	
	public String instrument;
	public String lieu;
	public void convert() throws IOException
	{
		/**
		 * lire les paramètres de convesion
		 */
		this.getParams();
		/**
		 * lire Sentence
		 */
		for(int i=0;i<sent.h;i++)
		{
			fs=new HashMap<String,String>();
			String term=sent.getValue(i,1);
			String tag=sent.getValue(i, 5);
			String cat="";
			if(tag!=null)
			{
				cat=tag.substring(0,1);
			}
			String form=sent.getValue(i, 1);
			lemma=sent.getValue(i,2);
		
			
			String val=null;
			
			if(!tag.isEmpty())
			{
				
				if(this.isPunctiation(form))
				{
					val="punct";
					if(tag.equals("SENT"))
					{
						val="SENT";
						System.out.println(term+" val "+val);
						fs.put(term, val);
						fl.addLast(fs);
					}
				}
				else 
				{
					this.logger.info("1 : forme {}, lemme {}, tag {}",form,lemma, tag);
					forme_lemme.put(form, lemma);
					for(Entry<String,String>e:conversion_map.entrySet())
					{
//						this.logger.info("CONVERSION MAP {}{}",e.getKey(),e.getValue());
						if(e.getKey().startsWith(cat))
						{
							int ind=Integer.valueOf(e.getKey().replaceAll(cat, ""));
							String[]kv=e.getValue().split("\\,");
							String[]k=kv[0].split("\\|");
							String[]v=kv[1].split("\\|");
//							this.logger.info("processing index {}",ind);
							try
							{
								for(int n=0;n<k.length;n++)
								{
//									this.logger.info("processing str {}",k[n]);
									if(ind<tag.length())
									{
//										this.logger.info("tag substring = {}",tag.substring(ind,ind+1));
										if(tag.substring(ind,ind+1).equals(k[n]))
										{
	//										System.out.println(form+",r_pos,"+v[n]);
											if(v[n].equals("null"))
											{
												v[n]=null;
											}
											if(val==null)
											{
												if(v[n]!=null)
												{
													val=v[n];
												}
											}
											else
											{
												if(k[n]!=null)
												{
													if(v[n]!=null)
													{
														val=val+v[n];
													}
												}
											}
											
										}
									}
									else 
									{
//										this.logger.info("tag substring = {}",tag.substring(tag.length()-1));
										if(tag.substring(tag.length()-1).equals(k[n]))
										{
	//										System.out.println(form+",r_pos,"+v[n]);
											
											if(val==null)
											{
												val=v[n];
											}
											else
											{
												if(k[n]!=null)
												{
													val=val+v[n];
												}
											}
											
										}
										
									}
								}
							}
							catch(StringIndexOutOfBoundsException exep)
							{
								System.out.println("EXCEPTION ");
							}
						}
					}
					System.out.println(term+" val "+val);
				
				}			
				fs.put(term,val);
			}
			
			fl.addLast(fs);
			
			
		}
		
		
//		System.out.println("PRINTNG THE CONTENTS OF THE MAP");
//		for(int z=0;z<fl.size()-1;z++)
//		{
//			for(Entry<String,String>e:fl.get(z).entrySet())
//			{
//				System.out.println(e.getKey()+" "+e.getValue());
//				
//			}
//		}
		this.logger.info("converted !");
		
	}
	public Map<String,String> forme_lemme=new HashMap<String,String>();
	public String action; //VERBE + OBJET
	public String previous_action;
	public LinkedList<String> sequence;
	public Integer position;
	public String carac;
	public void getCarac()
	{
		for(int i=0;i<fl.size()-2;i++)
		{
			for(Entry<String,String>e1:fl.get(i).entrySet())
			{
				for(Entry<String,String>e2:fl.get(i+1).entrySet())
				{
					if((e1.getValue()!=null)&&(e2.getValue()!=null))
					{
						String s1=e1.getValue();
						String s2=e2.getValue();
						if((s1.contains("Adj:Qualif:"))&&(s2.contains("Nom:")))
						{
							
							carac=forme_lemme.get(e2.getKey())+",r_carac,"+forme_lemme.get(e1.getKey());
							if(!carac.contains("unknown"))
							{
								System.out.println(carac);
								local_graph.add(carac);
							}
//							else 
//							{
//								
//							}
//							local_graph.add(e1.getKey()+",r_lemma,"+forme_lemme.get(e1.getKey()));
//							if()
						}
					}
				}
			}
		}
	}
	
	public String manner;
	public void getManner()
	{
		for(int i=0;i<fl.size()-2;)
		{
			for(Entry<String,String>e1:fl.get(i).entrySet())
			{
				for(Entry<String,String>e2:fl.get(i+1).entrySet())
				{
					
					if((e1.getValue()!=null)&&(e2.getValue()!=null))
					{
						
						String s1=e1.getValue();
						String s2=e2.getValue();
						String lemme_1=forme_lemme.get(e1.getKey());
						String lemme_2=forme_lemme.get(e2.getKey());
						
//						if((!lemme_1.contains("unknown"))&&(!lemme_2.contains("unknown")))
//						{
//							if((s1.contains("Adv:"))&&(s2.contains("Ver:")))
//							{
//								manner=forme_lemme.get(e2.getKey())+",r_manner,"+forme_lemme.get(e1.getKey());
//								System.out.println(manner);
//								local_graph.add(manner);
//							}
//							if((s2.contains("Adv:"))&&(s1.contains("Ver:")))
//							{
//								
//								manner=forme_lemme.get(e1.getKey())+",r_manner,"+forme_lemme.get(e2.getKey());
//								System.out.println(manner);
//								local_graph.add(manner);
//							}
//						System.out.println(e1.getKey()+" "+e1.getValue());
//						System.out.println(e2.getKey()+" "+e2.getValue());
						
						// règles morpho
						if((s1.contains("Ver:")))
						{
							String verbe=e1.getKey();
							
//							System.out.println(verbe.toUpperCase());
							
//							if(verbe.startsWith("вы"))
//							{
//								manner=forme_lemme.get(e1.getKey())+",r_manner,"+ e2.getKey();
//								System.out.println(manner);
////								local_graph.add(manner);
//							}
//							if(verbe.startsWith("по"))
//							{
//								manner=forme_lemme.get(verbe)+",r_manner,"+ "с незначительной интенсивностью";
//								System.out.println(manner);
//								local_graph.add(manner);
//							}
							if(verbe.startsWith("пере"))
							{
								
								if(verbe.endsWith("ся"))
								{
									manner=forme_lemme.get(verbe)+",r_manner,"+ "между собой";
									local_graph.add(manner);
									manner=forme_lemme.get(verbe)+",r_manner,"+ "вместе";
									local_graph.add(manner);
									
								}
								else
								{
									manner=forme_lemme.get(verbe)+",r_manner,"+ "один за другим";
									local_graph.add(manner);
									manner=forme_lemme.get(verbe)+",r_manner,"+ "слишком долго";
									local_graph.add(manner);
									manner=forme_lemme.get(verbe)+",r_manner,"+ "слишком много";
									local_graph.add(manner);
									manner=forme_lemme.get(verbe)+",r_manner,"+ "между";
									local_graph.add(manner);
								}
							}
//							об
							//рас
								
						}
//							if((s1.contains("Ver:"))&&(s2.contains("CasInstrumental"))&&(s2.contains("Pl:"))&&(s2.contains("Nom:")))
//							{
//								manner=forme_lemme.get(e1.getKey())+",r_manner,"+ e2.getKey();
//								System.out.println(manner);
//								local_graph.add(manner);
//								
//							}
//							if((s2.contains("Ver:"))&&(s1.contains("CasInstrumental"))&&(s1.contains("Nom:"))&&(s1.contains("Pl:")))
//							{
//								manner=forme_lemme.get(e2.getKey())+",r_manner,"+ e1.getKey();
//								System.out.println(manner);
//								local_graph.add(manner);
//								
//							}
							
//							if((s1.contains("Ver:"))&&(s2.contains("CasInstrumental"))&&(s1.contains("Pl:")))
//							{
//								manner=forme_lemme.get(e2.getKey())+",r_manner,"+ e1.getKey();
//								System.out.println(manner);
//								local_graph.add(manner);
//								i++;
//							}
//							if(e2.getKey().equals("до"))
//							{
//								
//							}
//						}
					}
				}
			}
			i++;
		}
	}
	
	public void getConseq()
	{
		String consequence=null;
		for(int i=0;i<fl.size()-2;i++) //size-2
		{
			this.logger.info("BUG i={}",i);
			this.logger.info("BUG {}",fl);
			for(Entry<String,String>e1:fl.get(i).entrySet())
			{
				for(Entry<String,String>e2:fl.get(i+1).entrySet())
				{
					String s1=e1.getKey();
					String s2=e2.getValue();
					if(s2!=null)
					{
						if((s1.equals("в"))&&(s2.contains("Adj:"))&&(s2.contains("CasAccusatif:")))
						{
							loop:for(int m=i+1;m<fl.size()-2;m++) //i+2
							{
								for(Entry<String,String>e3:fl.get(m).entrySet())
								{
									if(e3.getValue()!=null)
									{
										if(e3.getValue().contains("Nom:"))
										{
											consequence="B "+forme_lemme.get(e2.getKey())+" "+forme_lemme.get(e3.getKey());
//											consequence=e2.getKey()+" "+e3.getKey();
											break loop;
										}
									}
								}
							}
							second_loop:for(int m=i-1;m>0;m--)
							{
								for(Entry<String,String>e4:fl.get(m).entrySet())
								{
									if(e4.getValue()!=null)
									{
										if(e4.getValue().contains("Ver"))
										{
											action=forme_lemme.get(e4.getKey());
											if(consequence!=null)
											{
												local_graph.add(action+",r_consequence,"+consequence);
												break second_loop;
											}
										}
									}
								}
							}
						}
						if((s1.equals("до"))&&(s2.contains("Nom:"))&&(s2.contains("CasGenitif:")))
						{
							this.logger.info("BUG {} {}",s1,s2);
							this.logger.info("BUG fl size = {}", fl.size());
							this.logger.info("BUG i = {}", i);
							loop:for(int m=i+2;m<fl.size()-2;m++)//m++ i+2
							{
								this.logger.info("BUG m = {}", m);
								this.logger.info("BUG fl = {}", fl);
								this.logger.info("BUG fl.get(m) = {}", fl.get(m));
	//							if(fl.get(m).contains)
								for(Entry<String,String>e3:fl.get(m).entrySet())
								{
									if(e3.getValue()!=null)
									{
										if(e3.getValue().contains("Nom"))
										{
//											consequence=forme_lemme.get(e2.getKey())+" "+forme_lemme.get(e3.getKey());
//											consequence=e2.getKey()+" "+forme_lemme.get(e3.getKey());
											consequence=forme_lemme.get(e2.getKey())+" "+e3.getKey();
											break loop;
										}
									}
								}
							}
							second_loop:for(int m=i-1;m>0;m--)
							{
								for(Entry<String,String>e4:fl.get(m).entrySet())
								{
									if(e4.getValue()!=null)
									{
										System.out.println(e4.getValue());
										if(e4.getValue().contains("Ver"))
										{
											action=forme_lemme.get(e4.getKey());
											if(consequence!=null)
											{
												local_graph.add(action+",r_consequence,"+consequence);
												break second_loop;
											}
										}
									}
								}
							}
						}
						if((s1.equals("до"))&&(s2.contains("Adj:"))&&(s2.contains("CasGenitif:")))
						{
							loop:for(int m=i+2;m<fl.size()-2;m++)
							{
								for(Entry<String,String>e3:fl.get(m).entrySet())
								{
									if(e3.getValue()!=null)
									{
										if((e3.getValue().contains("Nom"))&&(e3.getValue().contains("CasGenitif")))
										{
//											consequence=forme_lemme.get(e2.getKey())+" "+forme_lemme.get(e3.getKey());
//											consequence=e2.getKey()+" "+forme_lemme.get(e3.getKey());
											consequence=e2.getKey()+" "+e3.getKey();
											break loop;
										}
									}
								}
							}
							second_loop:for(int m=i-1;m>0;m--)
							{
								for(Entry<String,String>e4:fl.get(m).entrySet())
								{
									this.logger.info("BUG i={}",i);
									this.logger.info("BUG m={}",m);
									this.logger.info("BUG get m={}",fl.get(m));
									if(e4.getValue()!=null)
									{
										if(e4.getValue().contains("Ver"))
										{
											action=forme_lemme.get(e4.getKey());
											if(consequence!=null)
											{
												local_graph.add(action+",r_consequence,"+consequence);
												break second_loop;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
			
	
	public String patient;
	public String verbe;
	public void getPatient()
	{
		for(int i=0;i<fl.size()-2;i++)
		{
			for(Entry<String,String>e1:fl.get(i).entrySet())
			{
				String s1=e1.getValue();
				
				if((s1.contains("CasAccusatif:"))&&(s1.contains("Nom:")))
				{
					patient=e1.getKey();
					
					loop:for(int m=i+1;m<fl.size();)
					{
						for(Entry<String,String>e2:fl.get(m).entrySet())
						{
							
							if((e2.getValue().contains("Nom:"))&&(e2.getValue().contains("CasAccusatif:")))
							{
								if(e2.getValue()!=null)
								{
									{
										patient=patient+"+"+e2.getKey();
									}
								if(e2.getValue().contains("Ver:"))
								{
									if(patient!=null)
									{
										System.out.println(forme_lemme.get(e2.getKey())+",r_patient,"+patient);
										local_graph.add(forme_lemme.get(e2.getKey())+",r_patient,"+patient);
									}
								}
								if(e2.getValue().equals("SENT"))
								{ 
										break loop;	
								}	
							}
						}
						
						m++;
					}
				}
				if(s1.contains("Ver:"))
				{
					verbe=forme_lemme.get(e1.getKey());
					
					loop:for(int m=i+1;m<fl.size();)
					{
						for(Entry<String,String>e2:fl.get(m).entrySet())
						{
							
							if(e2.getValue().contains("Ver:"))
							{
								if(e2.getValue().contains("Participle:"))
								{
									verbe=forme_lemme.get(e2.getKey())+"+"+verbe;
								}
								else 
								{
									verbe=verbe+"+"+forme_lemme.get(e2.getKey());
								}
							}
							if((e2.getValue().contains("Nom:"))&&(e2.getValue().contains("CasAccusatif:")))
							{
								if(verbe!=null)
								{
									System.out.println(verbe +",r_patient,"+forme_lemme.get(e2.getKey()));
									local_graph.add(verbe +",r_patient,"+forme_lemme.get(e2.getKey()));
								}
							}
							if(e2.getValue().equals("SENT"))
							{ 
									break loop;	
							}	
						}
						m++;
					}
				}
			}
		}
		}
	}
	public String lieu_action;
	public String rel;
	public void getLieu()
	{
		first_loop:for(int i=0;i<fl.size()-1;i++)
		{
			for(Entry<String,String>e:fl.get(i).entrySet())
			{
				if(e.getValue()!=null)
				{
					if(e.getValue().contains("CasPrepositionnel:")&&(e.getValue().contains("Nom:")))
					{	
						for(int m=i-1;m>0;)
						{
							for(Entry<String,String>e2:fl.get(m).entrySet())
							{
								if(e2.getKey().equals("на"))
								{
									lieu_action=forme_lemme.get(e.getKey());
									if(lieu_action.contains("unknown"))
									{
										lieu_action=e.getKey();
									}
									rel=",r_lieu_action::surface,";	
								}
								if((e2.getKey().equals("в"))|(e2.getKey().equals("во")))
								{
									lieu_action=forme_lemme.get(e.getKey());
									if(lieu_action.contains("unknown"))
									{
										lieu_action=e.getKey();
									}
									rel=",r_lieu_action::interieur,";
								}
								if(e2.getValue()!=null)
								{
									if(e2.getValue().contains("Ver:"))
									{
										if(e2.getValue().contains("Participle:"))
										{
											verbe=forme_lemme.get(e2.getKey());
											System.out.println(verbe+rel+ lieu_action);
											local_graph.add(verbe +rel+lieu_action);
											break first_loop;
										}
										else 
										{
											verbe=forme_lemme.get(e2.getKey());
											System.out.println(verbe +rel+lieu_action);
											local_graph.add(verbe +rel+lieu_action);
											break first_loop;
										}
									
									}
									if(e2.getValue().equals("SENT"))
									{ 
											System.out.println("END OF SENTENCE");
											verbe="";
											lieu_action="";
											break first_loop;	
									}
								}
							}
						
						m--;
					}
					if(i>0)
					{
						for(Entry<String,String>e2:fl.get(i-1).entrySet())
						{
							if(e2.getValue()!=null)
							{
								if(e2.getKey().equals("на"))
								{
									lieu_action=e.getKey();
									rel=",r_lieu_action::surface,";
		//								
									
								}
								if(e2.getKey().equals("в"))
								{
									lieu_action=e.getKey();
									rel=",r_lieu_action::interieur,";
									
								}
								
								if(e2.getValue().contains("Ver:"))
								{
									if(e2.getValue().contains("Participle:"))
									{
										verbe=forme_lemme.get(e2.getKey());
										System.out.println(verbe+rel+ lieu_action);
										local_graph.add(verbe +rel+lieu_action);
//										inferred_rels.add(verbe +rel+lieu_action);
										break first_loop;
									}
									else 
									{
										verbe=forme_lemme.get(e2.getKey());
										System.out.println(verbe +rel+lieu_action);
										local_graph.add(verbe +rel+lieu_action);
										break first_loop;
									}
									
								}
									
								if(e2.getValue().equals("SENT"))
								{ 
										System.out.println("END OF SENTENCE");
										verbe="";
										lieu_action="";
										break first_loop;	
								}
								if(e2.getValue().equals("SENT"))
								{ 
									
		//								System.out.println("END OF SENTENCE");
										break first_loop;	
								}
								else 
								{
									lieu_action="";
									break first_loop;
								}
							}
						}
					}
					for(Entry<String,String>e2:fl.get(i+1).entrySet())
					{
						if(e2.getValue()!=null)
						{
							if(e2.getValue().contains("Ver:"))
							{
								if(e2.getValue().contains("Participle:"))
								{
									verbe=forme_lemme.get(e2.getKey());
									System.out.println(verbe+rel+ lieu_action);
									local_graph.add(verbe +rel+lieu_action);
									break first_loop;						
								}
								else 
								{
									verbe=forme_lemme.get(e2.getKey());
									System.out.println(verbe +rel+lieu_action);
									local_graph.add(verbe +rel+lieu_action);
									break first_loop;	
								}
							}
						}
					}	
				}
			}
			}
		}
	}
	public void getSequence()
	{
		sequence=new LinkedList<String>();
		action="";

		for(int i=0;i<fl.size()-1;i++)
		{
			for(Entry<String,String>e:fl.get(i).entrySet())
			{
				System.out.println(e.getKey()+" "+e.getValue());
				if(e.getValue()!=null)
				{
					if(e.getValue().contains("Ver:"))
					{
						if(e.getValue().contains("Participle:Past:"))
						{
//							action=forme_lemme.get(e.getKey());
							sequence.addFirst(forme_lemme.get(e.getKey()));
						}
						else 
						{
//							action=e.getKey();
							sequence.addLast(forme_lemme.get(e.getKey()));
						}
					}
					if(e.getValue().equals("SENT"))
					{
						System.out.println(sequence);
						for(int p=0;p<sequence.size()-1;p++)
						{
							if(!(sequence.get(p).equals(sequence.get(p+1))))
							{
								local_graph.add(sequence.get(p)+",r_successor_time,"+sequence.get(p+1));
							}
						}
						
						sequence=new LinkedList<String>();
					}
				}
			}
		}
	}
	
	public Set<String>local_graph=new HashSet<String>();
	
	public void inferLocally()
	{
		Set<Vector<Object>> rels=new HashSet<Vector<Object>>();
		
		Map<String,Integer>map=new HashMap<String,Integer>();
		Map<Integer,String>imap=new HashMap<Integer,String>();

		
		int source;
		String reltype;
		int cible;
		int i=0;
		for(String s:this.local_graph)
		{
			System.out.println(s);
			
			
			String[] str=s.split("\\,");
			
			if(!map.containsKey(str[0]))
			{
				map.put(str[0], i);
				imap.put(i,str[0]);
				source=i;
				i++;
			}
			else 
			{
				source=map.get(str[0]);
			}
			if(!str[2].isEmpty())
			{
				if(!map.containsKey(str[2]))
				{
					map.put(str[2], i);
					imap.put(i,str[2]);
					cible=i;
					i++;
				}
				else 
				{
					cible=map.get(str[2]);
				}
			
			
				reltype=str[1];
				Vector<Object>relation=new Vector<Object>();
				relation.add(source);
				relation.add(reltype);
				relation.add(cible);
				rels.add(relation);
			}
		}
		System.out.println("in the map :");
		for(Entry<String,Integer>e:map.entrySet())
		{
			System.out.println(e.getKey()+" "+e.getValue());
		}
		
		System.out.println("in the imap :");
		
		for(Entry<Integer,String>e:imap.entrySet())
		{
			System.out.println(e.getKey()+" "+e.getValue());
		}
		
		for(String k:map.keySet())
		{
			// récup rels 
			int x=map.get(k);
			
			//récup les relations de la cible
			Set<Vector<Object>> d1=new HashSet<Vector<Object>>();
//			Set<Vector<Object>> d2=new HashSet<Vector<Object>>();
			int y;
			int z;
			String type1;
			String type2;
			String type3;
			for(Vector<Object> v:rels)
			{
				
				if((Integer)v.get(0)==x)
				{
					d1.add(v);
				}
			}
			for(Vector<Object> r1:d1)
			{
				for(Vector<Object> r2:d1)
				{
					type1=(String)r1.get(1);
					type2=(String)r2.get(1);
					y=(Integer)r1.get(2);
					z=(Integer)r2.get(2);
					if(type1.equals("r_patient")&&type2.contains("r_lieu_action"))
					{
						type3="r_lieu";
						local_graph.add("*"+imap.get(y)+","+type3+","+imap.get(z));
						if(type2.contains("surface"))
						{
							local_graph.add("*"+imap.get(z)+","+"r_isa"+","+"плоскость");
						}
						if(type2.contains("interieur"))
						{
							local_graph.add("*"+imap.get(z)+","+"r_isa"+","+"ёмкость");
						}
					}
					if(type1.equals("r_patient")&&type2.equals("r_manner"))
					{
						type3="r_holo";
						for(int n=0;n<fl.size()-1;n++)
						{
							for(Entry<String,String>e:fl.get(n).entrySet())
							{
								if(e.getKey().equals(imap.get(z)))
								{
									if(e.getValue().contains("Pl:"))
									{
										local_graph.add("*"+forme_lemme.get(imap.get(z))+","+type3+","+imap.get(y));
									}
								}
							}
						}
						
					}
				}
			}
		}
		
	}
	
	

	
	public void applyRules()
	{
		for(Map<String,String>map:fl)
		{
			this.logger.info("num {}, ",fl.indexOf(map));
			for(Entry<String,String>e:map.entrySet())
			{
//				this.logger.info("getting rules for features {}",e.getKey()+":"+e.getValue());
				if(e.getValue()!=null)
				{
					if(!e.getValue().equals("null"))
					{
					
						String[] s=e.getValue().split("\\,");
						for(int i=0;i<s.length-1;i++)
						{
							Foncteurs.getRulesForFeature(s[i],fl);
						}	
					}
				}
			}
		}
		
		/*lire les prémisses dans un map*/
		/*vérifier si les prémisses ont une condition*/
		
	}
	public static void main(String[] args) throws IOException
	{
		Extractor e=new Extractor();
		e.execute();
		
	}
	
	/**
	 * TODO fonction pour appliquer les règles de manière plus générique (algo modifié)
	 */
	
//	public void getSentences(File f) throws IOException
//	{
//
//		Stream<String> is=null;
//		try (Stream<String> stream = Files.lines(f.toPath())) 
//		{
//			
//			Iterator<String> iter=stream.iterator();
//			String line="";
//			while(iter.hasNext())
//			{
//				if((line.contains(start))||(line.contains(end)))
//				{
//					is=null;
//				}
//				else 
//				{
//					is=stream.peek();
//				}
//			}
//			
//			stream.forEach(System.out::println);
//
//		} 
//		catch (IOException e) 
//		{
//			e.printStackTrace();
//		}
//	}

	
}
