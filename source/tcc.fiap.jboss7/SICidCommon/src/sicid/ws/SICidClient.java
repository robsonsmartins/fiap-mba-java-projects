package sicid.ws;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import sun.misc.BASE64Encoder;

import com.robsonmartins.fiap.tcc.util.CertificadoAssinador;
import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;
import com.robsonmartins.fiap.tcc.util.JBossUtil;

/**
 * Implementa um cliente para o Servico de Identificacao do Cidadao (SICid).
 * 
 * <p>
 *   Descricao do arquivo properties, contendo opcoes de conexao:
 * <ul>
 * <li><em>sicid.url</em>: URL do Servico de Identificacao do Cidadao (SICid).
 * <li><em>sicid.namespace</em>: Namespace do Servico de Identificacao do Cidadao (SICid).
 * <li><em>sicid.service</em>: Nome do Servico de Identificacao do Cidadao (SICid).
 * <li><em>sicid.keyStore</em>: Nome do arquivo de keystore contendo o par de chaves do
 *   cliente para autenticar no Servico de Identificacao do Cidadao (SICid).
 * <li><em>sicid.storeType</em>: Tipo do arquivo de keystore (default: "JKS").
 * <li><em>sicid.storePass</em>: Senha do arquivo de keystore.
 * <li><em>sicid.keyAlias</em>: Alias do par de chaves a ser usado, dentro do arquivo
 *   de keystore.
 * <li><em>sicid.keyPass</em>: Senha do par de chaves dentro do arquivo de keystore.
 * </ul>
 * <p>
 * 
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class SICidClient {

	/* nomes das opcoes do cliente no arquivo properties */
	private final static String OPTION_SICID_URL           = "sicid.url"        ;
	private final static String OPTION_SICID_NAMESPACE     = "sicid.namespace"  ;
	private final static String OPTION_SICID_SERVICE       = "sicid.service"    ;
	private final static String OPTION_SICID_KEYSTORE      = "sicid.keyStore"   ;
	private final static String OPTION_SICID_KEYSTORE_TYPE = "sicid.storeType"  ;
	private final static String OPTION_SICID_KEYSTORE_PASS = "sicid.storePass"  ;
	private final static String OPTION_SICID_KEY_ALIAS     = "sicid.keyAlias"   ;
	private final static String OPTION_SICID_KEY_PASS      = "sicid.keyPass"    ;
	
	/* proxy para servico SICid */
	private ISICidService sicidService;  

	private static Logger logger; 
	private static boolean trace;

	/**
	 * Cria uma nova instancia do cliente SICid.
	 */ 
	public SICidClient() {
		logger = LogManager.getLogger(SICidClient.class);
		trace = logger.isTraceEnabled();
	}
	
	/**
	 * Cria uma nova instancia do cliente SICid.
	 * @param serviceURL URL do servico.
	 * @param namespace Namespace do servico.
	 * @param service Nome do servico.
	 * @param keyStoreFile Nome do arquivo de keystore
	 *   a ser usado para obter o par de chaves do cliente.
	 * @param keyStoreType Tipo do arquivo de keystore (default: JKS).
	 * @param keyAlias Alias do par de chaves dentro do arquivo de keystore.
	 * @param keyStorePass Senha do arquivo de keystore.
	 * @param keyPass Senha do par de chaves dentro do arquivo de keystore.
	 * @throws Exception
	 */
	public SICidClient(String serviceURL, String namespace, String service,
			String keyStoreFile, String keyStoreType, String keyAlias,
			String keyStorePass, String keyPass) throws Exception {
		
		this();
		connect(serviceURL, namespace, service, keyStoreFile, keyStoreType,
				keyAlias, keyStorePass, keyPass);
	}

	/**
	 * Cria uma nova instancia do cliente SICid.
	 * @param props Properties com as opcoes de conexao do cliente.
	 * @throws Exception
	 */
	public SICidClient(Properties props) throws Exception {
		this();
		connect(props);
	}

	/**
	 * Cria uma nova instancia do cliente SICid.
	 * @param propsFileName Nome do arquivo properties
	 *   com as opcoes de conexao do cliente.
	 * @throws Exception
	 */
	public SICidClient(String propsFileName) throws Exception {
		this();
		connect(propsFileName);
	}
	
	/**
	 * Conecta no Servico de Identificacao do Cidadao (SICid). 
	 * @param serviceURL URL do servico.
	 * @param namespace Namespace do servico.
	 * @param service Nome do servico.
	 * @param keyStoreFile Nome do arquivo de keystore
	 *   a ser usado para obter o par de chaves do cliente.
	 * @param keyStoreType Tipo do arquivo de keystore (default: JKS).
	 * @param keyAlias Alias do par de chaves dentro do arquivo de keystore.
	 * @param keyStorePass Senha do arquivo de keystore.
	 * @param keyPass Senha do par de chaves dentro do arquivo de keystore.
	 * @throws Exception
	 */
	public void connect(String serviceURL, String namespace, String service,
			String keyStoreFile, String keyStoreType, String keyAlias,
			String keyStorePass, String keyPass) throws Exception {
		
		X509Certificate cert = null;
		KeyPair keys = null;
		String keyStoreFileFullPath = null;
		
		if (trace) {
			logger.trace(String.format(
					"Searching for keystore file %s", keyStoreFile));
		}
		
		try {
			keyStoreFileFullPath = getKeyStoreFileFullPath(keyStoreFile);
			if (keyStoreFileFullPath == null) {
				logger.error(String.format(
						"Keystore file %s not found", keyStoreFile));
				throw new FileNotFoundException(
						String.format("File %s not found.", keyStoreFile));
			}
			if (trace) {
				logger.trace(String.format(
						"Keystore file found in %s", keyStoreFileFullPath));
			}
			
			keys = CertificadoAssinador.getKeyPairFromFile(
					keyStoreFileFullPath, keyStoreType,
					keyAlias, keyStorePass, keyPass);
			
			cert = CertificadoAssinador.getCertFromFile(
					keyStoreFileFullPath, keyStoreType,
					keyAlias, keyStorePass);
			
			if (cert == null || keys == null) {
				throw new Exception(String.format(
						"Error reading keystore file %s", keyStoreFileFullPath));
			}
		} catch (Exception e) {

			logger.error(String.format(
					"Error reading keystore file %s", keyStoreFile), e);
			Exception exception = new Exception(String.format(
					"Error reading keystore file %s", keyStoreFile));
			exception.initCause(e);
			throw exception;
		}
		
		if (trace) {
			logger.trace(String.format(
					"Connecting with SICid" +
					" (url=%s namespace=%s service=%s cert:DN=%s)",
					serviceURL, namespace, service,
					cert.getSubjectX500Principal().getName()));
		}

		try {
		
			URL wsdlUrl = new URL(serviceURL + "?wsdl");
			QName qnameService  = new QName(namespace, service);
		
			/* configura autenticador HTTP/BASIC para ler o WSDL */
			SICidBasicAuthenticator authenticator =
				new SICidBasicAuthenticator(cert, keys);
		
			Authenticator.setDefault(authenticator);
		
			Service wsService = Service.create(wsdlUrl, qnameService);
			sicidService = wsService.getPort(ISICidService.class);

			/* configura parametros de autenticacao para o proxy do servico */
			Map<String, Object> reqContext =
				((BindingProvider) sicidService).getRequestContext();
			reqContext.put(BindingProvider.USERNAME_PROPERTY,
					authenticator.getUsername());
			reqContext.put(BindingProvider.PASSWORD_PROPERTY,
					authenticator.getPassword());

			if (trace) {
				logger.trace("Connected with SICid service");
			}
			
		} catch (Exception e) {

			logger.error("Error connecting with SICid service", e);
			Exception exception =
				new Exception("Error connecting with SICid service");
			exception.initCause(e);
			throw exception;
		}
	}

	/**
	 * Conecta no Servico de Identificacao do Cidadao (SICid). 
	 * @param props Properties com as opcoes de conexao do cliente.
	 * @throws Exception
	 */
	public void connect(Properties props) throws Exception {
		String serviceURL   = props.getProperty(OPTION_SICID_URL          ); 
		String namespace    = props.getProperty(OPTION_SICID_NAMESPACE    ); 
		String service      = props.getProperty(OPTION_SICID_SERVICE      ); 
		String keyStoreFile = props.getProperty(OPTION_SICID_KEYSTORE     ); 
		String keyStoreType = props.getProperty(OPTION_SICID_KEYSTORE_TYPE); 
		String keyStorePass = props.getProperty(OPTION_SICID_KEYSTORE_PASS); 
		String keyAlias     = props.getProperty(OPTION_SICID_KEY_ALIAS    ); 
		String keyPass      = props.getProperty(OPTION_SICID_KEY_PASS     );
		
		connect(serviceURL, namespace, service, keyStoreFile,
				keyStoreType, keyAlias,	keyStorePass, keyPass);
	}

	/**
	 * Conecta no Servico de Identificacao do Cidadao (SICid). 
	 * @param propsFileName Nome do arquivo properties
	 *   com as opcoes de conexao do cliente.
	 * @throws Exception
	 */
	public void connect(String propsFileName) throws Exception {
		Properties props = getOptionsFromFile(propsFileName);
		connect(props);
	}
	
	/**
	 * Retorna as opcoes do cliente SICid definidas em arquivo properties. 
	 * @param propsFileName Nome do arquivo properties
	 *   com as opcoes de conexao do cliente.
	 * @return Opcoes presentes no arquivo.
	 * @throws Exception
	 */
	public Properties getOptionsFromFile(String propsFileName) throws Exception {
		
		Properties props = new Properties();
		InputStream inputStream = null;
		String filename = null;
		/* tenta obter path absoluto do arquivo no JBoss */
		filename = JBossUtil.getJBossAbsFilePath(propsFileName);
		/* tenta obter arquivo no path especificado via classloader */
		try {
			inputStream =
				this.getClass().getClassLoader().getResourceAsStream(filename);
		} catch (Exception e) { }
		if (inputStream == null) {
			/* se nao achou, tenta obter arquivo diretamente no path */
			try {
				inputStream = new FileInputStream(filename);
			} catch (Exception e) {	}
		}
		if (inputStream == null) {
			throw new FileNotFoundException(
					String.format("File %s not found.", propsFileName));
		}		
		props.load(inputStream);
		return props;
	}
	
	/**
	 * Retorna o proxy para o servico remoto SICid.
	 * @return Proxy para o servico SICid.
	 */
	public ISICidService getService() {
		return sicidService;
	}
	
	/* Retorna o caminho completo do arquivo de KeyStore.
	 * @param keyStoreFile Nome do arquivo de KeyStore.
	 * @return Caminho completo do arquivo, ou null se nao encontrado.
	 */
	private String getKeyStoreFileFullPath(String keyStoreFile) {
		String ksFileRealPath = null;
		try {
			/* tenta obter path absoluto do arquivo no JBoss */
			ksFileRealPath = JBossUtil.getJBossAbsFilePath(keyStoreFile);
		} catch (Exception e) { }
		return ksFileRealPath;
	}

	/* Implementa um autenticador HTTP/BASIC para o cliente do
	 *   Servico de Identificacao do Cidadao (SICid).
	 */
	private class SICidBasicAuthenticator extends Authenticator {
		
		private String username;
		private String password;
		
		/* Cria uma nova instancia do autenticador.
		 * @param cert Certificado do cliente.
		 * @param keys Par de chaves do cliente.
		 * @throws Exception 
		 */
		public SICidBasicAuthenticator(X509Certificate cert,
				KeyPair keys) throws Exception {
			
			/* username recebe o conteudo do certificado,
			 * em formato Base64 */
			this.username = CertificadoSerializador.certToStr(cert);

			
			/* password recebe a assinatura do conteudo do certificado,
			 * em formato Base64 */
			this.password = new BASE64Encoder().encode(
					CertificadoAssinador.sign(keys.getPrivate(), cert.getEncoded()))
						.replaceAll("\\s+","");
			
			if (trace) {
				logger.trace(
					String.format("SICid Basic Authenticator: uname=%s", this.username));
				logger.trace(
					String.format("SICid Basic Authenticator: pass=%s", this.password));
			}
			
		}

		/* Retorna o valor do campo Username.
		 * @return Valor de Username.
		 */
		public String getUsername() {
			return username;
		}

		/* Retorna o valor do campo Password.
		 * @return Valor de Password.
		 */
		public String getPassword() {
			return password;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password.toCharArray());
		}
	}
}


