package lrec.analyse;


	import java.io.BufferedReader;
	import java.io.ByteArrayInputStream;
	import java.io.File;
	import java.io.FileInputStream;
	import java.io.FileNotFoundException;
	import java.io.FileReader;
	import java.io.IOException;
	import java.io.InputStream;
	import java.nio.charset.StandardCharsets;
	import java.sql.Connection;
	import java.sql.DriverManager;
	import java.sql.SQLException;
	import java.util.Arrays;
	import java.util.Date;
	import java.util.HashMap;
	import java.util.HashSet;
	import java.util.Iterator;
	import java.util.LinkedList;
	import java.util.Locale;
	import java.util.Map;
	import java.util.Properties;
	import java.util.Scanner;
	import java.util.Set;
	import java.util.TreeSet;
	import java.util.regex.Matcher;
	import java.util.regex.Pattern;

	import builder.GraphBuilder;
	import builder.JDMRelType;

	import org.neo4j.graphdb.Direction;
	import org.neo4j.graphdb.Node;
	import org.neo4j.graphdb.Relationship;
	import org.neo4j.graphdb.Transaction;

	public class Analyzer extends GraphBuilder
	{
		public Analyzer(){super();}
		
		public static final File stopfile=new File("resources/stopwords.txt");
		
		static final String DB_DRIVER = "com.mysql.jdbc.Driver";  
		static final String DB_CONNECTION = "jdbc:mysql://localhost/karadoc.lirmm.fr?useUnicode=yes&characterEncoding=UTF-8";
		static final String DBNAME="lemma";//nom de la table
		static final String DB_USER = "root";
		static final String DB_PASSWORD = "texte2017";
		
		private Connection connection;
		/**
		 * 
		 * @return
		 * @throws IOException
		 * needs stop words file sufficiently completed
		 */
		public Set<String> getStopwords() throws IOException
		{
			Set<String> words=new TreeSet<String>();
			BufferedReader br=new BufferedReader(new FileReader(stopfile));
			String line="";
			while((line=br.readLine())!=null)
			{
				words.add("\\b"+line+"\\b");
				words.add(line);
			}
			br.close();
			return words;
		}
		/**
		 * 
		 * @param segment (input segment (the text is already split according to the punctuation marks)
		 * @return
		 * @throws IOException
		 */
		public LinkedList<String> ngram(String segment) throws IOException
		{
			//acquisition des stopwords (conjonctions)
			
			Set<String> stop=this.getStopwords();
			LinkedList<String> ngrams=new LinkedList<String>();
			segment=segment.replaceAll("\\(.+?\\)", "").replaceAll("\\.", "");
//			Pattern p=Pattern.compile("[A-Z][a-z]");
			
			/**
			 * lowercase the first word n the string
			 */
			
			
			//get relevant separators for the input text
			StringBuilder splitter=new StringBuilder();
			splitter.append("(");
			splitter.append("\\,\\s");
			splitter.append("|");
			splitter.append("\\.");
			splitter.append("|");
			for(String sw:stop)
			{
				if(segment.contains(" "+sw+" "))
				{
					splitter.append(" "+sw+" "+"|");
				}
			}
			
			splitter.append("\\d+)");
			
			String separator=splitter.toString();
			System.out.println("separator "+separator);
			
			//doing the first split by blank
			String[] words=segment.split("(\\,)? ");
//			Matcher m=p.matcher(words[0]);
//			if(m.find())
//			{
				words[0].toLowerCase();
//			}
			
			//iterating over the words in the splitted tab
			int i=0;
			int len=words.length-1;
			String[] start=new String[1];
			String s="";
			//while the end of the input text is has not been reached
			while(i<=len)
			{
				//we start from the i-th word in the text and we build candidate multiword terms (mwt)
				start[0]=words[i];
				StringBuilder str=new StringBuilder();
				str.append(start[0].toString());
				if(i==0)
				{
					start[0].toString().toLowerCase(Locale.ENGLISH);
					words[i].toString().toLowerCase(Locale.ENGLISH);
				}
				System.out.println("start term "+words[i]+", i= "+i);
				//if  the start term is a stopword, we skip and go for the next word of the text
				if((stop.contains(start[0]))|(start[0].equals("de"))|(start[0].equals("of"))|(start[0].equals("d'")))
				{
					System.out.println("start is a stopword "+start[0]);
				}
				//otherwise we continue building mwt
				else
				{
					System.out.println("first candidate "+start[0]);
					ngrams.add(start[0]);
					//set up a second "pointer"
					int j=i;
					//length of the ngram, how many times we increment the second "pointer"
					for(int n=0;n<len+1;n++)
					{
							//position of the second pointer
							for(j++;j<=len;)
							{
								str.append(" ");
								str.append(words[j]);
								j++;
								//add one more word next time
								n++;
								s=str.toString();
								String candidate="";
								if(s.length()<=2)
								{
									candidate =s.replaceAll(separator, "");
								}
								else
								{
									String[] terms=s.split(separator);
									for(String t:terms)
									{
										candidate=t.toString();
									}
								}
							if(!((candidate.endsWith(" de"))|(candidate.endsWith(" of"))))
							{
								System.out.println("candidate "+candidate);
								//build a list of candidates (without stopwords)
								ngrams.add(s);
							}
						}//end for
					}//end for
				}
					i++; 
			}//end while	
			return ngrams;
		}
		
		/**
		 * utility functions
		 */
		@SuppressWarnings("resource")
		public static String readFileAsString(String file) throws FileNotFoundException 
		{
				Locale loc=new Locale("fr","FR");
			   return new Scanner(new FileInputStream(file),"UTF-8").useDelimiter("\\Z").useLocale(loc).next();
		}
		
		@SuppressWarnings("unused")
		private Set<String> getAvailableMarkers() throws IOException
		{
			Set<String> markerset=new HashSet<String>();
			String marker=null;
			String propfile=readFileAsString("resources/markers.params");
			StringBuilder sb=new StringBuilder();
			InputStream input = new ByteArrayInputStream(propfile.getBytes(StandardCharsets.UTF_8.name()));
			Properties props=new Properties();
			props.load(input);
			marker=props.getProperty("property");
			sb.append(marker+"\n");
			markerset.add(marker);
			
			System.out.println(sb.toString());
			
			return markerset;
		}
		private String getMarker(String m) throws IOException
		{
			String marker=null;
			String propfile=readFileAsString("resources/markers.params");

			InputStream input = new ByteArrayInputStream(propfile.getBytes(StandardCharsets.UTF_8.name()));
			Properties props=new Properties();
			props.load(input);
			marker=props.getProperty(m);
			System.out.println("marker for "+m+" "+marker);
			return marker;
		}
		
		/**
		 * @param sentence
		 * @param lang is the language has to be provided as follows (in order to correspond to label names)  : en, fr, es, ru, miam
		 * @throws IOException 
		 */
		public LinkedList<Node> text2graph(String sentence,String lang) throws IOException
		{
			/**
			 * transform text into surface graph
			 */
			LinkedList<Node> surface=new LinkedList<Node>();
			try (Transaction tx = super.graphDb.beginTx())
			{
				for(String s:this.ngram(sentence))
				{
					System.out.println("processing "+s);
					if(surface.isEmpty())
					{
						Node n=null;
						n=super.findNodeByName(s, lang);
						if(n!=null)
						{
							surface.addLast(n);
						}
					}
					else
					{
						String l=surface.getLast().getProperty("name").toString();
						System.out.println("surface last = "+surface.getLast().getProperty("name").toString());
						if(s.contains(l))
						{
							Node n=null;
							n=super.findNodeByName(s, lang);
							if(n!=null)
							{
								surface.removeLast();
								surface.addLast(n);
							}
						}
						else
						{
							if(!(l.contains(s)))
							{
								Node n=null;
								n=super.findNodeByName(s, lang);
								if(n!=null)
								{
									surface.addLast(n);
								}	
							}
						}
					}
				}
				tx.success();
				tx.close();
				System.out.println(surface.size());
			}
			StringBuilder sb=new StringBuilder();
			Node last=surface.getLast();
			for(Node n:surface)
			{
				String nodename=String.valueOf(n.getProperty("name"));
				if(n.equals(last))
				{
					sb.append(nodename);
					
					
				}
				else
				{
					sb.append(nodename+"-->");
				}
			}
			System.out.println(sb.toString());
			return surface;
		}
		
		public Set<Relationship> augmentedGraph(LinkedList<Node> surface)
		{
			Set<Relationship> relset=new HashSet<Relationship>();
			
			/**
			 * relationships at d=1
			 */
			Map<Node,Set<Relationship>> relMap=new HashMap<Node,Set<Relationship>>();
			/**
			 * related nodes at d=1
			 */
			Map<Node,Set<Node>> neighborMap=new HashMap<Node,Set<Node>>();
			
			for(Node sn:surface)
			{
				if(sn.hasRelationship())
				{
					Iterator<Relationship> ri=sn.getRelationships().iterator();
					Set<Relationship> tr=new HashSet<Relationship>();
					Set<Node> tn=new HashSet<Node>();
					while(ri.hasNext())
					{
						Relationship rel=ri.next();
						if(relMap.containsKey(sn))
						{
							tr=relMap.get(sn);
							neighborMap.get(sn);
							tr.add(rel);
							tn.add(rel.getOtherNode(sn));
							relMap.replace(sn, tr);
							neighborMap.replace(sn, tn);
							relset.addAll(tr);
						}
						else
						{
							tr.add(rel);
							tn.add(rel.getOtherNode(sn));
							relMap.put(sn, tr);
							neighborMap.put(sn, tn);
							relset.addAll(tr);
						}
					}
				}
			}
			
			/**
			 * voir si le terme t a des relations sémantiques avec les autres termes du segment à analyser
			 * recopier les relations existantes
			 */
			StringBuilder sbr=new StringBuilder();
			Set<Relationship> tempset=new HashSet<Relationship>();
			Set<Relationship> outset=new HashSet<Relationship>();
			Set<String> done=new HashSet<String>();
			Set<Relationship> valid=new HashSet<Relationship>();
			for(Relationship r:relset)
			{
				if((relMap.keySet().contains(r.getStartNode()))&(relMap.keySet().contains(r.getEndNode())))
				{
//					String rstring=String.valueOf(r.getStartNode().getProperty("name")+"--"+r.getType().name()+"-->"+r.getEndNode().getProperty("name"));
//					sbr.append(rstring+"\n");
					valid.add(r);
					done.add(r.getType().name());
				}
				
				else
				{
					/**
					 * voir plus loin d=2 
					 */
					if(!(r.getType().name().equals("r_pos")))
					{
						if(!(r.getType().name().equals("r_covers")))
						{
							Node s=r.getStartNode();
							Node t=r.getEndNode();
//							System.err.println(Arrays.asList(done));
							if(!(relMap.keySet().contains(s)))
							{
								Iterator<Relationship> rs=s.getRelationships().iterator();
								while(rs.hasNext())
								{
									Relationship rel=rs.next();
									if(neighborMap.keySet().contains(rel.getOtherNode(s)))
									{
										if(!(done.contains(rel.getType().name())))
										{
											if(!(done.contains(r.getType().name())))
											{
		//										String rstring=String.valueOf(rel.getStartNode().getProperty("name")+"--"+rel.getType().name()+"-->"+rel.getEndNode().getProperty("name"));
		//										sbr.append(rstring+"\n");
												tempset.add(rel);
												tempset.add(r);
											}
										}
									}
									else
									{
										outset.add(r);
									}
								}
							}
							if(!(relMap.keySet().contains(t)))
							{
								Iterator<Relationship> rt=t.getRelationships().iterator();
								while(rt.hasNext())
								{
									Relationship rel=rt.next();
									if(neighborMap.keySet().contains(rel.getOtherNode(t)))
									{
										if(!(done.contains(rel.getType().name())))
										{
											if(!(done.contains(r.getType().name())))
											{
		//										String rstring=String.valueOf(rel.getStartNode().getProperty("name")+"--"+rel.getType().name()+"-->"+rel.getEndNode().getProperty("name"));
		//										sbr.append(rstring+"\n");
												tempset.add(rel);
												tempset.add(r);
											}
										}
									}
								}
							}
						}
					}	
				}
			}
			relset.retainAll(valid);
//			relset.addAll(tempset);
//			relset.addAll(outset);
			for(Relationship r:relset)
			{
				String rstring=String.valueOf(r.getStartNode().getProperty("name")+"--"+r.getType().name()+"-->"+r.getEndNode().getProperty("name"));
				sbr.append(rstring+"\n");
			}
			System.out.println("SEGMENT");
			System.out.println(sbr.toString());
			
			StringBuilder hs=new StringBuilder();
			for(Relationship r:outset)
			{
				String rstring=String.valueOf(r.getStartNode().getProperty("name")+"--"+r.getType().name()+"-->"+r.getEndNode().getProperty("name"));
				hs.append(rstring+"\n");
			}
			System.out.println("HORS SEGMENT "+outset.size());
			System.out.println(hs.toString());
			
			return relset;
		}
		
		public void augmentInterlingually(Set<Relationship> agraph)
		{
			for(Relationship r:agraph)
			{
				Node[]nset=r.getNodes();
				
			}
		}
		
		/**
		 * put into a base mySQL
		 * @param surface
		 */
		static Connection getDBConnection() throws InstantiationException, IllegalAccessException, SQLException 
		{
		   Connection dbConnection=DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
		   return dbConnection;
		}
		boolean goodXid(Long xid, Long someid)
		{
			boolean ok=false;
			if(someid==xid)
			{
				ok=true;
			}
			return ok;
		}
		/**
	//   * Create the evaluation table
	//   * @throws SQLException
	//   
		 * @throws IllegalAccessException 
		 * @throws InstantiationException */
		/**
		 * 
		 * @throws SQLException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * faire les fonctions
		 * create nodes (table d'analyse)
		 * create rels(table)
		 * critère de choix des multimots
		 */
	  protected void createNodesTable() throws SQLException, InstantiationException, IllegalAccessException
	  {
		  getDBConnection().createStatement().execute(
	          "CREATE TABLE `evaluation` ("
	          + "  `id`          	INT(11) PRIMARY KEY AUTO_INCREMENT    	COMMENT 'eval id', "
	          + "  `xid`			INT(11) NOT NULL                   		COMMENT 'Feature set of x', "
	          + "  `yid` 			INT(11) NOT NULL                     	COMMENT 'Feature set of y', "
	          + "  `type`        	INT(11) NOT NULL                   		COMMENT 'abducted relation', "
	          + "  `score`        DOUBLE(2,3) NOT NULL                   	COMMENT 'abducted relation probabilistic score', "
	          + "  `eval`        	INT(11) NOT NULL                      	COMMENT 'binary evaluation, values 0,1', "
	          + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin 	COMMENT=''");
	  
	  this.connection.createStatement().execute(
	          "ALTER TABLE `evaluation` "
	          + "  ADD KEY `EVALUATION_LINE`  (`xid`, `yid`, `type`, `score`,`eval`), "
	          + "  ADD KEY `SOURCE` (`xid`), "
	          + "  ADD KEY `DESTINATION` (`yid`), "
	          + "  ADD KEY `TYPE`    (`type`,`score`), "
	          + "  ADD KEY `EVALUATION` (`eval`)");

	      this.connection.commit();
	  }
		
		public void testAnalyzer() throws IOException
		{
			
			try (Transaction tx = super.graphDb.beginTx())
			{
				LinkedList<Node> surface=this.text2graph("nous pouvons parler de culture urbaine", "fr");
				this.augmentedGraph(surface);
				
				tx.success();
				tx.close();
			}
		}
		public static void main(String[] args) throws IOException
		{
			Analyzer a=new Analyzer();
			a.testAnalyzer();
		}
		

}
