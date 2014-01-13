package icp.model;

import icp.bean.Ca;
import icp.bean.Certificado;
import icp.bean.StatusCertificado;
import icp.bean.TipoCertificado;
import icp.bean.Usuario;
import icp.dao.CaDAO;
import icp.dao.CertificadoDAO;
import icp.dao.IcpBrasilDAO;
import icp.dao.RootCaDAO;
import icp.dao.UsuarioDAO;
import icp.util.SystemWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.security.util.StringPropertyReplacer;

import sicid.bean.Cidadao;
import sicid.util.CertificadoIcpBrasilParser;
import sicid.ws.SICidClient;
import sun.misc.BASE64Encoder;

import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;
import com.robsonmartins.fiap.tcc.util.JBossUtil;

/**
 * Aplicacao para Gerenciamento de uma PKI (ICP) no padrao ICP-Brasil.
 * @author Robson Martins (robson@robsonmartins.com)
 * @see <a target="_blank" href="http://www.iti.gov.br/twiki/bin/view/Certificacao/EstruturaIcp">Estrutura da ICP-Brasil</a>
 */
@PermitAll
@Stateless
public class IcpAdmin implements IIcpAdmin {

	/* nome do arquivo de configuracao da aplicacao */ 
	private static final String ICP_PROPERTIES_FILE_NAME = "icpadmin.properties";
	/* nome do arquivo de properties com a configuracao de conexao ao cliente sicid */ 
	private static final String ICPADMIN_SICID_CLIENT_PROPS_FILE = ICP_PROPERTIES_FILE_NAME;
	/* nome da unidade de persistencia configurada em persistence.xml */
	private static final String PERSISTENCE_UNIT_NAME = "icpadmin";
	
	/* caminho do diretorio base das AC's Raiz */
	private static final String ICP_ROOT_CA_BASE_DIR = File.separatorChar + "root";
	/* caminho do diretorio base das AC's Intermediarias */
	private static final String ICP_CA_BASE_DIR = File.separatorChar + "ca";

	/* Timeout para esperar termino de processo do Sistema Operacional, em ms */
	private static final long PROCESS_WAIT_TIMEOUT = 120000;

	/* diretorio base dos dados da ICP */
	private String baseDir;

	private RootCaDAO rootCaDAO;
	private CaDAO caDAO;
	private IcpBrasilDAO icpBrasilDAO;
	private CertificadoDAO certDAO;
	private UsuarioDAO usuarioDAO;
	/* Cliente SICid */
	private SICidClient sicidClient;
	
	/* EntityManager para JPA. */
	@PersistenceContext(unitName=PERSISTENCE_UNIT_NAME)
	private EntityManager entityManager;
	
	/* para fazer log */
	private static Logger logger;
	private static boolean trace;

	/**
	 * Cria uma nova instancia do Gerenciador de ICP (PKI).
	 * @throws Exception
	 */
	public IcpAdmin() {
		logger = LogManager.getLogger(IcpAdmin.class);
		trace = logger.isTraceEnabled();
	}
	
	/* inicializa DAO's */
	@PostConstruct
	protected void init() {
		Properties props = null;
		try {
			props = getPropertiesFromFile();
	        baseDir = props.getProperty("icpFilesDir");
	        /* substitui os valores das system props do JBoss */
	        baseDir = StringPropertyReplacer.replaceProperties(baseDir);
	        
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao ler arquivo properties", e);
			}
			baseDir = "icpadmin";
		}
		rootCaDAO = new RootCaDAO(baseDir + ICP_ROOT_CA_BASE_DIR);
		caDAO = new CaDAO(baseDir + ICP_CA_BASE_DIR);
		icpBrasilDAO = new IcpBrasilDAO(rootCaDAO, caDAO);

		certDAO = new CertificadoDAO(entityManager);
		usuarioDAO = new UsuarioDAO(entityManager);
		sicidClient = new SICidClient();
	}

	@Override
	public List<Ca> listarACRaiz() {
		return rootCaDAO.refreshListCA();
	}

	@Override
	public void adicionarACRaiz(String nomeAC, String lcrURI,
			String dpcURI, String acPassword, String subjC, String subjO,
			String subjOU, String subjCN) throws Exception {
		
		icpBrasilDAO.createRootCA(nomeAC, lcrURI, dpcURI, acPassword, subjC, subjO, subjOU, subjCN);
	}

	@Override
	public void removerACRaiz(String nomeAC) throws Exception {
		rootCaDAO.deleteCA(nomeAC);
		certDAO.excluirPorNomeAC(nomeAC);
	}
	
	@Override
	public void criarACRaizLCR(Ca acRaiz, String acPassword) throws Exception {
		icpBrasilDAO.createCRLforRootCA(acRaiz, acPassword);
	}

	@Override
	public List<Ca> listarACInterm() {
		return caDAO.refreshListCA();
	}

	@Override
	public void adicionarACInterm(Ca acRaiz, String nomeAC, String lcrURI,
			String dpcURI, String keyPassword, String acRaizPassword,
			String subjC, String subjO, String subjOU, String subjCN) throws Exception {
		
		icpBrasilDAO.createCA(nomeAC, acRaiz, lcrURI, dpcURI, keyPassword,
				acRaizPassword, subjC, subjO, subjOU, subjCN);
	}

	@Override
	public void removerACInterm(String nomeAC) throws Exception {
		caDAO.deleteCA(nomeAC);
		certDAO.excluirPorNomeAC(nomeAC);
	}

	@Override
	public void criarACIntermLCR(Ca ac, String acPassword) throws Exception {
		icpBrasilDAO.createCRLforCA(ac, acPassword);
	}

	@Override
	public List<TipoCertificado> listarTipoCert() {
		return Arrays.asList(TipoCertificado.values());
	}
	
	@Override
	public List<Certificado> listarCert(String nomeAC) {
		return certDAO.listarPorNomeAC(nomeAC);
	}
	
	@Override
	public void emitirCertEcpf(Ca acEmissora, String keyPassword, String acPassword,
			String nome, String cpf, String email, Date nascimento,
			String pisPasep, String rg,	String rgOrgEmissor, String rgUF,
			String cei, String titulo, String tituloZona, String tituloSecao,
			String tituloMunicipio,	String tituloUF, String login,
			String subjC, String subjO, String subjOU) throws Exception {
		
		String certOutFileName = acEmissora.getUsrCertFile   ();
		String keyOutFileName  = acEmissora.getUsrKeyFile    ();
		String reqOutFileName  = acEmissora.getUsrReqFile    ();
		
		icpBrasilDAO.createEcpf(acEmissora, certOutFileName, keyOutFileName, reqOutFileName, keyPassword,
				acPassword,	nome, cpf, email, nascimento, pisPasep, rg, rgOrgEmissor, rgUF, cei,
				titulo, tituloZona, tituloSecao, tituloMunicipio, tituloUF, login, subjC, subjO, subjOU);
		
		String commonName = 
			buildCnByTypeCert(TipoCertificado.ECPF, nome, null, null, null, cpf, null);

		exportarCertPkcs12(certOutFileName, keyOutFileName, keyPassword, commonName);
		
		cadastrarCert(acEmissora, certOutFileName, keyOutFileName, reqOutFileName,
				TipoCertificado.ECPF, commonName);
	}
	
	@Override
	public void emitirCertEcnpj(Ca acEmissora, String keyPassword, String acPassword,
			String nomePJ, String nome, String cpf,
			String email, Date nascimento, String pisPasep, String rg,
			String rgOrgEmissor, String rgUF, String cei,
			String cnpj, String subjC, String subjO, String subjOU,
			String subjL, String subjST) throws Exception {
		
		String certOutFileName = acEmissora.getUsrCertFile();
		String keyOutFileName  = acEmissora.getUsrKeyFile ();
		String reqOutFileName  = acEmissora.getUsrReqFile ();
		
		icpBrasilDAO.createEcnpj(acEmissora, certOutFileName, keyOutFileName, reqOutFileName,
				keyPassword, acPassword, nomePJ, nome, cpf, email, nascimento, pisPasep,
				rg, rgOrgEmissor, rgUF, cei, cnpj, subjC, subjOU, subjOU, subjL, subjST);
		
		String commonName = 
			buildCnByTypeCert(TipoCertificado.ECNPJ, nome, nomePJ, null, null, cpf, cnpj);

		exportarCertPkcs12(certOutFileName, keyOutFileName, keyPassword, commonName);

		cadastrarCert(acEmissora, certOutFileName, keyOutFileName, reqOutFileName,
				TipoCertificado.ECNPJ, commonName);
	}
	
	@Override
	public void emitirCertRic(Ca acEmissora, String keyPassword, String acPassword,
			String nome, String ric, String cpf, String email, Date nascimento,
			String pisPasep, String rg,	String rgOrgEmissor, String rgUF,
			String cei, String titulo, String tituloZona, String tituloSecao,
			String tituloMunicipio,	String tituloUF, String login,
			String subjC, String subjO, String subjOU) throws Exception {
		
		String certOutFileName = acEmissora.getUsrCertFile();
		String keyOutFileName  = acEmissora.getUsrKeyFile ();
		String reqOutFileName  = acEmissora.getUsrReqFile ();
		
		icpBrasilDAO.createRic(acEmissora, certOutFileName, keyOutFileName, reqOutFileName,
				keyPassword, acPassword, nome, ric, cpf, email, nascimento, pisPasep,
				rg, rgOrgEmissor, rgUF, cei, titulo, tituloZona, tituloSecao,
				tituloMunicipio, tituloUF, login, subjC, subjO, subjOU);

		String commonName = 
			buildCnByTypeCert(TipoCertificado.RIC, nome, null, null, null, cpf, null);

		exportarCertPkcs12(certOutFileName, keyOutFileName, keyPassword, commonName);

		cadastrarCert(acEmissora, certOutFileName, keyOutFileName, reqOutFileName,
				TipoCertificado.RIC, commonName);
	}

	@Override
	public void emitirCertEcodigo(Ca acEmissora, String keyPassword, String acPassword,
			String nomePJ, String nome, String cpf,
			String email, Date nascimento, String pisPasep, String rg,
			String rgOrgEmissor, String rgUF,
			String cnpj, String subjC, String subjO, String subjOU) throws Exception {
		
		String certOutFileName = acEmissora.getUsrCertFile();
		String keyOutFileName  = acEmissora.getUsrKeyFile ();
		String reqOutFileName  = acEmissora.getUsrReqFile ();
		
		icpBrasilDAO.createEcodigo(acEmissora, certOutFileName, keyOutFileName, reqOutFileName,
				keyPassword, acPassword, nomePJ, nome, cpf, email, nascimento, pisPasep,
				rg, rgOrgEmissor, rgUF, cnpj, subjC, subjO, subjOU);
		
		String commonName = 
			buildCnByTypeCert(TipoCertificado.ECODIGO, nome, nomePJ, null, null, cpf, cnpj);

		exportarCertPkcs12(certOutFileName, keyOutFileName, keyPassword, commonName);

		cadastrarCert(acEmissora, certOutFileName, keyOutFileName, reqOutFileName,
				TipoCertificado.ECODIGO, commonName);
	}

	@Override
	public void emitirCertEservidor(Ca acEmissora, String keyPassword, String acPassword,
			String nomeDNS, String nomePJ, String guid,
			String nome, String cpf, String email, Date nascimento,
			String pisPasep, String rg,	String rgOrgEmissor, String rgUF, String cnpj,
			String subjC, String subjO, String subjOU) throws Exception {
		
		String certOutFileName = acEmissora.getUsrCertFile();
		String keyOutFileName  = acEmissora.getUsrKeyFile ();
		String reqOutFileName  = acEmissora.getUsrReqFile ();
		
		icpBrasilDAO.createEservidor(acEmissora, certOutFileName, keyOutFileName, reqOutFileName,
				keyPassword, acPassword, nomeDNS, nomePJ, guid, nome, cpf, email, nascimento, pisPasep,
				rg, rgOrgEmissor, rgUF, cnpj, subjC, subjO, subjOU);
		
		String commonName = 
			buildCnByTypeCert(TipoCertificado.ESERVIDOR, nome, nomePJ, nomeDNS, null, cpf, cnpj);

		exportarCertPkcs12(certOutFileName, keyOutFileName, keyPassword, commonName);

		cadastrarCert(acEmissora, certOutFileName, keyOutFileName, reqOutFileName,
				TipoCertificado.ESERVIDOR, commonName);
	}
	
	@Override
	public void emitirCertEaplicacao(Ca acEmissora, String keyPassword, String acPassword,
			String nomeApp, String nomePJ, String nome, String cpf,
			String email, Date nascimento, String pisPasep, String rg,
			String rgOrgEmissor, String rgUF,
			String cnpj, String subjC, String subjO, String subjOU) throws Exception {
		
		String certOutFileName = acEmissora.getUsrCertFile();
		String keyOutFileName  = acEmissora.getUsrKeyFile ();
		String reqOutFileName  = acEmissora.getUsrReqFile ();
		
		icpBrasilDAO.createEaplicacao(acEmissora, certOutFileName, keyOutFileName, reqOutFileName,
				keyPassword, acPassword, nomeApp, nomePJ, nome, cpf, email, nascimento, pisPasep,
				rg, rgOrgEmissor, rgUF, cnpj, subjC, subjO, subjOU);
		
		String commonName = 
			buildCnByTypeCert(TipoCertificado.EAPLICACAO, nome, nomePJ, null, nomeApp, cpf, cnpj);

		exportarCertPkcs12(certOutFileName, keyOutFileName, keyPassword, commonName);

		cadastrarCert(acEmissora, certOutFileName, keyOutFileName, reqOutFileName,
				TipoCertificado.EAPLICACAO, commonName);
	}
	
	@Override
	public void revogarCert(Ca acEmissora, Certificado cert,
			String acPassword) throws Exception {
		
		caDAO.revokeCert(acEmissora, 
				baseDir + File.separatorChar + cert.getCertFilename(),
				acPassword);
		revogarCadastroCert(cert);
		
		if (acEmissora.getRoot() == null || "".equals(acEmissora.getRoot())) {
			icpBrasilDAO.createCRLforRootCA(acEmissora, acPassword);
		} else {
			icpBrasilDAO.createCRLforCA(acEmissora, acPassword);
		}
	}

	@Override
	public void renovarACRaizCert(Ca acRaiz, String password) throws Exception {
		icpBrasilDAO.renewRootCaCert(acRaiz, password);
	}

	@Override
	public void renovarACIntermCert(Ca ac, String password) throws Exception {
		icpBrasilDAO.renewCaCert(ac, password);
	}
	
	@Override
	public void renovarCert(Ca acEmissora, Certificado cert,
			String acPassword) throws Exception {
		
		icpBrasilDAO.renewCert(acEmissora,
				baseDir + File.separatorChar + cert.getCertFilename(),
				baseDir + File.separatorChar + cert.getReqFilename(),
				acPassword);
		renovarCadastroCert(cert);
	}
	
	@Override
	public void atualizarStatusCertExpirados() {
		certDAO.atualizarStatusCertExpirados();
	}
	
	@Override
	public void gerarBackup(String outFileName) throws Exception {
		String icpDirName = new File(baseDir).getName();
		String[] command = { "tar", "-czf", outFileName, "-C", baseDir,
				".." + File.separatorChar + icpDirName };
		Process p = SystemWrapper.executeCommand(command);
		if (SystemWrapper.processWait(p, PROCESS_WAIT_TIMEOUT) != 0) {
			throw new Exception(String.format(
					"Error in tar command: %s",
					SystemWrapper.getProcessErrorStr(p)));
		}
	}
	
	@Override
	public String getBaseDir() {
		return baseDir;
	}
	
	@Override
	public List<Usuario> listarUsuarios() {
		return usuarioDAO.listar();
	}

	@Override
	public List<Usuario> listarUsuariosPorRole(String role) {
		return usuarioDAO.listarPorRole(role);
	}

	@Override
	public Usuario localizarUsuario(String dname) {
		return usuarioDAO.localizar(dname);
	}

	@Override
	public Usuario localizarUsuarioPorUsername(String username) {
		return usuarioDAO.localizarPorUsername(username);
	}
	
	@Override
	public Usuario adicionarUsuario(InputStream istream, 
			String role) throws Exception {

		Usuario usuario = null;
		try {
			if (trace) {
				logger.trace("Adicionando usuario");
			}

			X509Certificate x509cert =
				CertificadoSerializador.loadCertFromStream(istream);
		
			String dname = x509cert.getSubjectX500Principal().getName();
			usuario = new Usuario();
			usuario.setDname(dname);
	
			MessageDigest md = MessageDigest.getInstance("SHA1");
			String id =
				new BASE64Encoder().encode(
						md.digest(dname.getBytes()));
			usuario.setUsername(id);
			
			String name = null;
			try {
				String content = CertificadoSerializador.certToStr(x509cert);
				sicidClient.connect(ICPADMIN_SICID_CLIENT_PROPS_FILE);
				Cidadao cidadao = sicidClient.getService().consultarCidadao(content);
				if (cidadao != null) { 
					name = cidadao.getNome();
				}
				if (name == null) {
					throw new Exception("Cidad\u00E3o n\u00E3o encontrado.");
				}
				
			} catch (Exception e) {
				
				if (trace) {
					logger.error("Erro ao obter o nome do cidadao", e);
				}
			}
			if (name == null) { 
				name = CertificadoIcpBrasilParser.extractNomeFromCN(dname);
				if (name == null) { name = dname; }
			}
			usuario.setName(name);
			usuario.setRole(role);
			
			usuarioDAO.inserir(usuario);

			if (trace) {
				logger.trace("Usuario adicionado com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao adicionar usuario", e);
			}
			throw e;
		}
		return usuario;
	}

	@Override
	public Usuario adicionarAdmin(InputStream istream) throws Exception {
		return adicionarUsuario(istream, ICPADMIN_ADMIN_ACCESS_ROLE);
	}
	
	@Override
	public void removerUsuario(String dname) throws Exception {

		try {
			if (trace) {
				logger.trace("Removendo usuario");
			}

			usuarioDAO.excluir(dname);
			
			if (trace) {
				logger.trace("Usuario removido com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao remover usuario", e);
			}
			throw e;
		}
	}
	
	/* Exporta um certificado como PKCS#12 e sua chave privada num arquivo descriptografado.
	 * @param certInFileName Nome do arquivo de certificado PEM.
	 * @param keyInFileName Nome do arquivo de chave privada (key) criptografada.
	 * @param keyPassword Senha da chave privada.
	 * @param keyAlias Nome amigavel do par de chaves dentro do arquivo PKCS#12.
	 * @throws Exception
	 */
	private void exportarCertPkcs12(String certInFileName, String keyInFileName,
			String keyPassword, String keyAlias) throws Exception {
		String pfxOutFileName    = certInFileName.replace(".pem.cer",".pfx");
		String keyNoPassFileName = keyInFileName .replace(".pem", ".plain");
		caDAO.exportCertPkcs12(certInFileName, keyInFileName, pfxOutFileName, keyPassword, keyAlias);
		caDAO.exportKeyNoPassword(keyInFileName, keyNoPassFileName, keyPassword);
	}

	/* Insere um novo certificado no cadastro.
	 * @param acEmissora Objeto que representa a AC emissora (issuer).
	 * @param certFileName Nome do arquivo de certificado a ser gerado.
	 * @param keyFileName Nome do arquivo de chave privada a ser gerado.
	 * @param reqFileName Nome do arquivo de requisicao a ser gerado.
	 * @param tipo Tipo do certificado.
	 * @param commonName Common Name (CN) do certificado.
	 * @throws Exception
	 */
	private void cadastrarCert(Ca acEmissora, String certFileName, String keyFileName,
			String reqFileName, TipoCertificado tipo,
			String commonName) throws Exception {

		Certificado cert = new Certificado();
		
		cert.setId          (0);
		cert.setCertFilename(certFileName.replace(baseDir+File.separatorChar,""));
		cert.setKeyFilename (keyFileName .replace(baseDir+File.separatorChar,""));
		cert.setReqFilename (reqFileName .replace(baseDir+File.separatorChar,""));
		cert.setNomeAC      (acEmissora.getName());
		cert.setCommonName  (commonName);
		cert.setTipo        (tipo);
		cert.setStatus      (StatusCertificado.VALIDO);
		cert.setEmissao     (new Date());

		Calendar calendar = new GregorianCalendar();
		calendar.setTime(cert.getEmissao());
		calendar.add(Calendar.DAY_OF_MONTH, icpBrasilDAO.getUserExpireDays());
		cert.setExpiracao(calendar.getTime());
		
		certDAO.inserir(cert);
	}
	

	/* Constroi o Common Name (CN), a partir do tipo de certificado.
	 * @param tipo Tipo do certificado.
	 * @param nome Nome do responsavel.
	 * @param nomePJ Nome empresarial.
	 * @param nomeDNS Nome de DNS do servidor.
	 * @param nomeApp Nome da aplicacao.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @return Common Name do certificado.
	 */
	private String buildCnByTypeCert(TipoCertificado tipo,
			String nome, String nomePJ, String nomeDNS, String nomeApp,
			String cpf, String cnpj) {
		
		StringBuilder cn = new StringBuilder();
		switch (tipo) {
			case ECPF      : cn.append(nome   ).append(':').append(cpf ); break;
			case ECNPJ     : cn.append(nomePJ ).append(':').append(cnpj); break;
			case RIC       : cn.append(nome   ).append(':').append(cpf ); break;
			case ECODIGO   : cn.append(nomePJ ).append(':').append(cnpj); break;
			case ESERVIDOR : cn.append(nomeDNS)                         ; break;
			case EAPLICACAO: cn.append(nomeApp).append(':').append(cnpj); break;
			default        :                                              break;
		}
		return cn.toString();
	}

	/* Atualiza um certificado no cadastro, no caso, uma revogacao.
	 * @param cert Certificado sendo revogado.
	 * @throws Exception
	 */
	private void revogarCadastroCert(Certificado cert) throws Exception {
		cert.setStatus(StatusCertificado.REVOGADO);
		certDAO.inserir(cert);
	}	

	/* Atualiza um certificado no cadastro, no caso, uma renovacao. 
	 * @param cert Certificado sendo renovado.
	 * @throws Exception
	 */
	private void renovarCadastroCert(Certificado cert) throws Exception {
		cert.setEmissao(new Date());
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(cert.getEmissao());
		calendar.add(Calendar.DAY_OF_MONTH, icpBrasilDAO.getUserExpireDays());
		cert.setExpiracao(calendar.getTime());
		cert.setStatus(StatusCertificado.VALIDO);
		certDAO.inserir(cert);
	}	

	/* Obtem arquivo de properties com a configuracao da aplicacao.
	 * @return Objeto contendo as properties do arquivo.
	 * @throws Exception
	 */
	private Properties getPropertiesFromFile() throws Exception {
		Properties props = new Properties();
		InputStream inputStream = null;
		String filename = null;
		/* tenta obter path absoluto do arquivo no JBoss */
		filename = JBossUtil.getJBossAbsFilePath(ICP_PROPERTIES_FILE_NAME);
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
			/* tenta obter arquivo no path META-INF especificado */
			try {
				inputStream =
					this.getClass().getClassLoader().getResourceAsStream(
							String.format("%s%s%s", "META-INF", 
									File.separator, ICP_PROPERTIES_FILE_NAME));
			} catch (Exception e) { }
		}
		if (inputStream == null) {
			throw new FileNotFoundException(
					String.format("File %s not found.", ICP_PROPERTIES_FILE_NAME));
		}		
		props.load(inputStream);
		return props;
	}
}
