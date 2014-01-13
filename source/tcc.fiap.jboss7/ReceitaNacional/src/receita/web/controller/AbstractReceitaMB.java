package receita.web.controller;

import java.io.Serializable;

import receita.bean.Cidadao;
import receita.model.IReceitaEngine;

import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;
import com.robsonmartins.fiap.tcc.util.FacesUtil;

/**
 * Classe base abstrata, responsavel por fornecer o acesso 'as funcionalidades
 *   da Receita Nacional 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public abstract class AbstractReceitaMB implements Serializable {
	
	/* acao selecionada na camada de visualizacao */
	protected String actionSelecionada;
	/* motor da Receita Nacional */
	protected IReceitaEngine receitaEngine;

	/* armazena usuario logado */
	protected String usernameLogado;
	protected Cidadao usuarioLogado;

	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" da Receita Nacional (camada model).
	 */
	public AbstractReceitaMB(IReceitaEngine engine) {
		receitaEngine = engine;
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
	protected Cidadao getUsuarioLogin() {
		String username = FacesUtil.getUserPrincipalName();
		if (username != null && !username.equals(usernameLogado)) {
			usernameLogado = username;
			String dname = null;
			try {
				dname = CertificadoSerializador.getDNByCertStr(usernameLogado);
				usuarioLogado = receitaEngine.localizarUsuario(dname);
			} catch (Exception e) { }
			if (usuarioLogado == null) {
				usuarioLogado = new Cidadao();
				usuarioLogado.setRic("00000000000");
				usuarioLogado.setName("Administrador");
				usuarioLogado.setRole("governo");
			}
		} else if (username == null) {
			usuarioLogado = null;
		}
		return usuarioLogado;
	}
}
