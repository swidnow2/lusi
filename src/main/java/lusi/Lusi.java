package lusi;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

class Lusi {
    private static final long MEG = 1024 * 1024;
    private static final String SEGMENT_INFO_FORMAT = "segment=%s\tsizeWithDocStores=%dM\tsizeWithoutDocStores=%dM\tdocCount=%d\tdelCount=%d";
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
        final TermEnum terms = reader.terms();

        Map<String, Long> fields = new HashMap<>();
        while (terms.next()) {
            final Term term = terms.term();
            fields.putIfAbsent(term.field(), 0L);
            fields.computeIfPresent(term.field(), (key, value) -> value + 1);
        }

        System.out.println("Unique term count: " + fields.size());
        System.out.println("List of terms with number of unique values: \n");

        fields.forEach((key, value) -> System.out.println(key + "\t" + value));
    }

    public void countFieldSize() throws IOException {
        final IndexReader reader = IndexReader.open(directory);
        final LongAdder sizeSummary = new LongAdder();

        Map<String, Long> fields = new HashMap<>();

        for (int i = 0; i < reader.maxDoc(); i++) {
            final Document document = reader.document(i);
            final List<Fieldable> documentFields = document.getFields();

            documentFields.forEach(f -> {
                fields.putIfAbsent(f.name(), 0L);
                fields.computeIfPresent(f.name(), (k, v) -> {
                    final int length = f.stringValue().getBytes().length;
                    sizeSummary.add(length);
                    return v + f.stringValue().getBytes().length;

                });
            });
        }

        System.out.println("Unique stored fields count: " + fields.size());
        System.out.println("Total size of fields: " + sizeSummary);
        System.out.println("List of stored fields with estimated sizes (in bytes): \n");

        fields.forEach((key, value) -> System.out.println(key + "\t" + value));
    }

    void printSegmentInfo() {
        System.out.println("\nSegment sizing:\n");
        si.iterator().forEachRemaining(segment -> {
            try {
                String info = String.format(
                        SEGMENT_INFO_FORMAT,
                        segment.name,
                        segment.sizeInBytes(true) / MEG,
                        segment.sizeInBytes(false) / MEG,
                        segment.docCount,
                        segment.getDelCount()
                );
                System.out.println(info);
            } catch (IOException ignored) {
            }
        });
    }

    void printDiagnostics() {
        System.out.println("\nDiagnostics:\n");
        si.iterator().forEachRemaining(segment -> System.out.println("segment=" + segment.name + "\t" + segment.getDiagnostics()));
    }

    void printFiles() {
        System.out.println("\nFiles:\n");
        si.iterator().forEachRemaining(segment -> {
            try {
                System.out.println("segment=" + segment.name + "\t" + segment.files());
            } catch (IOException e) {
            }
        });
    }
}
