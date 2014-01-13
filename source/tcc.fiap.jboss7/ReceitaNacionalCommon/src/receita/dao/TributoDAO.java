package receita.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import receita.bean.Tributo;

import com.robsonmartins.fiap.tcc.dao.GenericDAO;

/**
 * Classe de manipulacao de objetos persistidos via JPA
 * Objetos da classe bean {@link Tributo}
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class TributoDAO extends GenericDAO<Tributo> {

	protected static Logger logger;
	protected static boolean trace;
	
	/**
	 * Cria uma nova instancia de DAO.
	 * @param entityManager Objeto {@link EntityManager} da API JPA.
	 */
	public TributoDAO(EntityManager entityManager) {
		super(entityManager);
		logger = LogManager.getLogger(TributoDAO.class);
		trace = logger.isTraceEnabled();
	}

	/**
	 * Retorna o tributo de um cidadao, pelo RIC.
	 * @param ric RIC do cidadao.
	 * @return Objeto que representa o tributo.
	 */
	public Tributo localizarPorRic(String ric) {
		if (ric == null) { return null; }
		if (trace) {
			logger.trace(String.format(
					"Localizar tributo ric='%s'", ric));
		}
		Tributo situacao = null;
		Query q = null;
		try {
			q = em.createQuery("from Tributo where ric like :ric");
			q.setParameter("ric", ric);
			situacao = (Tributo) q.getSingleResult();
		} catch (Exception e) {
			if (trace) {
				logger.trace(String.format(
						"Localizar tributo: %s",e.getLocalizedMessage()));
			}
		}
		return situacao;		
	}
	
	/**
	 * Lista os tributos de todos cidadaos.
	 * @return Lista de tributos.
	 */
	@Override
	public List<Tributo> listar() {
		if (trace) {
			logger.trace("Listar tributos");
		}
		List<Tributo> list = null;
		try {
			list = super.listar();
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao Listar tributos", e);
			}
		}
		if (trace) {
			logger.trace(String.format("%d tributos encontrados",
					(list != null) ? list.size() : 0));
		}
		return list;
	}

	/**
	 * Insere ou Atualiza (persiste) um tributo. 
	 * @param tributo Tributo a ser persistido.
	 * @throws Exception 
	 */
	@Override
	public void inserir(Tributo tributo) throws Exception {
		if (trace) {
			logger.trace(String.format("Inserir/Atualizar tributo: RIC=%s",
					(tributo != null && tributo.getCidadao() != null) ? tributo.getCidadao().getRic() : null));
		}
		try {
			super.inserir(tributo);
			if (trace) {
				logger.trace("Tributo inserido/atualizado");
			}
		} catch (Exception e) {
			logger.error("Erro ao inserir/atualizar tributo", e);
			throw new Exception(
					"Erro ao inserir/atualizar tributo", e);
		}
	}
	
	/**
	 * Exclui do banco de dados o tributo de um cidadao.
	 * @param ric RIC do cidadao.
	 * @throws Exception 
	 */
	public void excluir(String ric) throws Exception {
		if (trace) {
			logger.trace(String.format("Excluir tributo: RIC=%s", ric));
		}
		try {
			super.excluir(ric);
			if (trace) {
				logger.trace("Tributo excluido");
			}
		} catch (Exception e) {
			logger.error("Erro ao excluir tributo", e);
			throw new Exception("Erro ao excluir tributo", e);
		}
	}
}
