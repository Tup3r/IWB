import java.io.*;
import java.util.*;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.System;
import java.lang.Object;
import java.lang.Thread;
import java.util.concurrent.*;
import java.util.stream.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.store.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.util.*;
import org.apache.lucene.document.*;

/*-----------------------------------------------------Loop for the Threads------------------------------------------------------------*/
class IndexerThreads extends Thread {
	
	private final IndexWriter w;
	private volatile Document doc;
	private final List<String> fileList;
	private final int filethreadRatio;
	private final int position;
	private final int endPosition;
	private final String path;
	
	public IndexerThreads(IndexWriter w,List<String> fileList,int filethreadRatio,int position,int endPosition,String path){
		
		this.fileList = fileList;
		this.w = w;
		this.filethreadRatio = filethreadRatio;
		this.position = position;
		this.endPosition = endPosition;
		this.path = path;
	}

	@Override
	public void run() {
		try {
			for ( int j = position ; j < endPosition ; j++){
				
				String filename = (Paths.get(path + "/" + fileList.get(j))).toString();	
				
				Document doc = new Document();
				File file = new File(filename);
				FileInputStream fstream = new FileInputStream(file);
				doc.add(new Field("filename", file.getName(), StoredField.TYPE));
				doc.add(new Field("content", new InputStreamReader(fstream),TextField.TYPE_NOT_STORED));  //Add content field    		
				w.addDocument(doc);	
				fstream.close();
			}	
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}

/*--------------------------------------------------Start of the Actual Program--------------------------------------------------------*/
public class B_OP_Benchmark {	

	StandardAnalyzer analyzer;	
	Directory indexDir ;
	IndexWriterConfig config ;
	IndexReader reader ;
	IndexSearcher searcher ;
	public long indexSize = 0;
	private static IndexWriter writer ;
	String title;
	String description;
		
	public static void main(String[] args) throws IOException,ParseException {
		
		int numThreads=0,ar1=0,ar2=0,ar3=0,filethreadRatio;
		long StartTime,EndTime,IndexTime,CommitTime,SearchTime;
		String path;
	//-------------------------------------------OBJECTS--------------------------------------------------------------
		B_OP_Benchmark l = new B_OP_Benchmark();
		StandardAnalyzer analyzer = new StandardAnalyzer();		
		Directory indexDir =  new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setCommitOnClose(false);	
		ConcurrentMergeScheduler cms = (ConcurrentMergeScheduler) config.getMergeScheduler();
	
		//----------------------------------GRABBING THE ARGUMENTS----------------------------------------------------
		if (args[0] != null ) {
			path  = args[0];
		} else {  
			throw new IOException("Argument 0 cannot be NULL"); 
		}
		if ( args[1] != null ) {
			// minimal number of documents required before the buffered in-memory documents are flushed as a new Segment
			String MaxBuffDocs = args[1];
			ar1 = Integer.parseInt(MaxBuffDocs);
			config.setMaxBufferedDocs(ar1);  
		}
		if ( args[2] != null )  {
			// RAM that may be used for buffering added documents and deletions before they are flushed to the Directory.
			String RAMBufferDocs = args[2];
			ar2 = Integer.parseInt(RAMBufferDocs);
			config.setRAMBufferSizeMB(ar2); 
		}
		if ( args[3] != null )  {
			String Threads = args[3];
			numThreads = Integer.parseInt(Threads);
		}
		if ( args[4].charAt(0) == 'T' ) {	
			cms.disableAutoIOThrottle();	
		}
	
	//-------------------------------------------OBJECTS--------------------------------------------------------------
		IndexWriter writer = new IndexWriter(indexDir, config);
		File filepath = new File(path);
		String [] folderlist = filepath.list();
		List<String> filearray =  Arrays.asList(filepath.list());
		List<Document> documents = new ArrayList();
		Thread[] threads = new Thread[numThreads];
		filethreadRatio = (int)( filearray.size() / numThreads );

		//***********************TIME1************************\\
		StartTime = System.currentTimeMillis();
		try {
			int position = 0,endPosition = filethreadRatio - 1;
			for ( int j = 0 ; j < numThreads ; j ++ ){
				
				threads[j] = new IndexerThreads(writer,filearray,filethreadRatio,position,endPosition,path);
				threads[j].start();
				position = position + filethreadRatio;
				
				if (j != numThreads - 1 ){
					endPosition = position + filethreadRatio -1;
				} else {
					endPosition = filearray.size() - 1;
				} 

			}																																	

			for ( int j = 0 ; j < numThreads ; j++ ){							
				threads[j].join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		EndTime = System.currentTimeMillis();
		writer.commit();
		writer.close();

		IndexTime = EndTime - StartTime;

		//______________________OUTPUT________________________\\
		System.out.println(0 + " " + IndexTime + " " + 0  + " " + ar1 + " " + ar2 + " "  + numThreads + " " + filethreadRatio);
	}
}
