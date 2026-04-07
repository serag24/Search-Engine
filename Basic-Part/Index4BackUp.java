
// import java.io.*;
// import java.util.Scanner;
// import java.util.ArrayList;
 
// class Index4 {
//     private static final String END_OF_DOCUMENT = "---END.OF.DOCUMENT---"; // constant string to check for end of document
 
//     private static class WikiItem {    // linked list 
//         String str;             // word
//         Document doc;           // linked list of document titles
//         WikiItem next;          // next element in hashtable at certain index i

//         WikiItem(String s, Document d) { // constructor
//             str = s;
//             doc = d;
//             next = null;
//         }
//     }

//     private static class Document { // linked list of document titles
//         String title;        // title
//         Document next;       // next title

//         Document(String t, Document n) { // constructor
//             title = t;
//             next = n;
//         }
//     }

//     // Set up hash table with linked lists at each index
//     private static class HashTable {
//         private static final int TABLE_SIZE = 17;
//         private WikiItem[] table; // the hash table itself - an array of linked lists

//         public HashTable() {
//             table = new WikiItem[TABLE_SIZE];

//             // initialize linked lists at each index to null
//             for (int i=0; i<TABLE_SIZE; i++) {
//                 table[i] = null;
//             }
//         }

//         // hash function
//         private int hash(String word) {
//             return word.length() % 17;
//         }
    
//         // insert object at beginning of linked list at calculated index in hash table
//         public void insert (String word, String filename) {
//             Document doc = Index4.getDocumentLinkedList(word, filename);
    
//             int index = hash(word);
//             WikiItem item = new WikiItem(word, doc);
    
//             if (table[index] == null) {
//                 table[index] = item;
//             } else {
//                 item.next = table[index];
//                 table[index] = item;
//             }
//         }

//         // delete object from linked list
//         public void delete (String word) {
//             int index = hash(word);
//             WikiItem current = table[index];

//             if(current.str.equals(word)) {
//                 table[index] = current.next;
//                 return;
//             }

//             while(current != null) {
//                 if(current.next.str.equals(word)) {
//                     current.next = current.next.next;
//                     return;
//                 }
//                 current = current.next;
//             }

//             System.out.println("Word not found");
//         }

//         // search for object in linked list and return true if found, false otherwise
//         public boolean search(String word) {
//             int index = hash(word);
//             WikiItem current = table[index];

//             while(current != null) {
//                 if(current.str.equals(word)) {
//                     return true;
//                 }
//                 current = current.next;
//             }
//             return false;
//         }

//         // search for object in linked list and return document titles if found, null otherwise
//         public Document getTitles(String word) {
//             int index = hash(word);
//             WikiItem current = table[index];

//             while(current != null) {
//                 if(current.str.equals(word)) {
//                     return current.doc;
//                 }
//                 current = current.next;
//             }
//             return null;
//         }
//     }

//     public static Document getDocumentLinkedList(String word, String filename) {
//         String word2, title;
//         Document doc = new Document(null, null);
//         try {
//             Scanner input = new Scanner(new File(filename), "UTF-8"); // First scanner
//             word2 = input.next();
//             title = word2;
//             ArrayList<String> titles = new ArrayList<>(); // empty list for doc titles

//             if(word2.equals(word)) { 
//                 input.close();
//                 return new Document(title, null);
//             }

//             while (input.hasNext()) {   // Read all words in input
//                 word2 = input.next();   // second word in file

//                 if(word2.equals(word)) { // if instance of current word is found
//                     titles.add(title);   // add title to list
//                     while(!word2.equals(END_OF_DOCUMENT)) { // skip to next document
//                         word2 = input.next();
//                     }
//                 }
//                 if (word2.equals(END_OF_DOCUMENT) && input.hasNext()) { 
//                     title = input.next(); // update title for next document
//                 }

//                 doc = Index4.createDocumentLinkedList(titles); // create linked list of document titles
//             }
//             input.close();
//         } catch (FileNotFoundException e) {
//             System.out.println("Error reading file " + filename);
//         }
//         return doc;
//     }

//     // Creates a linked list of document titles from an ArrayList of titles.
//     public static Document createDocumentLinkedList(ArrayList<String> titles) {
//         if (titles.isEmpty()) {
//             return new Document(null, null);
//         }
//         else {
//             Document startDoc = new Document(titles.get(0), null);
//             Document doc = startDoc;
//             for(int i = 1; i < titles.size(); i++) {
//                 doc.next = new Document(titles.get(i), null);
//                 doc = doc.next;
//             }
//             return startDoc;
//         }
//     }

//      // Prints the titles of all documents in the linked list.
//     public static void printDocumentTitles(Document doc) {
//         if(doc == null) { // case where word is not in the hash table
//             System.out.println("Word not found");
//             return;
//         }
//         Document current = doc;
//         while (current != null) {
//             System.out.println(current.title);
//             current = current.next;
//         }
//     }

//     // search for object in hash table and print its document titles
//     public static void search(String searchstr, HashTable hashTable) {
//         printDocumentTitles(hashTable.getTitles(searchstr));
//     }

//     // preprocess the file and insert objects into hash table
//     public Index4(String filename, HashTable hashTable) {
//         try {
//             Scanner input = new Scanner(new File(filename), "UTF-8");
//             while(input.hasNext()) {
//                 String word = input.next();
//                 hashTable.insert(word, filename);
//             }
//             input.close();
//         } catch (FileNotFoundException e) {
//             System.out.println("Error reading file " + filename);
//         }
//     }

//     // Main method that takes in a file name as param (run in terminal).
//     // Type "exit" to stop the program.
//     // Otherwise, it will search in the file for the string given in terminal and print whether it exists or not.
//     public static void main(String[] args) {
//         System.out.println("Preprocessing " + args[0]);
//         HashTable hashTable = new HashTable();
//         Index4 i = new Index4(args[0], hashTable);
//         Scanner console = new Scanner(System.in);
//         for (;;) {
//             System.out.println("Input search string or type exit to stop");
//             String searchstr = console.nextLine();
//             if (searchstr.equals("exit")) {
//                 break;
//             }
//             search(searchstr, hashTable);
//         }
//         console.close();
//     }

//     // First compile using $ javac Basic-Part/Index4.java

//     // Run using $ java Basic-Part/Index4.java DataFiles/WestburyLab.wikicorp.201004_100KB.txt

//     // To succesfully run some of the large files you may have to increase the 
//     // size of the maximum space to be used by the Java interpreter using the -Xmx flag. 
//     // For instance, java -Xmx128m Index4.java DataFiles/WestburyLab.wikicorp.201004_50MB.txt sets the maximum space to 128MB.
// }

// // question to teacher:
// // If the hash function is ok.
// // Here, I again give the first word only one title. Is this ok?
// // Do I need to have the search and delete operations available for the hash table?
// // If yes, do I need to adjust the user input, to take in the name of the operation - insert, search, delete?

