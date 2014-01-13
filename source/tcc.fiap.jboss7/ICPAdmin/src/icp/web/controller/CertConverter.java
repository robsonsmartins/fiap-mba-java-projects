package icp.web.controller;

import icp.bean.Certificado;
import icp.dao.CertificadoDAO;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * Conversor de Objetos Tipo {@link Certificado}
 *   para String, usado pelo JSF.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class CertConverter implements Converter {

	/* lista de certificados */
	private List<Certificado> certList;

	/**
	 * Cria uma instancia do Conversor JSF de Certificados.
	 * @param certList Lista de Certificados.
	 */
	public CertConverter(List<Certificado> certList) {
		this.certList = certList;
	}
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String s) {
		Object obj = null;
		try {
			obj = CertificadoDAO.getCertfromListById(certList, Long.valueOf(s));
		} catch (Exception e) { }
		return obj;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object obj) {
		if ((obj != null) && (obj instanceof Certificado)) {
			Certificado cert = (Certificado)obj;
			return String.valueOf(cert.getId());
        }
		return "";	
	}

	/**
	 * Retorna a lista de Certificados associada.
	 * @return Lista de Certificados.
	 */
	List<Certificado> getCertList() {
		return certList;
	}

	/**
	 * Configura a lista de Certificados associada.
	 * @param certList Lista de Certificados.
	 */
	void setCertList(List<Certificado> certList) {
		this.certList = certList;
	}

}
