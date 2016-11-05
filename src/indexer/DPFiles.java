package indexer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import exceptions.LigneException;
import logger.MonLogger;


/*
 * Classe permettant de créer des "ordonnances" à partir du fichier CSV qui seront indexées  
 */
public class DPFiles {
	// Nom du fichier à indexer
	private String fichier;
	// variables qui seront indexées
	private String colonnes[] = {Global.VARIABLE_AGE, Global.VARIABLE_CIP, Global.VARIABLE_DATE_DELIVRANCE,
			Global.VARIABLE_NUM_DELIVRANCE};
	// localisation des colonnes dans le fichier (sera déterminée par une fonction)
	private HashMap<String,Integer> map_colonne = new HashMap<String,Integer>();
	
	// separateur fichier
	private String colonne_separateur = Global.SEPARATEUR;
	
	// Reader du fichier
	private BufferedReader br = null;
	
	private MonLogger logger = new MonLogger();
	
	private ArrayList<String> CIPinclus = new ArrayList<String>();
	
	// stock toutes les ordonnances par num d'ordonnance
	private HashMap<String,Ordonnance> numOrdonnance;
	
	public HashMap<String,Ordonnance> get_num_cip(){
		return numOrdonnance;
	}
	
	
	// traite un seul fichier
	public DPFiles(String fichier) throws IOException{

        logger.message("Début du traitement du fichier" + fichier); 
		// Fichier à indexer
		this.fichier = fichier;
        br = new BufferedReader(new InputStreamReader(new FileInputStream(fichier),Charset.forName("ISO8859-1")));
        
		String premiere_ligne = br.readLine();
		if (Global.DEBUG) System.out.println("header : " + premiere_ligne);
		try {
			set_map_colonne(premiere_ligne);
		} catch (Exception e){
			logger.message("ERREUR " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public DPFiles(String fichier, String CIPinclusFichier) throws IOException{
		// appelle le constructeur ci-dessous
		this(fichier);
		BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(CIPinclusFichier),Charset.forName("UTF8")));
		String lines;
		while ((lines = br2.readLine()) != null) {
			CIPinclus.add(lines);
		}
		br2.close();
	}
	
	// set_numOrdonnance : fait le lien entre un numéro d'ordonnance  (num) et l'ordonnance
	public void set_numOrdonnance() throws IOException, LigneException, ParseException{
		// stock toutes les ordonnances par num d'ordonnance
		numOrdonnance = new HashMap<String,Ordonnance>();		
		
		int colonne_ordo = map_colonne.get(Global.VARIABLE_NUM_DELIVRANCE).intValue();
		int colonne_cip = map_colonne.get(Global.VARIABLE_CIP).intValue();
		
		int counter=0;
		String lines;
		while ((lines = br.readLine()) != null) {
			String line[] = lines.split(colonne_separateur);
			counter++;
			if (line.length != Global.N_COLONNE){
				throw new LigneException("Longueur de la colonne header différent de " + Global.N_COLONNE + "à la ligne " 
			+ counter + "dans le fichier " + getFichier());
			}
			
			// Si connait pas l'ordonnance alors on l'ajoute
			if (!numOrdonnance.containsKey(line[colonne_ordo])){
				numOrdonnance.put(line[colonne_ordo], new Ordonnance(getFichier(),
						line[map_colonne.get(Global.VARIABLE_NUM_DELIVRANCE)],
						line[map_colonne.get(Global.VARIABLE_DATE_DELIVRANCE)],
						line[map_colonne.get(Global.VARIABLE_AGE)]
						));
			}
			// ajout du code CIP
			String cip = line[colonne_cip];
			if (checkCIP(cip)) numOrdonnance.get(line[colonne_ordo]).add_CIP(cip);
			
			if (Global.DEBUG & counter == 50000) {
				System.out.println("Mode DEBUG - arret à la ligne " + counter + " du fichier " + getFichier());
				break;
			}
			
		}// fin boucle par ligne
		
		logger.message(getFichier() + " : " + counter + " lignes traitées");
		logger.message(numOrdonnance.size() + " numéros ordo différents ");
	}
	
	private boolean checkCIP(String cip){
		if (CIPinclus.contains(cip))return(true);
		return(false);
	}
	
	
	// Trouve les colonnes du fichier
	private void set_map_colonne(String premiere_ligne) throws LigneException{
		String header[] = premiere_ligne.split(colonne_separateur);
		if (header.length != Global.N_COLONNE){
			throw new LigneException("Longueur de la colonne header différent de " + Global.N_COLONNE + "dans le fichier " + getFichier());
		}
		
		// quel est le numéro des colonnes
		for (int i = 0; i<colonnes.length; i++){
			for (int y = 0; y<header.length ; y++){
				if (colonnes[i].equals(header[y])){
					map_colonne.put(colonnes[i], y);
				}
			}
		}
		
		// check si toutes les colonnes ont été trouvés
		for (String c : colonnes){
			if (!map_colonne.containsKey(c)){
				throw new LigneException("Colonne "+ c + " non trouvé dans le fichier" + getFichier());
			}
		}
		
	}
	
	
	// Dénombrer le nombre unique de valeurs :
	public HashMap<String, Integer> get_unique(String nom_variable, HashMap<String, Integer> valeurs) throws LigneException, IOException{
		// la variable doit être connue : 
		Boolean connu = false;
		for (String variable : colonnes){
			if (nom_variable.equals(variable)) connu = true;
		}
		if (connu == false){
			throw new LigneException(nom_variable + " non trouvé dans le fichier" + getFichier());
		}
		
		// la variable est connue, sa colonne est :
		int colonne_variable = map_colonne.get(nom_variable).intValue();
		
		//System.out.println(colonne_variable);
		
		// pour chaque ligne, on enregistre sa valeur
		String lines;
		//List<String> valeurs = new ArrayList<String>();
		int counter = 0;
		while ((lines = br.readLine()) != null) {
			counter++;
			String line[] = lines.split(colonne_separateur);
			String valeur = line[colonne_variable];
			// si la valeur est connue, on ajoute une occurence (+1)
			if (valeurs.containsKey(valeur)){
				valeurs.put(valeur, valeurs.get(valeur) + 1);
			} else { // si on ne la connait pas, on additionne 1
				valeurs.put(valeur, 1);
			}
			
			if (Global.DEBUG)System.out.println(counter);
		}
		
		return (valeurs);
		
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String fichier = "data/DP_20130819.csv";
		String CIPinclusfichier="interaction/5112016/CIP7inclus.txt";
		DPFiles indexer = new DPFiles(fichier,CIPinclusfichier);		
		try {
			indexer.set_numOrdonnance();
		} catch (Exception e){
			indexer.getLogger().message("ERREUR " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	public MonLogger getLogger() {
		return logger;
	}

	public String getFichier() {
		return fichier;
	}
}


