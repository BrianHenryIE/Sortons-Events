package ie.sortons.events.server.servlet;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


/**
 * A simple text cipher to encrypt/decrypt a string.
 * 
 * @see http://herbertwu.wordpress.com/2010/03/06/a-simple-string-cipher-in-java-6/
 */
public class SimpleStringCipher {
	private static byte[] linebreak = {}; // Remove Base64 encoder default linebreak

	private static SecretKey key;
	private static Cipher cipher;
	private static Base64 coder;

	public SimpleStringCipher(String secret) {
		// secret key length must be 16
		if (secret.length() < 16)
			while (secret.length() < 16)
				secret += " ";
		else
			secret = secret.substring(0, 16);

		try {
			key = new SecretKeySpec(secret.getBytes(), "AES");
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
			coder = new Base64(32, linebreak, true);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public String encrypt(String plainText) throws Exception {
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherText = cipher.doFinal(plainText.getBytes());
		return new String(coder.encode(cipherText));
	}

	public String decrypt(String codedText) throws Exception {
		byte[] encypted = coder.decode(codedText.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decrypted = cipher.doFinal(encypted);
		return new String(decrypted);
	}

}