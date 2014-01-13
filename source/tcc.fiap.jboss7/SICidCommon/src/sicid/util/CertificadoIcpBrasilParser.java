package sicid.util;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

/**
 * Implementa metodos para extrair informacoes de certificados no padrao ICP Brasil.
 * @author Robson Martins (robson@robsonmartins.com)
 * @see <a target="_blank" href="http://www.iti.gov.br/twiki/bin/view/Certificacao/EstruturaIcp">Estrutura da ICP-Brasil</a>
 */
public class CertificadoIcpBrasilParser {

	/**
	 * Enumera os principais atributos encontrados nos certificados
	 *   emitidos no padrao ICP Brasil. 
	 */
	public enum AtributoIcpBrasil {
		/** Nome do Responsavel ou Titular. */
		NOME_RESPONSAVEL,
		/** Nome da Pessoa Juridica (se certificado PJ). */
		NOME_PJ,
		/** Numero do Registro de Identidade Civil (RIC). */
		RIC,
		/** Numero do Cadastro de Pessoa Fisica (CPF). */
		CPF,
		/** Numero do Cadastro Nacional de Pessoa Juridica (CNPJ). */
		CNPJ,
		/** Endereco de email do titular ou responsavel. */
		EMAIL,
		/** Data de nascimento do titular ou responsavel. */
		NASCIMENTO,
		/** Numero de cadastro do Programa de Integracao Social (PIS) ou
		 *   Programa de Formacao do Patrimonio do Servidor Publico (PASEP) do
		 *   titular ou responsavel. */
		PISPASEP,
		/** Numero do Registro Geral (RG) do titular ou responsavel. */
		RG,
		/** Orgao Expedidor do RG do titular ou responsavel. */
		RG_ORGEXPEDIDOR,
		/** Unidade da Federacao (UF) do RG do titular ou responsavel. */
		RG_UF,
		/** Numero de inscricao do Cadastro Especifico do INSS (CEI) do
		 *   titular ou responsavel. */
		CEI,
		/** Numero do Titulo Eleitoral do titular ou responsavel. */
		TITULO,
		/** Zona do Titulo Eleitoral do titular ou responsavel. */
		TITULO_ZONA,
		/** Secao do Titulo Eleitoral do titular ou responsavel. */
		TITULO_SECAO,
		/** Municipio do Titulo Eleitoral do titular ou responsavel. */
		TITULO_MUNICIPIO,
		/** Unidade da Federacao (UF) do Titulo Eleitoral
		 *   do titular ou responsavel. */
		TITULO_UF,
		/** Nome de login do titular. */
		LOGIN,
		/** Globally Unique IDentifier (GUID) do servidor
		 *   (se certificado destinado para equipamento). */
		GUID_SERVER;
	}
	
	/* OID's definidos pela ICP Brasil */
	private static final DERObjectIdentifier OID_PF_DADOS_TITULAR     = new DERObjectIdentifier("2.16.76.1.3.1");
	private static final DERObjectIdentifier OID_PJ_RESPONSAVEL       = new DERObjectIdentifier("2.16.76.1.3.2");
	private static final DERObjectIdentifier OID_PJ_CNPJ              = new DERObjectIdentifier("2.16.76.1.3.3");
	private static final DERObjectIdentifier OID_PJ_DADOS_RESPONSAVEL = new DERObjectIdentifier("2.16.76.1.3.4");
	private static final DERObjectIdentifier OID_PF_ELEITORAL         = new DERObjectIdentifier("2.16.76.1.3.5");
	private static final DERObjectIdentifier OID_PF_CEI               = new DERObjectIdentifier("2.16.76.1.3.6");
	private static final DERObjectIdentifier OID_PJ_CEI               = new DERObjectIdentifier("2.16.76.1.3.7");
	private static final DERObjectIdentifier OID_PJ_NOME_EMPRESARIAL  = new DERObjectIdentifier("2.16.76.1.3.8");
	private static final DERObjectIdentifier OID_PF_RIC               = new DERObjectIdentifier("2.16.76.1.3.9");
	private static final DERObjectIdentifier OID_PF_LOGIN             = new DERObjectIdentifier("1.3.6.1.4.1.311.20.2.3");
	private static final DERObjectIdentifier OID_PJ_GUID_SERVER       = new DERObjectIdentifier("1.3.6.1.4.1.311.25.1");
	
	/**
	 * Retorna os atributos de um certificado digital, padrao ICP Brasil.
	 * @param cert Objeto {@link X509Certificate} que representa o certificado.
	 * @return Objeto {@link Map} contendo os atributos.
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static Map<AtributoIcpBrasil, String> getAtributosIcpBrasil(
			X509Certificate cert) throws Exception {
		
		Map<AtributoIcpBrasil, String> atributos = 
			new HashMap<AtributoIcpBrasil, String>();

		Collection subjAltNames = 
			X509ExtensionUtil.getSubjectAlternativeNames(cert);
	
		for (Object obj : subjAltNames) {
	
			if (obj instanceof ArrayList) {

				Object key   = ((ArrayList) obj).get(0);
				Object value = ((ArrayList) obj).get(1);
				
				if (((Number) key).intValue() == 1) {
					String email =
						extraiAtributo((String) value, -1, -1);
					atributos.put(AtributoIcpBrasil.EMAIL, email);
				}
				
				if (value instanceof ASN1Sequence) {
					ASN1Sequence seq = (ASN1Sequence) value;
					ASN1ObjectIdentifier oid =
						(ASN1ObjectIdentifier) seq.getObjectAt(0);
					ASN1TaggedObject tagged =
						(ASN1TaggedObject) seq.getObjectAt(1);
					String data = null;
					ASN1Primitive derObj = tagged.getObject();

					if (derObj instanceof DEROctetString) {
						DEROctetString octet = (DEROctetString) derObj;
						data = new String(octet.getOctets());
					} else if (derObj instanceof DERPrintableString) {
						DERPrintableString octet = (DERPrintableString) derObj;
						data = new String(octet.getOctets());
					} else if (derObj instanceof DERUTF8String) {
						DERUTF8String str = (DERUTF8String) derObj;
						data = str.getString();
					}
	
					if (data != null && !data.isEmpty()) {
						
						if (oid.equals(OID_PF_DADOS_TITULAR)
								|| oid.equals(OID_PJ_DADOS_RESPONSAVEL)) {
							
							String nascimento = extraiAtributo(data, 0, 8);
							atributos.put(AtributoIcpBrasil.NASCIMENTO, nascimento);

							String cpf = extraiAtributo(data, 8, 19);
							atributos.put(AtributoIcpBrasil.CPF, cpf);
							
							String pisPasep = extraiAtributo(data, 19, 30);
							atributos.put(AtributoIcpBrasil.PISPASEP, pisPasep);

							String rg       = extraiAtributo(data, 30, 45);
							String rgOrgExp = extraiAtributo(data, 45, 49);
							String rgUF     = extraiAtributo(data, 49, 51);
                            if ("".equals(rg)) { 
                            	rgOrgExp = ""; rgUF = "";
                            }
							atributos.put(AtributoIcpBrasil.RG, rg);
							atributos.put(AtributoIcpBrasil.RG_ORGEXPEDIDOR, rgOrgExp);
							atributos.put(AtributoIcpBrasil.RG_UF, rgUF);
							
						} else if (oid.equals(OID_PF_CEI)) {
							String cei = extraiAtributo(data, 0, 12);
							atributos.put(AtributoIcpBrasil.CEI, cei);
							
						} else if (oid.equals(OID_PF_ELEITORAL)) {
							String titulo    = extraiAtributo(data,  0, 12);
							String zona      = extraiAtributo(data, 12, 15);
							String secao     = extraiAtributo(data, 15, 19);
							String municipio = extraiAtributo(data, 19, 39);
							String uf        = extraiAtributo(data, 39, 41);
							if ("".equals(titulo)) {
								zona = ""; secao = ""; municipio = ""; uf = "";
							} 
							atributos.put(AtributoIcpBrasil.TITULO, titulo);
							atributos.put(AtributoIcpBrasil.TITULO_ZONA, zona);
							atributos.put(AtributoIcpBrasil.TITULO_SECAO, secao);
							atributos.put(AtributoIcpBrasil.TITULO_MUNICIPIO, municipio);
							atributos.put(AtributoIcpBrasil.TITULO_UF, uf);
							
						} else if (oid.equals(OID_PJ_RESPONSAVEL)) {
							String nome = extraiAtributo(data, -1, -1);
							atributos.put(AtributoIcpBrasil.NOME_RESPONSAVEL, nome);
							

						} else if (oid.equals(OID_PJ_CNPJ)) {
							String cnpj = extraiAtributo(data, -1, -1);
							atributos.put(AtributoIcpBrasil.CNPJ, cnpj);
						
						} else if (oid.equals(OID_PJ_CEI)) {
							String cei = extraiAtributo(data, 0, 12);
							atributos.put(AtributoIcpBrasil.CEI, cei);
							
						} else if (oid.equals(OID_PJ_NOME_EMPRESARIAL)) {
							String nomePJ = extraiAtributo(data, -1, -1);
							atributos.put(AtributoIcpBrasil.NOME_PJ, nomePJ);
							
						} else if (oid.equals(OID_PF_RIC)) {
							String ric = extraiAtributo(data, -1, -1);
							atributos.put(AtributoIcpBrasil.RIC, ric);
							
						} else if (oid.equals(OID_PF_LOGIN)) {
							String login = extraiAtributo(data, -1, -1);
							atributos.put(AtributoIcpBrasil.LOGIN, login);
							
						} else if (oid.equals(OID_PJ_GUID_SERVER)) {
							String guid = extraiAtributo(data, -1, -1);
							atributos.put(AtributoIcpBrasil.GUID_SERVER, guid);
						}
					}
				}
			}
		}
		/* se certificado for PF, nome esta' no subject CN, antes da separacao
		 * por ':' (conforme ICP Brasil, CN = <nome>':'<cpf|ric>). */
		String nome = atributos.get(AtributoIcpBrasil.NOME_RESPONSAVEL);
		if (nome == null) {
			nome = extractNomeFromCN(cert.getSubjectX500Principal().getName());
			if (nome != null) {
				atributos.put(AtributoIcpBrasil.NOME_RESPONSAVEL, nome);
			}
		}
		return atributos;
	}

	/**
	 * Extrai o nome do titular a partir do Common Name (CN), presente
	 *   no Distinguished Name (DN) de um certificado padrao ICP Brasil.<br/>
	 * Conforme especificacao, o campo CN e' composto de (nome):(documento).
	 * @param dname Distinguished Name (DN) do certificado.
	 * @return Nome extraido a partir do CN, ou null se nao encontrado.
	 */
	public static String extractNomeFromCN(String dname) {
		/* se certificado for PF, nome esta' no subject CN, antes da separacao
		 * por ':' (conforme ICP Brasil, CN = <nome>':'<cpf|ric>). */
		String nome = null;
		List<String> cnList = subjectParse(dname, "CN");
		if (cnList.size() != 0) {
			String cn = cnList.get(0);
			if (cn != null) {
				int endIndex = cn.indexOf(":");
				if (endIndex >= 0) {
					nome = cn.substring(0, endIndex);
				} else {
					nome = cn;
				}
			}
		}
		return nome;
	}

	/**
	 * Retorna os valores de um campo (field) de um subject (DN - Distinguished Name).
	 * @param subject String representando um subject.
	 * @param field Nome do campo a ser retornado (ex: "C", "O", "OU", etc.).
	 * @return Valores atribuidos ao campo, ou vazio se nenhum.
	 * @see <a href="http://www.x500standard.com/">X500 Standard</a>
	 */
	public static List<String> subjectParse(String subject, String field) {
		List<String> subjList = new ArrayList<String>();
		if (subject != null && !"".equals(subject)) {
			String pattern = String.format("%s=", field);
			int beginIndex = 0;
			int endIndex = 0;
			do {
				beginIndex = subject.indexOf(pattern, beginIndex);
				if (beginIndex >= 0) {
					beginIndex += pattern.length();
					endIndex = subject.indexOf(",", beginIndex);
					if (endIndex > - 0)
						subjList.add(subject.substring(beginIndex, endIndex));
					else
						subjList.add(subject.substring(beginIndex));
				}
			} while (beginIndex >= 0);
		}
		return subjList;
	}

	/* Extrai um atributo a partir de uma string.
	 * @param value String contendo o atributo.
	 * @param startIndex Indice de inicio do atributo na substring
	 *   (se negativo, usa string inteira).
	 * @param endIndex Indice de fim do atributo na substring.
	 * @return String com o atributo, vazio se atributo estiver preenchido
	 *   somente com zeros. 
	 */
	private static String extraiAtributo(
			String value, int startIndex, int endIndex) {
	
		String atributo = null;
		char zeroPattern[] = null;

		try {
			if (startIndex < 0) {
				atributo = value;
			} else {
				atributo = value.substring(startIndex, endIndex);
				zeroPattern = new char[endIndex - startIndex];
				Arrays.fill(zeroPattern, '0');
			}
			if (zeroPattern != null && 
					(new String(zeroPattern)).equals(atributo)) {
				atributo = "";
			}
		} catch (Exception e) { /* retorna vazio apenas */ }
		
		if (atributo == null) { atributo = ""; }
		return atributo.trim();
	}
	
}
