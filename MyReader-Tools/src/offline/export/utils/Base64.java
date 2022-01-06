package offline.export.utils;

public class Base64 {
	private static byte[] base64Alphabet;

	static final int BASELENGTH = 255;

	static final byte[] CHUNK_SEPARATOR = "\r\n".getBytes();

	static final int CHUNK_SIZE = 76;

	static final int EIGHTBIT = 8;

	static final int FOURBYTE = 4;

	private static byte[] lookUpBase64Alphabet = new byte[64];

	static final int LOOKUPLENGTH = 64;

	static final byte PAD = 61;

	static final int SIGN = -128;

	static final int SIXTEENBIT = 16;

	static final int TWENTYFOURBITGROUP = 24;

	static {
		base64Alphabet = new byte[255];
		int i;
		for (i = 0; i < 255; i++)
			base64Alphabet[i] = -1;
		for (i = 90; i >= 65; i--)
			base64Alphabet[i] = (byte) (i - 65);
		for (i = 122; i >= 97; i--)
			base64Alphabet[i] = (byte) (i - 97 + 26);
		for (i = 57; i >= 48; i--)
			base64Alphabet[i] = (byte) (i - 48 + 52);
		base64Alphabet[43] = 62;
		base64Alphabet[47] = 63;
		for (i = 0; i <= 25; i++)
			lookUpBase64Alphabet[i] = (byte) (65 + i);
		int j;
		for (i = 26, j = 0; i <= 51; i++, j++)
			lookUpBase64Alphabet[i] = (byte) (97 + j);
		for (i = 52, j = 0; i <= 61; i++, j++)
			lookUpBase64Alphabet[i] = (byte) (48 + j);
		lookUpBase64Alphabet[62] = 43;
		lookUpBase64Alphabet[63] = 47;
	}

	private static boolean isBase64(byte octect) {
		if (octect == 61)
			return true;
		if (base64Alphabet[0xFF & octect] == -1)
			return false;
		return true;
	}

	private byte[] quad = new byte[4];

	private int quadCount = 0;

	public void dispose() {
		this.quadCount = 0;
	}

	public static byte[] encodeBase64(byte[] binaryData) {
		int lengthDataBits = binaryData.length * 8;
		int fewerThan24bits = lengthDataBits % 24;
		int numberTriplets = lengthDataBits / 24;
		byte[] encodedData = null;
		int encodedDataLength = 0;
		if (fewerThan24bits != 0) {
			encodedDataLength = (numberTriplets + 1) * 4;
		} else {
			encodedDataLength = numberTriplets * 4;
		}
		encodedData = new byte[encodedDataLength];
		byte k = 0, l = 0, b1 = 0, b2 = 0, b3 = 0;
		int encodedIndex = 0;
		int dataIndex = 0;
		int i = 0;
		for (i = 0; i < numberTriplets; i++) {
			dataIndex = i * 3;
			b1 = binaryData[dataIndex];
			b2 = binaryData[dataIndex + 1];
			b3 = binaryData[dataIndex + 2];
			l = (byte) (b2 & 0xF);
			k = (byte) (b1 & 0x3);
			byte val1 = ((b1 & Byte.MIN_VALUE) == 0) ? (byte) (b1 >> 2) : (byte) (b1 >> 2 ^ 0xC0);
			byte val2 = ((b2 & Byte.MIN_VALUE) == 0) ? (byte) (b2 >> 4) : (byte) (b2 >> 4 ^ 0xF0);
			byte val3 = ((b3 & Byte.MIN_VALUE) == 0) ? (byte) (b3 >> 6) : (byte) (b3 >> 6 ^ 0xFC);
			encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
			encodedData[encodedIndex + 1] = lookUpBase64Alphabet[val2 | k << 4];
			encodedData[encodedIndex + 2] = lookUpBase64Alphabet[l << 2 | val3];
			encodedData[encodedIndex + 3] = lookUpBase64Alphabet[b3 & 0x3F];
			encodedIndex += 4;
		}
		dataIndex = i * 3;
		if (fewerThan24bits == 8) {
			b1 = binaryData[dataIndex];
			k = (byte) (b1 & 0x3);
			byte val1 = ((b1 & Byte.MIN_VALUE) == 0) ? (byte) (b1 >> 2) : (byte) (b1 >> 2 ^ 0xC0);
			encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
			encodedData[encodedIndex + 1] = lookUpBase64Alphabet[k << 4];
			encodedData[encodedIndex + 2] = 61;
			encodedData[encodedIndex + 3] = 61;
		} else if (fewerThan24bits == 16) {
			b1 = binaryData[dataIndex];
			b2 = binaryData[dataIndex + 1];
			l = (byte) (b2 & 0xF);
			k = (byte) (b1 & 0x3);
			byte val1 = ((b1 & Byte.MIN_VALUE) == 0) ? (byte) (b1 >> 2) : (byte) (b1 >> 2 ^ 0xC0);
			byte val2 = ((b2 & Byte.MIN_VALUE) == 0) ? (byte) (b2 >> 4) : (byte) (b2 >> 4 ^ 0xF0);
			encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
			encodedData[encodedIndex + 1] = lookUpBase64Alphabet[val2 | k << 4];
			encodedData[encodedIndex + 2] = lookUpBase64Alphabet[l << 2];
			encodedData[encodedIndex + 3] = 61;
		}
		return encodedData;
	}

	public static byte[] decodeBase64(byte[] base64Data) {
		base64Data = discardNonBase64(base64Data);
		if (base64Data.length == 0)
			return new byte[0];
		int numberQuadruple = base64Data.length / 4;
		byte[] decodedData = null;
		byte b1 = 0, b2 = 0, b3 = 0, b4 = 0, marker0 = 0, marker1 = 0;
		int encodedIndex = 0;
		int dataIndex = 0;
		int lastData = base64Data.length;
		while (base64Data[lastData - 1] == 61) {
			if (--lastData == 0)
				return new byte[0];
		}
		decodedData = new byte[lastData - numberQuadruple];
		for (int i = 0; i < numberQuadruple; i++) {
			dataIndex = i * 4;
			marker0 = base64Data[dataIndex + 2];
			marker1 = base64Data[dataIndex + 3];
			b1 = base64Alphabet[base64Data[dataIndex]];
			b2 = base64Alphabet[base64Data[dataIndex + 1]];
			if (marker0 != 61 && marker1 != 61) {
				b3 = base64Alphabet[marker0];
				b4 = base64Alphabet[marker1];
				decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
				decodedData[encodedIndex + 1] = (byte) ((b2 & 0xF) << 4 | b3 >> 2 & 0xF);
				decodedData[encodedIndex + 2] = (byte) (b3 << 6 | b4);
			} else if (marker0 == 61) {
				decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
			} else if (marker1 == 61) {
				b3 = base64Alphabet[marker0];
				decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
				decodedData[encodedIndex + 1] = (byte) ((b2 & 0xF) << 4 | b3 >> 2 & 0xF);
			}
			encodedIndex += 3;
		}
		return decodedData;
	}

	static byte[] discardNonBase64(byte[] data) {
		byte[] groomedData = new byte[data.length];
		int bytesCopied = 0;
		for (int i = 0; i < data.length; i++) {
			if (isBase64(data[i]))
				groomedData[bytesCopied++] = data[i];
		}
		byte[] packedData = new byte[bytesCopied];
		System.arraycopy(groomedData, 0, packedData, 0, bytesCopied);
		return packedData;
	}
}
