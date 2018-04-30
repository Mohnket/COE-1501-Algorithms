import java.util.Random;
import java.math.BigInteger;

import java.util.Arrays;
import java.util.BitSet;

public class LargeInteger {
	
	private final static byte[] ONE = {(byte) 1};
	private final static byte[] ZERO = {(byte) 0};
	public final static LargeInteger ONE_INTEGER = new LargeInteger(ONE);
	public final static LargeInteger ZERO_INTEGER = new LargeInteger(ZERO);

	private byte[] val;

	/**
	 * Construct the LargeInteger from a given byte array
	 * @param b the byte array that this LargeInteger should represent
	 */
	public LargeInteger(byte[] b) {
		val = b;
	}
    
    /**
	 * Construct the LargeInteger from a given Byte array
	 * @param b the Byte array that this LargeInteger should represent
	 */
	public LargeInteger(Byte[] b) {
        byte[] temp = new byte[b.length];
        for(int index = 0; index < b.length; ++index)
        {
            temp[index] = b[index];
        }
        
		val = temp;
	}

	/**
	 * Construct the LargeInteger by generatin a random n-bit number that is
	 * probably prime (2^-100 chance of being composite).
	 * @param n the bitlength of the requested integer
	 * @param rnd instance of java.util.Random to use in prime generation
	 */
	public LargeInteger(int n, Random rnd) {
		val = BigInteger.probablePrime(n, rnd).toByteArray();
	}
	
	/**
	 * Return this LargeInteger's val
	 * @return val
	 */
	public byte[] getVal() {
		return val;
	}

	/**
	 * Return the number of bytes in val
	 * @return length of the val byte array
	 */
	public int length() {
		return val.length;
	}

	/** 
	 * Add a new byte as the most significant in this
	 * @param extension the byte to place as most significant
	 */
	public void extend(byte extension) {
		byte[] newv = new byte[val.length + 1];
		newv[0] = extension;
		for (int i = 0; i < val.length; i++) {
			newv[i + 1] = val[i];
		}
		val = newv;
	}

	/**
	 * If this is negative, most significant bit will be 1 meaning most 
	 * significant byte will be a negative signed number
	 * @return true if this is negative, false if positive
	 */
	public boolean isNegative() {
		return (val[0] < 0);
	}

	/**
	 * Computes the sum of this and other
	 * @param other the other LargeInteger to sum with this
	 */
	public LargeInteger add(LargeInteger other) {
        
		byte[] a, b;
		// If operands are of different sizes, put larger first ...
		if (val.length < other.length()) {
			a = other.getVal();
			b = val;
		}
		else {
			a = val;
			b = other.getVal();
		}

		// ... and normalize size for convenience
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFF;
			}

			byte[] newb = new byte[a.length];
			for (int i = 0; i < diff; i++) {
				newb[i] = pad;
			}

			for (int i = 0; i < b.length; i++) {
				newb[i + diff] = b[i];
			}

			b = newb;
		}

		// Actually compute the add
		int carry = 0;
		byte[] res = new byte[a.length];
		for (int i = a.length - 1; i >= 0; i--) {
			// Be sure to bitmask so that cast of negative bytes does not
			//  introduce spurious 1 bits into result of cast
			carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

			// Assign to next byte
			res[i] = (byte) (carry & 0xFF);

			// Carry remainder over to next byte (always want to shift in 0s)
			carry = carry >>> 8;
		}

		LargeInteger res_li = new LargeInteger(res);
	
		// If both operands are positive, magnitude could increase as a result
		//  of addition
		if (!this.isNegative() && !other.isNegative()) {
			// If we have either a leftover carry value or we used the last
			//  bit in the most significant byte, we need to extend the result
			if (res_li.isNegative()) {
				res_li.extend((byte) carry);
			}
		}
		// Magnitude could also increase if both operands are negative
		else if (this.isNegative() && other.isNegative()) {
			if (!res_li.isNegative()) {
				res_li.extend((byte) 0xFF);
			}
		}

		// Note that result will always be the same size as biggest input
		//  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
		return res_li;
	}

	/**
	 * Negate val using two's complement representation
	 * @return negation of this
	 */
	public LargeInteger negate() {
		byte[] neg = new byte[val.length];
		int offset = 0;

		// Check to ensure we can represent negation in same length
		//  (e.g., -128 can be represented in 8 bits using two's 
		//  complement, +128 requires 9)
		if (val[0] == (byte) 0x80) { // 0x80 is 10000000
			boolean needs_ex = true;
			for (int i = 1; i < val.length; i++) {
				if (val[i] != (byte) 0) {
					needs_ex = false;
					break;
				}
			}
			// if first byte is 0x80 and all others are 0, must extend
			if (needs_ex) {
				neg = new byte[val.length + 1];
				neg[0] = (byte) 0;
				offset = 1;
			}
		}

		// flip all bits
		for (int i  = 0; i < val.length; i++) {
			neg[i + offset] = (byte) ~val[i];
		}

		LargeInteger neg_li = new LargeInteger(neg);
	
		// add 1 to complete two's complement negation
		return neg_li.add(new LargeInteger(ONE));
	}

	/**
	 * Implement subtraction as simply negation and addition
	 * @param other LargeInteger to subtract from this
	 * @return difference of this and other
	 */
	public LargeInteger subtract(LargeInteger other) {
		return this.add(other.negate());
	}

	/**
	 * Compute the product of this and other.
     *
     * So I was talking to another guy in class about how I did this and he was convinced that this wasn't the 'grade
     * school' algorithm WHICH IS BULLSHIT because this is litterally how you would multiply two numbers with pencil
     * and paper. The only difference is that the number is base 256 instead of base 10, other than that everything is
     * the same. You multiply the entirety of by a single digit of the other, then add 0s to express the maginitude, and
     * then keep track of a runnning some of single digit multiplies. It's that easy. Doesn't work with negative numbers
     * for some reason, but that's solved by negating before and after the multiply.
     *
	 * @param other LargeInteger to multiply by this
	 * @return product of this and other
	 */
	public LargeInteger multiply(LargeInteger other)
    {
        LargeInteger retval = new LargeInteger(new byte[1]);
        LargeInteger lhs = this;
        
        int length = other.length();
        
        boolean parity = false;
        if(lhs.isNegative())
        {
            parity = !parity;
            lhs = lhs.negate();
        }
        if(other.isNegative())
        {
            parity = !parity;
            other = other.negate();
        }
        
        for(int index = 0; index < length; ++index)
        {
            byte current = other.val[length - 1 - index];
            retval = retval.add(lhs.multiply(current).byteLeftShift(index));
        }
        
        if(parity == true)
        {
            retval = retval.negate();
        }
        
		return retval;
	}
    
    /**
	 * Compute the product of this and a single byte.
     *
	 * @param byteLiteral   The byte to multiply by. Treated as an unsigned value.
	 * @return LargeInteger The resulting value
     */
    private LargeInteger multiply(byte byteLiteral)
    {
        byte[] retval = new byte[val.length + 1];
        int carry = 0;
        for(int index = val.length - 1; index >= 0; --index)
        {
            int lhs = val[index] & 0xFF;
            int rhs = byteLiteral & 0xFF;
            int value = (lhs * rhs) + carry;
            carry = (value >>> 8) & 0xFF;
            retval[index + 1] = (byte)(value & 0xFF);
        }
        retval[0] = (byte)(carry & 0xFF);
        
        LargeInteger returnValue = new LargeInteger(retval);        
        return new LargeInteger(retval);
    }
    
    /**
	 * Left shifts this value some bytes over.
     *
	 * @param shiftAmount   The number of bytes to shift over.
	 * @return LargeInteger The resulting value
     */
    private LargeInteger byteLeftShift(int shiftAmount)
    {
        byte[] retval = new byte[val.length + shiftAmount];
        for(int index = 0; index < val.length; ++index)
        {
            retval[index] = val[index];
        }
        
        LargeInteger returnValue = new LargeInteger(retval);
        returnValue.reduce();
        
        return returnValue;
    }
    
    /**
	 * Removes leading 0s or Fs from this
     *
	 * @return LargeInteger The resulting value
     */
    private void reduce()
    {
        if((val[0] == (byte)0xFF) || (val[0] == 0))
        {
            int count = 0;
            while((count <  val.length - 1) && val[count] == val[count + 1])
            {
                count++;
            }
            byte[] shortened = new byte[val.length - count];
            for(int index = 0; index < shortened.length; index++)
            {
                shortened[index] = val[index + count];
            }
            val = shortened;
        }
    }
    
	/**
	 * Run the extended Euclidean algorithm on this and other
	 * @param other another LargeInteger
	 * @return an array structured as follows:
	 *   0:  the GCD of this and other
	 *   1:  a valid x value
	 *   2:  a valid y value
	 * such that this * x + other * y == GCD in index 0
	 */
    public LargeInteger[] XGCD(LargeInteger other)
    {
        // Implementation of https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm#Pseudocode
        
        LargeInteger x = ZERO_INTEGER;
        LargeInteger old_x = ONE_INTEGER;
        LargeInteger y = ONE_INTEGER;
        LargeInteger old_y = ZERO_INTEGER;
        LargeInteger r = other;
        LargeInteger old_r = this;
        
        while(r.isZero() == false)
        {
            LargeInteger quotient = old_r.divide(r)[0];
            
            LargeInteger temp = r;
            r = old_r.subtract(quotient.multiply(r));
            old_r = temp;
            
            temp = x;
            x = old_x.subtract(quotient.multiply(x));
            old_x = temp;
            
            temp = y;
            y = old_y.subtract(quotient.multiply(y));
            old_y = temp;
        }

        return new LargeInteger[]{old_r, old_x, old_y};
    }

    /**
	 * only works for positive numbers? (maybe???)
     *
	 * @return LargeInteger[]   Index 0 is result index 1 is remainder
     */
    public LargeInteger[] divide(LargeInteger other)
    {
        if(other.isZero())
        {
            return null;
        }
        
        if(this.isZero())
        {
            return new LargeInteger[]{this, this};
        }
        
        LargeInteger retval = new LargeInteger(new byte[1]);
        LargeInteger copy = this;
        
        // repeatedly subtract other from this, but with magnitude shifts of other to converge faster
        int shiftAmount = this.length() - other.length();
        if(shiftAmount < 0)
        {
            shiftAmount = 0;
        }
        
        while(shiftAmount >= 0)
        {   
            LargeInteger adding = ONE_INTEGER.byteLeftShift(shiftAmount);
            LargeInteger subtracting = other.byteLeftShift(shiftAmount);
            
            LargeInteger afterSubtraction = copy.subtract(subtracting);
            while(afterSubtraction.isNegative() == false)
            {
                retval = retval.add(adding);
                copy = afterSubtraction;
                afterSubtraction = copy.subtract(subtracting);
            }
            
            shiftAmount--;
        }
        
        retval.reduce();
        copy.reduce();
        
        return new LargeInteger[]{retval, copy};
    }
    
    /**
	 * @return boolean  true if this value is 0
     */
    public boolean isZero()
    {
        for(int index = 0; index < val.length; ++index)
        {
            if(val[index] != 0)
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
	 * @return boolean  true if this value is 1
     */
    public boolean isOne()
    {
        if(val[val.length - 1] == 1)
        {
            for(int index = val.length - 2; index >= 0; --index)
            {
                if(val[index] != 0)
                {
                    return false;
                }
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Only works for positive values
	 * @return boolean  true if this value is greaterThan other's value
     */
    public boolean greaterThan(LargeInteger other)
    {
        while(this.length() < other.length())
        {
            this.extend((byte)0);
        }
        
        while(other.length() < this.length())
        {
            other.extend((byte)0);
        }
        
        for(int index = val.length - 1; index >= 0; --index)
        {
            if(this.val[index] == other.val[index])
            {
                continue;
            }
            else
            {
                return this.val[index] > other.val[index];
            }
        }
        return false;
    }

    /**
     * Compute the result of raising this to the power of y mod n
     * @param y exponent to raise this to
     * @param n modulus value to use
     * @return this^y mod n
     */
    public LargeInteger modularExp(LargeInteger y, LargeInteger n)
    {
        LargeInteger result = ONE_INTEGER;
        
        byte[] littleEndian = new byte[y.length()];
        for(int index = 0; index < littleEndian.length; ++index)
        {
            littleEndian[index] = y.val[y.val.length - 1 - index];
        }
        
        BitSet bits = BitSet.valueOf(littleEndian);
        for(int index = bits.length() - 1; index >= 0; --index)
        {
            result = result.multiply(result).divide(n)[1];
            result = result.divide(n)[1];
            
            if(bits.get(index) == true)
            {
                result = result.multiply(this);
                result = result.divide(n)[1];
            }
        }
        
        return result;
    }
     
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for(int index = 0; index < val.length; ++index)
        {
            builder.append(String.format("%d", val[index])).append(" ");
        }
        return builder.toString();
        
        // return new BigInteger(val).toString();
    }
}
