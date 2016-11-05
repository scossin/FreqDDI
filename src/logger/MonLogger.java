package logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import indexer.Global;

/*
 * Classe pour enregistrer des logs
 */
public class MonLogger {

	private final static Logger logger = Logger.getLogger("MyLog") ;
	private FileHandler fh = null;
	
	public MonLogger() throws SecurityException, IOException{
		//logger = new Logger(Global.LOG_FILE, resource.);
		//logger = Logger.getLogger("MyLog");  
	    fh = new FileHandler(Global.LOG_FILE,true);  
	    logger.addHandler(fh);
	    SimpleFormatter formatter = new SimpleFormatter();  
	    fh.setFormatter(formatter);
	    
	}
	
	public static void main(String[] args) throws SecurityException, IOException {
		// TODO Auto-generated method stub
		// Logs
		MonLogger logger = new MonLogger();
		logger.message("test");
	}
	
	public void message(String message){
		logger.info(message);
	}
	
	public void close(){
		fh.close();
	}

}
