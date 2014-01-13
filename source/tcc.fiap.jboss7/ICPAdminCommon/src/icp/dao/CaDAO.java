package icp.dao;

import icp.bean.Ca;
import icp.util.OpenSSLWrapper;
import icp.util.SystemWrapper;

import java.io.File;

import org.ini4j.Ini;

/**
 * Gerencia uma Autoridade Certificadora Intermediaria.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class CaDAO extends RootCaDAO {

	/** OID default da Politica de Certificacao da CA para emitir certificados padrao A1. */
	public static final String CPS_CA_A1 = "2.16.76.1.2.1.0";

	/* oid da politica de certificacao da CA para Cert.A1 */
	protected String cpsCaA1;
	
	/**
	 * Cria uma instancia do gerenciador de CA.
	 * @param baseDir Diretorio base (raiz) das CA's.
	 */
	public CaDAO(String baseDir) {
		super(baseDir);
		this.cpsCaA1 = CPS_CA_A1;
	}

	/**
	 * Configura o OID da Politica de Certificacao da CA para emitir Certiticados padrao A1.
	 * @param value OID da Politica de Certificacao da CA para emitir Certiticados padrao A1.
	 */
	public void setCpsCaA1(String value) {
		this.cpsCaA1 = value;
	}

	/**
	 * Cria uma nova CA Intermediaria.
	 * @param name Nome da CA.
	 * @param rootCA Objeto que representa a CA Raiz.
	 * @param crlURI Caminho (URI) da Lista de Certificados Revogados (LCR).
	 * @param cpsURI Caminho (URI) do Documento com as Politicas de Certificacao (DPC).
	 * @param expireDays Duracao do certificado da CA, em dias.
	 * @param crlDays Atualizacao da Lista de Certificados Revogados (LCR), em dias.
	 * @param keySize Tamanho da chave de criptografia, em bits.
	 * @param algHash Algoritmo de hash a ser usado pelo certificado da CA.
	 * @param keyPassword Senha da CA, para criptografar a chave privada.
	 * @param rootPassword Senha da CA Raiz.
	 * @param subject String contendo o subject (DN - Distinguished Name) da CA.
	 * @return Objeto que representa a CA criada.
	 * @throws Exception
	 */
	public Ca createCA(String name, Ca rootCA, String crlURI,
			String cpsURI,int expireDays, int crlDays, int keySize,
			String algHash,	String keyPassword, String rootPassword,
			String subject) throws Exception {
		
		if (trace) {
			logger.trace(String.format("Creating CA: '%s'", name));
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
			this.createCaConfFile  (rootCA, ca, crlURI, cpsURI, subject);
			this.createUserConfFile(rootCA, ca, crlURI, cpsURI, subject);
			ca.refresh();
		} catch (Exception e) {
			logger.error(String.format("Error for '%s': %s", 
					name, e.getLocalizedMessage()));
			removeDirStructCA(ca);
			throw e;
		}
		/* cria requisicao para o certificado */
		if (trace) {
			logger.trace(String.format("Creating Certificate Request for '%s'", name));
		}
		Process p = null;
		String processErrorStr = null;
		try {
			p = OpenSSLWrapper.createCertReq(ca.getConfFileName(), ca.getPrivKeyFile(),
					ca.getReqFile(), keySize, algHash, keyPassword, subject);
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
		/* assina requisicao com a AC especificada */
		if (trace) {
			logger.trace(String.format("Signing Certificate Request for '%s'", name));
		}
		p = null;
		processErrorStr = null;
		try {
			p = OpenSSLWrapper.createCertFromReq(ca.getConfFileName(), ca.getReqFile(),
					rootCA.getPrivKeyFile(), rootCA.getCertFile(), ca.getCertFile(),
					expireDays, algHash, rootPassword);
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
			createCRL(ca, keyPassword, crlDays, algHash);
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
			logger.trace(String.format("CA '%s' created", name));
		}
		return ca;
	}

	/* Cria um arquivo de configuracao do OpenSSL para a CA.
	 * @param rootCa Objeto que representa a CA Raiz (issuer) desta CA.
	 * @param ca Objeto que representa a CA.
	 * @param crlURI Caminho (URI) da Lista de Certificados Revogados (LCR).
	 * @param cpsURI Caminho (URI) do Documento com as Politicas de Certificacao (DPC).
	 * @param subject String contendo o subject (DN - Distinguished Name) da CA.
	 * @throws Exception
	 */
	@Override
	protected void createCaConfFile(Ca rootCa, Ca ca, String crlURI,
			String cpsURI, String subject) throws Exception {
		
		super.createCaConfFile(rootCa, ca, crlURI, cpsURI, subject);
		File f = new File(ca.getConfFileName());
		Ini confFile = new Ini(f);
		Ini.Section sect;
		sect = confFile.get("v3_req");
			sect.put("certificatePolicies", "@polsection1, @polsection2");
		sect = confFile.get("v3_ca");
			sect.put("authorityKeyIdentifier", "keyid,issuer:always");
			sect.put("certificatePolicies", "@polsection1, @polsection2");
		sect = confFile.add("polsection1"); 
			sect.put("policyIdentifier", cpsCaA1);
			sect.put("CPS.1", "\"" + cpsURI + "\"");
		sect = confFile.add("polsection2"); 
			sect.put("policyIdentifier", cpsCaA3);
			sect.put("CPS.1", "\"" + cpsURI + "\"");
		confFile.store();
	}
}
