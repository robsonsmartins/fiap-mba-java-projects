package icp.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Representa o registro de um Certificado emitido, persistido em Banco de Dados.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Entity
@SuppressWarnings("serial")
public class Certificado implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private TipoCertificado tipo;
	private String nomeAC;
	private String commonName;
	private String certFilename;
	private String keyFilename;
	private String reqFilename;
	private StatusCertificado status;
	
	@Temporal(TemporalType.DATE)
	private Date emissao;
	@Temporal(TemporalType.DATE)
	private Date expiracao;
	
	/**
	 * Retorna o ID (Chave Primaria) do registro do Certificado.
	 * @return ID do registro.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Configura o ID (Chave Primaria) do registro do Certificado.
	 * @param id ID do registro.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Retorna o Tipo do Certificado.
	 * @return Tipo do Certificado.
	 */
	public TipoCertificado getTipo() {
		return tipo;
	}

	/**
	 * Configura o Tipo do Certificado.
	 * @param tipo Tipo do Certificado.
	 */
	public void setTipo(TipoCertificado tipo) {
		this.tipo = tipo;
	}

	/**
	 * Retorna o Nome da AC Emissora do Certificado.
	 * @return Nome da AC Emissora.
	 */
	public String getNomeAC() {
		return nomeAC;
	}

	/**
	 * Configura o Nome da AC Emissora do Certificado.
	 * @param nomeAC Nome da AC Emissora.
	 */
	public void setNomeAC(String nomeAC) {
		this.nomeAC = nomeAC;
	}

	/**
	 * Retorna o Nome Comum (CN - Common Name) do Certificado.
	 * @return CN do Certificado.
	 */
	public String getCommonName() {
		return commonName;
	}

	/**
	 * Configura o Nome Comum (CN - Common Name) do Certificado.
	 * @param commonName CN do Certificado.
	 */
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	/**
	 * Retorna o Nome do arquivo do Certificado.
	 * @return Nome do arquivo do Certificado.
	 */
	public String getCertFilename() {
		return certFilename;
	}

	/**
	 * Configura o Nome do arquivo do Certificado.
	 * @param certFilename Nome do arquivo do Certificado.
	 */
	public void setCertFilename(String certFilename) {
		this.certFilename = certFilename;
	}

	/**
	 * Retorna o Nome do arquivo da Chave Privada.
	 * @return Nome do arquivo da Chave Privada.
	 */
	public String getKeyFilename() {
		return keyFilename;
	}

	/**
	 * Configura o Nome do arquivo da Chave Privada.
	 * @param keyFilename Nome do arquivo da Chave Privada.
	 */
	public void setKeyFilename(String keyFilename) {
		this.keyFilename = keyFilename;
	}

	/**
	 * Retorna o Nome do arquivo de Requisicao.
	 * @return Nome do arquivo de Requisicao.
	 */
	public String getReqFilename() {
		return reqFilename;
	}

	/**
	 * Configura o Nome do arquivo de Requisicao.
	 * @param reqFilename Nome do arquivo de Requisicao.
	 */
	public void setReqFilename(String reqFilename) {
		this.reqFilename = reqFilename;
	}

	/**
	 * Retorna a Data de Emissao do Certificado.
	 * @return Data de Emissao.
	 */
	public Date getEmissao() {
		return emissao;
	}

	/**
	 * Configura a Data de Emissao do Certificado.
	 * @param emissao Data de Emissao.
	 */
	public void setEmissao(Date emissao) {
		this.emissao = emissao;
	}

	/**
	 * Retorna a Data de Expiracao do Certificado.
	 * @return Data de Expiracao.
	 */
	public Date getExpiracao() {
		return expiracao;
	}

	/**
	 * Configura a Data de Expiracao do Certificado.
	 * @param expiracao Data de Expiracao.
	 */
	public void setExpiracao(Date expiracao) {
		this.expiracao = expiracao;
	}

	/**
	 * Retorna o Status do Certificado.
	 * @return Status do Certificado.
	 */
	public StatusCertificado getStatus() {
		return status;
	}

	/**
	 * Configura o Status do Certificado.
	 * @param status Status do Certificado.
	 */
	public void setStatus(StatusCertificado status) {
		this.status = status;
	}
}
