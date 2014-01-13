package banco.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Representa uma conta bancaria, persistido via JPA.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Entity
@XmlRootElement(name="conta")
@SuppressWarnings("serial")
public class Conta implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="conta")
	private long numeroConta;
	
	private String cpf;
	
	@OneToOne(cascade={},optional=false)
    @JoinColumn(name="cpf",referencedColumnName="cpf",
    		nullable=false,unique=true,
    		insertable=false,updatable=false)
	private Usuario cliente;

	private float saldo;
	
	/**
	 * Retorna o numero da conta.
	 * @return Numero da conta.
	 */
	public long getNumeroConta() {
		return numeroConta;
	}

	/**
	 * Configura o numero da conta.
	 * @param numeroConta Numero da conta.
	 */
	public void setNumeroConta(long numeroConta) {
		this.numeroConta = numeroConta;
	}

	/**
	 * Retorna o CPF.
	 * @return CPF do cliente.
	 */
	@XmlTransient
	public String getCpf() {
		return cpf;
	}

	/**
	 * Configura o CPF.
	 * @param cpf CPF do cliente.
	 */
	public void setCpf(String cpf) {
		this.cpf = cpf;
		if (this.cliente != null) { this.cliente.setCpf(this.cpf); }
	}
	
	/**
	 * Retorna o Usuario (cliente) associado a conta.
	 * @return Cliente que possui a conta.
	 */
	@XmlElement(name="cliente")
	public Usuario getCliente() {
		return cliente;
	}

	/**
	 * Configura o Usuario (cliente) associado a conta.
	 * @param cliente Cliente que possui a conta.
	 */
	public void setCliente(Usuario cliente) {
		this.cliente = cliente;
		if (this.cliente != null) { this.cpf = this.cliente.getCpf(); }
	}

	/**
	 * Retorna o saldo corrente da conta.
	 * @return Saldo da conta.
	 */
	public float getSaldo() {
		return saldo;
	}

	/**
	 * Altera o saldo da conta.
	 * @param saldo Novo saldo da conta.
	 */
	public void setSaldo(float saldo) {
		this.saldo = saldo;
	}
}
