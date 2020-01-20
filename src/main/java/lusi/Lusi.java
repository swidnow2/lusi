package lusi;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

class Lusi {
    private final String indexPath;
    private SegmentInfos si;
    private FSDirectory directory;

    Lusi(final String indexPath) {
        this.indexPath = indexPath;
    }

    Lusi init() throws IOException {
        directory = FSDirectory.open(new File(indexPath));

        si = new SegmentInfos();
        si.read(directory);

        return this;
    }

    public void dumpTerms() throws IOException {


        final IndexReader reader = IndexReader.open(directory);

        printFields(reader);
        printModifiedTerms(reader);
    }

    private void printFields(IndexReader reader) throws IOException {
        System.out.println("---FIELDS---");
        Fields fields = MultiFields.getFields(reader);
        for (String field : fields) {
            System.out.println(field);
        }
        System.out.println("---FIELDS---");
    }

    private void printModifiedTerms(IndexReader reader) throws IOException {
        Date yerBeforeDate = Date.from(LocalDateTime.now().minusYears(1).atZone(ZoneId.systemDefault()).toInstant());
        String yearBeforeStr = DateTools.dateToString(yerBeforeDate, DateTools.Resolution.MILLISECOND);

        String now = DateTools.dateToString(new Date(), DateTools.Resolution.MILLISECOND);


        Query query = new TermRangeQuery("modified", new BytesRef(yearBeforeStr), new BytesRef(now), true, true);
        TotalHitCountCollector collector = new TotalHitCountCollector();
        new IndexSearcher(reader).search(query, collector);

        System.out.println("Hits=" + collector.getTotalHits());
    }
}
