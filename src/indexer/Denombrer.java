package indexer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import exceptions.LigneException;

/*
 *  Cette classe sert à dénombrer :
 *      Les codes CIP
 *      l'age
 *      le nombre de délivrance par date
 *      le sexe
 */
public class Denombrer {

	// trop long à écrire, fichier trop gros, préférer l'index
public void denombrer_numordo(String dossier_files, String fichier_sortie) throws IOException{
		
		File folder = new File(dossier_files);
		File[] listOfFiles = folder.listFiles();
		
		HashMap<String, Integer> valeurs = new HashMap<String, Integer>();
		
		for (int i = 0; i < listOfFiles.length; i++) {      
		     
			 String fichier = listOfFiles[i].getAbsolutePath();
			 System.out.println(fichier);
			 DPFiles indexer = new DPFiles(fichier);		
			 try {
				indexer.set_numOrdonnance();
				// pour chaque ordonnance : 				
				for (Entry<String, Ordonnance> entries : indexer.get_num_cip().entrySet()){
					String value = entries.getValue().get_OrdoDeli() + "\t" + entries.getValue().get_CIP().size();					
					if (valeurs.containsKey(value)){
						valeurs.put(value, valeurs.get(value) + 1);
					} else { // si on ne la connait pas, on additionne 1
						valeurs.put(value, 1);
					}
				}
				indexer.getLogger().message(indexer.getFichier() + " " + Global.VARIABLE_NUM_DELIVRANCE + " dénombré");
				} catch (Exception e){
					indexer.getLogger().message("ERREUR " + e.getMessage());
					e.printStackTrace();
				}
			if (Global.DEBUG) break;
		}
		System.out.println(valeurs.size() + " lignes à écrire");
		writer_hashmap(fichier_sortie, valeurs);
}

public void denombrer_dates(String dossier_files, String fichier_sortie) throws IOException{
	
	File folder = new File(dossier_files);
	File[] listOfFiles = folder.listFiles();
	
	HashMap<String, Integer> valeurs = new HashMap<String, Integer>();
	
	for (int i = 0; i < listOfFiles.length; i++) {      
	     
		 String fichier = listOfFiles[i].getAbsolutePath();
		 System.out.println(fichier);
		 DPFiles indexer = new DPFiles(fichier);		
		 try {
			
			indexer.set_numOrdonnance();
			
			// possible de faire long et d'avoir la date depuis 1/01/1970
			String value ; 
			// pour chaque ordonnance : 				
			for (Entry<String, Ordonnance> entries : indexer.get_num_cip().entrySet()){
				
				DateFormat df = new SimpleDateFormat(Global.FORMAT_DATE);
				value = df.format(entries.getValue().get_date_delivrance());
				
				if (valeurs.containsKey(value)){
					valeurs.put(value, valeurs.get(value) + 1);
				} else { // si on ne la connait pas, on additionne 1
					valeurs.put(value, 1);
				}
				
			}
			indexer.getLogger().message(indexer.getFichier() + " " + Global.VARIABLE_DATE_DELIVRANCE + " dénombré");
			} catch (Exception e){
				indexer.getLogger().message("ERREUR " + e.getMessage());
				e.printStackTrace();
			}
		if (Global.DEBUG) break;
	}
	System.out.println(valeurs.size() + " lignes à écrire");
	writer_hashmap(fichier_sortie, valeurs);
}

	
public void denombrer_ageSexe(String variable, String dossier_files, String fichier_sortie) throws IOException{
		
		File folder = new File(dossier_files);
		File[] listOfFiles = folder.listFiles();
		
		HashMap<Integer, Integer> valeurs = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < listOfFiles.length; i++) {      
		     
			 String fichier = listOfFiles[i].getAbsolutePath();
			 System.out.println(fichier);
			 DPFiles indexer = new DPFiles(fichier);		
			 try {
				indexer.set_numOrdonnance();
				// pour chaque ordonnance : 				
				for (Entry<String, Ordonnance> entries : indexer.get_num_cip().entrySet()){
					int value = 0 ;
					if (variable.equals(Global.VARIABLE_AGE)){
						value = entries.getValue().get_age();
					} 
					
					else if (variable.equals(Global.VARIABLE_SEXE)){
					//A implémenter
						//value = entries.getValue().get_Sexe();
					} 
					
					else if (variable.equals("Nmedocs")){
						value = entries.getValue().get_CIP().size();
					} 
						
					else {
						throw new LigneException("Variable " + variable + " doit correspondre à " + 
					Global.VARIABLE_AGE + " ou " + Global.VARIABLE_SEXE + " ou " + "Nmedocs");
					}
					
					if (valeurs.containsKey(value)){
						valeurs.put(value, valeurs.get(value) + 1);
					} else { // si on ne la connait pas, on additionne 1
						valeurs.put(value, 1);
					}
				}
				indexer.getLogger().message(indexer.getFichier() + " " + variable + " dénombré");
				} catch (Exception e){
					indexer.getLogger().message("ERREUR " + e.getMessage());
					e.printStackTrace();
				} finally{
					indexer.getLogger().close();
				}
			if (Global.DEBUG) break;
		} 
		writer_hashmap_age(fichier_sortie, valeurs);
}
	
public void denombre_folder(String variable, String dossier_files, String fichier_sortie) throws Exception{
		File folder = new File(dossier_files);
		File[] listOfFiles = folder.listFiles();
		
		HashMap<String, Integer> valeurs = new HashMap<String, Integer>();
		
		int counter  = 0;
		
		for (int i = 0; i < listOfFiles.length; i++) {      
		     counter++;
			 String fichier = listOfFiles[i].getAbsolutePath();
			 DPFiles indexer = new DPFiles(fichier);
			 indexer.getLogger().message(indexer.getFichier() + " en cours de dénombrement");
			 try {
				valeurs = indexer.get_unique(variable, valeurs);
				indexer.getLogger().message(indexer.getFichier() + " dénombré");
				} catch (Exception e){
					indexer.getLogger().message("ERREUR " + e.getMessage());
					e.printStackTrace();
				}
			if (Global.DEBUG & counter == 2) break;
	}
		writer_hashmap(fichier_sortie, valeurs);
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String dossier = "data";
		Denombrer denombrer = new Denombrer();
		
		Boolean denombrement_CIP = false;
		Boolean denombrement_age = true;
		Boolean denombrement_sexe = false;
		Boolean denombrement_numordo = false;
		Boolean denombrer_date = false;
		
		if (denombrement_CIP){
			String fichier_sortie = "output/liste_CIP.csv";
			denombrer.denombre_folder(Global.VARIABLE_CIP,dossier, fichier_sortie);
		}
		
		if (denombrement_age){
			String fichier_sortie = "output/liste_age.csv";
			denombrer.denombrer_ageSexe(Global.VARIABLE_AGE,dossier, fichier_sortie);
		}
		
		if (denombrement_sexe){
			String fichier_sortie = "output/liste_sexe.csv";

			denombrer.denombrer_ageSexe(Global.VARIABLE_SEXE,dossier, fichier_sortie);
		}
		
		if (denombrement_numordo){
			String fichier_sortie = "output/liste_ordo.csv";
			denombrer.denombrer_ageSexe("Nmedocs",dossier, fichier_sortie);
		}
		
		if (denombrer_date){
			String fichier_sortie = "output/liste_dates.csv";
			denombrer.denombrer_dates(dossier, fichier_sortie);
		}
	}
	
	
	public static void writer_hashmap(String fichier_sortie,HashMap<String, Integer> valeurs) throws IOException{
		
		List<String> sorties = new ArrayList<String>();
		Path out = Paths.get(fichier_sortie);
		
		for (Entry<String, Integer> entries : valeurs.entrySet()){
			sorties.add(entries.getKey() + "\t" + entries.getValue()) ;
		}
		
		Files.write(out,sorties,Charset.defaultCharset());
		System.out.println(fichier_sortie + " créé");
	}
	
	public static void writer_hashmap_age(String fichier_sortie,HashMap<Integer, Integer> valeurs) throws IOException{
		
		List<String> sorties = new ArrayList<String>();
		Path out = Paths.get(fichier_sortie);
		
		for (Entry<Integer, Integer> entries : valeurs.entrySet()){
			sorties.add(entries.getKey() + "\t" + entries.getValue()) ;
		}
		
		Files.write(out,sorties,Charset.defaultCharset());
		System.out.println(fichier_sortie + " créé");
	}

}
