package icp.web.controller;

import icp.model.IIcpAdmin;
import java.io.Serializable;

import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;
import com.robsonmartins.fiap.tcc.util.FacesUtil;
import icp.bean.Usuario;

/**
 * Classe base abstrata, responsavel por fornecer o acesso 'as funcionalidades
 *   de Gerenciamento de uma ICP (PKI) 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public abstract class AbstractIcpAdminMB implements Serializable {
	
	/* acao selecionada na camada de visualizacao */
	protected String actionSelecionada;
	/* motor (model) do ICPAdmin */
	protected IIcpAdmin icpAdmin;
	
	/* armazena usuario logado */
	protected String usernameLogado;
	protected Usuario usuarioLogado;
	
	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" do ICPAdmin (camada model).
	 */
	public AbstractIcpAdminMB(IIcpAdmin engine) {
		icpAdmin = engine;
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
				usuarioLogado = icpAdmin.localizarUsuario(dname);
			} catch (Exception e) { }
			if (usuarioLogado == null) {
				usuarioLogado = new Usuario();
				usuarioLogado.setUsername("admin");
				usuarioLogado.setName("Administrador");
				usuarioLogado.setRole("admin");
			}
		} else if (username == null) {
			usuarioLogado = null;
		}
		return usuarioLogado;
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
}
