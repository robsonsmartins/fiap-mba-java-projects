package sicid.model;

import java.io.InputStream;
import java.util.List;

import javax.ejb.Local;

import sicid.bean.CertificadoConfiavel;
import sicid.bean.CertificadoStatus;
import sicid.bean.Cidadao;
import sicid.bean.ConsumidorConfiavel;
import sicid.bean.Usuario;

/**
 * Motor do Servico de Identificacao do Cidadao (SICid).<br/>
 * Implementa as funcionalidades do servico.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Local
 public interface ISICidEngine {
	
	/** Role para acessar o servico SICid. */
	public static final String SICID_ACCESS_ROLE = "wsuser";
	/** Role para acessar a aplicacao web do SICid Admin. */
	public static final String SICIDWEB_ACCESS_ROLE = "admin";
	
	/**
	 * Valida um certificado digital.
	 * @param content Conteudo de um certificado, codificado em Base64.
	 * @return Status do certificado.
	 */
	public CertificadoStatus validarCertificado(String content);

	/**
	 * Consulta os dados cadastrais de um cidadao,
	 *   a partir de seu certificado digital.
	 * @param content Conteudo de um certificado, codificado em Base64.  
	 * @return Dados cadastrais de um cidadao.
	 */
	public Cidadao consultarCidadao(String content);

	/**
	 * Retorna a lista de cidadaos cadastrados.
	 * @return Lista de cidadaos.
	 */
	public List<Cidadao> listarCidadaos();

	/**
	 * Retorna a lista de certificados confiaveis cadastrados.
	 * @return Lista de certificados confiaveis.
	 */
	public List<CertificadoConfiavel> listarCertConfiaveis();

	/**
	 * Adiciona um certificado confiavel ao cadastro, a partir de um objeto
	 *   {@link InputStream} (que pode apontar para um arquivo de certificado).
	 * @param istream Objeto InputStream.
	 * @throws Exception
	 */
	public void adicionarCertConfiavel(InputStream istream) throws Exception;
	
	/**
	 * Remove um certificado confiavel do cadastro.
	 * @param id Id do certificado no banco de dados.
	 * @throws Exception
	 */
	public void removerCertConfiavel(long id) throws Exception;

	/**
	 * Retorna a lista de consumidores confiaveis cadastrados.
	 * @return Lista de consumidores confiaveis.
	 */
	public List<ConsumidorConfiavel> listarAppsConfiaveis();

	/**
	 * Adiciona uma aplicacao confiavel ao cadastro, a partir de um objeto
	 *   {@link InputStream} (que pode apontar para um arquivo de certificado).
	 * @param istream Objeto InputStream.
	 * @throws Exception
	 */
	public void adicionarAppConfiavel(InputStream istream) throws Exception;

	/**
	 * Remove um consumidor confiavel do cadastro.
	 * @param dname Distinguished Name (DN) do consumidor no banco de dados.
	 * @throws Exception
	 */
	public void removerAppConfiavel(String dname) throws Exception;
	
	/**
	 * Adiciona um usuario administrador ao cadastro, a partir de um objeto
	 *   {@link InputStream} (que pode apontar para um arquivo de certificado).
	 * @param istream Objeto InputStream.
	 * @throws Exception
	 */
	public void adicionarAdministrador(InputStream istream) throws Exception;

	/**
	 * Remove um usuario do cadastro.
	 * @param dname Distinguished Name (DN) do usuario no banco de dados.
	 * @throws Exception
	 */
	public void removerUsuario(String dname) throws Exception;

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
	 * Adiciona uma cidadao ao cadastro.
	 * @param cidadao Objeto com os atributos do cidadao.
	 * @throws Exception
	 */
	public void adicionarCidadao(Cidadao cidadao) throws Exception;

	/**
	 * Remove um cidadao do cadastro.
	 * @param dname Distinguished Name (DN) do cidadao no banco de dados.
	 * @throws Exception
	 */
	public void removerCidadao(String dname) throws Exception;
	
	/**
	 * Retorna informacoes de um cidadao contidas em seu certificado digital.
	 * @param content Conteudo de um certificado, codificado em Base64.
	 * @return Objeto com as informacoes de um cidadao.
	 * @throws Exception
	 */
	public Cidadao getCidadaoInfoFromCert(String content) throws Exception;
}
