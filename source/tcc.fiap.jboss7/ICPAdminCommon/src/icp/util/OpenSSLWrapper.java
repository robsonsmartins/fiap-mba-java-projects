package icp.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Encapsula funcionalidades proporcionadas pelo OpenSSL.
 * @author Robson Martins (robson@robsonmartins.com)
 * @see <a href="http://www.openssl.org/">OpenSSL Oficial Site</a>
 */
public class OpenSSLWrapper {

	/* Comando executavel do OpenSSL */ 
	private static final String[] OPENSSL_COMMAND_UNIX = { "openssl" };
	private static final String[] OPENSSL_COMMAND_WIN  = { "cmd", "/c", "openssl" };

	/* objeto para realizar log das operacoes */
	private static Logger logger;
	private static boolean trace;

	/* Inicializa atributos estaticos. */
	static {
		logger = LogManager.getLogger(OpenSSLWrapper.class);
		trace = logger.isTraceEnabled();
	}
	
	/**
	 * Anexa um campo (field) ao subject (DN - Distinguished Name).
	 * @param subj Subject a ter o campo anexado. Se null, sera' inicializado um subject vazio.
	 * @param field Nome do campo a ser anexado ao subject (ex: "C", "O", "OU", etc.).
	 * @param value Valor do campo a ser anexado ao subject.
	 *   Pode ser uma string contendo valores separados por ponto-e-virgula, para anexar
	 *   multiplos valores a um mesmo campo.
	 * @return Objeto StringBuilder que representa o subject, com o campo anexado.
	 * @see <a href="http://www.x500standard.com/">X500 Standard</a>
	 */
	public static StringBuilder subjectAppend(StringBuilder subj, String field, String value) {
		if (subj == null) subj = new StringBuilder();
		if (field != null && value != null) {
			String[] values = value.split(";");
			for (String v : values) {
				subj.append(String.format("/%s=%s", field, v));
			}
		}
		return subj;
	}
	
	/**
	 * Retorna os valores de um campo (field) de um subject (DN - Distinguished Name).
	 * @param subject String representando um subject.
	 * @param field Nome do campo a ser retornado (ex: "C", "O", "OU", etc.).
	 * @return Valores atribuidos ao campo, ou vazio se nenhum.
	 * @see <a href="http://www.x500standard.com/">X500 Standard</a>
	 */
	public static List<String> subjectParse(String subject, String field) {
		List<String> subjList = new ArrayList<String>();
		if (subject != null && !"".equals(subject)) {
			String pattern = String.format("/%s=", field);
			int beginIndex = 0;
			int endIndex = 0;
			do {
				beginIndex = subject.indexOf(pattern, beginIndex);
				if (beginIndex >= 0) {
					beginIndex += pattern.length();
					endIndex = subject.indexOf("/", beginIndex);
					if (endIndex > - 0)
						subjList.add(subject.substring(beginIndex, endIndex));
					else
						subjList.add(subject.substring(beginIndex));
				}
			} while (beginIndex >= 0);
		}
		return subjList;
	}
	
	/**
	 * Cria um certificado auto-assinado.
	 * @param configFile Nome do arquivo de configuracao a ser usado.
	 * @param keyOutFile Nome do arquivo de chave (key) a ser gerado.
	 * @param certOutFile Nome do arquivo de certificado (PEM) a ser gerado.
	 * @param expireDays Quantidade de dias para expirar o certificado.
	 * @param keySize Tamanho da chave (RSA), em bits (ex: 1024, 2048, 4096).
	 * @param algHash Algoritmo de Hash (ex: "sha1", "sha256").
	 * @param password Senha para o certificado.
	 * @param subject Subject (DN) para o certificado.
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process createAutoSignCert(String configFile, String keyOutFile,
			String certOutFile, int expireDays, int keySize, String algHash,
			String password, String subject) throws IOException {
	
		String[] args = 
			{ "req", "-new", "-x509", "-config",  configFile ,
			  "-keyout",  keyOutFile , "-out",  certOutFile ,
			  "-days", String.valueOf(expireDays),
			  "-newkey", "rsa:" + String.valueOf(keySize), "-" + algHash,
			  "-passin", "pass:" + password ,
			  "-passout", "pass:" + password ,
			  "-subj",  subject  };
		
		return executeOpenSSL(args);
	}

	/**
	 * Cria uma requisicao (PKCS#10).
	 * @param configFile Nome do arquivo de configuracao a ser usado.
	 * @param keyOutFile Nome do arquivo de chave (key) a ser gerado.
	 * @param reqOutFile Nome do arquivo de requisicao (PEM) a ser gerado.
	 * @param keySize Tamanho da chave (RSA), em bits (ex: 1024, 2048, 4096).
	 * @param algHash Algoritmo de Hash (ex: "sha1", "sha256").
	 * @param password Senha para a requisicao de certificado.
	 * @param subject Subject (DN) para o certificado.
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process createCertReq(String configFile, String keyOutFile,
			String reqOutFile, int keySize, String algHash,
			String password, String subject) throws IOException {
		
		String[] args =
			{ "req", "-outform", "PEM", "-out",  reqOutFile , "-verify", "-new",
			  "-newkey", "rsa:" + String.valueOf(keySize), "-keyform", "PEM",
			  "-keyout",  keyOutFile , "-" + algHash,
			  "-passin", "pass:" + password ,
			  "-passout", "pass:" + password ,
			  "-config",  configFile ,
			  "-subj",  subject  };

		return executeOpenSSL(args);
	}

	/**
	 * Importa uma requisicao de certificado (PKCS#10).
	 * @param configFile Nome do arquivo de configuracao a ser usado.
	 * @param reqInFile Nome do arquivo de requisicao a ser importado.
	 * @param reqFormat Formato do arquivo de requisicao a ser importado ("PEM" ou "DER").
	 * @param reqOutFile Nome do arquivo de requisicao (PEM) a ser gerado.
	 * @param password Senha da requisicao de certificado.
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process importCertReq(String configFile, String reqInFile,
			String reqFormat, String reqOutFile, String password) throws IOException {
		
		String[] args =
			{ "req", "-inform", reqFormat, "-outform", "PEM",
			  "-in",  reqInFile ,
			  "-passin", "pass:" + password ,
			  "-passout", "pass:" + password ,
			  "-out",  reqOutFile , "-verify",
			  "-config",  configFile  };

		return executeOpenSSL(args);
	}
	
	/**
	 * Cria (assina) um certificado (PEM) a partir de uma requisicao (PKCS#10).
	 * @param configFile Nome do arquivo de configuracao a ser usado.
	 * @param reqInFile Nome do arquivo de requisicao.
	 * @param caKeyFile Nome do arquivo de chave (key) da CA.
	 * @param caCertFile Nome do arquivo de certificado da CA
	 *   (de quem emite [issuer] este certificado).
	 * @param certOutFile Nome do arquivo de certificado (PEM) a ser gerado.
	 * @param expireDays Quantidade de dias para expirar o certificado.
	 * @param algHash Algoritmo de Hash (ex: "sha1", "sha256").
	 * @param caPassword Senha do certificado da CA (issuer).
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process createCertFromReq(String configFile, String reqInFile, String caKeyFile,
			String caCertFile, String certOutFile, int expireDays, String algHash,
			String caPassword) throws IOException {
		
		String[] args =
			{ "ca", "-config",  configFile , "-md", algHash,
			  "-out",  certOutFile ,
			  "-key",  caPassword , "-in",  reqInFile ,
			  "-days", String.valueOf(expireDays),
			  "-keyfile",  caKeyFile , "-cert",  caCertFile , "-batch" };

		return executeOpenSSL(args);
	}

	/**
	 * Revoga um certificado (PEM) ja' emitido.
	 * @param configFile Nome do arquivo de configuracao a ser usado.
	 * @param certInFile Nome do arquivo de certificado a ser revogado.
	 * @param caCertFile Nome do arquivo de certificado da CA
	 *   (de quem emitiu [issuer] este certificado).
	 * @param caKeyFile Nome do arquivo de chave (key) da CA.
	 * @param caPassword Senha do certificado da CA (issuer).
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process revokeCert(String configFile, String certInFile,
			String caCertFile, String caKeyFile, String caPassword) throws IOException {
		
		String[] args =
			{ "ca", "-revoke",  certInFile , "-config",  configFile ,
			  "-cert",  caCertFile , "-keyfile",  caKeyFile ,
			  "-key",  caPassword  };

		return executeOpenSSL(args);
	}
	
	/**
	 * Converte um certificado PEM para DER.
	 * @param pemCertFile Nome do arquivo do certificado (PEM) de origem.
	 * @param derCertFile Nome do arquivo do certificado (DER) de destino.
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process convertCertPem2Der(String pemCertFile,
			String derCertFile) throws IOException {
		
		String[] args =
			{ "x509", "-inform", "PEM", "-outform", "DER", "-in",  pemCertFile ,
			  "-out",  derCertFile };

		return executeOpenSSL(args);
	}
	
	/**
	 * Exporta um certificado para o formato PKCS#12 (.pfx). 
	 * @param certInFile Nome do arquivo de certificado a ser convertido.
	 * @param keyInFile Nome do arquivo de chave (key).
	 * @param pfxCertFile Nome do arquivo de certificado (PKCS#12) de destino.
	 * @param password Senha do certificado.
	 * @param alias Nome amigavel do par de chaves dentro do arquivo PKCS#12.
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process exportCertPkcs12(String certInFile, String keyInFile,
			String pfxCertFile, String password, String alias) throws IOException {
		
		String[] args =
			{ "pkcs12", "-export", "-inkey",  keyInFile,
			  "-passin", "pass:" + password,
			  "-passout", "pass:" + password,
			  "-in",  certInFile, "-out",  pfxCertFile,
			  "-name", alias };

		return executeOpenSSL(args);
	}
	
	/**
	 * Exporta uma chave privada para um arquivo descriptografado (sem senha).
	 * @param keyInFile Nome do arquivo de chave privada (key) criptografada.
	 * @param keyOutFile Nome do arquivo de chave (key) a ser gerado.
	 * @param password Senha da chave privada criptografada.
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process exportKeyNoPassword(String keyInFile, String keyOutFile,
			String password) throws IOException {
		
		String[] args =
			{ "rsa", "-in", keyInFile, "-out", keyOutFile,
			  "-passin", "pass:" + password };

		return executeOpenSSL(args);
	}

	/**
	 * Exporta uma cadeia de certificados como arquivo P7B.
	 * @param certInFiles Nome dos arquivos de certificado (PEM) que
	 *   formam a cadeia.
	 * @param p7bCertFile Nome do arquivo de cadeia (P7B) a ser gerado.
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process exportCertChain(String[] certInFiles,
			String p7bCertFile) throws IOException {
		
		String[] args = new String[4 + 2 * certInFiles.length];
		int argsIdx = 0;
		args[argsIdx++] = "crl2pkcs7";
		args[argsIdx++] = "-nocrl";
		
		for (int i = 0; i < certInFiles.length; i++) {
			args[argsIdx++] = "-certfile";
			args[argsIdx++] = certInFiles[i];
		}

		args[argsIdx++] = "-out";
		args[argsIdx++] = p7bCertFile;

		return executeOpenSSL(args);
	}
	
	/**
	 * Gera uma lista de certificados revogados (CRL).
	 * @param configFile Nome do arquivo de configuracao a ser usado.
	 * @param caCertFile Nome do arquivo de certificado.
	 * @param crlOutFile Nome do arquivo CRL a ser gerado.
	 * @param crlDays Quantidade de dias para expirar a lista.
	 * @param algHash Algoritmo de Hash (ex: "sha1", "sha256").
	 * @param password Senha do certificado.
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process generateCRL(String configFile, String caCertFile, String crlOutFile, 
			int crlDays, String algHash, String password) throws IOException {
		
		String[] args =
			{ "ca", "-config",  configFile , "-gencrl",
			  "-crldays", String.valueOf(crlDays), "-md", algHash,
			  "-out",  crlOutFile ,
			  "-cert",  caCertFile , "-key",  password  };

		return executeOpenSSL(args);
	}
	
	/**
	 * Retorna informacoes sobre uma lista de certificados revogados (CRL):
	 *   campo lastUpdate. 
	 * @param crlInFile Nome do arquivo CRL.
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process getCRLInfoLastUpdate(String crlInFile) throws IOException {
		
		String[] args =
			{ "crl", "-inform", "PEM", "-noout",
			  "-lastupdate", "-in",  crlInFile  };  

		return executeOpenSSL(args);
	}
	
	/**
	 * Retorna informacoes sobre uma lista de certificados revogados (CRL):
	 *   campo nextUpdate. 
	 * @param crlInFile Nome do arquivo CRL.
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	public static Process getCRLInfoNextUpdate(String crlInFile) throws IOException {
		
		String[] args =
			{ "crl", "-inform", "PEM", "-noout",
			  "-nextupdate", "-in",  crlInFile  };  

		return executeOpenSSL(args);
	}
	

	/* Executa o OpenSSL e retorna o processo de sistema associado.
	 * @param args Argumentos de linha de comando para o OpenSSL.
	 * @return Objeto Process, representando o processo do OpenSSL chamado. 
	 * @throws IOException
	 */
	private static Process executeOpenSSL(String[] args) throws IOException { 
		String[] openssl;
		String os = System.getProperty("os.name");

		if (os != null && os.toLowerCase().startsWith("windows")) {
			openssl = OPENSSL_COMMAND_WIN;
		} else {
			openssl = OPENSSL_COMMAND_UNIX;
		}
		String[] command = new String[openssl.length + args.length]; 

		System.arraycopy(openssl, 0, command, 0, openssl.length);
		System.arraycopy(args,  0, command, openssl.length, args.length);
		
		StringBuilder commandLine = new StringBuilder();
		for (String s : command) {
			if (commandLine.length() > 0) { commandLine.append(' '); }
			commandLine.append(s);
		}
		if (trace) {
			logger.trace(String.format("OpenSSL command-line: %s", commandLine.toString()));
		}
		
		return SystemWrapper.executeCommand(command);
	}
	
}
