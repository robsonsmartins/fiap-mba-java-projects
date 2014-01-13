import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import netscape.javascript.JSObject;
import sun.misc.BASE64Encoder;

/**
 * Applet para selecionar um certificado do cliente a partir de
 *   dispositivos criptograficos (tokens ou smart cards).<p/>
 * <b>Parametros:</b>
 * <ul>
 * <li><i>form</i>: ID do form onde a applet esta' inserida (na pagina HTML).</li>
 * <li><i>username</i>: Nome do campo username (default: j_username).</li>
 * <li><i>password</i>: Nome do campo password (default: j_password).</li>
 * </ul>
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class SICidApplet extends JApplet {

	/* componentes 'swing' */
	private JDesktopPane deskPane;

	private JInternalFrame dlgProviders;
	@SuppressWarnings("rawtypes")
	private JList listProviders;
	@SuppressWarnings("rawtypes")
	private DefaultListModel listModelProviders;
	private JPanel btnProviderPanel;
	private JButton btnProviderOk;
	private JButton btnProviderRefresh;
	private JButton btnPkcs12;
	
	private JInternalFrame dlgCerts;
	@SuppressWarnings("rawtypes")
	private JList listCerts;
	@SuppressWarnings("rawtypes")
	private DefaultListModel listModelCerts;
	private JPanel btnCertPanel;
	private JButton btnCertOk;
	private JButton btnCertCancelar;
	
	/* lista de providers disponiveis */
	private List<Provider> providers;
	/* lista de certificados presentes */
	private List<KeyStore.PrivateKeyEntry> certs;
	/* provider selecionado */
	private Provider selectedProvider;
	/* certificado selecionado */
	private KeyStore.PrivateKeyEntry selectedCert;
	
	/* objetos para interacao com o browser (JavaScript) */
	private JSObject browserWindow;
	private JSObject mainForm;
	private JSObject usernameField;
	private JSObject passwordField;
	/* id do form onde a applet esta' inserida */
	private String formId;
	/* nome dos campos de autenticacao */
	private String usernameFieldName;
	private String passwordFieldName;
	
	/**
	 * Inicializa a applet. 
	 */
	@Override
	public void init() {

		try {
			formId            = getParameter("form"    );
			usernameFieldName = getParameter("username");
			passwordFieldName = getParameter("password");

			browserWindow = JSObject.getWindow(this);
				
			if (formId != null && !"".equals(formId)) {
				mainForm = (JSObject) browserWindow.eval(
						String.format("document.getElementById('%s')", formId));
			} else {
				mainForm = (JSObject) browserWindow.eval("document.forms[0]");
			}
	
			if (usernameFieldName == null || "".equals(usernameFieldName)) {
				usernameFieldName = "j_username";
			}
			
			if (passwordFieldName == null || "".equals(passwordFieldName)) {
				passwordFieldName = "j_password";
			}
			
			usernameField = (JSObject) mainForm.getMember(usernameFieldName);
			passwordField = (JSObject) mainForm.getMember(passwordFieldName);

		} catch (Exception e) {	
			JOptionPane.showMessageDialog(null,"Erro ao iniciar applet.",
					"SICid Login Applet", JOptionPane.ERROR_MESSAGE);
		} 

		try {
			deskPane = new JDesktopPane();
			add(deskPane);

			createDlgProviders();
			createDlgCerts();

			deskPane.add(dlgProviders);
			deskPane.add(dlgCerts);
			
			dlgProviders.setMaximum(true);
			dlgCerts.setMaximum(true);
			
			dlgProviders.setVisible(true);
			dlgCerts.setVisible(false);
			
		} catch (Exception e) {	
			
			JOptionPane.showMessageDialog(null,"Erro ao exibir janela.",
					"SICid Login Applet", JOptionPane.ERROR_MESSAGE);
		} 

		refreshProviderList();
	}

	/* Cria o dialogo de selecao de provider. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createDlgProviders() {
		dlgProviders = new JInternalFrame("Selecione o Dispositivo");
		dlgProviders.setLayout(new BorderLayout(0, 0));
		
		listModelProviders = new DefaultListModel();

		listProviders = new JList();
		listProviders.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		listProviders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listProviders.setModel(listModelProviders);
		listProviders.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				btnProviderOk.setEnabled(listProviders.getSelectedIndex() >= 0);
			}
		});
		listProviders.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e){
				if (e.getClickCount() == 2){
					int idx = listProviders.getSelectedIndex();
					if (idx >= 0) {
						selectedProvider = providers.get(idx);
						dlgProviders.setVisible(false);
						dlgCerts.setVisible(true);
						refreshCertListFromProvider();
					} else {
						selectedProvider = null;
					}
				}
			}
		});
		
		dlgProviders.add(new JScrollPane(listProviders), BorderLayout.CENTER);
		
		btnProviderPanel = new JPanel();
		dlgProviders.add(btnProviderPanel, BorderLayout.SOUTH);
		btnProviderPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		btnProviderOk = new JButton("OK");
		btnProviderOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int idx = listProviders.getSelectedIndex();
				if (idx >= 0) {
					selectedProvider = providers.get(idx);
				} else {
					selectedProvider = null;
				}
				dlgProviders.setVisible(false);
				dlgCerts.setVisible(true);
				refreshCertListFromProvider();
			}
		});
		btnProviderOk.setEnabled(false);
		btnProviderPanel.add(btnProviderOk);
		
		btnProviderRefresh = new JButton("Recarregar");
		btnProviderRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshProviderList();
			}
		});
		btnProviderPanel.add(btnProviderRefresh);
		
		btnPkcs12 = new JButton("Arquivo");
		btnPkcs12.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlgProviders.setVisible(false);
				dlgCerts.setVisible(true);
				refreshCertListFromPkcs12();
				selectedProvider = null;
			}
		});
		btnProviderPanel.add(btnPkcs12);
		
	}

	/* Cria o dialogo de selecao de certificado. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createDlgCerts() {
		dlgCerts = new JInternalFrame("Selecione o Certificado");
		dlgCerts.setLayout(new BorderLayout(0, 0));

		listModelCerts = new DefaultListModel();

		listCerts = new JList();
		listCerts.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		listCerts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listCerts.setModel(listModelCerts);
		listCerts.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				btnCertOk.setEnabled(listCerts.getSelectedIndex() >= 0);
			}
		});
		listCerts.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e){
				if (e.getClickCount() == 2){
					int idx = listCerts.getSelectedIndex();
					if (idx >= 0) {
						selectedCert = certs.get(idx);
						dlgProviders.setVisible(false);
						dlgCerts.setVisible(false);
						postCertificate();
					} else {
						selectedCert = null;
					}
				}
			}
		});
		
		dlgCerts.add(new JScrollPane(listCerts), BorderLayout.CENTER);
		
		btnCertPanel = new JPanel();
		dlgCerts.add(btnCertPanel, BorderLayout.SOUTH);
		btnCertPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		btnCertOk = new JButton("OK");
		btnCertOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int idx = listCerts.getSelectedIndex();
				if (idx >= 0) {
					selectedCert = certs.get(idx);
					dlgProviders.setVisible(false);
					dlgCerts.setVisible(false);
					postCertificate();
				} else {
					selectedCert = null;
				}
			}
		});
		btnCertOk.setEnabled(false);
		btnCertPanel.add(btnCertOk);
		
		btnCertCancelar = new JButton("Voltar");
		btnCertCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlgCerts.setVisible(false);
				dlgProviders.setVisible(true);
				selectedCert = null;
			}
		});
		btnCertCancelar.setEnabled(false);
		btnCertPanel.add(btnCertCancelar);
	}
	
	/* Carrega o conjunto de providers presentes no sistema. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadProviders() {
		AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				try {
					providers = CryptoDeviceManager.getProviderList();
				} catch (Exception e) { 
					JOptionPane.showMessageDialog(null,
							"Erro ao carregar lista de dispositivos.",
							"SICid Login Applet", JOptionPane.ERROR_MESSAGE);
					providers = null;
				}	
				return null;
			}
		});
	}

	/* Carrega o conjunto de certificados presentes no dispositivo. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadCertsFromProvider() {
		AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				try {
					certs = CryptoDeviceManager.getCertificates(selectedProvider);
				} catch (Exception e) { 
					JOptionPane.showMessageDialog(null,
							"Erro ao carregar lista de certificados.",
							"SICid Login Applet", JOptionPane.ERROR_MESSAGE);
					certs = null;
				}	
				return null;
			}
		});
	}
	
	/* Carrega o conjunto de certificados presentes em um arquivo PKCS#12. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadCertsFromPkcs12() {
		AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				try {
					File ksFile = selectPkcsFile();
					if (ksFile == null) { return null; }
					certs = CryptoDeviceManager.getCertificatesFromFile(ksFile);
				} catch (Exception e) { 
					JOptionPane.showMessageDialog(null,
							"Erro ao carregar lista de certificados.",
							"SICid Login Applet", JOptionPane.ERROR_MESSAGE);
					certs = null;
				}	
				return null;
			}
		});
	}
	
	/* Exibe um dialogo de selecao e retorna um arquivo PKCS#12.
	 * @return Objeto {@link File} representando o arquivo PKCS#12 selecionado. */
	private File selectPkcsFile() {
		JFileChooser fileDlg = new JFileChooser();
		FileFilter filter = new FileFilter() {
			@Override
			public String getDescription() {
				return "Arquivos de certificado PKCS#12 (*.pfx,*.p12)";
			}
			@Override
			public boolean accept(File f) {
				return (f.isDirectory() || 
						f.getAbsolutePath().toLowerCase().endsWith(".pfx") ||
						f.getAbsolutePath().toLowerCase().endsWith(".p12"));
			}
		};
		fileDlg.setFileFilter(filter);
		if (fileDlg.showOpenDialog(deskPane) == JFileChooser.APPROVE_OPTION) {
			return fileDlg.getSelectedFile();
		} else {
			return null;
		}
	}

	/* Recarrega a lista de providers. */
	@SuppressWarnings("unchecked")
	private void refreshProviderList() {
		btnProviderOk.setEnabled(false);
		btnProviderRefresh.setEnabled(false);
		btnPkcs12.setEnabled(false);
		listModelProviders.clear();
		dlgProviders.paintImmediately(dlgProviders.getBounds());
		selectedProvider = null;
		dlgProviders.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		loadProviders();
		dlgProviders.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		if (providers != null) {
			for (Provider p : providers) {
				listModelProviders.addElement(p.toString());
			}
		}
		listProviders.setSelectedIndex((listModelProviders.size()) > 0 ? 0 : -1);
		btnProviderOk.setEnabled(listProviders.getSelectedIndex() >= 0);
		btnProviderRefresh.setEnabled(true);
		btnPkcs12.setEnabled(true);
	}
	
	/* Recarrega a lista de certificados,
	 * a partir de um dispositivo */
	private void refreshCertListFromProvider() {
		refreshCertList(false);
	}

	/* Recarrega a lista de certificados,
	 * a partir de um arquivo PKCS#12 */
	private void refreshCertListFromPkcs12() {
		refreshCertList(true);
	}

	/* Recarrega a lista de certificados,
	 * @param pkcs12 Se true, indica que a lista sera'
	 *   obtida a partir de um arquivo PKCS#12. Se false,
	 *   a lista sera' obtida a partir de um provider. */
	@SuppressWarnings("unchecked")
	private void refreshCertList(boolean pkcs12) {
		btnCertOk.setEnabled(false);
		btnCertCancelar.setEnabled(false);
		btnPkcs12.setEnabled(false);
		listModelCerts.clear();
		dlgCerts.paintImmediately(dlgCerts.getBounds());
		selectedCert = null;
		dlgCerts.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		if (pkcs12) {
			loadCertsFromPkcs12();
		} else {
			loadCertsFromProvider();
		}
		dlgCerts.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		if (certs != null) {
			for (KeyStore.PrivateKeyEntry entry : certs) {
				X509Certificate cert =
					(X509Certificate) entry.getCertificate();
				List<String> listCN = 
					CryptoDeviceManager.subjectParse(
						cert.getSubjectX500Principal().getName(),
						"CN"); 
				if (listCN.size() != 0) {
					listModelCerts.addElement(listCN.get(0));
				} else {
					listModelCerts.addElement("Unknown");
				}
			}
		}
		listCerts.setSelectedIndex((listModelCerts.size()) > 0 ? 0 : -1);
		btnCertOk.setEnabled(listCerts.getSelectedIndex() >= 0);
		btnCertCancelar.setEnabled(true);
		btnPkcs12.setEnabled(true);
	}
	
	/* Preenche os campos do form e realiza o submit */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void postCertificate() {
		AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				if (selectedCert != null) {
					try {
						X509Certificate cert = 
							(X509Certificate) selectedCert.getCertificate(); 
						String username = 
							CertificadoSerializador.certToStr(cert);
						String password =
							new BASE64Encoder().encode(
									CertificadoAssinador.sign(selectedProvider, "SHA1withRSA",
											selectedCert.getPrivateKey(), cert.getEncoded()));
						
						usernameField.setMember("value", username);
						passwordField.setMember("value", password);
						browserWindow.eval(String.format(
								"document.getElementById('%s').submit();", formId));
						
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null,
								"Erro ao submeter o certificado digital.",
								"SICid Login Applet", JOptionPane.ERROR_MESSAGE);
					}
				}
				return null;
			}
		});
	}
}
