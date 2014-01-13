package icp.web.controller;

import icp.bean.TipoCertificado;

import java.io.Serializable;
import java.util.Date;

/**
 * Armazena parametros obtidos de um formulario de emissao de Certificados.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class CertParams implements Serializable {
	
	private TipoCertificado tipo;
	private String keyPassword; 
	private String caPassword; 
	private String nome;
	private String nomePJ;
	private String nomeDNS;
	private String nomeApp;
	private String guidServer;
	private String cpf;
	private String cnpj;
	private String ric;
	private String email; 
	private Date nascimento; 
	private String pisPasep; 
	private String rg;
	private String rgOrgEmissor;
	private String rgUF;
	private String cei;
	private String titulo;
	private String tituloZona;
	private String tituloSecao;
	private String tituloMunicipio;
	private String tituloUF;
	private String login;
	
	private String subjC;
	private String subjO;
	private String subjOU;
	private String subjL;
	private String subjST;
	
	/**
	 * Cria uma instancia limpa do gerenciador de parametros de Certificado.
	 */
	public CertParams() {
		clear();
	}
	
	/**
	 * Limpa todos os parametros de Certificado.
	 */
	public void clear() {
		this.tipo            = null;
		this.keyPassword     = null;
		this.caPassword      = null;
		this.nome            = null;
		this.nomePJ          = null;
		this.nomeDNS         = null;
		this.nomeApp         = null;
		this.guidServer      = null;
		this.cpf             = null;
		this.cnpj            = null;
		this.ric             = null;
		this.email           = null;
		this.nascimento      = null;
		this.pisPasep        = null;
		this.rg              = null;
		this.rgOrgEmissor    = null;
		this.rgUF            = null;
		this.cei             = null;
		this.titulo          = null;
		this.tituloZona      = null;
		this.tituloSecao     = null;
		this.tituloMunicipio = null;
		this.tituloUF        = null;
		this.login           = null;
		this.subjC           = null;
		this.subjO           = null;
		this.subjOU          = null;
		this.subjL           = null;
		this.subjST          = null;
	}
	
	/**
	 * Retorna o Tipo de Certificado.
	 * @return Tipo de Certificado.
	 */
	public TipoCertificado getTipo() {
		return tipo;
	}
	
	/**
	 * Configura o Tipo de Certificado.
	 * @param tipo Tipo de Certificado.
	 */
	public void setTipo(TipoCertificado tipo) {
		this.tipo = tipo;
	}
	
	/**
	 * Retorna a senha da chave privada do certificado.
	 * @return Senha da chave privada.
	 */
	public String getKeyPassword() {
		return keyPassword;
	}
	
	/**
	 * Configura a senha da chave privada do certificado.
	 * @param keyPassword Senha da chave privada.
	 */
	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}
	
	/**
	 * Retorna a senha da AC Emissora do certificado.
	 * @return Senha da AC Emissora.
	 */
	public String getCaPassword() {
		return caPassword;
	}
	
	/**
	 * Configura a senha da AC Emissora do certificado.
	 * @param caPassword Senha da AC Emissora.
	 */
	public void setCaPassword(String caPassword) {
		this.caPassword = caPassword;
	}
	
	/**
	 * Retorna o nome do responsavel (PF) pelo certificado.
	 * @return Nome do responsavel.
	 */
	public String getNome() {
		return nome;
	}
	
	/**
	 * Configura o nome do responsavel (PF) pelo certificado.
	 * @param nome Nome do responsavel.
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	/**
	 * Retorna o nome empresarial (PJ) do responsavel pelo certificado.
	 * @return Nome empresarial.
	 */
	public String getNomePJ() {
		return nomePJ;
	}

	/**
	 * Configura o nome empresarial (PJ) do responsavel pelo certificado.
	 * @param nomePJ Nome empresarial.
	 */
	public void setNomePJ(String nomePJ) {
		this.nomePJ = nomePJ;
	}

	/**
	 * Retorna o nome de dominio (DNS) do servidor.
	 * @return Nome DNS do servidor.
	 */
	public String getNomeDNS() {
		return nomeDNS;
	}

	/**
	 * Configura o nome de dominio (DNS) do servidor.
	 * @param nomeDNS Nome DNS do servidor.
	 */
	public void setNomeDNS(String nomeDNS) {
		this.nomeDNS = nomeDNS;
	}

	/**
	 * Retorna o nome da aplicacao do certificado.
	 * @return Nome da aplicacao do certificado.
	 */
	public String getNomeApp() {
		return nomeApp;
	}

	/**
	 * Configura o nome da aplicacao do certificado.
	 * @param nomeApp Nome da aplicacao do certificado.
	 */
	public void setNomeApp(String nomeApp) {
		this.nomeApp = nomeApp;
	}

	/**
	 * Retorna o GUID do servidor.
	 * @return GUID do servidor.
	 */
	public String getGuidServer() {
		return guidServer;
	}

	/**
	 * Configura o GUID do servidor.
	 * @param guidServer GUID do servidor.
	 */
	public void setGuidServer(String guidServer) {
		this.guidServer = guidServer;
	}

	/**
	 * Retorna o CPF do responsavel pelo certificado.
	 * @return CPF do responsavel.
	 */
	public String getCpf() {
		return cpf;
	}
	
	/**
	 * Configura o CPF do responsavel pelo certificado.
	 * @param cpf
	 */
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	
	/**
	 * Retorna o CNPJ do responsavel pelo certificado.
	 * @return CNPJ do responsavel.
	 */
	public String getCnpj() {
		return cnpj;
	}
	
	/**
	 * Configura o CNPJ do responsavel pelo certificado.
	 * @param cnpj CNPJ do responsavel.
	 */
	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}
	
	/**
	 * Retorna o RIC do responsavel pelo certificado.
	 * @return RIC do responsavel.
	 */
	public String getRic() {
		return ric;
	}
	
	/**
	 * Configura o RIC do responsavel pelo certificado.
	 * @param ric RIC do responsavel.
	 */
	public void setRic(String ric) {
		this.ric = ric;
	}
	
	/**
	 * Retorna o email do responsavel pelo certificado.
	 * @return Email do responsavel.
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * Configura o email do responsavel pelo certificado.
	 * @param email Email do responsavel.
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Retorna a data de nascimento do responsavel pelo certificado.
	 * @return Data de nascimento do responsavel.
	 */
	public Date getNascimento() {
		return nascimento;
	}
	
	/**
	 * Configura a data de nascimento do responsavel pelo certificado.
	 * @param nascimento Data de nascimento do responsavel.
	 */
	public void setNascimento(Date nascimento) {
		this.nascimento = nascimento;
	}
	
	/**
	 * Retorna o PIS/PASEP do responsavel pelo certificado.
	 * @return PIS/PASEP do responsavel.
	 */
	public String getPisPasep() {
		return pisPasep;
	}
	
	/**
	 * Configura o PIS/PASEP do responsavel pelo certificado.
	 * @param pisPasep PIS/PASEP do responsavel.
	 */
	public void setPisPasep(String pisPasep) {
		this.pisPasep = pisPasep;
	}
	
	/**
	 * Retorna o RG do responsavel pelo certificado.
	 * @return RG do responsavel.
	 */
	public String getRg() {
		return rg;
	}
	
	/**
	 * Configura o RG do responsavel pelo certificado.
	 * @param rg RG do responsavel.
	 */
	public void setRg(String rg) {
		this.rg = rg;
	}
	
	/**
	 * Retorna o Orgao Emissor do RG do responsavel pelo certificado.
	 * @return Orgao Emissor do RG do responsavel.
	 */
	public String getRgOrgEmissor() {
		return rgOrgEmissor;
	}
	
	/**
	 * Configura o Orgao Emissor do RG do responsavel pelo certificado.
	 * @param rgOrgEmissor Orgao Emissor do RG do responsavel.
	 */
	public void setRgOrgEmissor(String rgOrgEmissor) {
		this.rgOrgEmissor = rgOrgEmissor;
	}
	
	/**
	 * Retorna a UF do RG do responsavel pelo certificado.
	 * @return UF do RG do responsavel.
	 */
	public String getRgUF() {
		return rgUF;
	}
	
	/**
	 * Configura a UF do RG do responsavel pelo certificado.
	 * @param rgUF UF do RG do responsavel.
	 */
	public void setRgUF(String rgUF) {
		this.rgUF = rgUF;
	}
	
	/**
	 * Retorna o CEI do responsavel pelo certificado.
	 * @return CEI do responsavel.
	 */
	public String getCei() {
		return cei;
	}
	
	/**
	 * Configura o CEI do responsavel pelo certificado.
	 * @param cei CEI do responsavel.
	 */
	public void setCei(String cei) {
		this.cei = cei;
	}
	
	/**
	 * Retorna o Titulo do responsavel pelo certificado.
	 * @return Titulo do responsavel.
	 */
	public String getTitulo() {
		return titulo;
	}
	
	/**
	 * Configura o Titulo do responsavel pelo certificado.
	 * @param titulo Titulo do responsavel.
	 */
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	
	/**
	 * Retorna a Zona Eleitoral do Titulo do responsavel pelo certificado.
	 * @return Zona Eleitoral do Titulo do responsavel.
	 */
	public String getTituloZona() {
		return tituloZona;
	}
	
	/**
	 * Configura a Zona Eleitoral do Titulo do responsavel pelo certificado.
	 * @param tituloZona Zona Eleitoral do Titulo do responsavel.
	 */
	public void setTituloZona(String tituloZona) {
		this.tituloZona = tituloZona;
	}
	
	/**
	 * Retorna a Secao Eleitoral do Titulo do responsavel pelo certificado.
	 * @return Secao Eleitoral do Titulo do responsavel.
	 */
	public String getTituloSecao() {
		return tituloSecao;
	}
	
	/**
	 * Configura a Secao Eleitoral do Titulo do responsavel pelo certificado.
	 * @param tituloSecao Secao Eleitoral do Titulo do responsavel.
	 */
	public void setTituloSecao(String tituloSecao) {
		this.tituloSecao = tituloSecao;
	}
	
	/**
	 * Retorna o Municipio do Titulo do responsavel pelo certificado.
	 * @return Municipio do Titulo do responsavel.
	 */
	public String getTituloMunicipio() {
		return tituloMunicipio;
	}
	
	/**
	 * Configura o Municipio do Titulo do responsavel pelo certificado.
	 * @param tituloMunicipio Municipio do Titulo do responsavel.
	 */
	public void setTituloMunicipio(String tituloMunicipio) {
		this.tituloMunicipio = tituloMunicipio;
	}
	
	/**
	 * Retorna a UF do Titulo do responsavel pelo certificado.
	 * @return UF do Titulo do responsavel.
	 */
	public String getTituloUF() {
		return tituloUF;
	}
	
	/**
	 * Configura a UF do Titulo do responsavel pelo certificado.
	 * @param tituloUF UF do Titulo do responsavel.
	 */
	public void setTituloUF(String tituloUF) {
		this.tituloUF = tituloUF;
	}
	
	/**
	 * Retorna o nome de login do responsavel pelo certificado.
	 * @return Nome de login do responsavel.
	 */
	public String getLogin() {
		return login;
	}
	
	/**
	 * Configura o nome de login do responsavel pelo certificado.
	 * @param login Nome de login do responsavel.
	 */
	public void setLogin(String login) {
		this.login = login;
	}
	
	/**
	 * Retorna o Subject (DN): Country (C) do certificado.
	 * @return Subject (DN): Country (C).
	 */
	public String getSubjC() {
		return subjC;
	}
	
	/**
	 * Configura o Subject (DN): Country (C) do certificado.
	 * @param subjC Subject (DN): Country (C).
	 */
	public void setSubjC(String subjC) {
		this.subjC = subjC;
	}
	
	/**
	 * Retorna o Subject (DN): Organization (O) do certificado.
	 * @return Subject (DN): Organization (O).
	 */
	public String getSubjO() {
		return subjO;
	}
	
	/**
	 * Configura o Subject (DN): Organization (O) do certificado.
	 * @param subjO Subject (DN): Organization (O).
	 */
	public void setSubjO(String subjO) {
		this.subjO = subjO;
	}
	
	/**
	 * Retorna o Subject (DN): Organizational Unit (OU) do certificado.
	 * @return Subject (DN): Organizational Unit (OU).
	 */
	public String getSubjOU() {
		return subjOU;
	}
	
	/**
	 * Configura o Subject (DN): Organizational Unit (OU) do certificado.
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 */
	public void setSubjOU(String subjOU) {
		this.subjOU = subjOU;
	}
	
	/**
	 * Retorna o Subject (DN): Locality (L) do certificado.
	 * @return Subject (DN): Locality (L).
	 */
	public String getSubjL() {
		return subjL;
	}

	/**
	 * Configura o Subject (DN): Locality (L) do certificado.
	 * @param subjL Subject (DN): Locality (L).
	 */
	public void setSubjL(String subjL) {
		this.subjL = subjL;
	}
	
	/**
	 * Retorna o Subject (DN): State (ST) do certificado.
	 * @return Subject (DN): State (ST).
	 */
	public String getSubjST() {
		return subjST;
	}
	
	/**
	 * Configura o Subject (DN): State (ST) do certificado.
	 * @param subjST Subject (DN): State (ST).
	 */
	public void setSubjST(String subjST) {
		this.subjST = subjST;
	}
}
