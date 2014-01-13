package sicid.model;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import sicid.bean.CertificadoConfiavel;
import sicid.bean.CertificadoStatus;
import sicid.bean.Cidadao;
import sicid.bean.ConsumidorConfiavel;
import sicid.bean.DocumentoRG;
import sicid.bean.DocumentoTitulo;
import sicid.bean.Usuario;
import sicid.dao.CertificadoConfiavelDAO;
import sicid.dao.CidadaoDAO;
import sicid.dao.ConsumidorConfiavelDAO;
import sicid.dao.UsuarioDAO;
import sicid.util.CertificadoIcpBrasilParser;
import sicid.util.CertificadoIcpBrasilParser.AtributoIcpBrasil;
import sicid.util.CertificadoValidador;
import sun.misc.BASE64Encoder;

import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;

/**
 * Motor do Servico de Identificacao do Cidadao (SICid).<br/>
 * Implementa as funcionalidades do servico.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@PermitAll
@Stateless
public class SICidEngine implements ISICidEngine {
	
	/* nome da unidade de persistencia configurada em persistence.xml */
	private static final String PERSISTENCE_UNIT_NAME = "sicid";
	
	/* DAO de certificados confiaveis */
	private CertificadoConfiavelDAO certConfiavelDAO;
	/* DAO de consumidores confiaveis */
	private ConsumidorConfiavelDAO consConfiavelDAO;
	/* DAO de usuarios */
	private UsuarioDAO usuarioDAO;
	/* DAO de cidadaos */
	private CidadaoDAO cidadaoDAO;
	
	/* EntityManager para JPA. */
	@PersistenceContext(unitName=PERSISTENCE_UNIT_NAME)
	private EntityManager entityManager;

	/* para fazer log */
	private static Logger logger;
	private static boolean trace;
	
	/**
	 * Cria uma nova instancia do motor do SICid.
	 */
	public SICidEngine() {
		logger = LogManager.getLogger(SICidEngine.class);
		trace = logger.isTraceEnabled();
	}
	
	/* inicializa DAO's */
	@PostConstruct
	protected void init() {
		certConfiavelDAO = new CertificadoConfiavelDAO(entityManager);
		consConfiavelDAO = new ConsumidorConfiavelDAO(entityManager);
		usuarioDAO = new UsuarioDAO(entityManager);
		cidadaoDAO = new CidadaoDAO(entityManager);
	}

	@Override
	public CertificadoStatus validarCertificado(String content) {
		X509Certificate x509Cert = null;
		try {
			if (trace) {
				logger.trace("Validando um certificado");
			}
			if (content == null) { 
				throw new NullPointerException("Certificado inv\u00E1lido (nulo).");
			}
			x509Cert = CertificadoSerializador.strToCert(content);
			if (trace) {
				logger.trace(String.format("Certificado (DN): %s", 
						x509Cert.getSubjectX500Principal().getName()));
			}
			
			if (CertificadoValidador.isSelfSigned(x509Cert)) {
				if (trace) {
					logger.trace("Certificado INVALIDO: auto-assinado");
				}
				return CertificadoStatus.INVALID;
			}
			if (!CertificadoValidador.isValidByDate(x509Cert)) {
				if (trace) {
					logger.trace("Certificado EXPIRADO");
				}
				return CertificadoStatus.EXPIRED;
			}
			if (!CertificadoValidador.isValidKeyChain(x509Cert, 
					listarX509CertConfiaveis())) {
				if (trace) {
					logger.trace("Certificado INVALIDO: cadeia nao-confiavel");
				}
				return CertificadoStatus.INVALID;
			}
			if (CertificadoValidador.isRevoked(x509Cert)) {
				if (trace) {
					logger.trace("Certificado REVOGADO");
				}
				return CertificadoStatus.REVOKED;
			}
			if (trace) {
				logger.trace("Certificado VALIDO");
			}
			return CertificadoStatus.VALID;
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao validar certificado", e);
			}
			return CertificadoStatus.UNKNOWN;
		}
	}

	@Override
	public Cidadao consultarCidadao(String content) {
		X509Certificate x509Cert = null;
		Cidadao cidadao = null;
		Map<AtributoIcpBrasil, String> props = null;
		try {
			if (trace) {
				logger.trace("Consultando informacoes de um cidadao");
			}
			if (content == null) { 
				throw new NullPointerException("Certificado inv\u00E1lido (nulo).");
			}

			x509Cert = CertificadoSerializador.strToCert(content);
			if (trace) {
				logger.trace(String.format("Certificado (DN): %s", 
						x509Cert.getSubjectX500Principal().getName()));
			}
			
			cidadao = 
				cidadaoDAO.localizar(
					x509Cert.getSubjectX500Principal().getName());
			if (cidadao == null) {
				throw new Exception("Cidad\u00E3o n\u00E3o encontrado.");
			}
			
			props =
				CertificadoIcpBrasilParser.getAtributosIcpBrasil(x509Cert);
			if (props != null) {
				cidadao.setEmail(props.get(AtributoIcpBrasil.EMAIL));
				cidadao.setLogin(props.get(AtributoIcpBrasil.LOGIN));
			}
			if (trace) {
				logger.trace(String.format(
						"Obtidas informacoes do cidadao '%s'", cidadao.getNome()));
			}
			
		} catch (Exception e) {	
			if (trace) {
				logger.error("Erro ao obter informacoes do cidadao", e);
			}
			cidadao = new Cidadao();
		}
		return cidadao;
	}

	@Override
	public List<Cidadao> listarCidadaos() {
		return cidadaoDAO.listar();
	}

	@Override
	public List<CertificadoConfiavel> listarCertConfiaveis() {
		return certConfiavelDAO.listar();
	}

	@Override
	public void adicionarCertConfiavel(InputStream istream) throws Exception {
		
		try {
			if (trace) {
				logger.trace("Adicionando certificado confiavel");
			}

			X509Certificate x509cert =
				CertificadoSerializador.loadCertFromStream(istream);
		
			CertificadoConfiavel trustedCert = new CertificadoConfiavel();
			trustedCert.setId(0);
			trustedCert.setX509Certificate(x509cert);
		
			certConfiavelDAO.inserir(trustedCert);
			
			if (trace) {
				logger.trace("Certificado confiavel adicionado com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao adicionar certificado confiavel", e);
			}
			throw e;
		}
	}
	
	@Override
	public void removerCertConfiavel(long id) throws Exception {
		
		try {
			if (trace) {
				logger.trace("Removendo certificado confiavel");
			}
			certConfiavelDAO.excluir(id);

			if (trace) {
				logger.trace("Certificado confiavel removido com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao remover certificado confiavel", e);
			}
			throw e;
		}
	}

	@Override
	public List<ConsumidorConfiavel> listarAppsConfiaveis() {
		return consConfiavelDAO.listar();
	}

	@Override
	public void adicionarAppConfiavel(InputStream istream) throws Exception {
		
		try {
			if (trace) {
				logger.trace("Adicionando aplicacao confiavel");
			}
			
			X509Certificate x509cert =
				CertificadoSerializador.loadCertFromStream(istream);
			
			ConsumidorConfiavel trustedApp = new ConsumidorConfiavel();
			String dname = x509cert.getSubjectX500Principal().getName();
			trustedApp.setDname(dname);
	
			MessageDigest md = MessageDigest.getInstance("SHA1");
			String id =
				new BASE64Encoder().encode(
						md.digest(dname.getBytes()));
			trustedApp.setId(id);
			
			String name = CertificadoIcpBrasilParser.extractNomeFromCN(dname);
			if (name == null) { name = dname; }

			trustedApp.setName(name);
			trustedApp.setRole(SICID_ACCESS_ROLE);
			
			consConfiavelDAO.inserir(trustedApp);
			
			if (trace) {
				logger.trace("Aplicacao confiavel adicionada com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao adicionar aplicacoa confiavel", e);
			}
			throw e;
		}
	}

	@Override
	public void removerAppConfiavel(String dname) throws Exception {
		
		try {
			if (trace) {
				logger.trace("Removendo aplicacao confiavel");
			}
			
			consConfiavelDAO.excluir(dname);
			
			if (trace) {
				logger.trace("Aplicacao confiavel removido com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao remover aplicacao confiavel", e);
			}
			throw e;
		}
	}
	
	@Override
	public void adicionarAdministrador(InputStream istream) throws Exception {
		
		try {
			if (trace) {
				logger.trace("Adicionando administrador");
			}

			X509Certificate x509cert =
				CertificadoSerializador.loadCertFromStream(istream);
		
			Usuario trustedUser = new Usuario();
			String dname = x509cert.getSubjectX500Principal().getName();
			trustedUser.setDname(dname);
	
			MessageDigest md = MessageDigest.getInstance("SHA1");
			String id =
				new BASE64Encoder().encode(
						md.digest(dname.getBytes()));
			trustedUser.setUsername(id);
			
			String name = CertificadoIcpBrasilParser.extractNomeFromCN(dname);
			if (name == null) { name = dname; }

			trustedUser.setName(name);
			trustedUser.setRole(SICIDWEB_ACCESS_ROLE);
			
			usuarioDAO.inserir(trustedUser);

			if (trace) {
				logger.trace("Administrador adicionado com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao adicionar administrador", e);
			}
			throw e;
		}
			
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
	public void adicionarCidadao(Cidadao cidadao) throws Exception {
		
		try {
			if (trace) {
				logger.trace("Adicionando cidadao");
			}

			cidadaoDAO.inserir(cidadao);
			
			if (trace) {
				logger.trace("Cidadao adicionado com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao adicionar cidadao", e);
			}
			throw e;
		}
	}

	@Override
	public void removerCidadao(String dname) throws Exception {
		
		try {
			if (trace) {
				logger.trace("Removendo cidadao");
			}

			cidadaoDAO.excluir(dname);
			
			if (trace) {
				logger.trace("Cidadao removido com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao remover cidadao", e);
			}
			throw e;
		}
	}
	
	@Override
	public Cidadao getCidadaoInfoFromCert(String content) throws Exception {
		X509Certificate cert = null;
		Map<AtributoIcpBrasil, String> props = null;

		Cidadao cidadao = new Cidadao();
		DocumentoRG rg = new DocumentoRG();
		DocumentoTitulo titulo = new DocumentoTitulo();
		
		if (trace) {
			logger.trace("Obtendo informacoes de um certificado");
		}

		try {
			cert = CertificadoSerializador.strToCert(content);
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao obter certificado: conteudo invalido", e);
			}
			throw e;
		}
		if (trace) {
			logger.trace(String.format("Certificado (DN): %s", 
					cert.getSubjectX500Principal().getName()));
		}
		try {
			props = CertificadoIcpBrasilParser.getAtributosIcpBrasil(cert);
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao obter informacoes do certificado", e);
			}
			throw e;
		}
		
		if (props != null) {
			cidadao.setDname(cert.getSubjectX500Principal().getName());
			cidadao.setNome(props.get(AtributoIcpBrasil.NOME_RESPONSAVEL));
			try {
				cidadao.setDataNascimento(
						new SimpleDateFormat("ddMMyyyy").parse(
								props.get(AtributoIcpBrasil.NASCIMENTO)));
			} catch (Exception e) {
				if (trace) {
					logger.trace("Erro ao obter data de nascimento", e);
				}
			}
			cidadao.setRic(props.get(AtributoIcpBrasil.RIC));
			cidadao.setCpf(props.get(AtributoIcpBrasil.CPF));
			
			rg.setNumero(props.get(AtributoIcpBrasil.RG));
			rg.setOrgExpedidor(props.get(AtributoIcpBrasil.RG_ORGEXPEDIDOR));
			rg.setUf(props.get(AtributoIcpBrasil.RG_UF));
			
			titulo.setNumero(props.get(AtributoIcpBrasil.TITULO));
			titulo.setZona(props.get(AtributoIcpBrasil.TITULO_ZONA));
			titulo.setSecao(props.get(AtributoIcpBrasil.TITULO_SECAO));
			titulo.setMunicipio(props.get(AtributoIcpBrasil.TITULO_MUNICIPIO));
			titulo.setUf(props.get(AtributoIcpBrasil.TITULO_UF));
			
			cidadao.setPisPasep(props.get(AtributoIcpBrasil.PISPASEP));
			cidadao.setCei(props.get(AtributoIcpBrasil.CEI));
			cidadao.setEmail(props.get(AtributoIcpBrasil.EMAIL));
			cidadao.setLogin(props.get(AtributoIcpBrasil.LOGIN));
		}
		cidadao.setRg(rg);
		cidadao.setTitulo(titulo);
		if (trace) {
			logger.trace(String.format(
					"Obtidas informacoes do cidadao '%s'", cidadao.getNome()));
		}
		return cidadao;
	}

	/* Retorna uma lista de objetos {@link X509Certificate}, que
	 *   representam os certificados confiaveis cadastrados.
	 * @return Lista de certificados confiaveis, como objetos
	 *   {@link X509Certificate}. 
	 */
	private X509Certificate[] listarX509CertConfiaveis() {
		List<CertificadoConfiavel> trustedCerts = listarCertConfiaveis();
		X509Certificate[] trustedX509Certs =
			new X509Certificate[(trustedCerts != null) ? trustedCerts.size() : 0];
		if (trustedCerts != null) {
			int idx = 0;
			for (CertificadoConfiavel cert : trustedCerts) {
				trustedX509Certs[idx++] = cert.getX509Certificate();
			}
		}
		return trustedX509Certs;
	}
}
