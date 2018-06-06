package wsh.io.edc;

/**
 * a Base64 encoder and decoder
 * @author Shaoheng.Weng (Mars)
 */
public class XBase64 {

	static final int _base_A_Z = 0;
	static final int _base_a_z = 26;
	static final int _base_0_9 = 52;
	static final int _base_add = 62;
	static final int _base_sub = 63;
	static final int _base_equ = 0;
	
	static final char[] _dict_ = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 
		'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 
		'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 
		'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 
		'w', 'x', 'y', 'z', '0', '1', '2', '3', 
		'4', '5', '6', '7', '8', '9', '+', '/'
	};
	
	/**
	 * encode binary input with base64 
	 * @param data - binary data
	 * @return encoded text
	 */
	public static String encode(byte[] data) {		
		if (data == null || data.length == 0) {
			return "";
		}
		
		StringBuilder resultBuffer = new StringBuilder();
		//mod
		int m = data.length % 3;
		//amount of data group with 3 byte
		int g = (m == 0) ? (data.length / 3) : (data.length / 3 + 1);
		final byte[] _clear_buff = {0, 0, 0};
		byte[] dataBuffer = new byte[3];
		char[] codeBuffer = new char[4];
		int offset = 0;
		for (int i = 0; i < g; ++i) {
			//clear buffer
			System.arraycopy(_clear_buff, 0, dataBuffer, 0, 3);
			//copy data for handling
			int copySize = (m != 0 && i == g - 1) ? m : 3;
			System.arraycopy(data, offset, dataBuffer, 0, copySize);
			//concatenate the byte data buffer as an integer
			int tmp = 0x00000000;
			for (int j = 0, z = 2; j < 3; ++j, --z) {
				int tt = ((dataBuffer[j] & 0xff) << (z * 8));
				tmp ^= tt;
			}
			//encode current data buffer with base64
			for (int k = 0; k < 4; ++k) {
				int p = (tmp >> (k * 6)) & 0x0000003F;
				codeBuffer[3 - k] = _dict_[p];
			}
			
			if (copySize != 3) {
				for (int e = 0; e < 3 - copySize; ++e) {
					codeBuffer[3 - e] = '=';
				}
			}
			
			resultBuffer.append(codeBuffer);
			offset += 3;
		}
		
		String encodeText = resultBuffer.toString();
		resultBuffer.delete(0, resultBuffer.length());
		return encodeText;
	}
	
	/**
	 * decode the base64 encoded text
	 * @param text - base64 encoded text
	 * @return decoded binary data
	 */
	public static byte[] decode(String text) {
		if (text == null || "".equals(text = text.trim())) {
			return null;
		}
		
		final int _char_size = 4; 
		
		int size = text.length();
		if ((size % _char_size) != 0) {
			throw new RuntimeException("invalid input text, "
					+ "length not enough.");
		}
		
		//compute the buffer size, the tail equality sign count will be deducted
		char cb1 = text.charAt(size - 1);
		char cb2 = text.charAt(size - 2);
		final int dataBufferSize = (size / 4 * 3) - ((cb2 == '=') ? 
				2 : ((cb1 == '=') ? 1 : 0));
		byte[] dataBuffer = new byte[dataBufferSize];
		int bufferIndex = 0;
		
		int equCnt = 0;
		
		//compute each charcter's index
		int tmp = 0;
		for (int i = 0; i < size; i += _char_size) {
			tmp = 0;
			int shiftFactor = 3;
			for (int j = i; j < i + _char_size; ++j) {
				int index = 0;
				char ch = text.charAt(j);
				if (ch >= 'A' && ch <= 'Z') {
					index = ch - 'A' + _base_A_Z;
				} else if (ch >= 'a' && ch <= 'z') {
					index = ch - 'a' + _base_a_z;
				} else if (ch >= '0' && ch <= '9') {
					index = ch - '0' + _base_0_9;
				} else if (ch == '+') {
					index = _base_add;
				} else if (ch == '/') {
					index = _base_sub;
				} else if (ch == '=') {
					index = _base_equ;
					++equCnt;
					if (equCnt > 2) 
						throw new RuntimeException("invalid format");
				} else {
					throw new RuntimeException("invalid input text, "
							+ "illegal character '" + ch + "'.");
				}
				tmp ^= (index << (shiftFactor * 6));
				--shiftFactor;
			}
			
			//decode to binary
			dataBuffer[bufferIndex++] = (byte)((tmp >>> 16) & 0xff);
			if (bufferIndex == dataBufferSize) break;
			dataBuffer[bufferIndex++] = (byte)((tmp >>> 8 ) & 0xff);
			if (bufferIndex == dataBufferSize) break;
			dataBuffer[bufferIndex++] = (byte)((tmp       ) & 0xff);
		}
		
		return dataBuffer;
	}
}
