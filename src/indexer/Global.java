package indexer;

public class Global {
	public static Boolean DEBUG = false;
	
	/*
	 * Variables dans les fichiers :
	 *  Date;N° Disp;Type Prescrip (Disp.);DEP Code (Disp.);Age;Sexe (Disp.);CIP7;Qté Disp
	 */
	
	// colonnes des fichiers
	public static String VARIABLE_NUM_DELIVRANCE = "N° Disp";
	public static String VARIABLE_CIP = "CIP7";
	public static String VARIABLE_DATE_DELIVRANCE = "Date";
	public static String VARIABLE_AGE = "Age";
	public static String VARIABLE_SEXE="Sexe";
	
	// format de la date
	public static String FORMAT_DATE = "dd/MM/yyyy";
	
	// chemin de l'index 
	public static String INDEX_FOLDER ="index";
	
	// chemin des données
	public static String DATA_FOLDER="data";
	
	// Fichier pour enregistrer les logs
	public static String LOG_FILE = "log/log1.txt";
	
	// nombre de colonnes
	public static int N_COLONNE = 8;
	
	// séparateur des colonnes des fichiers
	public static String SEPARATEUR = ";";
}
