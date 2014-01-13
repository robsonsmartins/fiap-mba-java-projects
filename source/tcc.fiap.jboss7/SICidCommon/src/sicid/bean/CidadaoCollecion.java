package sicid.bean;

import java.io.Serializable;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representa uma colecao (lista) de cidadaos, usada
 *   por metodos do servico SICid como representacao XML.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@XmlRootElement(name="cidadaos")
@SuppressWarnings("serial")
public class CidadaoCollecion implements Serializable {

	private Collection<Cidadao> cidadaos;

	/**
	 * Retorna a quantidade de elementos na colecao.
	 * @return Quantidade de elementos na colecao.
	 */
	@XmlAttribute(name="count")
	public Integer getCount() {
		return (cidadaos != null) ? cidadaos.size() : null;
	}
	
	/**
	 * Configura a quantidade de elementos na colecao.
	 * @param count Quantidade de elementos na colecao.
	 */
	public void setCount(Integer count) { }

	/**
	 * Retorna uma colecao de Cidadaos.
	 * @return Colecao de cidadaos.
	 */
	@XmlElement(name="cidadao")
	public Collection<Cidadao> getCidadaos() {
		return cidadaos;
	}

	/** 
	 * Configura a colecao de Cidadaos.
	 * @param cidadaos Colecao de cidadaos.
	 */
	public void setCidadaos(Collection<Cidadao> cidadaos) {
		this.cidadaos = cidadaos;
	}
	
}
