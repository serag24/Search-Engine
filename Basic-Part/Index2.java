import java.io.*;
import java.util.Scanner;
 
class Index2 {
 
    WikiItem start;
    private static final String END_OF_DOCUMENT = "---END.OF.DOCUMENT---";
 
    private class WikiItem {
        String str;
        WikiItem next;
 
        WikiItem(String s, WikiItem n) {
            str = s;
            next = n;
        }
    }
 
    // Iterates through the file, prints word by word, and creates WikiItem object: start = Word(firstWord, Word(secondWord, Word(...)))
    public Index2(String filename) {
        String word;
        WikiItem current, tmp;
        try {
            Scanner input = new Scanner(new File(filename), "UTF-8");
            word = input.next();
            start = new WikiItem(word, null);
            current = start;
            while (input.hasNext()) {   // Read all words in input
                word = input.next();
                System.out.println(word);
                tmp = new WikiItem(word, null);
                current.next = tmp;
                current = tmp;
            }
            input.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file " + filename);
        }
    }
 
// Linearly searches through the WikiItem object to see if searchstr is one of the strings in the file
// Uppercase and lowercase are treated as different strings. Ex: "In" and "in" or "I" and "i".
    public boolean search(String searchstr) {
        WikiItem current = start;
        String title = start.str;

        while (current != null) {
            if (current.str.equals(searchstr)) {
                System.out.println(title);

                while(!current.str.equals(END_OF_DOCUMENT)) {
                    current = current.next;
                }
            }

            if(current.str.equals(END_OF_DOCUMENT) && current.next != null) {
                title = current.next.str;
            }
            
            current = current.next;
        }
        return false;
    }
 
    // Main method that takes in a file name as param (run in terminal).
    // Type "exit" to stop the program.
    // Otherwise, it will search in the file for the string given in terminal and print whether it exists or not.
    public static void main(String[] args) {
        System.out.println("Preprocessing " + args[0]);
        Index2 i = new Index2(args[0]);
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

    // First compile using $ javac Basic-Part/Index2.java

    // Run using $ java Basic-Part/Index2.java DataFiles/WestburyLab.wikicorp.201004_100KB.txt

    // To succesfully run some of the large files you may have to increase the 
    // size of the maximum space to be used by the Java interpreter using the -Xmx flag. 
    // For instance, java -Xmx128m Index2.java DataFiles/WestburyLab.wikicorp.201004_50MB.txt sets the maximum space to 128MB.
}


// How it works:
// Index2 i = new Index2(args[0]); creates the data structure WikiItem(word, WikiItem(word, WikiItem(word, ...)))

// search(searchstr) searches for the searchstr in the data structure and prints the title of the document if the searchstr is found.
// if it's not found, nothing gets printed, and the engine waits for next search string.

// search(searchstr) saves the title of the first doc, then searches for searchstr in the rest of the doc.
// At every iteration of outer while loop, it checks if "---END.OF.DOCUMENT---" is found. 
// If it is, it saves the title of the next doc and continues searching for searchstr.

// If searchstr is found in one doc, the title gets printed, then we search for "---END.OF.DOCUMENT---" to skip to the next doc.