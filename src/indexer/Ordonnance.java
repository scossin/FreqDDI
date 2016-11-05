package indexer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.document.DateTools;

/*
 *  Une classe pour représenter les ordonnances
 */
public class Ordonnance {	
	private String fichier;
	private long OrdoDeli;
	private ArrayList<Long> CIP = new ArrayList<Long>() ;
	private Date date_delivrance;
	short age;
	
	
	public Ordonnance(String fichier, String OrdoDeli, String date_delivrance, String age) throws ParseException{
		this.fichier = fichier;
		this.OrdoDeli = Long.parseLong(OrdoDeli);
		
		///// Bug d'une fonction ultérieure conduit à mettre une heure (12h) à la date de délivrance
		// Lucene a besoin d'un String de date formatté pour enregistrer dans l'index
		// Pour cela il faut passer la date dans la fonction "DateTools.dateToString"
		// Mais cette fonction calcule l'heure GMT, donc retire 1 heure, donc change le jour !! (j-1)
		// en mettant une heure (12), on évite ce bug : (date 11h GMT)
		// Bug expliqué ici : http://www.gossamer-threads.com/lists/lucene/java-user/41229
		this.date_delivrance = new SimpleDateFormat (Global.FORMAT_DATE + " HH").parse(date_delivrance + " 12");
		this.age = (short) Integer.parseInt(age);
	}
	// getters :
	
	public String get_fichier(){
		return fichier;
	}
	
	public long get_OrdoDeli(){
		return OrdoDeli;
	}
	
	
	public short get_age(){
		return age;
	}
	
	public Date get_date_delivrance(){
		return date_delivrance;
	}
	
	public void add_CIP(String CIP){
		this.CIP.add(Long.parseLong(CIP));
	}
	
	public ArrayList<Long> get_CIP(){
		return CIP;
	}
	
	private String description(){
		String output = "Ordonnance num " + OrdoDeli + 
				"\n\t age = " + age + 
				"\n\t délivrance = " + date_delivrance.toString() + "\n\t CIP = ";
		for (long s : CIP){
			output = output.concat("\t" + s);
		}
		return(output);
	}
	
	public static void main(String[] args) throws ParseException {
		// test :
		Ordonnance ordonnance = new Ordonnance("fichier_test","190648163","19/08/2013","1");
		ordonnance.add_CIP("3400933316259");
		ordonnance.add_CIP("3400933316260");
		System.out.println(ordonnance.get_CIP().size());
		System.out.println(ordonnance.description());
	}
}
