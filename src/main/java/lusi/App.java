package lusi;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;

public class App {
    public static void main(String[] args) throws IOException {
        checkArgs(args);

        final Lusi lusi = new Lusi(args[0]).init();

        System.out.println(Instant.now().toString() + "\t" + args[0]);

        final HashSet<String> argsSet = new HashSet<>(Arrays.asList(args));

        if (argsSet.contains("--dumpTerms")) {
            lusi.dumpTerms();
        } else if (argsSet.contains("--storedFieldSize")) {
            lusi.countFieldSize();
        } else {
            lusi.printSegmentInfo();
            lusi.printDiagnostics();
            lusi.printFiles();
        }
    }

    private static void checkArgs(final String[] args) {
        if (args.length < 1) {
            System.out.println("Usage:\n$ sh lusi.sh <path to your index directory>x");
            System.out.println("Additional options:");
            System.out.println("--dumpTerms - prints index terms with number of unique values");
            System.out.println("--storedFieldSize - prints estimated size of stored fields");
            System.exit(1);
        }
    }
}
