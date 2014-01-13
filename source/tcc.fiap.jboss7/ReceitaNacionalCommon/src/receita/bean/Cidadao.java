package receita.bean;

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
public class Cidadao implements Serializable {

	@Id
	private String dname;
	@Column(nullable=false)
	private String ric;
	private String name;
	private String role;

	/**
	 * Retorna o Distinguished Name (DN).
	 * @return DN do certificado do cidadao.
	 */
	public String getDname() {
		return dname;
	}

	/**
	 * Configura o Distinguished Name (DN).
	 * @param dname DN do certificado do cidadao.
	 */
	public void setDname(String dname) {
		this.dname = dname;
	}

	/**
	 * Retorna o nome.
	 * @return Nome do cidadao.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Configura o nome.
	 * @param name Nome do cidadao.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retorna o RIC.
	 * @return RIC do cidadao.
	 */
	public String getRic() {
		return ric;
	}

	/**
	 * Configura o RIC.
	 * @param ric RIC do cidadao.
	 */
	public void setRic(String ric) {
		this.ric = ric;
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
