import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.store.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.util.*;
import org.apache.lucene.document.*;
import java.io.*;
import java.util.*;


class XSearchData {
    public static void main(String[] args) {
        Analyzer analyzer;
        RAMDirectory directory;
        IndexWriterConfig config;
        IndexWriter iwriter;
        DirectoryReader ireader;
        IndexSearcher isearcher;
        QueryBuilder builder;
        RandomAccessFile in;
        ArrayList<String> inputFiles;
        String termsFile;
        ArrayList<Document> documents;
        String line;
        long start, end;
        long indexTime, indexSize, searchTime;

        indexTime = 0;
        indexSize = 0;
        searchTime = 0;
        try {
            start = System.currentTimeMillis();
            inputFiles = new ArrayList<String>();

            in = new RandomAccessFile(args[0], "r");
            while ((line = in.readLine()) != null) {
                inputFiles.add(line);
            }
            in.close();
        
            termsFile = args[1];
            
            documents = new ArrayList<Document>();

            for (String inputFile : inputFiles) {
                File file = new File(inputFile);
                Document document = new Document();

                Field contentField = new Field("content", new InputStreamReader(new FileInputStream(file)),
                        TextField.TYPE_NOT_STORED);
                Field filenameField = new Field("filename", file.getName(), StoredField.TYPE);
                Field filepathField = new Field("filepath", file.getCanonicalPath(), StoredField.TYPE);

                document.add(contentField);
                document.add(filenameField);
                document.add(filepathField);

                documents.add(document);
            }

            analyzer = new StandardAnalyzer();
            directory = new RAMDirectory();
            config = new IndexWriterConfig(analyzer);
            iwriter = new IndexWriter(directory, config);

            for (Document document : documents) {
                iwriter.addDocument(document);
            }
            iwriter.commit();

            iwriter.close();
            end = System.currentTimeMillis();
            indexTime = (end - start);

            indexSize = directory.ramBytesUsed() / 1000;

            start = System.currentTimeMillis();
            ireader = DirectoryReader.open(directory);
            isearcher = new IndexSearcher(ireader);
            builder = new QueryBuilder(analyzer);

            in = new RandomAccessFile(termsFile, "r");
            while ((line = in.readLine()) != null) {
                Query query = builder.createBooleanQuery("content", line);
                ScoreDoc[] hits = isearcher.search(query, 1000).scoreDocs;
                //if (hits.length < 1) {
                //    System.out.println("Incorrect search result!");
                //}
            }
            in.close();

            ireader.close();
            directory.close();
            end = System.currentTimeMillis();
            searchTime = (end - start);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("IndexTime: " + indexTime + " ms");
        System.out.println("IndexSize: " + indexSize + " kB");
        System.out.println("SearchTime: " + searchTime + " ms");

        System.exit(0);  
    }
}
