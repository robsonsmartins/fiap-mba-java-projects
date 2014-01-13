package com.robsonmartins.fiap.tcc.dao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.EntityManager;

/**
 * Classe abstrata com operacoes basicas para mainupulacao
 * de dados (objetos persistidos) via JPA
 * @param <T> Classe do bean persistido
 * @author Robson Martins (robson@robsonmartins.com)
 */
public abstract class GenericDAO <T> {

	/* entity manager do JPA */
	protected EntityManager em;
	/* armazena a classe concreta derivada deste DAO */
	private Class<T> classe;
	
	/**
	 * Cria uma nova instancia de DAO.
	 * @param entityManager Objeto {@link EntityManager} da API JPA.
	 */
	@SuppressWarnings("unchecked")
	public GenericDAO(EntityManager entityManager){
		Class<?> thisClass = getClass();
		ParameterizedType t =
			(ParameterizedType) thisClass.getGenericSuperclass();
		Type t2 = t.getActualTypeArguments()[0];
		this.classe = (Class<T>) t2;
		em = entityManager;
	}

	/**
	 * Localiza um objeto persistido pelo id
	 * @param id Id do objeto
	 * @return Objeto persistido
	 * @throws Exception 
	 */
	public T localizar(Object id) throws Exception {
		T obj = null;
		try {
			obj = em.find(classe, id);
		} catch (Exception e) {
			throw e;
		}
		return obj;
	}

	/**
	 * Lista todos os objetos persistidos da classe
	 * @return Lista de objetos persistidos
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<T> listar() throws Exception {
		List<T> list = null;
		try {
			list = (List<T>) em.createQuery(
					"from " + classe.getSimpleName()).getResultList();
		} catch (Exception e) {
			throw e;
		}
		return list;
	}

	/**
	 * Insere (persiste) um objeto 
	 * @param obj Objeto a ser persistido
	 * @throws Exception 
	 */
	public synchronized void inserir(T obj) throws Exception {
		em.merge(obj);
		em.flush();
	}

	/**
	 * Exclui um objeto persistido
	 * @param id Id do objeto a ser removido
	 * @throws Exception 
	 */
	public synchronized void excluir(Object id) throws Exception {
		T obj = em.find(classe, id);
		em.remove(obj);
		em.flush();
	}
}
