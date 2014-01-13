package banco.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import banco.bean.Usuario;

import com.robsonmartins.fiap.tcc.dao.GenericDAO;

/**
 * Classe de manipulacao de objetos persistidos via JPA
 * Objetos da classe bean {@link Usuario}
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class UsuarioDAO extends GenericDAO<Usuario> {

	protected static Logger logger;
	protected static boolean trace;
	
	/**
	 * Cria uma nova instancia de DAO.
	 * @param entityManager Objeto {@link EntityManager} da API JPA.
	 */
	public UsuarioDAO(EntityManager entityManager) {
		super(entityManager);
		logger = LogManager.getLogger(UsuarioDAO.class);
		trace = logger.isTraceEnabled();
	}

	/**
	 * Retorna um Usuario pelo DN.
	 * @param dname Distinguished Name (DN)
	 *   do Usuario no registro do banco de dados.
	 * @return Objeto que representa o Usuario.
	 */
	public Usuario localizar(String dname) {
		if (trace) {
			logger.trace(String.format("Localizar usuario DN: %s", dname));
		}
		Usuario usuario = null;
		try {
			usuario = super.localizar(dname);
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao localizar usuario", e);
			}
		}
		if (usuario != null) {
			if (trace) {
				logger.trace(String.format("Usuario encontrado - DN: %s",
						dname));
			}
		} else {
			if (trace) {
				logger.trace("Usuario nao encontrado");
			}
		}
		return usuario;
	}

	/**
	 * Retorna um Usuario pelo CPF.
	 * @param cpf CPF do usuario.
	 * @return Objeto que representa o Usuario.
	 */
	public Usuario localizarPorCpf(String cpf) {
		if (cpf == null) { return null; }
		if (trace) {
			logger.trace(String.format(
					"Localizar usuario cpf='%s'", cpf));
		}
		Usuario usuario = null;
		Query q = null;
		try {
			q = em.createQuery("from Usuario where cpf like :cpf");
			q.setParameter("cpf", cpf);
			usuario = (Usuario) q.getSingleResult();
		} catch (Exception e) {
			if (trace) {
				logger.trace(String.format(
						"Localizar usuario: %s",e.getLocalizedMessage()));
			}
		}
		return usuario;		
	}
	
	/**
	 * Lista todos os Usuarios.
	 * @return Lista de Usuarios.
	 */
	@Override
	public List<Usuario> listar() {
		if (trace) {
			logger.trace("Listar usuarios");
		}
		List<Usuario> list = null;
		try {
			list = super.listar();
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao Listar usuarios", e);
			}
		}
		if (trace) {
			logger.trace(String.format("%d usuarios encontrados",
					(list != null) ? list.size() : 0));
		}
		return list;
	}
	
	/**
	 * Lista Usuarios por Role.
	 * @param role Role dos usuarios.
	 * @return Lista de Usuarios.
	 */
	@SuppressWarnings("unchecked")
	public List<Usuario> listarPorRole(String role) {
		if (trace) {
			logger.trace(String.format(
					"Listar usuarios por role=%s", role));
		}
		List<Usuario> list = null;
		Query q = null;
		try {
			q = em.createQuery("from Usuario where role like :role");
			q.setParameter("role", role);
			list = (List<Usuario>) q.getResultList();
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao Listar usuarios", e);
			}
		}
		if (trace) {
			logger.trace(String.format("%d usuarios encontrados",
					(list != null) ? list.size() : 0));
		}
		return list;
	}

	/**
	 * Insere ou Atualiza (persiste) um Usuario. 
	 * @param usuario Usuario a ser persistido.
	 * @throws Exception 
	 */
	@Override
	public void inserir(Usuario usuario) throws Exception {
		if (trace) {
			logger.trace(String.format("Inserir/Atualizar usuario: %s",
					usuario.getName()));
		}
		try {
			super.inserir(usuario);
			if (trace) {
				logger.trace("Usuario inserido/atualizado");
			}
		} catch (Exception e) {
			logger.error("Erro ao inserir/atualizar usuario", e);
			throw new Exception("Erro ao inserir/atualizar usuario", e);
		}
	}
	
	/**
	 * Exclui um Usuario do banco de dados.
	 * @param dname Distinguished Name (DN)
	 *   do Usuario a ser removido.
	 * @throws Exception 
	 */
	public void excluir(String dname) throws Exception {
		if (trace) {
			logger.trace(String.format("Excluir usuario DN: %s", dname));
		}
		try {
			super.excluir(dname);
			if (trace) {
				logger.trace("Usuario excluido");
			}
		} catch (Exception e) {
			logger.error("Erro ao excluir usuario", e);
			throw new Exception("Erro ao excluir usuario", e);
		}
	}
}
