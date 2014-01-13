package sicid.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.event.FileUploadEvent;

import sicid.bean.ConsumidorConfiavel;
import sicid.model.ISICidEngine;

import com.robsonmartins.fiap.tcc.util.FacesUtil;
import com.robsonmartins.fiap.tcc.util.JBossUtil;

/**
 * Classe responsavel por fornecer o acesso 'as funcionalidades do
 *   cadastro de aplicacoes confiaveis do Servico de Identificacao
 *   do Cidadao (SICid) 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class ConsumidorConfiavelMB extends AbstractSICidMB {
	
	/* objeto app selecionado na camada de visualizacao */
	private ConsumidorConfiavel appSelecionada;

	/* lista de consumidores confiaveis cadastrados no SICid */
	private List<ConsumidorConfiavel> apps;
	
	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" do SICid (camada model).
	 */
	public ConsumidorConfiavelMB(ISICidEngine engine) {
		super(engine);
		apps = new ArrayList<ConsumidorConfiavel>();
		atualizarListaApps();
	}
	
	/**
	 * Retorna uma lista de aplicacoes confiaveis cadastradas.
	 * @return Lista de aplicacoes confiaveis.
	 */
	public List<ConsumidorConfiavel> getListarAppsConfiaveis() {
		return apps;
	}
	
	/**
	 * Action para redirecionar ao cadastro de aplicacoes confiaveis.
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String goToTrustedApps() {
		appSelecionada = null;
		atualizarListaApps();
		return "trustedApps";
	}
	
	/**
	 * Event Handler do componente {@link FileUpload}, para adicionar uma
	 *   aplicacao confiavel ao cadastro. 
	 * @param event Objeto Event do componente FileUpload (Primefaces).
	 */
	public void uploadCertAppConfiavel(FileUploadEvent event) {
		try {
			sicidEngine.adicionarAppConfiavel(event.getFile().getInputstream());
			FacesUtil.addFacesMessage("Aplica\u00E7\u00E3o adicionada com sucesso.",
					null, FacesMessage.SEVERITY_INFO);
			atualizarListaApps();
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao adicionar a aplica\u00E7\u00E3o.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}

	/**
	 * Action para remover uma aplicacao confiavel do cadastro.
	 */
	public void removerAppConfiavel() {
		if (JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				sicidEngine.removerAppConfiavel(appSelecionada.getDname());
				FacesUtil.addFacesMessage("Aplica\u00E7\u00E3o removida com sucesso.",
						null, FacesMessage.SEVERITY_INFO);
				appSelecionada = null;
				atualizarListaApps();
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao remover a aplica\u00E7\u00E3o.",
						e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
		}
	}
	
	/**
	 * Retorna a selecao da aplicacao.
	 * @return Aplicacao selecionada.
	 */
	public ConsumidorConfiavel getAppSelecionada() {
		return appSelecionada;
	}

	/**
	 * Armazena a selecao da aplicacao.
	 * @param appSelecionada Aplicacao selecionada.
	 */
	public void setAppSelecionada(ConsumidorConfiavel appSelecionada) {
		this.appSelecionada = appSelecionada;
	}

	/* Atualiza lista de aplicacoes confiaveis. */
	private void atualizarListaApps() {
		apps.clear();
		apps.addAll(sicidEngine.listarAppsConfiaveis());
	}
}
