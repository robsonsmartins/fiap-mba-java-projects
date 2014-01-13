package banco.model;

import java.io.InputStream;
import java.util.List;

import javax.ejb.Local;

import banco.bean.ConsumidorConfiavel;
import banco.bean.Conta;
import banco.bean.Extrato;
import banco.bean.Usuario;

/**
 * Motor (Model) do Banco Seguro.<br/>
 * Implementa as funcionalidades da aplicacao.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Local
public interface IBancoSeguroEngine {

	/** Role para acessar a aplicacao web do Banco Seguro como gerente. */
	public static final String BANCOSEGURO_GERENTE_ACCESS_ROLE = "gerente";
	/** Role para acessar a aplicacao web do Banco Seguro como cliente. */
	public static final String BANCOSEGURO_CLIENTE_ACCESS_ROLE = "cliente";
	/** Role para acessar o servico Banco Seguro */
	public static final String BANCOSEGURO_SERVICE_ACCESS_ROLE = "wsuser";

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
	 * Retorna um Usuario pelo CPF.
	 * @param cpf CPF do Usuario no registro do banco de dados.
	 * @return Usuario ou null se nao encontrado.
	 */
	public Usuario localizarUsuarioPorCpf(String cpf);
	
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
	 * Adiciona um gerente ao cadastro, a partir de um objeto
	 *   {@link InputStream} (que pode apontar para um arquivo de certificado).
	 * @param istream Objeto InputStream.
	 * @return Usuario adicionado.
	 * @throws Exception
	 */
	public Usuario adicionarGerente(InputStream istream) throws Exception;

	/**
	 * Adiciona um cliente ao cadastro, a partir de um objeto
	 *   {@link InputStream} (que pode apontar para um arquivo de certificado).
	 * @param istream Objeto InputStream.
	 * @return Usuario adicionado.
	 * @throws Exception
	 */
	public Usuario adicionarCliente(InputStream istream) throws Exception;
	
	/**
	 * Remove um usuario do cadastro.
	 * @param dname Distinguished Name (DN) do usuario no banco de dados.
	 * @throws Exception
	 */
	public void removerUsuario(String dname) throws Exception;

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
	 * Retorna a lista de contas cadastradas.
	 * @return Lista de contas.
	 */
	public List<Conta> listarContas();

	/**
	 * Retorna uma Conta pelo numero de conta.
	 * @param numeroConta Numero da conta.
	 * @return Conta ou null se nao encontrada.
	 */
	public Conta localizarConta(long numeroConta);

	/**
	 * Retorna uma Conta pelo CPF do cliente.
	 * @param cpfCliente CPF do cliente.
	 * @return Conta ou null se nao encontrada.
	 */
	public Conta localizarContaPorCpf(String cpfCliente);

	/**
	 * Adiciona uma conta ao cadastro, a partir de um objeto
	 *   {@link InputStream} (que pode apontar para um arquivo de certificado).
	 * @param istream Objeto InputStream.
	 * @return Conta adicionada.
	 * @throws Exception
	 */
	public Conta adicionarConta(InputStream istream) throws Exception;

	/**
	 * Remove uma conta do cadastro.
	 * @param numeroConta Numero da conta.
	 * @throws Exception
	 */
	public void removerConta(long numeroConta) throws Exception;

	/**
	 * Realiza o deposito de um valor em uma conta.
	 * @param conta Conta a receber o deposito.
	 * @param valor Valor a ser depositado.
	 * @return Conta com o saldo atualizado.
	 * @throws Exception
	 */
	public Conta depositarConta(Conta conta, float valor) throws Exception;

	/**
	 * Realiza o deposito de um valor em uma conta,
	 *   localizada a partir do certificado digital do cliente.
	 * @param content Conteudo do certificado digital do cliente, codificado em Base64.
	 * @param valor Valor a ser depositado.
	 * @return Conta com o saldo atualizado.
	 * @throws Exception
	 */
	public Conta depositarConta(String content, float valor) throws Exception;
	
	/**
	 * Realiza o saque de um valor em uma conta. 
	 * @param conta Conta a ser sacada.
	 * @param valor Valor a ser sacado.
	 * @return Conta com o saldo atualizado.
	 * @throws Exception
	 */
	public Conta sacarConta(Conta conta, float valor) throws Exception;
	
	/**
	 * Realiza o saque de um valor em uma conta,
	 *   localizada a partir do certificado digital do cliente.
	 * @param content Conteudo do certificado digital do cliente, codificado em Base64.
	 * @param valor Valor a ser sacado.
	 * @return Conta com o saldo atualizado.
	 * @throws Exception
	 */
	public Conta sacarConta(String content, float valor) throws Exception;

	/**
	 * Realiza o pagamento de um titulo em uma conta. 
	 * @param conta Conta a ser sacada.
	 * @param valor Valor a ser pago.
	 * @return Conta com o saldo atualizado.
	 * @throws Exception
	 */
	public Conta pagarConta(Conta conta, float valor) throws Exception;

	/**
	 * Realiza o pagamento de um titulo em uma conta,
	 *   localizada a partir do certificado digital do cliente.
	 * @param content Conteudo do certificado digital do cliente, codificado em Base64.
	 * @param valor Valor a ser pago.
	 * @return Conta com o saldo atualizado.
	 * @throws Exception
	 */
	public Conta pagarConta(String content, float valor) throws Exception;

	/**
	 * Realiza transferencia de valores entre contas.
	 * @param contaOrigem Conta de origem.
	 * @param contaDestino Conta de destino.
	 * @param valor Valor a ser transferido.
	 * @return Conta de origem com o saldo atualizado.
	 * @throws Exception
	 */
	public Conta transferirConta(Conta contaOrigem,
			Conta contaDestino, float valor) throws Exception;
	
	/**
	 * Realiza transferencia de valores entre contas.<br/>
	 *   A conta origem e' localizada a partir do certificado digital do cliente.
	 * @param content Conteudo do certificado digital do cliente, codificado em Base64.
	 * @param numeroContaDestino Numero da Conta de destino.
	 * @param valor Valor a ser transferido.
	 * @return Conta com o saldo atualizado.
	 * @throws Exception
	 */
	public Conta transferirConta(String content,
			long numeroContaDestino, float valor) throws Exception;
	
	/**
	 * Obtem o extrato de uma conta.
	 * @param conta Objeto que representa a conta.
	 * @return Lista contendo as entradas do extrato.
	 */
	public List<Extrato> obterExtrato(Conta conta);
	
}
