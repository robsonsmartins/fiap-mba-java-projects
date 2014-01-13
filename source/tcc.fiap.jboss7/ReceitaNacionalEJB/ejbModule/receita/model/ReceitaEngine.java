package receita.model;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import receita.bean.Cidadao;
import receita.bean.Tributo;
import receita.dao.CidadaoDAO;
import receita.dao.TributoDAO;
import sicid.bean.CidadaoCollecion;
import sicid.ws.SICidClient;
import banco.bean.StatusOperacao;
import banco.ws.BancoSeguroClient;

import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;

/**
 * Motor (Model) da Receita Nacional.<br/>
 * Implementa as funcionalidades da aplicacao.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@PermitAll
@Stateless
public class ReceitaEngine implements IReceitaEngine {

	/* nome do arquivo de properties com a configuracao de conexao do cliente sicid */ 
	private static final String RECEITA_SICID_CLIENT_PROPS_FILE = "receita.properties";
	/* nome do arquivo de properties com a configuracao de conexao do cliente bancoseguro */ 
	private static final String RECEITA_BANCOSEGURO_CLIENT_PROPS_FILE = "receita.properties";
	
	/* nome da unidade de persistencia configurada em persistence.xml */
	private static final String PERSISTENCE_UNIT_NAME = "receita";
	
	/* DAO de cidadaos */
	private CidadaoDAO cidadaoDAO;
	/* DAO de tributos */
	private TributoDAO tributoDAO;
	/* Cliente SICid */
	private SICidClient sicidClient;
	/* Cliente BancoSeguro */
	private BancoSeguroClient bancoClient;
	
	/* EntityManager para JPA. */
	@PersistenceContext(unitName=PERSISTENCE_UNIT_NAME)
	private EntityManager entityManager;

	/* geracao de numeros aleatorios */
	private Random randomizer;
	
	/* para fazer log */
	private static Logger logger;
	private static boolean trace;
	
	/**
	 * Cria uma nova instancia do motor da Receita Nacional.
	 */
	public ReceitaEngine() {
		logger = LogManager.getLogger(ReceitaEngine.class);
		trace = logger.isTraceEnabled();
		randomizer = new Random();
	}
	
	/* inicializa DAO's */
	@PostConstruct
	protected void init() {
		cidadaoDAO = new CidadaoDAO(entityManager);
		tributoDAO = new TributoDAO(entityManager);
		sicidClient = new SICidClient();
		bancoClient = new BancoSeguroClient();
	}
	
	@Override
	public List<Cidadao> listarUsuarios() {
		return cidadaoDAO.listar();
	}

	@Override
	public List<Cidadao> listarUsuariosPorRole(String role) {
		return cidadaoDAO.listarPorRole(role);
	}

	@Override
	public Cidadao localizarUsuario(String dname) {
		return cidadaoDAO.localizar(dname);
	}

	@Override
	public Cidadao localizarUsuarioPorRic(String ric) {
		return cidadaoDAO.localizarPorRic(ric);
	}

	@Override
	public Cidadao adicionarUsuario(InputStream istream, 
			String role) throws Exception {

		Cidadao cidadao = null;
		try {
			if (trace) {
				logger.trace("Adicionando usuario");
			}

			X509Certificate x509cert =
				CertificadoSerializador.loadCertFromStream(istream);
		
			String dname = x509cert.getSubjectX500Principal().getName();
			cidadao = new Cidadao();
			cidadao.setDname(dname);
	
			String name = null;
			String ric  = null;
			try {
				String content = CertificadoSerializador.certToStr(x509cert);
				sicidClient.connect(RECEITA_SICID_CLIENT_PROPS_FILE);
				sicid.bean.Cidadao sicidCidadao = 
					sicidClient.getService().consultarCidadao(content);
				if (sicidCidadao != null) { 
					name = sicidCidadao.getNome();
					ric  = sicidCidadao.getRic();
				}
				if (name == null) {
					throw new Exception("Cidad\u00E3o n\u00E3o encontrado.");
				}
				
			} catch (Exception e) {
				
				if (trace) {
					logger.error("Erro ao obter dados do cidadao", e);
				}
				throw e;
			}
			cidadao.setName(name);
			cidadao.setRic(ric);
			cidadao.setRole(role);
			
			cidadaoDAO.inserir(cidadao);

			if (trace) {
				logger.trace("Usuario adicionado com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao adicionar usuario", e);
			}
			throw e;
		}
		return cidadao;
	}

	@Override
	public Cidadao adicionarAdmin(InputStream istream) throws Exception {
		return adicionarUsuario(istream, RECEITA_GOVERNO_ACCESS_ROLE);
	}

	@Override
	public Cidadao adicionarCidadao(InputStream istream) throws Exception {
		return adicionarUsuario(istream, RECEITA_CIDADAO_ACCESS_ROLE);
	}
	
	@Override
	public void removerUsuario(String dname) throws Exception {

		try {
			if (trace) {
				logger.trace("Removendo usuario");
			}

			cidadaoDAO.excluir(dname);
			
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
	public List<Tributo> listarTributos() {
		List<Tributo> list = tributoDAO.listar();
		if (list != null) {
			for (Tributo tributo : list) {
				if (tributo.getCidadao() == null) {
					Cidadao cidadao = 
						cidadaoDAO.localizarPorRic(tributo.getRic());
					tributo.setCidadao(cidadao);
				}
			}
		}
		return list;
	}
	
	@Override
	public Tributo localizarTributoPorCidadao(Cidadao cidadao) {
		Tributo tributo = tributoDAO.localizarPorRic(cidadao.getRic());
		if (tributo != null) { 
			if (tributo.getCidadao() == null) {
				tributo.setCidadao(cidadao);
			}
		}
		return tributo;
	}

	@Override
	public void calcularTributos() throws Exception {

		CidadaoCollecion cidadaos = null;
		/* consulta servico SICid, buscando lista de cidadaos */
		try {
			sicidClient.connect(RECEITA_SICID_CLIENT_PROPS_FILE);
			cidadaos = sicidClient.getService().listarCidadaos();
			if (cidadaos == null) {
				throw new Exception("Nenhum Cidad\u00E3o encontrado.");
			}
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao obter cadastro de cidadaos", e);
			}
			throw e;
		}
		
		/* remove todos tributos existentes */
		List<Tributo> listaTributos = tributoDAO.listar();
		for (Tributo tributo : listaTributos) {
			tributoDAO.excluir(tributo.getRic());
		}
		
		/* calcula valores aleatorios de tributos para cada cidadao */
		for (sicid.bean.Cidadao sicidCidadao : cidadaos.getCidadaos()) {
			Cidadao cidadao = 
				cidadaoDAO.localizarPorRic(sicidCidadao.getRic());
			
			if (cidadao == null) {
				cidadao = new Cidadao();
				cidadao.setRic(sicidCidadao.getRic());
				cidadao.setDname(sicidCidadao.getDname());
				cidadao.setName(sicidCidadao.getNome());
				cidadao.setRole(RECEITA_CIDADAO_ACCESS_ROLE);
				cidadaoDAO.inserir(cidadao);
			}

			Tributo tributo = 
				tributoDAO.localizarPorRic(sicidCidadao.getRic());
			if (tributo == null) {
				tributo = new Tributo();
			}
			
			/* calcula imposto aleatoriamente */
			/* imposto devido sera' sempre < 1000,00, com 20% de chance de isencao */ 
			float impostoDevido = (randomizer.nextFloat() - 0.2f) * 1000.0f;
			if (impostoDevido < 0.0f) { impostoDevido = 0.0f; } /* isento */
			boolean impostoPago = (impostoDevido == 0.0f) ? true : false;
			
			tributo.setImpostoDevido(impostoDevido);
			tributo.setImpostoPago(impostoPago);
			tributo.setCidadao(cidadao);
			
			tributoDAO.inserir(tributo);
		}
	}
	
	@Override
	public void pagarTributoOnline(Tributo tributo, String certContent) throws Exception {
		
		StatusOperacao status = null;
		try {
			bancoClient.connect(RECEITA_BANCOSEGURO_CLIENT_PROPS_FILE);
			status = bancoClient.getService().efetuarPagamento(certContent, tributo.getImpostoDevido());
			if (status == null || status.getValue() == null || !status.getValue().booleanValue()) {
				throw new Exception(String.format(
						"Erro durante transa\u00E7\u00E3o com institui\u00E7\u00E3o banc\u00E1ria. Motivo: %s",
						(status != null && status.getMotivo() != null)
							? status.getMotivo() : "Erro de comunica\u00E7\u00E3o."));
			}
			tributo.setImpostoPago(true);
			tributoDAO.inserir(tributo);
		
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao pagar tributo online", e);
			}
			throw e;
		}
	}
	
}
