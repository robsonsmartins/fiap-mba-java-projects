package sicid.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Representa um consumidor de servicos confiavel,
 *   persistido via JPA.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Entity
@SuppressWarnings("serial")
public class ConsumidorConfiavel implements Serializable {

	@Id
	private String dname;
	@Column(nullable=false)
	private String id;
	private String name;
	private String role;

	/**
	 * Retorna o Distinguished Name (DN).
	 * @return DN do certificado do consumidor.
	 */
	public String getDname() {
		return dname;
	}

	/**
	 * Configura o Distinguished Name (DN).
	 * @param dname DN do certificado do consumidor.
	 */
	public void setDname(String dname) {
		this.dname = dname;
	}
	
	/**
	 * Retorna o ID do consumidor confiavel.
	 * @return ID do consumidor.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Configura o ID do consumidor confiavel.
	 * @param id ID do consumidor.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Retorna o nome.
	 * @return Nome do consumidor.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Configura o nome.
	 * @param name Nome do consumidor.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retorna a role.
	 * @return Role associada ao consumidor.
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Configura a role.
	 * @param role Role associada ao consumidor.
	 */
	public void setRole(String role) {
		this.role = role;
	}

}
