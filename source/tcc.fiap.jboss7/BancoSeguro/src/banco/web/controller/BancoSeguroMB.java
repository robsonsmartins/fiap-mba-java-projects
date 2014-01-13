package banco.web.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.robsonmartins.fiap.tcc.util.FacesUtil;

import banco.bean.Conta;
import banco.bean.Usuario;
import banco.model.IBancoSeguroEngine;

/**
 * Managed Bean responsavel por fornecer o acesso 'as funcionalidades do
 *   Banco Seguro 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@ManagedBean(name="banco")
@SessionScoped
@SuppressWarnings("serial")
public class BancoSeguroMB implements Serializable {
	
	/* motor do Banco Seguro */
	@EJB
	private IBancoSeguroEngine bancoEngine;
	/* controller do cadastro de usuarios */
	private UsuarioMB usuarioMB;
	/* controller do cadastro de app confiaveis */
	private ConsumidorConfiavelMB appMB;
	/* controller do cadastro de contas */
	private ContaMB contaMB;
	/* controller do gerenciador do banco */
	private BancoMB bancoMB;
	
	/* inicializa ManagedBeans */
	@PostConstruct
	protected void init() {
		usuarioMB   = new UsuarioMB(bancoEngine);
		appMB       = new ConsumidorConfiavelMB(bancoEngine);
		contaMB     = new ContaMB(bancoEngine);
		bancoMB     = new BancoMB(bancoEngine);
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
	 * Obtem o Usuario logado.
	 * @return Objeto que representa o Usuario logado.
	 */
	public Usuario getUsuarioLogin() {
		return usuarioMB.getUsuarioLogin();
	}
	
	/** 
	 * Obtem a Conta associada ao Usuario logado (se houver uma).
	 * @return Objeto que representa a Conta associada ao logado,
	 *   ou null se usuario nao possuir conta.
	 */
	public Conta getContaUsuarioLogin() {
		return contaMB.getContaUsuarioLogin();
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
		return "banco";
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
	 * Retorna o controller de aplicacoes confiaveis.
	 * @return Controller que gerencia aplicacoes confiaveis.
	 */
	public ConsumidorConfiavelMB getApp() {
		return appMB;
	}
	
	/**
	 * Configura o controller de aplicacoes confiaveis.
	 * @param appMB Controller que gerencia aplicacoes confiaveis.
	 */
	public void setApp(ConsumidorConfiavelMB appMB) {
		this.appMB = appMB;
	}

	/**
	 * Retorna o controller de Contas.
	 * @return Controller que gerencia Contas.
	 */
	public ContaMB getConta() {
		return contaMB;
	}
	
	/**
	 * Configura o controller de Contas.
	 * @param contaMB Controller que gerencia Contas.
	 */
	public void setConta(ContaMB contaMB) {
		this.contaMB = contaMB;
	}

	/**
	 * Retorna o controller do gerenciador do Banco.
	 * @return Controller que gerencia o Banco.
	 */
	public BancoMB getBanco() {
		return bancoMB;
	}

	/**
	 * Configura o controller do gerenciador do Banco.
	 * @param bancoMB Controller que gerencia o Banco.
	 */
	public void setBanco(BancoMB bancoMB) {
		this.bancoMB = bancoMB;
	}

}
