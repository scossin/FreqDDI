package searcher;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.lucene.search.Query;

import exceptions.LigneException;
import indexer.Global;
import interaction.Interaction;

/*
 * Classe permettant de dénombrer les interactions
 */
public class DenombreInteraction {

	private LuceneSearcher lucene ;
	private Interaction interaction ;
	private ArrayList<String> resultats = new ArrayList<String>();
	
	public ArrayList<String> denombre() throws IOException, ParseException{		
		// Un peu moche : interaction faire une classe comme ordonnance
		// checker si CIPs1 et CIPs2 se chevauchent
		// exporter les résultats
		
		for (String[] s : interaction.get_interaction()){
			Long[] CIPs1 = interaction.get_moleculesCIP().get_CIP(s[0]);
			Long[] CIPs2 = interaction.get_moleculesCIP().get_CIP(s[1]);
			String date_min = s[2];
			String date_max= s[3];
			Query query = lucene.query_2_molecules_date(CIPs1, CIPs2, date_min, date_max);
			int Ninteraction = lucene.nHits(query, 10000000);
			
			String resultat = String.join("\t", s);
			resultat = resultat.concat("\t" + Integer.toString(Ninteraction));
			System.out.println(resultat);
			resultats.add(resultat);
		}
		return(resultats);
	}
	
	public ArrayList<String> denombredetails(Path fichierSortie) throws IOException, ParseException{		
		// Un peu moche : interaction faire une classe comme ordonnance
		// checker si CIPs1 et CIPs2 se chevauchent
		// exporter les résultats
		resultats = new ArrayList<String>();
		//header : 
		resultats.add("mol1\tmol2"+
				"\t" + Global.VARIABLE_AGE +
				"\t" + Global.VARIABLE_NUM_DELIVRANCE + 
				"\t" + "Nmedocs");
		
		int counter=0;
		int totalcounter=0;
		for (String[] s : interaction.get_interaction()){
			ArrayList<String> lignes = new ArrayList<String>();
			
			Long[] CIPs1 = interaction.get_moleculesCIP().get_CIP(s[0]);
			Long[] CIPs2 = interaction.get_moleculesCIP().get_CIP(s[1]);
			
			String date_min = s[2];
			String date_max= s[3];
			Query query = lucene.query_2_molecules_date(CIPs1, CIPs2, date_min, date_max);
			lignes = lucene.detailsHits(query, s, 10000000);
			if (lignes != null){
				resultats.addAll(lignes);
			}
			counter++;
			
			// toutes les 100 lignes écriture dans le fichier
			if (counter == 100) {
				Files.write(fichierSortie, resultats, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
				totalcounter = totalcounter + counter;
				System.out.println(totalcounter);
				counter=0;
				// reset : 
				resultats = new ArrayList<String>();
			}
		}
		// à la fin, je ne suis pas arrivé à 100 donc les dernières lignes ne sont pas écrites
		Files.write(fichierSortie, resultats, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
		return(resultats);
	}
	
	public DenombreInteraction(String dossier_index, String fichier_interaction, String fichier_moleculesCIP) throws IOException, LigneException{
		lucene = new LuceneSearcher(dossier_index);
		interaction = new Interaction(fichier_interaction,fichier_moleculesCIP);
	}
	
	public static void main(String[] args) throws IOException, LigneException, ParseException {
		// TODO Auto-generated method stub
		
		String fichier_interaction ="interaction/5112016/interactions5112016.csv";
		String fichier_moleculesCIP ="interaction/5112016/moleculesCIP5112016.csv";
		DenombreInteraction denombre = new DenombreInteraction(Global.INDEX_FOLDER,
				fichier_interaction,fichier_moleculesCIP);
		String fichierSortie = "output/denombrementCIP12112016.csv";
		Path file = Paths.get(fichierSortie);
		//Files.write(file, denombre.denombre(), Charset.forName("UTF-8"));
		denombre.denombredetails(file);
		
	}

}
