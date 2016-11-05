package interaction;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import exceptions.LigneException;
import indexer.Global;

public class MoleculesCIP {

	// Cette classe fait le lien entre une dci du thésaurus (molécules) et la liste des spécialités pharmaceutiques (codes CIP)
	
	private HashMap<String, Long[]> moleculesCIP = new HashMap<String, Long[]>();
	
	public MoleculesCIP(String fichier) throws IOException, LigneException{
		/*
		 *  format du fichier : 2 colonnes séparées par \t
		 *  	la première contient le nom de la molécule
		 *  	la deuxième contient la liste des codes CIP séparées par ;
		 */
		
		BufferedReader br = new BufferedReader(new FileReader(fichier));
		String lines;
		int counter = 1;
		while ((lines = br.readLine()) != null) {
			String line[] = lines.split("\t");
			if (line.length != 2){
				throw new LigneException("Le fichier " + fichier + " ne contient pas deux colonnes à la ligne " + counter);
			}
			counter++;
			
			// molécule
			String molecule = line[0];
			
			// CIP
			String CIP[] = line[1].split(";");
			Long[] CIP13 = new Long[CIP.length];
			for (int i = 0; i<CIP.length ; i++){
				CIP13[i] = Long.parseLong(CIP[i]);
			}
			if (moleculesCIP.containsKey(molecule)){
				throw new LigneException(molecule + " contient déjà une liste de codes CIP ");
			}
			moleculesCIP.put(molecule, CIP13);
		}
		br.close();
	}
	
	public HashMap<String, Long[]> get_moleculesCIP(){
		return(moleculesCIP);
	}
	
	public Long[] get_CIP(String molecule){
		return(moleculesCIP.get(molecule));
	}
	
	public static void main(String[] args) throws IOException, LigneException {
		// test
		String fichier = "interaction/5112016/moleculesCIP5112016.csv";
		MoleculesCIP moleculesCIP = new MoleculesCIP(fichier);
		System.out.print(moleculesCIP.get_moleculesCIP().size());
	}

}
