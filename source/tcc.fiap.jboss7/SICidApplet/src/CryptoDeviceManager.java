import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import sun.security.pkcs11.SunPKCS11;

import com.sun.security.auth.callback.DialogCallbackHandler;

/**
 * Gerencia os Dispositivos Criptograficos.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class CryptoDeviceManager {

	/**
	 * Retorna uma lista de Certificados presentes dentro do Dispositivo Criptografico.
	 * @param provider Provider de um Dispositivo Criptografico.
	 * @return Lista de Certificados.
	 * @throws Exception Se houve erro na obtencao dos certificados.
	 */
	public static List<KeyStore.PrivateKeyEntry> getCertificates(
			Provider provider) throws Exception {
		
		List<KeyStore.PrivateKeyEntry> certList = new ArrayList<KeyStore.PrivateKeyEntry>();
		KeyStore ks = instanceOfKeyStore(provider);
		KeyStore.PrivateKeyEntry pkEntry = null;
		Enumeration<String> aliasesEnum = ks.aliases();
		while (aliasesEnum.hasMoreElements()) {
			String alias = (String) aliasesEnum.nextElement();
			if (ks.isKeyEntry(alias)) {
				pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,	null);
				certList.add(pkEntry);
			}
		}
		removeProvider(provider);
		return certList;
	}

	/**
	 * Autodetecta e retorna uma lista com os Providers dos Dispositivos Criptograficos
	 *   presentes no sistema.
	 * @return Lista de Providers.
	 * @throws Exception Se houve erro na autodeteccao de dispositivos.
	 */
	public static List<Provider> getProviderList() throws Exception {
		List<Provider> providers = new ArrayList<Provider>(); 
       	for (CryptoDeviceType d: CryptoDeviceType.values()) {
       		Provider p = null; 
       		String libName = null;
       		String deviceName = d.getName();
       		try {
           		libName = getDeviceLibName(d);
           		if (libName != null) {
           			p = new SunPKCS11(getDeviceConfig(deviceName, libName));
           			providers.add(p);
           		}
       		} catch (ProviderException e) {	
       			continue;
       		}
       	}
       	return providers;
	}
	
	/**
	 * Libera os recursos de sistema alocados por um provider.
	 * @param provider Provider de um Dispositivo Criptografico.
	 */
	public static void removeProvider(Provider provider) {
		Security.removeProvider(provider.getName());
	}
	
	/**
	 * Retorna uma lista de Certificados presentes dentro de um arquivo de keystore.
	 * @param keyStoreFile Arquivo de keystore.
	 * @return Lista de Certificados.
	 * @throws Exception Se houve erro na obtencao dos certificados.
	 */
	public static List<KeyStore.PrivateKeyEntry>
							getCertificatesFromFile(File keyStoreFile) throws Exception {
		
		List<KeyStore.PrivateKeyEntry> certList = new ArrayList<KeyStore.PrivateKeyEntry>();
		JPasswordField passField = new JPasswordField();
		JLabel passLabel = new JLabel();
		
		passField.setText(null);
		passLabel.setText("Enter the keystore password:");
		JOptionPane.showConfirmDialog(null, new Object[]{ passLabel, passField },
				"Load PKCS#12 Keystore", JOptionPane.OK_CANCEL_OPTION);

		KeyStore ks = instanceOfKeyStoreFromFile(keyStoreFile, passField.getPassword());
		KeyStore.PrivateKeyEntry pkEntry = null;
		Enumeration<String> aliasesEnum = ks.aliases();

		while (aliasesEnum.hasMoreElements()) {
			String alias = (String) aliasesEnum.nextElement();
			if (ks.isKeyEntry(alias)) {
				passField.setText(null);
				passLabel.setText(String.format("Enter the token [%s] password:",alias));
				JOptionPane.showConfirmDialog(null, new Object[]{ passLabel, passField },
						"Load PKCS#12 Keystore", JOptionPane.OK_CANCEL_OPTION);
				KeyStore.ProtectionParameter protParam =
					new KeyStore.PasswordProtection(passField.getPassword());
				try {
					pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,	protParam);
					certList.add(pkEntry);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null,"Incorrect password.",
							"Load PKCS#12 Keystore", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		return certList;
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

	/* Retorna uma instancia de KeyStore para o Provider especificado.
	 * @param provider Provider de um Dispositivo Criptografico.
	 * @return Instancia de KeyStore.
	 * @throws Exception Se houve erro ao obter o KeyStore do Dispositivo.
	 */
	private static KeyStore instanceOfKeyStore(Provider provider) throws Exception {
		KeyStore keyStore = null;
        Security.addProvider(provider); 
        CallbackHandler cmdLineHdlr = new DialogCallbackHandler();
        KeyStore.Builder builder = KeyStore.Builder.newInstance ("PKCS11", null,
                   new KeyStore.CallbackHandlerProtection(cmdLineHdlr));
       	try {
			keyStore = builder.getKeyStore();
		} catch (KeyStoreException e) {
            throw new Exception("Incorrect password or invalid certificate.");  
		}
	    return keyStore;  
	}

	/* Retorna uma instancia de KeyStore para o Arquivo especificado.
	 * @param keyStoreFile Arquivo de keystore.
	 * @param password Senha do arquivo de keystore.
	 * @return Instancia de KeyStore.
	 * @throws Exception Se houve erro ao obter o KeyStore do Arquivo.
	 */
	private static KeyStore instanceOfKeyStoreFromFile(File keyStoreFile,
			char[] password) throws Exception {

		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		InputStream istream = new FileInputStream(keyStoreFile);
		keyStore.load(istream, password);
	    return keyStore;  
	}

	/* Retorna uma stream com o conteudo do arquivo de configuracao gerado para o
	 *   Dispositivo Criptografico especificado.
	 * @param deviceName Nome do dispositivo criptografico.
	 * @param libName Nome (e caminho) da biblioteca PKCS#11 do dispositivo.
	 * @return Conteudo do arquivo de configuracao.
	 * @throws Exception Se houve erro na geracao do arquivo.
	 */
	private static InputStream getDeviceConfig(String deviceName,
			String libName) throws Exception {
		
		StringBuilder conf = new StringBuilder();
		conf.append("name = ")
			.append(deviceName)
			.append("\n")
			.append("library = ")
			.append(libName)
			.append("\n")
			.append("showInfo = true");
		
		return new ByteArrayInputStream(conf.toString().getBytes("UTF-8")); 
	}
	
	/* Retorna o nome e o caminho da biblioteca PKCS#11 do dispositivo
	 *   criptografico especificado.
	 * @param device Dispositivo Criptografico.
	 * @return Nome da biblioteca PKCS#11.
	 */
	private static String getDeviceLibName(CryptoDeviceType device) {
		
		StringBuilder libName = null;
		String os = System.getProperty("os.name");
		File libFile = null;
		if (os == null || os.toLowerCase().startsWith("windows")) {
			libName = new StringBuilder(System.getenv("windir"));
			libName.append("\\system32\\")
 	       		.append(device.getLibrary())
 	       		.append(".dll");
			libFile = new File(libName.toString());
        	if (!libFile.exists()) { libName = null; }
        	
		} else {
			String paths[] =
				{ "/usr/lib/lib", "/lib/lib", "/usr/lib/", "/lib/",
				  "/usr/lib/watchdata/ICP/lib/lib" };
			for (String path : paths) {
				libName = new StringBuilder(path);
	        	libName.append(device.getLibrary())
	        		.append(".so");
				libFile = new File(libName.toString());
	        	if (libFile.exists()) { 
	        		break; 
	        	} else {
	        		libName = null;
	        	}
			}
		}
		return (libName != null) ? libName.toString() : null;
	}
	
}
