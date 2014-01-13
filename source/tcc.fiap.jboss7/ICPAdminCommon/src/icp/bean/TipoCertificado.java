package icp.bean;

/**
 * Enumera Tipos de Certificado, no ambito da ICP Brasil.
 * @author Robson Martins (robson@robsonmartins.com)
 * @see <a target="_blank" href="http://www.receita.fazenda.gov.br/acsrf/LeiautedeCertificadosdaSRF.pdf">
 *   Leiaute dos Certificados Digitais da SRFB</a>
 */
public enum TipoCertificado {

	/** Certificado padrao e-CPF. */
	ECPF("Certificado e-CPF"),                       /* 0 */
	
	/** Certificado padrao e-CNPJ. */
	ECNPJ("Certificado e-CNPJ"),                     /* 1 */
	
	/** Certificado padrao RIC. */
	RIC("Certificado RIC"),                          /* 2 */
	
	/** Certificado padrao e-CODIGO. */
	ECODIGO("Certificado e-C\u00F3digo"),            /* 3 */
	
	/** Certificado padrao e-SERVIDOR. */
	ESERVIDOR("Certificado e-Servidor"),             /* 4 */
	
	/** Certificado padrao e-APLICACAO. */
	EAPLICACAO("Certificado e-Aplica\u00E7\u00E3o"); /* 5 */

	/* decricao do tipo */
	private String descricao;
	
	/* Construtor do valor enumerado, com a descricao especificada.
	 * @param descricao Descricao do tipo.
	 */
	private TipoCertificado(String descricao) {
		this.descricao = descricao;
	}

	/**
	 * Retorna o ID (ordinal) do Tipo de Certificado.
	 * @return ID do Tipo de Certificado.
	 */
	public int getId() {
		return this.ordinal();
	}

	/**
	 * Retorna a descricao do Tipo de Certificado.
	 * @return Descricao do Tipo de Certificado.
	 */
	public String getDescricao() {
		return descricao;
	}

	/**
	 * Retorna um Tipo de Certificado pelo ID.
	 * @param id ID do Tipo de Certificado.
	 * @return Valor enumerado do Tipo de Certificado,
	 *   ou {@link EnumConstantNotPresentException} se o ID nao corresponde a
	 *   nenhum valor conhecido.
	 * @throws EnumConstantNotPresentException
	 */
	public static TipoCertificado byId(int id) throws EnumConstantNotPresentException {
		if (ECPF      .getId() == id) return ECPF;
		if (ECNPJ     .getId() == id) return ECNPJ;
		if (RIC       .getId() == id) return RIC;
		if (ECODIGO   .getId() == id) return ECODIGO;
		if (ESERVIDOR .getId() == id) return ESERVIDOR;
		if (EAPLICACAO.getId() == id) return EAPLICACAO;
		throw new EnumConstantNotPresentException(TipoCertificado.class,
					String.format("id: %d", id)); 
	}
}
