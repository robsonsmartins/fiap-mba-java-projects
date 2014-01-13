package icp.model;

import icp.bean.Ca;
import icp.bean.Certificado;
import icp.bean.TipoCertificado;
import icp.bean.Usuario;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;

/**
 * Aplicacao para Gerenciamento de uma PKI (ICP) no padrao ICP-Brasil.
 * @author Robson Martins (robson@robsonmartins.com)
 * @see <a target="_blank" href="http://www.iti.gov.br/twiki/bin/view/Certificacao/EstruturaIcp">Estrutura da ICP-Brasil</a>
 */
@Local
public interface IIcpAdmin {

	/** Role para acessar a aplicacao como admininistrador. */
	public static final String ICPADMIN_ADMIN_ACCESS_ROLE = "admin";
	
	/**
	 * Lista todas as AC Raiz existentes.
	 * @return Lista com todas AC Raiz.
	 */
	public List<Ca> listarACRaiz();

	/**
	 * Cria uma nova AC Raiz.
	 * @param nomeAC Nome da AC Raiz.
	 * @param lcrURI Caminho (URI) da Lista de Certificados Revogados (LCR).
	 * @param dpcURI Caminho (URI) do Documento com as Politicas de Certificacao (DPC).
	 * @param acPassword Senha da AC Raiz, para criptografar a chave privada.
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @param subjCN Subject (DN): Common Name (CN).
	 * @throws Exception
	 */
	public void adicionarACRaiz(String nomeAC, String lcrURI,
			String dpcURI, String acPassword, String subjC, String subjO,
			String subjOU, String subjCN) throws Exception;

	/**
	 * Remove uma AC Raiz.
	 * @param nomeAC Nome da AC Raiz.
	 * @throws Exception
	 */
	public void removerACRaiz(String nomeAC) throws Exception;
	
	/**
	 * Cria uma lista de certificados revogados (CRL), para uma AC Raiz.
	 * @param acRaiz Objeto que representa a AC Raiz.
	 * @param acPassword Senha da AC Raiz.
	 * @throws Exception
	 */
	public void criarACRaizLCR(Ca acRaiz, String acPassword) throws Exception;

	/**
	 * Lista todas AC Intermediarias existentes.
	 * @return Lista com todas AC Intermediarias.
	 */
	public List<Ca> listarACInterm();

	/**
	 * Cria uma nova AC Intermediaria.
	 * @param acRaiz Objeto que representa a AC Raiz.
	 * @param nomeAC Nome da AC.
	 * @param lcrURI Caminho (URI) da Lista de Certificados Revogados (LCR).
	 * @param dpcURI Caminho (URI) do Documento com as Politicas de Certificacao (DPC).
	 * @param keyPassword Senha da AC, para criptografar a chave privada.
	 * @param acRaizPassword Senha da AC Raiz.
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @param subjCN Subject (DN): Common Name (CN).
	 * @throws Exception
	 */
	public void adicionarACInterm(Ca acRaiz, String nomeAC, String lcrURI,
			String dpcURI, String keyPassword, String acRaizPassword,
			String subjC, String subjO, String subjOU, String subjCN) throws Exception;

	/**
	 * Remove uma AC Intermediaria.
	 * @param nomeAC Nome da AC Intermediaria.
	 * @throws Exception
	 */
	public void removerACInterm(String nomeAC) throws Exception;

	/**
	 * Cria uma lista de certificados revogados (CRL), para uma AC Intermediaria.
	 * @param ac Objeto que representa a AC Intermediaria.
	 * @param acPassword Senha da AC Intermediaria.
	 * @throws Exception
	 */
	public void criarACIntermLCR(Ca ac, String acPassword) throws Exception;

	/**
	 * Retorna uma lista com os nomes dos Tipos de Certificado disponiveis. 
	 * @return Lista com as descricoes dos Tipos de Certificado.
	 */
	public List<TipoCertificado> listarTipoCert();
	
	/**
	 * Retorna uma lista com todos os certificados emitidos por uma AC.
	 * @param nomeAC Nome da AC emissora.
	 * @return Lista com os certificados emitidos pela AC.
	 */
	public List<Certificado> listarCert(String nomeAC);
	
	/**
	 * Emite um certificado no padrao e-CPF.
	 * @param acEmissora Objeto que representa a AC emissora (issuer).
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param acPassword Senha da AC.
	 * @param nome Nome do titular do e-CPF.
	 * @param cpf Numero do CPF, sem pontuacao (11 numeros).
	 * @param email Endereco de email.
	 * @param nascimento Data de nascimento.
	 * @param pisPasep Numero do PIS/PASEP/NIS, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG (2 caracteres).
	 * @param cei Numero do CEI (12 numeros).
	 * @param titulo Numero do Titulo de Eleitor (max. 12 numeros).
	 * @param tituloZona Zona Eleitoral do Titulo (max. 3 numeros).
	 * @param tituloSecao Secao Eleitoral do Titulo (max. 4 numeros).
	 * @param tituloMunicipio Municipio do Titulo Eleitoral (max. 20 caracteres).
	 * @param tituloUF UF do Titulo Eleitoral (2 caracteres).
	 * @param login Nome de Usuario (login de rede).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @throws Exception
	 */
	public void emitirCertEcpf(Ca acEmissora, String keyPassword, String acPassword,
			String nome, String cpf, String email, Date nascimento,
			String pisPasep, String rg,	String rgOrgEmissor, String rgUF,
			String cei, String titulo, String tituloZona, String tituloSecao,
			String tituloMunicipio,	String tituloUF, String login,
			String subjC, String subjO, String subjOU) throws Exception;
	
	/**
	 * Emite um certificado no padrao e-CNPJ.
	 * @param acEmissora Objeto que representa a AC emissora (issuer).
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param acPassword Senha da AC.
	 * @param nomePJ Nome empresarial.
	 * @param nome Nome do responsavel.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cei Numero do CEI do responsavel (12 numeros).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @param subjL Subject (DN): Locality (L).
	 * @param subjST Subject (DN): State (ST).
	 * @throws Exception
	 */
	public void emitirCertEcnpj(Ca acEmissora, String keyPassword, String acPassword,
			String nomePJ, String nome, String cpf,
			String email, Date nascimento, String pisPasep, String rg,
			String rgOrgEmissor, String rgUF, String cei,
			String cnpj, String subjC, String subjO, String subjOU,
			String subjL, String subjST) throws Exception;
	
	/**
	 * Emite um certificado no padrao RIC.
	 * @param acEmissora Objeto que representa a AC emissora (issuer).
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param acPassword Senha da AC.
	 * @param nome Nome do titular do e-CPF.
	 * @param ric Numero do RIC, sem pontuacao (11 numeros).
	 * @param cpf Numero do CPF, sem pontuacao (11 numeros).
	 * @param email Endereco de email.
	 * @param nascimento Data de nascimento.
	 * @param pisPasep Numero do PIS/PASEP/NIS, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG (2 caracteres).
	 * @param cei Numero do CEI (12 numeros).
	 * @param titulo Numero do Titulo de Eleitor (max. 12 numeros).
	 * @param tituloZona Zona Eleitoral do Titulo (max. 3 numeros).
	 * @param tituloSecao Secao Eleitoral do Titulo (max. 4 numeros).
	 * @param tituloMunicipio Municipio do Titulo Eleitoral (max. 20 caracteres).
	 * @param tituloUF UF do Titulo Eleitoral (2 caracteres).
	 * @param login Nome de Usuario (login de rede).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @throws Exception
	 */
	public void emitirCertRic(Ca acEmissora, String keyPassword, String acPassword,
			String nome, String ric, String cpf, String email, Date nascimento,
			String pisPasep, String rg,	String rgOrgEmissor, String rgUF,
			String cei, String titulo, String tituloZona, String tituloSecao,
			String tituloMunicipio,	String tituloUF, String login,
			String subjC, String subjO, String subjOU) throws Exception;

	/**
	 * Emite um certificado no padrao e-CODIGO.
	 * @param acEmissora Objeto que representa a AC emissora (issuer).
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param acPassword Senha da AC.
	 * @param nomePJ Nome empresarial.
	 * @param nome Nome do responsavel.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @throws Exception
	 */
	public void emitirCertEcodigo(Ca acEmissora, String keyPassword, String acPassword,
			String nomePJ, String nome, String cpf,
			String email, Date nascimento, String pisPasep, String rg,
			String rgOrgEmissor, String rgUF,
			String cnpj, String subjC, String subjO, String subjOU) throws Exception;

	/**
	 * Emite um certificado no padrao e-SERVIDOR.
	 * @param acEmissora Objeto que representa a AC emissora (issuer).
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param acPassword Senha da AC.
	 * @param nomeDNS Nome de DNS do servidor.
	 * @param nomePJ Nome empresarial.
	 * @param guid GUID do servidor.
	 * @param nome Nome do responsavel.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @throws Exception
	 */
	public void emitirCertEservidor(Ca acEmissora, String keyPassword, String acPassword,
			String nomeDNS, String nomePJ, String guid,
			String nome, String cpf, String email, Date nascimento,
			String pisPasep, String rg,	String rgOrgEmissor, String rgUF, String cnpj,
			String subjC, String subjO, String subjOU) throws Exception;
	
	/**
	 * Emite um certificado no padrao e-APLICACAO.
	 * @param acEmissora Objeto que representa a AC emissora (issuer).
	 * @param keyPassword Senha para criptografar a chave privada.
	 * @param acPassword Senha da AC.
	 * @param nomeApp Nome da aplicacao.
	 * @param nomePJ Nome empresarial.
	 * @param nome Nome do responsavel.
	 * @param cpf Numero do CPF do responsavel, sem pontuacao (11 numeros).
	 * @param email Endereco de email do responsavel.
	 * @param nascimento Data de nascimento do responsavel.
	 * @param pisPasep Numero do PIS/PASEP/NIS do responsavel, sem pontuacao (max. 11 numeros).
	 * @param rg Numero do RG do responsavel, alfanumerico, sem pontuacao (max. 15 caracteres).
	 * @param rgOrgEmissor Orgao emissor do RG do responsavel (max. 4 caracteres). 
	 * @param rgUF UF de emissao do RG do responsavel (2 caracteres).
	 * @param cnpj Numero do CNPJ, sem pontuacao (max. 14 numeros).
	 * @param subjC Subject (DN): Country (C).
	 * @param subjO Subject (DN): Organization (O).
	 * @param subjOU Subject (DN): Organizational Unit (OU).
	 * @throws Exception
	 */
	public void emitirCertEaplicacao(Ca acEmissora, String keyPassword, String acPassword,
			String nomeApp, String nomePJ, String nome, String cpf,
			String email, Date nascimento, String pisPasep, String rg,
			String rgOrgEmissor, String rgUF,
			String cnpj, String subjC, String subjO, String subjOU) throws Exception;
	
	/**
	 * Revoga um certificado.
	 * @param acEmissora Objeto que representa a AC emissora (issuer).
	 * @param cert Certificado a ser revogado.
	 * @param acPassword Senha da AC.
	 * @throws Exception
	 */
	public void revogarCert(Ca acEmissora, Certificado cert,
			String acPassword) throws Exception;

	/**
	 * Renova o certificado de uma AC Raiz.
	 * @param acRaiz Objeto que representa a AC Raiz.
	 * @param password Senha da AC Raiz.
	 * @throws Exception
	 */
	public void renovarACRaizCert(Ca acRaiz, String password) throws Exception;

	/**
	 * Renova o certificado de uma AC Intermediaria.
	 * @param ac Objeto que representa a AC Intermediaria.
	 * @param password Senha da AC Intermediaria.
	 * @throws Exception
	 */
	public void renovarACIntermCert(Ca ac, String password) throws Exception;
	
	/**
	 * Renova um certificado (reemissao).
	 * @param acEmissora Objeto que representa a AC emissora (issuer).
	 * @param cert Certificado a ser renovado.
	 * @param acPassword Senha da AC.
	 * @throws Exception
	 */
	public void renovarCert(Ca acEmissora, Certificado cert,
			String acPassword) throws Exception;
	
	/**
	 * Atualiza o status de todos os certificados expirados,
	 *   no banco de dados.
	 * @throws Exception
	 */
	public void atualizarStatusCertExpirados();
	
	/**
	 * Executa um backup dos arquivos de dados da ICP.
	 * @param outFileName Nome do arquivo de backup a ser gerado.
	 * @throws Exception
	 */
	public void gerarBackup(String outFileName) throws Exception;
	
	/**
	 * Retorna o diretorio base dos dados da ICP.
	 * @return Caminho do diretorio base da ICP.
	 */
	public String getBaseDir();
	
	/**
	 * Retorna a lista de usuarios cadastrados.
	 * @return Lista de usuarios.
	 */
	public List<Usuario> listarUsuarios();

	/**
	 * Retorna a lista de usuarios cadastrados, para a role especificada.
	 * @param role Role dos usuarios.
	 * @return Lista de usuarios.
	 */
	public List<Usuario> listarUsuariosPorRole(String role);

	/**
	 * Retorna um Usuario pelo DN.
	 * @param dname Distinguished Name (DN)
	 *   do Usuario no registro do banco de dados.
	 * @return Usuario ou null se nao encontrado.
	 */
	public Usuario localizarUsuario(String dname);

	/**
	 * Retorna um Usuario pelo Username.
	 * @param username Username do Usuario no registro do banco de dados.
	 * @return Usuario ou null se nao encontrado.
	 */
	public Usuario localizarUsuarioPorUsername(String username);

	/**
	 * Adiciona um usuario ao cadastro, a partir de um objeto
	 *   {@link InputStream} (que pode apontar para um arquivo de certificado).
	 * @param istream Objeto InputStream.
	 * @param role Role do usuario a ser adicionado.
	 * @return Usuario adicionado.
	 * @throws Exception
	 */
	public Usuario adicionarUsuario(InputStream istream, String role) throws Exception;

	/**
	 * Adiciona um administrador ao cadastro, a partir de um objeto
	 *   {@link InputStream} (que pode apontar para um arquivo de certificado).
	 * @param istream Objeto InputStream.
	 * @return Usuario adicionado.
	 * @throws Exception
	 */
	public Usuario adicionarAdmin(InputStream istream) throws Exception;
	
	/**
	 * Remove um usuario do cadastro.
	 * @param dname Distinguished Name (DN) do usuario no banco de dados.
	 * @throws Exception
	 */
	public void removerUsuario(String dname) throws Exception;
}
