package com.robsonmartins.fiap.tcc.sicid.jaas;

import java.security.Principal;
import java.security.acl.Group;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.security.NestableGroup;
import org.jboss.security.SimpleGroup;
import org.jboss.security.auth.spi.DatabaseServerLoginModule;
import org.jboss.security.plugins.TransactionManagerLocator;

import com.robsonmartins.fiap.tcc.util.CertificadoAssinador;
import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;

import sicid.ws.SICidClient;
import sicid.bean.CertificadoStatus;
import sun.misc.BASE64Decoder;

/**
 * LoginModule para uso com o JAAS, baseado em banco de dados (JDBC).<br/>
 * Implementa a validacao de certificados digitais atraves de consulta ao
 *   Servico de Identificacao do Cidadao (SICid).
 *   
 * <p>
 *  Tabelas:
 * <ul>
 * <li>Principals(DN text, PrincipalID text)
 * <li>Roles(PrincipalID text, Role text, RoleGroup text)
 * </ul>
* <p>
 * Opcoes do LoginModule:
 * <ul> 
 * <li><em>dsJndiName</em>: O nome do DataSource do banco de dados que contem
 *   as tabelas Principals e Roles.
 * <li><em>principalsQuery</em>: A consulta SQL para obter um nome de usuario
 *   mapeado a partir de um Distinguished Name (DN) do certificado digital, equivalente a:
 * <pre>
 *    "select PrincipalID from Principals where DN=?"
 * </pre>
 * <li><em>rolesQuery</em>: A consulta SQL para obter os Roles e RoleGroups 
 *   a partir de um nome de usuario, equivalente a:
 * <pre>
 *    "select Role, RoleGroup from Roles where PrincipalID=?"
 * </pre>
 * <li><em>fullPrincipalsQuery</em>: A consulta SQL para obter todos usuarios cadastrados,
 *   equivalente a:
 * <pre>
 *    "select * from Principals"
 * </pre>
 * <li><em>principalIdForEmpty</em>: Id a ser atribuida a um usuario caso nao haja
 *   usuarios cadastrados.
 * <li><em>roleForEmpty</em>: Role a ser atribuida a um usuario caso nao haja usuarios
 *   cadastrados.
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
public class SICidDBLoginModule extends DatabaseServerLoginModule {
	
	/* nomes das opcoes do loginModule no arquivo login-config.xml */
	private final static String OPTION_FULL_QUERY          = "fullPrincipalsQuery";
	private final static String OPTION_ID_FOR_EMPTY        = "principalIdForEmpty";
	private final static String OPTION_ROLE_FOR_EMPTY      = "roleForEmpty"       ;

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

	private String dname;
	private Principal identity;
	private Principal usernameIdentity;
	private Group[] roles;

	private String fullPrincipalsQuery;
	private String principalIdForEmpty;
	private String roleForEmpty;
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
	
	private boolean isPrincipalsEmpty;
	
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

		log = LogManager.getLogger(SICidDBLoginModule.class);
		trace = log.isTraceEnabled();
		sicid = new SICidClient();

		super.initialize(subject, callbackHandler, sharedState, options);
		
		Map<String, String> loginModuleOpts = parseOptions(options);

		fullPrincipalsQuery = loginModuleOpts.get(OPTION_FULL_QUERY         );
		principalIdForEmpty = loginModuleOpts.get(OPTION_ID_FOR_EMPTY       );
		roleForEmpty        = loginModuleOpts.get(OPTION_ROLE_FOR_EMPTY     );
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
		isPrincipalsEmpty = false;

		if (sicidKeyStorePass == null) { sicidKeyStorePass = ""; }
		if (sicidKeyPass      == null) { sicidKeyPass      = ""; }
		if (sicidKeyAlias     == null) { sicidKeyAlias     = ""; }
		
		if (sicidKeyStoreType == null || "".equals(sicidKeyStoreType)) {
			sicidKeyStoreType = "JKS";
		}
		
		if (trace) {
			log.trace(String.format("%s=%s", OPTION_FULL_QUERY         , fullPrincipalsQuery));
			log.trace(String.format("%s=%s", OPTION_ID_FOR_EMPTY       , principalIdForEmpty));
			log.trace(String.format("%s=%s", OPTION_ROLE_FOR_EMPTY     , roleForEmpty       ));
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
			super.loginOk = false;
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
			
			isPrincipalsEmpty = isPrincipalsEmpty();

			dname = cert.getSubjectX500Principal().getName();
			String username = (String) getUsersPassword();

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

			if (this.identity == null) {
				try {
					this.identity = createIdentity(username);
				} catch (Exception e) {
					if (trace) {
						log.trace(String.format("Failed to create principal: %s",username), e);
					}
					LoginException exception =
							new LoginException(
									String.format("Failed to create principal: %s",username));
					exception.initCause(e);
					throw exception;
				}
			}

			this.roles = getRoleSets();			

			if (trace) {
				log.trace(String.format("Roles: %s", Arrays.toString(roles)));
			}

			super.loginOk = true;
			
			if (trace) {
				log.trace("User '" + this.identity + "' authenticated, loginOk="+loginOk);
			}
			
			// substitui username pelo certificado no identity primario
			this.usernameIdentity = this.identity; 
			try {
				this.identity = createIdentity(name);
			} catch (Exception e) {
				if (trace) {
					log.trace(String.format("Failed to create principal: %s",name), e);
				}
				LoginException exception =
					new LoginException(
							String.format("Failed to create principal: %s",name));
				exception.initCause(e);
				throw exception;
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
		Set<Principal> principals = this.subject.getPrincipals();
		if (this.identity != null) {
			principals.add(this.identity);
		}
		if (this.usernameIdentity != null) {
			principals.add(this.usernameIdentity);
		}
		if (this.roles != null) {
			for (Group group : this.roles) {

				String name = group.getName();
				Group subjectGroup = createGroup(name, principals);
				
				if (subjectGroup instanceof NestableGroup) {
					SimpleGroup sg = new SimpleGroup("Roles");
			        subjectGroup.addMember(sg);
			        subjectGroup = sg;
				}
				
				Enumeration<? extends Principal> members = group.members();
				while (members.hasMoreElements()) {
					Principal role = (Principal) members.nextElement();
					subjectGroup.addMember(role);
				}
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
		this.identity        = null;
		this.usernameIdentity     = null;
		return true;
	}

    /** 
     * Remove the user identity and roles added to the Subject during commit.
     * @return true always.
     */
	@Override
	public boolean logout() throws LoginException {
		Set<Principal> principals = this.subject.getPrincipals();
		if (this.identity != null) {
			principals.remove(this.identity);
		}
		if (this.usernameIdentity != null) {
			principals.remove(this.usernameIdentity);
		}
		return true;
	}

    /** 
     * Get the expected password for the current username available via
     * the getUsername() method. This is called from within the login()
     * method after the CallbackHandler has returned the username and
     * candidate password.
     * @return the valid password String.
     */
	@Override
	protected String getUsersPassword() throws LoginException {

		if (isPrincipalsEmpty) {
			return principalIdForEmpty;
		}
		
		String username = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		Transaction tx = null;
		if (suspendResume) {
			try {
				if (tm == null) {
					throw new IllegalStateException("Transaction Manager is null");
				}
				tx = tm.suspend();
			} catch (SystemException e)	{
				throw new RuntimeException(e);
			}
			if (trace) {
				log.trace("suspendAnyTransaction");
			}
		}

		try	{
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(dsJndiName);
			conn = ds.getConnection();
			if (trace) {
				log.trace(
						String.format("Executing query: %s, with DN: %s",
								principalsQuery, dname));
			}
			ps = conn.prepareStatement(principalsQuery);
			ps.setString(1, dname);
			rs = ps.executeQuery();
			if (rs.next() == false) {
				if (trace) {
					log.trace("Query returned no matches from db");
				}
				throw new FailedLoginException(
						"No matching username found in Principals");
			}

			username = rs.getString(1);
			if (trace) {
				log.trace("Obtained username");
			}
			
		} catch (NamingException ex) {
			
			LoginException le =
				new LoginException(
						String.format("Error looking up DataSource from: %s", dsJndiName));
			le.initCause(ex);
			throw le;
			
		} catch(SQLException ex) {
			
			LoginException le = new LoginException("Query failed");
			le.initCause(ex);
			throw le;
			
		} finally {
			
			if (rs != null)	{
				try	{
					rs.close();
				} catch (SQLException e) {}
			}
			if (ps != null) {
				try	{
					ps.close();
				} catch(SQLException e) {}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {}
			}
			if (suspendResume) {
				try	{
					tm.resume(tx);
				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
				if (trace) {
					log.trace("resumeAnyTransaction");
				}
			}
		}
		return username;
	}
	
	/**
	 * Returns the username.
	 * @return the username.
	 */
	@Override
	protected String getUsername() {
		String username = null;
		if (getIdentity() != null) {
			username = getIdentity().getName();
		}
		return username;
	}
	
	/**
	 * Returns the Principal that corresponds to
	 * the user primary identity.
	 * @return the user primary identity.
	 */
	@Override
	protected Principal getIdentity() {
		return this.identity;
	}

    /** Execute the rolesQuery against the dsJndiName to obtain the roles for
     *  the authenticated user.
     *  @return Group[] containing the sets of roles
     */
	@Override
	protected Group[] getRoleSets() throws LoginException {
		
		Group[] roleSets = null;
			
		if (isPrincipalsEmpty) {
			roleSets = new Group[1];
			SimpleGroup sg = new SimpleGroup("Roles");
			try {
				Principal p = createIdentity(roleForEmpty);
				sg.addMember(p);
			} catch (Exception e) {
				if (trace) {
					log.trace(String.format(
						"Failed to create principal: %s", roleForEmpty), e);
				}
			}
			roleSets[0] = sg;
			return roleSets;			
		}
		
		String username = getUsername();
		if (trace) {
			log.trace(String.format(
					"getRoleSets using rolesQuery: %s, username: %s",
					rolesQuery, username));
		}
		roleSets =
			getRoleSets(username, dsJndiName, rolesQuery, suspendResume);
		
		return roleSets;
	}

	/* Retorna as opcoes do LoginModule.
	 * @param options Opcoes passadas via arquivo login-config.xml.
	 * @return Opcoes do LoginModule.
	 */
	private Map<String, String> parseOptions(Map<String, ?> options) {

		Map<String, String> optList = new HashMap<String, String>();
		
		String fullQuery    = (String) options.get(OPTION_FULL_QUERY         );
		String idForEmpty   = (String) options.get(OPTION_ID_FOR_EMPTY       );
		String roleForEmpty = (String) options.get(OPTION_ROLE_FOR_EMPTY     );
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
		
		optList.put(OPTION_FULL_QUERY         , fullQuery   );
		optList.put(OPTION_ID_FOR_EMPTY       , idForEmpty  );
		optList.put(OPTION_ROLE_FOR_EMPTY     , roleForEmpty);
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
	
	/* Retorna true se a tabela de usuarios esta' vazia.
	 * @return True se nao ha' usuarios cadastrados.
	 * @throws Exception
	 */
	private boolean isPrincipalsEmpty() {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		TransactionManager tm = null;

		if (suspendResume) {
			TransactionManagerLocator tml = new TransactionManagerLocator();
			try {
				tm = tml.getTM("java:jboss/TransactionManager");
			} catch (NamingException e1) {
				if (trace) {
					log.trace("Error in Transaction Manager", e1);
				}
				return false;
			}
			if (tm == null) {
				if (trace) {
					log.trace("Transaction Manager is null");
				}
				return false;
			}
		}      
		Transaction tx = null;
		if (suspendResume) {
			try	{
				tx = tm.suspend();
			} catch (SystemException e)	{
				if (trace) {
					log.trace("Error suspending transaction", e);
				}
				return false;
			}
			if (trace) {
				log.trace("suspendAnyTransaction");
			}
		}

		try	{
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(dsJndiName);
			conn = ds.getConnection();
			if (trace) {
				log.trace(String.format(
						"Executing query: %s", fullPrincipalsQuery));
			}
			ps = conn.prepareStatement(fullPrincipalsQuery);
			rs = ps.executeQuery();
			if (rs.next() == false) {
				if (trace) {
					log.trace("Principals table is empty");
				}
				return true;
			}
		} catch (Exception e) {
			if (trace) {
				log.trace("Error executing query", e);
			}
			return false;
			
		} finally {
			if (rs != null)	{
				try	{
					rs.close();
				} catch (SQLException e) {}
			}
			if (ps != null)	{
				try	{
					ps.close();
				} catch(SQLException e)	{}
			}
			if (conn != null) {
				try	{
					conn.close();
				} catch (Exception ex) {}
			}
			if (suspendResume) {
				try {
					tm.resume(tx);
				} catch (Exception e) {
					if (trace) {
						log.trace("Error resuming transaction", e);
					}
					return false;
				}
				if (trace) {
					log.trace("resumeAnyTransaction");
				}
			}
		}
		if (trace) {
			log.trace("Principals table is not empty");
		}
		return false;
	}
	
	/* 
	 * Execute the rolesQuery against the dsJndiName to obtain the roles
	 *   for the authenticated user.
	 * @return Group[] containing the sets of roles
	 * 
	 * Source: org.jboss.security.auth.spi.DbUtil.java
	 */
	private Group[] getRoleSets(String username, String dsJndiName,
			String rolesQuery, boolean suspendResume) throws LoginException {
		
		Connection conn = null;
		HashMap<String,Group> setsMap = new HashMap<String,Group>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		TransactionManager tm = null;

		if (suspendResume) {
			TransactionManagerLocator tml = new TransactionManagerLocator();
			try {
				tm = tml.getTM("java:/TransactionManager");
				
			} catch (NamingException e1) {
				
				throw new RuntimeException(e1);
			}
			
			if (tm == null) {
				throw new IllegalStateException("Transaction Manager is null");
			}
		}      
		Transaction tx = null;
		if (suspendResume) {
			try	{
				tx = tm.suspend();
				
			} catch (SystemException e)	{
				
				throw new RuntimeException(e);
			}
			if (trace) {
				log.trace("suspendAnyTransaction");
			}
		}

		try	{
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(dsJndiName);
			conn = ds.getConnection();
			if (trace) {
				log.trace(String.format(
						"Executing query: %s, with username: %s", rolesQuery, username));
			}
			ps = conn.prepareStatement(rolesQuery);
			try {
				ps.setString(1, username);
			} catch (ArrayIndexOutOfBoundsException ignore) {}
			
			rs = ps.executeQuery();
			if (rs.next() == false) {
				if (trace) {
					log.trace("No roles found");
				}
		        if (getUnauthenticatedIdentity() == null) {
		        	throw new FailedLoginException("No matching username found in Roles");
		        }
				Group[] roleSets = { new SimpleGroup("Roles") };
				return roleSets;
			}

			do {
				String name = rs.getString(1);
				String groupName = rs.getString(2);
				if (groupName == null || groupName.length() == 0) {
					groupName = "Roles";
				}
				Group group = (Group) setsMap.get(groupName);
				if (group == null) {
					group = new SimpleGroup(groupName);
					setsMap.put(groupName, group);
				}

				try {
					Principal p = createIdentity(name);
					if (trace) {
						log.trace(String.format(
								"Assign user to role %s", name));
					}
					group.addMember(p);
				}
				catch (Exception e) {
					if (trace) {
						log.trace(String.format(
							"Failed to create principal: %s", name), e);
					}
				}
				
			} while (rs.next());
			
		} catch (NamingException ex) {
			
			LoginException le =
				new LoginException(String.format(
						"Error looking up DataSource from: %s", dsJndiName));
			le.initCause(ex);
			throw le;
			
		} catch (SQLException ex) {
			
			LoginException le = new LoginException("Query failed");
			le.initCause(ex);
		 	throw le;
		 	
		} finally {

			if (rs != null)	{
				try	{
					rs.close();
				} catch (SQLException e) {}
			}
			if (ps != null)	{
				try	{
					ps.close();
				} catch(SQLException e)	{}
			}
			if (conn != null) {
				try	{
					conn.close();
				} catch (Exception ex) {}
			}
			if (suspendResume) {
				try {
					tm.resume(tx);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				if (trace) {
					log.trace("resumeAnyTransaction");
				}
			}
		}

		Group[] roleSets = new Group[setsMap.size()];
		setsMap.values().toArray(roleSets);
		return roleSets;
	}
}
