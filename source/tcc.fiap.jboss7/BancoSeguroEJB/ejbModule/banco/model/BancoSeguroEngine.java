package banco.model;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import sicid.bean.CertificadoStatus;
import sicid.bean.Cidadao;
import sicid.util.CertificadoIcpBrasilParser;
import sicid.ws.SICidClient;

import sun.misc.BASE64Encoder;

import banco.bean.ConsumidorConfiavel;
import banco.bean.Conta;
import banco.bean.Extrato;
import banco.bean.Operacao;
import banco.bean.Usuario;
import banco.dao.ConsumidorConfiavelDAO;
import banco.dao.ContaDAO;
import banco.dao.ExtratoDAO;
import banco.dao.UsuarioDAO;

import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;

/**
 * Motor (Model) do Banco Seguro.<br/>
 * Implementa as funcionalidades da aplicacao.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@PermitAll
@Stateless
public class BancoSeguroEngine implements IBancoSeguroEngine {

	/* nome do arquivo de properties com a configuracao de conexao ao cliente sicid */ 
	private static final String BANCOSEGURO_SICID_CLIENT_PROPS_FILE = "bancoseguro.properties";
	/* nome da unidade de persistencia configurada em persistence.xml */
	private static final String PERSISTENCE_UNIT_NAME = "bancoseguro";
	
	/* DAO de usuarios */
	private UsuarioDAO usuarioDAO;
	/* DAO de contas */
	private ContaDAO contaDAO;
	/* DAO do extrato */
	private ExtratoDAO extratoDAO;
	/* DAO de consumidores confiaveis */
	private ConsumidorConfiavelDAO consConfiavelDAO;
	
	/* EntityManager para JPA. */
	@PersistenceContext(unitName=PERSISTENCE_UNIT_NAME)
	private EntityManager entityManager;

	/* Cliente SICid */
	private SICidClient sicidClient;

	/* para fazer log */
	private static Logger logger;
	private static boolean trace;
	
	/**
	 * Cria uma nova instancia do motor do Banco Seguro.
	 */
	public BancoSeguroEngine() {
		logger = LogManager.getLogger(BancoSeguroEngine.class);
		trace = logger.isTraceEnabled();
	}

	/* inicializa DAO's */
	@PostConstruct
	protected void init() {
		usuarioDAO = new UsuarioDAO(entityManager);
		consConfiavelDAO = new ConsumidorConfiavelDAO(entityManager);
		contaDAO = new ContaDAO(entityManager);
		extratoDAO = new ExtratoDAO(entityManager);
		sicidClient = new SICidClient();
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
	public Usuario localizarUsuarioPorCpf(String cpf) {
		return usuarioDAO.localizarPorCpf(cpf);
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
	
			String name = null;
			String cpf  = null;
			try {
				String content = CertificadoSerializador.certToStr(x509cert);
				sicidClient.connect(BANCOSEGURO_SICID_CLIENT_PROPS_FILE);
				Cidadao cidadao = sicidClient.getService().consultarCidadao(content);
				if (cidadao != null) { 
					name = cidadao.getNome();
					cpf  = cidadao.getCpf();
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
			usuario.setName(name);
			usuario.setCpf(cpf);
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
	public Usuario adicionarGerente(InputStream istream) throws Exception {
		return adicionarUsuario(istream, BANCOSEGURO_GERENTE_ACCESS_ROLE);
	}

	@Override
	public Usuario adicionarCliente(InputStream istream) throws Exception {
		return adicionarUsuario(istream, BANCOSEGURO_CLIENTE_ACCESS_ROLE);
	}
	
	@Override
	public void removerUsuario(String dname) throws Exception {

		try {
			if (trace) {
				logger.trace("Removendo usuario");
			}
			
			Usuario cliente = usuarioDAO.localizar(dname);
			String cpf = (cliente != null) ? cliente.getCpf() : null; 
			Conta conta = contaDAO.localizarPorUsuario(cpf);
			long numeroConta = (conta != null) ? conta.getNumeroConta() : 0;

			extratoDAO.excluirPorConta(conta);
			contaDAO.excluir(numeroConta);
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
			trustedApp.setRole(BANCOSEGURO_SERVICE_ACCESS_ROLE);
			
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
	public List<Conta> listarContas() {
		return contaDAO.listar();
	}

	@Override
	public Conta localizarConta(long numeroConta) {
		return contaDAO.localizar(numeroConta);
	}

	@Override
	public Conta localizarContaPorCpf(String cpfCliente) {
		return contaDAO.localizarPorUsuario(cpfCliente);
	}

	@Override
	public Conta adicionarConta(InputStream istream) throws Exception {
		Conta conta = null;
		try {
			if (trace) {
				logger.trace("Adicionando conta");
			}
			X509Certificate x509cert =
				CertificadoSerializador.loadCertFromStream(istream);
			String dname = x509cert.getSubjectX500Principal().getName();

			Usuario cliente = localizarUsuario(dname);
			if (cliente == null) {
				istream.reset();
				cliente = adicionarCliente(istream);
			}
			
			conta = new Conta();
			conta.setNumeroConta(0);
			conta.setSaldo(0.0f);
			conta.setCliente(cliente);

			contaDAO.inserir(conta);

			if (trace) {
				logger.trace("Conta adicionada com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao adicionar conta", e);
			}
			throw e;
		}
		return conta;
	}

	@Override
	public void removerConta(long numeroConta) throws Exception {
		Conta conta = localizarConta(numeroConta);
		Usuario cliente = null;
		if (conta != null) {
			cliente = conta.getCliente();
		}
		try {
			if (trace) {
				logger.trace("Removendo conta");
			}

			extratoDAO.excluirPorConta(conta);
			contaDAO.excluir(numeroConta);
			
			if (cliente != null &&
					!BANCOSEGURO_GERENTE_ACCESS_ROLE.equals(cliente.getRole())) {
				removerUsuario(cliente.getDname());
			}
			if (trace) {
				logger.trace("Conta removida com sucesso");
			}
			
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao remover conta", e);
			}
			throw e;
		}
	}

	@Override
	public Conta depositarConta(Conta conta, float valor) throws Exception {
		if (trace) {
			logger.trace("Deposito em conta");
		}
		
		try {
			conta = movimentarConta(conta, null, Operacao.DEPOSITO, valor);
			
			if (trace) {
				logger.trace(String.format(
						"Deposito em conta: numero=%d valor=$%7.2f saldoFinal=$%7.2f",
						conta.getNumeroConta(), valor, conta.getSaldo()));
			}
				
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao realizar deposito em conta", e);
			}
			throw e;
		}
		return conta;
	}

	@Override
	public Conta depositarConta(String content, float valor) throws Exception {
		Conta conta = null; 
		if (trace) {
			logger.trace("Localizar conta a partir de certificado digital");
		}
		try {
			conta = getContaFromCert(content);
			if (trace) {
				if (conta != null) {
					logger.trace(String.format(
							"Conta localizada: numero=%d",conta.getNumeroConta()));
				}
			}
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao localizar conta", e);
			}
			throw e;
		}
		return depositarConta(conta, valor);
	}
	
	@Override
	public Conta sacarConta(Conta conta, float valor) throws Exception {
		if (trace) {
			logger.trace("Saque em conta");
		}
		
		try {
			conta = movimentarConta(conta, null, Operacao.SAQUE, valor);

			if (trace) {
				logger.trace(String.format(
						"Saque em conta: numero=%d valor=$%7.2f saldoFinal=$%7.2f",
						conta.getNumeroConta(), valor, conta.getSaldo()));
			}
				
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao realizar saque em conta", e);
			}
			throw e;
		}
		return conta;
	}
	
	@Override
	public Conta sacarConta(String content, float valor) throws Exception {
		Conta conta = null; 
		if (trace) {
			logger.trace("Localizar conta a partir de certificado digital");
		}
		try {
			conta = getContaFromCert(content);
			if (trace) {
				if (conta != null) {
					logger.trace(String.format(
							"Conta localizada: numero=%d",conta.getNumeroConta()));
				}
			}
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao localizar conta", e);
			}
			throw e;
		}
		return sacarConta(conta, valor);
	}

	@Override
	public Conta pagarConta(Conta conta, float valor) throws Exception {
		if (trace) {
			logger.trace("Pagamento de titulo");
		}
		
		try {
			conta = movimentarConta(conta, null, Operacao.PAGAMENTO, valor);

			if (trace) {
				logger.trace(String.format(
						"Pagamento / conta: numero=%d valor=$%7.2f saldoFinal=$%7.2f",
						conta.getNumeroConta(), valor, conta.getSaldo()));
			}
				
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao realizar pagamento", e);
			}
			throw e;
		}
		return conta;
	}

	@Override
	public Conta pagarConta(String content, float valor) throws Exception {
		Conta conta = null; 
		if (trace) {
			logger.trace("Localizar conta a partir de certificado digital");
		}
		try {
			conta = getContaFromCert(content);
			if (trace) {
				logger.trace(String.format(
						"Conta localizada: numero=%d",conta.getNumeroConta()));
			}
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao localizar conta", e);
			}
			throw e;
		}
		return pagarConta(conta, valor);
	}

	@Override
	public Conta transferirConta(Conta contaOrigem,
			Conta contaDestino, float valor) throws Exception {
		
		if (trace) {
			logger.trace("Transferencia entre contas");
		}
		
		try {
			contaOrigem = movimentarConta(contaOrigem, contaDestino, Operacao.TRANSFERENCIA, valor);

			if (trace) {
				logger.trace(String.format(
						"Transferencia de conta: numero=%d para conta: numero=%d / valor=$%7.2f saldoFinal=$%7.2f",
						contaOrigem.getNumeroConta(), contaDestino.getNumeroConta(), valor, contaOrigem.getSaldo()));
			}
				
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao realizar transferencia", e);
			}
			throw e;
		}
		return contaOrigem;
	}
	
	@Override
	public Conta transferirConta(String content, long numeroContaDestino,
			float valor) throws Exception {
		
		Conta contaOrigem = null; 
		Conta contaDestino = null; 
		if (trace) {
			logger.trace("Localizar conta a partir de certificado digital");
		}
		try {
			contaOrigem = getContaFromCert(content);
			contaDestino = localizarConta(numeroContaDestino);
			if (trace) {
				if (contaOrigem != null) {
					logger.trace(String.format(
							"Conta origem localizada: numero=%d",contaOrigem.getNumeroConta()));
				}
				if (contaDestino != null) {
					logger.trace(String.format(
							"Conta destino localizada: numero=%d",contaDestino.getNumeroConta()));
				}
			}
		} catch (Exception e) {
			if (trace) {
				logger.error("Erro ao localizar conta", e);
			}
			throw e;
		}
		
		return transferirConta(contaOrigem, contaDestino, valor);
	}
	
	@Override
	public List<Extrato> obterExtrato(Conta conta) {
		return extratoDAO.listar(conta);
	}
	
	/* Obtem uma conta associada a um cliente, pelo seu certificado.
	 * @param content Conteudo do certificado do cliente, codificado em Base64.
	 * @return Conta associada, ou null se cliente nao possui conta.
	 * @throws Exception
	 */
	private Conta getContaFromCert(String content) throws Exception {
		CertificadoStatus certStatus = null;
		Cidadao cidadao = null;
		Conta conta = null;

		try {
			sicidClient.connect(BANCOSEGURO_SICID_CLIENT_PROPS_FILE);
			certStatus = sicidClient.getService().validarCertificado(content);
			cidadao = sicidClient.getService().consultarCidadao(content);
			
		} catch (Exception e) {
			Exception ex = new Exception(
					"Erro ao conectar com o Servi\u00E7o de Identifica\u00E7\u00E3o do Cidad\u00E3o.");
			ex.initCause(e);
			throw ex;
		}
		
		if (certStatus != CertificadoStatus.VALID) {
			throw new Exception("Certificado Inv\u00E1lido.");
		}
		
		if (cidadao == null) {
			throw new Exception("Cidad\u00E3o n\u00E3o encontrado no cadastro.");
		}
		
		conta = localizarContaPorCpf(cidadao.getCpf());
		if (conta == null) {
			throw new Exception("Conta n\u00E3o encontrada.");
		}
		return conta;
	}

	/* Realiza a movimentacao de uma conta.
	 * @param contaOrigem Conta a ser movimentada.
	 * @param contaDestino Conta de destino, caso a operacao seja 
	 *   {@link Operacao.TRANSFERENCIA}, null caso contrario.
	 * @param operacao Operacao a ser realizada.
	 * @param valor Valor da operacao (numero positivo). 
	 * @return Conta de origem com o saldo atualizado. 
	 * @throws Exception
	 */
	private Conta movimentarConta(Conta contaOrigem, Conta contaDestino, 
			Operacao operacao, float valor) throws Exception {
		
		if (contaOrigem == null) {
			throw new NullPointerException("Conta inv\u00E1lida.");
		}
		if (valor <= 0.0f) {
			throw new Exception(String.format(
				"Valor inv\u00E1lido ($%7.2f).", valor));
		}
		if ((operacao == Operacao.PAGAMENTO || operacao == Operacao.SAQUE ||
			 operacao == Operacao.TRANSFERENCIA) &&
				contaOrigem.getSaldo() < valor) {
			
			throw new Exception(String.format(
				"Saldo insuficiente ($%7.2f).", contaOrigem.getSaldo()));
		}
		if (operacao == Operacao.TRANSFERENCIA) {
			if (contaDestino == null || 
					contaDestino.getNumeroConta() == contaOrigem.getNumeroConta()) {
				throw new Exception("Conta de destino inv\u00E1lida.");
			}
		}

		Extrato extrato = new Extrato();
		extrato.setSequencia(0);
		extrato.setConta(contaOrigem);
		extrato.setData(new Date());
		extrato.setValor(valor);
		
		Extrato extratoDest = new Extrato();
		extratoDest.setSequencia(0);
		extratoDest.setConta(contaDestino);
		extratoDest.setData(new Date());
		extratoDest.setValor(valor);

		switch (operacao) {
			case DEPOSITO:
				contaOrigem.setSaldo(contaOrigem.getSaldo() + valor);
				extrato.setOperacao(Operacao.DEPOSITO);
				break;
			case SAQUE:
				contaOrigem.setSaldo(contaOrigem.getSaldo() - valor);
				extrato.setOperacao(Operacao.SAQUE);
				break;
			case PAGAMENTO:
				contaOrigem.setSaldo(contaOrigem.getSaldo() - valor);
				extrato.setOperacao(Operacao.PAGAMENTO);
				break;
			case TRANSFERENCIA:
				contaOrigem.setSaldo(contaOrigem.getSaldo() - valor);
				contaDestino.setSaldo(contaDestino.getSaldo() + valor);
				extrato.setOperacao(Operacao.TRANSFERENCIA);
				extratoDest.setOperacao(Operacao.DEPOSITO_TRANSF);
				extratoDest.setSaldoFinal(contaDestino.getSaldo());
				break;
			default:
				throw new Exception("Opera\u00E7\u00E3o inv\u00E1lida.");
		}
		
		extrato.setSaldoFinal(contaOrigem.getSaldo());
		contaDAO.inserir(contaOrigem);
		extratoDAO.inserir(extrato);

		if (operacao == Operacao.TRANSFERENCIA) {
			contaDAO.inserir(contaDestino);
			extratoDAO.inserir(extratoDest);
		}
		
		return contaOrigem;
	}
}
