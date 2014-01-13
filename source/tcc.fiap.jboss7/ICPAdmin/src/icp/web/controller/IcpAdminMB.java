package icp.web.controller;

import icp.bean.Usuario;
import icp.model.IIcpAdmin;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.robsonmartins.fiap.tcc.util.FacesUtil;

/**
 * Managed Bean responsavel por fornecer o acesso 'as funcionalidades de
 *   Gerenciamento de uma ICP (PKI) 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@ManagedBean(name="icpadmin")
@SessionScoped
@SuppressWarnings("serial")
public class IcpAdminMB implements Serializable {
	
	/* motor do ICPAdmin */
	@EJB
	private IIcpAdmin icpAdmin;
	/* controller do cadastro de usuarios */
	private UsuarioMB usuarioMB;
	/* controller do gerenciamento da icp */
	private IcpBrasilMB icpBrasilMB;
	
	/* inicializa ManagedBeans */
	@PostConstruct
	protected void init() {
		usuarioMB   = new UsuarioMB  (icpAdmin);
		icpBrasilMB = new IcpBrasilMB(icpAdmin);
	}

	/**
	 * Retorna o nome completo do usuario logado.
	 * @return Nome do usuario logado.
	 */
	public String getLoginName() {
		Usuario usuario = getUsuarioLogin(); 
		return (usuario != null) ? usuario.getName() : "";
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
		return "acraiz";
	}
	
	/** 
	 * Obtem o Usuario logado.
	 * @return Objeto que representa o Usuario logado.
	 */
	public Usuario getUsuarioLogin() {
		return usuarioMB.getUsuarioLogin();
	}
	
	/**
	 * Retorna o controller de Usuarios.
	 * @return Controller que gerencia Usuarios.
	 */
	public UsuarioMB getUsuario() {
		return usuarioMB;
	}
	
	/**
	 * Configura o controller de Usuarios.
	 * @param usuarioMB Controller que gerencia Usuarios.
	 */
	public void setUsuario(UsuarioMB usuarioMB) {
		this.usuarioMB = usuarioMB;
	}
	
	/**
	 * Retorna o controller de Gerenciamento da ICP.
	 * @return Controller que gerencia a ICP.
	 */
	public IcpBrasilMB getIcp() {
		return icpBrasilMB;
	}
	
	/**
	 * Configura o controller de Gerenciamento da ICP.
	 * @param icpBrasilMB Controller que gerencia a ICP.
	 */
	public void setIcp(IcpBrasilMB icpBrasilMB) {
		this.icpBrasilMB = icpBrasilMB;
	}
}
