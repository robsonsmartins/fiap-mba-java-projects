package banco.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Representa um usuario do sistema, persistido via JPA.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Entity
@XmlRootElement(name="usuario")
@SuppressWarnings("serial")
public class Usuario implements Serializable {

	@Id
	private String dname;
	@Column(nullable=false)
	private String cpf;
	private String name;
	private String role;

	/**
	 * Retorna o Distinguished Name (DN).
	 * @return DN do certificado do usuario.
	 */
	@XmlTransient
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
	 * Retorna o CPF.
	 * @return CPF do usuario.
	 */
	public String getCpf() {
		return cpf;
	}

	/**
	 * Configura o CPF.
	 * @param cpf CPF do usuario.
	 */
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	/**
	 * Retorna a role.
	 * @return Role associada ao usuario.
	 */
	@XmlTransient
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
