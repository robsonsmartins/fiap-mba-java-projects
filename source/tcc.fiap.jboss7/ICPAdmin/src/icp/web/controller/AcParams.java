package icp.web.controller;

import java.io.Serializable;

/**
 * Armazena parametros obtidos de um formulario de cadastro de AC
 *   (Autoridade Certificadora).
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class AcParams implements Serializable {

	private String name;
	private String crlURI;
	private String cpsURI;
	private String keyPassword;
	private String caPassword;
	private String subjC;
	private String subjO;
	private String subjOU;
	private String subjCN;
	
	/**
	 * Cria uma instancia limpa do gerenciador de parametros de AC.
	 */
	public AcParams() {
		clear();
	}
	
	/**
	 * Limpa todos os parametros de AC.
	 */
	public void clear() {
		this.name        = null;
		this.crlURI      = null;
		this.cpsURI      = null;
		this.keyPassword = null;
		this.caPassword  = null;
		this.subjC       = null;
		this.subjO       = null;
		this.subjOU      = null;
		this.subjCN      = null;
	}

	/**
	 * Retorna o Nome da AC.
	 * @return Nome da AC.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Configura o Nome da AC.
	 * @param name Nome da AC.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Retorna a URI da Lista de Certificados Revogados (LCR) da AC.
	 * @return URI da LCR da AC.
	 */
	public String getCrlURI() {
		return crlURI;
	}
	
	/**
	 * Configura a URI da Lista de Certificados Revogados (LCR) da AC.
	 * @param crlURI URI da LCR da AC.
	 */
	public void setCrlURI(String crlURI) {
		this.crlURI = crlURI;
	}
	
	/**
	 * Retorna a URI do Documento de Politicas de Certificacao (DPC) da AC.
	 * @return URI da DPC da AC.
	 */
	public String getCpsURI() {
		return cpsURI;
	}

	/**
	 * Configura a URI do Documento de Politicas de Certificacao (DPC) da AC.
	 * @param cpsURI URI da DPC da AC.
	 */
	public void setCpsURI(String cpsURI) {
		this.cpsURI = cpsURI;
	}
	
	/**
	 * Retorna a senha da chave privada a ser assinada pela AC.
	 * @return Senha da chave privada a ser assinada pela AC.
	 */
	public String getKeyPassword() {
		return keyPassword;
	}
	
	/**
	 * Configura a senha da chave privada a ser assinada pela AC.
	 * @param password Senha da chave privada a ser assinada pela AC.
	 */
	public void setKeyPassword(String password) {
		this.keyPassword = password;
	}
	
	/**
	 * Retorna a senha da AC Emissora (Issuer).
	 * @return Senha da AC Emissora.
	 */
	public String getCaPassword() {
		return caPassword;
	}

	/**
	 * Configura a senha da AC Emissora (Issuer).
	 * @param password Senha da AC Emissora.
	 */
	public void setCaPassword(String password) {
		this.caPassword = password;
	}

	/**
	 * Retorna o Subject (DN): Country (C) da AC.
	 * @return Subject (DN): Country (C).
	 */
	public String getSubjC() {
		return subjC;
	}
	
	/**
	 * Configura o Subject (DN): Country (C) da AC.
	 * @param subjC Subject (DN): Country (C).
	 */
	public void setSubjC(String subjC) {
		this.subjC = subjC;
	}
	
	/**
	 * Retorna o Subject (DN): Organization (O) da AC.
	 * @return Subject (DN): Organization (O).
	 */
	public String getSubjO() {
		return subjO;
	}
	
	/**
	 * Configura o Subject (DN): Organization (O) da AC.
	 * @param subjO Subject (DN): Organization (O).
	 */
	public void setSubjO(String subjO) {
		this.subjO = subjO;
	}
	
	/**
	 * Retorna o Subject (DN): Organizational Unit (OU) da AC.
	 * @return Subject (DN): Organizational Unit (OU).
	 */
	public String getSubjOU() {
		return subjOU;
	}
	
	/**
	 * Configura o Subject (DN): Organizational Unit (OU) da AC.
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 */
	public void setSubjOU(String subjOU) {
		this.subjOU = subjOU;
	}
	
	/**
	 * Retorna o Subject (DN): Common Name (CN) da AC.
	 * @return Subject (DN): Common Name (CN).
	 */
	public String getSubjCN() {
		return subjCN;
	}
	
	/**
	 * Configura o Subject (DN): Common Name (CN) da AC.
	 * @param subjCN Subject (DN): Common Name (CN).
	 */
	public void setSubjCN(String subjCN) {
		this.subjCN = subjCN;
	}
}
