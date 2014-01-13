package icp.web.controller;

import icp.bean.Usuario;
import icp.model.IIcpAdmin;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.event.FileUploadEvent;

import com.robsonmartins.fiap.tcc.util.FacesUtil;
import com.robsonmartins.fiap.tcc.util.JBossUtil;

/**
 * Classe responsavel por fornecer o acesso 'as funcionalidades do
 *   cadastro de usuarios do ICP Admin 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class UsuarioMB extends AbstractIcpAdminMB {
	
	/* objeto user selecionado na camada de visualizacao */
	private Usuario userSelecionado;

	/* lista de usuarios */
	private List<Usuario> usuarios;
	/* lista de admins */
	private List<Usuario> admins;

	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" do ICP Admin (camada model).
	 */
	public UsuarioMB(IIcpAdmin engine) {
		super(engine);
		usuarios = new ArrayList<Usuario>();
		admins = new ArrayList<Usuario>();
		atualizarListaUsuarios();
	}
	
	/**
	 * Retorna uma lista de administradores cadastrados.
	 * @return Lista de administradores.
	 */
	public List<Usuario> getListarAdmin() {
		return admins;
	}
	
	/**
	 * Action para redirecionar ao cadastro de administradores.
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String goToAdmin() {
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
				icpAdmin.adicionarAdmin(event.getFile().getInputstream());
				FacesUtil.addFacesMessage("Usu\u00E1rio adicionado com sucesso.",
						null, FacesMessage.SEVERITY_INFO);
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
				icpAdmin.removerUsuario(userSelecionado.getDname());
				FacesUtil.addFacesMessage("Usu\u00E1rio removido com sucesso.",
						null, FacesMessage.SEVERITY_INFO);
				userSelecionado = null;
				atualizarListaUsuarios();
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao remover a Usu\u00E1rio.",
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
		usuarios.addAll(icpAdmin.listarUsuarios());

		admins.clear();
		admins.addAll(icpAdmin.listarUsuariosPorRole(
				IIcpAdmin.ICPADMIN_ADMIN_ACCESS_ROLE));
	}
}
