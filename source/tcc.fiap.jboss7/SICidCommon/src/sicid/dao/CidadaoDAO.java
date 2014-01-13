package sicid.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.robsonmartins.fiap.tcc.dao.GenericDAO;

import sicid.bean.Cidadao;

/**
 * Classe de manipulacao de objetos persistidos via JPA
 * Objetos da classe bean {@link Cidadao}
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class CidadaoDAO extends GenericDAO<Cidadao> {

	protected static Logger logger;
	protected static boolean trace;
	
	/**
	 * Cria uma nova instancia de DAO.
	 * @param entityManager Objeto {@link EntityManager} da API JPA.
	 */
	public CidadaoDAO(EntityManager entityManager) {
		super(entityManager);
		logger = LogManager.getLogger(CidadaoDAO.class);
		trace = logger.isTraceEnabled();
	}

	/**
	 * Retorna um Cidadao pelo DN.
	 * @param dname Distinguished Name (DN)
	 *   do Cidadao no registro do banco de dados.
	 * @return Objeto que representa o Cidadao.
	 */
	public Cidadao localizar(String dname) {
		if (trace) {
			logger.trace(String.format("Localizar cidadao DN: %s", dname));
		}
		Cidadao cidadao = null;
		try {
			cidadao = super.localizar(dname);
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao localizar cidadao", e);
			}
		}
		if (cidadao != null) {
			if (trace) {
				logger.trace(String.format("Cidadao encontrado - DN: %s",
						dname));
			}
		} else {
			if (trace) {
				logger.trace("Cidadao nao encontrado");
			}
		}
		return cidadao;
	}
	
	/**
	 * Lista todos os Cidadaos.
	 * @return Lista de Cidadaos.
	 */
	@Override
	public List<Cidadao> listar() {
		if (trace) {
			logger.trace("Listar cidadaos");
		}
		List<Cidadao> list = null;
		try {
			list = super.listar();
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao Listar cidadao", e);
			}
		}
		if (trace) {
			logger.trace(String.format("%d cidadaos encontrados",
					(list != null) ? list.size() : 0));
		}
		return list;
	}
	
	/**
	 * Insere ou Atualiza (persiste) um Cidadao. 
	 * @param cidadao Cidadao a ser persistido.
	 * @throws Exception 
	 */
	@Override
	public void inserir(Cidadao cidadao) throws Exception {
		if (trace) {
			logger.trace(String.format("Inserir/Atualizar cidadao: %s",
					cidadao.getNome()));
		}
		try {
			super.inserir(cidadao);
			if (trace) {
				logger.trace("Cidadao inserido/atualizado");
			}
		} catch (Exception e) {
			logger.error("Erro ao inserir/atualizar cidadao", e);
			throw new Exception("Erro ao inserir/atualizar cidad\u00E3o", e);
		}
	}
	
	/**
	 * Exclui um Cidadao do banco de dados.
	 * @param dname Distinguished Name (DN)
	 *   do Cidadao a ser removido.
	 * @throws Exception 
	 */
	public void excluir(String dname) throws Exception {
		if (trace) {
			logger.trace(String.format("Excluir cidadao DN: %s", dname));
		}
		try {
			super.excluir(dname);
			if (trace) {
				logger.trace("Cidadao excluido");
			}
		} catch (Exception e) {
			logger.error("Erro ao excluir cidadao", e);
			throw new Exception("Erro ao excluir cidad\u00E3o", e);
		}
	}
}
