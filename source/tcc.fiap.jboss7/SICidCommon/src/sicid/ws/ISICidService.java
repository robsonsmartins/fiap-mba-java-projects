package sicid.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import sicid.bean.CertificadoStatus;
import sicid.bean.Cidadao;
import sicid.bean.CidadaoCollecion;

/**
 * Interface do Servico de Identificacao do Cidadao (SICid).
 * @author Robson Martins (robson@robsonmartins.com)
 */
@WebService(name="SICidService")
@SOAPBinding(style=Style.DOCUMENT)
public interface ISICidService {

	/**
	 * Valida um certificado digital.
	 * @param content Conteudo de um certificado, codificado em Base64.  
	 * @return Status do certificado.
	 */
	@WebMethod
	@WebResult(name="status")
	public CertificadoStatus validarCertificado(
			@WebParam(name="certificado") String content);
	
	/**
	 * Consulta os dados cadastrais de um cidadao,
	 *   a partir de seu certificado digital.
	 * @param content Conteudo de um certificado, codificado em Base64.  
	 * @return Dados cadastrais de um cidadao.
	 */
	@WebMethod
	@WebResult(name="cidadao")
	public Cidadao consultarCidadao(
			@WebParam(name="certificado") String content);
	
	/**
	 * Retorna uma lista de todos cidadaos cadastrados.
	 * @return Lista de cidadaos cadastrados.
	 */
	@WebMethod
	@WebResult(name="cidadaos")
	public CidadaoCollecion listarCidadaos();
	
	
}
