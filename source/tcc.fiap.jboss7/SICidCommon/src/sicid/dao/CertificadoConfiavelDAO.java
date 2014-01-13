package sicid.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.robsonmartins.fiap.tcc.dao.GenericDAO;

import sicid.bean.CertificadoConfiavel;

/**
 * Classe de manipulacao de objetos persistidos via JPA
 * Objetos da classe bean {@link CertificadoConfiavel}
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class CertificadoConfiavelDAO extends GenericDAO<CertificadoConfiavel> {

	protected static Logger logger;
	protected static boolean trace;
	
	/**
	 * Cria uma nova instancia de DAO.
	 * @param entityManager Objeto {@link EntityManager} da API JPA.
	 */
	public CertificadoConfiavelDAO(EntityManager entityManager) {
		super(entityManager);
		logger = LogManager.getLogger(CertificadoConfiavelDAO.class);
		trace = logger.isTraceEnabled();
	}

	/**
	 * Retorna um Certificado Confiavel pelo ID.
	 * @param id Id do Certificado no registro do banco de dados.
	 * @return Objeto que representa o Certificado.
	 */
	public CertificadoConfiavel localizar(long id) {
		if (trace) {
			logger.trace(String.format("Localizar certificado id: %d", id));
		}
		CertificadoConfiavel cert = null;
		try {
			cert = super.localizar(id);
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao localizar certificado", e);
			}
		}
		if (cert != null) {
			if (trace) {
				logger.trace(String.format("Certificado encontrado - DN: %s",
						cert.getX509Certificate().getSubjectX500Principal().getName()));
			}
		} else {
			if (trace) {
				logger.trace("Certificado nao encontrado");
			}
		}
		return cert;
	}
	
	/**
	 * Lista todos os Certificados Confiaveis.
	 * @return Lista de Certificados.
	 */
	@Override
	public List<CertificadoConfiavel> listar() {
		if (trace) {
			logger.trace("Listar certificados");
		}
		List<CertificadoConfiavel> list = null;
		try {
			list = super.listar();
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao Listar certificado", e);
			}
		}
		if (trace) {
			logger.trace(String.format("%d certificados encontrados",
						(list != null) ? list.size() : 0));
		}
		return list;
	}
	
	/**
	 * Insere ou Atualiza (persiste) um Certificado Confiavel. 
	 * @param cert Certificado a ser persistido.
	 * @throws Exception 
	 */
	@Override
	public void inserir(CertificadoConfiavel cert) throws Exception {
		if (trace) {
			logger.trace(String.format("Inserir/Atualizar certificado DN: %s",
					cert.getX509Certificate().getSubjectX500Principal().getName()));
		}
		try {
			super.inserir(cert);
			if (trace) {
				logger.trace("Certificado inserido/atualizado");
			}
		} catch (Exception e) {
			logger.error("Erro ao inserir/atualizar certificado", e);
			throw new Exception("Erro ao inserir/atualizar certificado", e);
		}
	}
	
	/**
	 * Exclui um Certificado Confiavel do banco de dados.
	 * @param id ID do Certificado Confiavel a ser removido.
	 * @throws Exception 
	 */
	public void excluir(long id) throws Exception {
		if (trace) {
			logger.trace(String.format("Excluir certificado id: %d", id));
		}
		try {
			super.excluir(id);
			if (trace) {
				logger.trace("Certificado excluido");
			}
		} catch (Exception e) {
			logger.error("Erro ao excluir certificado", e);
			throw new Exception("Erro ao excluir certificado", e);
		}
	}
	
}
