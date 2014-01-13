package receita.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import receita.bean.Tributo;
import receita.bean.Cidadao;
import receita.model.IReceitaEngine;

import com.robsonmartins.fiap.tcc.util.FacesUtil;

/**
 * Classe responsavel por fornecer o acesso 'as funcionalidades de
 *   gerenciamento de tributos da Receita Nacional 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class TributoMB extends AbstractReceitaMB {
	
	/* objeto tributo selecionado na camada de visualizacao */
	private Tributo tributoSelecionado;
	/* usuario logado */
	private Cidadao usuarioLogado;

	/* lista de tributos */
	private List<Tributo> tributos;
	/* lista de tributos do cidadao logado */
	private List<Tributo> tributosCidadaoLogado;

	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" da Receita Nacional (camada model).
	 */
	public TributoMB(IReceitaEngine engine) {
		super(engine);
		usuarioLogado = super.getUsuarioLogin();
		tributos = new ArrayList<Tributo>();
		tributosCidadaoLogado = new ArrayList<Tributo>();
		atualizarListaTributos();
		atualizarListaTributosCidadaoLogado();
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
	 * Retorna uma lista de tributos.
	 * @return Lista de tributos.
	 */
	public List<Tributo> getListarTributos() {
		return tributos;
	}
	
	/**
	 * Action para calcular tributos e atribuir a todos
	 * os cidadaos cadastrados no SICid. 
	 */
	public void calcularTributos() {
		try {
			receitaEngine.calcularTributos();
			FacesUtil.addFacesMessage("Tributos calculados com sucesso.",
					null, FacesMessage.SEVERITY_INFO);
			atualizarListaTributos();
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao calcular tributos.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}
	
	/**
	 * Action para efetivar o pagemento de um tributo
	 *  (online via conexao com o Banco Seguro).
	 */
	public void pagarTributo() {
		try {
			String certCidadaoLogado = FacesUtil.getUserPrincipalName();
			receitaEngine.pagarTributoOnline(tributoSelecionado, certCidadaoLogado);
			FacesUtil.addFacesMessage("Tributo pago com sucesso.",
					null, FacesMessage.SEVERITY_INFO);
			tributoSelecionado = null;
			atualizarListaTributos();
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao pagar tributo.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}
	
	/**
	 * Retorna uma lista de tributos do cidadao logado.
	 * @return Lista de tributos.
	 */
	public List<Tributo> getListarTributosCidadaoLogado() {
		return tributosCidadaoLogado;
	}
	
	/**
	 * Action para redirecionar 'a pagina de tributacao.
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String goToGoverno() {
		atualizarListaTributos();
		return "governo";
	}
	
	/**
	 * Action para redirecionar 'a pagina do cidadao.
	 * @return String com o nome do destino (target)
	 *   do redirecionamento da action.
	 */
	public String goToCidadao() {
		tributoSelecionado = null;
		atualizarListaTributosCidadaoLogado();
		return "cidadao";
	}

	/**
	 * Retorna a selecao do tributo.
	 * @return Tributo selecionado.
	 */
	public Tributo getTributoSelecionado() {
		return tributoSelecionado;
	}

	/**
	 * Armazena a selecao do tributo.
	 * @param tributoSelecionado Tributo selecionado.
	 */
	public void setTributoSelecionado(Tributo tributoSelecionado) {
		this.tributoSelecionado = tributoSelecionado;
	}

	/* Atualiza lista de tributos. */
	private void atualizarListaTributos() {
		tributos.clear();
		tributos.addAll(receitaEngine.listarTributos());
	}
	
	/* Atualiza lista de tributos do usuario logado. */
	private void atualizarListaTributosCidadaoLogado() {
		tributosCidadaoLogado.clear();
		Tributo tributo = 
			receitaEngine.localizarTributoPorCidadao(usuarioLogado);
		if (tributo != null) { tributosCidadaoLogado.add(tributo); }
	}
	
}
