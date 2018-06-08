package wsh.io.edc;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * a Number helper
 * @author Shaoheng.Weng (Mars)
 */
public class XNumber {
	
	/**
	 * 
	 * @param numClz
	 * @param data
	 * @param offset
	 * @return
	 */
	public static <T extends Number> T translateByteArrayToNumber(
			Class<T> numClz, byte[] data, int offset) {

		try {
			if (numClz == Double.class || numClz == Float.class || numClz == BigDecimal.class) {
				throw new RuntimeException(numClz.getName() + " is not supported!");
			}
			
			T dummyObj = numClz.getConstructor(String.class).newInstance("0");

			if (data == null || data.length == 0) return dummyObj;
			
			int _bytes = (numClz != BigInteger.class) ? 
					(Integer)numClz.getField("BYTES").get(dummyObj) : 
					(Integer.MAX_VALUE / Integer.SIZE + 1);

			int copySize = data.length - offset;
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
