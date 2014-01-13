/**
 * Enumera os Tipos de Dispositivos Criptograficos compativeis com esta applet.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public enum CryptoDeviceType {
	
	A3_ACOS5          ("ACOS5"          ,"acospkcs11"       ), 
	A3_ATHENA         ("ATHENA"         ,"asepkcs"          ),
	A3_IDPROTECT      ("IDPROTECT"      ,"ASEP11"           ),
	A3_STARCOS        ("STARCOS"        ,"aetpkss1"         ),
	A3_WATCHDATA      ("WATCHDATA"      ,"wdpkcs_icp"       ),
	A3_ETOKEN         ("ETOKEN"         ,"etpkcs11"         ),
	A3_SAFEWEB        ("SAFEWEB"        ,"cmp11"            ),
	A3_EPASS1000      ("EPASS1000"      ,"ep1pk111"         ),
	A3_EPASS2000      ("EPASS2000"      ,"ep2pk11"          ),
	A3_EPASS3000      ("EPASS3000"      ,"ngp11v211"        ),
	A3_EPASS3003      ("EPASS3003"      ,"shuttlecsp11_3003"),
	A3_EPASS2000LX    ("EPASS2000LX"    ,"epsng_p11"        ),
	A3_AR_MINIKEY     ("ARMINIKEY"      ,"sadaptor"         ),
	A3_ALOAHA         ("ALOAHA"         ,"aloaha_pkcs11"    ),
	A3_ASIGN          ("ASIGN"          ,"psepkcs11"        ),
	A3_ATRUST         ("ATRUST"         ,"asignp11"         ),
	A3_CHRYSALIS      ("CHRYSALIS"      ,"cryst32"          ),
	A3_LUNA           ("LUNA"           ,"cryst201"         ),
	A3_IBUTTON        ("IBUTTON"        ,"dspkcs"           ),
	A3_ERACOM         ("ERACOM"         ,"cryptoki"         ),
	A3_GEMPLUS        ("GEMPLUS"        ,"gclib"            ),
	A3_GEMPLUS2       ("GEMPLUS2"       ,"pk2priv"          ),
	A3_GEMSOFT        ("GEMSOFT"        ,"w32pk2ig"         ),
	A3_IBM_DSI        ("IBM_DSI"        ,"cccsigit"         ),
	A3_IBM_ESS        ("IBM_ESS"        ,"csspkcs11"        ),
	A3_IBM_PSG        ("IBM_PSG"        ,"ibmpkcss"         ),
	A3_ID2            ("ID2"            ,"id2cbox"          ),
	A3_NFAST          ("NFAST"          ,"cknfast"          ),
	A3_NEXUS          ("NEXUS"          ,"nxpkcs11"         ),
	A3_MIRCADO        ("MIRCADO"        ,"micardopkcs11"    ),
	A3_CRYPTOSWIFT    ("CRYPTOSWIFT"    ,"cryptoki22"       ),
	A3_CRYPTOSWIFT_HSM("CRYPTOSWIFT_HSM","iveacryptoki"     ),
	A3_IKEY1000       ("IKEY1000"       ,"k1pk112"          ),
	A3_IKEY2000       ("IKEY2000"       ,"dkck201"          ),
	A3_IKEY2032       ("IKEY2032"       ,"dkck232"          ),
	A3_SAFELAYER_HSM  ("SAFELAYER_HSM"  ,"p11card"          ),
	A3_CRYPTOFLEX     ("CRYPTOFLEX"     ,"acpkcs"           ),
	A3_CRYPTOFLEX2    ("CRYPTOFLEX2"    ,"slbck"            ),
	A3_SETOKI         ("SETOKI"         ,"settoki"          ),
	A3_HIPATH         ("HIPATH"         ,"siecap11"         ),
	A3_SMARTTRUST     ("SMARTTRUST"     ,"smartp11"         ),
	A3_SPYRUS         ("SPYRUS"         ,"spypk11"          ),
	A3_UTIMACO        ("UTIMACO"        ,"pkcs201n"         ),
	A3_ACTIVCLIENT    ("ACTIVCLIENT"    ,"acpkcs211"        ),
	A3_FORTEZZA       ("FORTEZZA"       ,"fort32"           ),
	A3_AUTHENTIC      ("AUTHENTIC"      ,"aucryptoki2-0"    ),
	A3_SCW_3GI        ("SCW_3GI"        ,"3gp11csp"         ),
	A3_TELESEC        ("TELESEC"        ,"pkcs11"           ),
	A3_OPENSC         ("OPENSC"         ,"opensc-pkcs11"    );

	/* Nome do dispositivo. */
	private final String name;
	/* Nome da biblioteca */
	private final String library;
	
	/* Cria uma instancia de CryptoDeviceType.
	 * @param name Nome do dispositivo.
	 * @param library Nome da biblioteca.
	 */
	private CryptoDeviceType(String name, String library) {
		this.name    = name;
		this.library = library;
	}

	/**
	 * Retorna o nome do dispositivo.
	 * @return Nome do dispositivo.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retorna o nome da biblioteca.
	 * @return Nome da biblioteca.
	 */
	public String getLibrary() {
		return library;
	}
}
