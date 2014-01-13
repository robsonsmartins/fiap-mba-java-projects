package sicid.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Representa um usuario do sistema, persistido via JPA.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Entity
@SuppressWarnings("serial")
public class Usuario implements Serializable {

	@Id
	private String dname;
	@Column(nullable=false)
	private String username;
	private String name;
	private String role;

	/**
	 * Retorna o Distinguished Name (DN).
	 * @return DN do certificado do usuario.
	 */
	public String getDname() {
		return dname;
	}

	/**
	 * Configura o Distinguished Name (DN).
	 * @param dname DN do certificado do usuario.
	 */
	public void setDname(String dname) {
		this.dname = dname;
	}

	/**
	 * Retorna o username. 
	 * @return Username do usuario.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Configura o username.
	 * @param username Username do usuario.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Retorna o nome.
	 * @return Nome do usuario.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Configura o nome.
	 * @param name Nome do usuario.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retorna a role.
	 * @return Role associada ao usuario.
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Configura a role.
	 * @param role Role associada ao usuario.
	 */
	public void setRole(String role) {
		this.role = role;
	}

}
