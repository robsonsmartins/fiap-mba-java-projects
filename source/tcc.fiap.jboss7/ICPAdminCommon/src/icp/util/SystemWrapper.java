package icp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Encapsula funcionalidades do Sistema Operacional.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class SystemWrapper {

	/* Objeto Runtime para execucao de comandos no S.O. */
	private static final Runtime runtime = Runtime.getRuntime(); 
	/* Comando executavel para remover diretorios e arquivos recursivamente */ 
	private static final String[] RM_COMMAND_UNIX = { "rm", "-Rf" };
	private static final String[] RM_COMMAND_WIN  = { "cmd", "/c", "rd", "/s", "/q" };
	
	/* objeto para realizar log das operacoes */
	private static Logger logger;
	private static boolean trace;

	/* Inicializa atributos estaticos. */
	static {
		logger = LogManager.getLogger(SystemWrapper.class);
		trace = logger.isTraceEnabled();
	}

	/**
	 * Executa um comando no sistema operacional. 
	 * @param command Array de Strings contendo o comando a ser executado e seus argumentos.
	 * @return Objeto Process, representando o processo do comando chamado. 
	 * @throws IOException
	 */
	public static Process executeCommand(String[] command) throws IOException {
		if (trace) {
			StringBuilder commandLine = new StringBuilder();
			for (String s : command) {
				if (commandLine.length() > 0) { commandLine.append(' '); }
				commandLine.append(s);
			}
			logger.trace(String.format("Execute: %s", commandLine.toString()));
		}

		return runtime.exec(command);
	}
	
	/**
	 * Espera pelo termino de um processo e retorna seu codigo de saida.
	 * @param p Objeto Process, que representa um processo.
	 * @return Codigo de saida do processo.
	 * @throws Exception
	 */
	public static int processWait(Process p, long timeout) throws Exception {
		int exitValue = 0;
		boolean exitOK = false;
		long start = System.currentTimeMillis();
		while (!exitOK) {
			try {
				exitValue = p.exitValue();
				exitOK = true;
			} catch (IllegalThreadStateException e) {
				Thread.sleep(10);
			}
			if (!exitOK && System.currentTimeMillis() >= (start + timeout)) {
				throw new TimeoutException("Timeout: process not terminated.");
			}
		}
		return exitValue;
	}
	
	/**
	 * Retorna uma lista com os nomes dos subdiretorios contidos em um diretorio. 
	 * @param dir Caminho do diretorio a ser listado.
	 * @return Lista com os nomes dos subdiretorios contidos em <i>dir</i>.
	 */
	public static Collection<String> lsDir(String dir) {
		Collection<String> resultList = new TreeSet<String>();
		File rootDir = new File(dir);
		if (rootDir != null && rootDir.isDirectory()) {

			File[] subDirs = rootDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return (pathname.isDirectory()
							&& !".".equals(pathname.getName())
							&& !"..".equals(pathname.getName()));
				}
			});
			
			for (File subDir : subDirs) {
				resultList.add(subDir.getName());
			}
		}
		return resultList;
	}

	/**
	 * Cria um diretorio no sistema de arquivos (nao recursivo).
	 * @param dir Caminho do diretorio a ser criado
	 * @return True se diretorio foi criado, false se houve erro.
	 */
	public static boolean mkDir(String dir) {
		File rootDir = new File(dir);
		return rootDir.mkdir();
	}
	
	/**
	 * Remove um diretorio e todo seu conteudo (recursivo).
	 * @param dir Caminho do diretorio a ser removido.
	 * @return Objeto Process, representando o processo do comando chamado. 
	 * @throws IOException
	 */
	public static Process rmDir(String dir) throws IOException {
		String[] command;
		String os = System.getProperty("os.name");
		if (os != null && os.toLowerCase().startsWith("windows")) {
			command = new String[RM_COMMAND_WIN.length + 1];
			System.arraycopy(RM_COMMAND_WIN, 0, command, 0, RM_COMMAND_WIN.length);
			dir = dir.replace('/', '\\');
		} else {
			command = new String[RM_COMMAND_UNIX.length + 1];
			System.arraycopy(RM_COMMAND_UNIX, 0, command, 0, RM_COMMAND_UNIX.length);
		}
		command[command.length - 1] = dir;
		
		if (trace) {
			StringBuilder commandLine = new StringBuilder();
			for (String s : command) {
				if (commandLine.length() > 0) { commandLine.append(' '); }
				commandLine.append(s);
			}
			logger.trace(String.format("RMDIR command-line: %s", commandLine.toString()));
		}
		
		return executeCommand(command);
	}

	/**
	 * Retorna uma String contendo a saida do console de erro de um processo (stderr).
	 * @param p Objeto Process, que representa um processo.
	 * @return Saida do console de erro (stderr).
	 * @throws IOException
	 */
	public static String getProcessErrorStr(Process p) throws IOException {
		BufferedReader in =
			new BufferedReader(new InputStreamReader(p.getErrorStream()));
		StringBuilder errorStr = new StringBuilder();
		String line = null;
		while ((line = in.readLine()) != null) {
			errorStr.append(line).append("\n");
		}
		return errorStr.toString();
	}

	/**
	 * Retorna uma String contendo a saida do console de saida de um processo (stdout).
	 * @param p Objeto Process, que representa um processo.
	 * @return Saida do console de saida (stdout).
	 * @throws IOException
	 */
	public static String getProcessOutStr(Process p) throws IOException {
		BufferedReader in =
			new BufferedReader(new InputStreamReader(p.getInputStream()));
		StringBuilder outStr = new StringBuilder();
		String line = null;
		while ((line = in.readLine()) != null) {
			outStr.append(line).append("\n");
		}
		return outStr.toString();
	}
}
