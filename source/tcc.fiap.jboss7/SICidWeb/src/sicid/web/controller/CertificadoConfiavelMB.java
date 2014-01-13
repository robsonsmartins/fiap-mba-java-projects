package sicid.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.event.FileUploadEvent;

import sicid.bean.CertificadoConfiavel;
import sicid.model.ISICidEngine;

import com.robsonmartins.fiap.tcc.util.FacesUtil;
import com.robsonmartins.fiap.tcc.util.JBossUtil;

/**
 * Classe responsavel por fornecer o acesso 'as funcionalidades do
 *   cadastro de certificados confiaveis do Servico de Identificacao
 *   do Cidadao (SICid) 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class CertificadoConfiavelMB extends AbstractSICidMB {
	
	/* objeto certificado selecionado na camada de visualizacao */
	private CertificadoConfiavel certSelecionado;
	
	/* lista de certificados confiaveis cadastrados no SICid */
	private List<CertificadoConfiavel> certs;
	
	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" do SICid (camada model).
	 */
	public CertificadoConfiavelMB(ISICidEngine engine) {
		super(engine);
		certs = new ArrayList<CertificadoConfiavel>();
		atualizarListaCertificados();
	}
	
	/**
	 * Retorna uma lista de certificados confiaveis cadastrados.
	 * @return Lista de certificados confiaveis.
	 */
	public List<CertificadoConfiavel> getListarCertConfiaveis() {
		return certs;
	}
	
	/**
	 * Action para redirecionar ao cadastro de certificados confiaveis.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public String goToTrustedCert() {
		certSelecionado = null;
		atualizarListaCertificados();
		return "trustedCert";
	}
	
	/**
	 * Event Handler do componente {@link FileUpload}, para adicionar um
	 *   certificado confiavel ao cadastro. 
	 * @param event Objeto Event do componente FileUpload (Primefaces).
	 */
	public void uploadCertConfiavel(FileUploadEvent event) {
		try {
			sicidEngine.adicionarCertConfiavel(event.getFile().getInputstream());
			FacesUtil.addFacesMessage("Certificado adicionado com sucesso.", null, FacesMessage.SEVERITY_INFO);
			atualizarListaCertificados();
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao adicionar o certificado.", e.getLocalizedMessage(),
					FacesMessage.SEVERITY_ERROR);
		}
	}
	
	/**
	 * Action para remover um certificado confiavel do cadastro.
	 */
	public void removerCertConfiavel() {
		if (JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				sicidEngine.removerCertConfiavel(certSelecionado.getId());
				FacesUtil.addFacesMessage("Certificado removido com sucesso.", null, FacesMessage.SEVERITY_INFO);
				certSelecionado = null;
				atualizarListaCertificados();
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao remover o certificado.", e.getLocalizedMessage(),
						FacesMessage.SEVERITY_ERROR);
			}
		}
	}
	
	/**
	 * Retorna a selecao de certificado.
	 * @return Certificado selecionado.
	 */
	public CertificadoConfiavel getCertSelecionado() {
		return certSelecionado;
	}

	/**
	 * Armazena a selecao de certificado.
	 * @param certSelecionado Certificado selecionado.
	 */
	public void setCertSelecionado(CertificadoConfiavel certSelecionado) {
		this.certSelecionado = certSelecionado;
	}
	
	/* Atualiza lista de certificados confiaveis. */
	private void atualizarListaCertificados() {
		certs.clear();
		certs.addAll(sicidEngine.listarCertConfiaveis());
	}
}
