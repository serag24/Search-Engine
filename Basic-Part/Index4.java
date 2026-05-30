import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
 
class Index4 {
    private static final String END_OF_DOCUMENT = "---END.OF.DOCUMENT---";
    private static int count;

    private static class WikiItem {
        String str;
        Document doc;
        WikiItem next;

        WikiItem(String s, Document d) {
            str = s;
            doc = d;
            next = null;
        }
    }

    private static class Document {
        String title;
        Document next;

        Document(String t, Document n) {
            title = t;
            next = n;
        }
    }

    
    //When the number of distinct words (WikiItems) reaches table length, rehash to 2*m+1 and new (a,b)
    private static class HashTable {
        private static final int INITIAL_CAPACITY = 17;

        private WikiItem[] table;
        
        private int v;   // keeps track of how many distinct words are in the hash table
        private int hashA;
        private int hashB;

        public HashTable() {
            table = new WikiItem[INITIAL_CAPACITY];
            chooseHashParameters();
        }

        private void chooseHashParameters() {
            int m = table.length;
            ThreadLocalRandom r = ThreadLocalRandom.current();
            // index = (a * k + b)% m with a in [1, m-1], b in [0, m-1]
            hashA = 1 + r.nextInt(m - 1);
            hashB = r.nextInt(m);
        }

         // s[i] is the i-th character of the string s, converted to an int value (ex. 'A' → 65, 'a' → 97).
        // How hashCode() works:
        // s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]

        private int indexFor(String word) {
            int m = table.length;
            int kMod = Math.floorMod(word.hashCode(), m);
            return (int) Math.floorMod((long) hashA * kMod + hashB, m);
        }

        // Rehashing the hash table when the number of distinct words reaches table length.
        // Takes each WikiItem in old table and inserts it into the new table.
        private void rehash() {
            WikiItem[] old = table;
            int newLen = old.length * 2 + 1;
            table = new WikiItem[newLen];
            for (int i = 0; i < newLen; i++) {
                table[i] = null;
            }
            chooseHashParameters();
            for (WikiItem head : old) {
                WikiItem cur = head;
                while (cur != null) {
                    WikiItem next = cur.next;
                    cur.next = null;
                    insertItemAtBucket(cur);
                    cur = next;
                }
            }
        }

        // insert item from old table into new table
        private void insertItemAtBucket(WikiItem item) {
            int i = indexFor(item.str);
            item.next = table[i];
            table[i] = item;
        }

        private static void addTitleIfMissing(WikiItem item, String title) {
            Document head = item.doc;
            if (head != null && head.title.equals(title)) {
                return;
            }
            item.doc = new Document(title, head);
            //Index4.count+=title.length()+(8-title.length()%8);
        }
         
        public void addWord(String word, String title) {
            int i = indexFor(word);
            WikiItem cur = table[i];
            while (cur != null) {
                if (cur.str.equals(word)) { // word is already in the table
                    addTitleIfMissing(cur, title);
                    return;
                }
                cur = cur.next;
            }
            if (v == table.length) {   // rehash when v == table.length before adding a new distinct word
                rehash();
                i = indexFor(word);
            }
            WikiItem item = new WikiItem(word, new Document(title, null));
            item.next = table[i];
            table[i] = item;
            v++;
            //Index4.count+=word.length()+(8-word.length()%8);
        }

        public Document getTitles(String word) {
            int i = indexFor(word);
            WikiItem current = table[i];

            while (current != null) {
                if (current.str.equals(word)) {
                    return current.doc;
                }
                current = current.next;
            }
            return null;
        }

        public void printIndexStatistics() {
            int m = table.length;
            System.out.println("Table size m: " + m);
            int uniqueWords = 0;
            int titlesOverall = 0;
            for (int i = 0; i < m; i++) {
                WikiItem w = table[i];
                while (w != null) {
                    uniqueWords++;
                    Document d = w.doc;
                    while (d != null) {
                        titlesOverall++;
                        d = d.next;
                    }
                    w = w.next;
                }
            }
            System.out.println("Unique words: " + uniqueWords);
            System.out.println("Titles overall: " + titlesOverall);
        }
    }

    public static void printDocumentTitles(Document doc) {
        if (doc == null) {
            System.out.println("Word not found");
            return;
        }
        Document current = doc;
        while (current != null) {
            System.out.println(current.title);
            current = current.next;
        }
    }

    public static void search(String searchstr, HashTable hashTable) {
        printDocumentTitles(hashTable.getTitles(searchstr));
    }

    public Index4(String filename, HashTable hashTable) {
        try {
            Scanner input = new Scanner(new File(filename), StandardCharsets.UTF_8.name());
            String title = input.next();
            String word = input.next();
            hashTable.addWord(word, title);

            while (input.hasNext()) {
                if (word.equals(END_OF_DOCUMENT)) {
                    while (input.hasNextLine()) {
                        String line = input.nextLine();
                        if (!line.isEmpty()) {
                            title = line;
                            break;
                        }
                    }
                    if (title == null) { // end of file
                        break;
                    }
                }
                word = input.next();
                hashTable.addWord(word, title);
            }
            input.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file " + filename);
        }
    }

    public static void main(String[] args) {
        System.out.println("Preprocessing " + args[0]);
        long preprocessStart = System.nanoTime();
        HashTable hashTable = new HashTable();
        Index4 i = new Index4(args[0], hashTable);
        long preprocessEnd = System.nanoTime();
        long preprocessMs = (preprocessEnd - preprocessStart) / 1_000_000L;
        System.out.println("Preprocessing time: " + preprocessMs + " ms");
        //hashTable.printIndexStatistics();
        //System.out.println("Number of bytes for all actual strings: " + Index4.count);
        Scanner console = new Scanner(System.in);
        //System.out.println("Words: " + hashTable.v);
        for (;;) {
            System.out.println("Input search string or type exit to stop");
            String searchstr = console.nextLine();
            long searchStart = System.nanoTime();
            if (searchstr.equals("exit")) {
                break;
            }
            search(searchstr, hashTable);
            long searchEnd = System.nanoTime();
            long searchMs = (searchEnd - searchStart) / 1_000_000L;
            System.out.println("Search time: " + searchMs + " ms");
        }
        console.close();
    }

    // First compile using $ javac Basic-Part/Index4.java

    // Run using $ java Basic-Part/Index4.java DataFiles/WestburyLab.wikicorp.201004_100KB.txt

    // To succesfully run some of the large files you may have to increase the 
    // size of the maximum space to be used by the Java interpreter using the -Xmx flag. 
    // For instance, java -Xmx128m Basic-Part/Index4.java DataFiles/WestburyLab.wikicorp.201004_50MB.txt sets the maximum space to 128MB.
}
