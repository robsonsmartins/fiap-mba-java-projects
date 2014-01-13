package banco.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.robsonmartins.fiap.tcc.dao.GenericDAO;

import banco.bean.Conta;

/**
 * Classe de manipulacao de objetos persistidos via JPA
 * Objetos da classe bean {@link Conta}
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class ContaDAO extends GenericDAO<Conta> {

	protected static Logger logger;
	protected static boolean trace;
	
	/**
	 * Cria uma nova instancia de DAO.
	 * @param entityManager Objeto {@link EntityManager} da API JPA.
	 */
	public ContaDAO(EntityManager entityManager) {
		super(entityManager);
		logger = LogManager.getLogger(ContaDAO.class);
		trace = logger.isTraceEnabled();
	}

	/**
	 * Retorna uma Conta por numero de conta.
	 * @param numeroConta Numero da conta.
	 * @return Objeto que representa a Conta.
	 */
	public Conta localizar(long numeroConta) {
		if (trace) {
			logger.trace(String.format(
					"Localizar conta: numero=%d", numeroConta));
		}
		Conta conta = null;
		try {
			conta = super.localizar(numeroConta);
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao localizar conta", e);
			}
		}
		if (conta != null) {
			if (trace) {
				logger.trace(String.format(
						"Conta encontrada: numero=%d Cliente=%s",
						conta.getNumeroConta(),
						conta.getCliente().getName()));
			}
		} else {
			if (trace) {
				logger.trace("Conta nao encontrada");
			}
		}
		return conta;
	}
	
	/**
	 * Localiza Conta pelo CPF do cliente.
	 * @param cpf CPF do cliente.
	 * @return Objeto que representa a Conta.
	 */
	public Conta localizarPorUsuario(String cpf) {
		if (trace) {
			logger.trace(String.format(
					"Localizar conta cpf='%s'", cpf));
		}
		Conta conta = null;
		Query q = null;
		try {
			q = em.createQuery("from Conta where cpf like :cpf");
			q.setParameter("cpf", cpf);
			conta = (Conta) q.getSingleResult();
		} catch (Exception e) {
			if (trace) {
				logger.trace(String.format(
						"Erro ao localizar conta: %s",e.getLocalizedMessage()));
			}
		}
		if (conta != null) {
			if (trace) {
				logger.trace(String.format(
						"Conta encontrada: numero=%d Cliente=%s",
						conta.getNumeroConta(),
						conta.getCliente().getName()));
			}
		} else {
			if (trace) {
				logger.trace("Conta nao encontrada");
			}
		}
		return conta;
	}

	/**
	 * Lista todas as Contas.
	 * @return Lista de Contas.
	 */
	@Override
	public List<Conta> listar() {
		if (trace) {
			logger.trace("Listar contas");
		}
		List<Conta> list = null;
		try {
			list = super.listar();
		} catch (Exception e) {
			if (trace) {
				logger.trace("Erro ao Listar contas", e);
			}
		}
		if (trace) {
			logger.trace(String.format("%d contas encontradas",
					(list != null) ? list.size() : 0));
		}
		return list;
	}

	/**
	 * Insere ou Atualiza (persiste) uma Conta. 
	 * @param conta Conta a ser persistida.
	 * @throws Exception 
	 */
	@Override
	public void inserir(Conta conta) throws Exception {
		if (trace) {
			logger.trace(String.format(
					"Inserir/Atualizar conta: numero=%d Cliente=%s",
					conta.getNumeroConta(),
					conta.getCliente().getName()));
		}
		try {
			super.inserir(conta);
			if (trace) {
				logger.trace("Conta inserida/atualizada");
			}
		} catch (Exception e) {
			logger.error("Erro ao inserir/atualizar conta", e);
			throw new Exception("Erro ao inserir/atualizar conta", e);
		}
	}
	
	/**
	 * Exclui uma Conta do banco de dados.
	 * @param numeroConta Numero da conta.
	 * @throws Exception 
	 */
	public void excluir(long numeroConta) throws Exception {
		
		if (trace) {
			logger.trace(String.format(
					"Excluir conta: numero=%d",	numeroConta));
		}
		try {
			if (numeroConta > 0) {
				super.excluir(numeroConta);
			}
			if (trace) {
				logger.trace("Conta excluida");
			}
		} catch (Exception e) {
			logger.error("Erro ao excluir conta", e);
			throw new Exception("Erro ao excluir conta", e);
		}
	}
}
