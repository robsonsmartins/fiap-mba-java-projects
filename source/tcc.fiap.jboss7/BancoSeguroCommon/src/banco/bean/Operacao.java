package banco.bean;

/**
 * Enumera Operacoes bancarias oferecidas pelo Banco Seguro.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public enum Operacao {

	/** Operacao Desconhecida. */
	DESCONHECIDA   ("Desconhecida"),
	/** Transferencia entre contas. */
	TRANSFERENCIA  ("Transfer\u00EAncia"),
	/** Pagamento de titulos. */
	PAGAMENTO      ("Pagamento"),
	/** Saque. */
	SAQUE          ("Saque"),
	/** Deposito. */
	DEPOSITO       ("Dep\u00F3sito"),
	/** Deposito via transferencia. */
	DEPOSITO_TRANSF("Dep\u00F3sito via Transfer\u00EAncia");
	
	/* decricao do tipo */
	private String descricao;
	
	/* Construtor do valor enumerado, com a descricao especificada.
	 * @param descricao Descricao da operacao.
	 */
	private Operacao(String descricao) {
		this.descricao = descricao;
	}
	
	/**
	 * Retorna a descricao da Operacao.
	 * @return Descricao da Operacao.
	 */
	public String getDescricao() {
		return this.descricao;
	}
	
	@Override
	public String toString() {
		return this.descricao;
	}
}
