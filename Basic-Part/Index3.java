import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
 
class Index3 {
 
    WikiItem start;
    private static final String END_OF_DOCUMENT = "---END.OF.DOCUMENT---"; // constant string to check for end of document
 
    private class WikiItem {    // linked list 
        String str;             // word
        Document doc;           // linked list of document titles
        WikiItem next;         // next word
 
        WikiItem(String s, Document d, WikiItem n) { // constructor
            str = s;
            doc = d;
            next = n;
        }
    }

    private class Document { // linked list of document titles
        String title;        // title
        Document next;       // next title

        Document(String t, Document n) { // constructor
            title = t;
            next = n;
        }
    }

        // Iterates through the file (outer while loop) taking one word at a time and creating a WikiItem object for it.
        // Then it iterates through all words in the file (inner while loop), saving the titles of all documents that contain the current word.
        public Index3(String filename) {
            String word, word2, title;
            WikiItem current;
            Document startDoc;
            try {
                Scanner input = new Scanner(new File(filename), "UTF-8"); // First scanner
                word = input.next();
                title = word;
                startDoc = new Document(title, null);
    
                start = new WikiItem(word, startDoc, null); // first WikiItem
                current = start;
                while (input.hasNext()) {   // Read all words in input
                    word = input.next();   // second word in file
                    Scanner documentSeeker = new Scanner(new File(filename), "UTF-8"); // Second scanner
                    ArrayList<String> titles = new ArrayList<>(); // empty list for doc titles
    
                    while(documentSeeker.hasNext()) {
                        word2 = documentSeeker.next();
                        if(word2.equals(word)) { // if instance of current word is found
                            titles.add(title);   // add title to list
                            while(!word2.equals(END_OF_DOCUMENT)) { // skip to next document
                                word2 = documentSeeker.next();
                            }
                        }
                        if (word2.equals(END_OF_DOCUMENT) && documentSeeker.hasNext()) { 
                            title = documentSeeker.next(); // update title for next document
                        }
                    }
                    documentSeeker.close();

                    Document doc = createDocument(titles); // create linked list of document titles
    
                    current.next = new WikiItem(word, doc, null); // create new WikiItem with current word and its document titles
                    current = current.next;
                    title = start.str; // reset to first title
                }
                input.close();
            } catch (FileNotFoundException e) {
                System.out.println("Error reading file " + filename);
            }
        }

        // Creates a linked list of document titles from an ArrayList of titles.
        public Document createDocument(ArrayList<String> titles) {
            if (titles.isEmpty()) {
                return new Document(null, null);
            }
            else {
                Document startDoc = new Document(titles.get(0), null);
                Document doc = startDoc;
                for(int i = 1; i < titles.size(); i++) {
                    doc.next = new Document(titles.get(i), null);
                    doc = doc.next;
                }
                return startDoc;
            }
        }
 
// Linearly searches through the WikiItem object to see if searchstr is one of the strings in the file.
// If it is, it prints the titles of all documents that contain the searchstr.
    public boolean search(String searchstr) {
        WikiItem current = start;

        while (current != null) {
            if (current.str.equals(searchstr)) {

                printDocumentTitles(current.doc);
                return true;
            }
            
            current = current.next;
        }
        return false;
    }

    // Prints the titles of all documents in the linked list.
    public void printDocumentTitles(Document doc) {
        Document current = doc;
        while (current != null) {
            System.out.println(current.title);
            current = current.next;
        }
    }
 
    // Main method that takes in a file name as param (run in terminal).
    // Type "exit" to stop the program.
    // Otherwise, it will search in the file for the string given in terminal and print whether it exists or not.
    public static void main(String[] args) {
        System.out.println("Preprocessing " + args[0]);
        Index3 i = new Index3(args[0]);
        Scanner console = new Scanner(System.in);
        for (;;) {
            System.out.println("Input search string or type exit to stop");
            String searchstr = console.nextLine();
            if (searchstr.equals("exit")) {
                break;
            }
            i.search(searchstr);
        }
        console.close();
    }

    // First compile using $ javac Basic-Part/Index3.java

    // Run using $ java Index3.java DataFiles/WestburyLab.wikicorp.201004_100KB.txt

    // To succesfully run some of the large files you may have to increase the 
    // size of the maximum space to be used by the Java interpreter using the -Xmx flag. 
    // For instance, java -Xmx128m Index3.java DataFiles/WestburyLab.wikicorp.201004_50MB.txt sets the maximum space to 128MB.
}