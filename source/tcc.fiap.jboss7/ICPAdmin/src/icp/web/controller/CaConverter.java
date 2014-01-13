package icp.web.controller;

import java.util.List;

import icp.bean.Ca;
import icp.dao.CaDAO;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * Conversor de Objetos Tipo {@link Ca} (Autoridade Certificadora)
 *   para String, usado pelo JSF.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class CaConverter implements Converter {

	/* lista de CA's */
	private List<Ca> caList;
	
	/**
	 * Cria uma instancia do Conversor JSF de CA's.
	 * @param caList Lista de CA's,
	 */
	public CaConverter(List<Ca> caList) {
		this.caList = caList;
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String s) {
		return CaDAO.getCaFromListByName(caList, s);
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object obj) {
		if ((obj != null) && (obj instanceof Ca)) {
			Ca ca = (Ca)obj;
			if (ca.getName() != null) { return ca.getName(); }
        }
		return "";	
	}

	/**
	 * Retorna a lista de CA's associada.
	 * @return Lista de CA's.
	 */
	List<Ca> getCaList() {
		return caList;
	}

	/**
	 * Configura a lista de CA's associada.
	 * @param caList Lista de CA's.
	 */
	void setCaList(List<Ca> caList) {
		this.caList = caList;
	}
}
