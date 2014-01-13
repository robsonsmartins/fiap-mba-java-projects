package banco.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import banco.bean.StatusOperacao;

/**
 * Interface do Servico Web do Banco Seguro.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@WebService(name="BancoSeguroService")
@SOAPBinding(style=Style.DOCUMENT)
public interface IBancoSeguroService {
	
	/**
	 * Efetua o pagamento de um titulo em uma conta.
	 * @param content Conteudo do certificado digital do cliente, codificado em Base64.
	 * @param valor Valor a ser pago.
	 * @return Status da operacao.
	 */
	@WebMethod
	@WebResult(name="status")
	public StatusOperacao efetuarPagamento(
			@WebParam(name="certificado") String content,
			@WebParam(name="valor") Float valor);
	
	/**
	 * Efetua o saque de um valor em uma conta.
	 * @param content Conteudo do certificado digital do cliente, codificado em Base64.
	 * @param valor Valor a ser sacado.
	 * @return Status da operacao.
	 */
	@WebMethod
	@WebResult(name="status")
	public StatusOperacao efetuarSaque(
			@WebParam(name="certificado") String content,
			@WebParam(name="valor") Float valor);
	
	/**
	 * Efetua o deposito de um valor em uma conta.
	 * @param content Conteudo do certificado digital do cliente, codificado em Base64.
	 * @param valor Valor a ser depositado.
	 * @return Status da operacao.
	 */
	@WebMethod
	@WebResult(name="status")
	public StatusOperacao efetuarDeposito(
			@WebParam(name="certificado") String content,
			@WebParam(name="valor") Float valor);

	/**
	 * Efetua a transferencia de um valor entre contas.
	 * @param content Conteudo do certificado digital do cliente, codificado em Base64.
	 * @param valor Valor a ser transferido.
	 * @param contaDestino Numero da Conta de destino.
	 * @return Status da operacao.
	 */
	@WebMethod
	@WebResult(name="status")
	public StatusOperacao efetuarTransferencia(
			@WebParam(name="certificado") String content,
			@WebParam(name="valor") Float valor,
			@WebParam(name="conta") Long contaDestino);
	
}
