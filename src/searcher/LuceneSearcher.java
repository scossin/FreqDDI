package searcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import indexer.Global;

public class LuceneSearcher {

	private IndexReader ireader ;
	private IndexSearcher isearcher;
	
	public LuceneSearcher (String dossier_index) throws IOException{
		Directory directory = FSDirectory.open(new File(dossier_index));
		ireader = DirectoryReader.open(directory);
		isearcher = new IndexSearcher(ireader);
		
		// Pour éviter une erreur :
    	BooleanQuery.setMaxClauseCount(50000);
	}
		
	public Query date_searcher(String date_min, String date_max) throws ParseException, IOException{
		Date date_delivrance_min = new SimpleDateFormat (Global.FORMAT_DATE + " HH").parse(date_min + " 12");
		Date date_delivrance_max = new SimpleDateFormat (Global.FORMAT_DATE+ " HH").parse(date_max+ " 12");
        String lowerDate = buildDate(date_delivrance_min);
        String upperDate = buildDate(date_delivrance_max);
        boolean includeLower = true;
        boolean includeUpper = true;
        TermRangeQuery query = new TermRangeQuery(Global.VARIABLE_DATE_DELIVRANCE,new BytesRef(lowerDate.getBytes()), 
        		new BytesRef(upperDate.getBytes()), includeLower, includeUpper);
        return (query);
	}
	
    // fonction pour transformer une date dans un String date conçu pour Lucene
	private String buildDate(Date date) {
	        return DateTools.dateToString(date, Resolution.DAY);
	}
	
    // Fonction permettant de renvoyer le résultat de la recherche
    private void print_ScoreDoc (ScoreDoc[] hits, int max_hit) throws IOException{
    	
    	if (hits.length == 0) {
    		System.out.println ("Aucun résultat");
	    }
    	// si N résultat inférieur à max_hit, on renvoie N résultats
	    int max_resultat = hits.length < max_hit ? hits.length : max_hit ;
	    String resultat = "";
	    for (int i = 0; i < max_resultat; i++) {
	       Document doc = isearcher.doc(hits[i].doc);
	       resultat = i + "\n\tdate" +doc.get(Global.VARIABLE_DATE_DELIVRANCE) + 
	    		   "\tAge : " + doc.get(Global.VARIABLE_AGE) + 
	    		   "\tSexe : " + doc.get(Global.VARIABLE_SEXE) + 
	    		   //"\tCIP : " + doc.get(Global.VARIABLE_CIP) + 
	    		   "\tordo : " + doc.get("id")  +
	    		   "\tNmedocs : " + doc.get("Nmedocs");
	       
	    	}
	    System.out.println("Nombre de résultat : " + hits.length);
    }
    
    public Query query_CIP (Long CIP) throws IOException{
    	//if (Global.DEBUG)System.out.println("DEBUG search_exact_term : " + terme);
    	//Query query = new TermQuery(new Term(champs,terme));
    	BytesRef bytes = new BytesRef();
    	NumericUtils.longToPrefixCoded(CIP, 0, bytes);
    	//TopDocs td = searcher.search(new TermQuery(new Term("id", bytes)), 10);
    	Query query = new TermQuery(new Term(Global.VARIABLE_CIP, bytes));
    	return (query); 
    }
    
    // une molécule correspond à plusieurs codes CIP
    public Query query_molecule (Long[] CIPs) throws IOException{
    	BooleanQuery bool = new BooleanQuery();
    	for (long cip : CIPs){
    		bool.add(query_CIP(cip), Occur.SHOULD);
    	}
    	return(bool);
    }
    
    /* ancienne version : ne gère pas l'intersection
    public Query query_2_molecules (Long[] CIPs1, Long[] CIPs2) throws IOException{
    	BooleanQuery bool = new BooleanQuery();
    	chevauchement(CIPs1, CIPs2);
    	bool.add(query_molecule(CIPs1), Occur.MUST);
    	bool.add(query_molecule(CIPs2), Occur.MUST);
    	return(bool);
    }*/
    
    private Query query_2_molecules(Long[] CIPs1, Long[] CIPs2) throws IOException{
		/*
		 *  gérer le cas où l'intersection entre CIPs1 et CIPs2 n'est pas nulle
		 *  ceci signifie que les deux molécules peuvent être combinées dans un CIP
		 */
    	ArrayList <Long> CIPcommuns = new ArrayList<Long>();
    	for (long cip1 : CIPs1){
    		for (long cip2 : CIPs2){
    			if (cip1 == cip2){
    				CIPcommuns.add(cip1);
    			}
    		}
    	}
    	
    	if (CIPcommuns.isEmpty()){
        	BooleanQuery bool = new BooleanQuery();
    		bool.add(query_molecule(CIPs1), Occur.MUST);
        	bool.add(query_molecule(CIPs2), Occur.MUST);
        	return(bool);
    	} else {
			System.out.println("DOUBLONS DETECTES");
    		// faut faire une requête badass
    		BooleanQuery bool = new BooleanQuery();
    		// en retirant dans les cips communs dans CIPs1 :
    		ArrayList <Long> CIP1 = new ArrayList<Long>(Arrays.asList(CIPs1));
    		CIP1.removeAll(CIPcommuns);    		
        	BooleanQuery boolCIP1 = new BooleanQuery();
        	boolCIP1.add(query_molecule(CIP1.toArray(new Long[CIP1.size()])), Occur.MUST);
        	boolCIP1.add(query_molecule(CIPs2), Occur.MUST);
        	
        	// en retirant dans les cips communs dans CIPs1 :
    		ArrayList <Long> CIP2 = new ArrayList<Long>(Arrays.asList(CIPs2));
    		CIP2.removeAll(CIPcommuns);    		
        	BooleanQuery boolCIP2 = new BooleanQuery();
        	boolCIP2.add(query_molecule(CIPs1), Occur.MUST);
        	boolCIP2.add(query_molecule(CIP2.toArray(new Long[CIP2.size()])), Occur.MUST);
        	
        	bool.add(boolCIP1, Occur.SHOULD);
        	bool.add(boolCIP2, Occur.SHOULD);
        	return (bool);
    	}
    }
    
    public Query query_2_molecules_date (Long[] CIPs1, Long[] CIPs2, String date_min, String date_max) throws IOException, ParseException{
    	BooleanQuery bool = new BooleanQuery();
    	bool.add(query_2_molecules(CIPs1,CIPs2), Occur.MUST);
    	bool.add(date_searcher(date_min,date_max), Occur.MUST);
    	return(bool);
    }
    
    public void search_query (Query query, int max_hit) throws IOException{
    	ScoreDoc[] hits = isearcher.search(query, null, max_hit).scoreDocs;
    	System.out.println(hits.length);
    	print_ScoreDoc(hits,max_hit);	
    }
    
    public int nHits (Query query, int max_hit) throws IOException{
    	ScoreDoc[] hits = isearcher.search(query, null, max_hit).scoreDocs;
    	return(hits.length);
    }
    
    public void write_result(Query query, String champs[], int max_hit, String fichier_sortie) throws IOException{
    	ScoreDoc[] hits = isearcher.search(query, null, max_hit).scoreDocs;
    	int max_resultat = hits.length < max_hit ? hits.length : max_hit ;
    	
	    for (int i = 0; i < max_resultat; i++) {
	    	String resultat = "";
	    	Document doc = isearcher.doc(hits[i].doc);
	    	for (String s : champs){
	    		resultat = resultat + doc.get(s) + "\t";
	    	}
	    	resultat = resultat + "\n";
	    	Files.write(Paths.get(fichier_sortie), resultat.getBytes(), StandardOpenOption.APPEND);
	    }
    	print_ScoreDoc(hits,max_hit);
    }
	
    public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		LuceneSearcher lucene = new LuceneSearcher(Global.INDEX_FOLDER);
		//lucene.print_ScoreDoc(lucene.date_searcher(),10);
		Long[] CIPs1 = {3400934744198L};
		Long[] CIPs2 = {3400935955838L};
		String date_min = "01/01/2000";
		String date_max= "30/03/2015";
		Query query = lucene.query_2_molecules_date(CIPs1, CIPs2, date_min, date_max);
		System.out.println(lucene.nHits(query, 10000000));
		//String[] champs = {Global.VARIABLE_NUM_DELIVRANCE, Global.VARIABLE_NIR};
		//lucene.write_result(query,champs,500,"test.txt" );
		// résultat : 327 - vérifié avec R
		
		//lucene.search_exact_term(3400936853812L,Global.VARIABLE_CIP, 10);
	}

	public ArrayList<String> detailsHits(Query query, String[] s, int max_hit) throws IOException {
		ArrayList<String> lignes = new ArrayList<String>();
		ScoreDoc[] hits = isearcher.search(query, null, max_hit).scoreDocs;
    	
		if (hits.length == 0) {
    		return null;
	    }
    	// si N résultat inférieur à max_hit, on renvoie N résultats
	    int max_resultat = hits.length < max_hit ? hits.length : max_hit ;
	    
	    for (int i = 0; i < max_resultat; i++) {
	       String resultat = "";
	       Document doc = isearcher.doc(hits[i].doc);
	       resultat = s[0] + "\t" 
	                 + s[1] + "\t" 
	                 // date min
	    		   //+ s[2] + "\t" 
	               // date max  
	    		   //+ s[3] + "\t" +
	                 // date de délivrance
	    		  //+  doc.get(Global.VARIABLE_DATE_DELIVRANCE) 
	               + "\t" + doc.get(Global.VARIABLE_AGE) + 
	               // numéro de la dispensation
	    		   "\t" + doc.get("id")  +
	    		   "\t" + doc.get("Nmedocs");
	       if (Global.DEBUG) System.out.println("detailsHits : " + resultat);
	       lignes.add(resultat);
	    }
		return lignes;
	}

}
