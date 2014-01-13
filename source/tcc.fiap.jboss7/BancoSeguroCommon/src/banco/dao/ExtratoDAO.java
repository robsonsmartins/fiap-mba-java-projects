package banco.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.robsonmartins.fiap.tcc.dao.GenericDAO;

import banco.bean.Conta;
import banco.bean.Extrato;

/**
 * Classe de manipulacao de objetos persistidos via JPA
 * Objetos da classe bean {@link Extrato}
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class ExtratoDAO extends GenericDAO<Extrato> {

	protected static Logger logger;
	protected static boolean trace;
	
	/**
	 * Cria uma nova instancia de DAO.
	 * @param entityManager Objeto {@link EntityManager} da API JPA.
	 */
	public ExtratoDAO(EntityManager entityManager) {
		super(entityManager);
		logger = LogManager.getLogger(ExtratoDAO.class);
		trace = logger.isTraceEnabled();
	}

	/**
	 * Retorna um Extrato.
	 * @param sequencia Numero de sequencia do Extrato.
	 * @return Objeto que representa o Extrato.
	 */
	public Extrato localizar(long sequencia) {
		if (trace) {
			logger.trace(String.format(
					"Localizar extrato: sequencia=%d",
					sequencia));
		}
		Extrato extrato = null;
		try {
			extrato = super.localizar(sequencia);
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao localizar extrato", e);
			}
		}
		if (extrato != null) {
			if (trace) {
				logger.trace(String.format(
						"Extrato encontrado: conta=%d Cliente=%s",
						extrato.getConta().getNumeroConta(), 
						extrato.getConta().getCliente().getName()));
			}
		} else {
			if (trace) {
				logger.trace("Extrato nao encontrado");
			}
		}
		return extrato;
	}
	
	/**
	 * Lista todos os Extratos.
	 * @return Lista de Extratos.
	 */
	@Override
	public List<Extrato> listar() {
		if (trace) {
			logger.trace("Listar extratos");
		}
		List<Extrato> list = null;
		try {
			list = super.listar();
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao Listar extratos", e);
			}
		}
		if (trace) {
			logger.trace(String.format("%d extratos encontrados",
					(list != null) ? list.size() : 0));
		}
		return list;
	}
	
	/**
	 * Lista Extratos por Conta.
	 * @param conta Objeto que representa a conta.
	 * @return Lista de Extratos.
	 */
	@SuppressWarnings("unchecked")
	public List<Extrato> listar(Conta conta) {
		if (trace) {
			logger.trace(String.format(
					"Localizar extratos: conta=%d",
					(conta != null) ? conta.getNumeroConta() : 0));
		}
		List<Extrato> list = null;
		Query q = null;
		try {
			q = em.createQuery(
					"from Extrato where conta = :conta");
			q.setParameter("conta"  , conta);
			list = (List<Extrato>) q.getResultList();
		} catch (Exception e) {
			if (trace) {
				logger.trace(String.format(
						"Erro ao listar extratos: %s",e.getLocalizedMessage()));
			}
		}
		if (trace) {
			logger.trace(String.format("%d extratos encontrados",
					(list != null) ? list.size() : 0));
		}
		return list;
	}

	/**
	 * Insere ou Atualiza (persiste) um Extrato. 
	 * @param extrato Extrato a ser persistido.
	 * @throws Exception 
	 */
	@Override
	public void inserir(Extrato extrato) throws Exception {
		if (trace) {
			logger.trace(String.format(
					"Inserir/Atualizar extrato: conta=%d",
					extrato.getConta().getNumeroConta()));
		}
		try {
			super.inserir(extrato);
			if (trace) {
				logger.trace("Extrato inserido/atualizado");
			}
		} catch (Exception e) {
			logger.error("Erro ao inserir/atualizar extrato", e);
			throw new Exception("Erro ao inserir/atualizar extrato", e);
		}
	}
	
	/**
	 * Exclui um Extrato do banco de dados.
	 * @param sequencia Numero de sequencia do Extrato.
	 * @throws Exception 
	 */
	public void excluir(long sequencia) throws Exception {
		if (trace) {
			logger.trace(String.format(
					"Excluir extrato: sequencia=%d",
					sequencia));
		}
		try {
			super.excluir(sequencia);
			if (trace) {
				logger.trace("Extrato excluido");
			}
		} catch (Exception e) {
			logger.error("Erro ao excluir extrato", e);
			throw new Exception("Erro ao excluir extrato", e);
		}
	}

	/**
	 * Exclui Extratos do banco de dados, por conta.
	 * @param conta Objeto que representa a conta.
	 * @throws Exception 
	 */
	public void excluirPorConta(Conta conta) throws Exception {
		if (trace) {
			logger.trace(String.format(
					"Excluir extrato: conta=%d",
					(conta != null) ? conta.getNumeroConta() : 0));
		}
		Query q = null;
		try {
			q = em.createQuery(
				"delete from Extrato where conta = :conta");
			q.setParameter("conta"  , conta);
			q.executeUpdate();
			if (trace) {
				logger.trace("Extrato excluido");
			}
		} catch (Exception e) {
			logger.error("Erro ao excluir extrato", e);
			throw new Exception("Erro ao excluir extrato", e);
		}
	}
}
