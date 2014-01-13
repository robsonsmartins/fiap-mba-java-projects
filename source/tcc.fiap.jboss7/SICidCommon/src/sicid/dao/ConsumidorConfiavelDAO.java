package sicid.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.robsonmartins.fiap.tcc.dao.GenericDAO;

import sicid.bean.ConsumidorConfiavel;

/**
 * Classe de manipulacao de objetos persistidos via JPA
 * Objetos da classe bean {@link ConsumidorConfiavel}
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class ConsumidorConfiavelDAO extends GenericDAO<ConsumidorConfiavel> {

	protected static Logger logger;
	protected static boolean trace;
	
	/**
	 * Cria uma nova instancia de DAO.
	 * @param entityManager Objeto {@link EntityManager} da API JPA.
	 */
	public ConsumidorConfiavelDAO(EntityManager entityManager) {
		super(entityManager);
		logger = LogManager.getLogger(ConsumidorConfiavelDAO.class);
		trace = logger.isTraceEnabled();
	}

	/**
	 * Retorna um Consumidor Confiavel pelo DN.
	 * @param dname Distinguished Name (DN)
	 *   do Consumidor no registro do banco de dados.
	 * @return Objeto que representa o Consumidor.
	 */
	public ConsumidorConfiavel localizar(String dname) {
		if (trace) {
			logger.trace(String.format("Localizar consumidor DN: %s", dname));
		}
		ConsumidorConfiavel cons = null;
		try {
			cons = super.localizar(dname);
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao localizar consumidor", e);
			}
		}
		if (cons != null) {
			if (trace) {
				logger.trace(String.format("Consumidor encontrado - DN: %s",
						dname));
			}
		} else {
			if (trace) {
				logger.trace("Consumidor nao encontrado");
			}
		}
		return cons;
	}
	
	/**
	 * Lista todos os Consumidores Confiaveis.
	 * @return Lista de Consumidores.
	 */
	@Override
	public List<ConsumidorConfiavel> listar() {
		if (trace) {
			logger.trace("Listar consumidores");
		}
		List<ConsumidorConfiavel> list = null;
		try {
			list = super.listar();
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao Listar consumidor", e);
			}
		}
		if (trace) {
			logger.trace(String.format("%d consumidores encontrados",
					(list != null) ? list.size() : 0));
		}
		return list;
	}
	
	/**
	 * Insere ou Atualiza (persiste) um Consumidor Confiavel. 
	 * @param cons Consumidor a ser persistido.
	 * @throws Exception 
	 */
	@Override
	public void inserir(ConsumidorConfiavel cons) throws Exception {
		if (trace) {
			logger.trace(String.format("Inserir/Atualizar consumidor: %s",
					cons.getName()));
		}
		try {
			super.inserir(cons);
			if (trace) {
				logger.trace("Consumidor inserido/atualizado");
			}
		} catch (Exception e) {
			logger.error("Erro ao inserir/atualizar consumidor", e);
			throw new Exception("Erro ao inserir/atualizar consumidor", e);
		}
	}
	
	/**
	 * Exclui um Consumidor Confiavel do banco de dados.
	 * @param dname Distinguished Name (DN)
	 *   do Consumidor Confiavel a ser removido.
	 * @throws Exception 
	 */
	public void excluir(String dname) throws Exception {
		if (trace) {
			logger.trace(String.format("Excluir consumidor DN: %s", dname));
		}
		try {
			super.excluir(dname);
			if (trace) {
				logger.trace("Consumidor excluido");
			}
		} catch (Exception e) {
			logger.error("Erro ao excluir consumidor", e);
			throw new Exception("Erro ao excluir consumidor", e);
		}
	}
	
}
