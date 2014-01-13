package receita.model;

import java.io.InputStream;
import java.util.List;

import javax.ejb.Local;

import receita.bean.Cidadao;
import receita.bean.Tributo;

/**
 * Motor (Model) da Receita Nacional.<br/>
 * Implementa as funcionalidades da aplicacao.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Local
public interface IReceitaEngine {

	/** Role para acessar a aplicacao web da Receita Nacional como 'governo'. */
	public static final String RECEITA_GOVERNO_ACCESS_ROLE = "governo";
	/** Role para acessar a aplicacao  web da Receita Nacional como 'cidadao'. */
	public static final String RECEITA_CIDADAO_ACCESS_ROLE = "cidadao";
	
	/**
	 * Retorna a lista de usuarios cadastrados.
	 * @return Lista de usuarios.
	 */
	public List<Cidadao> listarUsuarios();

	/**
	 * Retorna a lista de usuarios cadastrados, para a role especificada.
	 * @param role Role dos usuarios.
	 * @return Lista de usuarios.
	 */
	public List<Cidadao> listarUsuariosPorRole(String role);

	/**
	 * Retorna um Usuarios pelo DN.
	 * @param dname Distinguished Name (DN)
	 *   do Usuario no registro do banco de dados.
	 * @return Usuario ou null se nao encontrado.
	 */
	public Cidadao localizarUsuario(String dname);

	/**
	 * Retorna um Usuario pelo RIC.
	 * @param ric RIC do Usuario no registro do banco de dados.
	 * @return Usuario ou null se nao encontrado.
	 */
	public Cidadao localizarUsuarioPorRic(String ric);
	
	/**
	 * Adiciona um usuario ao cadastro, a partir de um objeto
	 *   {@link InputStream} (que pode apontar para um arquivo de certificado).
	 * @param istream Objeto InputStream.
	 * @param role Role do usuario a ser adicionado.
	 * @return Usuario adicionado.
	 * @throws Exception
	 */
	public Cidadao adicionarUsuario(InputStream istream, String role) throws Exception;

	/**
	 * Adiciona um admin ao cadastro, a partir de um objeto
	 *   {@link InputStream} (que pode apontar para um arquivo de certificado).
	 * @param istream Objeto InputStream.
	 * @return Usuario adicionado.
	 * @throws Exception
	 */
	public Cidadao adicionarAdmin(InputStream istream) throws Exception;

	/**
	 * Adiciona um cidadao ao cadastro, a partir de um objeto
	 *   {@link InputStream} (que pode apontar para um arquivo de certificado).
	 * @param istream Objeto InputStream.
	 * @return Cidadao adicionado.
	 * @throws Exception
	 */
	public Cidadao adicionarCidadao(InputStream istream) throws Exception;
	
	/**
	 * Remove um usuario do cadastro.
	 * @param dname Distinguished Name (DN) do usuario no banco de dados.
	 * @throws Exception
	 */
	public void removerUsuario(String dname) throws Exception;

	/**
	 * Retorna a lista de tributos.
	 * @return Lista de tributos.
	 */
	public List<Tributo> listarTributos();
	
	/**
	 * Retorna o tributo de um cidadao.
	 * @param cidadao Cidadao associado ao tributo.
	 * @return Tributo do cidadao.
	 */
	public Tributo localizarTributoPorCidadao(Cidadao cidadao);

	/**
	 * Calcula tributos e atribui a todos os cidadaos cadastrados no SICid.
	 * @throws Exception
	 */
	public void calcularTributos() throws Exception;
	
	/**
	 * Realiza o pagamento de um tributo online, via BancoSeguro.
	 * @param tributo Tributo a ser pago.
	 * @param certContent Conteudo do certificado do cliente do banco, codificado em Base64.
	 * @throws Exception
	 */
	public void pagarTributoOnline(Tributo tributo, String certContent) throws Exception;
}
