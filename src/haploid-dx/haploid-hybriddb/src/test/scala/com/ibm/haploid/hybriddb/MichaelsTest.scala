package com.ibm.haploid.hybriddb
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.Version
import org.junit.Test
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TopScoreDocCollector

@Test private class MichaelsTest {

  @Test def testAnything = {
    
  }
  
  @Test def luceneTest = {
    def addDoc(writer: IndexWriter, s: String) = {
      val doc = new Document()
      doc.add(new Field("title", s, Field.Store.YES, Field.Index.ANALYZED))
      writer.addDocument(doc)
    }
    
    val analyzer: StandardAnalyzer = new StandardAnalyzer(Version.LUCENE_36)
    val index = new RAMDirectory();
    val config = new IndexWriterConfig(Version.LUCENE_36, analyzer)
    val writer = new IndexWriter(index, config)
    
    addDoc(writer, "Lucene in Action aaabaa")
    addDoc(writer, "Lucene for Dummies bb")
    addDoc(writer, "Managing Lucene Gigabytes")
    addDoc(writer, "The Art of Computer Science")
    writer.close()
    
    val querystr = "b*"
    val query = new QueryParser(Version.LUCENE_36, "title", analyzer).parse(querystr);
    
    val reader = IndexReader.open(index)
    val searcher = new IndexSearcher(reader)
    val collector = TopScoreDocCollector.create(10, true)
    searcher.search(query, collector)
    
    val hits = collector.topDocs().scoreDocs
    
    println("Found " + collector.getTotalHits() + " hits.")
    for (i <- 0 to hits.length - 1) {
      val docid = hits(i).doc
      val doc = searcher.doc(docid)
      println(i + 1 + ". " + doc.get("title"))
    }
  }
  
}