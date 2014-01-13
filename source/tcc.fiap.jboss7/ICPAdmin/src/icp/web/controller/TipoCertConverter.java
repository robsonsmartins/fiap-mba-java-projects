package icp.web.controller;

import icp.bean.TipoCertificado;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * Conversor de Objetos Tipo {@link TipoCertificado}
 *   para String, usado pelo JSF.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class TipoCertConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String s) {
		Object obj = null;
		try {
			obj = TipoCertificado.byId(Integer.valueOf(s)); 
		} catch (Exception e) { }
		return obj;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object obj) {
		if ((obj != null) && (obj instanceof TipoCertificado)) {
			TipoCertificado tipo = (TipoCertificado)obj;
			return String.valueOf(tipo.getId());
        }
		return "-1";	
	}
}
