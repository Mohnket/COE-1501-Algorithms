/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

public class MyLZW
{
    private static final int BITS_PER_CHAR = 8;
    private static final int RADIX = 256;                       // number of input chars
    
    private static final int CODEWORDS_RESET = 512;
    private static int m_MaxCodeWords = CODEWORDS_RESET;        // number of codewords = 2^m_CodeWordWidth
    
    private static final int MAX_CODEWORD_WIDTH = 16;           // Max width for codewords
    private static final int CODEWORD_WIDTH_RESET = 9;
    private static int m_CodeWordWidth = CODEWORD_WIDTH_RESET;  // codeword width

    private static long m_UncompressedBits = 0;
    private static long m_CompressedBits = 0;
    private static double m_InitialCompressionRatio = 0.0;
    private static final double RATIO_RESET_THRESHHOLD = 1.1;
    
    public static void compress(Mode mode)
    { 
        BinaryStdOut.write(mode.toString(), 8);
    
        String input = BinaryStdIn.readString();
        TST<Integer> symbolTable = new TST<Integer>();
        for(int i = 0; i < RADIX; i++)
        {
            symbolTable.put("" + (char) i, i);
        }
            
        int code = RADIX + 1;   // The value of RADIX is the codeword for EOF

        while(input.length() > 0)
        {
            String prefix = symbolTable.longestPrefixOf(input);             // Find max prefix match of the input.
            BinaryStdOut.write(symbolTable.get(prefix), m_CodeWordWidth);   // Print the prefix's encoding.
            
            m_UncompressedBits += prefix.length() * BITS_PER_CHAR;
            m_CompressedBits += m_CodeWordWidth;
            
            // Expand if needed
            if(code == m_MaxCodeWords && m_CodeWordWidth <= MAX_CODEWORD_WIDTH)
            {
                switch(mode)
                {
                    case Normal:
                    {
                        if(m_CodeWordWidth < MAX_CODEWORD_WIDTH)
                        {
                            m_CodeWordWidth++;
                            m_MaxCodeWords *= 2;
                        }
                    }
                    break;
                    
                    case Reset:
                    {
                        if(m_CodeWordWidth == MAX_CODEWORD_WIDTH)
                        {
                            m_MaxCodeWords = CODEWORDS_RESET;
                            m_CodeWordWidth = CODEWORD_WIDTH_RESET;
                            code = RADIX + 1;
                            
                            symbolTable = new TST<Integer>();
                            for(int i = 0; i < RADIX; i++)
                            {
                                symbolTable.put("" + (char) i, i);
                            }
                        }
                        else
                        {
                            m_CodeWordWidth++;
                            m_MaxCodeWords *= 2;
                        }
                    }
                    break;
                    
                    case Monitor:
                    {
                        if(m_CodeWordWidth == MAX_CODEWORD_WIDTH)
                        {
                            if(m_InitialCompressionRatio == 0.0)
                            {
                                m_InitialCompressionRatio = (double)m_UncompressedBits/(double)m_CompressedBits;
                            }
                            else
                            {
                                double ratioOfRatios = m_InitialCompressionRatio/((double)m_UncompressedBits/(double)m_CompressedBits);
                                if(ratioOfRatios > RATIO_RESET_THRESHHOLD)
                                {
                                    m_MaxCodeWords = CODEWORDS_RESET;
                                    m_CodeWordWidth = CODEWORD_WIDTH_RESET;
                                    code = RADIX + 1;
                                    
                                    symbolTable = new TST<Integer>();
                                    for(int i = 0; i < RADIX; i++)
                                    {
                                        symbolTable.put("" + (char) i, i);
                                    }
                                    
                                    m_InitialCompressionRatio = 0.0;
                                }
                            }
                        }
                        else
                        {
                            m_CodeWordWidth++;
                            m_MaxCodeWords *= 2;
                        }
                    }
                    break;
                }
                
            }
            
            int prefixLength = prefix.length();
            if(prefixLength < input.length() && code < m_MaxCodeWords)      // Add prefix + the next input char to the symbol table
            {
                symbolTable.put(input.substring(0, prefixLength + 1), code++);
            }
                
            input = input.substring(prefixLength);                          // Scan past prefix in input.
        }
        
        BinaryStdOut.write(RADIX, m_CodeWordWidth);
        BinaryStdOut.close();
    } 

    public static void expand()
    {
        String[] symbolTable = new String[1 << MAX_CODEWORD_WIDTH];
        int index; // next available codeword value

        // initialize symbol table with all 1-character strings
        for(index = 0; index < RADIX; index++)
        {
            symbolTable[index] = "" + (char) index;
        }
            
        symbolTable[index++] = "";                        // (unused) lookahead for EOF

        Mode mode = Mode.getMode(Character.toString(BinaryStdIn.readChar()));
        int codeword = BinaryStdIn.readInt(m_CodeWordWidth);
        
        if (codeword == RADIX)
        {
            return;                                     // expanded message is empty string
        }
        String writeValue = symbolTable[codeword];
        
        m_UncompressedBits += writeValue.length() * BITS_PER_CHAR;
        m_CompressedBits += m_CodeWordWidth;
        while(true)
        {
            BinaryStdOut.write(writeValue);
            codeword = BinaryStdIn.readInt(m_CodeWordWidth);
            if(codeword == RADIX)
            {
                break;
            }   
            
            String currentString = symbolTable[codeword];
            
            if(index == codeword) // special case hack
            {
                currentString = writeValue + writeValue.charAt(0);   
            }
            
            if(index < m_MaxCodeWords)
            {
                symbolTable[index++] = writeValue + currentString.charAt(0);
            }
            
            m_UncompressedBits += currentString.length() * BITS_PER_CHAR;
            m_CompressedBits += m_CodeWordWidth;
            
            // expand if needed
            if(index == m_MaxCodeWords && m_CodeWordWidth <= MAX_CODEWORD_WIDTH)
            {
                switch(mode)
                {
                    case Normal:
                    {
                        if(m_CodeWordWidth < MAX_CODEWORD_WIDTH)
                        {
                            m_CodeWordWidth++;
                            m_MaxCodeWords *= 2;
                        }
                    }
                    break;
                    
                    case Reset:
                    {
                        if(m_CodeWordWidth == MAX_CODEWORD_WIDTH)
                        {
                            m_MaxCodeWords = CODEWORDS_RESET;
                            m_CodeWordWidth = CODEWORD_WIDTH_RESET;
                            index = RADIX + 1;
                            
                            for(int ii = index; ii < symbolTable.length; ++ii)
                            {
                                symbolTable[ii] = null;
                            }
                        }
                        else
                        {
                            m_CodeWordWidth++;
                            m_MaxCodeWords *= 2;
                        }
                    }
                    break;
                    
                    case Monitor:
                    {
                        if(m_CodeWordWidth == MAX_CODEWORD_WIDTH)
                        {
                            if(m_InitialCompressionRatio == 0.0)
                            {
                                m_InitialCompressionRatio = (double)m_UncompressedBits/(double)m_CompressedBits;
                            }
                            else
                            {
                                double ratioOfRatios = m_InitialCompressionRatio/((double)m_UncompressedBits/(double)m_CompressedBits);
                                if(ratioOfRatios > RATIO_RESET_THRESHHOLD)
                                {
                                    m_MaxCodeWords = CODEWORDS_RESET;
                                    m_CodeWordWidth = CODEWORD_WIDTH_RESET;
                                    index = RADIX + 1;
                                    
                                    for(int ii = index; ii < symbolTable.length; ++ii)
                                    {
                                        symbolTable[ii] = null;
                                    }
                                    
                                    m_InitialCompressionRatio = 0.0;
                                }
                            }
                        }
                        else
                        {
                            m_CodeWordWidth++;
                            m_MaxCodeWords *= 2;
                        }
                    }
                    break;
                }
               
            }
            
            writeValue = currentString;
        }
        
        BinaryStdOut.close();
    }

    public static void main(String[] args)
    {
        if (args[0].equals("-"))
        {
            compress(Mode.getMode(args[1]));
        }
        else if(args[0].equals("+"))
        {
            expand();
        }
        else
        {
            throw new IllegalArgumentException("Illegal command line argument");
        }
    }
}

enum Mode
{
    Normal("n"), Reset("r"), Monitor("m");
    
    private String m_String;
    
    Mode(String string)
    {
        m_String = string;
    }
    
    static Mode getMode(String string) throws IllegalArgumentException
    {
        switch(string)
        {
            case "n":
                return Normal;
            case "r":
                return Reset;
            case "m":
                return Monitor;
            default:
                throw new IllegalArgumentException("Illegal command line argument");
        }
    }
    
    public String toString()
    {
        return m_String;
    }
}