package com.robsonmartins.fiap.tcc.sicid.jaas;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.spi.UsersRolesLoginModule;

import com.robsonmartins.fiap.tcc.util.CertificadoAssinador;
import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;
import sicid.ws.SICidClient;
import sicid.bean.CertificadoStatus;
import sun.misc.BASE64Decoder;

/**
 * LoginModule para uso com o JAAS, baseado em arquivos properties.<br/>
 * Implementa a validacao de certificados digitais atraves de consulta ao
 *   Servico de Identificacao do Cidadao (SICid).
 * 
 * <p>
 *   Descricao dos arquivos properties:
 * <ul>
 * 	<li><b>users.properties</b>: Mapeia os Distinguished Name (DN) dos certificados
 *   com os usernames da aplicacao.</li>
 *  <li><b>roles.properties</b>: Mapeia os usernames com as roles.</li>
 * </ul>
 * <p>
 *   Opcoes do LoginModule:
 * <ul>
 * <li><em>usersProperties</em>: O nome do arquivo properties contendo o mapeamento de
 *   Distinguished Name (DN) dos certificados digitais para nomes de usuario (username).
 *   O default e' "users.properties".
 * <li><em>rolesProperties</em>: O nome do arquivo properties contendo o mapeamento de
 *   usuarios (username) para Roles. O default e' "roles.properties".
 * <li><em>sicid.disable</em>: Desabilita a consulta ao Servico de Identificacao
 *   do Cidadao (SICid), considerando qualquer certificado como valido.
 * <li><em>sicid.clientProps</em>: Nome do arquivo que contem a configuracao do cliente do
 *   Servico de Identificacao do Cidadao (SICid). Se especificado, todas as opcoes seguintes
 *   sao ignoradas, pois sao obtidas a partir desse arquivo.
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
public class SICidPropsLoginModule extends UsersRolesLoginModule {
	
	/* nomes das opcoes do loginModule no arquivo login-config.xml */
	private final static String OPTION_SICID_CLIENT_PROPS  = "sicid.clientProps";
	private final static String OPTION_SICID_DISABLE       = "sicid.disable"    ;

	private final static String OPTION_SICID_URL           = "sicid.url"        ;
	private final static String OPTION_SICID_NAMESPACE     = "sicid.namespace"  ;
	private final static String OPTION_SICID_SERVICE       = "sicid.service"    ;
	private final static String OPTION_SICID_KEYSTORE      = "sicid.keyStore"   ;
	private final static String OPTION_SICID_KEYSTORE_TYPE = "sicid.storeType"  ;
	private final static String OPTION_SICID_KEYSTORE_PASS = "sicid.storePass"  ;
	private final static String OPTION_SICID_KEY_ALIAS     = "sicid.keyAlias"   ;
	private final static String OPTION_SICID_KEY_PASS      = "sicid.keyPass"    ;
	
	private Subject subject;
	private CallbackHandler callbackHandler;
	private static Logger log;
	private static boolean trace;

	private Properties users;
	private Properties roles;
	private String dname;
	private String user;
	private String role;

	private String sicidDisable;
	private String sicidClientProps;
	private String sicidServiceURL;
	private String sicidNamespace;
	private String sicidService;
	private String sicidKeyStoreFile;
	private String sicidKeyStoreType;
	private String sicidKeyStorePass;
	private String sicidKeyAlias;
	private String sicidKeyPass;

	/* cliente do servico SICid */
	private SICidClient sicid = null;
	/* indica se a consulta ao servico SICid esta' habilitada */
	private boolean sicidEnabled = true;

    /**
	 * Initialize this LoginModule.
     * @param subject the Subject to update after a successful login.
     * @param callbackHandler the CallbackHandler that will be used to obtain the
     *    the user identity and credentials.
     * @param sharedState a Map shared between all configured login module instances
     * @param options the parameters passed to the login module.
	 */
	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {

		log = LogManager.getLogger(SICidPropsLoginModule.class);
		trace = log.isTraceEnabled();
		sicid = new SICidClient();
		
		super.initialize(subject, callbackHandler, sharedState, options);

		Map<String, String> loginModuleOpts = parseOptions(options);

		sicidDisable        = loginModuleOpts.get(OPTION_SICID_DISABLE      );
		sicidClientProps    = loginModuleOpts.get(OPTION_SICID_CLIENT_PROPS );
		sicidServiceURL     = loginModuleOpts.get(OPTION_SICID_URL          );
		sicidNamespace      = loginModuleOpts.get(OPTION_SICID_NAMESPACE    );
		sicidService        = loginModuleOpts.get(OPTION_SICID_SERVICE      );
		sicidKeyStoreFile   = loginModuleOpts.get(OPTION_SICID_KEYSTORE     );
		sicidKeyStoreType   = loginModuleOpts.get(OPTION_SICID_KEYSTORE_TYPE);
		sicidKeyStorePass   = loginModuleOpts.get(OPTION_SICID_KEYSTORE_PASS);
		sicidKeyAlias       = loginModuleOpts.get(OPTION_SICID_KEY_ALIAS    );
		sicidKeyPass        = loginModuleOpts.get(OPTION_SICID_KEY_PASS     );

		this.subject = subject;
		this.callbackHandler = callbackHandler;
		
		sicidEnabled = !("true".equalsIgnoreCase(sicidDisable));
		
		if (sicidKeyStorePass == null) { sicidKeyStorePass = ""; }
		if (sicidKeyPass      == null) { sicidKeyPass      = ""; }
		if (sicidKeyAlias     == null) { sicidKeyAlias     = ""; }
		
		if (sicidKeyStoreType == null || "".equals(sicidKeyStoreType)) {
			sicidKeyStoreType = "JKS";
		}

		try {
			users = createUsers(options);
			roles = createRoles(options);

			if (trace) {
				log.trace(String.format("users: %s", users.toString()));
				log.trace(String.format("roles: %s", roles.toString()));
			}
		
		} catch (Exception e) {
			log.error("Failed to load users/roles files", e);
		}	
		
		if (trace) {
			log.trace(String.format("%s=%s", OPTION_SICID_DISABLE      , sicidDisable       ));
			log.trace(String.format("%s=%s", OPTION_SICID_CLIENT_PROPS , sicidClientProps   ));
			log.trace(String.format("%s=%s", OPTION_SICID_URL          , sicidServiceURL    ));
			log.trace(String.format("%s=%s", OPTION_SICID_NAMESPACE    , sicidNamespace     ));
			log.trace(String.format("%s=%s", OPTION_SICID_SERVICE      , sicidService       ));
			log.trace(String.format("%s=%s", OPTION_SICID_KEYSTORE     , sicidKeyStoreFile  ));
			log.trace(String.format("%s=%s", OPTION_SICID_KEYSTORE_TYPE, sicidKeyStoreType  ));
			log.trace(String.format("%s=%s", OPTION_SICID_KEYSTORE_PASS, sicidKeyStorePass  ));
			log.trace(String.format("%s=%s", OPTION_SICID_KEY_ALIAS    , sicidKeyAlias      ));
			log.trace(String.format("%s=%s", OPTION_SICID_KEY_PASS     , sicidKeyPass       ));
		}
	}

	/** 
	 * Perform the authentication of the username and password.
	 * @return True if success. 
	 */
	@Override
	public boolean login() throws LoginException {
		try {
			
			NameCallback nameCallback =
				new NameCallback("Username: ");
			PasswordCallback passwordCallback =
				new PasswordCallback("Password: ", false);
			
			Callback[] callbacks = {nameCallback, passwordCallback};
			callbackHandler.handle(callbacks);
			
			String name = 
				new String(nameCallback.getName())
					.replaceAll("\\s+","");
			String pass = 
				new String(passwordCallback.getPassword())
					.replaceAll("\\s+","");
			
			passwordCallback.clearPassword();

			if (trace) {
				log.trace(String.format("Cert content: %s",name));
				log.trace(String.format("Sign content: %s",pass));
			}

			X509Certificate cert = null;
			
			try {
				cert = CertificadoSerializador.strToCert(name);
				
			} catch (Exception e) {
				
				LoginException exception =
					new LoginException("Error getting the certificate");
				exception.initCause(e);
				throw exception;
			}

			dname = cert.getSubjectX500Principal().getName();
			String username = (String) users.get(dname);

			if (trace) {
				log.trace(String.format("Login: certificate DN: %s", dname));
			}

			try {
				validateCert(name, pass);
				
			} catch (Exception e) {
				
				LoginException exception =
					new LoginException(String.format(
							"Invalid Certificate %s", dname));
				exception.initCause(e);
				throw exception;
			}
			
			if (username == null) {
				LoginException exception =
					new LoginException(String.format(
							"No user mapping for DN: %s", dname));
				throw exception;
			}
			
			if (trace) {
				log.trace(String.format("Username: %s", username));
			}

			// substitui username pelo certificado no identity primario
			//this.user = username;
			this.user = name;
			String rolename = (String) roles.get(username);			

			if (rolename != null) {
				this.role = rolename;
			}
			
			if (trace) {
				log.trace(String.format("Role: %s", rolename));
			}

			return true;
			
		} catch (Exception e) {

			log.error("Login error", e);
			LoginException exception =
				new LoginException("Login error");
			exception.initCause(e);
			throw exception;
		}
	}

    /** 
     * Method to commit the authentication process (phase 2). If the login
     * method completed successfully as indicated by loginOk == true, this
     * method adds the getIdentity() value to the subject getPrincipals() Set.
     * It also adds the members of each Group returned by getRoleSets()
     * to the subject getPrincipals() Set.
     * @return true always.
     */
	@Override
	public boolean commit() throws LoginException {
		if (user != null) {
			subject.getPrincipals().add(new SimplePrincipal(user));			
			if (role != null) {
				SimpleGroup sg = new SimpleGroup("Roles");
				sg.addMember(new SimplePrincipal(role));
				subject.getPrincipals().add(sg);
			}
		}		
		return true;	
	}

    /** 
     * Method to abort the authentication process (phase 2).
     * @return true alaways.
     */
	@Override
	public boolean abort() throws LoginException {
		this.subject         = null;
		this.callbackHandler = null;
		this.user            = null;
		return true;
	}

    /** 
     * Remove the user identity and roles added to the Subject during commit.
     * @return true always.
     */
	@Override
	public boolean logout() throws LoginException {
		if (role != null) {
			subject.getPrincipals().remove(role);	
		}
		subject.getPrincipals().remove(user);		
		return true;
	}
	
	/* Retorna as opcoes do LoginModule.
	 * @param options Opcoes passadas via arquivo login-config.xml.
	 * @return Opcoes do LoginModule.
	 */
	private Map<String, String> parseOptions(Map<String, ?> options) {

		Map<String, String> optList = new HashMap<String, String>();
		
		String disable      = (String) options.get(OPTION_SICID_DISABLE      );
		String clientProps  = (String) options.get(OPTION_SICID_CLIENT_PROPS );
		String serviceURL   = (String) options.get(OPTION_SICID_URL          );
		String namespace    = (String) options.get(OPTION_SICID_NAMESPACE    );
		String service      = (String) options.get(OPTION_SICID_SERVICE      );
		String keyStoreFile = (String) options.get(OPTION_SICID_KEYSTORE     );
		String keyStoreType = (String) options.get(OPTION_SICID_KEYSTORE_TYPE);
		String keyStorePass = (String) options.get(OPTION_SICID_KEYSTORE_PASS);
		String keyAlias     = (String) options.get(OPTION_SICID_KEY_ALIAS    );
		String keyPass      = (String) options.get(OPTION_SICID_KEY_PASS     );

		if (clientProps != null && !"".equals(clientProps)) {
			
			try {
				Properties props = sicid.getOptionsFromFile(clientProps);

				serviceURL   = props.getProperty(OPTION_SICID_URL          ); 
				namespace    = props.getProperty(OPTION_SICID_NAMESPACE    ); 
				service      = props.getProperty(OPTION_SICID_SERVICE      ); 
				keyStoreFile = props.getProperty(OPTION_SICID_KEYSTORE     ); 
				keyStoreType = props.getProperty(OPTION_SICID_KEYSTORE_TYPE); 
				keyStorePass = props.getProperty(OPTION_SICID_KEYSTORE_PASS); 
				keyAlias     = props.getProperty(OPTION_SICID_KEY_ALIAS    ); 
				keyPass      = props.getProperty(OPTION_SICID_KEY_PASS     );
				
			} catch (Exception e) {
				if (trace) {
					log.error("Error getting SICid Client options", e);
				}
			}
		}
		
		optList.put(OPTION_SICID_DISABLE      , disable     );
		optList.put(OPTION_SICID_CLIENT_PROPS , clientProps );
		optList.put(OPTION_SICID_URL          , serviceURL  );
		optList.put(OPTION_SICID_NAMESPACE    , namespace   );
		optList.put(OPTION_SICID_SERVICE      , service     );
		optList.put(OPTION_SICID_KEYSTORE     , keyStoreFile);
		optList.put(OPTION_SICID_KEYSTORE_TYPE, keyStoreType);
		optList.put(OPTION_SICID_KEYSTORE_PASS, keyStorePass);
		optList.put(OPTION_SICID_KEY_ALIAS    , keyAlias    );
		optList.put(OPTION_SICID_KEY_PASS     , keyPass     );
		
		return optList;
	}
	
	/* Valida um certificado, realizando uma consulta ao servico SICid.
	 * @param content Conteudo do certificado, codificado em Base64.
	 * @param signed Assinatura do certificado, codificada em Base64.
	 * @throws Exception
	 */
	private void validateCert(String content, String signed) throws Exception {

		CertificadoStatus status = CertificadoStatus.INVALID;

		try {
			X509Certificate cert = CertificadoSerializador.strToCert(content);
			byte[] sigBuffer = new BASE64Decoder().decodeBuffer(signed);

			if (trace) {
				log.trace(String.format("Validating certificate: %s", 
						cert.getSubjectX500Principal().getName()));
			}

			if (!CertificadoAssinador.verify(cert.getPublicKey(),
					cert.getEncoded(), sigBuffer)) {
				throw new Exception("Wrong signature");
			}
			
			if (sicidEnabled) {
				try {
					sicid.connect(sicidServiceURL, sicidNamespace, sicidService,
							sicidKeyStoreFile, sicidKeyStoreType, sicidKeyAlias, 
							sicidKeyStorePass, sicidKeyPass);
					status = sicid.getService().validarCertificado(content);
					
				} catch (Exception e) { 
					log.error("Failed to connect in SICid service", e);
					Exception exception =
						new Exception("Failed to connect in SICid service");
					exception.initCause(e);
					throw exception;
				}
				
			} else {
				status = CertificadoStatus.VALID;
			}
			
			if (status != CertificadoStatus.VALID) {
				throw new Exception(
						String.format("Certificate status: %s", status));
			}
			
			if (trace) {
				log.trace(String.format("Certificate status: %s", status));
			}
			
		} catch (Exception e) {
			
			if (trace) {
				log.trace(String.format("Certificate: %s", status));
			}
			Exception exception = new Exception("Invalid certificate");
			exception.initCause(e);
			throw exception;
		}
	}
}
