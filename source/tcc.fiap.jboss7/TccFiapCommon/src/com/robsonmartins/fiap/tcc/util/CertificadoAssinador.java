package com.robsonmartins.fiap.tcc.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * Implementa metodos para assinar conteudos com certificados digitais.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class CertificadoAssinador {

	/**
	 * Retorna um par de chaves armazenado em um arquivo de keystore.
	 * @param keyStoreFile Nome do arquivo de keystore.
	 * @param keyStoreType Tipo do arquivo de keystore.
	 * @param keyAlias Alias do par de chaves dentro do keystore.
	 * @param keyStorePass Senha do keystore.
	 * @param keyPass Senha do par de chaves dentro do keystore.
	 * @return Par de chaves (publica e privada).
	 * @throws Exception
	 */
	public static KeyPair getKeyPairFromFile(String keyStoreFile,
			String keyStoreType, String keyAlias, 
			String keyStorePass, String keyPass) throws Exception {

		PrivateKey privateKey = null;
		PublicKey  publicKey  = null;
		KeyPair    keys       = null;
		
		KeyStore keyStore =
			loadKeyStoreFromFile(keyStoreFile, keyStorePass, keyStoreType);
		Key key = keyStore.getKey(keyAlias, keyPass.toCharArray());

		if (key instanceof PrivateKey) {
			privateKey = (PrivateKey) key;
		}
		
		Certificate cert = keyStore.getCertificate(keyAlias);
		publicKey = cert.getPublicKey();
		
		if (publicKey != null && privateKey != null) {
			keys = new KeyPair(publicKey, privateKey);
		}
		
		return keys;
	}

	/**
	 * Retorna um par de chaves armazenado em um arquivo de keystore (JKS).
	 * @param keyStoreFile Nome do arquivo de keystore (JKS).
	 * @param keyAlias Alias do par de chaves dentro do keystore.
	 * @param keyStorePass Senha do keystore.
	 * @param keyPass Senha do par de chaves dentro do keystore.
	 * @return Par de chaves (publica e privada).
	 * @throws Exception
	 */
	public static KeyPair getKeyPairFromFile(String keyStoreFile,
			String keyAlias, String keyStorePass, String keyPass) throws Exception {
		
		return getKeyPairFromFile(keyStoreFile, "JKS", keyAlias, keyStorePass, keyPass);
	}
	
	/**
	 * Retorna um certificado armazenado em um arquivo de keystore.	
	 * @param keyStoreFile Nome do arquivo de keystore.
	 * @param keyStoreType Tipo do arquivo de keystore.
	 * @param keyAlias Alias do par de chaves dentro do keystore.
	 * @param keyStorePass Senha do keystore.
	 * @return Objeto que representa um certificado digital.
	 * @throws Exception
	 */
	public static X509Certificate getCertFromFile(String keyStoreFile,
			String keyStoreType, String keyAlias,
			String keyStorePass) throws Exception {

		KeyStore keyStore =
			loadKeyStoreFromFile(keyStoreFile, keyStorePass, keyStoreType);
		return (X509Certificate) keyStore.getCertificate(keyAlias);
	}

	/**
	 * Retorna um certificado armazenado em um arquivo de keystore (JKS).	
	 * @param keyStoreFile Nome do arquivo de keystore (JSK).
	 * @param keyAlias Alias do par de chaves dentro do keystore.
	 * @param keyStorePass Senha do keystore.
	 * @return Objeto que representa um certificado digital.
	 * @throws Exception
	 */
	public static X509Certificate getCertFromFile(String keyStoreFile,
			String keyAlias, String keyStorePass) throws Exception {
		
		return getCertFromFile(keyStoreFile, "JKS", keyAlias, keyStorePass);
	}

	/**
	 * Assina um conteudo usando uma chave privada.
	 * @param provider Provider de criptografia.
	 * @param algorithm Algoritmo de assinatura a ser usado.
	 * @param key Chave privada.
	 * @param content Conteudo a ser assinado.
	 * @return Assinatura do conteudo.
	 * @throws Exception
	 */
	public static byte[] sign(Provider provider, String algorithm, PrivateKey key,
			byte[] content) throws Exception {

		if (algorithm == null) { algorithm = "SHA1withRSA"; }
		if (provider  == null) { return sign(algorithm, key, content); }
		Signature sig = Signature.getInstance(algorithm, provider);
		sig.initSign(key);
		sig.update(content, 0, content.length);
		return sig.sign();
	}
	
	/**
	 * Assina um conteudo usando uma chave privada
	 *   (provider default).
	 * @param algorithm Algoritmo de assinatura a ser usado.
	 * @param key Chave privada.
	 * @param content Conteudo a ser assinado.
	 * @return Assinatura do conteudo.
	 * @throws Exception
	 */
	public static byte[] sign(String algorithm, PrivateKey key,
			byte[] content) throws Exception {

		Signature sig = Signature.getInstance(algorithm);
		sig.initSign(key);
		sig.update(content, 0, content.length);
		return sig.sign();
	}

	/**
	 * Assina um conteudo usando uma chave privada
	 *   (provider default, algoritmo SHA1 com RSA).
	 * @param key Chave privada.
	 * @param content Conteudo a ser assinado.
	 * @return Assinatura do conteudo.
	 * @throws Exception
	 */
	public static byte[] sign(PrivateKey key,
			byte[] content) throws Exception {
		
		return sign("SHA1withRSA", key, content);
	}

	/**
	 * Verifica a autenticidade de um conteudo.
	 * @param provider Provider de criptografia.
	 * @param algorithm Algoritmo de assinatura a ser usado.
	 * @param key Chave publica.
	 * @param content Conteudo a ser verificado.
	 * @param signed Assinatura do conteudo.
	 * @return True se autenticidade foi confirmada, false se nao.
	 * @throws Exception
	 */
	public static boolean verify(Provider provider, String algorithm, PublicKey key,
			byte[] content, byte[] signed) throws Exception {
		
		if (algorithm == null) { algorithm = "SHA1withRSA"; }
		if (provider  == null) { return verify(algorithm, key, content, signed); }
		Signature sig = Signature.getInstance(algorithm, provider);
		sig.initVerify(key);
		sig.update(content, 0, content.length);
		return sig.verify(signed);
	}

	/**
	 * Verifica a autenticidade de um conteudo
	 *   (provider default).
	 * @param algorithm Algoritmo de assinatura a ser usado.
	 * @param key Chave publica.
	 * @param content Conteudo a ser verificado.
	 * @param signed Assinatura do conteudo.
	 * @return True se autenticidade foi confirmada, false se nao.
	 * @throws Exception
	 */
	public static boolean verify(String algorithm, PublicKey key,
			byte[] content, byte[] signed) throws Exception {
		
		Signature sig = Signature.getInstance(algorithm);
		sig.initVerify(key);
		sig.update(content, 0, content.length);
		return sig.verify(signed);
	}

	/**
	 * Verifica a autenticidade de um conteudo
	 *   (provider default, algoritmo SHA1 com RSA).
	 * @param key Chave publica.
	 * @param content Conteudo a ser verificado.
	 * @param signed Assinatura do conteudo.
	 * @return True se autenticidade foi confirmada, false se nao.
	 * @throws Exception
	 */
	public static boolean verify(PublicKey key, byte[] content,
			byte[] signed) throws Exception {
		
		return verify("SHA1withRSA", key, content, signed);
	}
	
	/* Retorna um objeto {@link KeyStore} a partir de um arquivo.
	 * @param keyStoreFile Nome do arquivo de keystore.
	 * @param keyStorePass Senha do keystore.
	 * @param keyStoreType Tipo do arquivo de keystore.
	 * @return Objeto KeyStore.
	 * @throws Exception
	 */
	private static KeyStore loadKeyStoreFromFile(String keyStoreFile, 
			String keyStorePass, String keyStoreType) throws Exception {
		
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		InputStream istream = new FileInputStream(keyStoreFile);
		keyStore.load(istream, keyStorePass.toCharArray());
		return keyStore;
	}
	
}
