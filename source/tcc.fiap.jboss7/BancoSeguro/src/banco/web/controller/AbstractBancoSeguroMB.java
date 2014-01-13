package banco.web.controller;

import java.io.Serializable;

import banco.bean.Conta;
import banco.bean.Usuario;
import banco.model.IBancoSeguroEngine;

import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;
import com.robsonmartins.fiap.tcc.util.FacesUtil;

/**
 * Classe base abstrata, responsavel por fornecer o acesso 'as funcionalidades
 *   do Banco Seguro 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public abstract class AbstractBancoSeguroMB implements Serializable {
	
	/* acao selecionada na camada de visualizacao */
	protected String actionSelecionada;
	/* motor do Banco Seguro */
	protected IBancoSeguroEngine bancoEngine;

	/* armazena usuario logado */
	protected String usernameLogado;
	protected Usuario usuarioLogado;
	/* conta do usuario logado */
	protected Conta contaUsuarioLogado;

	
	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" do Banco Seguro (camada model).
	 */
	public AbstractBancoSeguroMB(IBancoSeguroEngine engine) {
		bancoEngine = engine;
	}
	
	/**
	 * Retorna a selecao de action.
	 * @return Acao selecionada.
	 */
	public String getActionSelecionada() {
		return actionSelecionada;
	}

	/**
	 * Armazena a selecao de action.
	 * @param actionSelecionada Acao selecionada.
	 */
	public void setActionSelecionada(String actionSelecionada) {
		this.actionSelecionada = actionSelecionada;
	}
	
	/** 
	 * Obtem o Usuario logado.
	 * @return Objeto que representa o Usuario logado.
	 */
	protected Usuario getUsuarioLogin() {
		String username = FacesUtil.getUserPrincipalName();
		if (username != null && !username.equals(usernameLogado)) {
			usernameLogado = username;
			String dname = null;
			try {
				dname = CertificadoSerializador.getDNByCertStr(usernameLogado);
				usuarioLogado = bancoEngine.localizarUsuario(dname);
			} catch (Exception e) { }
			if (usuarioLogado == null) {
				usuarioLogado = new Usuario();
				usuarioLogado.setCpf("00000000000");
				usuarioLogado.setName("Gerente");
				usuarioLogado.setRole("gerente");
			}
		} else if (username == null) {
			usuarioLogado = null;
		}
		return usuarioLogado;
	}
	
	/** 
	 * Obtem a Conta associada ao Usuario logado (se houver uma).
	 * @return Objeto que representa a Conta associada ao logado,
	 *   ou null se usuario nao possuir conta.
	 */
	protected Conta getContaUsuarioLogin() {
		contaUsuarioLogado = null;
		Usuario cliente = getUsuarioLogin();
		String cpf = (cliente != null) ? cliente.getCpf() : null;
		contaUsuarioLogado = bancoEngine.localizarContaPorCpf(cpf);
		return contaUsuarioLogado;
	}

}
