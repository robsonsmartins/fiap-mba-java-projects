package sicid.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representa os dados de um documento RG, persistidos via JPA e usados
 *   por metodos do servico SICid como representacao XML.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Embeddable
@XmlRootElement(name="rg")
@SuppressWarnings("serial")
public class DocumentoRG implements Serializable, Cloneable {

	@Column(name="rg",length=15,nullable=false)
	private String numero;
	@Column(name="rgOrgExpedidor",length=4,nullable=false)
	private String orgExpedidor;
	@Column(name="rgUF",length=2,nullable=false)
	private String uf;

	/* Cria um clone do objeto corrente.
	 * @return Clone do objeto corrente, ou null se houve erro. */
	@Override
	public DocumentoRG clone() {
		try {
			return (DocumentoRG) super.clone();
		} catch (CloneNotSupportedException e) {
			return this;
		}
	}
	
	/* Retorna uma representacao String do objeto corrente.
	 * @return Representacao do objeto como String. */
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("RG[")
			.append(String.format("numero='%s',",numero))
			.append(String.format("orgExpedidor='%s',",orgExpedidor))
			.append(String.format("uf='%s'",uf))
			.append("]");
		return strBuilder.toString();
	}
	
	
	/**
	 * Retorna o numero do RG.
	 * @return Numero do RG.
	 */
	public String getNumero() {
		return numero;
	}

	/**
	 * Configura o numero do RG.
	 * @param numero Numero do RG.
	 */
	public void setNumero(String numero) {
		this.numero = numero;
	}

	/**
	 * Retorna o Orgao Expedidor do RG.
	 * @return Orgao Expedidor do RG.
	 */
	public String getOrgExpedidor() {
		return orgExpedidor;
	}

	/**
	 * Configura o Orgao Expedidor do RG.
	 * @param orgExpedidor Orgao Expedidor do RG.
	 */
	public void setOrgExpedidor(String orgExpedidor) {
		this.orgExpedidor = orgExpedidor;
	}

	/**
	 * Retorna a Unidade da Federacao (UF) do RG.
	 * @return UF de expedicao RG.
	 */
	public String getUf() {
		return uf;
	}

	/**
	 * Configura a Unidade da Federacao (UF) do RG.
	 * @param uf UF de expedicao RG.
	 */
	public void setUf(String uf) {
		this.uf = uf;
	}
	
}
