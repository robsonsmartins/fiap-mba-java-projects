package receita.bean;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

/**
 * Representa o tributo de um cidadao, persistido via JPA.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Entity
@SuppressWarnings("serial")
public class Tributo implements Serializable {

	@Id
	private String ric;
	
	@OneToOne
    @JoinColumn(name="ric",referencedColumnName="ric",
    		insertable=false,updatable=false)
	private Cidadao cidadao;

	private float impostoDevido;
	private boolean impostoPago;
	
	/** 
	 * Cria uma nova instancia de Tributo. 
	 */
	public Tributo() {
		cidadao = new Cidadao();
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
		if (this.cidadao != null) { this.cidadao.setRic(this.ric); }
	}
	
	/**
	 * Retorna o valor de imposto devido.
	 * @return Valor de imposto devido.
	 */
	public float getImpostoDevido() {
		return impostoDevido;
	}

	/**
	 * Configura o valor de imposto devido.
	 * @param impostoDevido Valor de imposto devido.
	 */
	public void setImpostoDevido(float impostoDevido) {
		this.impostoDevido = impostoDevido;
	}

	/**
	 * Retorna se o imposto foi pago.
	 * @return True se o imposto foi pago.
	 */
	public boolean isImpostoPago() {
		return impostoPago;
	}

	/**
	 * Configura se o imposto foi pago.
	 * @param impostoPago True se o imposto foi pago.
	 */
	public void setImpostoPago(boolean impostoPago) {
		this.impostoPago = impostoPago;
	}

	/**
	 * Retorna o cidadao associado ao tributo.
	 * @return Cidadao associado ao tributo.
	 */
	public Cidadao getCidadao() {
		return cidadao;
	}

	/**
	 * Configura o cidadao associado ao tributo.
	 * @param cidadao Cidadao associado ao tributo.
	 */
	public void setCidadao(Cidadao cidadao) {
		this.cidadao = cidadao;
		if (this.cidadao != null) { this.ric = this.cidadao.getRic(); }
	}
	
	/**
	 * Configura o objeto cidadao associado ao tributo,
	 * 	 atraves do RIC.
	 */
	@PrePersist
	public void initializeCidadao() {
		this.cidadao.setRic(this.ric);
	}
	
}
