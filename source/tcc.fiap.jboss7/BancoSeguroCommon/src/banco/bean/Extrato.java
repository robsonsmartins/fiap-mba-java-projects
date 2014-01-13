package banco.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Representa uma entrada de extrato bancario, persistido via JPA.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Entity
@SuppressWarnings("serial")
public class Extrato implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long sequencia;
	
	@ManyToOne
    @JoinColumn(name="conta",referencedColumnName="conta",nullable=false)
	private Conta conta;
	
	@Temporal(TemporalType.DATE)
	private Date data;
	
	private Operacao operacao;
	
	private float valor;
	
	private float saldoFinal;

	/**
	 * Retorna o numero de sequencia da entrada no extrato.
	 * @return Numero de sequencia.
	 */
	public long getSequencia() {
		return sequencia;
	}

	/**
	 * Configura o numero de sequencia da entrada no extrato.
	 * @param sequencia Numero de sequencia.
	 */
	public void setSequencia(long sequencia) {
		this.sequencia = sequencia;
	}

	/**
	 * Retorna a conta associada a entrada de extrato.
	 * @return Conta associada.
	 */
	public Conta getConta() {
		return conta;
	}

	/**
	 * Configura a conta associada a entrada de extrato.
	 * @param conta Conta associada.
	 */
	public void setConta(Conta conta) {
		this.conta = conta;
	}

	/**
	 * Retorna a data da operacao no extrato.
	 * @return Data da operacao.
	 */
	public Date getData() {
		return data;
	}

	/**
	 * Configura a data da operacao no extrato.
	 * @param data Data da operacao.
	 */
	public void setData(Date data) {
		this.data = data;
	}

	/**
	 * Retorna a operacao efetuada.
	 * @return Operacao efetuada.
	 */
	public Operacao getOperacao() {
		return operacao;
	}

	/**
	 * Configura a operacao efetuada.
	 * @param operacao Operacao efetuada.
	 */
	public void setOperacao(Operacao operacao) {
		this.operacao = operacao;
	}

	/**
	 * Retorna o valor da operacao.
	 * @return Valor da operacao.
	 */
	public float getValor() {
		return valor;
	}

	/**
	 * Configura o valor da operacao.
	 * @param valor Valor da operacao.
	 */
	public void setValor(float valor) {
		this.valor = valor;
	}

	/**
	 * Retorna o saldo final apos a efetivacao da operacao.
	 * @return Saldo final da conta.
	 */
	public float getSaldoFinal() {
		return saldoFinal;
	}

	/**
	 * Configura o saldo final apos a efetivacao da operacao.
	 * @param saldoFinal Saldo final da conta.
	 */
	public void setSaldoFinal(float saldoFinal) {
		this.saldoFinal = saldoFinal;
	}

	
}
