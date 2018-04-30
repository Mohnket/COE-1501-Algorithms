import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.io.File;

import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedReader;

import java.util.ArrayList;
import java.util.Arrays;

public class RsaSign
{    
    public static void main(String... args) throws Exception
    {
        File check = new File(args[1]);
        if(check.exists() == false)
        {
            System.out.printf("%s not found\n", args[1]);
            System.exit(1);
        }
        
        if(args[0].equals("s"))
        {
            check = new File("privkey.rsa");
            if(check.exists() == false)
            {
                System.out.printf("%s not found\n", "privkey.rsa");
                System.exit(1);
            }
            
            LargeInteger value = new LargeInteger(hash(args[1]));
            LargeInteger[] temp = getKeyFromFile("privkey.rsa");
            LargeInteger key = temp[0];
            LargeInteger mod = temp[1];
            
            LargeInteger signature = value.modularExp(key, mod);
            
            PrintWriter output = new PrintWriter(args[1] + ".sig");
            output.println(signature.toString());
            output.close();
        }
        else if(args[0].equals("v"))
        {
            check = new File("pubkey.rsa");
            if(check.exists() == false)
            {
                System.out.printf("%s not found\n", "privkey.rsa");
                System.exit(1);
            }
            
            check = new File(args[1] + ".sig");
            if(check.exists() == false)
            {
                System.out.printf("%s not found\n", args[1] + ".sig");
                System.exit(1);
            }
            
            LargeInteger value = new LargeInteger(hash(args[1]));
            LargeInteger[] temp = getKeyFromFile("pubkey.rsa");
            LargeInteger key = temp[0];
            LargeInteger mod = temp[1];
            
            LargeInteger encryptedValue = getIntegerFromFile(args[1] + ".sig");
            LargeInteger decryptedValue = encryptedValue.modularExp(key, mod);
            
            boolean equal = false;
            // The decryptedValue will usually have an extra 0 padding byte for whatever reason.
            if((decryptedValue.getVal()[0] == 0) && (value.length() == decryptedValue.length() - 1))
            {
                byte[] unpaddedVal = Arrays.copyOfRange(decryptedValue.getVal(), 1, decryptedValue.length());
                equal = Arrays.equals(value.getVal(), unpaddedVal);
            }
            else
            {
                equal = Arrays.equals(value.getVal(), decryptedValue.getVal());
            }
            
            
            System.out.println(equal ? "Valid" : "Invalid");
        }
        else
        {
            
        }
    }
    
    private static LargeInteger getIntegerFromFile(String fileName) throws Exception
    {
        BufferedReader file = new BufferedReader(new FileReader(fileName));
        
        ArrayList<Byte> buffer = new ArrayList<Byte>();
        
        String[] bytes = file.readLine().split(" ");
        for(String token : bytes)
        {
            buffer.add(Byte.valueOf(token));
        }
        file.close();
        
        return new LargeInteger(buffer.toArray(new Byte[0]));
    }
    
    private static LargeInteger[] getKeyFromFile(String fileName) throws Exception
    {
        BufferedReader file = new BufferedReader(new FileReader(fileName));
        
        ArrayList<Byte> buffer = new ArrayList<Byte>();
        
        String[] bytes = file.readLine().split(" ");
        for(String token : bytes)
        {
            buffer.add(Byte.valueOf(token));
        }
        LargeInteger key = new LargeInteger(buffer.toArray(new Byte[0]));
        
        buffer = new ArrayList<Byte>();
        bytes = file.readLine().split(" ");
        for(String token : bytes)
        {
            buffer.add(Byte.valueOf(token));
        }
        file.close();
        
        LargeInteger mod = new LargeInteger(buffer.toArray(new Byte[0]));
        
        return new LargeInteger[]{key, mod};
    }
    
    private static byte[] hash(String fileName)
    {
        byte[] retval = null;
        
        // lazily catch all exceptions...
		try
        {
			// read in the file to hash
			Path path = Paths.get(fileName);
			byte[] data = Files.readAllBytes(path);

			// create class instance to create SHA-256 hash
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			// process the file
			md.update(data);
			// generate a has of the file
			retval = md.digest();
		} 
        catch(Exception e)
        {
			System.out.println(e.toString());
		}
        
        return retval;
    }
}