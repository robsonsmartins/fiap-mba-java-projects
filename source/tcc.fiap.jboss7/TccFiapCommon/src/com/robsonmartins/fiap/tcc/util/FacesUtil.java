package com.robsonmartins.fiap.tcc.util;

import java.security.Principal;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Encapsula funcionalidades uteis no contexto de JSF (Java Server Faces).
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class FacesUtil {

	/** 
	 * Obtem o objeto que representa a requisicao HTTP
	 *  a partir do contexto do servlet.
	 * @return Objeto da requisicao HTTP.
	 */
	public static HttpServletRequest getRequest() {
		HttpServletRequest request =
			(HttpServletRequest) FacesContext.getCurrentInstance()
				.getExternalContext().getRequest();
		return request;
	}
	
	/**
	 * Retorna o username do Usuario logado na sessao HTTP. 
	 * @return Username do usuario logado na sessao corrente.
	 */
	public static String getUserPrincipalName() {
		Principal principal = null; 
		HttpServletRequest request = getRequest();
		if (request != null) { principal = request.getUserPrincipal(); }
		return (principal != null) ? principal.getName() : null;	
	}
	
	/**
	 * Adiciona uma mensagem no contexto do JSF.
	 * @param summary Texto de sumario da mensagem.
	 * @param detail Texto de detalhe da mensagem.
	 * @param severity Severidade da mensagem.
	 */
	public static void addFacesMessage(String summary, String detail, Severity severity) {
		FacesMessage facesMessage = new FacesMessage(severity, summary, detail);  
		FacesContext.getCurrentInstance().addMessage(null, facesMessage);
	}
	
}
