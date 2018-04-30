import java.util.Random;
import java.io.PrintWriter;

public class RsaKeyGen
{
    public static final int LENGTH = 256;
    public static Random random = new Random();
    
    public static void generate()
    {
        LargeInteger p = new LargeInteger(LENGTH, random);
        LargeInteger q = new LargeInteger(LENGTH, random);
        
        LargeInteger n = p.multiply(q);
        LargeInteger phi = p.subtract(LargeInteger.ONE_INTEGER).multiply(q.subtract(LargeInteger.ONE_INTEGER));
        
        LargeInteger e = new LargeInteger(2 * LENGTH, random);
        
        // I could probably write a smarter xgcd that always returns a positive d value, but this is easier
        LargeInteger[] xgcd = e.XGCD(phi);        
        while((xgcd[0].isOne() == false) || (xgcd[1].isNegative() == true))
        {
            e = new LargeInteger(LENGTH * 2 - 1, random);
            xgcd = e.XGCD(phi);
        }
        
        LargeInteger d = xgcd[1];        
        try
        {
            PrintWriter publicKey = new PrintWriter("pubkey.rsa");
            publicKey.println(e.toString());
            publicKey.println(n.toString());
            publicKey.close();
            
            PrintWriter privateKey = new PrintWriter("privkey.rsa");
            privateKey.println(d.toString());
            privateKey.println(n.toString());
            privateKey.close();
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
        
    }
    
    public static void main(String... args)
    {
        generate();
    }
}