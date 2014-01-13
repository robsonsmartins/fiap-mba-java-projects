package com.robsonmartins.fiap.tcc.util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Serializa um objeto certificado ({@link X509Certificate})
 *   no formato Base64.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class CertificadoSerializador {

	/* encoder Base64 */
	private static BASE64Encoder encoder = new BASE64Encoder();
	/* decoder Base64 */
	private static BASE64Decoder decoder = new BASE64Decoder();
	
	/**
	 * Serializa um certificado para string (Base64). 
	 * @param cert Objeto que representa um certificado.
	 * @return String codificada em Base64. 
	 * @throws Exception
	 */
	public static String certToStr(X509Certificate cert) throws Exception {
		String out = null;
		if (cert != null) {
			out = encoder.encode(cert.getEncoded()).replaceAll("\\s+","");
		}
		return out;
	}
	
	/**
	 * Desserializa um certificado a partir de uma string (Base64).
	 * @param s String codificada em Base64.
	 * @return Objeto que representa um certificado.
	 * @throws Exception
	 */
	public static X509Certificate strToCert(String s) throws Exception {
        CertificateFactory certificateFactory = null;
        X509Certificate out = null;
		if (s != null) {
	        certificateFactory = CertificateFactory.getInstance("X.509");
			ByteArrayInputStream byteInStream =
				new ByteArrayInputStream(decoder.decodeBuffer(s.replaceAll("\\s+","")));
	        out = (X509Certificate) certificateFactory.generateCertificate(byteInStream);
		}
		return out;
	}

	/**
	 * Serializa um certificado para um array de bytes. 
	 * @param cert Objeto que representa um certificado.
	 * @return Array de bytes. 
	 * @throws Exception
	 */
	public static byte[] certToBytes(X509Certificate cert) throws Exception {
		byte[] out = null;
		if (cert != null) {
			out = cert.getEncoded();
		}
		return out;
	}
	
	/**
	 * Desserializa um certificado a partir de um array de bytes.
	 * @param buffer Array de bytes.
	 * @return Objeto que representa um certificado.
	 * @throws Exception
	 */
	public static X509Certificate bytesToCert(byte[] buffer) throws Exception {
        CertificateFactory certificateFactory = null;
        X509Certificate out = null;
		if (buffer != null) {
	        certificateFactory = CertificateFactory.getInstance("X.509");
			ByteArrayInputStream byteInStream =
				new ByteArrayInputStream(buffer);
	        out = (X509Certificate) certificateFactory.generateCertificate(byteInStream);
		}
		return out;
	}
	
	/**
	 * Retorna o Distinguished Name (DN) de um certificado. 
	 * @param content Conteudo do certificado, codificado em Base64.
	 * @return DN do certificado
	 */
	public static String getDNByCertStr(String content) throws Exception {
		X509Certificate cert = strToCert(content);
		return cert.getSubjectX500Principal().getName();
	}

	/**
	 * Obtem um objeto certificado ({@link X509Certificate}) a partir de
	 *   uma instancia de {@link InputStream}, que pode apontar para um
	 *   arquivo de certificado (formatos PEM ou DER). 
	 * @param istream Objeto InputStream que aponta para um arquivo
	 *   de certificado.
	 * @return Objeto que representa um certificado.
	 * @throws Exception
	 */
	public static X509Certificate loadCertFromStream(InputStream istream)
										throws Exception {
		
		InputStream filteredStream = filterPemCert(istream);
		
		CertificateFactory certFactory =
			CertificateFactory.getInstance("X.509");

		X509Certificate x509cert =
			(X509Certificate)certFactory.generateCertificate(filteredStream);
		
		return x509cert;
	}

	/*
	 * Filtra o conteudo de um certificado (PEM, Base64), eliminando tudo
	 *   o que estiver entre o delimitador ("-----BEGIN CERTIFICATE-----").
	 *   
	 * Isso permite a importacao de arquivos PEM gerados pelo OpenSSL,
	 *   incompativeis com o JCA.
	 * 
	 * @param istream Objeto InputStream de entrada, com o conteudo do
	 *   certificado, binario (DER) ou Base64 (PEM).
	 * @return Objeto InputStream com o conteudo do certificado,
	 *   binario (DER) ou Base64 (PEM).
	 * @throws Exception
	 */
	private static InputStream filterPemCert(InputStream istream)
			throws Exception {

		/* delimitador de inicio do conteudo do certificado */ 
		final byte[] begin = "-----BEGIN CERTIFICATE-----".getBytes();
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[4 * 1024];
		int offset = 0;
		int len = 0;
		
		/* copia todo conteudo da inputStream de entrada para um
		 * buffer interno */
		while (-1 != (len = istream.read(buffer))) {
			output.write(buffer, 0, len);
		}
		
		buffer = output.toByteArray();
		
		/* vertifica tamanho do buffer */
		offset = 0;
		len = buffer.length;
		
		if (buffer.length < begin.length) {
			throw new Exception("Invalid input.");
		}
		
		/* procura pelo delimitador de inicio de certificado */
		int idxBuffer = 0;
		while (idxBuffer < buffer.length) {
			boolean found = true;
			for (int idxBegin = 0; idxBegin < begin.length; idxBegin++) {
				if (buffer[idxBuffer] != begin[idxBegin]) {
					found = false;
					break;
				}
				idxBuffer++;
			}
			if (found) {
				/* se achou, ignora todo o conteudo anterior
				   ao delimitador */
				offset = idxBuffer - begin.length;
				len = buffer.length - offset;
				break;
			}
			idxBuffer++;
		}

		/* se nao achou delimitador, considera o conteudo na
		   integra, pode estar em formato binario (DER) */
		
		ByteArrayInputStream internalStream =
			new ByteArrayInputStream(buffer,offset,len);
		
		/* retorna um InputStream baseado no buffer interno */
		return internalStream;
	}
}
