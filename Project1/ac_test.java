/**
 * Implementation of a De La Briandais Trie
 * Created for coe 1501 Project 1
 * Maps String to Integers, the String are the dictionary provided for the Project, the Integers will be the number of
 * times the user has accesed that String
 *
 * @author Tyler Mohnke
 */
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.util.Scanner;
 
 public class ac_test
 {
    private Node m_Root;
    
    /**
     * Constructor
     */
    public ac_test()
    {
        // For implementation convience, have a dummy root node
        m_Root = new Node('.');
    }
    
    /**
     * Maps String key to some Integer value. Dublpicate keys will overwrite the previously mapped value. Null keys are
     * not permitted. Null values are also not permitted
     * 
     * @param key   Any non null String
     * @param value Any non null Integer
     */
    public void put(String key, Integer value)
    {
        if((key == null) || (value == null))
        {
            return;
        }
        
        // find (create) the node, set value
        Node node = buildTrie(key, 0, m_Root);
        node.setVlaue(value);
    }
    
    /**
     * Gets the value associated with String key. Returns null if key was not put into this object
     *
     * @param key   Any non null String
     * @return      Integer
     */
    public Integer get(String key)
    {
        if(key == null)
        {
            return null;
        }
        
        // Find the node, return the value. If the key is a prefix of an existing key, but not a key itself, the Node's
        // value should be null
        Node node = getNode(key, 0, m_Root);
        if(node == null)
        {
            return null;
        }
        else
        {
            return node.getValue();
        }
    }
    
    /**
     * Gets a set of 5 StringValue mappings. The Strings in the StringValue all begin with prefixKey. The mappings are
     * ordered such that highest values are first, then the least String for equal values. If less than five mappings
     * exist for a given prefix, the remaining entries in the returned array will be null
     *
     * @param prefixKey The shared prefix of a set of keys in this object
     * @return          StringValue[]
     */
    public StringValue[] getPriorityPrefixSet(String prefixKey)
    {
        StringValue[] retval = new StringValue[5];
        
        if(prefixKey == null)
        {
            return retval;
        }
        
        Node subTreeRoot = getNode(prefixKey, 0, m_Root);
        if(subTreeRoot == null)
        {
            return retval;
        }
        
        // initialize the set with the sub trie root if needed.
        if(subTreeRoot.getValue() != null)
        {
            retval[0] = new StringValue(prefixKey, subTreeRoot.getValue());
        }
        fillSet(retval, prefixKey, subTreeRoot.getChild());
        
        return retval;
    }
    
    /**
     * Writes every key and its associated value to the FileWriter. Each entry is seperated by a carriage return and a
     * line feed. Each Key Value mapping is deliminated by a tab.
     *
     * @param dstination    A FileWriter
     * @throws IOEception   Throws whenever FileWriter.append would throw
     */
    public void writeObject(FileWriter destination) throws IOException
    {
        write(destination, "", m_Root.getChild());
    }
    
    /**
     * Searches through the nodes for a node representing key. Recursively search downwards, iteratively search through 
     * siblings. Each recursive call to this function finds the next Node with a key value of key.charAt(keyIndex). If
     * the node does not exist it will be created.
     *
     * @param key           The string being stored.
     * @param keyIndex      The index of the char in the key that is being searched for
     * @param parentNode    A reference to the Node found one layer up
     * @return              Node that represents the given key
     */
    private Node buildTrie(String key, int keyIndex, Node parentNode)
    {
        // Base case, if the whole string has been iterated through return that node
        if(key.length() == keyIndex)
        {
            return parentNode;
        }
        
        // If the child is null add a node
        if(parentNode.getChild() == null)
        {
            parentNode.setChild(new Node(key.charAt(keyIndex)));
        }
        
        // Search through current layer for current key
        Node currentNode = parentNode.getChild();
        while((currentNode.hasKey(key.charAt(keyIndex)) == false) && (currentNode.getSibling() != null))
        {
            currentNode = currentNode.getSibling();
        }
        
        // key is either in currentNode or the key was not found and sibling needs to be added
        if(currentNode.hasKey(key.charAt(keyIndex)))
        {
            return buildTrie(key, keyIndex + 1, currentNode);
        }
        else
        {
            assert (currentNode.getSibling() == null);
            
            currentNode.setSibling(new Node(key.charAt(keyIndex)));
            return buildTrie(key, keyIndex + 1, currentNode.getSibling());
        }
    }
    
    /**
     * Function equivalently to buildTrie, but returns null if a Node is not found instead of creating a new Node
     *
     * @see buildTrie
     * @param key           The string being stored.
     * @param keyIndex      The index of the char in the key that is being searched for
     * @param parentNode    A reference to the Node found one layer up
     * @return              Node that represents the given key
     */
    private Node getNode(String key, int keyIndex, Node parentNode)
    {
        // base case, if the whole string has been iterated through return that node
        if(key.length() == keyIndex)
        {
            return parentNode;
        }
        
        // if child is null, key not found
        if(parentNode.getChild() == null)
        {
            return null;
        }
        
        // Search for current key
        Node currentNode = parentNode.getChild();
        while((currentNode.hasKey(key.charAt(keyIndex)) == false) && (currentNode.getSibling() != null))
        {
            currentNode = currentNode.getSibling();
        }
        
        // key is in currentNode or key is not in the Trie
        if(currentNode.hasKey(key.charAt(keyIndex)))
        {
            return getNode(key, keyIndex + 1, currentNode);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Fills prioritySet with the least StringValue pairings in the sub-trie represented by prefix and node. Least is
     * defined by StringValue.compareTo
     *
     * @param prioritySet   An array to add StringValue mappings to
     * @param prefix        Represents the path of nodes taken to get to node
     * @param node          The current node that is the root of the sub-trie that is being searched through
     */
    private void fillSet(StringValue[] prioritySet, String prefix, Node node)
    {
        if(node == null)
        {
            return;
        }
        
        // Add to the prefix the current Node's key to find the String represented by node
        String s = prefix + Character.toString(node.getKey());
        // If value is not null, node represents an entry in the trie
        if(node.getValue() != null)
        {
            StringValue item = new StringValue(s, node.getValue());
            int index = 0;
            
            // search for the place to add the new entry into prioritySet
            // Note: the items in prioritySet may be null, compareTo is properly implemented to handle null values and
            //       the code is written to not access a member of any objects in prioritySet.
            while((index < prioritySet.length) && (item.compareTo(prioritySet[index]) > 0))
            {
                index++;
            }
            
            // Swap entries in prioritySet down to insert the new entry
            while(index < prioritySet.length)
            {
                StringValue temp = prioritySet[index];
                prioritySet[index] = item;
                item = temp;
                index++;
            }
        }
        
        // Recursively search through children, then siblings
        fillSet(prioritySet, s, node.getChild());
        fillSet(prioritySet, prefix, node.getSibling());
    }
    
    /**
     * Recursively find and writes all entries to fileWriter. Implemented equivalently to fillSet but does not consider
     * the order of returned mappings
     *
     * @param fileWriter    The FileWriter being written to
     * @param prefix        Represents the path of nodes taken to get to node
     * @param node          The current node that is the root of the sub-trie that is being searched through
     * @throws IOEception   Throws whenever FileWriter.append would throw
     */
    private void write(FileWriter fileWriter, String prefix, Node node) throws IOException
    {
        if(node == null)
        {
            return;
        }
        
        String s = prefix + Character.toString(node.getKey());
        if(node.getValue() != null)
        {
            fileWriter.append(s).append("\t").append(node.getValue().toString()).append("\r\n");
        }
        
        write(fileWriter, s, node.getChild());
        write(fileWriter, prefix, node.getSibling());
    }
    
    
    /**
     * Internal Node class for constucting the Trie
     */
    class Node
    {
        private Node m_Child;
        private Node m_Sibling;
        private char m_Key;
        private Integer m_Value;
        
        public Node(char key)
        {
            m_Child = null;
            m_Sibling = null;
            m_Key = key;
            m_Value = null;
        }
        
        public void setChild(Node node)
        {
            m_Child = node;
        }
        
        public Node getChild()
        {
            return m_Child;
        }
        
        public void setSibling(Node node)
        {
            m_Sibling = node;
        }
        
        public Node getSibling()
        {
            return m_Sibling;
        }
        
        public void setVlaue(Integer value)
        {
            m_Value = value;
        }
        
        public Integer getValue()
        {
            return m_Value;
        }
        
        public char getKey()
        {
            return m_Key;
        }
        
        public boolean hasKey(char key)
        {
            return (key == m_Key);
        }
    }
    
    /**
     * Internal class for returning mappings of the trie. This object is not used for storage purposes
     */
    class StringValue implements Comparable<StringValue>
    {
        private String s;
        private Integer v;
        
        public StringValue(String s, Integer v)
        {
            this.s = s;
            this.v = v;
        }
        
        public String getString()
        {
            return s;
        }
        
        public Integer getValue()
        {
            return v;
        }
        
        public int compareTo(StringValue o)
        {
            if(o == null)
            {
                return -1;
            }
            
            // If the values are equivalent, compare Strings. Otherwise compare values
            if(o.v.equals(this.v))
            {
                return this.s.compareTo(o.s);
            }
            else
            {
                // returns a negative number for o.v > this.v
                return o.v.compareTo(this.v);
            }
        }
    }
    
    /**
     * Runs the main application
     */
    public static void main(String... args) throws Exception
    {
        // The trie maps words (Strings) to the number of times that the user has written that string
        ac_test dlbTrie = new ac_test();
        
        // If userhistory exists, use that. Otherwise start fresh with the dictionary
        File userHistory = new File("user_history.txt");
        if(userHistory.isFile())
        {
            new BufferedReader(new FileReader(userHistory)).lines().forEach(s -> dlbTrie.put(s.split("\t")[0], new Integer(s.split("\t")[1])));
        }
        else
        {
            new BufferedReader(new FileReader("dictionary.txt")).lines().forEach(s -> dlbTrie.put(s, 0));
        }
        
        Scanner userInput = new Scanner(System.in);
        String string = ""; // current input
        boolean isFirstChar = true;
        boolean isFinished = false;
        
        double averageTime = 0;
        double measurements = 0;
        
        // main application loop. Exists on '!'
        while(isFinished == false)
        {
            if(isFirstChar == true)
            {
                System.out.print("Enter your first character: ");
                isFirstChar = false;
            }
            else
            {
                System.out.print("Enter the next character: ");
            }
            
            // Get user input
            String nextChar = userInput.next();
            switch(nextChar)
            {
                case "!":
                {
                    // Exit the app
                    isFinished = true;
                    System.out.println();
                    System.out.println();
                }
                break;
                
                case "1":
                case "2":
                case "3":
                case "4":
                case "5":
                {
                    // Get the get the suggestions from the trie. Should be the same suggestions as last loop because
                    // string has not been modified since the last loop.
                    
                    int index = Integer.parseInt(nextChar) - 1;
                    StringValue[] suggestions = dlbTrie.getPriorityPrefixSet(string);
                    
                    System.out.println(suggestions[index].getString());
                    
                    // re add the string with a value 1 greater
                    dlbTrie.put(suggestions[index].getString(), suggestions[index].getValue() + 1);
                    
                    System.out.println();
                    System.out.println();
                    System.out.println("WORD COMPLETED: " + suggestions[index].getString());
                    System.out.println();
                    
                    // reset input
                    isFirstChar = true;
                    string = "";
                }
                break;
                
                case "$":
                {
                    // If the finished string exists add 1 to its value. If the string does not exist, add it to the trie
                    
                    Integer value = dlbTrie.get(string);
                    if(value == null)
                    {
                        dlbTrie.put(string, 1);
                    }
                    else
                    {
                        dlbTrie.put(string, value + 1);
                    }
                    
                    System.out.println();
                    System.out.println();
                    System.out.println("WORD COMPLETED: " + string);
                    System.out.println();
                    
                    // reset input
                    isFirstChar = true;
                    string = "";
                }
                break;
                
                default:
                {
                    // for any other character add it to the string, then search and present the suggestions
                    string += nextChar;
                    
                    long before = System.nanoTime();
                    StringValue[] suggestions = dlbTrie.getPriorityPrefixSet(string);
                    long duration = System.nanoTime() - before;
                    double elapsedTimeSeconds = (double)duration / 1000000000.0;
                    
                    // keep an average of access times for display on app exit
                    averageTime = ((averageTime * measurements) + elapsedTimeSeconds) / (measurements + 1);
                    measurements += 1;
                    
                    System.out.println("\n(" + elapsedTimeSeconds + " s)");
                    System.out.println("Predications:");
                    
                    int index = 0;
                    while((index < suggestions.length) && (suggestions[index] != null))
                    {
                        System.out.print("(" + (index + 1) + ") " + suggestions[index].getString() + "\t");
                        index++;
                    }
                    
                    System.out.println();
                    System.out.println();
                    System.out.println();
                }
                break;
            }
        }
        
        // present average access time
        System.out.println("Average Time: " + averageTime + " s");
        
        // Write the Trie to file
        FileWriter fileWriter = new FileWriter(userHistory);
        dlbTrie.writeObject(fileWriter);
        fileWriter.close();
    }
}