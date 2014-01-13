package icp.dao;

import icp.bean.Ca;
import icp.util.OpenSSLWrapper;
import icp.util.SystemWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.ini4j.Ini;

/**
 * Gerencia uma Autoridade Certificadora Raiz.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class RootCaDAO {
	
	/** OID default da Politica de Certificacao da CA Raiz. */
	public static final String CPS_ROOT_CA = "2.16.76.1.1.0";
	/** OID default da Politica de Certificacao da CA para emitir certificados padrao A3. */
	public static final String CPS_CA_A3   = "2.16.76.1.2.3.0";
	
	/* Timeout para esperar termino de processo do Sistema Operacional, em ms */
	protected static final long PROCESS_WAIT_TIMEOUT = 120000;
	
	/* oid da politica de certificacao da CA Raiz */
	protected String cpsRootCa;
	/* oid da politica de certificacao da CA para Cert.A3 */
	protected String cpsCaA3;

	/* diretorio base (raiz) das CA's */
	protected String baseDir;
	/* Lista com as CA's existentes */ 
	protected List<Ca> caList = new ArrayList<Ca>();

	/* objeto para realizar log das operacoes */
	protected static Logger logger; 
	protected static boolean trace;

	/**
	 * Cria uma instancia do gerenciador de CA Raiz.
	 * @param baseDir Diretorio base (raiz) das CA's.
	 */
	public RootCaDAO(String baseDir) {
		this.baseDir   = baseDir;
		this.cpsRootCa = CPS_ROOT_CA;
		this.cpsCaA3   = CPS_CA_A3;
		logger = LogManager.getLogger(RootCaDAO.class);
		trace = logger.isTraceEnabled();
		loadCAList();
	}

	/**
	 * Retorna uma CA, a partir de uma lista, pelo nome.
	 * @param caList Lista com objetos CA.
	 * @param name Nome da CA.
	 * @return Objeto da CA, ou null se nao encontrada.
	 */
	public static Ca getCaFromListByName(List<Ca> caList, String name) {
		for (Ca ca : caList) {
			if (ca.getName().equals(name)) { return ca;	}
		}
		return null;
	}
	
	/**
	 * Configura o OID da Politica de Certificacao da CA Raiz.
	 * @param value OID da Politica de Certificacao da CA Raiz.
	 */
	public void setCpsRootCa(String value) {
		this.cpsRootCa = value;
	}
	
	/**
	 * Configura o OID da Politica de Certificacao da CA para emitir certificados padrao A3.
	 * @param value OID da Politica de Certificacao da CA para emitir certificados padrao A3.
	 */
	public void setCpsCaA3(String value) {
		this.cpsCaA3 = value;
	}
	
	/**
	 * Retorna uma lista com as CA's existentes.
	 * @return Lista com as CA's existentes.
	 * @see #refreshListCA()
	 */
	public List<Ca> listCA() {
		return caList;
	}

	/**
	 * Atualiza a lista com as CA's existentes e retorna.
	 * @return Lista com as CA's existentes.
	 */
	public List<Ca> refreshListCA() {
		loadCAList();
		return caList;
	}

	/**
	 * Retorna uma CA pelo nome (case insensitive).
	 * @param name Nome da CA.
	 * @return Objeto que representa uma CA.
	 */
	public Ca getCA(String name) {
		for (Ca ca : caList) {
			if (name != null && name.equalsIgnoreCase(ca.getName())) {
				return ca;
			}
		}
		return null;
	}

	/**
	 * Cria uma nova CA Raiz.
	 * @param name Nome da CA Raiz.
	 * @param crlURI Caminho (URI) da Lista de Certificados Revogados (LCR).
	 * @param cpsURI Caminho (URI) do Documento com as Politicas de Certificacao (DPC).
	 * @param expireDays Duracao do certificado da CA Raiz, em dias.
	 * @param crlDays Atualizacao da Lista de Certificados Revogados (LCR), em dias.
	 * @param keySize Tamanho da chave de criptografia, em bits.
	 * @param algHash Algoritmo de hash a ser usado pelo certificado da CA Raiz.
	 * @param password Senha da CA Raiz, para criptografar a chave privada.
	 * @param subject String contendo o subject (DN - Distinguished Name) da CA Raiz.
	 * @return Objeto que representa a CA Raiz criada.
	 * @throws Exception
	 */
	public Ca createRootCA(String name, String crlURI, String cpsURI,
			int expireDays, int crlDays, int keySize, String algHash,
			String password, String subject) throws Exception {
		
		if (trace) {
			logger.trace(String.format("Creating Root CA: '%s'", name));
		}
		/* cria objeto CA */
		Ca ca = new Ca(name, baseDir);
		/* cria estrutura de diretorios da CA */
		if (trace) {
			logger.trace(String.format("Creating Dir Structure for '%s'", name));
		}
		try {
			createDirStructCA(ca);
		} catch (Exception e) {
			logger.error(String.format("Error for '%s': %s", 
					name, e.getLocalizedMessage()));
			throw e;
		}
		/* configura arquivos conf */
		if (trace) {
			logger.trace(String.format("Creating OpenSSL Config Files for '%s'", name));
		}
		try {
			createCaConfFile  (null, ca, crlURI, cpsURI, subject);
			createUserConfFile(null, ca, crlURI, cpsURI, subject);
			ca.refresh();
		} catch (Exception e) {
			logger.error(String.format("Error for '%s': %s", 
					name, e.getLocalizedMessage()));
			removeDirStructCA(ca);
			throw e;
		}
		/* cria cert auto-assinado */
		if (trace) {
			logger.trace(String.format("Creating Self-sign Certificate for '%s'", name));
		}
		Process p = null;
		String processErrorStr = null;
		try {
			p = OpenSSLWrapper.createAutoSignCert(ca.getConfFileName(), ca.getPrivKeyFile(),
					ca.getCertFile(), expireDays, keySize, algHash, password, subject);
			if (SystemWrapper.processWait(p, PROCESS_WAIT_TIMEOUT) != 0) {
				throw new Exception("Error in openssl command");
			}
		} catch (Exception e) {
			processErrorStr = (p != null) ? 
					("\nProcess STDERR:\n" + SystemWrapper.getProcessErrorStr(p)) : ""; 
			logger.error(String.format("Error for '%s': %s%s", 
					name, e.getLocalizedMessage(), processErrorStr));
			removeDirStructCA(ca);
			throw new Exception(e.getLocalizedMessage() + processErrorStr);
		}
		/* gera lcr */
		try {
			createCRL(ca, password, crlDays, algHash);
		} catch (Exception e) {
			removeDirStructCA(ca);
			throw e;
		}
		/* exporta cadeia de certificados */
		try {
			exportCertChain(ca, ca.getCertChainFile());
		} catch (Exception e) {
			removeDirStructCA(ca);
			throw e;
		}
		/* adiciona na lista de ca's */
		caList.add(ca);
		if (trace) {
			logger.trace(String.format("Root CA '%s' created", name));
		}
		return ca;
	}
	
	/**
	 * Remove uma CA.
	 * @param name Nome da CA a ser removida.
	 * @throws Exception
	 */
	public void deleteCA(String name) throws Exception {
		if (trace) {
			logger.trace(String.format("Deleting CA: '%s'", name));
		}
		refreshListCA();
		Ca ca = getCA(name);
		if (ca == null) {
			logger.error(String.format("CA '%s' not found", name));
			throw new FileNotFoundException("CA not found: " + name);
		}
		removeDirStructCA(ca);
		caList.remove(ca);
		if (trace) {
			logger.trace(String.format("CA '%s' deleted", name));
		}
	}
	
	/**
	 * Cria uma lista de certificados revogados (CRL).
	 * @param ca Objeto que representa a CA.
	 * @param password Senha da CA.
	 * @param crlDays Atualizacao da Lista de Certificados Revogados (LCR), em dias.
	 * @param algHash Algoritmo de hash a ser usado pela CRL da CA.
	 * @throws Exception
	 */
	public void createCRL(Ca ca, String password,
			int crlDays, String algHash) throws Exception {

		Process p = null;
		String processErrorStr = null;
		/* gera lcr */
		if (trace) {
			logger.trace(String.format("Creating CRL for '%s'", ca.getName()));
		}
		try {
			p = OpenSSLWrapper.generateCRL(ca.getConfFileName(), ca.getCertFile(), ca.getCrlFile(),
					crlDays, algHash, password);
			if (SystemWrapper.processWait(p, PROCESS_WAIT_TIMEOUT) != 0) {
				throw new Exception("Error in openssl command");
			}
		} catch (Exception e) {
			processErrorStr = (p != null) ? 
					("\nProcess STDERR:\n" + SystemWrapper.getProcessErrorStr(p)) : ""; 
			logger.error(String.format("Error for '%s': %s%s", 
					ca.getName(), e.getLocalizedMessage(), processErrorStr));
			throw new Exception(e.getLocalizedMessage() + processErrorStr);
		}
		if (trace) {
			logger.trace(String.format("CRL for '%s' created", ca.getName()));
		}
	}

	/**
	 * Cria uma requisicao de certificado para a propria CA.
	 * @param ca Objeto que representa a CA.
	 * @param reqOutFileName Nome do arquivo de requisicao a ser gerado.
	 * @param keyOutFileName Nome do arquivo de chave privada a ser gerado.
	 * @param keySize Tamanho da chave de criptografia, em bits.
	 * @param algHash Algoritmo de hash a ser usado.
	 * @param password Senha para criptografar a chave privada.
	 * @param subject String contendo o subject (DN - Distinguished Name) do certificado.
	 * @throws Exception
	 */
	public void createCaCertReq(Ca ca, String reqOutFileName, String keyOutFileName,
			int keySize, String algHash, String password, String subject) throws Exception {
		
		createCertReq(ca.getConfFileName(), reqOutFileName, keyOutFileName,
				keySize, algHash, password, subject);
	}

	/**
	 * Cria uma requisicao de certificado de um usuario para uma CA.
	 * @param ca Objeto que representa a CA.
	 * @param reqOutFileName Nome do arquivo de requisicao a ser gerado.
	 * @param keyOutFileName Nome do arquivo de chave privada a ser gerado.
	 * @param keySize Tamanho da chave de criptografia, em bits.
	 * @param algHash Algoritmo de hash a ser usado.
	 * @param password Senha para criptografar a chave privada.
	 * @param subject String contendo o subject (DN - Distinguished Name) do certificado.
	 * @throws Exception
	 */
	public void createUserCertReq(Ca ca, String reqOutFileName, String keyOutFileName,
			int keySize, String algHash, String password, String subject) throws Exception {
		
		createCertReq(ca.getUserConfFileName(), reqOutFileName, keyOutFileName,
				keySize, algHash, password, subject);
	}
	
	/**
	 * Assina uma requisicao de certificado da propria CA.
	 * @param ca Objeto que representa a CA.
	 * @param reqInFileName Nome do arquivo de requisicao.
	 * @param certOutFileName Nome do arquivo de certificado a ser gerado.
	 * @param expireDays Duracao do certificado, em dias.
	 * @param algHash Algoritmo de hash a ser usado.
	 * @param password Senha da CA.
	 * @throws Exception
	 */
	public void createCaCertByReq(Ca ca, String reqInFileName, String certOutFileName,
			int expireDays, String algHash, String password) throws Exception {

		createCertByReq(ca.getConfFileName(), ca, reqInFileName, certOutFileName,
				expireDays, algHash, password);
	}

	/**
	 * Assina uma requisicao de certificado de um usuario da CA.
	 * @param ca Objeto que representa a CA.
	 * @param reqInFileName Nome do arquivo de requisicao.
	 * @param certOutFileName Nome do arquivo de certificado a ser gerado.
	 * @param expireDays Duracao do certificado, em dias.
	 * @param algHash Algoritmo de hash a ser usado.
	 * @param password Senha da CA.
	 * @throws Exception
	 */
	public void createUserCertByReq(Ca ca, String reqInFileName, String certOutFileName,
			int expireDays, String algHash, String password) throws Exception {

		createCertByReq(ca.getUserConfFileName(), ca, reqInFileName, certOutFileName,
				expireDays, algHash, password);
	}

	/**
	 * Gera um novo certificado para a propria CA. 
	 * @param ca Objeto que representa a CA.
	 * @param certOutFileName Nome do arquivo de certificado a ser gerado.
	 * @param keyOutFileName Nome do arquivo de chave privada a ser gerado.
	 * @param reqOutFileName Nome do arquivo de requisicao a ser gerado.
	 * @param expireDays Duracao do certificado, em dias.
	 * @param keySize Tamanho da chave de criptografia, em bits.
	 * @param algHash Algoritmo de hash a ser usado.
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param caPassword Senha da CA.
	 * @param subject String contendo o subject (DN - Distinguished Name) do certificado.
	 * @throws Exception
	 */
	public void createCaCert(Ca ca, String certOutFileName, String keyOutFileName,
			String reqOutFileName, int expireDays, int keySize, String algHash,
			String keyPassword,	String caPassword, String subject) throws Exception {
		
		createCaCertReq(ca, reqOutFileName, keyOutFileName, keySize, algHash, keyPassword, subject);
		createCaCertByReq(ca, reqOutFileName, certOutFileName, expireDays, algHash, caPassword);
	}
	
	/**
	 * Gera um novo certificado para um usuario da CA. 
	 * @param ca Objeto que representa a CA.
	 * @param certOutFileName Nome do arquivo de certificado a ser gerado.
	 * @param keyOutFileName Nome do arquivo de chave privada a ser gerado.
	 * @param reqOutFileName Nome do arquivo de requisicao a ser gerado.
	 * @param expireDays Duracao do certificado, em dias.
	 * @param keySize Tamanho da chave de criptografia, em bits.
	 * @param algHash Algoritmo de hash a ser usado.
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param caPassword Senha da CA.
	 * @param subject String contendo o subject (DN - Distinguished Name) do certificado.
	 * @throws Exception
	 */
	public void createUserCert(Ca ca, String certOutFileName, String keyOutFileName,
			String reqOutFileName, int expireDays, int keySize, String algHash,
			String keyPassword,	String caPassword, String subject) throws Exception {
		
		createUserCertReq(ca, reqOutFileName, keyOutFileName, keySize, algHash, keyPassword, subject);
		createUserCertByReq(ca, reqOutFileName, certOutFileName, expireDays, algHash, caPassword);
	}
	
	/**
	 * Revoga um certificado emitido pela CA especificada.
	 * @param ca Objeto que representa a CA.
	 * @param certInFileName Nome do arquivo de certificado a ser revogado.
	 * @param caPassword Senha da CA.
	 * @throws Exception
	 */
	public void revokeCert(Ca ca, String certInFileName,
			String caPassword) throws Exception {

		if (trace) {
			logger.trace("Revoking Certificate");
		}
		try {
			Process p = OpenSSLWrapper.revokeCert(
					ca.getUserConfFileName(), certInFileName, ca.getCertFile(),
					ca.getPrivKeyFile(), caPassword);
			if (SystemWrapper.processWait(p, PROCESS_WAIT_TIMEOUT) != 0) {
				throw new Exception(
						"Error in OpenSSL: \n"
						+ SystemWrapper.getProcessErrorStr(p));
			}
		} catch (Exception e) {
			logger.error(String.format("Error in revoke certificate: %s", 
					e.getLocalizedMessage()));
			throw e;
		}
		if (trace) {
			logger.trace("Certificate revoked");
		}
	}
	
	/**
	 * Renova o certificado da CA Raiz [NAO IMPLEMENTADO].
	 * @param rootCa Objeto que representa a CA Raiz.
	 * @param certFileName Nome do arquivo de certificado a ser renovado.
	 * @param expireDays Duracao do certificado, em dias.
	 * @param algHash Algoritmo de hash a ser usado.
	 * @param password Senha da CA Raiz.
	 * @throws Exception
	 */
	public void renewRootCaCert(Ca rootCa, String certFileName,
			int expireDays, String algHash,	String password) throws Exception {
		/*
		 * TODO: Implementar o procedimento para renovacao do certificado de AC Raiz.
		 * Uma opcao e' o descrito em http://marc.info/?l=openssl-users&m=113292902213919
		 */
		logger.error("Error in renew root CA certificate: not implemented");
		throw new Exception("Not implemented");
	}
	
	/**
	 * Renova o certificado da propria CA.
	 * @param ca Objeto que representa a CA.
	 * @param certFileName Nome do arquivo de certificado a ser renovado.
	 * @param reqFileName Nome do arquivo de requisicao.
	 * @param expireDays Duracao do certificado, em dias.
	 * @param algHash Algoritmo de hash a ser usado.
	 * @param caPassword Senha da CA.
	 * @throws Exception
	 */
	public void renewCaCert(Ca ca, String certFileName, String reqFileName,
			int expireDays, String algHash,	String caPassword) throws Exception {

		revokeCert(ca, certFileName, caPassword);
		createCaCertByReq(ca, reqFileName, certFileName, expireDays, algHash, caPassword);
	}

	/**
	 * Renova um certificado de um usuario da CA.
	 * @param ca Objeto que representa a CA.
	 * @param certFileName Nome do arquivo de certificado a ser renovado.
	 * @param reqFileName Nome do arquivo de requisicao.
	 * @param expireDays Duracao do certificado, em dias.
	 * @param algHash Algoritmo de hash a ser usado.
	 * @param caPassword Senha da CA.
	 * @throws Exception
	 */
	public void renewUserCert(Ca ca, String certFileName, String reqFileName,
			int expireDays, String algHash, String caPassword) throws Exception {

		revokeCert(ca, certFileName, caPassword);
		createUserCertByReq(ca, reqFileName, certFileName, expireDays, algHash, caPassword);
	}
	
	/**
	 * Exporta cadeia de certificados da CA.
	 * @param ca Objeto que representa a CA.
	 * @param p7bFileName Nome do arquivo de cadeia de certificados (P7B) a ser gerado.
	 * @throws Exception
	 */
	public void exportCertChain(Ca ca, String p7bFileName) throws Exception {
		
		if (trace) {
			logger.trace(String.format("Exporting Certificate Chain for '%s'", ca.getName()));
		}
		
		List<String> certInFiles = new ArrayList<String>(); 
		certInFiles.add(ca.getCertFile());

		String root    = null;
		String rootDir = null;
		do {
			root    = ca.getRoot();
			rootDir = ca.getRootDir();
			if ("".equals(root)) { root = null; }
			if (root != null) {
				ca = new Ca(root, rootDir);
				certInFiles.add(ca.getCertFile());
			}
		} while (root != null);
		
		try {
			Process p = OpenSSLWrapper.exportCertChain(
					certInFiles.toArray(new String[0]), p7bFileName);
			if (SystemWrapper.processWait(p, PROCESS_WAIT_TIMEOUT) != 0) {
				throw new Exception(
						"Error in OpenSSL: \n"
						+ SystemWrapper.getProcessErrorStr(p));
			}
		} catch (Exception e) {
			logger.error(String.format("Error for '%s' cert chain export: %s", 
					ca.getName(), e.getLocalizedMessage()));
			throw e;
		}
		if (trace) {
			logger.trace("Certificate Chain exported");
		}
	}
	
	/**
	 * Exporta um certificado (e sua chave privada) para o formato PKCS#12.
	 * @param certInFileName Nome do arquivo de certificado a ser convertido.
	 * @param keyInFileName Nome do arquivo de chave (key).
	 * @param pfxFileName Nome do arquivo de certificado (PKCS#12) de destino.
	 * @param keyPassword Senha da chave privada.
	 * @param keyAlias Nome amigavel do par de chaves dentro do arquivo PKCS#12.
	 * @throws Exception
	 */
	public void exportCertPkcs12(String certInFileName, String keyInFileName,
			String pfxFileName, String keyPassword, String keyAlias) throws Exception {
		
		if (trace) {
			logger.trace(String.format("Exporting Certificate '%s' to PKCS#12", certInFileName));
		}
		try {
			Process p =
				OpenSSLWrapper.exportCertPkcs12(certInFileName, keyInFileName,
						pfxFileName, keyPassword, keyAlias);
			if (SystemWrapper.processWait(p, PROCESS_WAIT_TIMEOUT) != 0) {
				throw new Exception(
						"Error in OpenSSL: \n"
						+ SystemWrapper.getProcessErrorStr(p));
			}
		} catch (Exception e) {
			logger.error(String.format("Error in cert export: %s", 
					e.getLocalizedMessage()));
			throw e;
		}
		if (trace) {
			logger.trace("Certificate exported to PKCS#12");
		}
	}
	
	/**
	 * Exporta uma chave privada para um arquivo descriptografado (sem senha).
	 * @param keyInFileName Nome do arquivo de chave privada (key) criptografada.
	 * @param keyOutFileName Nome do arquivo de chave (key) a ser gerado.
	 * @param keyPassword Senha da chave privada criptografada.
	 * @throws Exception
	 */
	public void exportKeyNoPassword(String keyInFileName,
			String keyOutFileName, String keyPassword) throws Exception {

		if (trace) {
			logger.trace(String.format("Decrypting Privake Key '%s'", keyInFileName));
		}
		try {
			Process p =
				OpenSSLWrapper.exportKeyNoPassword(keyInFileName, keyOutFileName, keyPassword);
			if (SystemWrapper.processWait(p, PROCESS_WAIT_TIMEOUT) != 0) {
				throw new Exception(
						"Error in OpenSSL: \n"
						+ SystemWrapper.getProcessErrorStr(p));
			}
		} catch (Exception e) {
			logger.error(String.format("Error in key decrypt: %s", 
					e.getLocalizedMessage()));
			throw e;
		}
		if (trace) {
			logger.trace(String.format("Private Key decrypted in '%s'", keyOutFileName));
		}
	}
	
	/* Cria um arquivo de configuracao do OpenSSL para a CA.
	 * @param rootCa Objeto que representa a CA Raiz (issuer) desta CA.
	 * @param ca Objeto que representa a CA.
	 * @param crlURI Caminho (URI) da Lista de Certificados Revogados (LCR).
	 * @param cpsURI Caminho (URI) do Documento com as Politicas de Certificacao (DPC).
	 * @param subject String contendo o subject (DN - Distinguished Name) da CA Raiz.
	 * @throws Exception
	 */
	protected void createCaConfFile(Ca rootCa, Ca ca, String crlURI,
			String cpsURI, String subject) throws Exception {
		
		createConfFile(ca.getConfFileName(), rootCa, ca, crlURI, cpsURI, subject);
	}

	/* Cria um arquivo de configuracao do OpenSSL para usuario da CA.
	 * @param rootCa Objeto que representa a CA Raiz (issuer) desta CA.
	 * @param ca Objeto que representa a CA.
	 * @param crlURI Caminho (URI) da Lista de Certificados Revogados (LCR).
	 * @param cpsURI Caminho (URI) do Documento com as Politicas de Certificacao (DPC).
	 * @param subject String contendo o subject (DN - Distinguished Name) da CA Raiz.
	 * @throws Exception
	 */
	protected void createUserConfFile(Ca rootCa, Ca ca, String crlURI,
			String cpsURI, String subject) throws Exception {
		
		createConfFile(ca.getUserConfFileName(), rootCa, ca, crlURI, cpsURI, subject);
		
		File f = new File(ca.getUserConfFileName());
		Ini confFile = new Ini(f);
		Ini.Section sect;
		sect = confFile.get("v3_req");
			sect.put("basicConstraints", "CA:FALSE");
			sect.put("keyUsage", "cRLSign, digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment, keyAgreement, keyCertSign, cRLSign");
			sect.put("subjectKeyIdentifier", "hash");
			sect.put("crlDistributionPoints", "URI:" + crlURI);
			sect.put("certificatePolicies", "@polsection");
			sect.put("extendedKeyUsage", "serverAuth, clientAuth, codeSigning, emailProtection, timeStamping, 1.3.6.1.4.1.311.20.2.2");
		sect = confFile.get("v3_ca"); 
			sect.put("basicConstraints", "critical,CA:FALSE");
			sect.put("keyUsage", "digitalSignature,	nonRepudiation, keyEncipherment, dataEncipherment");
			sect.put("authorityKeyIdentifier", "keyid,issuer:always");
			sect.put("crlDistributionPoints", "URI:" + crlURI);
			sect.put("certificatePolicies", "@polsection");
			sect.put("extendedKeyUsage", "serverAuth, clientAuth, codeSigning, emailProtection, timeStamping, 1.3.6.1.4.1.311.20.2.2");
			sect.put("subjectAltName", "@subject_alt_section");
		sect = confFile.get("polsection"); 
			sect.put("policyIdentifier", cpsCaA3);
			sect.put("CPS.1", "\"" + cpsURI + "\"");
		sect = confFile.get("req"); 
			sect.put("default_bits", "1024");
		sect = confFile.add("subject_alt_section");
			sect.put("subjectAltName", "");
		confFile.store();
	}

	/* Cria a estrutura de diretorios e arquivos necessaria para abrigar uma CA.
	 * @param ca Objeto que representa a CA.
	 * @throws Exception
	 */
	protected void createDirStructCA(Ca ca) throws Exception {
		/* verifica se ja existe */
		loadCAList();
		if (getCA(ca.getName()) != null) {
			throw new IOException("CA already exists: " + ca.getName());
		}
		if (!SystemWrapper.mkDir(ca.getDir())) {
			throw new IOException("Error in mkdir(" + ca.getDir() + ")");
		}
		/* cria subdirs */
		//mkdir ${CATOP}/certs 
		if (!SystemWrapper.mkDir(ca.getCertsDir())) {
			throw new IOException("Error in mkdir(" + ca.getCertsDir() + ")");
		}
		//mkdir ${CATOP}/crl 
		if (!SystemWrapper.mkDir(ca.getCrlDir())) {
			throw new IOException("Error in mkdir(" + ca.getCrlDir() + ")");
		}
		//mkdir ${CATOP}/newcerts
		if (!SystemWrapper.mkDir(ca.getNewCertsDir())) {
			throw new IOException("Error in mkdir(" + ca.getNewCertsDir() + ")");
		}
		//mkdir ${CATOP}/private
		if (!SystemWrapper.mkDir(ca.getPrivateDir())) {
			throw new IOException("Error in mkdir(" + ca.getPrivateDir() + ")");
		}
		/* cria arquivos */
		try {
			//serial
			PrintWriter newFile;
			newFile = new PrintWriter(new FileWriter(ca.getSerialFile()));
			newFile.println("01");
			newFile.close();
			//serial_crl
			newFile = new PrintWriter(new FileWriter(ca.getCrlSerialFile()));
			newFile.println("01");
			newFile.close();
			// index.txt
			new File(ca.getDatabaseFile()).createNewFile();
		} catch (Exception e) {
			removeDirStructCA(ca);
			throw e;
		}
	}
	
	/* Remove uma estrutura de diretorios e todos arquivos de uma CA.
	 * @param ca Objeto que representa a CA.
	 * @throws Exception
	 */
	protected void removeDirStructCA(Ca ca) throws Exception {
		Process p = SystemWrapper.rmDir(ca.getDir());
		if (SystemWrapper.processWait(p, PROCESS_WAIT_TIMEOUT) != 0) {
			throw new Exception("Error in rm command:\n" 
					+ SystemWrapper.getProcessErrorStr(p));
		}
	}

	/* Carrega a lista de CA's existentes. */
	private void loadCAList() {
		caList.clear();
		Collection<String> dirList = SystemWrapper.lsDir(baseDir);
		for (String dirName : dirList) {
			Ca ca = new Ca(dirName, baseDir);
			caList.add(ca);
		}
	}

	/* Cria um arquivo de configuracao do OpenSSL para a CA.
	 * @param filename Nome do arquivo de configuracao a ser criado.
	 * @param rootCa Objeto que representa a CA Raiz (issuer) desta CA.
	 * @param ca Objeto que representa a CA.
	 * @param crlURI Caminho (URI) da Lista de Certificados Revogados (LCR).
	 * @param cpsURI Caminho (URI) do Documento com as Politicas de Certificacao (DPC).
	 * @param subject String contendo o subject (DN - Distinguished Name) da CA Raiz.
	 * @throws Exception
	 */
	private void createConfFile(String filename, Ca rootCa, Ca ca,
			String crlURI, String cpsURI, String subject) throws Exception {
		
		File f = new File(filename);
		f.createNewFile();
		Ini confFile = new Ini(f);
		Ini.Section sect;

		sect = confFile.add("icpAdmin");
			addConfAttrFromSubject(sect, subject, "CN");
			addConfAttrFromSubject(sect, subject, "C");
			addConfAttrFromSubject(sect, subject, "O");
			addConfAttrFromSubject(sect, subject, "OU");
			if (rootCa != null) {
				sect.put("root", rootCa.getName());
				sect.put("rootDir", rootCa.getBaseDir());
			} else {
				sect.put("root", "");
				sect.put("rootDir", "");
			}
		
		sect = confFile.add("ca");
			sect.put("default_ca", "CA_default");
		sect = confFile.add("CA_default");
			sect.put("dir", ca.getDir());
			sect.put("certs", "$dir/certs");
			sect.put("crl_dir", "$dir/crl");
			sect.put("database", "$dir/index.txt");
			sect.put("new_certs_dir", "$dir/newcerts");
			sect.put("certificate", "$dir/certs/cacert.pem.cer");
			sect.put("serial", "$dir/serial");
			sect.put("crl", "$dir/crl/lcr.pem.crl");
			sect.put("crlnumber", "$dir/crl/serial_crl");
			sect.put("private_key", "$dir/private/cakey.pem");
			sect.put("RANDFILE", "$dir/private/.rand");
			sect.put("x509_extensions", "v3_ca");
			sect.put("crl_extensions", "crl_ext");
			sect.put("default_days", "3650");
			sect.put("default_crl_days", "730");
			sect.put("default_md", "sha1");
			sect.put("preserve", "yes");
			sect.put("policy", "policy_anything");
		sect = confFile.add("policy_anything");
			sect.put("C", "supplied");
			sect.put("O", "supplied");
			sect.put("OU", "supplied");
			sect.put("L", "optional");
			sect.put("ST", "optional");
			sect.put("CN", "supplied");		
		sect = confFile.add("v3_req"); 
			sect.put("basicConstraints", "CA:TRUE");
			sect.put("keyUsage", "cRLSign, keyCertSign");
			sect.put("subjectKeyIdentifier", "hash");
			sect.put("crlDistributionPoints", "URI:" + crlURI);
			sect.put("certificatePolicies", "@polsection");
		sect = confFile.add("v3_ca"); 
			sect.put("basicConstraints", "critical,CA:TRUE");
			sect.put("keyUsage", "cRLSign, keyCertSign");
			sect.put("authorityKeyIdentifier", "hash");
			sect.put("subjectKeyIdentifier", "hash");
			sect.put("issuerAltName", "issuer:copy");
			sect.put("crlDistributionPoints", "URI:" + crlURI);
			sect.put("certificatePolicies", "@polsection");
		sect = confFile.add("polsection"); 
			sect.put("policyIdentifier", cpsRootCa);
			sect.put("CPS.0", "\"" + cpsURI + "\"");
		sect = confFile.add("crl_ext"); 
			sect.put("authorityKeyIdentifier", "keyid:always");		
		sect = confFile.add("req"); 
			sect.put("default_bits", "2048");
			sect.put("default_keyfile", "privkey.pem");
			sect.put("distinguished_name", "req_distinguished_name");
			sect.put("x509_extensions", "v3_req");
			sect.put("string_mask", "nombstr");
			sect.put("req_extensions", "v3_req");
			sect.put("attributes", "req_attributes");
		sect = confFile.add("req_distinguished_name"); 
			sect.put("countryName", "Country Name (2 letter code)");
			sect.put("countryName_default", "BR");
			sect.put("countryName_min", "2");
			sect.put("countryName_max", "2");
			sect.put("stateOrProvinceName", "State or Province Name (full name)");
			sect.put("stateOrProvinceName_default", "Some-State");
			sect.put("localityName", "Locality Name (eg, city)");
			sect.put("0.organizationName", "Organization Name (eg, company)");
			sect.put("0.organizationName_default", "Internet Widgits Pty Ltd");
			sect.put("organizationalUnitName", "Organizational Unit Name (eg, section)");
			sect.put("commonName", "Common Name (eg, YOUR name)");
			sect.put("commonName_max", "64");
			sect.put("emailAddress", "Email Address");
			sect.put("emailAddress_max", "64");
		sect = confFile.add("req_attributes");
			sect.put("challengePassword", "A challenge password");
			sect.put("challengePassword_min", "4");
			sect.put("challengePassword_max", "20");
			sect.put("unstructuredName", "An optional company name");
		confFile.store();
	}

	/* Gera uma requisicao de certificado.
	 * @param confFileName Nome do arquivo de configuracao do OpenSSL a ser usado.
	 * @param reqOutFileName Nome do arquivo de requisicao a ser gerado.
	 * @param keyOutFileName Nome do arquivo de chave privada a ser gerado.
	 * @param keySize Tamanho da chave de criptografia, em bits.
	 * @param algHash Algoritmo de hash a ser usado.
	 * @param password Senha para criptografar a chave privada.
	 * @param subject String contendo o subject (DN - Distinguished Name) do certificado.
	 * @throws Exception
	 */
	private void createCertReq(String confFileName, String reqOutFileName, String keyOutFileName,
			int keySize, String algHash, String password, String subject) throws Exception {
		
		if (trace) {
			logger.trace(String.format("Creating Certificate Request for '%s'", subject));
		}
		try {
			Process p = OpenSSLWrapper.createCertReq(confFileName, keyOutFileName,
					reqOutFileName, keySize, algHash, password, subject);
			if (SystemWrapper.processWait(p, PROCESS_WAIT_TIMEOUT) != 0) {
				throw new Exception(
						"Error in OpenSSL: \n"
						+ SystemWrapper.getProcessErrorStr(p));
			}
		} catch (Exception e) {
			logger.error(String.format("Error for '%s' request: %s", 
					subject, e.getLocalizedMessage()));
			throw e;
		}
		if (trace) {
			logger.trace("Certificate Request created");
		}
	}
	
	/* Assina uma requisicao de certificado.
	 * @param confFileName Nome do arquivo de configuracao do OpenSSL a ser usado.
	 * @param ca Objeto que representa a CA.
	 * @param reqInFileName Nome do arquivo de requisicao.
	 * @param certOutFileName Nome do arquivo de certificado a ser gerado.
	 * @param expireDays Duracao do certificado, em dias.
	 * @param algHash Algoritmo de hash a ser usado.
	 * @param password Senha da CA.
	 * @throws Exception
	 */
	private void createCertByReq(String confFileName, Ca ca, String reqInFileName,
			String certOutFileName, int expireDays, String algHash,
			String password) throws Exception {
		
		if (trace) {
			logger.trace("Signing Certificate Request");
		}
		try {
			Process p = OpenSSLWrapper.createCertFromReq(
					confFileName, reqInFileName, ca.getPrivKeyFile(), ca.getCertFile(),
					certOutFileName, expireDays, algHash, password);
			if (SystemWrapper.processWait(p, PROCESS_WAIT_TIMEOUT) != 0) {
				throw new Exception(
						"Error in OpenSSL: \n"
						+ SystemWrapper.getProcessErrorStr(p));
			}
		} catch (Exception e) {
			logger.error(String.format("Error in sign certificate: %s", 
					e.getLocalizedMessage()));
			throw e;
		}
		if (trace) {
			logger.trace("Certificate signed");
		}
	}
	
	/* Adiciona um atributo numa secao do arquivo de configuracao do OpenSSL, baseado no
	 *   parsing de um campo de um subject (DN).
	 * @param sect Secao do arquivo de configuracao.
	 * @param subject String contendo o subject (DN - Distinguished Name).
	 * @param field Nome do campo a ser retornado (ex: "C", "O", "OU", etc.).
	 */
	private void addConfAttrFromSubject(Ini.Section sect, String subject, String field) {
		StringBuilder attr = new StringBuilder();
		for (String s : OpenSSLWrapper.subjectParse(subject, field)) {
			if (attr.length() != 0) attr.append(';');
			attr.append(s);
		}
		if (attr.length() != 0) {
			sect.put(field, attr.toString());
		}
	}

}
