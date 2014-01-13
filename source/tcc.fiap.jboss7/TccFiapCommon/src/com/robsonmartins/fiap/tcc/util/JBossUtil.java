package com.robsonmartins.fiap.tcc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Encapsula funcionalidades uteis de acesso ao servidor de aplicacao JBoss.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class JBossUtil {
	
	/** Define se os usuarios terao restricoes para alterar administradores
	 *  e outras configuracoes criticas nas aplicacoes web. */
	public static final boolean TCC_FIAP_RESTRICT_USER_CHANGES = true;

	/**
	 *  Retorna caminho absoluto de um arquivo de configuracao no JBoss.
	 *  @param filename Nome do arquivo, relativo ou absoluto. 
	 *  @return Nome de arquivo com caminho absoluto.
	 *  @throws Exception
	 */
	public static String getJBossAbsFilePath(String filename) throws Exception {
		/* substitui system properties no nome de arquivo */
		int sprop = -1;
		int eprop = -1;
		StringBuilder fNameBuilder = new StringBuilder(filename);
		StringBuilder newFileName = null;
		do {
			sprop = fNameBuilder.toString().indexOf("${");
			eprop = fNameBuilder.toString().indexOf("}");
			if (sprop >= 0 && eprop >= 0) {
				newFileName = new StringBuilder();
				newFileName.append(fNameBuilder.toString().substring(0, sprop));
				newFileName.append(System.getProperty(fNameBuilder.toString().substring(sprop+2,eprop)));
				newFileName.append(fNameBuilder.toString().substring(eprop+1));
				fNameBuilder = newFileName; 
			}
		} while (sprop >= 0 && eprop >= 0);
		filename = fNameBuilder.toString();
		/* tenta obter path "conf" do JBoss */
		String confDir = getJBossConfigPath();
		InputStream inputStream = null;
		if (confDir != null) {
			/* tenta obter arquivo no path especificado via classloader */
			try {
				inputStream =
						JBossUtil.class.getClassLoader().getResourceAsStream(
							String.format("%s%s%s", confDir, File.separator, filename));
				filename = String.format("%s%s%s", confDir, File.separator, filename);
			} catch (Exception e) { }
		}
		if (inputStream == null) {
			/* se nao achou no dir JBoss, tenta obter arquivo via classloader */
			try {
				inputStream =
						JBossUtil.class.getClassLoader().getResourceAsStream(filename);
			} catch (Exception e) {	}
		}
		if (inputStream == null) {
			/* se nao achou, tenta obter arquivo diretamente no path */
			try {
				inputStream = new FileInputStream(
						String.format("%s%s%s", confDir, File.separator, filename));
				filename = String.format("%s%s%s", confDir, File.separator, filename);
			} catch (Exception e) {	}
		}
		if (inputStream == null) {
			/* se nao achou, tenta obter arquivo diretamente */
			try {
				inputStream = new FileInputStream(filename);
			} catch (Exception e) {	}
		}
		if (inputStream == null) {
			throw new FileNotFoundException(
					String.format("File %s not found.", filename));
		}
		try { inputStream.close(); } catch (Exception e) { }
		return filename.replace('/', File.separatorChar);
	}
	
	/**
	 *  Retorna o diretorio de configuracao do JBoss AS. 
	 *  @return Caminho do diretorio de configuracao do JBoss,
	 *   ou null se nao encontrado.
	 */
	public static String getJBossConfigPath() {
		String confDir = null;
		/* tenta obter path "conf" do JBoss */
		confDir = System.getProperty("jboss.server.config.url");
		if (confDir == null) {
			confDir = System.getProperty("jboss.server.config.dir");
		}

		if (confDir == null) {
			/* tenta obter path "serverhome"/conf do JBoss */
			confDir = System.getProperty("jboss.server.home.url");
			if (confDir == null) {
				confDir = System.getProperty("jboss.server.base.dir");
			}
			if (confDir != null) { 
				File f = new File(confDir + File.separatorChar + "conf");
				if (f.exists()) {
					confDir += File.separatorChar + "conf";
				} else {
					f = new File(confDir + File.separatorChar + "configuration");
					if (f.exists()) {
						confDir += File.separatorChar + "configuration";
					}					
				}
			}
		}
		return confDir;
	}

}
