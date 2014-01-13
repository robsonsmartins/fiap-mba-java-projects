package banco.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.event.FileUploadEvent;

import banco.bean.Usuario;
import banco.model.IBancoSeguroEngine;

import com.robsonmartins.fiap.tcc.util.FacesUtil;
import com.robsonmartins.fiap.tcc.util.JBossUtil;

/**
 * Classe responsavel por fornecer o acesso 'as funcionalidades do
 *   cadastro de usuarios do Banco Seguro 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class UsuarioMB extends AbstractBancoSeguroMB {
	
	/* objeto user selecionado na camada de visualizacao */
	private Usuario userSelecionado;
	/* usuario logado */
	private Usuario usuarioLogado;
	
	/* lista de usuarios */
	private List<Usuario> usuarios;
	/* lista de gerentes */
	private List<Usuario> gerentes;

	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" do Banco Seguro (camada model).
	 */
	public UsuarioMB(IBancoSeguroEngine engine) {
		super(engine);
		usuarioLogado = super.getUsuarioLogin();
		usuarios = new ArrayList<Usuario>();
		gerentes = new ArrayList<Usuario>();
		atualizarListaUsuarios();
	}
	
	/** 
	 * Obtem o Usuario logado.
	 * @return Objeto que representa o Usuario logado.
	 */
	@Override
	public Usuario getUsuarioLogin() {
		return usuarioLogado;
	}
	
	/**
	 * Retorna uma lista de gerentes cadastradas.
	 * @return Lista de gerentes.
	 */
	public List<Usuario> getListarGerentes() {
		return gerentes;
	}
	
	/**
	 * Action para redirecionar ao cadastro de gerentes.
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String goToGerentes() {
		userSelecionado = null;
		atualizarListaUsuarios();
		return "gerentes";
	}
	
	/**
	 * Event Handler do componente {@link FileUpload}, para adicionar um
	 *   gerente ao cadastro. 
	 * @param event Objeto Event do componente FileUpload (Primefaces).
	 */
	public void uploadCertGerente(FileUploadEvent event) {
		if (JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				bancoEngine.adicionarGerente(event.getFile().getInputstream());
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
				bancoEngine.removerUsuario(userSelecionado.getDname());
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
	public Usuario getUserSelecionado() {
		return userSelecionado;
	}

	/**
	 * Armazena a selecao do usuario.
	 * @param userSelecionado Usuario selecionado.
	 */
	public void setUserSelecionado(Usuario userSelecionado) {
		this.userSelecionado = userSelecionado;
	}

	/* Atualiza lista de usuarios. */
	private void atualizarListaUsuarios() {
		usuarios.clear();
		usuarios.addAll(bancoEngine.listarUsuarios());

		gerentes.clear();
		gerentes.addAll(bancoEngine.listarUsuariosPorRole(
				IBancoSeguroEngine.BANCOSEGURO_GERENTE_ACCESS_ROLE));
	}
}
