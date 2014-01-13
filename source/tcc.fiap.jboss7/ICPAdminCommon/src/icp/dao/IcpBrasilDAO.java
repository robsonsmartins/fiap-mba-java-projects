package icp.dao;

import icp.bean.Ca;
import icp.util.OpenSSLWrapper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ini4j.Ini;

/**
 * Gerencia uma PKI (ICP) no padrao ICP-Brasil.<br>
 * Wrapper para as classes {@link RootCaDAO} e {@link CaDAO}.
 * @author Robson Martins (robson@robsonmartins.com)
 * @see <a target="_blank" href="http://www.iti.gov.br/twiki/bin/view/Certificacao/EstruturaIcp">Estrutura da ICP-Brasil</a>
 */
public class IcpBrasilDAO {

	/** OID default da Politica de Certificacao da CA para emitir certificados
	 *  padrao A3 para Pessoa Fisica. */
	public static final String CPS_CA_A3_PF = CaDAO.CPS_CA_A3;

	/** OID default da Politica de Certificacao da CA para emitir certificados
	 *  padrao A3 para Pessoa Juridica. */
	public static final String CPS_CA_A3_PJ = CaDAO.CPS_CA_A3;

	/* Algoritmo de hash (default) do certificado da CA Raiz. */
	private static final String ROOTCA_ALGHASH     = "sha512"; 
	/* Tamanho, em bits (default) da chave do certificado da CA Raiz. */
	private static final int    ROOTCA_KEYSIZE     = 4096; 
	/* Tempo, em dias (default) de expiracao do certificado da CA Raiz. */
	private static final int    ROOTCA_EXPIRE_DAYS = 4745;
	/* Tempo, em dias (default) de atualizacao da LCR da CA Raiz. */
	private static final int    ROOTCA_CRL_DAYS    = 90;

	/* Algoritmo de hash (default) do certificado da CA Intermediaria. */
	private static final String CA_ALGHASH     = "sha512"; 
	/* Tamanho, em bits (default) da chave do certificado da CA Intermediaria. */
	private static final int    CA_KEYSIZE     = 4096; 
	/* Tempo, em dias (default) de expiracao do certificado da CA Intermediaria. */
	private static final int    CA_EXPIRE_DAYS = 3650;
	/* Tempo, em dias (default) de atualizacao da LCR da CA Intermediaria. */
	private static final int    CA_CRL_DAYS    = 90;

	/* Algoritmo de hash (default) do certificado de usuario da CA. */
	private static final String USER_ALGHASH     = "sha1"; 
	/* Tamanho, em bits (default) da chave do certificado de usuario da CA. */
	private static final int    USER_KEYSIZE     = 2048; 
	/* Tempo, em dias (default) de expiracao do certificado de usuario da CA. */
	private static final int    USER_EXPIRE_DAYS = 2190;
	
	/* Padrao do campo email na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_EMAIL_PATTERN = "%s"; 
	/* Padrao do campo DNS na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_DNS_PATTERN = "%s"; 
	/* Padrao do campo otherName:2.16.76.1.3.1 na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_2_16_76_1_3_1_PATTERN = "2.16.76.1.3.1;UTF8:%8.8s%11.11s%11.11s%15.15s%-6.6s"; 
	/* Padrao do campo otherName:2.16.76.1.3.2 na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_2_16_76_1_3_2_PATTERN = "2.16.76.1.3.2;UTF8:%s"; 
	/* Padrao do campo otherName:2.16.76.1.3.3 na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_2_16_76_1_3_3_PATTERN = "2.16.76.1.3.3;UTF8:%14.14s"; 
	/* Padrao do campo otherName:2.16.76.1.3.4 na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_2_16_76_1_3_4_PATTERN = "2.16.76.1.3.4;UTF8:%8.8s%11.11s%11.11s%15.15s%-6.6s"; 
	/* Padrao do campo otherName:2.16.76.1.3.5 na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_2_16_76_1_3_5_PATTERN = "2.16.76.1.3.5;UTF8:%12.12s%3.3s%4.4s%-22.22s"; 
	/* Padrao do campo otherName:2.16.76.1.3.6 na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_2_16_76_1_3_6_PATTERN = "2.16.76.1.3.6;UTF8:%12.12s"; 
	/* Padrao do campo otherName:2.16.76.1.3.7 na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_2_16_76_1_3_7_PATTERN = "2.16.76.1.3.7;UTF8:%12.12s"; 
	/* Padrao do campo otherName:2.16.76.1.3.8 na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_2_16_76_1_3_8_PATTERN = "2.16.76.1.3.8;UTF8:%s"; 
	/* Padrao do campo otherName:2.16.76.1.3.9 na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_2_16_76_1_3_9_PATTERN = "2.16.76.1.3.9;UTF8:%11.11s"; 
	/* Padrao do campo otherName:1.3.6.1.4.1.311.20.2.3 na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_1_3_6_1_4_1_311_20_2_3_PATTERN = "1.3.6.1.4.1.311.20.2.3;UTF8String:%s"; 
	/* Padrao do campo otherName:1.3.6.1.4.1.311.25.1 na secao "Subject Alternative Name" do arquivo de config do OpenSSL. */
	private static final String SUBJ_ALT_1_3_6_1_4_1_311_25_1_PATTERN = "1.3.6.1.4.1.311.25.1;FORMAT:HEX,OCT:%32.32s"; 
	
	/* "Extended Key Usage" para emissao de e-CPF */
	private static final String ECPF_EXTENDED_KEY_USAGE =
		"serverAuth, clientAuth, emailProtection, timeStamping, 1.3.6.1.4.1.311.20.2.2";
	/* "Extended Key Usage" para emissao de e-CNPJ */
	private static final String ECNPJ_EXTENDED_KEY_USAGE =
		"serverAuth, clientAuth, emailProtection, timeStamping";
	/* "Extended Key Usage" para emissao de e-CODIGO */
	private static final String ECODIGO_EXTENDED_KEY_USAGE =
		"serverAuth, clientAuth, codeSigning, emailProtection, timeStamping";
	/* "Extended Key Usage" para emissao de e-SERVIDOR */
	private static final String ESERVIDOR_EXTENDED_KEY_USAGE =
		"serverAuth, clientAuth";
	/* "Extended Key Usage" para emissao de e-APLICACAO */
	/* Igual ao e-CODIGO */
	//private static final String EAPLICACAO_EXTENDED_KEY_USAGE =
	//	ECODIGO_EXTENDED_KEY_USAGE;
	
	/* gerenciador de CA Raiz */
	private RootCaDAO rootCaDAO;
	/* gerenciador de CA Intermediaria */
	private CaDAO caDAO;
	
	/* oid da politica de certificacao da CA para Cert.A3/PF */
	private String cpsCaA3PF;
	/* oid da politica de certificacao da CA para Cert.A3/PJ */
	private String cpsCaA3PJ;
	/* algoritmo de hash do certificado da CA Raiz */
	private String rootCaAlgHash;
	/* tamanho (em bits) da chave do certificado da CA Raiz */
	private int rootCaKeySize;
	/* tempo (em dias) de expiracao do certificado da CA Raiz */
	private int rootCaExpireDays;
	/* tempo (em dias) de atualizacao da LCR da CA Raiz */
	private int rootCaCrlDays;
	/* algoritmo de hash do certificado da CA Intermediaria */
	private String caAlgHash;
	/* tamanho (em bits) da chave do certificado da CA Intermediaria */
	private int caKeySize;
	/* tempo (em dias) de expiracao do certificado da CA Intermediaria */
	private int caExpireDays;
	/* tempo (em dias) de atualizacao da LCR da CA Intermediaria */
	private int caCrlDays;
	/* algoritmo de hash do certificado de usuario da CA */
	private String userAlgHash;
	/* tamanho (em bits) da chave do certificado de usuario da CA */
	private int userKeySize;
	/* tempo (em dias) de expiracao do certificado de usuario da CA */
	private int userExpireDays;

	/**
	 * Cria uma instancia do gerenciador da PKI (ICP).
	 * @param rootCaDAO Objeto gerenciador de CA Raiz.
	 * @param caDAO Objeto gerenciador de CA Intermediaria.
	 */
	public IcpBrasilDAO(RootCaDAO rootCaDAO, CaDAO caDAO) {
		this.rootCaDAO        = rootCaDAO;
		this.caDAO            = caDAO;
		this.cpsCaA3PF        = CPS_CA_A3_PF;
		this.cpsCaA3PJ        = CPS_CA_A3_PJ;
		this.rootCaAlgHash    = ROOTCA_ALGHASH;
		this.rootCaKeySize    = ROOTCA_KEYSIZE;
		this.rootCaExpireDays = ROOTCA_EXPIRE_DAYS;
		this.rootCaCrlDays    = ROOTCA_CRL_DAYS;
		this.caAlgHash        = CA_ALGHASH;
		this.caKeySize        = CA_KEYSIZE;
		this.caExpireDays     = CA_EXPIRE_DAYS;
		this.caCrlDays        = CA_CRL_DAYS;
		this.userAlgHash      = USER_ALGHASH;
		this.userKeySize      = USER_KEYSIZE;
		this.userExpireDays   = USER_EXPIRE_DAYS;
	}

	/**
	 * Configura o OID da Politica de Certificacao da CA para emitir
	 *   certiticados padrao A3, Pessoa Fisica.
	 * @param cpsCaA3PF OID da Politica de Certificacao da CA para emitir
	 *   certiticados padrao A3, Pessoa Fisica.
	 */
	public void setCpsCaA3PF(String cpsCaA3PF) {
		this.cpsCaA3PF = cpsCaA3PF;
	}

	/**
	 * Retorna o OID da Politica de Certificacao da CA para emitir
	 *   certiticados padrao A3, Pessoa Fisica.
	 * @return OID da Politica de Certificacao da CA para emitir
	 *   certiticados padrao A3, Pessoa Fisica.
	 */
	public String getCpsCaA3PF() {
		return cpsCaA3PF;
	}
	
	/**
	 * Configura o OID da Politica de Certificacao da CA para emitir
	 *   certiticados padrao A3, Pessoa Juridica.
	 * @param cpsCaA3PJ OID da Politica de Certificacao da CA para emitir
	 *   certiticados padrao A3, Pessoa Juridica.
	 */
	public void setCpsCaA3PJ(String cpsCaA3PJ) {
		this.cpsCaA3PJ = cpsCaA3PJ;
	}

	/**
	 * Retorna o OID da Politica de Certificacao da CA para emitir
	 *   certiticados padrao A3, Pessoa Juridica.
	 * @return OID da Politica de Certificacao da CA para emitir
	 *   certiticados padrao A3, Pessoa Juridica.
	 */
	public String getCpsCaA3PJ() {
		return cpsCaA3PJ;
	}

	/**
	 * Configura o Algoritmo de hash do certificado da CA Raiz.
	 * @param rootCaAlgHash Algoritmo de hash do certificado da CA Raiz.
	 */
	public void setRootCaAlgHash(String rootCaAlgHash) {
		this.rootCaAlgHash = rootCaAlgHash;
	}

	/**
	 * Retorna o Algoritmo de hash do certificado da CA Raiz.
	 * @return Algoritmo de hash do certificado da CA Raiz.
	 */
	public String getRootCaAlgHash() {
		return rootCaAlgHash;
	}

	/**
	 * Configura o Tamanho, em bits, da chave do certificado da CA Raiz.
	 * @param rootCaKeySize Tamanho, em bits, da chave do certificado da CA Raiz.
	 */
	public void setRootCaKeySize(int rootCaKeySize) {
		this.rootCaKeySize = rootCaKeySize;
	}

	/**
	 * Retorna o Tamanho, em bits, da chave do certificado da CA Raiz.
	 * @return Tamanho, em bits, da chave do certificado da CA Raiz.
	 */
	public int getRootCaKeySize() {
		return rootCaKeySize;
	}

	/**
	 * Configura o Tempo, em dias, de expiracao do certificado da CA Raiz.
	 * @param rootCaExpireDays Tempo, em dias, de expiracao do certificado da CA Raiz.
	 */
	public void setRootCaExpireDays(int rootCaExpireDays) {
		this.rootCaExpireDays = rootCaExpireDays;
	}

	/**
	 * Retorna o Tempo, em dias, de expiracao do certificado da CA Raiz.
	 * @return Tempo, em dias, de expiracao do certificado da CA Raiz.
	 */
	public int getRootCaExpireDays() {
		return rootCaExpireDays;
	}

	/**
	 * Configura o Tempo, em dias, de atualizacao da LCR da CA Raiz.
	 * @param rootCaCrlDays Tempo, em dias, de atualizacao da LCR da CA Raiz.
	 */
	public void setRootCaCrlDays(int rootCaCrlDays) {
		this.rootCaCrlDays = rootCaCrlDays;
	}

	/**
	 * Retorna o Tempo, em dias, de atualizacao da LCR da CA Raiz.
	 * @return Tempo, em dias, de atualizacao da LCR da CA Raiz.
	 */
	public int getRootCaCrlDays() {
		return rootCaCrlDays;
	}

	/**
	 * Configura o Algoritmo de hash do certificado da CA Intermediaria.
	 * @param caAlgHash Algoritmo de hash do certificado da CA Intermediaria.
	 */
	public void setCaAlgHash(String caAlgHash) {
		this.caAlgHash = caAlgHash;
	}

	/**
	 * Retorna o Algoritmo de hash do certificado da CA Intermediaria.
	 * @return Algoritmo de hash do certificado da CA Intermediaria.
	 */
	public String getCaAlgHash() {
		return caAlgHash;
	}

	/**
	 * Configura o Tamanho, em bits, da chave do certificado da CA Intermediaria.
	 * @param caKeySize Tamanho, em bits, da chave do certificado da CA Intermediaria.
	 */
	public void setCaKeySize(int caKeySize) {
		this.caKeySize = caKeySize;
	}

	/**
	 * Retorna o Tamanho, em bits, da chave do certificado da CA Intermediaria.
	 * @return Tamanho, em bits, da chave do certificado da CA Intermediaria.
	 */
	public int getCaKeySize() {
		return caKeySize;
	}

	/**
	 * Configura o Tempo, em dias, de expiracao do certificado da CA Intermediaria.
	 * @param caExpireDays Tempo, em dias, de expiracao do certificado da CA Intermediaria.
	 */
	public void setCaExpireDays(int caExpireDays) {
		this.caExpireDays = caExpireDays;
	}

	/**
	 * Retorna o Tempo, em dias, de expiracao do certificado da CA Intermediaria.
	 * @return Tempo, em dias, de expiracao do certificado da CA Intermediaria.
	 */
	public int getCaExpireDays() {
		return caExpireDays;
	}

	/**
	 * Configura o Tempo, em dias, de atualizacao da LCR da CA Intermediaria.
	 * @param caCrlDays Tempo, em dias, de atualizacao da LCR da CA Intermediaria.
	 */
	public void setCaCrlDays(int caCrlDays) {
		this.caCrlDays = caCrlDays;
	}

	/**
	 * Retorna o Tempo, em dias, de atualizacao da LCR da CA Intermediaria.
	 * @return Tempo, em dias, de atualizacao da LCR da CA Intermediaria.
	 */
	public int getCaCrlDays() {
		return caCrlDays;
	}

	/**
	 * Configura o Algoritmo de hash do certificado de usuario da CA.
	 * @param userAlgHash Algoritmo de hash do certificado de usuario da CA.
	 */
	public void setUserAlgHash(String userAlgHash) {
		this.userAlgHash = userAlgHash;
	}

	/**
	 * Retorna o Algoritmo de hash do certificado de usuario da CA.
	 * @return Algoritmo de hash do certificado de usuario da CA.
	 */
	public String getUserAlgHash() {
		return userAlgHash;
	}

	/**
	 * Configura o Tamanho, em bits, da chave do certificado de usuario da CA.
	 * @param userKeySize Tamanho, em bits, da chave do certificado de usuario da CA.
	 */
	public void setUserKeySize(int userKeySize) {
		this.userKeySize = userKeySize;
	}

	/**
	 * Retorna o Tamanho, em bits, da chave do certificado de usuario da CA.
	 * @return Tamanho, em bits, da chave do certificado de usuario da CA.
	 */
	public int getUserKeySize() {
		return userKeySize;
	}

	/**
	 * Configura o Tempo, em dias, de expiracao do certificado de usuario da CA.
	 * @param userExpireDays Tempo, em dias, de expiracao do certificado de usuario da CA.
	 */
	public void setUserExpireDays(int userExpireDays) {
		this.userExpireDays = userExpireDays;
	}

	/**
	 * Retorna o Tempo, em dias, de expiracao do certificado de usuario da CA.
	 * @return Tempo, em dias, de expiracao do certificado de usuario da CA.
	 */
	public int getUserExpireDays() {
		return userExpireDays;
	}

	/**
	 * Cria uma nova CA Raiz.
	 * @param name Nome da CA Raiz.
	 * @param crlURI Caminho (URI) da Lista de Certificados Revogados (LCR).
	 * @param cpsURI Caminho (URI) do Documento com as Politicas de Certificacao (DPC).
	 * @param password Senha da CA Raiz, para criptografar a chave privada.
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @param subjCN Subject (DN): Common Name (CN).
	 * @return Objeto que representa a CA Raiz criada.
	 * @throws Exception
	 */
	public Ca createRootCA(String name, String crlURI,
			String cpsURI, String password, String subjC, String subjO,
			String subjOU, String subjCN) throws Exception {

		StringBuilder subject;
		subject = OpenSSLWrapper.subjectAppend(null   , "C" , subjC );
		subject = OpenSSLWrapper.subjectAppend(subject, "O" , subjO );
		subject = OpenSSLWrapper.subjectAppend(subject, "OU", subjOU);
		subject = OpenSSLWrapper.subjectAppend(subject, "CN", subjCN);

		return rootCaDAO.createRootCA(name, crlURI, cpsURI, rootCaExpireDays,
				rootCaCrlDays, rootCaKeySize, rootCaAlgHash, password,
				subject.toString());
	}

	/**
	 * Cria uma nova CA Intermediaria.
	 * @param name Nome da CA.
	 * @param rootCA Objeto que representa a CA Raiz.
	 * @param crlURI Caminho (URI) da Lista de Certificados Revogados (LCR).
	 * @param cpsURI Caminho (URI) do Documento com as Politicas de Certificacao (DPC).
	 * @param keyPassword Senha da CA, para criptografar a chave privada.
	 * @param rootPassword Senha da CA Raiz.
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @param subjCN Subject (DN): Common Name (CN).
	 * @return Objeto que representa a CA criada.
	 * @throws Exception
	 */
	public Ca createCA(String name, Ca rootCA, String crlURI,
			String cpsURI, String keyPassword, String rootPassword,
			String subjC, String subjO, String subjOU,
			String subjCN) throws Exception {
		
		StringBuilder subject;
		subject = OpenSSLWrapper.subjectAppend(null   , "C" , subjC );
		subject = OpenSSLWrapper.subjectAppend(subject, "O" , subjO );
		subject = OpenSSLWrapper.subjectAppend(subject, "OU", subjOU);
		subject = OpenSSLWrapper.subjectAppend(subject, "CN", subjCN);
		
		return caDAO.createCA(name, rootCA, crlURI, cpsURI, caExpireDays,
				caCrlDays, caKeySize, caAlgHash, keyPassword,
				rootPassword, subject.toString());
	}
	
	/**
	 * Cria uma lista de certificados revogados (CRL), para uma CA Raiz.
	 * @param rootCA Objeto que representa a CA Raiz.
	 * @param password Senha da CA Raiz.
	 * @throws Exception
	 */
	public void createCRLforRootCA(Ca rootCA, String password) throws Exception {
		rootCaDAO.createCRL(rootCA, password, rootCaCrlDays, rootCaAlgHash);
	}
	
	/**
	 * Cria uma lista de certificados revogados (CRL), para uma CA Intermediaria.
	 * @param ca Objeto que representa a CA Intermediaria.
	 * @param password Senha da CA Intermediaria.
	 * @throws Exception
	 */
	public void createCRLforCA(Ca ca, String password) throws Exception {
		caDAO.createCRL(ca, password, caCrlDays, caAlgHash);
	}

	/**
	 * Renova o certificado de uma CA Raiz.
	 * @param rootCa Objeto que representa a CA Raiz.
	 * @param password Senha da CA Raiz.
	 * @throws Exception
	 */
	public void renewRootCaCert(Ca rootCa, String password) throws Exception {
		caDAO.renewRootCaCert(rootCa, rootCa.getCertFile(),
				rootCaExpireDays, rootCaAlgHash, password);
	}

	/**
	 * Renova o certificado de uma CA Intermediaria.
	 * @param ca Objeto que representa a CA Intermediaria.
	 * @param password Senha da CA Intermediaria.
	 * @throws Exception
	 */
	public void renewCaCert(Ca ca, String password) throws Exception {
		caDAO.renewCaCert(ca, ca.getCertFile(), ca.getReqFile(),
				caExpireDays, caAlgHash, password);
	}
	
	/**
	 * Renova um certificado de usuario da CA.
	 * @param ca Objeto que representa a CA.
	 * @param certFileName Nome do arquivo de certificado a ser renovado.
	 * @param reqFileName Nome do arquivo de requisicao.
	 * @param caPassword Senha da CA.
	 * @throws Exception
	 */
	public void renewCert(Ca ca, String certFileName,
			String reqFileName, String caPassword) throws Exception {
		
		caDAO.renewUserCert(ca, certFileName, reqFileName,
				userExpireDays, userAlgHash, caPassword);
	}

	/**
	 * Emite um certificado no padrao e-CPF.
	 * @param ca Objeto que representa a CA emissora (issuer).
	 * @param certOutFileName Nome do arquivo de certificado a ser gerado.
	 * @param keyOutFileName Nome do arquivo de chave privada a ser gerado.
	 * @param reqOutFileName Nome do arquivo de requisicao a ser gerado.
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param caPassword Senha da CA.
	 * @param nome Nome do titular do e-CPF.
	 * @param cpf Numero do CPF, sem pontuacao (11 numeros).
	 * @param email Endereco de email.
	 * @param nascimento Data de nascimento.
	 * @param pisPasep Numero do PIS/PASEP/NIS, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG (2 caracteres).
	 * @param cei Numero do CEI (12 numeros).
	 * @param titulo Numero do Titulo de Eleitor (max. 12 numeros).
	 * @param tituloZona Zona Eleitoral do Titulo (max. 3 numeros).
	 * @param tituloSecao Secao Eleitoral do Titulo (max. 4 numeros).
	 * @param tituloMunicipio Municipio do Titulo Eleitoral (max. 20 caracteres).
	 * @param tituloUF UF do Titulo Eleitoral (2 caracteres).
	 * @param login Nome de Usuario (login de rede).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @throws Exception
	 */
	public void createEcpf(Ca ca, String certOutFileName, String keyOutFileName,
			String reqOutFileName, String keyPassword, String caPassword,
			String nome, String cpf, String email, Date nascimento,
			String pisPasep, String rg,	String rgOrgEmissor, String rgUF,
			String cei, String titulo, String tituloZona, String tituloSecao,
			String tituloMunicipio,	String tituloUF, String login,
			String subjC, String subjO, String subjOU) throws Exception {
		
		StringBuilder subject = buildSubject(nome, cpf, subjC, subjO, subjOU);
		
		Map<String, String> subjAlt =
			buildEcpfAltSubject(cpf, email, nascimento, pisPasep, rg,
				rgOrgEmissor, rgUF, cei, titulo, tituloZona, tituloSecao,
				tituloMunicipio, tituloUF, login);

		createEcpfConfFile(ca, subjAlt);
		
		caDAO.createUserCert(ca, certOutFileName, keyOutFileName, reqOutFileName,
				userExpireDays, userKeySize, userAlgHash,
				keyPassword, caPassword, subject.toString());
	}

	/**
	 * Emite um certificado no padrao e-CNPJ.
	 * @param ca Objeto que representa a CA emissora (issuer).
	 * @param certOutFileName Nome do arquivo de certificado a ser gerado.
	 * @param keyOutFileName Nome do arquivo de chave privada a ser gerado.
	 * @param reqOutFileName Nome do arquivo de requisicao a ser gerado.
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param caPassword Senha da CA.
	 * @param nomePJ Nome empresarial.
	 * @param nome Nome do responsavel.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cei Numero do CEI do responsavel (12 numeros).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @param subjL Subject (DN): Locality (L).
	 * @param subjST Subject (DN): State (ST).
	 * @throws Exception
	 */
	public void createEcnpj(Ca ca, String certOutFileName, String keyOutFileName,
			String reqOutFileName, String keyPassword, String caPassword,
			String nomePJ, String nome, String cpf,
			String email, Date nascimento, String pisPasep, String rg,
			String rgOrgEmissor, String rgUF, String cei,
			String cnpj, String subjC, String subjO, String subjOU,
			String subjL, String subjST) throws Exception {
		
		StringBuilder subject = buildSubject(nomePJ, cnpj, subjC, subjO, subjOU, subjL, subjST);
		
		Map<String, String> subjAlt =
			buildEcnpjAltSubject(cpf, email, nascimento, pisPasep, rg,
				rgOrgEmissor, rgUF, cei, cnpj, nome);

		createEcnpjConfFile(ca, subjAlt);
		
		caDAO.createUserCert(ca, certOutFileName, keyOutFileName, reqOutFileName,
				userExpireDays, userKeySize, userAlgHash,
				keyPassword, caPassword, subject.toString());
	}

	/**
	 * Emite um certificado no padrao RIC.
	 * @param ca Objeto que representa a CA emissora (issuer).
	 * @param certOutFileName Nome do arquivo de certificado a ser gerado.
	 * @param keyOutFileName Nome do arquivo de chave privada a ser gerado.
	 * @param reqOutFileName Nome do arquivo de requisicao a ser gerado.
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param caPassword Senha da CA.
	 * @param nome Nome do titular do RIC.
	 * @param ric Numero do RIC, sem pontuacao (11 numeros).
	 * @param cpf Numero do CPF, sem pontuacao (11 numeros).
	 * @param email Endereco de email.
	 * @param nascimento Data de nascimento.
	 * @param pisPasep Numero do PIS/PASEP/NIS, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG (2 caracteres).
	 * @param cei Numero do CEI (12 numeros).
	 * @param titulo Numero do Titulo de Eleitor (max. 12 numeros).
	 * @param tituloZona Zona Eleitoral do Titulo (max. 3 numeros).
	 * @param tituloSecao Secao Eleitoral do Titulo (max. 4 numeros).
	 * @param tituloMunicipio Municipio do Titulo Eleitoral (max. 20 caracteres).
	 * @param tituloUF UF do Titulo Eleitoral (2 caracteres).
	 * @param login Nome de Usuario (login de rede).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @throws Exception
	 */
	public void createRic(Ca ca, String certOutFileName, String keyOutFileName,
			String reqOutFileName, String keyPassword, String caPassword,
			String nome, String ric, String cpf, String email, Date nascimento,
			String pisPasep, String rg, String rgOrgEmissor, String rgUF,
			String cei, String titulo, String tituloZona, String tituloSecao,
			String tituloMunicipio,	String tituloUF, String login,
			String subjC, String subjO, String subjOU) throws Exception {
		
		StringBuilder subject = buildSubject(nome, cpf, subjC, subjO, subjOU);
		
		Map<String, String> subjAlt =
			buildRicAltSubject(ric, cpf, email, nascimento, pisPasep, rg,
				rgOrgEmissor, rgUF, cei, titulo, tituloZona, tituloSecao,
				tituloMunicipio, tituloUF, login);

		createRicConfFile(ca, subjAlt);
		
		caDAO.createUserCert(ca, certOutFileName, keyOutFileName, reqOutFileName,
				userExpireDays, userKeySize, userAlgHash,
				keyPassword, caPassword, subject.toString());
	}
	
	/**
	 * Emite um certificado no padrao e-CODIGO.
	 * @param ca Objeto que representa a CA emissora (issuer).
	 * @param certOutFileName Nome do arquivo de certificado a ser gerado.
	 * @param keyOutFileName Nome do arquivo de chave privada a ser gerado.
	 * @param reqOutFileName Nome do arquivo de requisicao a ser gerado.
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param caPassword Senha da CA.
	 * @param nomePJ Nome empresarial.
	 * @param nome Nome do responsavel.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @throws Exception
	 */
	public void createEcodigo(Ca ca, String certOutFileName, String keyOutFileName,
			String reqOutFileName, String keyPassword, String caPassword,
			String nomePJ, String nome, String cpf,
			String email, Date nascimento, String pisPasep, String rg,
			String rgOrgEmissor, String rgUF, String cnpj,
			String subjC, String subjO, String subjOU) throws Exception {
		
		StringBuilder subject = buildSubject(nomePJ, cnpj, subjC, subjO, subjOU);
		
		Map<String, String> subjAlt =
			buildEcodigoAltSubject(cpf, email, nascimento, pisPasep, rg,
				rgOrgEmissor, rgUF, cnpj, nomePJ, nome);

		createEcodigoConfFile(ca, subjAlt);
		
		caDAO.createUserCert(ca, certOutFileName, keyOutFileName, reqOutFileName,
				userExpireDays, userKeySize, userAlgHash,
				keyPassword, caPassword, subject.toString());
	}

	/**
	 * Emite um certificado no padrao e-SERVIDOR.
	 * @param ca Objeto que representa a CA emissora (issuer).
	 * @param certOutFileName Nome do arquivo de certificado a ser gerado.
	 * @param keyOutFileName Nome do arquivo de chave privada a ser gerado.
	 * @param reqOutFileName Nome do arquivo de requisicao a ser gerado.
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param caPassword Senha da CA.
	 * @param nomeDNS Nome de DNS do servidor.
	 * @param nomePJ Nome empresarial.
	 * @param guid GUID do servidor.
	 * @param nome Nome do responsavel.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @throws Exception
	 */
	public void createEservidor(Ca ca, String certOutFileName, String keyOutFileName,
			String reqOutFileName, String keyPassword, String caPassword,
			String nomeDNS, String nomePJ, String guid,
			String nome, String cpf, String email, Date nascimento,
			String pisPasep, String rg,	String rgOrgEmissor, String rgUF, String cnpj,
			String subjC, String subjO, String subjOU) throws Exception {
		
		StringBuilder subject = buildSubject(nomeDNS, null, subjC, subjO, subjOU);
		
		Map<String, String> subjAlt =
			buildEservidorAltSubject(cpf, email, nascimento, pisPasep, rg,
				rgOrgEmissor, rgUF, cnpj, nomeDNS, nomePJ, nome, guid);

		createEservidorConfFile(ca, subjAlt);
		
		caDAO.createUserCert(ca, certOutFileName, keyOutFileName, reqOutFileName,
				userExpireDays, userKeySize, userAlgHash,
				keyPassword, caPassword, subject.toString());
	}

	/**
	 * Emite um certificado no padrao e-APLICACAO.
	 * @param ca Objeto que representa a CA emissora (issuer).
	 * @param certOutFileName Nome do arquivo de certificado a ser gerado.
	 * @param keyOutFileName Nome do arquivo de chave privada a ser gerado.
	 * @param reqOutFileName Nome do arquivo de requisicao a ser gerado.
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param caPassword Senha da CA.
	 * @param nomeApp Nome da aplicacao.
	 * @param nomePJ Nome empresarial.
	 * @param nome Nome do responsavel.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @throws Exception
	 */
	public void createEaplicacao(Ca ca, String certOutFileName, String keyOutFileName,
			String reqOutFileName, String keyPassword, String caPassword, String nomeApp,
			String nomePJ, String nome, String cpf, String email, Date nascimento,
			String pisPasep, String rg, String rgOrgEmissor, String rgUF, String cnpj,
			String subjC, String subjO, String subjOU) throws Exception {
		
		StringBuilder subject = buildSubject(nomeApp, cnpj, subjC, subjO, subjOU);
		
		Map<String, String> subjAlt =
			buildEaplicacaoAltSubject(cpf, email, nascimento, pisPasep, rg,
				rgOrgEmissor, rgUF, cnpj, nomePJ, nome);

		createEaplicacaoConfFile(ca, subjAlt);
		
		caDAO.createUserCert(ca, certOutFileName, keyOutFileName, reqOutFileName,
				userExpireDays, userKeySize, userAlgHash,
				keyPassword, caPassword, subject.toString());
	}
	
	/* Cria um "Subject Alernative Name" para emissao de certificados padrao e-CPF.
	 * @param cpf Numero do CPF, sem pontuacao (11 numeros).
	 * @param email Endereco de email.
	 * @param nascimento Data de nascimento.
	 * @param pisPasep Numero do PIS/PASEP/NIS, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG (2 caracteres).
	 * @param cei Numero do CEI (12 numeros).
	 * @param titulo Numero do Titulo de Eleitor (max. 12 numeros).
	 * @param tituloZona Zona Eleitoral do Titulo (max. 3 numeros).
	 * @param tituloSecao Secao Eleitoral do Titulo (max. 4 numeros).
	 * @param tituloMunicipio Municipio do Titulo Eleitoral (max. 20 caracteres).
	 * @param tituloUF UF do Titulo Eleitoral (2 caracteres).
	 * @param login Nome de Usuario (login de rede).
	 * @return Map contendo os campos da secao "Subject Alternative Name".
	 */
	private Map<String, String> buildEcpfAltSubject(String cpf, String email,
			Date nascimento, String pisPasep, String rg, String rgOrgEmissor,
			String rgUF, String cei, String titulo, String tituloZona,
			String tituloSecao,	String tituloMunicipio, String tituloUF, String login) {
		
		Map<String, String> subjAlt = new HashMap<String, String>();
		
		String strNasc = null;
		try {
			strNasc = (nascimento != null) ?
				new SimpleDateFormat("ddMMyyyy").format(nascimento) : null;
		} catch (Exception e) {	}
		
		if (email           == null                     ) { email           = ""; }
		if (strNasc         == null                     ) { strNasc         = ""; } 
		if (cpf             == null                     ) { cpf             = ""; }
		if (pisPasep        == null                     ) { pisPasep        = ""; }
		if (rg              == null                     ) { rg              = ""; }
		if (rgOrgEmissor    == null || "".equals(rg)    ) { rgOrgEmissor    = ""; }
		if (rgUF            == null || "".equals(rg)    ) { rgUF            = ""; }
		if (titulo          == null                     ) { titulo          = ""; }
		if (tituloZona      == null || "".equals(titulo)) { tituloZona      = ""; }
		if (tituloSecao     == null || "".equals(titulo)) { tituloSecao     = ""; }
		if (tituloMunicipio == null || "".equals(titulo)) { tituloMunicipio = ""; }
		if (tituloUF        == null || "".equals(titulo)) { tituloUF        = ""; }
		if (cei             == null                     ) { cei             = ""; }
		if (login           == null                     ) { login           = ""; }
		
		strNasc         = formatNumberForAltSubject(strNasc        ,  8);
		cpf             = formatNumberForAltSubject(cpf            , 11);
		pisPasep        = formatNumberForAltSubject(pisPasep       , 11);
		rg              = formatNumberForAltSubject(rg             , 15);
		rgOrgEmissor    = formatTextForAltSubject  (rgOrgEmissor   ,  4);
		rgUF            = formatTextForAltSubject  (rgUF           ,  2);
		titulo          = formatNumberForAltSubject(titulo         , 12);
		tituloZona      = formatNumberForAltSubject(tituloZona     ,  3);
		tituloSecao     = formatNumberForAltSubject(tituloSecao    ,  4);
		tituloMunicipio = formatTextForAltSubject  (tituloMunicipio, 20);
		tituloUF        = formatTextForAltSubject  (tituloUF       ,  2);
		cei             = formatNumberForAltSubject(cei            , 12);
		
		subjAlt.put("email", 
				String.format(SUBJ_ALT_EMAIL_PATTERN, email));
		
		subjAlt.put("otherName.0",
				String.format(SUBJ_ALT_2_16_76_1_3_1_PATTERN,
					strNasc, cpf, pisPasep, rg, rgOrgEmissor + rgUF));

		subjAlt.put("otherName.1",
				String.format(SUBJ_ALT_2_16_76_1_3_5_PATTERN,
					titulo, tituloZona, tituloSecao, tituloMunicipio + tituloUF));
		
		subjAlt.put("otherName.2",
				String.format(SUBJ_ALT_2_16_76_1_3_6_PATTERN, cei));

		subjAlt.put("otherName.3",
				String.format(SUBJ_ALT_1_3_6_1_4_1_311_20_2_3_PATTERN, login));
		
		return subjAlt;
	}

	/* Cria um "Subject Alernative Name" para emissao de certificados padrao e-CNPJ.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cei Numero do CEI do responsavel (12 numeros).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param nome Nome do responsavel.
	 * @return Map contendo os campos da secao "Subject Alternative Name".
	 */
	private Map<String, String> buildEcnpjAltSubject(String cpf, String email,
			Date nascimento, String pisPasep, String rg, String rgOrgEmissor,
			String rgUF, String cei, String cnpj, String nome) {
		
		Map<String, String> subjAlt = new HashMap<String, String>();

		String strNasc = null;
		try {
			strNasc = (nascimento != null) ?
				new SimpleDateFormat("ddMMyyyy").format(nascimento) : null;
		} catch (Exception e) {	}
		
		if (email           == null                     ) { email           = ""; }
		if (strNasc         == null                     ) { strNasc         = ""; } 
		if (cpf             == null                     ) { cpf             = ""; }
		if (pisPasep        == null                     ) { pisPasep        = ""; }
		if (rg              == null                     ) { rg              = ""; }
		if (rgOrgEmissor    == null || "".equals(rg)    ) { rgOrgEmissor    = ""; }
		if (rgUF            == null || "".equals(rg)    ) { rgUF            = ""; }
		if (cnpj            == null                     ) { cnpj            = ""; }
		if (cei             == null                     ) { cei             = ""; }
		
		strNasc         = formatNumberForAltSubject(strNasc        ,  8);
		cpf             = formatNumberForAltSubject(cpf            , 11);
		pisPasep        = formatNumberForAltSubject(pisPasep       , 11);
		rg              = formatNumberForAltSubject(rg             , 15);
		rgOrgEmissor    = formatTextForAltSubject  (rgOrgEmissor   ,  4);
		rgUF            = formatTextForAltSubject  (rgUF           ,  2);
		cei             = formatNumberForAltSubject(cei            , 12);
		cnpj            = formatNumberForAltSubject(cnpj           , 14);

		subjAlt.put("email", 
				String.format(SUBJ_ALT_EMAIL_PATTERN, email));
		
		subjAlt.put("otherName.0",
				String.format(SUBJ_ALT_2_16_76_1_3_4_PATTERN,
					strNasc, cpf, pisPasep, rg, rgOrgEmissor + rgUF));

		subjAlt.put("otherName.1",
				String.format(SUBJ_ALT_2_16_76_1_3_2_PATTERN, nome));

		subjAlt.put("otherName.2",
				String.format(SUBJ_ALT_2_16_76_1_3_3_PATTERN, cnpj));

		subjAlt.put("otherName.3",
				String.format(SUBJ_ALT_2_16_76_1_3_7_PATTERN, cei));
		
		return subjAlt;
	}

	/* Cria um "Subject Alernative Name" para emissao de certificados padrao RIC.
	 * @param ric Numero do RIC, sem pontuacao (11 numeros).
	 * @param cpf Numero do CPF, sem pontuacao (11 numeros).
	 * @param email Endereco de email.
	 * @param nascimento Data de nascimento.
	 * @param pisPasep Numero do PIS/PASEP/NIS, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG (2 caracteres).
	 * @param cei Numero do CEI (12 numeros).
	 * @param titulo Numero do Titulo de Eleitor (max. 12 numeros).
	 * @param tituloZona Zona Eleitoral do Titulo (max. 3 numeros).
	 * @param tituloSecao Secao Eleitoral do Titulo (max. 4 numeros).
	 * @param tituloMunicipio Municipio do Titulo Eleitoral (max. 20 caracteres).
	 * @param tituloUF UF do Titulo Eleitoral (2 caracteres).
	 * @param login Nome de Usuario (login de rede).
	 * @return Map contendo os campos da secao "Subject Alternative Name".
	 */
	private Map<String, String> buildRicAltSubject(String ric, String cpf,
			String email, Date nascimento, String pisPasep, String rg,
			String rgOrgEmissor, String rgUF, String cei, String titulo,
			String tituloZona, String tituloSecao, String tituloMunicipio,
			String tituloUF, String login) {
		
		Map<String, String> subjAlt = new HashMap<String, String>();

		String strNasc = null;
		try {
			strNasc = (nascimento != null) ?
				new SimpleDateFormat("ddMMyyyy").format(nascimento) : null;
		} catch (Exception e) {	}
		
		if (email           == null                     ) { email           = ""; }
		if (strNasc         == null                     ) { strNasc         = ""; } 
		if (ric             == null                     ) { ric             = ""; }
		if (cpf             == null                     ) { cpf             = ""; }
		if (pisPasep        == null                     ) { pisPasep        = ""; }
		if (rg              == null                     ) { rg              = ""; }
		if (rgOrgEmissor    == null || "".equals(rg)    ) { rgOrgEmissor    = ""; }
		if (rgUF            == null || "".equals(rg)    ) { rgUF            = ""; }
		if (titulo          == null                     ) { titulo          = ""; }
		if (tituloZona      == null || "".equals(titulo)) { tituloZona      = ""; }
		if (tituloSecao     == null || "".equals(titulo)) { tituloSecao     = ""; }
		if (tituloMunicipio == null || "".equals(titulo)) { tituloMunicipio = ""; }
		if (tituloUF        == null || "".equals(titulo)) { tituloUF        = ""; }
		if (cei             == null                     ) { cei             = ""; }
		if (login           == null                     ) { login           = ""; }
		
		strNasc         = formatNumberForAltSubject(strNasc        ,  8);
		cpf             = formatNumberForAltSubject(cpf            , 11);
		pisPasep        = formatNumberForAltSubject(pisPasep       , 11);
		rg              = formatNumberForAltSubject(rg             , 15);
		rgOrgEmissor    = formatTextForAltSubject  (rgOrgEmissor   ,  4);
		rgUF            = formatTextForAltSubject  (rgUF           ,  2);
		titulo          = formatNumberForAltSubject(titulo         , 12);
		tituloZona      = formatNumberForAltSubject(tituloZona     ,  3);
		tituloSecao     = formatNumberForAltSubject(tituloSecao    ,  4);
		tituloMunicipio = formatTextForAltSubject  (tituloMunicipio, 20);
		tituloUF        = formatTextForAltSubject  (tituloUF       ,  2);
		cei             = formatNumberForAltSubject(cei            , 12);
		ric             = formatNumberForAltSubject(ric            , 11);

		subjAlt.put("email", 
				String.format(SUBJ_ALT_EMAIL_PATTERN, email));
		
		subjAlt.put("otherName.0",
				String.format(SUBJ_ALT_2_16_76_1_3_1_PATTERN,
					strNasc, cpf, pisPasep, rg, rgOrgEmissor + rgUF));

		subjAlt.put("otherName.1",
				String.format(SUBJ_ALT_2_16_76_1_3_5_PATTERN,
					titulo, tituloZona, tituloSecao, tituloMunicipio + tituloUF));
		
		subjAlt.put("otherName.2",
				String.format(SUBJ_ALT_2_16_76_1_3_6_PATTERN, cei));

		subjAlt.put("otherName.3",
				String.format(SUBJ_ALT_1_3_6_1_4_1_311_20_2_3_PATTERN, login));
		
		subjAlt.put("otherName.4",
				String.format(SUBJ_ALT_2_16_76_1_3_9_PATTERN, ric));

		return subjAlt;
	}

	/* Cria um "Subject Alernative Name" para emissao de certificados padrao e-CODIGO.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param nomePJ Nome empresarial.
	 * @param nome Nome do responsavel.
	 * @return Map contendo os campos da secao "Subject Alternative Name".
	 */
	private Map<String, String> buildEcodigoAltSubject(String cpf, String email,
			Date nascimento, String pisPasep, String rg, String rgOrgEmissor,
			String rgUF, String cnpj, String nomePJ, String nome) {

		Map<String, String> subjAlt = new HashMap<String, String>();

		String strNasc = null;
		try {
			strNasc = (nascimento != null) ?
				new SimpleDateFormat("ddMMyyyy").format(nascimento) : null;
		} catch (Exception e) {	}
		
		if (email           == null                     ) { email           = ""; }
		if (strNasc         == null                     ) { strNasc         = ""; } 
		if (cpf             == null                     ) { cpf             = ""; }
		if (pisPasep        == null                     ) { pisPasep        = ""; }
		if (rg              == null                     ) { rg              = ""; }
		if (rgOrgEmissor    == null || "".equals(rg)    ) { rgOrgEmissor    = ""; }
		if (rgUF            == null || "".equals(rg)    ) { rgUF            = ""; }
		if (cnpj            == null                     ) { cnpj            = ""; }
		
		strNasc         = formatNumberForAltSubject(strNasc        ,  8);
		cpf             = formatNumberForAltSubject(cpf            , 11);
		pisPasep        = formatNumberForAltSubject(pisPasep       , 11);
		rg              = formatNumberForAltSubject(rg             , 15);
		rgOrgEmissor    = formatTextForAltSubject  (rgOrgEmissor   ,  4);
		rgUF            = formatTextForAltSubject  (rgUF           ,  2);
		cnpj            = formatNumberForAltSubject(cnpj           , 14);

		subjAlt.put("email", 
				String.format(SUBJ_ALT_EMAIL_PATTERN, email));
		
		subjAlt.put("otherName.0",
				String.format(SUBJ_ALT_2_16_76_1_3_4_PATTERN,
					strNasc, cpf, pisPasep, rg, rgOrgEmissor + rgUF));

		subjAlt.put("otherName.1",
				String.format(SUBJ_ALT_2_16_76_1_3_2_PATTERN, nome));

		subjAlt.put("otherName.2",
				String.format(SUBJ_ALT_2_16_76_1_3_3_PATTERN, cnpj));

		subjAlt.put("otherName.3",
				String.format(SUBJ_ALT_2_16_76_1_3_8_PATTERN, nomePJ));

		return subjAlt;
	}
	
	/* Cria um "Subject Alernative Name" para emissao de certificados padrao e-SERVIDOR.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param nomeDNS Nome de DNS do servidor.
	 * @param nomePJ Nome empresarial.
	 * @param nome Nome do responsavel.
	 * @param guid GUID do servidor.
	 * @return Map contendo os campos da secao "Subject Alternative Name".
	 */
	private Map<String, String> buildEservidorAltSubject(String cpf, String email,
			Date nascimento, String pisPasep, String rg, String rgOrgEmissor, String rgUF,
			String cnpj, String nomeDNS, String nomePJ, String nome, String guid) {

		Map<String, String> subjAlt = new HashMap<String, String>();

		String strNasc = null;
		try {
			strNasc = (nascimento != null) ?
				new SimpleDateFormat("ddMMyyyy").format(nascimento) : null;
		} catch (Exception e) {	}
		
		if (email           == null                     ) { email           = ""; }
		if (strNasc         == null                     ) { strNasc         = ""; } 
		if (cpf             == null                     ) { cpf             = ""; }
		if (pisPasep        == null                     ) { pisPasep        = ""; }
		if (rg              == null                     ) { rg              = ""; }
		if (rgOrgEmissor    == null || "".equals(rg)    ) { rgOrgEmissor    = ""; }
		if (rgUF            == null || "".equals(rg)    ) { rgUF            = ""; }
		if (cnpj            == null                     ) { cnpj            = ""; }
		if (guid            == null                     ) { guid            = ""; }
		
		strNasc         = formatNumberForAltSubject(strNasc        ,  8);
		cpf             = formatNumberForAltSubject(cpf            , 11);
		pisPasep        = formatNumberForAltSubject(pisPasep       , 11);
		rg              = formatNumberForAltSubject(rg             , 15);
		rgOrgEmissor    = formatTextForAltSubject  (rgOrgEmissor   ,  4);
		rgUF            = formatTextForAltSubject  (rgUF           ,  2);
		cnpj            = formatNumberForAltSubject(cnpj           , 14);
		guid            = formatNumberForAltSubject(guid           , 32);

		subjAlt.put("email", 
				String.format(SUBJ_ALT_EMAIL_PATTERN, email));
		
		subjAlt.put("otherName.0",
				String.format(SUBJ_ALT_2_16_76_1_3_4_PATTERN,
					strNasc, cpf, pisPasep, rg, rgOrgEmissor + rgUF));

		subjAlt.put("otherName.1",
				String.format(SUBJ_ALT_2_16_76_1_3_2_PATTERN, nome));

		subjAlt.put("otherName.2",
				String.format(SUBJ_ALT_2_16_76_1_3_3_PATTERN, cnpj));

		subjAlt.put("otherName.3",
				String.format(SUBJ_ALT_2_16_76_1_3_8_PATTERN, nomePJ));

		subjAlt.put("otherName.4",
				String.format(SUBJ_ALT_1_3_6_1_4_1_311_25_1_PATTERN, guid));
		
		subjAlt.put("DNS", 
				String.format(SUBJ_ALT_DNS_PATTERN, nomeDNS));

		return subjAlt;
	}

	/* Cria um "Subject Alernative Name" para emissao de certificados padrao e-APLICACAO.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param nomePJ Nome empresarial.
	 * @param nome Nome do responsavel.
	 * @return Map contendo os campos da secao "Subject Alternative Name".
	 */
	private Map<String, String> buildEaplicacaoAltSubject(String cpf, String email,
			Date nascimento, String pisPasep, String rg, String rgOrgEmissor,
			String rgUF, String cnpj, String nomePJ, String nome) {

		return buildEcodigoAltSubject(cpf, email, nascimento, pisPasep,
				rg, rgOrgEmissor, rgUF, cnpj, nomePJ, nome);
	}

	/* Cria arquivo de configuracao de usuario do OpenSSL para emissao de e-CPF.
	 * @param ca Objeto que representa a CA.
	 * @param subjAlt Subject Alernative Name para o certificado.
	 * @throws Exception
	 */
	private void createEcpfConfFile(Ca ca,
			Map<String, String> subjAlt) throws Exception {

		createUserConfFile(ca, subjAlt);
		File f = new File(ca.getUserConfFileName());
		Ini confFile = new Ini(f);
		Ini.Section sect;
		sect = confFile.get("v3_req");
			sect.put("extendedKeyUsage", ECPF_EXTENDED_KEY_USAGE);
		sect = confFile.get("v3_ca"); 
			sect.put("extendedKeyUsage", ECPF_EXTENDED_KEY_USAGE);
		sect = confFile.get("polsection"); 
			sect.put("policyIdentifier", cpsCaA3PF);
		confFile.store();
	}

	/* Cria arquivo de configuracao de usuario do OpenSSL para emissao de e-CNPJ.
	 * @param ca Objeto que representa a CA.
	 * @param subjAlt Subject Alernative Name para o certificado.
	 * @throws Exception
	 */
	private void createEcnpjConfFile(Ca ca,
			Map<String, String> subjAlt) throws Exception {
		
		createUserConfFile(ca, subjAlt);
		File f = new File(ca.getUserConfFileName());
		Ini confFile = new Ini(f);
		Ini.Section sect;
		sect = confFile.get("v3_req");
			sect.put("extendedKeyUsage", ECNPJ_EXTENDED_KEY_USAGE);
		sect = confFile.get("v3_ca"); 
			sect.put("extendedKeyUsage", ECNPJ_EXTENDED_KEY_USAGE);
		sect = confFile.get("polsection"); 
			sect.put("policyIdentifier", cpsCaA3PJ);
		confFile.store();
	}

	/* Cria arquivo de configuracao de usuario do OpenSSL para emissao de RIC.
	 * @param ca Objeto que representa a CA.
	 * @param subjAlt Subject Alernative Name para o certificado.
	 * @throws Exception
	 */
	private void createRicConfFile(Ca ca,
			Map<String, String> subjAlt) throws Exception {
		createEcpfConfFile(ca, subjAlt);
	}
	
	/* Cria arquivo de configuracao de usuario do OpenSSL para emissao de e-CODIGO.
	 * @param ca Objeto que representa a CA.
	 * @param subjAlt Subject Alernative Name para o certificado.
	 * @throws Exception
	 */
	private void createEcodigoConfFile(Ca ca,
			Map<String, String> subjAlt) throws Exception {
		
		createEcnpjConfFile(ca, subjAlt);
		File f = new File(ca.getUserConfFileName());
		Ini confFile = new Ini(f);
		Ini.Section sect;
		sect = confFile.get("v3_req"); 
			sect.put("extendedKeyUsage", ECODIGO_EXTENDED_KEY_USAGE);
		sect = confFile.get("v3_ca"); 
			sect.put("extendedKeyUsage", ECODIGO_EXTENDED_KEY_USAGE);
		confFile.store();
	}

	/* Cria arquivo de configuracao de usuario do OpenSSL para emissao de e-APLICACAO.
	 * @param ca Objeto que representa a CA.
	 * @param subjAlt Subject Alernative Name para o certificado.
	 * @throws Exception
	 */
	private void createEaplicacaoConfFile(Ca ca,
			Map<String, String> subjAlt) throws Exception {
		createEcodigoConfFile(ca, subjAlt);
	}

	/* Cria arquivo de configuracao de usuario do OpenSSL para emissao de e-SERVIDOR.
	 * @param ca Objeto que representa a CA.
	 * @param subjAlt Subject Alernative Name para o certificado.
	 * @throws Exception
	 */
	private void createEservidorConfFile(Ca ca,
			Map<String, String> subjAlt) throws Exception {
		
		createEcnpjConfFile(ca, subjAlt);
		File f = new File(ca.getUserConfFileName());
		Ini confFile = new Ini(f);
		Ini.Section sect;
		sect = confFile.get("v3_req"); 
			sect.put("extendedKeyUsage", ESERVIDOR_EXTENDED_KEY_USAGE);
		sect = confFile.get("v3_ca"); 
			sect.put("extendedKeyUsage", ESERVIDOR_EXTENDED_KEY_USAGE);
		confFile.store();
	}

	/* Cria arquivo de configuracao de usuario do OpenSSL para emissao de certificados.
	 * @param ca Objeto que representa a CA.
	 * @param subjAlt Subject Alernative Name para o certificado.
	 * @throws Exception
	 */
	private void createUserConfFile(Ca ca,
			Map<String, String> subjAlt) throws Exception {
		
		File f = new File(ca.getUserConfFileName());
		Ini confFile = new Ini(f);
		Ini.Section sect;
		sect = confFile.get("v3_req");
			sect.put("basicConstraints", "CA:FALSE");
			sect.put("keyUsage", "cRLSign, digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment, keyAgreement, keyCertSign, cRLSign");
			sect.put("subjectKeyIdentifier", "hash");
			sect.put("certificatePolicies", "@polsection");
		sect = confFile.get("v3_ca"); 
			sect.put("basicConstraints", "critical,CA:FALSE");
			sect.put("keyUsage", "digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment, keyAgreement, keyCertSign, cRLSign");
			sect.put("authorityKeyIdentifier", "keyid,issuer:always");
			sect.put("certificatePolicies", "@polsection");
			sect.put("subjectAltName", "@subject_alt_section");
		sect = confFile.get("subject_alt_section");
			sect.clear();
			for (String key : subjAlt.keySet()) {
				sect.put(key, subjAlt.get(key));
			}
		confFile.store();
	}

	/* Constroi um subject (DN - Distinguished Name), padrao ICP-Brasil.
	 * @param cn1 Subject (DN): Common Name (CN), parte 1 (nome). 
	 * @param cn2 Subject (DN): Common Name (CN), parte 2 (cpf ou cnpj).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @param subjL Subject (DN): Locality (L).
	 * @param subjST Subject (DN): State (ST).
	 * @return Objeto StringBuilder que representa o subject, com os campos anexados.
	 * @see <a href="http://www.x500standard.com/">X500 Standard</a>
	 */
	private StringBuilder buildSubject(String cn1, String cn2,
			String subjC, String subjO, String subjOU,
			String subjL, String subjST) {
		
		StringBuilder subject;
		subject = OpenSSLWrapper.subjectAppend(null   , "C" , subjC );
		subject = OpenSSLWrapper.subjectAppend(subject, "O" , subjO );
		subject = OpenSSLWrapper.subjectAppend(subject, "L" , subjL );
		subject = OpenSSLWrapper.subjectAppend(subject, "ST", subjST);
		subject = OpenSSLWrapper.subjectAppend(subject, "OU", subjOU);
		
		StringBuilder cn = new StringBuilder(cn1);
		if (cn2 != null && !"".equals(cn2)) {
			cn.append(":");
			cn.append(cn2);
		}
		subject = OpenSSLWrapper.subjectAppend(subject, "CN", cn.toString());
		return subject;
	}

	/* Constroi um subject (DN - Distinguished Name), padrao ICP-Brasil.
	 * @param cn1 Subject (DN): Common Name (CN), parte 1 (nome). 
	 * @param cn2 Subject (DN): Common Name (CN), parte 2 (cpf / cnpj).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @return Objeto StringBuilder que representa o subject, com os campos anexados.
	 * @see <a href="http://www.x500standard.com/">X500 Standard</a>
	 */
	private StringBuilder buildSubject(String cn1, String cn2,
			String subjC, String subjO, String subjOU) {
		
		return buildSubject(cn1, cn2, subjC, subjO, subjOU, null, null);
	}

	/* Formata um campo numerico no padrao ICP-Brasil.
	 * @param number String que representa um valor numerico.
	 * @param len Tamanho desejado para o campo, em digitos.
	 * @return String com o campo numerico formatado,
	 *   preenchido com '0' a esquerda, se necessario.
	 */
	private String formatNumberForAltSubject(String number, int len) {
		char patternArray[] = new char[len];  
		Arrays.fill(patternArray,'0'); 
		String pattern = String.valueOf(patternArray);
		int strLen = number.length();
		return (pattern + number).substring(strLen, pattern.length() + strLen);
	}
	
	/* Formata um campo texto no padrao ICP-Brasil.
	 * @param text String que representa um valor texto.
	 * @param len Tamanho desejado para o campo, em caracteres.
	 * @return String com o campo texto formatado,
	 *   preenchido com espacos a direita, se necessario.
	 */
	private String formatTextForAltSubject(String text, int len) {
		char patternArray[] = new char[len];  
		Arrays.fill(patternArray,' '); 
		String pattern = String.valueOf(patternArray);
		return (text + pattern).substring(0, pattern.length());
	}
	
}
