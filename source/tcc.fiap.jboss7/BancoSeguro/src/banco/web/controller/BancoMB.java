package banco.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import com.robsonmartins.fiap.tcc.util.FacesUtil;

import banco.bean.Conta;
import banco.bean.Extrato;
import banco.model.IBancoSeguroEngine;

/**
 * Classe responsavel por fornecer o acesso 'as funcionalidades de
 *   movimentacao de contas do Banco Seguro 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class BancoMB extends AbstractBancoSeguroMB {
	
	/* valor da operacao a ser realizada */
	private Float valorOperacao;
	/* numero da conta destino (se transferencia) */
	private long numeroContaDestino;
	
	/* extrato */
	private List<Extrato> extrato;
	
	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" do Banco Seguro (camada model).
	 */
	public BancoMB(IBancoSeguroEngine engine) {
		super(engine);
		contaUsuarioLogado = super.getContaUsuarioLogin();
		extrato = new ArrayList<Extrato>();
		resetParams();
		atualizarExtrato();
	}
	
	/**
	 * Action para redirecionar a pagina de movimentacao do banco.
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String goToBanco() {
		contaUsuarioLogado = super.getContaUsuarioLogin();
		resetParams();
		atualizarExtrato();
		return "banco";
	}
	
	/**
	 * Retorna o extrato da conta do usuario logado (se houver).
	 * @return Extrato da conta do usuario logado.
	 */
	public List<Extrato> getExtratoUsuarioLogin() {
		return extrato;
	}
	
	/**
	 * Limpa os parametros de operacao de conta.
	 */
	public void resetParams() {
		valorOperacao = null;
		numeroContaDestino = 0;
	}
	
	/**
	 * Efetua um saque na conta. 
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String efetuarSaque() {
		try {
			Conta conta = super.getContaUsuarioLogin();
			bancoEngine.sacarConta(conta, valorOperacao);
			FacesUtil.addFacesMessage("Saque efetuado com sucesso.",
					null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao efetuar saque.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
		return goToBanco();
	}

	/**
	 * Efetua um deposito na conta. 
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String efetuarDeposito() {
		try {
			Conta conta = super.getContaUsuarioLogin();
			bancoEngine.depositarConta(conta, valorOperacao);
			FacesUtil.addFacesMessage("Dep\u00F3sito efetuado com sucesso.",
					null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao efetuar dep\u00F3sito.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
		return goToBanco();
	}

	/**
	 * Efetua um pagamento na conta. 
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String efetuarPagamento() {
		try {
			Conta conta = super.getContaUsuarioLogin();
			bancoEngine.pagarConta(conta, valorOperacao);
			FacesUtil.addFacesMessage("Pagamento efetuado com sucesso.",
					null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao efetuar pagamento.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
		return goToBanco();
	}

	/**
	 * Efetua uma transferencia entre contas. 
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String efetuarTransferencia() {
		try {
			Conta conta = super.getContaUsuarioLogin();
			Conta contaDestino = bancoEngine.localizarConta(numeroContaDestino);
			bancoEngine.transferirConta(conta, contaDestino, valorOperacao);
			FacesUtil.addFacesMessage("Transfer\u00EAncia efetuada com sucesso.",
					null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao efetuar transfer\u00EAncia.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
		return goToBanco();
	}
	
	/**
	 * Retorna o valor da operacao a ser efetuada.
	 * @return Valor da operacao.
	 */
	public Float getValorOperacao() {
		return valorOperacao;
	}

	/**
	 * Configura o valor da operacao a ser efetuada.
	 * @param valorOperacao Valor da operacao.
	 */
	public void setValorOperacao(Float valorOperacao) {
		this.valorOperacao = valorOperacao;
	}

	/**
	 * Retorna o numero da conta destino (numa operacao
	 *   de transferencia).
	 * @return Numero da conta de destino.
	 */
	public long getNumeroContaDestino() {
		return numeroContaDestino;
	}

	/**
	 * Configura o numero da conta destino (numa operacao
	 *   de transferencia).
	 * @param numeroContaDestino  Numero da conta de destino.
	 */
	public void setNumeroContaDestino(long numeroContaDestino) {
		this.numeroContaDestino = numeroContaDestino;
	}
	
	@Override
	public Conta getContaUsuarioLogin() {
		return contaUsuarioLogado;
	}
	
	/* Atualiza extrato. */
	private void atualizarExtrato() {
		Conta conta = getContaUsuarioLogin();
		extrato.clear();
		extrato.addAll(bancoEngine.obterExtrato(conta));
	}
}
