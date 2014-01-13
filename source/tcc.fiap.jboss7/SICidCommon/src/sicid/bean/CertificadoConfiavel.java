package sicid.bean;

import java.io.Serializable;
import java.security.cert.X509Certificate;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * Representa um certificado confiavel (trusted), persistido via JPA.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@Entity
@SuppressWarnings("serial")
public class CertificadoConfiavel implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@Embedded
	private CertificadoEntity certificado;

	/**
	 * Retorna o Id interno do certificado.
	 * @return Id do certificado no banco de dados.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Configura o Id interno do certificado.
	 * @param id Id do certificado no banco de dados.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Retorna o objeto {@link X509Certificate} que representa o
	 *   certificado digital armazenado.
	 * @return Objeto que representa o certificado.
	 */
	@Transient
	public X509Certificate getX509Certificate() {
		if (certificado == null) { return null; }
		return certificado.getX509Certificate();
	}
	
	/**
	 * Configura o objeto {@link X509Certificate} que representa o
	 *   certificado digital armazenado.
	 * @param cert Objeto que representa o certificado.
	 */
	public void setX509Certificate(X509Certificate cert) {
		if (certificado == null) {
			certificado = new CertificadoEntity();
		}
		certificado.setX509Certificate(cert);
	}

}
