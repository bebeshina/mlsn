package builder;
import java.lang.reflect.Field;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;


public enum JDMRelType implements RelationshipType,Relationship{


	/**
	 * Relation type enumeration.
	 */
		// fields ---------------------------------------------------------------


		r_associated (0, "r_associated"),
		r_raff_sem(1, "r_raff_sem"),
		r_raff_morpho(2, "r_raff_morpho"),
		r_domain (3, "r_domain"),
		r_pos (4, "r_pos"),
		r_syn (5, "r_syn"),
		r_isa (6, "r_isa"),
		r_anto (7, "r_anto"),
		r_hypo (8, "r_hypo"),
		r_has_part(9, "r_has_part"),
		r_holo(10,"r_holo"),
		r_locution(11,"r_locution"),
		r_agent(13,"r_agent"),
		r_patient(14,"r_patient"),
		r_lieu(15,"r_lieu"),
		r_instr(16,"r_instr"),
		r_carac (17, "r_carac"),
		r_data (18, "r_data"),	//Informations diverses (plutôt d'ordre lexicale
		r_lemma (19, "r_lemma"),
		r_has_magn (20, "r_has_magn"),
		r_has_antimagn (21, "r_has_antimagn"),
		r_carac_1 (23, "r_carac-1"),
		r_agent_1(24,"r_agent-1"),
		r_instr_1(25,"r_instr-1"),
		r_patient_1(26,"r_patient-1"),
		r_domain_1 (27, "r_domain-1"),
		r_lieu_1(28,"r_lieu-1"),
		r_lieu_action(28,"r_lieu_action"),
		r_action_lieu(31,"r_action_lieu"),
		r_manner (34, "r_manner"),
		r_telic_role(37,"r_telic_role"),
		r_conseq(41,"r_conseq"),
		r_causatif(42,"r_causatif"),
		r_object_mater(50,"r_object>mater"),
		r_mater_object(51,"r_mater>object"),
		r_successeur_time(52,"r_successeur-time"),
		r_implication(57,"r_implication"),
		r_quantificateur(58,"r_quantificateur"),
		r_manner_1(62,"r_manner-1"),
		r_agentive_implication(63,"r_agentive_implication"),
		r_similar(67,"r_similar"),
		r_accomp(75,"r_accomp"),
		r_verb_ppas(77,"r_verb_ppas"),
		r_cohypo(78,"r_cohypo"),
		r_verb_ppre(79,"r_verb_ppre"),	
		r_color(106,"r_color"),
		r_predecesseur_time(109,"r_predecesseur-time"),
		r_foncteur(117,"r_foncteur"),
		r_but(119,"r_but"),
		r_beneficiaire(150,"r_beneficiaire"),
		r_covers(555,"r_covers"),
		r_predicate(666,"r_predicate"),
		r_head(777,"r_head"),
		r_annotation(998,"r_annotation"),
		r_source(888,"r_source"),
		r_target(898,"r_target");
	
		
		// object members -------------------------------------------------------
		
		/**
		 * Identifier.
		 */
		private int id;
		
		/**
		 * Type name.
		 */
		private String typeName;
		/**
		 * type of the relationship, nature of the corresponding lexical function
		 */
		private String constraint;

		// object methods -------------------------------------------------------
		
		RelationshipType getType(Relationship r) 
		{
			return  r.getType();
			
		}
		/**
		 * Constructs a new <code>RelationType</code> with specified id and name.
		 * 
		 * @param id the identifier
		 * @param typeName the type name
		 */
		JDMRelType(int id, String typeName)
		{
			this.id       = id;
			this.typeName = typeName; 
			
		}
		public boolean hasName(String name)
		{
			boolean ok=false;
			for(Field f:JDMRelType.class.getDeclaredFields())
			{
				System.out.println(f);
				if (f.toString().equals(name))
				{
					ok=true;
				}	
			}
			return ok;
		}
		/**
		 * @return the identifier
		 */
		public int id()
		{
			return this.id;
		}
		
		/**
		 * @return the name
		 */
		public String typeName()
		{
			return this.typeName;
		}
		public String constraintType()
		{
			return this.constraint;
		}
		@Override
		public GraphDatabaseService getGraphDatabase() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public boolean hasProperty(String key) {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public Object getProperty(String key) {
			// TODO Auto-generated method stub
			return null;
		}
	
		
		@Override
		public Object removeProperty(String key) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public Iterable<String> getPropertyKeys() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public Map<String, java.lang.Object> getProperties(String... keys) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public Map<String, java.lang.Object> getAllProperties() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public long getId() {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public void delete() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public Node getStartNode() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public Node getEndNode() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public Node getOtherNode(Node node) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public Node[] getNodes() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public RelationshipType getType() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public boolean isType(RelationshipType type) {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public java.lang.Object getProperty(String arg0, java.lang.Object arg1) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public void setProperty(String arg0, java.lang.Object arg1) {
			// TODO Auto-generated method stub
			
		}
		
}
