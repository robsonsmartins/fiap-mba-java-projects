package sicid.web.controller;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.event.FileUploadEvent;

import sicid.bean.Cidadao;
import sicid.bean.DocumentoRG;
import sicid.bean.DocumentoTitulo;
import sicid.model.ISICidEngine;

import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;
import com.robsonmartins.fiap.tcc.util.FacesUtil;
import com.robsonmartins.fiap.tcc.util.JBossUtil;

/**
 * Classe responsavel por fornecer o acesso 'as funcionalidades do
 *   Cadastro Nacional do Cidadao, do Servico de Identificacao
 *   do Cidadao (SICid) 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class CidadaoMB extends AbstractSICidMB {
	
	/* objeto cidadao selecionado na camada de visualizacao */
	private Cidadao cidadaoSelecionado;
	/* objeto que representa um novo cidadao */
	private Cidadao newCidadao;
	
	/* lista de cidadaos cadastrados no SICid */
	private List<Cidadao> cidadaos;
	
	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" do SICid (camada model).
	 */
	public CidadaoMB(ISICidEngine engine) {
		super(engine);
		cidadaos = new ArrayList<Cidadao>();
		atualizarListaCidadaos();
		criaNovoCidadao();
	}
	
	/**
	 * Retorna uma lista de cidadaos cadastrados.
	 * @return Lista de cidadaos.
	 */
	public List<Cidadao> getListarCidadaos() {
		return cidadaos;
	}
	
	/**
	 * Action para redirecionar ao cadastro de cidadaos.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public String goToCidadao() {
		cidadaoSelecionado = null;
		atualizarListaCidadaos();
		return "cidadao";
	}
	
	/**
	 * Action para redirecionar 'a pagina de novo cidadao.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public String goToNovoCidadao() {
		cidadaoSelecionado = null;
		criaNovoCidadao();
		atualizarListaCidadaos();
		return "newcidadao";
	}
	
	/**
	 * Action para redirecionar 'a pagina de editar cidadao.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public String goToEditarCidadao() {
		newCidadao = cidadaoSelecionado.clone();
		if (newCidadao == null) { criaNovoCidadao(); }
		atualizarListaCidadaos();
		return "newcidadao";
	}

	/**
	 * Event Handler do componente {@link FileUpload}, para adicionar um
	 *   certificado de cidadao ao cadastro. 
	 * @param event Objeto Event do componente FileUpload (Primefaces).
	 */
	public void uploadCertCidadao(FileUploadEvent event) {
		try {
			InputStream istream = event.getFile().getInputstream();
			X509Certificate cert = 
				CertificadoSerializador.loadCertFromStream(istream);
			String content =
				CertificadoSerializador.certToStr(cert);
			newCidadao = sicidEngine.getCidadaoInfoFromCert(content);
			if (newCidadao == null) {
				criaNovoCidadao();
			}
			
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao obter informa\u00E7\u00F5es do certificado.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}
	
	/**
	 * Action para adicionar um cidadao ao cadastro.
	 */
	public String adicionarCidadao() {
		try {
			if (newCidadao == null) {
				throw new NullPointerException("Novo cidad\u00E3o inv\u00E1lido.");
			}
			sicidEngine.adicionarCidadao(newCidadao);
			FacesUtil.addFacesMessage("Cidad\u00E3o adicionado com sucesso.",
					null, FacesMessage.SEVERITY_INFO);
			criaNovoCidadao();
			atualizarListaCidadaos();
			return goToCidadao();
			
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao adicionar o cidad\u00E3o.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			return null;
		}
	}
	
	/**
	 * Action para remover um cidadao do cadastro.
	 */
	public void removerCidadao() {
		if (JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				sicidEngine.removerCidadao(cidadaoSelecionado.getDname());
				FacesUtil.addFacesMessage("Cidad\u00E3o removido com sucesso.", null, FacesMessage.SEVERITY_INFO);
				cidadaoSelecionado = null;
				atualizarListaCidadaos();
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao remover o cidad\u00E3o.", e.getLocalizedMessage(),
						FacesMessage.SEVERITY_ERROR);
			}
		}
	}
	
	/**
	 * Retorna a selecao de cidadao.
	 * @return Cidadao selecionado.
	 */
	public Cidadao getCidadaoSelecionado() {
		return cidadaoSelecionado;
	}

	/**
	 * Armazena a selecao de cidadao.
	 * @param cidadaoSelecionado Cidadao selecionado.
	 */
	public void setCidadaoSelecionado(Cidadao cidadaoSelecionado) {
		this.cidadaoSelecionado = cidadaoSelecionado;
	}
	
	/**
	 * Retorna o novo cidadao.
	 * @return Novo cidadao.
	 */
	public Cidadao getNewCidadao() {
		return newCidadao;
	}

	/**
	 * Armazena o novo cidadao.
	 * @param newCidadao Novo cidadao.
	 */
	public void setNewCidadao(Cidadao newCidadao) {
		this.newCidadao = newCidadao;
	}

	/**
	 * Cria um objeto representando um novo cidadao.
	 */
	public void criaNovoCidadao() {
		this.newCidadao = new Cidadao();
		this.newCidadao.setRg(new DocumentoRG());
		this.newCidadao.setTitulo(new DocumentoTitulo());
	}
	
	/* Atualiza lista de cidadaos. */
	private void atualizarListaCidadaos() {
		cidadaos.clear();
		cidadaos.addAll(sicidEngine.listarCidadaos());
	}
}
