package icp.dao;

import icp.bean.Certificado;
import icp.bean.StatusCertificado;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.robsonmartins.fiap.tcc.dao.GenericDAO;

/**
 * Classe de manipulacao de objetos persistidos via JPA
 * Objetos da classe bean {@link Certificado}
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class CertificadoDAO extends GenericDAO<Certificado> {

	/* objeto para realizar log das operacoes */
	protected static Logger logger;
	protected static boolean trace;
	
	/**
	 * Cria uma nova instancia de DAO.
	 * @param entityManager Objeto {@link EntityManager} da API JPA.
	 */
	public CertificadoDAO(EntityManager entityManager) {
		super(entityManager);
		logger = LogManager.getLogger(CertificadoDAO.class);
		trace = logger.isTraceEnabled();
	}

	/**
	 * Retorna um Certificado, a partir de uma lista, pelo ID.
	 * @param certList Lista com objetos Certificado.
	 * @param id ID do Certificado.
	 * @return Objeto do Certificado, ou null se nao encontrado.
	 */
	public static Certificado getCertfromListById(List<Certificado> certList, long id) {
		for (Certificado cert : certList) {
			if (cert.getId() == id) { return cert; }
		}
		return null;
	}
	
	/**
	 * Retorna um Certificado pelo ID.
	 * @param id Id do Certificado no registro do banco de dados.
	 * @return Objeto que representa o Certificado.
	 */
	public Certificado localizar(long id) {
		if (trace) {
			logger.trace(String.format("Localizar certificado id: %d", id));
		}
		Certificado cert = null;
		try {
			cert = super.localizar(id);
		} catch (Exception e) {
			if (trace) {
				logger.trace(String.format("Localizar certificado: %s",e.getLocalizedMessage()));
			}
		}
		if (trace) {
			if (cert != null) {
				logger.trace(String.format("Certificado encontrado - CN: %s", cert.getCommonName()));
			} else {
				logger.trace("Certificado nao encontrado");
			}					
		}
		return cert;
	}
	
	/**
	 * Lista todos os Certificados.
	 * @return Lista de Certificados.
	 */
	@Override
	public List<Certificado> listar() {
		if (trace) {
			logger.trace("Listar certificados");
		}
		List<Certificado> list = null;
		try {
			list = super.listar();
		} catch (Exception e) {
			if (trace) {
				logger.trace(String.format("Listar certificado: %s",e.getLocalizedMessage()));
			}
		}
		if (trace) {
			logger.trace(String.format("%d certificados encontrados", (list != null) ? list.size() : 0));
		}
		return list;
	}
	
	/**
	 * Insere ou Atualiza (persiste) um Certificado. 
	 * @param cert Certificado a ser persistido.
	 */
	@Override
	public void inserir(Certificado cert) {
		if (trace) {
			logger.trace(String.format("Inserir/Atualizar certificado CN: %s",cert.getCommonName()));
		}
		try {
			super.inserir(cert);
			if (trace) {
				logger.trace("Certificado inserido/atualizado");
			}
		} catch (Exception e) {
			logger.error(String.format("Erro ao inserir/atualizar certificado: %s",e.getLocalizedMessage()));
		}
	}
	
	/**
	 * Exclui um Certificado do banco de dados.
	 * @param id ID do Certificado a ser removido.
	 */
	public void excluir(long id) {
		if (trace) {
			logger.trace(String.format("Excluir certificado id: %d", id));
		}
		try {
			super.excluir(id);
			if (trace) {
				logger.trace("Certificado excluido");
			}
		} catch (Exception e) {
			logger.error(String.format("Erro ao excluir certificado: %s",e.getLocalizedMessage()));
		}
	}
	
	/**
	 * Retorna uma lista de certificados emitidos por uma AC (por nome).
	 * @param nomeAC Nome da Autoridade Certificadora (AC).
	 *   Se nulo, retorna uma lista com os certificados emitidos por todas AC's.
	 * @return Lista de certificados emitidos pela AC.
	 */
	@SuppressWarnings("unchecked")
	public List<Certificado> listarPorNomeAC(String nomeAC) {
		if (nomeAC == null) { return listar(); }
		if (trace) {
			logger.trace(String.format("Listar certificados da AC '%s'", nomeAC));
		}
		List<Certificado> list = null;
		Query q = null;
		try {
			q = em.createQuery("from Certificado where nomeAC like :nomeAC");
			q.setParameter("nomeAC", nomeAC);
			list = (List<Certificado>) q.getResultList();
		} catch (Exception e) {
			if (trace) {
				logger.trace(String.format("Listar certificados: %s",e.getLocalizedMessage()));
			}
		}
		if (trace) {
			logger.trace(String.format("%d certificados encontrados", (list != null) ? list.size() : 0));
		}
		return list;		
	}
	
	/**
	 * Exclui todos certificados emitidos por uma AC (por nome).
	 * @param nomeAC Nome da Autoridade Certificadora (AC).
	 *   Se nulo, nao exclui nenhum certificado.
	 * @return Quantidade de certificados excluidos.
	 */
	public synchronized int excluirPorNomeAC(String nomeAC) {
		if (nomeAC == null) { return 0; }
		if (trace) {
			logger.trace(String.format("Excluir certificados da AC '%s'", nomeAC));
		}
		int count = 0;
		try {
			Query q = em.createQuery("delete from Certificado where nomeAC like :nomeAC"); 
			q.setParameter("nomeAC", nomeAC);
			count = q.executeUpdate();			
		} catch (Exception e) {
			logger.error(String.format("Erro ao excluir certificados: %s",e.getLocalizedMessage()));
		}
		if (trace) {
			logger.trace(String.format("%d certificados excluidos", count));
		}
		return count;
	}
	
	/**
	 * Atualiza o status de todos os certificados expirados.<br>
	 * Compara a data de expiracao com a data atual e atualiza o registro no banco de dados.
	 * @return Quantidade de registros atualizados.
	 */
	public synchronized int atualizarStatusCertExpirados() {
		if (trace) {
			logger.trace("Atualizar status dos certificados expirados");
		}
		int count = 0;
		try {
			Query q = em.createQuery("update Certificado set status = :status where expiracao <= :hoje"); 
			q.setParameter("status", StatusCertificado.EXPIRADO);
			q.setParameter("hoje", new Date());
			count = q.executeUpdate();			
		} catch (Exception e) {
			logger.error(String.format("Erro ao atualizar status: %s",e.getLocalizedMessage()));
		}
		if (trace) {
			logger.trace(String.format("%d registros atualizados", count));
		}
		return count;
	}
}
