package indexer;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import logger.MonLogger;


/* 
 * Classe permettant d'indexer les ordonnances dans un index Lucene
 */

public class LuceneIndexer {
	
	String dossier_index=  null;
	Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_47);
	IndexWriter iwriter;
	OpenMode openMode = null;
		
	public LuceneIndexer(String dossier_index, OpenMode openMode) throws IOException{
		this.dossier_index = dossier_index;
		this.openMode = openMode;
		File path = new File (dossier_index);
		Directory directory = FSDirectory.open(path);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
	    config.setOpenMode(openMode);//config.setOpenMode(OpenMode.CREATE_OR_APPEND);
	    iwriter = new IndexWriter(directory, config);
	}
	
	public void indexer(String fichier,String CIPinclusfichier) throws IOException{	    
		DPFiles indexer = new DPFiles(fichier,CIPinclusfichier);		
		try {
			indexer.set_numOrdonnance();
		} catch (Exception e){
			indexer.getLogger().message("ERREUR " + e.getMessage());
			e.printStackTrace();
		}
		//HashMap Map;
	    Map<String,Ordonnance> map = indexer.get_num_cip();
	    Iterator<Entry<String, Ordonnance>> entries = map.entrySet().iterator();
	    
	    while (entries.hasNext()) {
	        Map.Entry<String, Ordonnance> entry = entries.next();
	        Ordonnance ordonnance = entry.getValue();
	        Document doc = new Document();
	        
	        doc.add(new Field(Global.VARIABLE_DATE_DELIVRANCE, DateTools.dateToString(ordonnance.get_date_delivrance(),DateTools.Resolution.DAY),
	                Field.Store.YES, Field.Index.NOT_ANALYZED));
	        doc.add(new IntField(Global.VARIABLE_AGE, ordonnance.get_age(), Field.Store.YES));
	        doc.add(new LongField("id", ordonnance.get_OrdoDeli(), Field.Store.YES));
	        doc.add(new IntField("Nmedocs", ordonnance.get_CIP().size(), Field.Store.YES));
	        for (Long CIP : ordonnance.get_CIP()){
	            Field field = new LongField(Global.VARIABLE_CIP, CIP, Field.Store.YES);
	            doc.add(field);
	        }
	        
	        try{
				iwriter.addDocument(doc);
			} catch(Exception e){
				System.out.println("Exception");
			    e.printStackTrace();
			} finally{
				indexer.getLogger().close();
			}
	    } // fin boucle Ordonnances
    }
	
	public void close_writer() throws IOException{
		iwriter.close();
	}
		
	public void index_folder(String dossier_files, String CIPinclusfichier) throws Exception{
		
		File folder = new File(dossier_files);
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {      
			String fichier = listOfFiles[i].getAbsolutePath();
			indexer(fichier,CIPinclusfichier);
			if (Global.DEBUG) break;
	}
	}
	  // environ 5 minutes
		public static void main(String args[]) throws Exception{
			String CIPinclusfichier="interaction/5112016/CIP7inclus.txt";
			LuceneIndexer lucene = new LuceneIndexer(Global.INDEX_FOLDER,OpenMode.CREATE);
			lucene.index_folder(Global.DATA_FOLDER,CIPinclusfichier);
			lucene.close_writer();
		}
}
