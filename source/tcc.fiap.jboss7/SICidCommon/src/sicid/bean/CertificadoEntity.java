package sicid.bean;

import java.io.Serializable;
import java.security.cert.X509Certificate;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.robsonmartins.fiap.tcc.util.CertificadoSerializador;

/**
 * Representa um certificado digital, persistido via JPA.
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
@Embeddable
public class CertificadoEntity implements Serializable {

	/* conteudo serializado (formato Base64) do certificado */ 
	private String content;
	
	/**
	 * Retorna o conteudo do certificado, codificado em Base64.
	 * @return Conteudo do certificado, ou null se houve erro.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Armazena o certificado (a partir do conteudo codificado em Base64).
	 * @param content Conteudo do certificado, codificado em Base64.
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * Retorna o objeto {@link X509Certificate} que representa o
	 *   certificado digital armazenado.
	 * @return Objeto que representa o certificado.
	 */
	@Transient
	public X509Certificate getX509Certificate() {
		try {
			return CertificadoSerializador.strToCert(content);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Configura o objeto {@link X509Certificate} que representa o
	 *   certificado digital armazenado.
	 * @param cert Objeto que representa o certificado.
	 */
	public void setX509Certificate(X509Certificate cert) {
		try {
			this.content = CertificadoSerializador.certToStr(cert);
		} catch (Exception e) {	}
	}
}
