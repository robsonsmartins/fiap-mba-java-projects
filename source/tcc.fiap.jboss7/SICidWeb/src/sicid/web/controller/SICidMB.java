package sicid.web.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import sicid.bean.Usuario;
import sicid.model.ISICidEngine;
import com.robsonmartins.fiap.tcc.util.FacesUtil;

/**
 * Managed Bean responsavel por fornecer o acesso 'as funcionalidades de
 *   administracao do Servico de Identificacao do Cidadao (SICid)
 *   da camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@ManagedBean(name="sicid")
@SessionScoped
@SuppressWarnings("serial")
public class SICidMB implements Serializable {
	
	/* motor do SICid */
	@EJB
	private ISICidEngine sicidEngine;
	/* controller do cadastro de certificados confiaveis */
	private CertificadoConfiavelMB certMB;
	/* controller do cadastro de aplicacoes confiaveis */
	private ConsumidorConfiavelMB appMB;
	/* controller do cadastro de usuarios */
	private UsuarioMB usuarioMB;
	/* controller do cadastro nacional do cidadao */
	private CidadaoMB cidadaoMB;
	
	/* inicializa ManagedBeans */
	@PostConstruct
	protected void init() {
		certMB      = new CertificadoConfiavelMB(sicidEngine);
		appMB       = new ConsumidorConfiavelMB (sicidEngine);
		usuarioMB   = new UsuarioMB             (sicidEngine);
		cidadaoMB   = new CidadaoMB             (sicidEngine);
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
	 * Obtem o Usuario logado.
	 * @return Objeto que representa o Usuario logado.
	 */
	public Usuario getUsuarioLogin() {
		return usuarioMB.getUsuarioLogin();
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
	 * Retorna o controller que gerencia o cadastro de
	 *   Certificados Confiaveis.
	 * @return Controller do cadastro de Certificados Confiaveis.
	 */
	public CertificadoConfiavelMB getCert() {
		return certMB;
	}

	/**
	 * Configura o controller que gerencia o cadastro de
	 *   Certificados Confiaveis.
	 * @param cert Controller do cadastro de Certificados Confiaveis.
	 */
	public void setCert(CertificadoConfiavelMB cert) {
		this.certMB = cert;
	}

	/**
	 * Retorna o controller que gerencia o cadastro de
	 *   Aplicacoes Confiaveis.
	 * @return Controller do cadastro de Aplicacoes Confiaveis.
	 */
	public ConsumidorConfiavelMB getApp() {
		return appMB;
	}

	/**
	 * Configura o controller que gerencia o cadastro de
	 *   Aplicacoes Confiaveis.
	 * @param app Controller do cadastro de Aplicacoes Confiaveis.
	 */
	public void setApp(ConsumidorConfiavelMB app) {
		this.appMB = app;
	}

	/**
	 * Retorna o controller que gerencia o cadastro de Usuarios.
	 * @return Controller do cadastro de Usuarios.
	 */
	public UsuarioMB getUsuario() {
		return usuarioMB;
	}

	/**
	 * Configura o controller que gerencia o cadastro de Usuarios.
	 * @param usuario Controller do cadastro de Usuarios.
	 */
	public void setUsuario(UsuarioMB usuario) {
		this.usuarioMB = usuario;
	}

	/**
	 * Retorna o controller que gerencia o cadastro de Cidadaos.
	 * @return Controller do cadastro de Cidadaos.
	 */
	public CidadaoMB getCidadao() {
		return cidadaoMB;
	}

	/**
	 * Configura o controller que gerencia o cadastro de Cidadaos.
	 * @param cidadao Controller do cadastro de Cidadaos.
	 */
	public void setCidadao(CidadaoMB cidadao) {
		this.cidadaoMB = cidadao;
	}
	
}
