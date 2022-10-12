package pocketmemory.com.data.entity;

import java.security.PrivateKey;
import java.security.PublicKey;

public class EncryptVO {
	private PublicKey publicKey = null;
	private String modulus = null;
	private String exponent = null;
	private PrivateKey privateKey = null;
	
	public PublicKey getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	public String getModulus() {
		return modulus;
	}
	public void setModulus(String modulus) {
		this.modulus = modulus;
	}
	public String getExponent() {
		return exponent;
	}
	public void setExponent(String exponent) {
		this.exponent = exponent;
	}
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
}
