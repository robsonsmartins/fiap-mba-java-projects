package banco.ws;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;

import org.jboss.security.annotation.SecurityDomain;
import org.jboss.ws.api.annotation.WebContext;

import banco.bean.Conta;
import banco.bean.StatusOperacao;
import banco.model.IBancoSeguroEngine;

/**
 * Implementacao do Servico Web do Banco Seguro.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@WebService(serviceName = "BancoSeguroService", portName = "BancoSeguroPort",
	targetNamespace = "http://ws.bancoseguro/", endpointInterface = "banco.ws.IBancoSeguroService")

@SecurityDomain("BancoSeguroService")
@RolesAllowed("wsuser")

//@WebContext(contextRoot = "/bancoseguro/service", urlPattern = "/banco",
//	transportGuarantee = "CONFIDENTIAL", authMethod = "BASIC", secureWSDLAccess = true)

@WebContext(contextRoot = "/bancoseguro/service", urlPattern = "/banco",
	authMethod = "BASIC", secureWSDLAccess = true)

@Stateless
public class BancoSeguroService implements IBancoSeguroService {

	/* Instancia do motor (MVC 'model') do BancoSeguro */
	@EJB
	private IBancoSeguroEngine bancoEngine;
	
	@Override
	public StatusOperacao efetuarPagamento(String content, Float valor) {
		Conta conta = null;
		StatusOperacao status = new StatusOperacao();
		try {
			conta = bancoEngine.pagarConta(content, valor);
			status.setConta(conta);
			status.setValue(true);
			
		} catch (Exception e) {	
			status.setMotivo(e.getLocalizedMessage());
			status.setValue(false);
		}
		return status;
	}

	@Override
	public StatusOperacao efetuarSaque(String content, Float valor) {
		Conta conta = null;
		StatusOperacao status = new StatusOperacao();
		try {
			conta = bancoEngine.sacarConta(content, valor);
			status.setConta(conta);
			status.setValue(true);
			
		} catch (Exception e) {	
			status.setMotivo(e.getLocalizedMessage());
			status.setValue(false);
		}
		return status;
	}

	@Override
	public StatusOperacao efetuarDeposito(String content, Float valor) {
		Conta conta = null;
		StatusOperacao status = new StatusOperacao();
		try {
			conta = bancoEngine.depositarConta(content, valor);
			status.setConta(conta);
			status.setValue(true);
			
		} catch (Exception e) {	
			status.setMotivo(e.getLocalizedMessage());
			status.setValue(false);
		}
		return status;
	}

	@Override
	public StatusOperacao efetuarTransferencia(String content, Float valor,
			Long contaDestino) {

		Conta conta = null;
		StatusOperacao status = new StatusOperacao();
		try {
			conta = bancoEngine.transferirConta(content, contaDestino, valor);
			status.setConta(conta);
			status.setValue(true);
			
		} catch (Exception e) {	
			status.setMotivo(e.getLocalizedMessage());
			status.setValue(false);
		}
		return status;
	}

}
