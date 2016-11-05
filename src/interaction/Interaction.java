package interaction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import exceptions.LigneException;
import indexer.Global;

public class Interaction {

	private MoleculesCIP moleculesCIP;
	private List<String[]> interaction = new ArrayList<String[]>();
	
	// l'objectif de stocker l'information des interactions (on gère les dates pour interroger avec plusieurs thesaurus)
	/*
	 * Le fichier doit contenir 4 colonnes :
	 * 	1) nom de la première molécule
	 *  2) nom de la seconde molécule
	 *  3) date min
	 *  4) date max
	 */
	
	public List<String[]> get_interaction(){
		return (interaction);
	}
	
	public MoleculesCIP get_moleculesCIP(){
		return (moleculesCIP);
	}
	
	public Interaction(String fichier_interaction, String fichier_moleculesCIP) throws IOException, LigneException{
		this.moleculesCIP = new MoleculesCIP(fichier_moleculesCIP);
		
		BufferedReader br = new BufferedReader(new FileReader(fichier_interaction));
		String lines;
		int counter = 1;
		while ((lines = br.readLine()) != null) {
			String line[] = lines.split("\t");
			
			// check 4 colonnes
			if (line.length != 4){
				throw new LigneException("Le fichier " + fichier_interaction + " ne contient pas 4 colonnes à la ligne " + counter);
			}
			
			
			// check molécules connues : 
			String molecule1 = line[0];
			if (!moleculesCIP.get_moleculesCIP().containsKey(molecule1)){
				throw new LigneException("Le fichier " + fichier_moleculesCIP + " ne contient pas " + molecule1 + " contrairement à " + fichier_interaction);
			}
			String molecule2 = line[1];
			if (!moleculesCIP.get_moleculesCIP().containsKey(molecule2)){
				throw new LigneException("Le fichier " + fichier_moleculesCIP + " ne contient pas " + molecule2 + " contrairement à " + fichier_interaction);
			}
			
			// check date au bon format
			try {
				new SimpleDateFormat (Global.FORMAT_DATE + " HH").parse(line[2] + " 12");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				throw new LigneException("Le format de la date " + fichier_moleculesCIP + " n'est pas bon " + line[2] + "ligne " + counter);
			}
			
			// check date au bon format
			try {
				new SimpleDateFormat (Global.FORMAT_DATE + " HH").parse(line[3] + " 12");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				throw new LigneException("Le format de la date " + fichier_moleculesCIP + " n'est pas bon " + line[3] + "ligne " + counter);
			}
			
			counter++;
			// si tout est ok, on ajoute la ligne
			interaction.add(line);
		}
		br.close();
	}
	
	public static void main(String[] args) throws IOException, LigneException{
		Interaction interaction = new Interaction("interaction/interaction2012.txt","interaction/moleculesCIP.txt");
	}
}
