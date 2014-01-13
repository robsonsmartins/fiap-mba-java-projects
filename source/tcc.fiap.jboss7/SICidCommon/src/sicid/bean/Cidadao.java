package sicid.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representa um cidadao, persistido via JPA e usado
 *   por metodos do servico SICid como representacao XML.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Entity
@XmlRootElement(name="cidadao")
@SuppressWarnings("serial")
public class Cidadao implements Serializable, Cloneable {

	@Id
	private String dname;
	@Column(nullable=false)
	private String nome;
	@Column(nullable=true)
	private String nomePai;
	@Column(nullable=true)
	private String nomeMae;
	@Column(nullable=false)
	@Temporal(TemporalType.DATE)
	private Date dataNascimento;
	@Column(length=11)
	private String ric;
	@Column(length=11,nullable=true)
	private String cpf;
	@Embedded
	@Column(nullable=true)
	private DocumentoRG rg;
	@Embedded
	@Column(nullable=true)
	private DocumentoTitulo titulo;
	@Column(length=11,nullable=true)
	private String pisPasep;
	@Column(length=12,nullable=true)
	private String cei;
	@Transient
	private String email;
	@Transient
	private String login;
	
	/* Cria um clone do objeto corrente.
	 * @return Clone do objeto corrente, ou null se houve erro. */
	@Override
	public Cidadao clone() {
		try {
			return (Cidadao) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	/* Retorna uma representacao String do objeto corrente.
	 * @return Representacao do objeto como String. */
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(Cidadao.class.getSimpleName())
			.append("[")
			.append(String.format("nome='%s',",nome))
			.append(String.format("nomePai='%s',",nomePai))
			.append(String.format("nomeMae='%s',",nomeMae))
			.append(String.format("dataNascimento='%s',",
					new SimpleDateFormat("dd/MM/yyyy").
					format(dataNascimento)))
			.append(String.format("ric='%s',",ric))
			.append(String.format("cpf='%s',",cpf))
			.append(String.format("rg='%s',",rg))
			.append(String.format("titulo='%s',",titulo))
			.append(String.format("pisPasep='%s',",pisPasep))
			.append(String.format("cei='%s',",cei))
			.append(String.format("email='%s',",email))
			.append(String.format("login='%s'",login))
			.append("]");
		return strBuilder.toString();
	}
	
	/**
	 * Retorna o Distinguished Name (DN) do certificado
	 *   digital do cidadao.
	 * @return DN do certificado.
	 */
	public String getDname() {
		return dname;
	}

	/**
	 * Configura o Distinguished Name (DN) do certificado
	 *   digital do cidadao.
	 * @param dname DN do certificado.
	 */
	public void setDname(String dname) {
		this.dname = dname;
	}

	/**
	 * Retorna o nome completo do cidadao.
	 * @return Nome do cidadao.
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Configura o nome completo do cidadao.
	 * @param nome Nome do cidadao.
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * Retorna o nome completo do pai.
	 * @return Nome do pai.
	 */
	public String getNomePai() {
		return nomePai;
	}

	/**
	 * Configura o nome completo do pai.
	 * @param nomePai Nome do pai.
	 */
	public void setNomePai(String nomePai) {
		this.nomePai = nomePai;
	}

	/**
	 * Retorna o nome completo da mae.
	 * @return Nome da mae.
	 */
	public String getNomeMae() {
		return nomeMae;
	}

	/**
	 * Configura o nome completo da mae.
	 * @param nomeMae Nome da mae.
	 */
	public void setNomeMae(String nomeMae) {
		this.nomeMae = nomeMae;
	}

	/**
	 * Retorna a data de nascimento.
	 * @return Data de nascimento.
	 */
	public Date getDataNascimento() {
		return dataNascimento;
	}

	/**
	 * Configura a data de nascimento.
	 * @param dataNascimento Data de nascimento.
	 */
	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	/**
	 * Retorna o numero do Registro de Identidade Civil (RIC).
	 * @return Numero do RIC.
	 */
	public String getRic() {
		return ric;
	}

	/**
	 * Configura o numero do Registro de Identidade Civil (RIC).
	 * @param ric Numero do RIC.
	 */
	public void setRic(String ric) {
		this.ric = ric;
	}

	/**
	 * Retorna o numero do Cadastro de Pessoa Fisica (CPF).
	 * @return Numero do CPF.
	 */
	public String getCpf() {
		return cpf;
	}

	/**
	 * Configura o numero do Cadastro de Pessoa Fisica (CPF).
	 * @param cpf Numero do CPF.
	 */
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	/**
	 * Retorna os dados do Registro Geral (RG).
	 * @return Objeto com os dados do RG.
	 */
	public DocumentoRG getRg() {
		return rg;
	}

	/**
	 * Configura os dados do Registro Geral (RG).
	 * @param rg Objeto com os dados do RG.
	 */
	public void setRg(DocumentoRG rg) {
		this.rg = rg;
	}

	/**
	 * Retorna os dados do Titulo Eleitoral.
	 * @return Objeto com os dados do Titulo Eleitoral.
	 */
	public DocumentoTitulo getTitulo() {
		return titulo;
	}

	/**
	 * Configura os dados do Titulo Eleitoral.
	 * @param titulo Objeto com os dados do Titulo Eleitoral.
	 */
	public void setTitulo(DocumentoTitulo titulo) {
		this.titulo = titulo;
	}

	/**
	 * Retorna o numero de inscricao no Programa de Integracao Social (PIS)
	 *   ou no Programa de Formacao do Patrimonio do Servidor Publico (PASEP).
	 * @return Numero do PIS/PASEP.
	 */
	public String getPisPasep() {
		return pisPasep;
	}

	/**
	 * Configura o numero de inscricao no Programa de Integracao Social (PIS)
	 *   ou no Programa de Formacao do Patrimonio do Servidor Publico (PASEP).
	 * @param pisPasep Numero do PIS/PASEP.
	 */
	public void setPisPasep(String pisPasep) {
		this.pisPasep = pisPasep;
	}

	/**
	 * Retorna o numero do Cadastro Especifico do INSS (CEI).
	 * @return Numero do CEI.
	 */
	public String getCei() {
		return cei;
	}

	/**
	 * Configura o numero do Cadastro Especifico do INSS (CEI).
	 * @param cei Numero do CEI.
	 */
	public void setCei(String cei) {
		this.cei = cei;
	}

	/**
	 * Retorna o endereco de e-mail do cidadao.<br>
	 *   (Atributo nao persistido em banco de dados).  
	 * @return Endereco de e-mail.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Configura o endereco de e-mail do cidadao.
	 * @param email Endereco de e-mail.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Retorna o nome de login do cidadao.<br>
	 *   (Atributo nao persistido em banco de dados).  
	 * @return Nome de login (username).
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * Configura o nome de login do cidadao.
	 * @param login Nome de login (username).
	 */
	public void setLogin(String login) {
		this.login = login;
	}
}
