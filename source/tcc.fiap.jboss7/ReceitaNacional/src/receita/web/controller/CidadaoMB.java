package receita.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.event.FileUploadEvent;

import receita.bean.Cidadao;
import receita.model.IReceitaEngine;

import com.robsonmartins.fiap.tcc.util.FacesUtil;
import com.robsonmartins.fiap.tcc.util.JBossUtil;

/**
 * Classe responsavel por fornecer o acesso 'as funcionalidades do
 *   cadastro de usuarios da Receita Nacional 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class CidadaoMB extends AbstractReceitaMB {
	
	/* objeto user selecionado na camada de visualizacao */
	private Cidadao userSelecionado;
	/* usuario logado */
	private Cidadao usuarioLogado;
	
	/* lista de usuarios */
	private List<Cidadao> usuarios;
	/* lista de admins */
	private List<Cidadao> admins;

	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" da Receita Nacional (camada model).
	 */
	public CidadaoMB(IReceitaEngine engine) {
		super(engine);
		usuarioLogado = super.getUsuarioLogin();
		usuarios = new ArrayList<Cidadao>();
		admins = new ArrayList<Cidadao>();
		atualizarListaUsuarios();
	}
	
	/** 
	 * Obtem o Usuario logado.
	 * @return Objeto que representa o Usuario logado.
	 */
	@Override
	public Cidadao getUsuarioLogin() {
		return usuarioLogado;
	}
	
	/**
	 * Retorna uma lista de administradores cadastradas.
	 * @return Lista de administradores.
	 */
	public List<Cidadao> getListarAdmins() {
		return admins;
	}
	
	/**
	 * Action para redirecionar ao cadastro de administradores.
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String goToAdmins() {
		userSelecionado = null;
		atualizarListaUsuarios();
		return "admin";
	}
	
	/**
	 * Event Handler do componente {@link FileUpload}, para adicionar um
	 *   administrador ao cadastro. 
	 * @param event Objeto Event do componente FileUpload (Primefaces).
	 */
	public void uploadCertAdmin(FileUploadEvent event) {
		if (JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				receitaEngine.adicionarAdmin(event.getFile().getInputstream());
				FacesUtil.addFacesMessage("Usu\u00E1rio adicionado com sucesso.",
						null, FacesMessage.SEVERITY_INFO);
				usuarioLogado = super.getUsuarioLogin();
				atualizarListaUsuarios();
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao adicionar o usu\u00E1rio.",
						e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
		}
	}
	
	/**
	 * Action para remover um usuario do cadastro.
	 */
	public void removerUsuario() {
		if (JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				receitaEngine.removerUsuario(userSelecionado.getDname());
				FacesUtil.addFacesMessage("Usu\u00E1rio removido com sucesso.",
						null, FacesMessage.SEVERITY_INFO);
				userSelecionado = null;
				usuarioLogado = super.getUsuarioLogin();
				atualizarListaUsuarios();
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao remover o Usu\u00E1rio.",
						e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
		}
	}
	
	/**
	 * Retorna a selecao do usuario.
	 * @return Usuario selecionado.
	 */
	public Cidadao getUserSelecionado() {
		return userSelecionado;
	}

	/**
	 * Armazena a selecao do usuario.
	 * @param userSelecionado Usuario selecionado.
	 */
	public void setUserSelecionado(Cidadao userSelecionado) {
		this.userSelecionado = userSelecionado;
	}

	/* Atualiza lista de usuarios. */
	private void atualizarListaUsuarios() {
		usuarios.clear();
		usuarios.addAll(receitaEngine.listarUsuarios());

		admins.clear();
		admins.addAll(receitaEngine.listarUsuariosPorRole(
				IReceitaEngine.RECEITA_GOVERNO_ACCESS_ROLE));
	}
}
