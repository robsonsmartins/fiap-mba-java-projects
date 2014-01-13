package sicid.bean;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Enumera status de validacao de certificados digitais pelo 
 *   Servico de Identificacao do Cidadao (SICid).
 * @author Robson Martins (robson@robsonmartins.com)
 */
@XmlType(name="status")
@XmlEnum
public enum CertificadoStatus {

	/** Desconhecido (ou corrompido). */
	UNKNOWN,
	/** Valido. */
	VALID,
	/** Expirado. */
	EXPIRED,
	/** Revogado. */
	REVOKED,
	/** Invalido (auto-assinado ou assinado por AC desconhecida). */
	INVALID;
	
}
