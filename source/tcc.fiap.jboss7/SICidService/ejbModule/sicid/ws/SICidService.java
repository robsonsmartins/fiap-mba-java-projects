package sicid.ws;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;

import org.jboss.security.annotation.SecurityDomain;
import org.jboss.ws.api.annotation.WebContext;

import sicid.bean.CertificadoStatus;
import sicid.bean.Cidadao;
import sicid.bean.CidadaoCollecion;
import sicid.model.ISICidEngine;

/**
 * Implementacao do Servico de Identificacao do Cidadao (SICid).
 * @author Robson Martins (robson@robsonmartins.com)
 */
@WebService(serviceName = "SICidService", portName = "SICidPort",
	targetNamespace = "http://ws.sicid/", endpointInterface = "sicid.ws.ISICidService")

@SecurityDomain("SICidService")
@RolesAllowed("wsuser")

//@WebContext(contextRoot = "/sicid/service", urlPattern = "/sicid",
//	transportGuarantee = "CONFIDENTIAL", authMethod = "BASIC", secureWSDLAccess = true)

@WebContext(contextRoot = "/sicid/service", urlPattern = "/sicid",
	authMethod = "BASIC", secureWSDLAccess = true)

@Stateless
public class SICidService implements ISICidService {

	/* Instancia do motor (MVC 'model') do SICid */
	@EJB
	private ISICidEngine sicidEngine;
	
	@Override
	public CertificadoStatus validarCertificado(String content) {
		return sicidEngine.validarCertificado(content);
	}

	@Override
	public Cidadao consultarCidadao(String content) {
		return sicidEngine.consultarCidadao(content);
	}
	
	@Override
	public CidadaoCollecion listarCidadaos() {
		CidadaoCollecion cidadaos = new CidadaoCollecion();
		cidadaos.setCidadaos(sicidEngine.listarCidadaos());
		return cidadaos;
	}

}
