package icp.bean;

import java.io.File;
import java.io.Serializable;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.ini4j.Ini;

/**
 * Representa uma Autoridade Certificadora (Raiz ou Intermediaria).
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class Ca implements Serializable {

	/* nome do arquivo de configuracao OpenSSL da CA. */
	private final static String CONFIG_FILENAME      = "ca_ossl.cnf"       ;
	/* nome do arquivo de configuracao OpenSSL para usuario da CA. */
	private final static String USER_CONFIG_FILENAME = "user_ossl.cnf"     ;
	/* subdiretorio de certificados emitidos da CA. */
	private final static String CERTS_SUBDIR         = "certs"             ;
	/* subdiretorio de LCR da CA. */
	private final static String CRL_SUBDIR           = "crl"               ;
	/* subdiretorio de certificados emitidos para usuarios da CA. */
	private final static String NEW_CERTS_SUBDIR     = "newcerts"          ;
	/* subdiretorio de chave privada da CA. */
	private final static String PRIVATE_SUBDIR       = "private"           ;
	/* nome do arquivo de certificado da CA. */
	private final static String CA_CERT_FILENAME     = "cacert.pem.cer"    ;
	/* nome do arquivo da cadeia de certificados da CA. */
	private final static String CA_CHAIN_FILENAME    = "cachain.p7b"       ;
	/* nome do arquivo de LCR da CA. */
	private final static String CRL_FILENAME         = "lcr.pem.crl"       ;
	/* nome do arquivo de chave privada da CA. */
	private final static String CA_KEY_FILENAME      = "cakey.pem"         ;
	/* nome do arquivo de requisiacao da CA. */
	private final static String CA_REQ_FILENAME      = "careq.pem"         ;
	/* nome do arquivo de database da CA. */
	private final static String DATABASE_FILENAME    = "index.txt"         ; 
	/* nome do arquivo de serial da CA. */
	private final static String SERIAL_FILENAME      = "serial"            ; 
	/* nome do arquivo de serial de LCR da CA. */
	private final static String SERIAL_CRL_FILENAME  = "serial_crl"        ;
	/* nome do arquivo de requisicao de usuario para a CA. */
	private final static String USER_REQ_FILENAME    = "usrreq_%s.pem"     ;
	/* nome do arquivo de certificado de usuario. */
	private final static String USER_CERT_FILENAME   = "usrcert_%s.pem.cer";
	/* nome do arquivo de chave privada de usuario. */
	private final static String USER_KEY_FILENAME    = "usrkey_%s.pem"     ;

	private String name;
	private String commonName;
	private String country;
	private String organization;
	private String orgUnit;
	private String root;
	private String rootDir;
	private String dir;
	private String baseDir;
	private String confFileName;
	private String userConfFileName;
	private String certsDir;
	private String crlDir;
	private String newCertsDir;
	private String certFile;
	private String certChainFile;
	private String crlFile;
	private String privateDir;
	private String privKeyFile;
	private String reqFile;
	private String databaseFile;
	private String serialFile;
	private String crlSerialFile;
	
	/* gerador de numeros aleatorios para criacao dos
	 * nomes de arquivo de requisicao */
	private Random randGen = new Random();
	
	/**
	 * Cria uma instancia de CA (Autoridade Certificadora).
	 * @param name Nome base da CA.
	 * @param baseDir Caminho do diretorio base (raiz) onde a CA sera' estabelecida.
	 */
	public Ca(String name, String baseDir) {
		this.name             = name;
		this.baseDir          = baseDir;
		this.dir              = this.baseDir     + File.separatorChar + name                ;
		this.confFileName     = this.dir         + File.separatorChar + CONFIG_FILENAME     ;
		this.userConfFileName = this.dir         + File.separatorChar + USER_CONFIG_FILENAME;
		this.certsDir         = this.dir         + File.separatorChar + CERTS_SUBDIR        ; 
		this.crlDir           = this.dir         + File.separatorChar + CRL_SUBDIR          ; 
		this.newCertsDir      = this.dir         + File.separatorChar + NEW_CERTS_SUBDIR    ;
		this.privateDir       = this.dir         + File.separatorChar + PRIVATE_SUBDIR      ;
		this.certFile         = this.certsDir    + File.separatorChar + CA_CERT_FILENAME    ;
		this.certChainFile    = this.certsDir    + File.separatorChar + CA_CHAIN_FILENAME   ;
		this.crlFile          = this.crlDir      + File.separatorChar + CRL_FILENAME        ;
		this.privKeyFile      = this.privateDir  + File.separatorChar + CA_KEY_FILENAME     ; 
		this.reqFile          = this.newCertsDir + File.separatorChar + CA_REQ_FILENAME     ; 
		this.databaseFile     = this.dir         + File.separatorChar + DATABASE_FILENAME   ;
		this.serialFile       = this.dir         + File.separatorChar + SERIAL_FILENAME     ;
		this.crlSerialFile    = this.crlDir      + File.separatorChar + SERIAL_CRL_FILENAME ;
		/* carrega as propriedades dentro do arquivo de configuracao da CA */
		loadConfFile();
	}

	/**
	 * Recarrega as propriedades da CA.
	 */
	public void refresh() {
		/* carrega as propriedades dentro do arquivo de configuracao da CA */
		loadConfFile();
	}

	/**
	 * Retorna o nome da CA (Autoridade Certificadora).
	 * @return Nome da CA.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retorna o nome comum (Common Name - CN) da CA (Autoridade Certificadora).
	 * @return Common Name da CA.
	 */
	public String getCommonName() {
		if (commonName == null) { loadConfFile(); }
		return commonName;
	}

	/**
	 * Retorna o pais (Country - C) da CA (Autoridade Certificadora).
	 * @return Country da CA.
	 */
	public String getCountry() {
		if (country == null) { loadConfFile(); }
		return country;
	}

	/**
	 * Retorna a organizacao (Organization - O) da CA (Autoridade Certificadora).
	 * @return Organization da CA.
	 */
	public String getOrganization() {
		if (organization == null) { loadConfFile(); }
		return organization;
	}

	/**
	 * Retorna a unidade organizacional (Organisational Unit  - OU)
	 *   da CA (Autoridade Certificadora).
	 * @return Organisational Unit da CA.
	 */
	public String getOrgUnit() {
		if (orgUnit == null) { loadConfFile(); }
		return orgUnit;
	}

	/**
	 * Retorna o nome da CA Raiz (issuer) desta CA (Autoridade Certificadora).
	 * @return Nome da CA Raiz (emissora).
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * Retorna o diretorio base da CA Raiz (issuer) desta CA (Autoridade Certificadora).
	 * @return Diretorio base da CA Raiz (emissora).
	 */
	public String getRootDir() {
		return rootDir;
	}

	/**
	 * Retorna o diretorio onde esta' a CA (Autoridade Certificadora).
	 * @return Diretorio da CA.
	 */
	public String getDir() {
		return dir;
	}
	
	/**
	 * Retorna o diretorio base onde residem as CA's (Autoridades Certificadoras).
	 * @return Diretorio base das CA's.
	 */
	public String getBaseDir() {
		return baseDir;
	}

	/**
	 * Retorna o nome do arquivo de configuracao (OpenSSL) da CA (Autoridade Certificadora).
	 * @return Nome e caminho do arquivo de configuracao da CA.
	 */
	public String getConfFileName() {
		return confFileName;
	}

	/**
	 * Retorna o nome do arquivo de configuracao (OpenSSL) de usuario da CA
	 *   (Autoridade Certificadora).
	 * @return Nome e caminho do arquivo de configuracao de usuario da CA.
	 */
	public String getUserConfFileName() {
		return userConfFileName;
	}

	/**
	 * Retorna o nome do subdiretorio de certificados gerados pela CA
	 *   (Autoridade Certificadora).
	 * @return Caminho do subdiretorio de certificados da CA.
	 */
	public String getCertsDir() {
		return certsDir;
	}

	/**
	 * Retorna o nome do subdiretorio de LCR gerados pela CA
	 *   (Autoridade Certificadora).
	 * @return Caminho do subdiretorio de LCR (Lista de Certificados Revogados) da CA.
	 */
	public String getCrlDir() {
		return crlDir;
	}

	/**
	 * Retorna o nome do subdiretorio de certificados de usuario gerados pela CA
	 *   (Autoridade Certificadora).
	 * @return Caminho do subdiretorio de certificados de usuario da CA.
	 */
	public String getNewCertsDir() {
		return newCertsDir;
	}

	/**
	 * Retorna o nome do arquivo de certificado da CA (Autoridade Certificadora).
	 * @return Nome e caminho do arquivo de certificado da CA.
	 */
	public String getCertFile() {
		return certFile;
	}

	/** 
	 * Retorna o nome do arquivo da cadeia de certificados da CA (Autoridade Certificadora).
	 * @return Nome e caminho do arquivo da cadeia de certificados da CA.
	 */
	public String getCertChainFile() {
		return certChainFile;
	}

	/**
	 * Retorna o nome do arquivo de LCR da CA (Autoridade Certificadora).
	 * @return Nome e caminho do arquivo de LCR (Lista de Certificados Revogados) da CA.
	 */
	public String getCrlFile() {
		return crlFile;
	}

	/**
	 * Retorna o nome do arquivo de chave privada da CA (Autoridade Certificadora).
	 * @return Nome e caminho do arquivo de chave privada da CA.
	 */
	public String getPrivKeyFile() {
		return privKeyFile;
	}

	/**
	 * Retorna o nome do arquivo de requisicao da CA (Autoridade Certificadora).
	 * @return Nome e caminho do arquivo de requisicao da CA.
	 */
	public String getReqFile() {
		return reqFile;
	}

	/**
	 * Retorna o nome do arquivo de database (index) da CA (Autoridade Certificadora).
	 * @return Nome e caminho do arquivo de database da CA.
	 */
	public String getDatabaseFile() {
		return databaseFile;
	}

	/**
	 * Retorna o nome do arquivo de serial da CA (Autoridade Certificadora).
	 * @return Nome e caminho do arquivo de serial da CA.
	 */
	public String getSerialFile() {
		return serialFile;
	}

	/**
	 * Retorna o nome do arquivo de serial de LCR da CA (Autoridade Certificadora).
	 * @return Nome e caminho do arquivo de serial de LCR
	 *   (Lista de Certificados Revogados) da CA.
	 */
	public String getCrlSerialFile() {
		return crlSerialFile;
	}

	/**
	 * Retorna o nome do subdiretorio da chave privada da CA (Autoridade Certificadora).
	 * @return Caminho do subdiretorio da chave privada da CA.
	 */
	public String getPrivateDir() {
		return privateDir;
	}

	/**
	 * Retorna o nome (aleatorio) de um arquivo de requisicao para o
	 *   usuario da CA (Autoridade Certificadora).
	 * @return Nome e caminho do arquivo de requisicao para o usuario da CA.
	 */
	public String getUsrReqFile() {
		return newCertsDir + File.separatorChar
			+ String.format(USER_REQ_FILENAME, getRamdomBase64Str(16)); 
	}

	/**
	 * Retorna o nome (aleatorio) de um arquivo de certificado para o
	 *   usuario da CA.
	 * @return Nome e caminho do arquivo de certificado para o usuario da CA.
	 */
	public String getUsrCertFile() {
		return certsDir + File.separatorChar
		+ String.format(USER_CERT_FILENAME, getRamdomBase64Str(16)); 
	}
	
	/**
	 * Retorna o nome (aleatorio) de um arquivo de chave privada para o
	 *   usuario da CA.
	 * @return Nome e caminho do arquivo de chave privada para o usuario da CA.
	 */
	public String getUsrKeyFile() {
		return privateDir + File.separatorChar
		+ String.format(USER_KEY_FILENAME, getRamdomBase64Str(16)); 
	}

	/* Carrega as propriedades dentro do arquivo de configuracao da CA. */
	private void loadConfFile() {
		File f = new File(confFileName);
		Ini confFile;
		try {
			confFile = new Ini(f);
			this.commonName   = confFile.get("icpAdmin", "CN"  );
			this.country      = confFile.get("icpAdmin", "C"   );
			this.organization = confFile.get("icpAdmin", "O"   );
			this.orgUnit      = confFile.get("icpAdmin", "OU"  );
			this.root         = confFile.get("icpAdmin", "root");
			this.rootDir      = confFile.get("icpAdmin", "rootDir");
		} catch (Exception e) {
			this.commonName   = null;
			this.country      = null;
			this.organization = null;
			this.orgUnit      = null;
			this.root         = null;
			this.rootDir      = null;
		}
	}

	/* Gera uma string aleatoria codificada em Base64.
	 * @param len Tamanho da string a ser gerada, deve ser divisivel por 4.
	 * @return String aleatoria codificada em Base64. */
	private String getRamdomBase64Str(int len) {
		byte bytes[] = new byte[(len > 1) ? (len * 3 / 4) : 1];
		randGen.nextBytes(bytes);
		String encoded =
			Base64.encodeBase64String(bytes)
				.replace('/','_')
				.replace('+','-');
		return encoded;
	}
}
