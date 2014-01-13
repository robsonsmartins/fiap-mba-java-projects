package banco.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="status")
@SuppressWarnings("serial")
public class StatusOperacao implements Serializable {

	private Boolean value;
	private Conta conta;
	private String motivo;

	/**
	 * @return the value
	 */
	@XmlAttribute(name="value")
	public Boolean getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Boolean value) {
		this.value = value;
	}

	/**
	 * @return the conta
	 */
	@XmlElement(name="conta")
	public Conta getConta() {
		return conta;
	}

	/**
	 * @param conta the conta to set
	 */
	public void setConta(Conta conta) {
		this.conta = conta;
	}

	/**
	 * @return the motivo
	 */
	@XmlElement(name="motivo")
	public String getMotivo() {
		return motivo;
	}

	/**
	 * @param motivo the motivo to set
	 */
	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
	
}
