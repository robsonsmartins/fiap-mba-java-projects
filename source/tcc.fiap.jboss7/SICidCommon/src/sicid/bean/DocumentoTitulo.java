package sicid.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representa os dados de um documento Titulo Eleitoral,
 *   persistidos via JPA e usados por metodos do servico
 *   SICid como representacao XML.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Embeddable
@XmlRootElement(name="titulo")
@SuppressWarnings("serial")
public class DocumentoTitulo implements Serializable, Cloneable {

	@Column(name="titulo",length=12,nullable=false)
	private String numero;
	@Column(name="tituloZona",length=3,nullable=false)
	private String zona;
	@Column(name="tituloSecao",length=4,nullable=false)
	private String secao;
	@Column(name="tituloMunicipio",length=20,nullable=false)
	private String municipio;
	@Column(name="tituloUF",length=2,nullable=false)
	private String uf;

	/* Cria um clone do objeto corrente.
	 * @return Clone do objeto corrente, ou null se houve erro. */
	@Override
	public DocumentoTitulo clone() {
		try {
			return (DocumentoTitulo) super.clone();
		} catch (CloneNotSupportedException e) {
			return this;
		}
	}
		
	/* Retorna uma representacao String do objeto corrente.
	 * @return Representacao do objeto como String. */
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Titulo[")
			.append(String.format("numero='%s',",numero))
			.append(String.format("zona='%s',",zona))
			.append(String.format("secao='%s',",secao))
			.append(String.format("municipio='%s',",municipio))
			.append(String.format("uf='%s'",uf))
			.append("]");
		return strBuilder.toString();
	}
	
	/**
	 * Retorna o numero do Titulo Eleitoral.
	 * @return Numero do Titulo.
	 */
	public String getNumero() {
		return numero;
	}

	/**
	 * Configura o numero do Titulo Eleitoral.
	 * @param numero Numero do Titulo.
	 */
	public void setNumero(String numero) {
		this.numero = numero;
	}

	/**
	 * Retorna a Zona do Titulo Eleitoral.
	 * @return Zona Eleitoral do Titulo.
	 */
	public String getZona() {
		return zona;
	}

	/**
	 * Configura a Zona do Titulo Eleitoral.
	 * @param zona Zona Eleitoral do Titulo.
	 */
	public void setZona(String zona) {
		this.zona = zona;
	}

	/**
	 * Retorna a Secao do Titulo Eleitoral.
	 * @return Secao Eleitoral do Titulo.
	 */
	public String getSecao() {
		return secao;
	}

	/**
	 * Configura a Secao do Titulo Eleitoral.
	 * @param secao Secao Eleitoral do Titulo.
	 */
	public void setSecao(String secao) {
		this.secao = secao;
	}

	/**
	 * Retorna o Municipio do Titulo Eleitoral.
	 * @return Municipio do Titulo.
	 */
	public String getMunicipio() {
		return municipio;
	}

	/**
	 * Configura o Municipio do Titulo Eleitoral.
	 * @param municipio Municipio do Titulo.
	 */
	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	/**
	 * Retorna a Unidade da Federacao (UF) do Titulo Eleitoral.
	 * @return UF do Titulo Eleitoral.
	 */
	public String getUf() {
		return uf;
	}

	/**
	 * Configura a Unidade da Federacao (UF) do Titulo Eleitoral.
	 * @param uf UF do Titulo Eleitoral.
	 */
	public void setUf(String uf) {
		this.uf = uf;
	}
}
