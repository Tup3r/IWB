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

class IndexerThreads extends Thread {

	private final IndexWriter w;
	private volatile Document doc;
	private final List<String> fileList;
	private final int filethreadRatio;
	private final int position;
	private final int endPosition;
	private final String path;


	public IndexerThreads(IndexWriter w,List<String> fileList,int filethreadRatio,int position,int endPosition,String path) {
		
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

//				Iterable<Document> docList;
//				List<Document> documents = new ArrayList();

//				System.out.println(" POSITION : " + position + " END POSITION: " + endPosition );
				for ( int j = position ; j < endPosition ; j++){

					Document doc = new Document();
					String filename = (Paths.get(path + "/" + fileList.get(j))).toString();	
					File file = new File(filename);
					FileInputStream fstream = new FileInputStream(file);
					doc.add(new Field("filename", file.getName(), StoredField.TYPE));
					doc.add(new Field("content", new InputStreamReader(fstream),TextField.TYPE_NOT_STORED));  //Add content field    		
					//documents.add(doc);
					w.addDocument(doc);	
					fstream.close();
				}
//				System.out.println(" Max Docs "  + w.maxDoc() + " NO of docs indexed " + w.numDocs()); 
//				docList = documents;
//				w.addDocuments(docList);	

			} catch(Exception e){
				e.printStackTrace();
			}
	}
}

public class luceneBenchConcurrentv2 {

		StandardAnalyzer analyzer;	
		Directory indexDir ;
		IndexWriterConfig config ;
		IndexReader reader ;
        IndexSearcher searcher ;
        public long indexSize = 0;
        private static IndexWriter writer ;
        String title;
        String description;
        //Query q ;
        //QueryParser queryParse ;
		
	public static void main(String[] args) throws IOException,ParseException
		{
		//Getting the filepath to the data//

			long StartTime,EndTime,IndexTime,CommitTime,SearchTime;
			luceneBenchConcurrentv2 l = new luceneBenchConcurrentv2();
			StandardAnalyzer analyzer = new StandardAnalyzer();		
			//PrintStream out = new PrintStream(new File("Log.txt"));
//			PrintStream out = new PrintStream(new FileHandler("%h/Log.txt",true));
		//	PrintStream console = System.out;dd
		//	System.setOut(out);
			
			//String path = scanner.nextLine();
			int numThreads=0,ar1=0,ar2=0,ar3=0,ar4=0,ar6=0;
			String path ;
			Directory indexDir =  new RAMDirectory();
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			
			ConcurrentMergeScheduler cms = (ConcurrentMergeScheduler) config.getMergeScheduler();
                        cms.setDefaultMaxMergesAndThreads(false);	
			
			if (args[0] != null ) {
				path  = args[0];
			} else {  throw new IOException("Argument 0 cannot be NULL"); 
			}
	
			if ( args[1] != null ) {
    	                    String MaxBuffDocs = args[1];
        	                ar1 = Integer.parseInt(MaxBuffDocs);
                	        config.setMaxBufferedDocs(ar1);  // minimal number of documents required before the buffered in-memory documents are flushed as a new Segment
                         }

                        if ( args[2] != null )  {
                        	String RAMBufferDocs = args[2];
	                        ar2 = Integer.parseInt(RAMBufferDocs);
        	                config.setRAMBufferSizeMB(ar2); /// RAM that may be used for buffering added documents and deletions before they are flushed to the Directory.
                         }
                        if ( args[3] != null )  {
                        	String maxMergeCount = args[3];
	                        ar3 = Integer.parseInt(maxMergeCount);
                         }
                        if ( args[4] != null )  {
                        	String maxThreadCount = args[4];
	                        ar4 = Integer.parseInt(maxThreadCount);
                         }
			
			if ( args[5].charAt(0) == 'T' ) {	
				 cms.disableAutoIOThrottle();	
			}
                        
			if ( args[6] != null )  {
                        	String maxSegmentMerge = args[6];
	                        ar6 = Integer.parseInt(maxSegmentMerge);
                         }
			if ( args[7] != null )  {
                        	String Threads = args[7];
	                         numThreads = Integer.parseInt(Threads);
                         }
			
			//Set MaxMergeCount and MaxThreadCount	
			cms.setMaxMergesAndThreads(ar3,ar4);
			config.setMergeScheduler(cms);
				
			IndexWriter writer = new IndexWriter(indexDir, config);
			
			//Setting the Merge Factor using the MergePolicy
			MergePolicy mp = writer.getConfig().getMergePolicy();
			if ( mp instanceof TieredMergePolicy) {
				TieredMergePolicy tmp =  (TieredMergePolicy) mp;
				tmp.setMaxMergeAtOnce(ar6);
				}

			File filepath = new File(path);
			String [] folderlist = filepath.list();
			//Stream<Path> files = Files.list(Paths.get(path));
			List<String> filearray =  Arrays.asList(filepath.list());
			List<Document> documents = new ArrayList();
			//mrl.getMBPerSec();
			Thread[] threads = new Thread[numThreads];
			int filethreadRatio;
//			System.out.println("#######   No of Docs is set to : "+ filearray.size() + " ###########");
			

//			if ( filearray.size() % numThreads == 0) {
//				filethreadRatio = filearray.size()/numThreads;
//			
//			} else {

				filethreadRatio = (int)( filearray.size() / numThreads );

//			}

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

			IndexTime = EndTime - StartTime;

			
			StartTime = System.nanoTime();	
			writer.commit();
			EndTime = System.nanoTime();
			CommitTime = EndTime - StartTime;
			
		/*INdexedDocs , INdexTime, CommitTIme, maxBufferedDocs, RamBudderSizeMB, MaxMErgeCOunt, MaxThreadCOunt, SetIOthrottle, maxSegmentmergeatonce, Threads, FIlethreadRatio, */
	//		System.out.println("INdexDocsNo " + writer.numDocs() + "\tIndex Time " + IndexTime + "\tCommit time " + CommitTime + "\tmaxBufferedDocs " + ar1 + "\tRamBufferMB " + ar2 + "\tmaxMergeCount " + cms.getMaxMergeCount() + "\tMaxThreadCount " +  cms.getMaxThreadCount() + "\tAutoIOThrottle " + cms.getAutoIOThrottle() + "\tmaxSegmentMergeAtOnce " + ar6 + "\tThreads #" + numThreads + "\tFilethreadRatio " + filethreadRatio);
			System.out.println(writer.numDocs() + " " + IndexTime + " " + CommitTime + " " + ar1 + " " + ar2 + " " + cms.getMaxMergeCount() + " " +  cms.getMaxThreadCount() + " " + cms.getAutoIOThrottle() + " " + ar6 + " " + numThreads + " " + filethreadRatio);

			writer.close();
			/////////ENDED HERE

			String querystr = args[8].length() > 0 ? args[8] : "ehson";
		
	       	 // the "title" arg specifies the default field to use
        	// when no field is explicitly specified in the query.
			QueryParser qParse = new QueryParser("description", analyzer);
        		Query q = qParse.parse(querystr);
				
        	// 3. search
	        	//int hitsPerPage = 10;
	        	IndexReader reader = DirectoryReader.open(indexDir);
        		//IndexSize = indexDir.ramBytesUsed() / 1000;
        		IndexSearcher searcher = new IndexSearcher(reader);

        		StartTime = System.currentTimeMillis();	 ///Get Search Time in miliseconds
	        	TopDocs docs = searcher.search(q, 100);        	
        		EndTime = System.currentTimeMillis();	
			SearchTime =  EndTime - StartTime;

        		ScoreDoc[] hits = docs.scoreDocs;

	        // 4. display results
        	/*	System.out.println("Found " + hits.length + " documents for TERM >> " + querystr);
        		for(int i=0;i<hits.length;++i) {
	          	  	int docId = hits[i].doc;
        	    		Document d = searcher.doc(docId);
	            		System.out.println((i + 1) + "." + "\t" + d.get("title"));
        		}*/

		        reader.close();
			
		}
}
