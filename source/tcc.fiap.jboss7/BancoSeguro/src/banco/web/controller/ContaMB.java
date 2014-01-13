package banco.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.event.FileUploadEvent;

import com.robsonmartins.fiap.tcc.util.FacesUtil;

import banco.bean.Conta;
import banco.model.IBancoSeguroEngine;

/**
 * Classe responsavel por fornecer o acesso 'as funcionalidades do
 *   cadastro de contas do Banco Seguro 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class ContaMB extends AbstractBancoSeguroMB {
	
	/* objeto conta selecionado na camada de visualizacao */
	private Conta contaSelecionada;
	
	/* lista de contas */
	private List<Conta> contas; 

	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" do Banco Seguro (camada model).
	 */
	public ContaMB(IBancoSeguroEngine engine) {
		super(engine);
		contaUsuarioLogado = super.getContaUsuarioLogin();
		contas = new ArrayList<Conta>();
		atualizarListaContas();
	}
	
	/** 
	 * Obtem a Conta associada ao Usuario logado (se houver uma).
	 * @return Objeto que representa a Conta associada ao logado,
	 *   ou null se usuario nao possuir conta.
	 */
	@Override
	public Conta getContaUsuarioLogin() {
		return contaUsuarioLogado;
	}

	/**
	 * Retorna uma lista de contas cadastradas.
	 * @return Lista de contas.
	 */
	public List<Conta> getListarContas() {
		return contas;
	}
	
	/**
	 * Action para redirecionar ao cadastro de contas.
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String goToContas() {
		contaSelecionada = null;
		contaUsuarioLogado = super.getContaUsuarioLogin();
		atualizarListaContas();
		return "contas";
	}
	
	/**
	 * Event Handler do componente {@link FileUpload}, para adicionar uma
	 *   conta ao cadastro. 
	 * @param event Objeto Event do componente FileUpload (Primefaces).
	 */
	public void uploadCertCliente(FileUploadEvent event) {
		try {
			bancoEngine.adicionarConta(event.getFile().getInputstream());
			FacesUtil.addFacesMessage("Nova conta aberta com sucesso.",
					null, FacesMessage.SEVERITY_INFO);
			contaUsuarioLogado = super.getContaUsuarioLogin();
			atualizarListaContas();
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao abrir a nova conta.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}
	
	/**
	 * Action para remover uma conta do cadastro.
	 */
	public void removerConta() {
		try {
			bancoEngine.removerConta(contaSelecionada.getNumeroConta());
			FacesUtil.addFacesMessage("Conta fechada com sucesso.",
					null, FacesMessage.SEVERITY_INFO);
			contaSelecionada = null;
			contaUsuarioLogado = super.getContaUsuarioLogin();
			atualizarListaContas();
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao fechar a conta.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}
	
	/**
	 * Retorna a selecao da conta.
	 * @return Conta selecionada.
	 */
	public Conta getContaSelecionada() {
		return contaSelecionada;
	}

	/**
	 * Armazena a selecao da conta.
	 * @param contaSelecionada Conta selecionada.
	 */
	public void setContaSelecionada(Conta contaSelecionada) {
		this.contaSelecionada = contaSelecionada;
	}
	
	/* Atualiza lista de contas. */
	private void atualizarListaContas() {
		contas.clear();
		contas.addAll(bancoEngine.listarContas());
	}
}
