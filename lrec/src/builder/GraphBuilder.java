package builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.exp.jdm4j.RelationType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Resource;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.internal.kernel.api.exceptions.EntityNotFoundException;
import org.neo4j.kernel.api.exceptions.*;



@SuppressWarnings("unused")
public class GraphBuilder {
	//private final static String PATH="C:\\Program Files\\neo4j-community-3.1.1\\data\\databases\\estab.db";
	//newTester.db
	private final static String PATH="C:/Program Files/neo4j/data/databases/newTester.db";
	//paths needed when building interlingual nodes
	private final static String CONFIG="C:\\Program Files\\neo4j\\conf\\neo4j.conf";
	private final static Label LABEL_ENTRY = Label.label("entryID");
	private final static Label INT=Label.label("int");
	public final Date date = new Date(System.currentTimeMillis());
	
	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{		
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				graphDb.shutdown();				
			}
		});
	}
	public GraphDatabaseService graphDb;
	public GraphBuilder()
	{
		this(PATH,CONFIG);
	}
	
	//public Long id;
	public GraphBuilder(String path, String conf) 
	{    
		this.graphDb = new GraphDatabaseFactory()
		.newEmbeddedDatabaseBuilder(new File(path))
		.loadPropertiesFromFile(conf)
		.newGraphDatabase();		
		registerShutdownHook(this.graphDb);	   
	}
	//public void putIndex(long duration, TimeUnit timeUnit)
	public void putIndex()
	{		
		try ( Transaction tx = this.graphDb.beginTx() )
		{
			Schema schema = this.graphDb.schema();			
			Iterator<IndexDefinition> iter = schema.getIndexes(GraphBuilder.LABEL_ENTRY).iterator();
			IndexDefinition index;
			boolean gotIndex = false;
			while(iter.hasNext())
			{
				index = iter.next();
				for(String key : index.getPropertyKeys())
				{
					if(key.equals("name"))
					{
						gotIndex = true;
					}
				}
			}
			if(!gotIndex){
				schema.indexFor(GraphBuilder.LABEL_ENTRY)
				.on("name")
				.create();
				//schema.awaitIndexesOnline(duration, timeUnit);
			}
			tx.success();
			tx.close();
		}
	}
	public Traverser traverseSemNode(Node startNode, JDMRelType type) {
		try (Transaction tx = this.graphDb.beginTx()) {
			TraversalDescription td = graphDb.traversalDescription()
					.evaluator(Evaluators.toDepth(1))
					.relationships(type, Direction.BOTH)
					.evaluator(Evaluators.excludeStartPosition())
					.uniqueness(Uniqueness.NODE_PATH);
			tx.success();
			return td.traverse(startNode);
		}
	}
	public Traverser traverseDirectIsa(Node startNode) {
		try (Transaction tx = this.graphDb.beginTx()) {
			TraversalDescription td = graphDb.traversalDescription()
					.evaluator(Evaluators.toDepth(1))
					.relationships(JDMRelType.r_isa, Direction.OUTGOING)
					.relationships(JDMRelType.r_hypo, Direction.INCOMING)
					.evaluator(Evaluators.excludeStartPosition())
					.uniqueness(Uniqueness.NODE_PATH);
			tx.success();
			return td.traverse(startNode);
		}
	}
	
	public Traverser traverseDirectMero(Node startNode) {
		try (Transaction tx = this.graphDb.beginTx()) {
			TraversalDescription td = graphDb.traversalDescription()
					.evaluator(Evaluators.toDepth(1))
					.relationships(JDMRelType.r_has_part, Direction.OUTGOING)
					.relationships(JDMRelType.r_mater_object, Direction.OUTGOING)
					.relationships(JDMRelType.r_holo, Direction.INCOMING)
					.evaluator(Evaluators.excludeStartPosition())
					.uniqueness(Uniqueness.NODE_PATH);
			tx.success();
			return td.traverse(startNode);
		}
	}
	
	public Traverser traverseDirectManner(Node startNode) {
		try (Transaction tx = this.graphDb.beginTx()) {
			TraversalDescription td = graphDb.traversalDescription()
					.evaluator(Evaluators.toDepth(1))
					.relationships(JDMRelType.r_manner, Direction.OUTGOING)
					.evaluator(Evaluators.excludeStartPosition())
					.uniqueness(Uniqueness.NODE_PATH);
			tx.success();
			return td.traverse(startNode);
		}
	}
	
	public Traverser traverseDirectObject(Node startNode) {
		try (Transaction tx = this.graphDb.beginTx()) {
			TraversalDescription td = graphDb.traversalDescription()
					.evaluator(Evaluators.toDepth(1))
					.relationships(JDMRelType.r_patient, Direction.INCOMING)
					.evaluator(Evaluators.excludeStartPosition())
					.uniqueness(Uniqueness.NODE_PATH);
			tx.success();
			return td.traverse(startNode);
		}
	}
	
	public Traverser traverseRelated(Node startNode) {
		try (Transaction tx = this.graphDb.beginTx()) {
			TraversalDescription td = graphDb.traversalDescription()
					.evaluator(Evaluators.toDepth(1))
					.relationships(JDMRelType.r_associated, Direction.BOTH)
					.evaluator(Evaluators.excludeStartPosition())
					.uniqueness(Uniqueness.NODE_PATH);
			tx.success();
			return td.traverse(startNode);
		}
	}
	
	public Traverser traverseDirectHypo(Node startNode) {
		try (Transaction tx = this.graphDb.beginTx()) {
			TraversalDescription td = graphDb.traversalDescription()
					.evaluator(Evaluators.toDepth(1))
					.relationships(JDMRelType.r_isa, Direction.INCOMING)
					.relationships(JDMRelType.r_hypo, Direction.OUTGOING)
					.evaluator(Evaluators.excludeStartPosition())
					.uniqueness(Uniqueness.NODE_PATH);
			tx.success();
			return td.traverse(startNode);
		}
	}
	
	
	
	public Traverser traverseGloseNode(Node startNode) {
		try (Transaction tx = this.graphDb.beginTx()) {
			TraversalDescription td = graphDb.traversalDescription().evaluator(Evaluators.toDepth(1))
					.relationships(JDMRelType.r_domain, Direction.OUTGOING)
					.relationships(JDMRelType.r_isa, Direction.OUTGOING)
					.relationships(JDMRelType.r_syn, Direction.OUTGOING)
					.evaluator(Evaluators.excludeStartPosition())
					.uniqueness(Uniqueness.NODE_PATH);
			tx.success();
			return td.traverse(startNode);
		}
	}

	public Traverser traverseNode(Node startNode)
	{
		try (Transaction tx = this.graphDb.beginTx())
		{
			TraversalDescription td=graphDb.traversalDescription()
					.evaluator(Evaluators.toDepth(2))
					.evaluator(Evaluators.excludeStartPosition())
					.uniqueness(Uniqueness.NODE_PATH);;
			tx.success();
			tx.close();
			return td.traverse(startNode);	
		}
	}
	
	public Traverser traversePair(Node startNode)
	{
		try (Transaction tx = this.graphDb.beginTx())
		{
			TraversalDescription td=graphDb.traversalDescription()
					.evaluator(Evaluators.toDepth(2))
					.relationships(JDMRelType.r_covers,Direction.BOTH)
					.evaluator(Evaluators.excludeStartPosition());
			tx.success();
			tx.close();
			return td.traverse(startNode);	
		}
	}
	private static Long lastId=0L;
	private static Label label;
	private Long getLastId()
	{
		if(lastId==0L)
		{
			Set<Long> set=new HashSet<Long>();
			try (Transaction tx = this.graphDb.beginTx())
			{
				if(graphDb.getAllNodes().iterator().hasNext())
				{
					for(Node n:graphDb.getAllNodes())
					{
						if(n.hasProperty("entryID"))
						{
							String getid=String.valueOf(n.getProperty("entryID"));
							//System.out.println("getid ="+getid);
							Long id=Long.valueOf(getid);
							set.add(id);
						}
					}
					System.out.println("set size "+set.size());
					if(set.size()>0)
					{
						lastId=Collections.max(set);
					}
				}
				tx.success();
				tx.close();
			}
		}
		return lastId;
	}
	protected Map<String,Node> nodemap=new HashMap<String,Node>();	
	protected Map<Integer,Node> idnodemap=new HashMap<Integer,Node>();	
	
	/**
	 * @param String name; //(node name)
		String lab
	 * @param String label; //corresponds to the language
	 * 
	 */
	public Node findNodeByName(String name,String lab)
	{
		Map<String,Node> m=this.nodemap;
		//label=Label.label(lab);
		Node n = null;
		try (Transaction tx = this.graphDb.beginTx())
		{
			if((!(m.isEmpty()))&&(m.containsKey(name)))
			{
				n=m.get(name);
			}
			else
			{
				//GraphBuilder.label
				//n = this.graphDb.findNode(LABEL_ENTRY, "name", name);
				n = this.graphDb.findNode(Label.label(lab), "name", name);
				if(n!=null)
				{
					m.put(name, n);
					if(n!=null)
					{
						System.out.println(n.getProperty("name")+" "+n.getProperty("entryID"));
					}
				}
			}
			tx.success();
			tx.close();
		}
		
		return n;
	}
	public Node getNodeByName(String name)
	{
		Map<String,Node> m=this.nodemap;
		//label=Label.label(lab);
		Node n = null;
		try (Transaction tx = this.graphDb.beginTx())
		{
			if((!(m.isEmpty()))&&(m.containsKey(name)))
			{
				n=m.get(name);
			}
			else
			{
				//GraphBuilder.label
				//n = this.graphDb.findNode(LABEL_ENTRY, "name", name);
				n = this.graphDb.findNode(LABEL_ENTRY, "name", name);
				if(n!=null)
				{
					m.put(name, n);
					if(n!=null)
					{
						System.out.println(n.getProperty("name")+" "+n.getProperty("entryID"));
					}
				}
			}
			tx.success();
			tx.close();
		}
		
		return n;
	}
	public void checkAvailableProperties()
	{
		try (Transaction tx = this.graphDb.beginTx())
		{
			for(String k:graphDb.getAllPropertyKeys())
			{
				System.out.println("property =" +k);
			}
			tx.success();
			tx.close();
		}
		
	}
	public void handleDuplicatedNodes(String lab)
	{
		label=Label.label(lab);
		Set<Node> set=new HashSet<Node>();
		try(Transaction tx = this.graphDb.beginTx())
		{
			ResourceIterator<Node> iter=graphDb.findNodes(label);
			
			while(iter.hasNext())
			{
				set.add(iter.next());
			}
			tx.success();
			tx.close();
		}
		for(Node n1:set)
		{	
				for(Node n2:set)
				{
					boolean duplicated=false;
					String name1=String.valueOf(n1.getProperty("name"));
					String name2=String.valueOf(n2.getProperty("name"));
					long id1=Long.valueOf(String.valueOf(n1.getProperty("entryID")));
					long id2=Long.valueOf(String.valueOf(n2.getProperty("entryID")));
					if(name1.equals(name2)&(id1!=id2))
					{
						duplicated=true;
						System.err.println("duplicated = "+duplicated+" n1=>"+n1.getProperty("name")+" "+n1.getProperty("entryID")+" n2=>"+n2.getProperty("name")+" "+n2.getProperty("entryID"));
					}
					if(duplicated)
					{
						Set<Relationship> relset=new HashSet<Relationship>();
						try(Transaction tx = this.graphDb.beginTx())
						{
						
							//get relationships
							
							if(n1.hasRelationship())
							{
								//récupérer les relations de n1
								Iterator<Relationship> itern1=n1.getRelationships().iterator();
								while(itern1.hasNext())
								{
									Relationship r=itern1.next();
									relset.add(r);
								}
							}
							if(n2.hasRelationship())
							{
								//récupérer les relations de n2
								Iterator<Relationship> itern2=n2.getRelationships().iterator();
								while(itern2.hasNext())
								{
									Relationship r=itern2.next();
									relset.add(r);
								}
							}
							tx.success();
							tx.close();
						}
						//get ids
						Node tokeep=null;
						Node tothrow=null;
						
						if(id1<id2)
						{
							tokeep=n1;
							tothrow=n2;
						}
						else
						{
							tokeep=n2;
							tothrow=n1;
						}
						if((tokeep!=null)&&(tothrow!=null))
						{
							try(Transaction tx2=this.graphDb.beginTx())
							{
								System.out.println("keeping the node "+tokeep.getProperty("name")+" "+tokeep .getProperty("entryID"));
								for(Relationship rel:relset)
								{
									
									System.out.println(rel.getStartNode().getProperty("name")+" "+rel.getType()+" "+rel.getEndNode().getProperty("name"));
									Node startNode=rel.getStartNode();
									Node endNode=rel.getEndNode();
									RelationshipType type=rel.getType();
									if((startNode.equals(tokeep))||(endNode.equals(tokeep)))
									{
										System.out.println("skipping existing relation... "+rel.toString());
									}
									else
									{
										if(startNode.equals(tothrow))
										{
											this.makeRelationship(tokeep,type,endNode);
											System.out.println("re-mapping relationship "+tothrow.getProperty("entryID")+" --> "+tokeep.getProperty("entryID")+" "+rel.getType()+" "+rel.getEndNode().getProperty("name"));
										
										}
										if(endNode.equals(tothrow))
										{
											this.makeRelationship(startNode, type, tokeep);
											System.out.println("re-mapping relationship "+rel.getStartNode().getProperty("name")+" "+rel.getType()+" "+rel.getEndNode()+" "+tothrow.getProperty("entryID")+" --> "+tokeep.getProperty("entryID"));
										}
										//rel.delete();
									}
								}
								tx2.success();
								tx2.close();
								}
								System.err.println("tothrow has relationships ? "+tothrow.hasRelationship());
								if(!(tothrow.hasRelationship()))
								{
									try(Transaction tx3=this.graphDb.beginTx())
									{
									//tothrow.delete();
									String idt=String.valueOf(tothrow.getProperty("entryID"));
									this.graphDb.execute("MATCH (n) WHERE n.entryID='"+String.valueOf(idt)+"' DETACH DELETE n");
									System.out.println("killing the node "+tothrow.getProperty("name")+" "+tothrow.getProperty("entryID")+"\n");
									
									tx3.success();
									tx3.close();
									}
								}
									
							
						}//end duplicated
					}//end for n2
					
				}//end for n1
		
			}//end try
	}
	public void detachDelete(int eid)
	{
		try(Transaction tx = this.graphDb.beginTx())
		{
			this.graphDb.execute("MATCH (n) WHERE n.entryID='"+eid+"' DETACH DELETE n");
			tx.success();
			tx.close();
		}	
	}
	public void createRel(Node s,String relname,Node t)
	{
		try(Transaction tx = this.graphDb.beginTx())
		{
			/**
			 * check if relationship already exists
			 */
			String sid=String.valueOf(s.getProperty("entryID"));
			String tid=String.valueOf(t.getProperty("entryID"));
			Result res=this.graphDb.execute("MATCH (p)=(n)-[r:"+relname+"]->(m) WHERE n.entryID='"+sid+"' AND m.entryID='"+tid+"' RETURN p");
			if(res==null)
			{
				this.graphDb.execute("MATCH (n),(m) WHERE n.entryID='"+sid+"' AND m.entryID='"+tid+" CREATE (n)-[r:"+relname+"]->(m)");	
			}
			tx.success();
			tx.close();
		}
	}
	protected Set<Node> deleted=new HashSet<Node>();
	public void deleteNodeWithRels(Node node) throws EntityNotFoundException,NotFoundException
	{
		if(!(deleted.contains(node)))
		{
			try(Transaction tx = this.graphDb.beginTx())
			{
				try
				{
					if(node.hasRelationship())
					{
						Iterable<Relationship> allNodeRelationships = node.getRelationships();
					    for (Relationship relationship : allNodeRelationships) 
					    {
					        relationship.delete();
					    }
					}
					}
				catch(Exception e)
				{
					if(node != null)
					{
						System.err.println(node.getId());
					}
					else
					{
						System.err.println("tutu");
					}
				}
				deleted.add(node);
				System.out.println("DELETED SIZE = "+deleted.size());
			    node.delete();
			    
			    tx.success();
			    tx.close();
			}
		}
	}	
	public void deleteNode(int eid)
	{
		Node node = this.findNodeById(eid);
		try(Transaction tx = this.graphDb.beginTx())
		{
			boolean connected=node.hasRelationship();
			if(connected)
			{
				System.out.println("node is connected, you need to delete relationships first");
			}
			else
			{
				deleted.add(node);
				node.delete();
			}
			tx.success();
			tx.close();
		}
	}
	public void deleteRelationship(Relationship rel)
	{
		Node start=rel.getStartNode();
		Node end=rel.getEndNode();
		RelationshipType type=rel.getType();
		
		try(Transaction tx = this.graphDb.beginTx())
		{
			/**
			 * describe the relationship for further check
			 */
			rel.delete();
			tx.success();
			tx.close();
		}
		/**
		 * check whether the relationship is deleted
		 */
		try(Transaction tx2 = this.graphDb.beginTx())
		{
			Iterable<Relationship> allRelationships = start.getRelationships();
			boolean exists=false;
		    for (Relationship r : allRelationships) 
		    {
		    	if(r.getEndNode().equals(end))
		    	{
		    		if(r.getType().equals(type))
		    		{
		    			exists=true;
		    		}
		    	}
		    }
		    if(!exists)
		    {
		    	System.out.println("relationship deleted");
		    }     
		   tx2.success();
		   tx2.close();
		}
	}
	public void getDuplicatedNodesByName(String name)
	{
		
		Set<Node> set=new HashSet<Node>();
		Node choice=null;
		try(Transaction tx = this.graphDb.beginTx())
		{
			for(Node node:graphDb.getAllNodes())
			{
				if(String.valueOf(node.getProperty("name")).equals(name))
				{
					set.add(node);
				}
			}
			if(!(set.isEmpty()))
			{
				Long id=this.getLastId();
				for(Node ns:set)
				{
					System.out.println(ns);
					long found=Long.valueOf(String.valueOf(ns.getProperty("entryID")));
					System.out.println("found id "+found+" last id = "+id);
					if(found<=id)
					{
						id=found;
						choice=ns;
					}
				}
				System.out.println(choice.getProperty("name")+" "+choice.getProperty("entryID"));
			}
			tx.success();
			tx.close();
		}
	}
	public Node findNodeById(Integer id)
	{
		Map<Integer,Node> m=this.idnodemap;
		
		Node n = null;
		try (Transaction tx = this.graphDb.beginTx())
		{
			if((!(m.isEmpty()))&&(m.containsKey(id)))
			{
				n=m.get(id);
			}
			else
			{
				n = this.graphDb.findNode(LABEL_ENTRY,"entryID",String.valueOf(id));
				if(n!=null)
				{
					m.put(id, n);
					System.out.println(n.getProperty("name")+" "+n.getProperty("entryID"));
				}
				else
				{
					System.out.println("not found");
				}
			}
			
			tx.success();
			tx.close();
		}
		return n;
	}
	public Node makeNode(String s,String lab)
	{
		Node node=null;
		Node lookup=null;
		try (Transaction tx = this.graphDb.beginTx())
		{
			//check if already exist
			lookup=this.findNodeByName(s,lab);
			if(lookup==null)
			{
				System.out.println("creating a new node");
				if(lastId==0L)
				{
					lastId=this.getLastId()+1L;
					System.out.println("lastiId ==0 "+lastId);
					node=this.graphDb.createNode();
					node.addLabel(Label.label(lab));
					node.addLabel(LABEL_ENTRY);
					node.setProperty("entryID",String.valueOf(lastId));
					node.setProperty("name", s);
					node.setProperty("weight", "25");
				}
				else
				{
					lastId++;
					System.out.println("new lastiId "+lastId);
					node=this.graphDb.createNode();
					node.addLabel(Label.label(lab));
					node.addLabel(LABEL_ENTRY);
					node.setProperty("entryID",String.valueOf(lastId));
					node.setProperty("name", s);
					node.setProperty("weight", "25");
				}
					if(graphDb.getAllNodes()==null)
					{
						System.out.println("no nodes in the graph "+lastId);
						long newid=1;
						node=this.graphDb.createNode();
						node.addLabel(Label.label(lab));
						node.addLabel(LABEL_ENTRY);
						node.setProperty("entryID",String.valueOf(newid));
						node.setProperty("name", s);
						node.setProperty("weight", "25");
					}
			}
			else
			{
				if(!(deleted.contains(lookup)))
				{
					node=lookup;
					System.out.println("node already exists : "+node.getProperty("name"));
				}
					
			}
		tx.success();
		tx.close();
		}
		
		
		return node;
	}
	/**takes as input  a map (dictionary) language-term
	if term has refinements, check whether the refinements match and link
	otherwise calculate the similarity (jaccard) on lexical relations (we're trying to match labels)
	and calculate the sim on semantics (isa, haspart, matter, loc, instr, telic role)
	 * @param s
	 * @param exter
	 * @return
	 */
	public Node makeInterlingualNode(String s,Map<String,String> exter)
	{
		Node node=null;
		Node lookup=null;
		try (Transaction tx = this.graphDb.beginTx())
		{
			//check properties, if already exist
			for(Node n:graphDb.getAllNodes())
			{
				if(n.hasProperty("lex"))
				{
					@SuppressWarnings("unchecked")
					Map<String,String> map=(Map<String,String>) n.getProperty("lex");
					int intersection=0;
					boolean larger=false;
					if(exter.size()>map.size())
					{
						larger=true;
					}
					int difference=0;
					for(Entry<String,String> e1:map.entrySet())
					{
						String lang=e1.getKey();
						String term_map=e1.getValue();
						if(exter.containsKey(lang))
						{
							String term_exter=exter.get(lang);
							if(term_map.equals(term_exter))
							{
								intersection++;
							}
							else
							{
								difference++;
							}
						}
					}
					if((intersection>=2)&&(larger))
					{
						node=n;
						node.setProperty("lex", exter);
					}
					
					if((intersection==1)&&(difference>=intersection))
					{
						//@TODO create a supra blank node that covers the intersection
						//and a separate inter node for 
						//and refinement relationship 
						
						
					}
				}
			}
//					intersection.addAll(lex);
//					union.addAll(lex);
//					union.addAll(set);
//					intersection.retainAll(set);
//					
//					
				
				//@TODO check the cover relationship
				
			}
			//check if already exist
			lookup=this.findNodeByName(s,"int");
			if((lookup==null))
			{
				System.out.println("lookup is null");
				//get the highest id
				Set<Long> set=new HashSet<Long>();
				if(graphDb.getAllNodes().iterator().hasNext())
				{
					for(Node n:graphDb.getAllNodes())
					{
							String getid=String.valueOf(n.getProperty("entryID"));
							System.out.println("getid ="+getid);
							Long id=Long.valueOf(getid);
							set.add(id);
					}
					System.out.println("set size "+set.size());
					if(set.size()>0)
					{
						long lastid=Collections.max(set);
						long newid=lastid+1;
						node=this.graphDb.createNode();
						node.addLabel(INT);
						node.setProperty("entryID",String.valueOf(newid));
						node.setProperty("name", s);
						node.setProperty("weight", "25");
					}
					if(set.size()==0)
					{
						long newid=1;
						node=this.graphDb.createNode();
						node.addLabel(INT);
						node.setProperty("entryID",String.valueOf(newid));
						node.setProperty("name", s);
						node.setProperty("weight", "25");
					}
				}
				else
				{
					long newid=1;
					node=this.graphDb.createNode();
					node.addLabel(INT);
					node.setProperty("entryID",String.valueOf(newid));
					node.setProperty("name", s);
					node.setProperty("weight", "25");
				}
			//add the next node	
			}
			else
			{
				node=lookup;
				System.out.println("lookup is not null : "+lookup.getProperty("name"));
			}
			

		return node;
		}
		//return node;
	
	public Integer relationshipExists(Node s, RelationshipType type,Node t)
	{
		int w=0;
		try (Transaction tx = this.graphDb.beginTx())
		{
			//check if relationship exist 
			if(s!=null)
			{
				if(s.hasRelationship(Direction.OUTGOING,type))
				{
					for(Relationship r:s.getRelationships(Direction.OUTGOING,type))
					{
						Node target=r.getEndNode();
						if(target.equals(t))
						{
							w=Integer.valueOf(String.valueOf(r.getProperty("weight")));
						}
					}
				}
			}
			
			tx.success();
			tx.close();
		}
		return w;
	}
	
	public Relationship createRelationship(String s, JDMRelType type, String t,String labs,String labt)
	{
		Relationship rel=null;
		
		try (Transaction tx = this.graphDb.beginTx())
		{
			Node n=this.makeNode(s,labs);
			Node m=this.makeNode(t,labt);
		
			//check if relationship exist 
			int w=this.relationshipExists(n,type,m);
			if(w>0)
			{
				System.out.println("w>0");
				for(Relationship r:n.getRelationships(Direction.OUTGOING, type))
				{
					System.out.println("r end node = "+r.getEndNode().getProperty("name"));
					if(r.getEndNode().equals(m))
					{
						System.out.println("in the if loop");
						//increment weight if exists
						r.setProperty("weight", Integer.valueOf(String.valueOf(r.getProperty("weight")))+5);
						rel=r;
					}
				}
			}
			else
			{
				//création
				if(n!=null)
				{
					n.createRelationshipTo(m,type).setProperty("weight", 25);
					//vérification
					for(Relationship r:n.getRelationships(Direction.OUTGOING, type))
					{
						if(r.getEndNode().equals(m))
						{
							rel=r;
						}
					}
				}
			}
			tx.success();
			tx.close();
		}
		return rel;
	}
	public Relationship makeRelationship(Node s, RelationshipType type, Node t)
	{
		Relationship rel=null;
		try (Transaction tx = this.graphDb.beginTx())
		{
			//check if relationship exist 
			int w=this.relationshipExists(s,type,t);
			if(w>0)
			{
				System.out.println("Relationship exists between "+s.getProperty("name")+" "+type.name()+" "+t.getProperty("name")+" w>0");
				for(Relationship r:s.getRelationships(Direction.BOTH, type))
				{
					System.out.println("r end node = "+r.getEndNode().getProperty("name"));
					/**
					 * outgoing relation
					 */
					if(r.getEndNode().equals(t))
					{
						//increment weight if exists
						r.setProperty("weight", Integer.valueOf(String.valueOf(r.getProperty("weight")))+5);
						rel=r;
					}
					/**
					 * incoming relation
					 */
					if(r.getStartNode().equals(t))
					{
						//increment weight if exists
						r.setProperty("weight", Integer.valueOf(String.valueOf(r.getProperty("weight")))+5);
						rel=r;
					}
				}
			}
			else
			{
				//création
				if((s!=null)&&(t!=null))
				{
					s.createRelationshipTo(t,type).setProperty("weight", 25);
					//vérification
					for(Relationship r:s.getRelationships(Direction.OUTGOING, type))
					{
						if(r.getEndNode().equals(t))
						{
							rel=r;
						}
					}
				}
			}
			tx.success();
			tx.close();
		}
		return rel;
	}
	//binary triples' builder from file, need to know the mark and the relation type
	public void BuildBinaryRelsFromFile(String path, String mark, JDMRelType type, String l1, String l2) throws IOException
	{
		File file=new File(path);
		BufferedReader br=new BufferedReader(new FileReader(file));
		String line="";
		
			List<String> mylist = null;
			long batchNumber = 1;
			int batchSize = 500;
			while((line=br.readLine())!=null)
			{	
				System.err.println("Batch Number # " + batchNumber + "\n");
				System.out.println("processing line "+line);
				mylist = readBatch(br, batchSize);
				try(Transaction tx = this.graphDb.beginTx())
				{
					for (int i = 0; i < mylist.size(); i++) 
					{  
						String[] nodes=mylist.get(i).trim().split(mark);
						System.out.println("nodes len ="+nodes.length);
						if(nodes.length==2)
						{
							System.out.println("creating relationship from "+nodes[0]+" to "+nodes[1]);
							this.createRelationship(nodes[0].trim(), type, nodes[1].trim(),l1,l2);
							//this.createRelationship(nodes[0].toLowerCase(), RelType.r_object, nodes[0].toLowerCase(),l1,l2);
						}
						if(nodes.length>2)
						{
							for(int j=1;j<nodes.length-1;j++)
							{
								System.out.println("creating relationship from "+nodes[0]+" to "+nodes[j]);
								this.createRelationship(nodes[0].trim(), type, nodes[j].trim(),l1,l2);
								//this.createRelationship(nodes[j].toLowerCase(), RelType.r_isa, nodes[0].toLowerCase(),l1,l2);
								//this.createRelationship(nodes[0], RelType.r_isa,"recipe", "en", "int");
							}
						}
					}
				 batchNumber++;
				 tx.success();
				 tx.close();
			}
		}
		br.close();
	}
	
	public void BuildSelfRelsFromFile(String path, String mark, JDMRelType type, String l) throws IOException
	{
		File file=new File(path);
		BufferedReader br=new BufferedReader(new FileReader(file));
		String line="";
		try(Transaction tx = this.graphDb.beginTx())
		{
			while((line=br.readLine())!=null)
			{
				String[] nodes=line.trim().split(mark);
				System.out.println("nodes len ="+nodes.length);
//				for(int i=1;i<nodes.length-1;i++)
//				{
					System.out.println("creating lemma relationship from "+nodes[0]+" to "+nodes[0]);
					this.createRelationship(nodes[0], type, nodes[0],l,l);
				//}
			}
			tx.success();
		}
		br.close();
	}
	/**
	 * would work for language specific nodes
	 * @param n1 (ls node) 
	 * @param n2 (ls node) 
	 */
	public void CreateSupraNode(Node n1,Node n2)
	{
		try(Transaction tx = this.graphDb.beginTx())
		{
			Set<Node> ns1=this.getCoverSet(n1);
			Set<Node> ns2=this.getCoverSet(n2);
			Set<Node> intersection=new HashSet<Node>();
			intersection.addAll(ns1);
			intersection.retainAll(ns2);
			if(intersection.size()>=1)
			{
				System.err.println("ns1->"+ns1.size()+" "+" ns2->"+ns2.size()+" intersection->"+intersection.size());
			}
			Node supra=null;
			if((ns2.size()>=2)&&(ns1.size()>=2))
			{
				if(intersection.size()>=(ns1.size()-1))
				{
					long lid=this.getLastId();
					supra=this.makeNode("int:"+String.valueOf(lid+1L)+"/", "int");
					supra.setProperty("src","supraBot");
					supra.setProperty("date", String.valueOf(date));
					System.err.println("creating supra node "+supra.getProperty("name"));
					String lex=null;
					for(Node n:intersection)
					{
						this.makeRelationship(supra, JDMRelType.r_hypo, n);
						if(n.hasProperty("lex"))
						{
							lex=lex+","+String.valueOf(n.getProperty("lex")).replaceAll("(\\[|\\])","");
						}
						else
						{
							Iterator<Relationship> iter=n.getRelationships(Direction.OUTGOING, JDMRelType.r_covers).iterator();
							while(iter.hasNext())
							{
								Relationship r=iter.next();
								Node end=r.getEndNode();
								Set<String> languageLabels=new HashSet<String>();
								for(Label l:end.getLabels())
								{
									if(!(l.name().equals("entryID")))
									{
										languageLabels.add(l.name());
									}
								}
								String s=String.valueOf(r.getEndNode().getProperty("name"));
								for(String lstr:languageLabels)
								{
									lex=lex+","+s+"@"+lstr;
								}	
							}
						}
					}
					if(lex!=null)
					{
						supra.setProperty("lex", lex);
					}
				}
			}
			tx.success();
			tx.close();
		}
	}
	
	public void BuildFromWordpairs(String path,String mark,String lang1,String lang2,String dataSource) throws IOException
	{
		File file=new File(path);
		BufferedReader br=new BufferedReader(new FileReader(file));
		String line="";
		
			while((line=br.readLine())!=null)
			{
				try(Transaction tx = this.graphDb.beginTx())
				{
					String[] nodes=line.trim().split(mark);
					if(nodes.length>1)
					{
						System.out.println("Processing "+line);
						Node n1=this.makeNode(nodes[0], lang1);
						Node n2=this.makeNode(nodes[1], lang2);
						if(!(String.valueOf(n1.getProperty("src")).equals(dataSource)))
						{
							n1.setProperty("src", n1.getProperty("src")+","+dataSource);
						}
						if(!(String.valueOf(n2.getProperty("src")).equals(dataSource)))
						{
							n2.setProperty("src", n2.getProperty("src")+","+dataSource);
						}
						Node cover =null;
						if(this.isCovered(n1))
						{
							System.err.println("first if");
							cover=this.getCover(n1);
							this.makeRelationship(cover, JDMRelType.r_covers, n2);
						}
						else
						{
							if(this.isCovered(n2))
							{
								System.err.println("second if");
								cover=this.getCover(n2);
								this.makeRelationship(cover, JDMRelType.r_covers, n1);
							}
						}
						if(cover==null)
						{
							System.err.println("no cover found");
							Node in=this.makeNode("int:"+nodes[0]+"/", "int");
							in.setProperty("src", dataSource);
							this.makeRelationship(in, JDMRelType.r_covers, n1);
							this.makeRelationship(in, JDMRelType.r_covers, n2);
						}
						
					}
				tx.success();
				tx.close();
			}
		}
		br.close();
		
	}
	public void BuildTernaryRelsFromFile(String path, String mark, JDMRelType type1, JDMRelType type2, String l1,String l2,String l3, boolean isVerbal) throws IOException
	{
		File file=new File(path);
		if(isVerbal=true)
		{
			//following the pattern Predicate Arg1 Arg2
			//default String mark="\\|"; --> trouver comment faire avec default
			BufferedReader br=new BufferedReader(new FileReader(file));
			String line="";
			try(Transaction tx = this.graphDb.beginTx())
			{
				while((line=br.readLine())!=null)
				{
					String[] nodes=line.toLowerCase().trim().split(mark);
					System.out.println("nodes len ="+nodes.length);
					{
						System.out.println("creating relationship from "+nodes[0]+" to "+nodes[1]);
						//create ternary node's name 
						String ternary=nodes[0]+"["+type1.name()+"]"+nodes[1]+"["+type1.name()+"]"+nodes[2];
						
						//create relationships
						this.createRelationship(ternary, JDMRelType.r_predicate, nodes[0], l1,l2);
//						this.createRelationship(ternary, type1, nodes[1]);
//						this.createRelationship(ternary, type2, nodes[2]);
					}		
				}
				tx.success();
				tx.close();
			}
			br.close();
		}
		else
		{
			//in this case we follow the pattern Head Mod1 Mod2
			BufferedReader br=new BufferedReader(new FileReader(file));
			String line="";
			try(Transaction tx = this.graphDb.beginTx())
			{
				while((line=br.readLine())!=null)
				{
					String[] nodes=line.toLowerCase().trim().split(mark);
					System.out.println("nodes len ="+nodes.length);
					{
						System.out.println("creating relationship from "+nodes[0]+" to "+nodes[1]);
						//create ternary node's name 
						String ternary=nodes[0]+"["+type1.name()+"]"+nodes[1]+"["+type1.name()+"]"+nodes[2];
						/**
						 * finer  if conditions 
						 */
						//create relationships
					this.createRelationship(ternary, JDMRelType.r_head, nodes[0],l1, l2);
//						this.createRelationship(ternary, type1, nodes[1],l2);
//						this.createRelationship(ternary, type2, nodes[2]);
					}		
				}
				tx.success();
				tx.close();
			}
			br.close();
			
		}
		//format for predicative ternary nodes
		

	}
	public void createAnnotatedRel(String s, String t, JDMRelType type,String a, String label_lang,String label_annot) throws IOException
	{
		//string format (annotated rel) wheat germ|haspart>>certain|wheat, ">>" is the mark of annotation
		try(Transaction tx = this.graphDb.beginTx())
		{
			this.createRelationship(s, type, t, label_lang, label_lang);
			String binary=s+"["+type.name()+"]"+t;
			this.createRelationship(binary,JDMRelType.r_annotation, a, label_lang, label_annot);
			this.createRelationship(binary,JDMRelType.r_source,s, label_lang, label_lang);
			this.createRelationship(binary,JDMRelType.r_target,t,label_lang, label_lang);	
			
			tx.success();
			tx.close();
		}
	}	
	public void BuildAnnotatedRel(String s, String mark, JDMRelType type,String annot, String label_lang,String label_annot) throws IOException
	{
		//string format (annotated rel) wheat germ|haspart>>certain|wheat, ">>" is the mark of annotation
			try(Transaction tx = this.graphDb.beginTx())
			{
				
					String[] nodes=s.replaceAll("_"," ").trim().split(mark);//toLowercase()
					{
						String src=nodes[0];
						String tgt=nodes[1];
						System.out.println("creating relationship from "+nodes[0]+" to "+nodes[1]);
						//create annotated binary node
						String binary=nodes[0]+"["+type.name()+"]"+nodes[1];
						
						this.createRelationship(binary,JDMRelType.r_annotation, annot, label_lang, label_annot);
						this.createRelationship(binary,JDMRelType.r_source,src, label_lang, label_lang);
						this.createRelationship(binary,JDMRelType.r_target,tgt,label_lang, label_lang);
					}		
				tx.success();
				tx.close();
		}
	}	
	public void BuildInterlingualNode(String en, String fr, String es,String ru) throws IOException, EntityNotFoundException
	{
		String[]args={en,fr,es,ru};
		//if we have at least 2 languages
		if(args.length>=2)
		{
			//if english term!=null, connect to the english graph
			String enURI="";
			String frURI="";
			//String esURI="";
			String ruURI="";
//			@SuppressWarnings("unused")
			String intURI="";
			String intName="";
			
			String[] uris=new String[4];
			//set intName en, es or fr
			//default int:en
			if(en.length()>0)
			{
				intName="int:"+en+"/";
			}
			else
			{
				if(es.length()>0)
				{
				//create an URI for the interlingual node int/name/id
				intName="int:"+es+"/";
				}
				else
				{
					intName="int:"+fr+"/";
				}
			}
			//get the URIs of lexicalized en, es, fr, ru nodes
			if(en!=null)
			{
				//GraphBuilder gbEn=new GraphBuilder(ENG,CONFIG);
				//this.graphDb=gbEn.graphDb;
				try(Transaction tx = this.graphDb.beginTx())
				{
					//create or check the existence of the node 
					if(this.makeNode(en,"en")!=null)
					{
						//create an URI for this node db/data/name/id
						String id=String.valueOf(this.makeNode(en,"en").getProperty("entryID"));
						enURI="en:"+en+"/"+id;
						uris[0]=enURI;
					}
					tx.success();
				}
			}
//			if(es!=null)
//			{
//				GraphBuilder gbEs=new GraphBuilder(ESP,CONFIG);
//				//this.graphDb=gbEs.graphDb;
//				try(Transaction tx = gbEs.graphDb.beginTx())
//				{
//					//create or check the existence of the node 
//					if(this.makeNode(es)!=null)
//					{
//						//create an URI for this node db/data/name/id
//						String id=String.valueOf(this.makeNode(es).getProperty("id"));
//						esURI="es:"+es+"/"+id;
//						propertyArray.add(esURI);
//					}
//					tx.success();
//				}
//			}
			if(fr!=null)
			{
//				GraphBuilder gbFr=new GraphBuilder(FRA,CONFIG);
//				//this.graphDb=gbFr.graphDb;
				try(Transaction tx = this.graphDb.beginTx())
				{
					//create or check the existence of the node 
					if(this.makeNode(fr,"fr")!=null)
					{
						//create an URI for this node db/data/name/id
						String id=String.valueOf(this.makeNode(fr,"fr").getProperty("entryID"));
						frURI="fr:"+fr+"/"+id;
						uris[1]=frURI;
					}
					tx.success();
					
				}
			}
			uris[2]="";
			uris[0]="";
			if(ru!=null)
			{
				//GraphBuilder gbRu=new GraphBuilder(RUS,CONFIG);
				//this.graphDb=gbRu.graphDb;
				try(Transaction tx = this.graphDb.beginTx())
				{
					//create or check the existence of the node 
					if(this.makeNode(ru,"ru")!=null)
					{
						//create an URI for this node db/data/name/id
						String id=String.valueOf(this.makeNode(ru,"ru").getProperty("entryID"));
						ruURI="ru:"+ru+"/"+id;
						uris[3]=ruURI;
					}
					tx.success();
				}
			}
			//if the interlingual name is not null
			if(intName!=null)
			{
				//create interlingual node
				//GraphBuilder gbInt=new GraphBuilder(PATH,CONFIG);
				try(Transaction tx = this.graphDb.beginTx())
				{
					Node interlingual=this.makeNode(intName,"int");
					interlingual.setProperty("lex", uris);
					String id=String.valueOf(interlingual.getProperty("entryID"));
					intURI=intName+id;
					//this.makeRelationship(interlingual, RelType.r_covers, this.makeNode(ru, "ru"));
					this.makeRelationship(interlingual, JDMRelType.r_covers, this.makeNode(en, "en"));
					this.makeRelationship(interlingual, JDMRelType.r_covers, this.makeNode(fr, "fr"));
					tx.success();
					tx.close();
				}
			}
			
		}
	}
	public void BuildFromWN(File f) throws IOException
	{
		/**
		 * obtain meta-information (POS, RelType) from file names
		 */
		String fname=f.getName();
		Pattern p=Pattern.compile("wn\\_en\\_(?<pos>[A-Z]+)\\_(?<rtype>r\\_\\w+)\\.txt");
		Matcher m=p.matcher(fname);
		
		JDMRelType type=null;
		Node pos_node=null;
		while(m.find())
		{
			String rname=String.valueOf(m.group("rtype"));
			String npos=String.valueOf(m.group("pos"));
			if(!(rname.equals("r_else")))
			{
				
				if(rname.equals("r_mero"))
				{
					type=JDMRelType.r_has_part;
				}
//				else
//				{
//					if(rname.equals("r_hypo"))
//					{
//						type=RelType.r_hypo;
//					}
//				}
			}
			if(npos.equals("NOUN"))
			{
				pos_node=this.findNodeByName("int:Noun/", "int");
			}
			else
			{
				if(npos.equals("VERB"))
				{
					pos_node=this.findNodeByName("int:Verb/", "int");
				}
			}
		}
		if(type!=null)
		{
			
			/**
			 * first, check if the node exists and if it is covered
			 */
			BufferedReader br=new BufferedReader(new FileReader(f));
			String line="";
			String mark="\\|";
			
			
			while((line=br.readLine())!=null)
			{
				String[] str=line.split(mark);
				try(Transaction tx=this.graphDb.beginTx())
				{
					Node source=null;
					Node target=null;
					/***** modifications *****/
					source=this.findNodeByName(str[0], "en");
					target=this.findNodeByName(str[2], "en");
					if((source!=null)&&(target!=null))
					{
						System.out.println("source and target do exist ");
						System.out.println("source "+source);
						System.out.println("target "+target);
						//this.makeRelationship(source,RelType.r_pos,target);
						
						Set<Node> s_cover=this.getCoverSet(source);
						Set<Node> t_cover=this.getCoverSet(target);
						/**
						 * looking for the corresponding covering nodes
						 */
						Node ns=null;
						Node nt=null;
						if((!(s_cover.isEmpty())&&(!(t_cover.isEmpty()))))
						{
							for(Node n:s_cover)
							{
								if((String.valueOf(n.getProperty("name"))).equals("int:"+str[0]+"/"))
								{
									ns=n;
								}
							}
							for(Node n:t_cover)
							{
								if((String.valueOf(n.getProperty("name"))).equals("int:"+str[1]+"/"))
								{
									nt=n;
								}
							}
						}
						if((ns!=null)&&(nt!=null))
						{
							System.out.println("ns = "+ns);
							System.out.println("nt = "+nt);
							
							Relationship r=this.makeRelationship(source,type,target);
							System.out.println("r="+r);
							r.setProperty("src","WordNet");
							
							Relationship rel=this.makeRelationship(ns,type,nt);
							rel.setProperty("src","WordNet");
//							this.makeRelationship(ns,RelType.r_pos,pos_node);
//							this.makeRelationship(nt,RelType.r_pos,pos_node);
						}
						else
						{
							Relationship re=this.makeRelationship(source,type,target);
							re.setProperty("src","WordNet");;
//							this.makeRelationship(source,RelType.r_pos,pos_node);
//							this.makeRelationship(target,RelType.r_pos,pos_node);
						}
					}
					
					tx.success();
					tx.close();
				}
			}
			br.close();
			}
			
		/**
		 * second, create the relation in the language specific part
		 */
		/**
		 * third, create the same relation in the interlingual layer if both source and target nodeds are covered
		 */
	}
	public void BuildInterlingualNodeFromDBNary(Map<String,String[]>args) throws IOException
	{
		String intName="";
		String pos=null;
		
		//first pass with english as source language
		try(Transaction tx = this.graphDb.beginTx())
		{
			for(Entry<String,String[]> e:args.entrySet())
			{
				/**
				 * if we actually have a pair (or more)
				 */
				if(e.getValue()!=null)
				{
					if(e.getKey().contains("Noun"))
					{
						pos="int:Noun/";
					}
					if(e.getKey().contains("Proper_noun"))
					{
						pos="int:PropNoun/";
					}
					if(e.getKey().contains("Adjective"))
					{
						pos="int:Adj/";
					}
					if(e.getKey().contains("Verb"))
					{
						pos="int:Verb/";
					}
					if(e.getKey().contains("Adverb"))
					{
						pos="int:Adv/";
					}
					if(e.getKey().contains("Conjunction"))
					{
						pos="int:Conj/";
					}
					if(e.getKey().contains("Interjection"))
					{
						pos="int:Interj/";
					}
					if(e.getKey().contains("Phrase"))
					{
						pos="int:Phrase/";
					}
					if(e.getKey().contains("Pronoun"))
					{
						pos="int:Pronoun/";
					}
					if(pos!=null)
					{
						
						intName="int:"+e.getKey().replaceAll("en\\:","").replaceAll("\\_\\_[A-Z]?[a-z]+(\\_[a-z]+)?\\_\\_\\d", "").replaceAll("\\_"," ").trim()+"/";	
						System.out.println("pos is not null, int name is "+intName);
						
						Node posnode=this.makeNode(pos, "int");
						Node inter=this.makeNode(intName,"int");
						inter.setProperty("lex", e.getValue());
						for(String str:e.getValue())
						{
							if(str.endsWith("@en"))
							{
								
								String term=str.replaceAll("\\@en", "").trim();
								Node thisnode=this.makeNode(term, "en");
								this.makeRelationship(thisnode, JDMRelType.r_pos, posnode);
								this.makeRelationship(inter, JDMRelType.r_covers, thisnode);
							}
							if(str.endsWith("@fr"))
							{
								String term=str.replaceAll("\\@fr", "").trim();
								Node thisnode=this.makeNode(term, "fr");
								this.makeRelationship(thisnode, JDMRelType.r_pos, posnode);
								this.makeRelationship(inter, JDMRelType.r_covers, thisnode);
							}
							if(str.endsWith("@es"))
							{
								String term=str.replaceAll("\\@es", "").trim();
								Node thisnode=this.makeNode(term, "es");
								this.makeRelationship(thisnode, JDMRelType.r_pos, posnode);
								this.makeRelationship(inter, JDMRelType.r_covers, thisnode);
							}
							if(str.endsWith("@ru"))
							{
								String term=str.replaceAll("\\@ru", "").trim();
								Node thisnode=this.makeNode(term, "ru");
								this.makeRelationship(thisnode, JDMRelType.r_pos, posnode);
								this.makeRelationship(inter, JDMRelType.r_covers, thisnode);
							}
						}
					}
				}
			}
		tx.success();
		tx.close();
		}
		
	}
	public Traverser hasCover(Node startNode)
	{
		try (Transaction tx = this.graphDb.beginTx())
		{
			TraversalDescription td=graphDb.traversalDescription()
					.relationships(JDMRelType.r_covers,Direction.INCOMING)
					.evaluator(Evaluators.toDepth(1))
					//.evaluator(Evaluators.excludeStartPosition())
					;
			tx.success();
			tx.close();
			return td.traverse(startNode);	
		}
	}
	/**
	 * to be applied to covering nodes in order to grasp generic semantics (not language specific) as is it used to calculate Jaccard index
	 * @param coverNode
	 * @return
	 */
	public Traverser getSemantics(Node coverNode)
	{
		try (Transaction tx = this.graphDb.beginTx())
		{
			TraversalDescription td=graphDb.traversalDescription()
					.relationships(JDMRelType.r_has_part,Direction.OUTGOING)
					.relationships(JDMRelType.r_holo,Direction.OUTGOING)
					.relationships(JDMRelType.r_mater_object,Direction.OUTGOING)
					.relationships(JDMRelType.r_patient,Direction.BOTH)
					.relationships(JDMRelType.r_carac,Direction.OUTGOING)
					.relationships(JDMRelType.r_manner,Direction.OUTGOING)
					.evaluator(Evaluators.toDepth(1))
					//.evaluator(Evaluators.excludeStartPosition())
					;
			tx.success();
			tx.close();
			return td.traverse(coverNode);	
		}
	}
	public Traverser getRel(Node node,JDMRelType type)
	{
		try (Transaction tx = this.graphDb.beginTx())
		{
			TraversalDescription td=graphDb.traversalDescription()
					.depthFirst()
					.relationships(type,Direction.OUTGOING)
					.evaluator(Evaluators.toDepth(1))
					.evaluator(Evaluators.excludeStartPosition());
			tx.success();
			tx.close();
			return td.traverse(node);	
		}
	}

	public boolean isCovered(Node node)
	{
		boolean hcov=false;
		try(Transaction tx=this.graphDb.beginTx())
		{
			if(node.hasRelationship(Direction.INCOMING, JDMRelType.r_covers))
			{
			
				hcov=true;	
			}
			
			//System.out.println(node.getProperty("name")+" is covered ="+hcov);
			tx.success();
			tx.close();
		}
		return hcov;
	}
	public Node getCover(Node node)
	{
		Node interlingual=null;
		boolean is_covered=this.isCovered(node);
		if(is_covered)
		{
			Iterator<Relationship> iter=node.getRelationships(JDMRelType.r_covers).iterator();
			while(iter.hasNext())
			{
				Relationship relinter=iter.next();
				interlingual=relinter.getOtherNode(node);
				//System.out.println("interlingual node is "+interlingual.getProperty("name"));
			}

		}
		
		return interlingual;
	}
	public Set<Node> getCoverSet(Node node)
	{
		Set<Node> nodeSet=new HashSet<Node>();
		try (Transaction tx = this.graphDb.beginTx())
		{
			Node interlingual=null;
			boolean is_covered=this.isCovered(node);
			
			if(is_covered)
			{
				Iterator<Relationship> iter=node.getRelationships(Direction.INCOMING,JDMRelType.r_covers).iterator();
				while(iter.hasNext())
				{
					Relationship relinter=iter.next();
					interlingual=relinter.getStartNode();
					nodeSet.add(interlingual);
				}
	
			}
			
			tx.success();
		}
		catch(NullPointerException e)
		{
			System.out.println("no cover");
			return null;
		}
		
		return nodeSet;
	}
	public Set<Node> getAllCoveredNodes()
	{
		Set<Node> nodeset=new HashSet<Node>();
		
		try (Transaction tx = this.graphDb.beginTx())
		{
			int i=0;
			for(Node n1:this.graphDb.getAllNodes())
			{
	
				if(this.isCovered(n1))
				{
					nodeset.add(n1);
				}
				i++;
			}
			
			tx.success();
			System.err.println("total number of nodes = "+i+" covered size = "+nodeset.size());
		}	
		return nodeset;
	}
	public Set<Node> getCoveredNodes(Node intNode)
	{
		Set<Node> nodeset=new HashSet<Node>();
		try (Transaction tx = this.graphDb.beginTx())
		{
			int i=0;
			Iterator<Relationship> iter=intNode.getRelationships(Direction.OUTGOING,JDMRelType.r_covers).iterator();
			while(iter.hasNext())
			{
				Relationship r=iter.next();
				Node n=r.getEndNode();
				nodeset.add(n);
				i++;
			}
			
			tx.success();
//			System.err.println("number of covered nodes = "+i+" covered size = "+nodeset.size());
		}	
		return nodeset;
	}
	/**
	 * 
	 * @param file f with a list of domains to be integrated
	 */
	public void setDomains(File f)
	{
		
	}
	public void inferRelations(Node node)
	{
		Node inter=this.getCover(node);
		if(inter!=null)
		{
			Iterator<Relationship> iter=node.getRelationships(Direction.OUTGOING).iterator();
			while(iter.hasNext())
			{
				Relationship r=iter.next();
				RelationshipType t=r.getType();
				System.out.println("printing type "+t);
				Node n=r.getEndNode();
				System.out.println("traversing "+n.getProperty("name"));
				Node internode=this.getCover(n);
				if(internode!=null)
				{
					this.makeRelationship(inter,t,internode);
				}
				else
				{
					System.out.println("n is not covered, no changes");
				}		
			}
		}
	}
	public void inferEveryRelation()
	{
		for(Node node:this.graphDb.getAllNodes())
		{
			Node inter=this.getCover(node);
			
			if(inter!=null)
			{
				System.out.println(inter.getProperty("name"));
				System.out.println("inter is not null");
				Iterator<Relationship> iter=node.getRelationships(Direction.BOTH).iterator();
				while(iter.hasNext())
				{
					
					Relationship r=iter.next();
					RelationshipType t=r.getType();
					Node n=r.getEndNode();
					System.out.println("traversing "+n.getProperty("name"));
					Node internode=this.getCover(n);
					if(internode!=null)
					{
						this.makeRelationship(inter,t,internode);
					}
					else
					{
						System.out.println("node is not covered, no changes");
					}		
				}
			}
		}
	}
	private static List<String> readBatch(BufferedReader br, int batchSize) throws IOException 
	{
	    // Create a List object which will contain your Batch Sized lines
	    List<String> result = new ArrayList<>();
	    for (int i = 1; i < batchSize; i++) {  // loop thru all your lines
	        String line = br.readLine();
	        if (line != null) {
	            result.add(line);   // add your lines to your (List) result
	        } else {
	            return result;  // Return your (List) result
	        }
	    }
	    return result;   // Return your (List) result
	}
	public void getPhrase(String lab)
	{
		try (Transaction tx = this.graphDb.beginTx())
		{
			if(((ResourceIterator<Node>)graphDb.findNodes(Label.label(lab))).hasNext())
			{
				for(Node n:graphDb.getAllNodes())
				{
					String term=String.valueOf(n.getProperty("name"));
					if(!(term.contains("(")))
					{
						String[] tab=term.split(" ");
						
						if(tab.length>=2)
						{
							System.err.println(term);
							for(String t:tab)
							{
								
									String nodename=String.valueOf(t);
									Node target=this.makeNode(nodename,lab);
									this.makeRelationship(n,JDMRelType.r_locution,target);
									System.out.println(term+" --> "+nodename);
							}
						}
					}
				}
			}
			tx.success();
		}
		
	}
	public void LexicalInclusion() throws IOException
	{
		PrintWriter pw=new PrintWriter(new FileWriter(new File("C:/Users/clairet/Documents/CORPORA/LEMMA/ENGLISH/inferred_rels_1.txt")));
		PrintWriter comp=new PrintWriter(new FileWriter(new File("C:/Users/clairet/Documents/CORPORA/LEMMA/ENGLISH/inferred_mwt_1.txt")));
		Set<String> vu=new HashSet<String>();
		try (Transaction tx = this.graphDb.beginTx())
		{
			if(graphDb.getAllNodes().iterator().hasNext())
			{
				for(Node n:graphDb.getAllNodes())
				{
					//get positions in the phrase
					Map<String,Integer> lm=new HashMap<String,Integer>();
					String phrase=String.valueOf(n.getProperty("name"));
					String[] position=phrase.split(" ");
					if(position.length>=2)
					{
						System.out.println("phrase "+phrase);
						for(int i=0;i<position.length;i++)
						{
							lm.put(position[i], i);
							System.out.println(position[i]+","+i);
						}
					}
					if(n.hasRelationship(Direction.OUTGOING, JDMRelType.r_locution))
					{
						Map<Node,Integer> ln=new HashMap<Node,Integer>();
						Iterator<Relationship> iter=n.getRelationships(Direction.OUTGOING, JDMRelType.r_locution).iterator();
						while(iter.hasNext())
						{
							Node neighbour=iter.next().getEndNode();
							String nName=String.valueOf(neighbour.getProperty("name"));
							System.out.println("nName "+nName);
							//get position of the nName
							Integer posit=lm.get(nName);
							System.out.println("posit "+posit);
							ln.put(neighbour, posit);
						}
						for(Entry<Node,Integer> e1:ln.entrySet())
						{
							for(Entry<Node,Integer> e2:ln.entrySet())
							{
								Node n1=e1.getKey();
								Node n2=e2.getKey();
								String s1=String.valueOf(n1.getProperty("name"));
								String s2=String.valueOf(n2.getProperty("name"));
								if((n1.hasRelationship(Direction.OUTGOING, JDMRelType.r_pos))&&(n2.hasRelationship(Direction.OUTGOING, JDMRelType.r_pos)))
								{
									System.out.println(ln.get(n2));
									System.out.println(ln.get(n1));
									Integer offset=(Integer)ln.get(n2)-ln.get(n1);
									System.out.println(offset);
									
									Iterator<Relationship> p1=n1.getRelationships(Direction.OUTGOING, JDMRelType.r_pos).iterator();
									Iterator<Relationship> p2=n2.getRelationships(Direction.OUTGOING, JDMRelType.r_pos).iterator();
									
									String pos1="";
									while(p1.hasNext())
									{
										Node psp1=p1.next().getEndNode();
										pos1=String.valueOf(psp1.getProperty("name"));
									}
									
									String pos2="";
									while(p2.hasNext())
									{
										Node psp2=p2.next().getEndNode();
										pos2=String.valueOf(psp2.getProperty("name"));
									}
									if(offset==1)
									{
										if((pos1.equals("JJ"))&&((pos2.equals("NN"))|(pos2.equals("NNS"))))
										{
											if(!(vu.contains(s1+" "+s2+"|r_isa|"+s2)))
											{
												pw.println(s1+" "+s2+"|r_isa|"+s2);
												vu.add(s1+" "+s2+"|r_isa|"+s2);
											}
											if(!(vu.contains(s2+"|r_carac|"+s1)))
											{
												pw.println(s2+"|r_carac|"+s1);
												vu.add(s2+"|r_carac|"+s1);
											}
											pw.flush();
										}
										if(((pos1.equals("NN"))|(pos1.equals("NNS")))&&((pos2.equals("NN"))|(pos2.equals("NNS"))))
										{
//											if(!(vu.contains(s2+"|r_haspart|"+s1)))
//											{
//												pw.println(s2+"|r_haspart|"+s1);
//												vu.add(s2+"|r_haspart|"+s1);
//											}
//											if(!(vu.contains(s1+"|r_holo|"+s2)))
//											{
//												pw.println(s1+"|r_holo|"+s2);
//												vu.add(s1+"|r_holo|"+s2);
//											}
											if(!(vu.contains(s1+" "+s2)))
											{
												comp.println(s1+" "+s2);
												vu.add(s1+" "+s2);
											}
											
											
											
											pw.flush();
											
											comp.flush();
										}
										
									}
									if(offset==2)
									{
										if((pos1.equals("JJ"))&&((pos2.equals("NN"))|(pos2.equals("NNS"))))
										{
											//isa
											//carac
											pw.println(s1+" "+s2+"|r_isa|"+s2);
											pw.println(s2+"|r_carac|"+s1);
											pw.flush();
										}
									}
										
								}
							}
						}
					}
				}
			}
			tx.success();			
		}
		pw.close();
		comp.close();
	}
	public void propagateOntologyRels()
	{
		/**
		 * grab annotations
		 */
		try(Transaction tx=this.graphDb.beginTx())
		{
			for(Relationship r:this.graphDb.getAllRelationships())
			{
				if(r.isType(JDMRelType.r_annotation))
				{
					/**
					 * get reified relationships
					 */
					Node reification=r.getStartNode();
					Iterator<Relationship> iter= reification.getRelationships().iterator();
					while(iter.hasNext())
					{
						Relationship rel=iter.next();
						if(rel.isType(JDMRelType.r_source))
						{
							Node source=rel.getEndNode();
							Iterable<Relationship> source_pos=source.getRelationships(Direction.OUTGOING, JDMRelType.r_pos);
							Iterator<Relationship> its=source_pos.iterator();
							Set<Node> sourcepos_set=new HashSet<Node>();
							while(its.hasNext())
							{
								Relationship rp=its.next();
								Node pos_node=rp.getEndNode();
								sourcepos_set.add(pos_node);
							}
							if(this.isCovered(source))
							{
								Set<Node> nset=this.getCoverSet(source);
								Node unode=null;
								for(Node n:nset)
								{
									Iterable<Relationship> cpos=n.getRelationships(Direction.OUTGOING, JDMRelType.r_pos);
									Iterator<Relationship> itc=cpos.iterator();
									Set<Node> pos_set=new HashSet<Node>();
									while(itc.hasNext())
									{
										Relationship rp=itc.next();
										Node pos_node=rp.getEndNode();
										pos_set.add(pos_node);
									}
									Set<Node> inter_pos=new HashSet<Node>();
									inter_pos.addAll(sourcepos_set);
									inter_pos.retainAll(pos_set);
									if(inter_pos.size()>0)
									{
										unode=n;
									}
								}
								if(unode!=null)
								{
									
								}
								
								/**
								 * compare pos of the covering nodes with the pos of the source node
								 */
								
								/**
								 * compare isa relationships of the covering node with the isa relationship 
								 */
								
								/**
								 * elevate the relationships
								 */
								
							}
						}
						else
						{
							if(rel.isType(JDMRelType.r_target))
							{
								/**
								 * même procédé
								 */
							}
						}
					}
				}
			}
			tx.success();
		}
		
		/**
		 * identify ontology terms (incoming relationships typed r_source or r_target)
		 * if covered, put into set 
		 */
		
	}
	
	public boolean isSemantic(Relationship r)
	{
		boolean iss=false;
		boolean g=this.isGrammatical(r);
		//boolean l=this.isLexical(r);
		boolean c=this.isCrosslingual(r);
		if(!(g|c))
		{
			iss=true;
		}
		return iss;
	}
	public boolean isGrammatical(Relationship r)
	{
		boolean isg=false;
		try (Transaction tx = this.graphDb.beginTx())
		{
			String rname=null;
			rname=r.getType().name();
			
			if((rname.equals("r_pos"))|(rname.equals("r_phrase")))
			{
				isg=true;
			}
			if((rname.equals("r_lemma")) && r.getStartNode().equals(r.getEndNode()))
			{
				isg=true;
			}
			tx.success();
		}
		return isg;
	}
	/**
	 * finding our whether the relationship is a cross-lingual relationship
	 * @param r
	 * @return
	 */
	public boolean isCrosslingual(Relationship r)
	{
		boolean isc=false;
		try (Transaction tx = this.graphDb.beginTx())
		{
			String rname=null;
			rname=r.getType().name();
			if(rname.equals("r_covers"))
			{
				isc=true;
			}
			tx.success();
		}
		return isc;
	}
	
	/**
	 * 
	

 * 
 * @param n
	 * @param posname constrained to Noun, Verb, Adj, Adv
	 * @return
	 */
	public boolean isSimilar(Node n1,Node n2,double threshold)
	{
		double sim=this.similarityScore(n1, n2, threshold);
		boolean isSim=false;
		if(sim>0.0)
		{
			isSim=true;
		}
		return isSim;
	}
	////////*** END BOOLEAN FLAGS ***////////////
/**
 * @param Node n1
 * @param Node n2
 * returns 
 * 
 */
	public double similarityScore(Node n1,Node n2,double threshold)
	{
		boolean is_similar=false;
		double jaccard=0.0;
		double result=0.0;
		Set<Node> intersection = new HashSet<Node>();
		Set<Node> union=new HashSet<Node>();
		Set<Node> retain = new HashSet<Node>();
		Iterator<Relationship>n1_iter=n1.getRelationships(Direction.OUTGOING).iterator();
		while(n1_iter.hasNext())
		{
			Relationship r=n1_iter.next();
			if(this.isSemantic(r))
			{
				intersection.add(r.getEndNode());
			}
		}
		Iterator<Relationship>n1_cross=n1.getRelationships(Direction.INCOMING).iterator();
		while(n1_cross.hasNext())
		{
			Relationship r=n1_cross.next();
			if(this.isCrosslingual(r))
			{
				intersection.add(r.getStartNode());
			}
		}
		Iterator<Relationship>n2_iter=n2.getRelationships(Direction.OUTGOING).iterator();
		while(n2_iter.hasNext())
		{
			Relationship r=n2_iter.next();
			if(this.isSemantic(r))
			{
				retain.add(r.getEndNode());
			}
		}
		Iterator<Relationship>n2_cross=n2.getRelationships(Direction.INCOMING).iterator();
		while(n2_cross.hasNext())
		{
			Relationship r=n2_cross.next();
			if(this.isCrosslingual(r))
			{
				retain.add(r.getStartNode());
			}
		}
		System.out.println("intersection size "+intersection.size());
		System.out.println("retain size "+retain.size());
		union.addAll(intersection);
//		for(Node n:intersection)
//		{
////			System.out.println("intersection "+n.getProperty("name"));
//		}
//		for(Node n:retain)
//		{
//			System.out.println("retain "+n.getProperty("name"));
//		}
		union.addAll(retain);
		intersection.retainAll(retain);
//		System.out.println("intersection after retaining "+intersection.size());
		jaccard=(double)intersection.size()/union.size();
		System.out.println("Jaccard index "+n1.getProperty("name")+" "+n2.getProperty("name")+" "+jaccard);
		if (jaccard>=threshold)
		{
			is_similar=true;
			if(is_similar)
			{
				result=jaccard;
			}
			
		}
		
		return result;
	}
	
	public boolean checkPos(Node n,String posname)
	{
		boolean posok=false;
		if(n.hasRelationship(Direction.OUTGOING, JDMRelType.r_pos))
		{
			if(n.getProperty("name").toString().contains(posname.toLowerCase()))
			{
				posok=true;
			}
			else
			{
				Iterator<Relationship> iter=n.getRelationships(Direction.OUTGOING, JDMRelType.r_pos).iterator();
				while(iter.hasNext())
				{
					Relationship r=iter.next();
					Node pos=r.getEndNode();
					String name=String.valueOf(pos.getProperty("name"));
					if(name.compareToIgnoreCase(posname)>=1)
					{
						posok=true;
					}		
				}
			}
			
		}
		return posok;
	}
	public Set<Node> getPos(Node n)
	{
		Set<Node>set=new HashSet<Node>();
		if(n.hasRelationship(Direction.OUTGOING, JDMRelType.r_pos))
		{
			Iterator<Relationship> iter=n.getRelationships(Direction.OUTGOING, JDMRelType.r_pos).iterator();
			while(iter.hasNext())
			{
				Relationship r=iter.next();
				Node pos=r.getEndNode();
				set.add(pos);	
			}
		}
		return set;
	}
	/**
	 * handling a formatted string output 
	 * @param args
	 * @throws IOException
	 */
//	 @Override
//	    public String toString()
//	    {
//	        return String.format(
//	            "Explanation(t2={},zid={},t3={})",
//	            this.t2, this.zid, this.t3);
//	    }
	/**
	 * handling comparisons between objects
	 * @param args
	 * @throws IOException
	 * @throws NotFoundException 
	 * @throws EntityNotFoundException 
	 */
//	 @Override
//	    public boolean equals(Object other)
//	    {
//	        if (other == null)
//	        {
//	            return false;
//	        }
//	        if (other == this)
//	        {
//	            return true;
//	        }
//	        if (!(other instanceof Feature))
//	        {
//	            return false;
//	        }
//
//	        Feature otherFeature = (Feature) other;
//	        return this.node == otherFeature.node && this.type == otherFeature.type;
//	    }
	
	public static void main(String[] args) throws IOException, EntityNotFoundException, NotFoundException
	{
		GraphBuilder gb=new GraphBuilder();
		
//		Set<Node> cn=new HashSet<Node>();
//		try (Transaction tx = gb.graphDb.beginTx())
//		{
//			cn=gb.getCoveredNodes();
//			tx.success();
//		}
//		File[] rep=new File("C:/Users/clairet/workspace/jwi-master/target").listFiles();
//		for(File f:rep)
//		{
			try (Transaction tx = gb.graphDb.beginTx())
			{
				
//				gb.createRelationship("viande",RelType.r_isa,"produit initial","fr","fr");
				
//				gb.createRelationship("lieu",RelType.r_refinement,"lieu(poisson)","fr","fr");
//				gb.createRelationship("lieu(poisson)",RelType.r_glose,"poisson","fr","fr");
				
				/**
				 * acquisition of relevant domain nodes
				 */
			
	
//				Set<Node> domainset=new HashSet<Node>();
//				String[]domains={"cuisine","Cuisine","Gastronomie","pâtisserie","boulangerie","Art culinaire","art culinaire","alimentation",
//								"gastronomie","préparation culinaire","fruit", "dessert", 
//								"viande","mets","aliment","ingrédient de recette de cuisine",
//								"boisson","nutrition","aliment", "ingrédient","légume","plat"};
//				for(String d:domains)
//				{
//					Node dom=null;
//					dom=gb.findNodeByName(d.toString(), "entryID");
//					
//					if(dom!=null)
//					{
//						domainset.add(dom);
//					}
//				}
//				File input=new File("C:/Users/clairet/Documents/LISTS/jdmexport1704.csv");
//				BufferedReader br=new BufferedReader(new FileReader(input));
//				String line="";
//				PrintWriter pwr=new PrintWriter(new FileWriter(new File("refined.txt")));
//				PrintWriter uwr=new PrintWriter(new FileWriter(new File("unrefined.txt")));
//				while((line=br.readLine())!=null)
//				{
//					String[]l=line.split("\\,");
//					if(l[0].contains(">"))
//					{
//						/**
//						 * 1. domain test ( on jdm graph)
//						 */
//						Node n=gb.findNodeByName(l[0], "entryID");
//						if(n.hasRelationship(Direction.OUTGOING,RelType.r_domain))
//						{
//							Iterator<Relationship> domiter=n.getRelationships(Direction.OUTGOING,RelType.r_domain).iterator();
//							while(domiter.hasNext())
//							{
//								Relationship r=domiter.next();
//								Node d=r.getEndNode();
//								if(domainset.contains(d))
//								{
//									
//								}
//								
//							}
//						}
//					}
//					if(l[3].contains(">"))
//					{
//						
//					}
//					if(line.contains(">"))
//					{
//						Pattern dom=Pattern.compile("114839|");
//						pwr.println(line);
//						pwr.flush();
//					}
//					else
//					{
//						uwr.println(line);
//						uwr.flush();
//					}
//				}
//				pwr.close();
//				uwr.close();
//				br.close();
//				Node n=gb.findNodeByName("pain", "fr");
//				
//				
//				Iterator<Relationship> iter=n.getRelationships(Direction.INCOMING).iterator();
//				while(iter.hasNext())
//				{
//					Relationship r=iter.next();
////					System.out.println(r.getStartNode().getProperty("name")+"-"+r.getType().name()+"->"+r.getEndNode().getProperty("name"));
//					if(r.getOtherNode(n).getProperty("name").toString().contains("restaurant"))
//					{
//						if(r.getType().name().equals("r_isa"))
//						{
//							System.out.println(r.getStartNode().getProperty("name")+"-"+r.getType().name()+"->"+r.getEndNode().getProperty("name"));
//							gb.deleteRelationship(r);
//						}
//					}
////////				
//					if(r.getOtherNode(n).getProperty("name").toString().contains("chocolate(beverage)"))
//					{
//						if(r.getType().name().equals("r_covers"))
//						{
//							gb.deleteRelationship(r);
//						}
//					}
//				}
//				gb.createRelationship("minestrone", RelType.r_location, "Italie", "fr", "fr");
//				gb.createAnnotatedRel("minestrone", "Italie", RelType.r_location, "int:country/", "fr", "int");
//				gb.createRelationship("minestrone", RelType.r_has_part, "pasta", "fr", "fr");
//				gb.createRelationship("minestrone", RelType.r_has_part, "pâtes", "fr", "fr");
//				gb.createRelationship("minestrone", RelType.r_carac, "épais", "fr", "fr");
//				gb.createRelationship("minestrone", RelType.r_has_part, "riz", "fr", "fr");
//				gb.createAnnotatedRel("minestrone", "légume", RelType.r_has_part, "int:raw product/", "fr", "int");
//				gb.createAnnotatedRel("minestrone", "basilic", RelType.r_has_part, "int:aroma/", "fr", "int");
//				gb.createAnnotatedRel("minestrone", "parmesan", RelType.r_has_part, "int:aroma/", "fr", "int");
//				gb.createRelationship("minestrone", RelType.r_has_part, "céleri branches", "fr", "fr");
//				gb.createRelationship("minestrone", RelType.r_has_part, "tomate", "fr", "fr");
//				gb.createRelationship("minestrone", RelType.r_has_part, "oignon", "fr", "fr");
//				gb.createRelationship("minestrone", RelType.r_has_part, "haricots blancs", "fr", "fr");
//				gb.createRelationship("minestrone", RelType.r_has_part, "lard", "fr", "fr");
//				gb.createRelationship("minestrone", RelType.r_has_part, "basilic", "fr", "fr");
//				gb.createRelationship("minestrone", RelType.r_matter, "huile d'olive", "fr", "fr");
//				
//				gb.createAnnotatedRel("potée", "légume", RelType.r_has_part, "int:raw product/", "fr", "int");
//				gb.createRelationship("potée", RelType.r_matter, "viande", "fr", "fr");
//				gb.createRelationship("potée", RelType.r_has_part, "charcuterie", "fr", "fr");
//				gb.createRelationship("potée", RelType.r_matter, "huile", "fr", "fr");
//				gb.createRelationship("int:income/", RelType.r_covers, "recette/dividende", "int", "fr");
//				
//				gb.createRelationship("recette", RelType.r_refinement, "recette/dividende", "fr", "fr");
//				
//				for(Node n:gb.graphDb.getAllNodes())
//				{
//					if(n.hasLabel(Label.label("ru")))
//					{
//						String name=String.valueOf(n.getProperty("name"));
//						if(name.endsWith(" суп"))
//						{
//							System.out.println(name);
//						}
//					}
//				}
//				for(Relationship r:gb.graphDb.getAllRelationships())
//				{
//					if(r.getType().name().equals("r_causes"))
//					{
//						System.out.println(r.getStartNode().getProperty("name")+"-"+r.getType().name()+"->"+r.getEndNode().getProperty("name"));
//					}
//				}
				
				
				Node n=gb.findNodeByName("découpe[r_agentive_implication]couper", "fr");
				
				
				Iterator<Relationship> iter=n.getRelationships().iterator();
				while(iter.hasNext())
				{
					Relationship r=iter.next();
//					if(r.getType().name().equals("r_hypo"))
//					{
						System.out.println(r.getStartNode().getProperty("name")+"-"+r.getType().name()+"->"+r.getEndNode().getProperty("name"));
//					}
				}
//				gb.createRelationship("dessert", RelType.r_has_part, "cuisine", "fr", "fr");
//				gb.createRelationship("pays", RelType.r_related, "cuisine", "fr", "fr");
//				gb.createRelationship("Russia", RelType.r_isa, "country", "en", "en");
//				gb.createRelationship("Spain", RelType.r_isa, "country", "en", "en");
//				gb.createRelationship("Italy", RelType.r_isa, "country", "en", "en");
//				gb.createRelationship("Bulgaria", RelType.r_isa, "country", "en", "en");
//				gb.createRelationship("Japan", RelType.r_isa, "country", "en", "en");
//				gb.createRelationship("Ireland", RelType.r_isa, "country", "en", "en");
//				gb.createRelationship("England", RelType.r_isa, "country", "en", "en");	
//				gb.createRelationship("USA", RelType.r_isa, "country", "en", "en");
//				gb.createRelationship("India", RelType.r_isa, "country", "en", "en");
//				gb.createRelationship("bortsch", RelType.r_location, "Russia", "en", "en");
//				gb.createRelationship("bortsch", RelType.r_has_part, "beetroot", "en", "en");
//				gb.createRelationship("bortsch", RelType.r_has_part, "vegetable", "en", "en");
//				gb.createRelationship("bortsch", RelType.r_has_part, "meat", "en", "en");
//				gb.createRelationship("bortsch", RelType.r_matter, "oil", "en", "en");
//				gb.createRelationship("bortsch", RelType.r_carac, "hot", "en", "en");
//				gb.createRelationship("bortsch", RelType.r_carac, "liquid", "en", "en");
//				gb.createRelationship("pepper pot", RelType.r_location, "Jamaica", "en", "en");
//				gb.createRelationship("pepper pot", RelType.r_location, "Philadelphia", "en", "en");
//				gb.createRelationship("pepper pot", RelType.r_location, "USA", "en", "en");
//				gb.createRelationship("pepper pot", RelType.r_has_part, "pepper", "en", "en");
//				gb.createRelationship("pepper pot", RelType.r_has_part, "bell pepper", "en", "en");
//				gb.createRelationship("pepper pot", RelType.r_has_part, "meat", "en", "en");
//				gb.createRelationship("mulligatawny", RelType.r_location, "India", "en", "en");
//				gb.createRelationship("mulligatawny", RelType.r_carac, "hot", "en", "en");
//				gb.createRelationship("mulligatawny", RelType.r_carac, "spicy", "en", "en");
//				gb.createRelationship("cocky-leeky", RelType.r_location, "Scotland", "en", "en");
//				gb.createRelationship("cocky-leeky", RelType.r_location, "England", "en", "en");
//				gb.createRelationship("cocky-leeky", RelType.r_isa, "soup", "en", "en");
//				gb.createRelationship("petite marmite", RelType.r_location, "France", "en", "en");
//				gb.createRelationship("petite marmite", RelType.r_isa, "soup", "en", "en");
//				gb.createRelationship("alphabet soup", RelType.r_location, "country", "en", "en");
//				gb.createRelationship("solyanka", RelType.r_location, "Russia", "en", "en");
//				gb.createRelationship("solyanka", RelType.r_isa, "soup", "en", "en");
//				gb.createRelationship("shchi", RelType.r_location, "Russia", "en", "en");
//				gb.createRelationship("shchi", RelType.r_isa, "soup", "en", "en");
//				
////				gb.createRelationship("int:chocolate/", RelType.r_refinement, "int:chocolate/color/", "int", "int");
//				gb.createRelationship("int:chocolate/color/", RelType.r_glose, "int:color/", "int", "int");
//				gb.createRelationship("int:round/", RelType.r_covers, "round shaped", "int", "en");
//				gb.createRelationship("int:shaped/", RelType.r_isa, "shaped", "int", "en");
//				
//				gb.createRelationship("int:square/", RelType.r_covers, "carré", "int", "fr");
//				gb.createRelationship("int:square/", RelType.r_covers, "square", "int", "en");
//				gb.createRelationship("int:square/", RelType.r_covers, "square shaped", "int", "en");
				
//				gb.createRelationship("int:flat/adj/", RelType.r_covers, "plat", "int", "fr");
//				gb.createRelationship("int:flat/adj/", RelType.r_covers, "flat", "int", "en");
//				gb.createRelationship("int:flat/adj/", RelType.r_isa, "int:shaped/", "int", "int");
//				gb.createRelationship("int:flatened/", RelType.r_covers, "aplati", "int", "fr");
//				gb.createRelationship("int:flatened/", RelType.r_covers, "flatened", "int", "en");
//				
//				gb.createRelationship("int:triangular/", RelType.r_covers, "triangulaire", "int", "fr");
//				gb.createRelationship("int:triangular/", RelType.r_covers, "triangular", "int", "en");
//				gb.createRelationship("int:triangular/", RelType.r_isa, "int:shaped/", "int", "int");
//				
//				gb.createAnnotatedRel("pain", "rond",RelType.r_carac, "int:shaped/", "fr", "int");
//				gb.createAnnotatedRel("pain", "plat",RelType.r_carac, "int:shaped/", "fr", "int");
//				gb.createAnnotatedRel("pain", "en boule",RelType.r_carac, "int:shaped/", "fr", "int");
//				gb.createAnnotatedRel("pain", "aplati",RelType.r_carac, "int:shaped/", "fr", "int");
//				gb.createAnnotatedRel("pain", "triangulaire",RelType.r_carac, "int:shaped/", "fr", "int");
//				gb.createAnnotatedRel("pain", "en losange",RelType.r_carac, "int:shaped/", "fr", "int");
			
//				gb.createRelationship("recette/dividende/", RelType.r_syn, "dividende", "fr", "fr");
//				
				
//				gb.createRelationship("int:viscosity/", RelType.r_isa, "int:consistency/", "int", "int");
//				
//				gb.createRelationship("int:light/low fat/", RelType.r_covers, "allégé/nutrition/", "int", "fr");
//				gb.createRelationship("int:light/low fat/", RelType.r_covers, "light/low fat/", "int", "en");
//				gb.createRelationship("int:light/", RelType.r_refinement, "int:light/low fat/", "int", "int");
//				gb.createRelationship("allégé", RelType.r_refinement, "allégé/nutrition/", "fr", "fr");
//				gb.createRelationship("light", RelType.r_refinement, "light/low fat/", "en", "en");
//				gb.createRelationship("int:surface/", RelType.r_carac, "int:bright/shiny/", "int", "int");
//				gb.createRelationship("int:state/", RelType.r_carac, "int:processed/", "int", "int");
//				gb.createRelationship("int:state/", RelType.r_carac, "int:fresh/", "int", "int");
//				gb.createRelationship("int:state/", RelType.r_carac, "int:liquid/", "int", "int");
//				gb.createRelationship("int:state/", RelType.r_carac, "int:light/low fat/", "int", "int");
//				gb.createRelationship("int:state/", RelType.r_carac, "int:dry/adj/", "int", "int");
//				gb.createRelationship("int:consistence/", RelType.r_carac, "int:oily/", "int", "int");
//				gb.createRelationship("int:consistence/", RelType.r_carac, "int:gelatinous/", "int", "int");
				
//				gb.createRelationship("int:dry/verb/", RelType.r_covers, "sécher", "int", "fr");
//				gb.createRelationship("int:dry/verb/", RelType.r_covers, "faire sécher", "int", "fr");
//				
//				gb.createRelationship("int:dry/", RelType.r_refinement, "int:dry/adj/", "int", "int");
//				gb.createRelationship("int:dry/", RelType.r_refinement, "int:dry/verb/", "int", "int");
				
//				gb.createRelationship("int:bright/sparkling/", RelType.r_covers, "éclatant", "int", "fr");
//				gb.createRelationship("int:bright/intelligent/", RelType.r_covers, "brillant/intelligent/", "int", "fr");
//				gb.createRelationship("int:bright/shiny/", RelType.r_covers, "brillant/surface/", "int", "fr");
//				gb.createRelationship("int:bright/luminous/", RelType.r_covers, "lumineux", "int", "fr");
//				gb.createRelationship("int:bright/cheerful/", RelType.r_covers, "resplendissant", "int", "fr");
//				gb.createRelationship("int:bright/sparkling/", RelType.r_covers, "éclatant", "int", "fr");
//				
//				gb.createRelationship("int:bright/sparkling/", RelType.r_covers, "bright/sparkling/", "int", "en");
//				gb.createRelationship("int:bright/intelligent/", RelType.r_covers, "very intelligent", "int", "en");
//				gb.createRelationship("int:bright/sparkling/", RelType.r_covers, "shiny/surface/", "int", "en");
//				gb.createRelationship("int:bright/luminous/", RelType.r_covers, "bright/luminous/", "int", "en");
//				gb.createRelationship("int:bright/cheerful/", RelType.r_covers, "bright/cheerful/", "int", "en");
//				
//				
//				gb.createRelationship("int:bright/intelligent/", RelType.r_covers, "listo", "int", "es");
//				gb.createRelationship("int:bright/sparkling/", RelType.r_covers, "brillante/superficie", "int", "es");
//				gb.createRelationship("int:bright/luminous/", RelType.r_covers, "luminoso", "int", "es");
//				gb.createRelationship("int:bright/cheerful/", RelType.r_covers, "vivaracho", "int", "es");
//				
//				gb.createRelationship("int:bright/", RelType.r_refinement, "int:bright/intelligent/", "int", "int");
//				gb.createRelationship("int:bright/", RelType.r_refinement, "int:bright/sparkling/", "int", "int");
//				gb.createRelationship("int:bright/", RelType.r_refinement, "int:bright/luminous/", "int", "int");
//				gb.createRelationship("int:bright/", RelType.r_refinement, "int:bright/shiny/", "int", "int");
//				gb.createRelationship("int:bright/", RelType.r_refinement, "int:bright/cheerful/", "int", "int");
////				gb.createRelationship("int:incandescent/angry/", RelType.r_covers, "incandescent/angry/", "int", "en");
////				gb.createRelationship("int:incandescent/shining brightly/", RelType.r_covers, "incandescent/shining brightly/", "int", "en");
////				gb.createRelationship("int:incandescent/angry/", RelType.r_covers, "furieux", "int", "fr");
////				gb.createRelationship("int:incandescent/shining brightly/", RelType.r_covers, "incandescent", "int", "fr");
////				gb.createRelationship("int:incandescent/angry/", RelType.r_pos, "int:Adj/", "int", "int");
////				gb.createRelationship("int:incandescent/shining brightly/", RelType.r_pos, "int:Adj/", "int", "int");
////				gb.createRelationship("int:crushed/grind/", RelType.r_covers, "concassé", "int", "fr");
////				
//				Node n2=gb.findNodeByName("int:dry/", "int");
//				Iterator<Relationship> iter2=n2.getRelationships().iterator();
//				while(iter2.hasNext())
//				{
//					Relationship r=iter2.next();
//					System.out.println(r.getStartNode().getProperty("name")+"-"+r.getType().name()+"->"+r.getEndNode().getProperty("name"));
////					if(String.valueOf(r.getStartNode().getProperty("name")).equals("int:bright/"))
//					if(r.getType().name().equals("r_covers"))
//					{
//						System.out.println("deleting relationship ");
//						gb.deleteRelationship(r);
//					}
//					
//				}
//				
				
//				PrintWriter pw=new PrintWriter(new FileWriter(new File("output/uncovered_miam.txt")));
//				for(Node n:gb.graphDb.getAllNodes())
//				{
//					
//					if(n.hasLabel(Label.label("miam")))
//					{
//						if(!(gb.isCovered(n)))
//						{
//							pw.println(String.valueOf(n.getProperty("name")));
//						}
//					}
//				}
//				Node n=gb.findNodeByName("porc","fr");
//				if(n.hasRelationship())
//				{
//					//RelType.r_covers,RelType.r_refinement RelType.r_covers,RelType.r_refinement,RelType.r_hypo
//					Iterator<Relationship> iter=n.getRelationships(RelType.r_covers).iterator();
//					while(iter.hasNext())
//					{
//						Relationship r=iter.next();
//						System.out.println(r.getStartNode().getProperty("name")+"-"+r.getType().name()+"->"+r.getEndNode().getProperty("name"));
//					}
//				}
			
//				Node n=gb.findNodeByName("whore","en");
//				gb.deleteNodeWithRels(n);
//				gb.createRelationship("int:giblets/",RelType.r_covers, "потроха(птица)", "int", "ru");
//				gb.createRelationship("int:giblets/",RelType.r_covers, "abattis", "int", "fr");
//				gb.createRelationship("int:giblets/",RelType.r_covers, "giblets", "int", "en");
//				gb.createRelationship("int:offal/",RelType.r_covers, "abats", "int", "fr");
//				gb.createRelationship("int:offal/",RelType.r_covers, "offal", "int", "en");
//				gb.createRelationship("int:chicken/",RelType.r_refinement, "int:chicken/meat/", "int", "int");
//				gb.createRelationship("int:duck/",RelType.r_refinement, "int:duck/meat/", "int", "int");
//				gb.createRelationship("int:poultry/",RelType.r_refinement, "int:poultry/meat/", "int", "int");
//				gb.createRelationship("int:meat/",RelType.r_hypo, "int:poultry/meat/", "int", "int");
//				gb.createRelationship("int:poultry/meat/",RelType.r_isa, "int:meat/", "int", "int");
//				gb.createRelationship("int:poultry/meat/",RelType.r_hypo, "int:turkey/meat/", "int", "int");
//				gb.createRelationship("int:poultry/meat/",RelType.r_hypo, "int:chicken/meat/", "int", "int");
//				gb.createRelationship("int:poultry/meat/",RelType.r_hypo, "int:guinea fowl/meat/", "int", "int");
//				gb.createRelationship("int:poultry/meat/",RelType.r_hypo, "int:duck/meat/", "int", "int");
//				gb.createRelationship("int:guinea fowl/",RelType.r_refinement, "int:guinea fowl/meat/", "int", "int");
//				gb.createRelationship("int:guinea fowl/",RelType.r_covers, "pintade", "int", "fr");
//				gb.createRelationship("int:guinea fowl/",RelType.r_covers, "guinea fowl", "int", "en");
//				
////				gb.createRelationship("int:turkey/",RelType.r_covers, "volaille type dinde", "int", "fr");
////				gb.createRelationship("int:turkey/",RelType.r_covers, "volaille type dinde", "int", "fr");
				
//				gb.createRelationship("int:solid state/",RelType.r_covers, "твёрдое состояние", "int", "ru");
//				gb.createRelationship("état physique ou forme aliment",RelType.r_related, "forme aliment", "fr", "fr");
//				gb.createRelationship("état physique ou forme aliment",RelType.r_related, "état physique", "fr", "fr");
//				gb.createRelationship("int:state/",RelType.r_covers, "état produit", "int", "fr");
//				gb.createRelationship("int:physical state/",RelType.r_covers, "physical state", "int", "en");
////				gb.createRelationship("int:physical state/",RelType.r_covers, "estado fsico", "int", "es");
//				gb.createRelationship("int:physical state/",RelType.r_covers, "физическое состояние", "int", "ru");
//				gb.createRelationship("int:lamb/meat",RelType.r_covers, "ягнятина", "int", "ru");
////				gb.createRelationship("int:lamb/",RelType.r_refinement, "int:lamb/animal", "int", "int");
				/**
				
				
				
				
				*/
//				gb.createRelationship("zeste(concept aliment)",RelType.r_hypo, "zeste d'orange", "fr", "fr");
//				gb.createRelationship("zeste(concept aliment)",RelType.r_hypo, "zeste de bergamote", "fr", "fr");
//				gb.createRelationship("zeste(concept aliment)",RelType.r_hypo, "zeste de lime", "fr", "fr");
//				gb.createRelationship("zeste(concept aliment)",RelType.r_hypo, "zeste de mandarine", "fr", "fr");
//				gb.createRelationship("zeste(concept aliment)",RelType.r_hypo, "zeste de pamplemousse", "fr", "fr");
//				gb.createRelationship("int:lamb/meat",RelType.r_covers, "lamb/meat", "int", "en");
//				gb.createRelationship("int:lamb/",RelType.r_covers, "lamb/animal", "int", "en");
//				gb.createRelationship("lamb",RelType.r_refinement, "lamb/meat", "en", "en");
//				gb.createRelationship("lamb",RelType.r_refinement, "lamb/animal", "en", "en");
//
//				//				gb.createRelationship("porc",RelType.r_refinement, "porc/animal", "fr", "fr");
////				
////				gb.createRelationship("porc/viande",RelType.r_isa, "porc/produit initial", "fr", "fr");
////				gb.createRelationship("int:pork/noun/",RelType.r_covers, "porc/viande", "int", "fr");
////				gb.createRelationship("int:pork/",RelType.r_covers, "porc/viande", "int", "fr");
////				gb.createRelationship("int:pork/noun/",RelType.r_covers, "porc/viande", "int", "fr");
////				gb.createRelationship("int:pork/",RelType.r_covers, "porc/viande", "int", "fr");
////				gb.createRelationship("int:pig/noun/",RelType.r_covers, "porc/animal", "int", "fr");
////				gb.createRelationship("int:pig/noun/",RelType.r_covers, "porc(animal)", "int", "fr");
////				gb.createRelationship("int:pig/",RelType.r_covers, "porc/animal", "int", "fr");
////				gb.createRelationship("int:pig/",RelType.r_covers, "porc(animal)", "int", "fr");
//				
				//gb.BuildFromWN(f);
	//			for(Node n1:cn)
	//			{
	//				
	//				for(Node n2:cn)
	//				{
	//					String name1=String.valueOf(n1.getProperty("name"));
	//					String name2=String.valueOf(n2.getProperty("name"));
	//					Pattern p=Pattern.compile("([A-Z]+|[А-Я]+|\\d+)");
	//					Matcher m1=p.matcher(name1);
	//					Matcher m2=p.matcher(name2);
	//					if((!m1.find())&&(!m2.find()))
	//					{
	//						gb.CreateSupraNode(n1, n2);
	//					}
	//				}
	//			}
				
				tx.success();
				tx.close();
			}
//		}
	}
}
