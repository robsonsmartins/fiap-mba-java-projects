package receita.web.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import receita.bean.Cidadao;
import receita.model.IReceitaEngine;

import com.robsonmartins.fiap.tcc.util.FacesUtil;

/**
 * Managed Bean responsavel por fornecer o acesso 'as funcionalidades da
 *   Receita Nacional 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@ManagedBean(name="receita")
@SessionScoped
@SuppressWarnings("serial")
public class ReceitaMB implements Serializable {
	
	/* motor da Receita Nacional */
	@EJB
	private IReceitaEngine receitaEngine;
	/* controller do cadastro de cidadaos */
	private CidadaoMB cidadaoMB;
	/* controller do gerenciador de tributos */
	private TributoMB tributoMB;
	
	/* inicializa ManagedBeans */
	@PostConstruct
	protected void init() {
		cidadaoMB = new CidadaoMB(receitaEngine);
		tributoMB = new TributoMB(receitaEngine);
	}

	/**
	 * Retorna o nome completo do usuario logado.
	 * @return Nome do usuario logado.
	 */
	public String getLoginName() {
		Cidadao usuario = getUsuarioLogin(); 
		return (usuario != null) ? usuario.getName() : "";
	}
	
	/** 
	 * Obtem o Usuario logado.
	 * @return Objeto que representa o Usuario logado.
	 */
	public Cidadao getUsuarioLogin() {
		return cidadaoMB.getUsuarioLogin();
	}
	
	/**
	 * Action para realizar logoff.
	 * @return Action redirecionada apos logoff.
	 */
	public String logoff() {
		HttpServletRequest request = FacesUtil.getRequest();
		try {
			request.logout();
		} catch (ServletException e) { }
		request.getSession().invalidate();
		return "cidadao";
	}

	/**
	 * Retorna o controller de Cidadaos.
	 * @return Controller que gerencia Cidadaos.
	 */
	public CidadaoMB getCidadao() {
		return cidadaoMB;
	}
	
	/**
	 * Configura o controller de Cidadaos.
	 * @param cidadaoMB Controller que gerencia Cidadaos.
	 */
	public void setCidadao(CidadaoMB cidadaoMB) {
		this.cidadaoMB = cidadaoMB;
	}

	/**
	 * Retorna o controller de Tributos.
	 * @return Controller que gerencia Tributos.
	 */
	public TributoMB getTributo() {
		return tributoMB;
	}

	/**
	 * Configura o controller de Tributos.
	 * @param tributoMB Controller que gerencia Tributos.
	 */
	public void setTributoMB(TributoMB tributoMB) {
		this.tributoMB = tributoMB;
	}
}
