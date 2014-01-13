package icp.bean;

/**
 * Enumera os possiveis Status de um Certificado.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public enum StatusCertificado {

	/** Certificado Valido. */
	VALIDO  ("V\u00E1lido"),       /* 0 */
	
	/** Certificado Expirado. */
	EXPIRADO("Expirado"),          /* 1 */
	
	/** Certificado Revogado. */
	REVOGADO("Revogado");          /* 2 */
	
	/* decricao do tipo */
	private String descricao;
	
	/* Construtor do valor enumerado, com a descricao especificada.
	 * @param descricao Descricao do status.
	 */
	private StatusCertificado(String descricao) {
		this.descricao = descricao;
	}

	/**
	 * Retorna o ID (ordinal) do Status de Certificado.
	 * @return ID do Status de Certificado.
	 */
	public int getId() {
		return this.ordinal();
	}

	/**
	 * Retorna a descricao do Status de Certificado.
	 * @return Descricao do Status de Certificado.
	 */
	public String getDescricao() {
		return descricao;
	}
	
	/**
	 * Retorna um Status de Certificado pelo ID.
	 * @param id ID do Status de Certificado.
	 * @return Valor enumerado do Status de Certificado,
	 *   ou {@link EnumConstantNotPresentException} se o ID nao corresponde a
	 *   nenhum valor conhecido.
	 * @throws EnumConstantNotPresentException
	 */
	public static StatusCertificado byId(int id) throws EnumConstantNotPresentException {
		if (VALIDO  .getId() == id) return VALIDO;
		if (EXPIRADO.getId() == id) return EXPIRADO;
		if (REVOGADO.getId() == id) return REVOGADO;
		throw new EnumConstantNotPresentException(StatusCertificado.class,
					String.format("id: %d", id)); 
	}
}
