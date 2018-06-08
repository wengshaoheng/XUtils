package wsh.io.edc;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * a Number helper
 * @author Shaoheng.Weng (Mars)
 */
public class XNumber {
	
	static  int AUTO_COPY_SIZE = -1;
	
	public static long byteArrayToLong(byte[] data) {
		return byteArrayToLong(data, 0);
	}
	
	public static long byteArrayToLong(byte[] data, int offset) {
		return byteArrayToLong(data, offset, AUTO_COPY_SIZE);
	}
	
	public static long byteArrayToLong(byte[] data, int offset, int size) {
		return translateByteArrayToNumber(Long.class, data, offset, size);
	}
	
	public static long byteArrayToInteger(byte[] data) {
		return byteArrayToLong(data, 0);
	}
	
	public static long byteArrayToInteger(byte[] data, int offset) {
		return byteArrayToLong(data, offset, AUTO_COPY_SIZE);
	}
	
	public static int byteArrayToInteger(byte[] data, int offset, int size) {
		return translateByteArrayToNumber(Integer.class, data, offset, size);
	}
	
	public static long byteArrayToBigInteger(byte[] data) {
		return byteArrayToLong(data, 0);
	}
	
	public static long byteArrayToBigInteger(byte[] data, int offset) {
		return byteArrayToLong(data, offset, AUTO_COPY_SIZE);
	}
	
	public static BigInteger byteArrayToBigInteger(byte[] data, int offset, int size) {
		return translateByteArrayToNumber(BigInteger.class, data, offset, size);
	}
	
	public static long byteArrayToShort(byte[] data) {
		return byteArrayToLong(data, 0);
	}
	
	public static long byteArrayToShort(byte[] data, int offset) {
		return byteArrayToLong(data, offset, AUTO_COPY_SIZE);
	}
	
	public static short byteArrayToShort(byte[] data, int offset, int size) {
		return translateByteArrayToNumber(Short.class, data, offset, size);
	}
	
	/**
	 * 
	 * @param numClz
	 * @param data
	 * @param offset
	 * @return
	 */
	public static <T extends Number> T translateByteArrayToNumber(
			Class<T> numClz, byte[] data, int offset, int size) {

		try {
			if (numClz == Double.class || numClz == Float.class || numClz == BigDecimal.class) {
				throw new RuntimeException(numClz.getName() + " is not supported!");
			}
			
			T dummyObj = numClz.getConstructor(String.class).newInstance("0");

			if (data == null || data.length == 0) return dummyObj;
			
			int _bytes = (numClz != BigInteger.class) ? 
					(Integer)numClz.getField("BYTES").get(dummyObj) : 
					(Integer.MAX_VALUE / Integer.SIZE + 1);

			int copySize = (size == AUTO_COPY_SIZE) ? (data.length - offset) : size;
			if (copySize < 1) return dummyObj;
			if (copySize > _bytes) copySize = _bytes;
			
			byte[] dataBuffer = new byte[copySize];
			System.arraycopy(data, offset, dataBuffer, 0, copySize);
			
			if (numClz == BigInteger.class) {
				return numClz.getConstructor(byte[].class).newInstance(dataBuffer);
			}
			
			long template = 0L;
			
			int shiftFactor = copySize - 1;
			for (int i = 0; i < copySize; ++i) {
				template ^= ((dataBuffer[i] & 0xffL) << (shiftFactor * Byte.SIZE));
				--shiftFactor;
			}
			
			return numClz.getConstructor(String.class).newInstance(String.valueOf(template));
			
		} catch (Exception e) {
			throw new RuntimeException("failed to tansfer", e);
		}
	}

}
