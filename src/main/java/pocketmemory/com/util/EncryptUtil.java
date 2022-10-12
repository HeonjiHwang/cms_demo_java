package pocketmemory.com.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptUtil {
	private static final Logger logger = LoggerFactory.getLogger(EncryptUtil.class);
	private static EncryptUtil instance = new EncryptUtil();
	
	//AES based key
	private static String _aes128Key = "skwibleifhqavnxe";
	private static String _aes256Key = "flbiclwkdnclaiwhbkleslfiendcmdzw";
	
	public class RSAKey {
		public PrivateKey _privateKey = null;
		public PublicKey _publicKey = null;
		public String modulus = null;
		public String exponent = null;
	}
	
	private EncryptUtil() {};
	
	public static EncryptUtil getInstance() {
		return instance;
	}
	
	public RSAKey genRSAKeyPair() throws NoSuchAlgorithmException{
		
		try {
			SecureRandom _secureRandom = new SecureRandom();
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(1024, _secureRandom);
			
			RSAKey key = new RSAKey();
			
			KeyPair keyPair = generator.genKeyPair();
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			
			key._privateKey = keyPair.getPrivate();
			
			PublicKey publicKey = keyPair.getPublic();
			key._publicKey = keyPair.getPublic();
			
			RSAPublicKeySpec publicSpec = (RSAPublicKeySpec) keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
			key.modulus = publicSpec.getModulus().toString(16);
			key.exponent = publicSpec.getPublicExponent().toString(16);
			
			return key;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String publicKeyToString(PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException{
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec _encodeKeySpec = keyFactory.getKeySpec(publicKey, X509EncodedKeySpec.class);
		return Base64.encodeBase64String(_encodeKeySpec.getEncoded());
	}
	
	private static byte[] hexToByteArray(String hex) {
		if(hex == null || hex.length() % 2 != 0) return new byte[] {};
		
		byte[] bytes = new byte[hex.length() / 2];
		for(int  i=0;i<hex.length(); i += 2) {
			byte value = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
			bytes[(int) Math.floor(i / 2)] = value;
		}
		
		return bytes;
	}
	
	public String decryptRSA(PrivateKey privateKey, String encryptedstr) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA");
		byte[] encryptedBytes = hexToByteArray(encryptedstr);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
		
		String decryptedValue = new String(decryptedBytes, "utf-8");
		
		return decryptedValue;
	}
	
	public String decryptRSA(String encryptedstr, PrivateKey privateKey) {
		if(encryptedstr == null || encryptedstr.length() == 0 || privateKey == null)
			return null;
		
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			
			byte[] cipherData = Base64.decodeBase64(encryptedstr);
			byte[] plainText = cipher.doFinal(cipherData);
			
			return new String(plainText, "utf-8");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
