package icp.web.controller;

import icp.bean.Ca;
import icp.bean.Certificado;
import icp.bean.TipoCertificado;
import icp.dao.CaDAO;
import icp.dao.CertificadoDAO;
import icp.model.IIcpAdmin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import com.robsonmartins.fiap.tcc.util.FacesUtil;
import com.robsonmartins.fiap.tcc.util.JBossUtil;

/**
 * Classe responsavel por fornecer o acesso 'as funcionalidades de
 *   Gerenciamento de uma ICP (PKI) 'a camada de visualizacao do JSF. 
 * @author Robson Martins (robson@robsonmartins.com)
 */
@SuppressWarnings("serial")
public class IcpBrasilMB extends AbstractIcpAdminMB {
	
	/* listas de AC */
	private List<Ca> listaACRaiz;
	private List<Ca> listaACIntermediaria;
	private List<Ca> listaAC;
	
	/* lista de certificados */
	private List<Certificado> listaCert;
	
	/* objetos selecionados na camada de visualizacao */
	private Ca acSelecionada;
	private Certificado certSelecionado;
	private String actionSelecionada;
	
	/* parametros dos forms da camada de visualizacao */
	private AcParams acParams;
	private CertParams certParams;

	/* conversores de objetos do JSF */
	private CaConverter caConverter;
	private CertConverter certConverter;
	private TipoCertConverter tipoCertConverter;

	/**
	 * Cria uma nova instancia do controller.
	 * @param engine "Motor" do ICP Admin (camada model).
	 */
	public IcpBrasilMB(IIcpAdmin engine) {
		super(engine);

		acParams = new AcParams();
		certParams = new CertParams();
		
		listaACRaiz          = new ArrayList<Ca>();
		listaACIntermediaria = new ArrayList<Ca>();
		listaAC              = new ArrayList<Ca>();
		listaCert            = new ArrayList<Certificado>();
		
		recarregarListasAC();
		atualizarStatusCertExpirados();
		recarregarListaCert();
		
		caConverter = new CaConverter(listaAC);
		certConverter = new CertConverter(listaCert);
		tipoCertConverter = new TipoCertConverter();
	}
	
	/**
	 * Retorna a lista corrente de AC's Raiz.
	 * @return Lista de AC's Raiz.
	 */
	public List<Ca> getListarACRaiz() {
		return listaACRaiz;
	}
	
	/**
	 * Retorna a lista corrente de AC's Intermediarias.
	 * @return Lista de AC's Intermediarias.
	 */
	public List<Ca> getListarACInterm() {
		return listaACIntermediaria;
	}

	/**
	 * Retorna a lista corrente de todas AC's.
	 * @return Lista de AC's.
	 */
	public List<Ca> getListarAC() {
		return listaAC;
	}
	
	/**
	 * Retorna a lista dos Tipos de Certificado implementados.
	 * @return Lista de Tipos de Certificado.
	 */
	public List<TipoCertificado> getListarTipoCert() {
		return icpAdmin.listarTipoCert();
	}
	
	/**
	 * Retorna a lista corrente de Certificados.
	 * @return Lista de Certificados.
	 */
	public List<Certificado> getListarCert() {
		return listaCert;
	}
	
	/**
	 * Action para adicionar uma AC Raiz.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public synchronized String adicionarACRaiz() {
		try {
			icpAdmin.adicionarACRaiz(acParams.getName(), acParams.getCrlURI(), acParams.getCpsURI(),
					acParams.getKeyPassword(), acParams.getSubjC(), acParams.getSubjO(),
					acParams.getSubjOU(), acParams.getSubjCN());
			FacesUtil.addFacesMessage("AC Raiz adicionada.", null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao criar nova AC Raiz.", e.getLocalizedMessage(),
					FacesMessage.SEVERITY_ERROR);
			return null;
		}
		acSelecionada = null;
		resetAcParams();
		recarregarListasAC();
		return "acraiz";
	}

	/**
	 * Action para adicionar uma AC Intermediaria.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public synchronized String adicionarACInterm() {
		try {
			Ca acRaiz = CaDAO.getCaFromListByName(listaAC, acSelecionada.getName());
			if (acRaiz == null) {
				throw new Exception("CA '" + acSelecionada.getName() + "' not found.");
			}
			icpAdmin.adicionarACInterm(acRaiz, acParams.getName(), acParams.getCrlURI(), acParams.getCpsURI(),
					acParams.getKeyPassword(), acParams.getCaPassword(), acParams.getSubjC(), acParams.getSubjO(),
					acParams.getSubjOU(), acParams.getSubjCN());
			FacesUtil.addFacesMessage("AC Intermedi\u00E1ria adicionada.", null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao criar nova AC Intermedi\u00E1ria.", e.getLocalizedMessage(),
					FacesMessage.SEVERITY_ERROR);
			return null;
		}
		acSelecionada = null;
		resetAcParams();
		recarregarListasAC();
		return "acinterm";
	}

	/**
	 * Action para remover uma AC Raiz.
	 */
	public synchronized void removerACRaiz() {
		if (acSelecionada != null) {
			if ("icprmartins".equalsIgnoreCase(acSelecionada.getName()) && 
					JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
				FacesUtil.addFacesMessage(
						"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
						null, FacesMessage.SEVERITY_WARN);
			} else {
				try {
					icpAdmin.removerACRaiz(acSelecionada.getName());
					FacesUtil.addFacesMessage("AC '" + acSelecionada.getName() + "' removida com sucesso.", null,
							FacesMessage.SEVERITY_INFO);
					recarregarListasAC();
					recarregarListaCert();
					acSelecionada = null;
				} catch (Exception e) {
					FacesUtil.addFacesMessage("Erro ao remover AC Raiz.", e.getLocalizedMessage(),
							FacesMessage.SEVERITY_ERROR);
				}
			}
		}
	}

	/**
	 * Action para remover uma AC Intermediaria.
	 */
	public synchronized void removerACInterm() {
		if (acSelecionada != null) {
			if ("acrmartins".equalsIgnoreCase(acSelecionada.getName()) && 
					JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
				FacesUtil.addFacesMessage(
						"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
						null, FacesMessage.SEVERITY_WARN);
			} else {
				try {
					icpAdmin.removerACInterm(acSelecionada.getName());
					FacesUtil.addFacesMessage("AC '" + acSelecionada.getName() + "' removida com sucesso.", null,
							FacesMessage.SEVERITY_INFO);
					recarregarListasAC();
					recarregarListaCert();
					acSelecionada = null;
				} catch (Exception e) {
					FacesUtil.addFacesMessage("Erro ao remover AC Intermedi\u00E1ria.", e.getLocalizedMessage(),
							FacesMessage.SEVERITY_ERROR);
				}
			}
		}
	}
	
	/**
	 * Action para emitir a LCR de uma AC Raiz.
	 */
	public synchronized void criarACRaizLCR() {
		try {
			Ca ac = CaDAO.getCaFromListByName(listaAC, acSelecionada.getName());
			if (ac == null) {
				throw new Exception("CA '" + acSelecionada.getName() + "' not found.");
			}
			icpAdmin.criarACRaizLCR(ac, acParams.getCaPassword());
			FacesUtil.addFacesMessage("LCR emitida com sucesso.", null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao criar a LCR da AC.", e.getLocalizedMessage(),
					FacesMessage.SEVERITY_ERROR);
		}
		resetAcParams();
	}
	
	/**
	 * Action para emitir a LCR de uma AC Intermediaria.
	 */
	public synchronized void criarACIntermLCR() {
		try {
			Ca ac = CaDAO.getCaFromListByName(listaAC, acSelecionada.getName());
			if (ac == null) {
				throw new Exception("CA '" + acSelecionada.getName() + "' not found.");
			}
			icpAdmin.criarACIntermLCR(ac, acParams.getCaPassword());
			FacesUtil.addFacesMessage("LCR emitida com sucesso.", null, FacesMessage.SEVERITY_INFO);
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao criar a LCR da AC.", e.getLocalizedMessage(),
					FacesMessage.SEVERITY_ERROR);
		}
		resetAcParams();
	}

	/**
	 * Action para realizar download de certificado de uma AC.
	 */
	public synchronized void downloadCertAC() {
		try {
			Ca ac = CaDAO.getCaFromListByName(listaAC, acSelecionada.getName());
			if (ac == null) {
				throw new Exception("CA '" + acSelecionada.getName() + "' not found.");
			}
			downloadFile(ac.getCertFile(), 
					acSelecionada.getName() + ".cer",
					"application/x-x509-ca-cert");
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao realizar o download do certificado da AC.", e.getLocalizedMessage(),
					FacesMessage.SEVERITY_ERROR);
		}
	}
	
	/**
	 * Action para realizar download de uma cadeia de certificados de uma AC.
	 */
	public synchronized void downloadCertChainAC() {
		try {
			Ca ac = CaDAO.getCaFromListByName(listaAC, acSelecionada.getName());
			if (ac == null) {
				throw new Exception("CA '" + acSelecionada.getName() + "' not found.");
			}
			downloadFile(ac.getCertChainFile(), 
					acSelecionada.getName() + ".p7b",
					"application/pkcs7-mime");
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao realizar o download da cadeia de certificados da AC.",
					e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}

	/**
	 * Action para realizar download da LCR de uma AC.
	 */
	public synchronized void downloadLcrAC() {
		try {
			Ca ac = CaDAO.getCaFromListByName(listaAC, acSelecionada.getName());
			if (ac == null) {
				throw new Exception("CA '" + acSelecionada.getName() + "' not found.");
			}
			downloadFile(ac.getCrlFile(), 
					acSelecionada.getName() + ".crl",
					"application/x-pkcs7-crl");
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao realizar o download da LCR da AC.", e.getLocalizedMessage(),
					FacesMessage.SEVERITY_ERROR);
		}
	}

	/**
	 * Action para realizar download de um certificado.
	 */
	public synchronized void downloadCert() {
		try {
			Certificado cert = CertificadoDAO.getCertfromListById(listaCert, certSelecionado.getId());
			if (cert == null) {
				throw new Exception("Certificate ID '" + certSelecionado.getId() + "' not found.");
			}
			String cn = cert.getCommonName();
			if (cn == null) { cn = String.valueOf(cert.getId()); }
			downloadFile(icpAdmin.getBaseDir() + "/" + cert.getCertFilename(), 
					cn.replace(':', '-').replace(' ', '_') + ".cer",
					"application/x-x509-ca-cert");
		} catch (Exception e) {
			FacesUtil.addFacesMessage("Erro ao realizar o download do certificado.", e.getLocalizedMessage(),
					FacesMessage.SEVERITY_ERROR);
		}
	}
	
	/**
	 * Action para realizar download de um certificado PKCS#12.
	 */
	public synchronized void downloadCertPkcs12() {
		if (("icprmartins".equalsIgnoreCase(acSelecionada.getName()) ||
				"acrmartins".equalsIgnoreCase(acSelecionada.getName())) && 
				JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				Certificado cert = CertificadoDAO.getCertfromListById(listaCert, certSelecionado.getId());
				if (cert == null) {
					throw new Exception("Certificate ID '" + certSelecionado.getId() + "' not found.");
				}
				String cn = cert.getCommonName();
				if (cn == null) { cn = String.valueOf(cert.getId()); }
				downloadFile(icpAdmin.getBaseDir() + "/" +
						cert.getCertFilename().replace(".pem.cer",".pfx"), 
						cn.replace(':', '-').replace(' ', '_') + ".pfx",
						"application/x-pkcs12");
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao realizar o download do certificado.", e.getLocalizedMessage(),
						FacesMessage.SEVERITY_ERROR);
			}
		}
	}

	/**
	 * Action para realizar download de uma chave privada.
	 */
	public synchronized void downloadKey() {
		if (("icprmartins".equalsIgnoreCase(acSelecionada.getName()) ||
				"acrmartins".equalsIgnoreCase(acSelecionada.getName())) && 
				JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				Certificado cert = CertificadoDAO.getCertfromListById(listaCert, certSelecionado.getId());
				if (cert == null) {
					throw new Exception("Certificate ID '" + certSelecionado.getId() + "' not found.");
				}
				String cn = cert.getCommonName();
				if (cn == null) { cn = String.valueOf(cert.getId()); }
				downloadFile(icpAdmin.getBaseDir() + "/" + cert.getKeyFilename(), 
						cn.replace(':', '-').replace(' ', '_') + ".key",
						"application/x-pem-key");
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao realizar o download da chave privada.", e.getLocalizedMessage(),
						FacesMessage.SEVERITY_ERROR);
			}
		}
	}

	/**
	 * Action para realizar download de uma chave privada descriptografada.
	 */
	public synchronized void downloadKeyPlain() {
		if (("icprmartins".equalsIgnoreCase(acSelecionada.getName()) ||
				"acrmartins".equalsIgnoreCase(acSelecionada.getName())) && 
				JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				Certificado cert = CertificadoDAO.getCertfromListById(listaCert, certSelecionado.getId());
				if (cert == null) {
					throw new Exception("Certificate ID '" + certSelecionado.getId() + "' not found.");
				}
				String cn = cert.getCommonName();
				if (cn == null) { cn = String.valueOf(cert.getId()); }
				downloadFile(icpAdmin.getBaseDir() + "/" +
						cert.getKeyFilename().replace(".pem", ".plain"), 
						cn.replace(':', '-').replace(' ', '_') + ".key",
						"application/x-pem-key");
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao realizar o download da chave privada.", e.getLocalizedMessage(),
						FacesMessage.SEVERITY_ERROR);
			}
		}
	}

	/**
	 * Action para emitir um novo certificado.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public synchronized String emitirCert() {
		if (("icprmartins".equalsIgnoreCase(acSelecionada.getName()) ||
				"acrmartins".equalsIgnoreCase(acSelecionada.getName())) && 
				JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
			return null;
		} else {
			try {
				Ca acEmissora = CaDAO.getCaFromListByName(listaAC, acSelecionada.getName());
				if (acEmissora == null) {
					throw new Exception("CA '" + acSelecionada.getName() + "' not found.");
				}
				emitirCertPorTipo(acEmissora, certParams);
				FacesUtil.addFacesMessage("Certificado emitido com sucesso.", null, FacesMessage.SEVERITY_INFO);
				certSelecionado = null;
				resetCertParams();
				recarregarListaCert();
				return "cert";
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao emitir o certificado.", e.getLocalizedMessage(),
						FacesMessage.SEVERITY_ERROR);
				return null;
			}
		}
	}

	/**
	 * Action para revogar um certificado.
	 */
	public synchronized void revogarCert() {
		if (("icprmartins".equalsIgnoreCase(acSelecionada.getName()) ||
				"acrmartins".equalsIgnoreCase(acSelecionada.getName())) && 
				JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				Ca acEmissora = CaDAO.getCaFromListByName(listaAC, acSelecionada.getName());
				if (acEmissora == null) {
					throw new Exception("CA '" + acSelecionada.getName() + "' not found.");
				}
				Certificado cert = CertificadoDAO.getCertfromListById(listaCert, certSelecionado.getId());
				if (cert == null) {
					throw new Exception("Certificate ID '" + certSelecionado.getId() + "' not found.");
				}
				icpAdmin.revogarCert(acEmissora, cert, certParams.getCaPassword());
				FacesUtil.addFacesMessage("Certificado revogado com sucesso.", null, FacesMessage.SEVERITY_INFO);
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao revogar o certificado.", e.getLocalizedMessage(),
						FacesMessage.SEVERITY_ERROR);
			}
		}
		resetCertParams();
		recarregarListaCert();
	}
	
	/**
	 * Action para renovar o certificado de uma AC Raiz.
	 */
	public synchronized void renovarACRaizCert() {
		if ("icprmartins".equalsIgnoreCase(acSelecionada.getName()) && 
				JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				Ca acEmissora = CaDAO.getCaFromListByName(listaAC, acSelecionada.getName());
				if (acEmissora == null) {
					throw new Exception("CA '" + acSelecionada.getName() + "' not found.");
				}
				icpAdmin.renovarACRaizCert(acEmissora, acParams.getCaPassword());
				FacesUtil.addFacesMessage("Certificado renovado com sucesso.", null, FacesMessage.SEVERITY_INFO);
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao renovar o certificado da AC Raiz.", e.getLocalizedMessage(),
						FacesMessage.SEVERITY_ERROR);
			}
		}
		resetAcParams();
	}
	
	/**
	 * Action para renovar o certificado de uma AC Intermediaria.
	 */
	public synchronized void renovarACIntermCert() {
		if ("acrmartins".equalsIgnoreCase(acSelecionada.getName()) && 
				JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				Ca acEmissora = CaDAO.getCaFromListByName(listaAC, acSelecionada.getName());
				if (acEmissora == null) {
					throw new Exception("CA '" + acSelecionada.getName() + "' not found.");
				}
				icpAdmin.renovarACIntermCert(acEmissora, acParams.getCaPassword());
				FacesUtil.addFacesMessage("Certificado renovado com sucesso.", null, FacesMessage.SEVERITY_INFO);
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao renovar o certificado da AC.", e.getLocalizedMessage(),
						FacesMessage.SEVERITY_ERROR);
			}
		}
		resetAcParams();
	}

	/**
	 * Action para renovar um certificado.
	 */
	public synchronized void renovarCert() {
		if (("icprmartins".equalsIgnoreCase(acSelecionada.getName()) ||
				"acrmartins".equalsIgnoreCase(acSelecionada.getName())) && 
				JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				Ca acEmissora = CaDAO.getCaFromListByName(listaAC, acSelecionada.getName());
				if (acEmissora == null) {
					throw new Exception("CA '" + acSelecionada.getName() + "' not found.");
				}
				Certificado cert = CertificadoDAO.getCertfromListById(listaCert, certSelecionado.getId());
				if (cert == null) {
					throw new Exception("Certificate ID '" + certSelecionado.getId() + "' not found.");
				}
				icpAdmin.renovarCert(acEmissora, cert, certParams.getCaPassword());
				FacesUtil.addFacesMessage("Certificado renovado com sucesso.", null, FacesMessage.SEVERITY_INFO);
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao renovar o certificado.", e.getLocalizedMessage(),
						FacesMessage.SEVERITY_ERROR);
			}
		}
		resetCertParams();
		recarregarListaCert();
	}

	/**
	 * Action para realizar download do backup da ICP.
	 */
	public synchronized void downloadBackupIcp() {
		if (JBossUtil.TCC_FIAP_RESTRICT_USER_CHANGES) {
			FacesUtil.addFacesMessage(
					"Fun\u00E7\u00E3o n\u00E3o dispon\u00EDvel nesta instala\u00E7\u00E3o.",
					null, FacesMessage.SEVERITY_WARN);
		} else {
			try {
				String outFileName = icpAdmin.getBaseDir() + ".tar.gz";
				icpAdmin.gerarBackup(outFileName);
				
				downloadFile(outFileName, 
						String.format("icpadmin-%s.tar.gz",
								new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())),
						"application/x-gzip");
			} catch (Exception e) {
				FacesUtil.addFacesMessage("Erro ao realizar o download do backup da ICP.",
						e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
		}
	}

	/**
	 * Action para recarregar a lista de certificados, de acordo com a AC selecionada.
	 */
	public void recarregarListaCert() {
		listaCert.clear();
		if (acSelecionada != null && !".".equals(acSelecionada.getName())) {
			listaCert.addAll(icpAdmin.listarCert(acSelecionada.getName()));
		}
		certSelecionado = null;
	}

	/**
	 * Action para redirecionar ao cadastro de AC Raiz.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public String goToAcRaiz() {
		acSelecionada = null;
		certSelecionado = null;
		return "acraiz";
	}

	/**
	 * Action para redirecionar ao cadastro de AC Intermediaria.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public String goToAcInterm() {
		acSelecionada = null;
		certSelecionado = null;
		return "acinterm";
	}

	/**
	 * Action para redirecionar ao cadastro de Certificados.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public String goToCert() {
		certSelecionado = null;
		recarregarListaCert();
		return "cert";
	}
	
	/**
	 * Action para redirecionar ao formulario: Adicionar AC Raiz.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public String goToNewAcRaiz() {
		resetAcParams();
		return "newacraiz";
	}
	
	/**
	 * Action para redirecionar ao formulario: Adicionar AC Intermediaria.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public String goToNewAcInterm() {
		resetAcParams();
		return "newac";
	}
	
	/**
	 * Action para redirecionar ao formulario: Emitir Certificado.
	 * @return String com o nome do destino (target) do redirecionamento da action.
	 */
	public String goToNewCert() {
		resetCertParams();
		return "newcert";
	}
	
	/**
	 * Limpa todos os parametros do formulario de cadastro de AC.
	 */
	public void resetAcParams() {
		this.acParams.clear();
	}

	/**
	 * Limpa todos os parametros do formulario de emissao de Certificado.
	 */
	public void resetCertParams() {
		this.certParams.clear();
	}

	/**
	 * Retorna o conversor de objetos {@link Ca} para o JSF.
	 * @return Objeto conversor JSF {@link CaConverter}.
	 */
	public CaConverter getCaConverter() {
		caConverter.setCaList(listaAC);
		return caConverter;
	}

	/**
	 * Retorna o conversor de objetos {@link Certificado} para o JSF.
	 * @return Objeto conversor JSF {@link CertConverter}.
	 */
	public CertConverter getCertConverter() {
		certConverter.setCertList(listaCert);
		return certConverter;
	}

	/**
	 * Retorna o conversor de objetos {@link TipoCertificado} para o JSF.
	 * @return Objeto conversor JSF {@link TipoCertConverter}.
	 */
	public TipoCertConverter getTipoCertConverter() {
		return tipoCertConverter;
	}

	/**
	 * Configura o objeto da AC selecionada na camada de visualizacao.
	 * @param acSelecionada AC selecionada.
	 */
	public void setAcSelecionada(Ca acSelecionada) {
		this.acSelecionada = acSelecionada;
	}

	/**
	 * Retorna o objeto da AC selecionada na camada de visualizacao.
	 * @return AC selecionada.
	 */
	public Ca getAcSelecionada() {
		return acSelecionada;
	}

	/**
	 * Configura o objeto do Certificado selecionado na camada de visualizacao.
	 * @param certSelecionado Certificado selecionado.
	 */
	public void setCertSelecionado(Certificado certSelecionado) {
		this.certSelecionado = certSelecionado;
	}

	/**
	 * Retorna o objeto do Certificado selecionado na camada de visualizacao.
	 * @return Certificado selecionado.
	 */
	public Certificado getCertSelecionado() {
		return certSelecionado;
	}

	/**
	 * Retorna a string contendo a Action selecionada na camada de visualizacao.
	 * @return Action selecionada.
	 */
	public String getActionSelecionada() {
		return actionSelecionada;
	}

	/**
	 * Configura a string contendo a Action selecionada na camada de visualizacao.
	 * @param actionSelecionada Action selecionada.
	 */
	public void setActionSelecionada(String actionSelecionada) {
		this.actionSelecionada = actionSelecionada;
	}

	/**
	 * Retorna o objeto com os parametros de formulario de cadastro de AC.
	 * @return Parametros do cadastro de AC.
	 */
	public AcParams getAcParams() {
		return acParams;
	}

	/**
	 * Configura o objeto com os parametros de formulario de cadastro de AC.
	 * @param acParams Parametros do cadastro de AC.
	 */
	public void setAcParams(AcParams acParams) {
		this.acParams = acParams;
	}
	
	/**
	 * Retorna o objeto com os parametros de formulario de emissao de Certificado.
	 * @return Parametros de emissao de Certificado.
	 */
	public CertParams getCertParams() {
		return certParams;
	}

	/**
	 * Configura o objeto com os parametros de formulario de emissao de Certificado.
	 * @param certParams Parametros de emissao de Certificado.
	 */
	public void setCertParams(CertParams certParams) {
		this.certParams = certParams;
	}

	/* Recarrega as listas de AC. */
	private void recarregarListasAC() {
		listaACRaiz.clear();
		listaACIntermediaria.clear();
		listaAC.clear();
		listaACRaiz.addAll(icpAdmin.listarACRaiz());
		listaACIntermediaria.addAll(icpAdmin.listarACInterm());
		listaAC.addAll(listaACRaiz);
		listaAC.addAll(listaACIntermediaria);
	}

	/* Atualiza (no banco de dados) o status dos certificados expirados.
	 * @throws Exception
	 */
	private void atualizarStatusCertExpirados() {
		icpAdmin.atualizarStatusCertExpirados();
	}

	/* Realiza o download de um arquivo atraves do JSF. 
	 * @param fileName Nome do arquivo (com path). 
	 * @param alias Nome exibido no dialogo de download.
	 * @param mimeType Tipo MIME.
	 * @throws Exception
	 */
	private void downloadFile(String fileName, String alias,
			String mimeType) throws Exception {
		
		if (fileName == null || "".equals(fileName)) {
			throw new FileNotFoundException("File \"\" not found.");
		}

		FacesContext facesContext = FacesContext.getCurrentInstance(); 
		ExternalContext context = facesContext.getExternalContext();
		
        File file = new File(
        		fileName.replaceAll("(\\\\|/)+", 
        				Matcher.quoteReplacement(File.separator)));    
    
        HttpServletResponse response = (HttpServletResponse) context.getResponse();    
        response.setHeader("Content-Disposition", "attachment;filename=\"" + alias + "\"");    
        response.setContentLength((int) file.length());    
        response.setContentType(mimeType);    
    
        FileInputStream in = new FileInputStream(file);    
        OutputStream out = response.getOutputStream();    

        byte[] buf = new byte[4096];    
        int count;    
        while ((count = in.read(buf)) >= 0) {    
            out.write(buf, 0, count);    
        }    
        in.close();    
        out.flush();    
        out.close();    
        facesContext.responseComplete();    
	}

	/* Emite um certificado, de acordo com o tipo especificado nos parametros.
	 * @param acEmissora AC Emissora (issuer) do certificado.
	 * @param certParams Parametros de emissao do certificado.
	 * @throws Exception
	 */
	private void emitirCertPorTipo(Ca acEmissora, CertParams certParams) throws Exception {
		switch (certParams.getTipo()) {
			case ECPF:
				icpAdmin.emitirCertEcpf(acEmissora, certParams.getKeyPassword(), certParams.getCaPassword(),
						certParams.getNome(), certParams.getCpf(), certParams.getEmail(),
						certParams.getNascimento(), certParams.getPisPasep(), certParams.getRg(),
						certParams.getRgOrgEmissor(), certParams.getRgUF(), certParams.getCei(),
						certParams.getTitulo(), certParams.getTituloZona(), certParams.getTituloSecao(),
						certParams.getTituloMunicipio(), certParams.getTituloUF(), certParams.getLogin(),
						certParams.getSubjC(), certParams.getSubjO(), certParams.getSubjOU());
				break;
			case ECNPJ:
				icpAdmin.emitirCertEcnpj(acEmissora, certParams.getKeyPassword(), certParams.getCaPassword(),
						certParams.getNomePJ(), certParams.getNome(), certParams.getCpf(), certParams.getEmail(),
						certParams.getNascimento(), certParams.getPisPasep(), certParams.getRg(),
						certParams.getRgOrgEmissor(), certParams.getRgUF(), certParams.getCei(),
						certParams.getCnpj(), certParams.getSubjC(), certParams.getSubjO(), certParams.getSubjOU(),
						certParams.getSubjL(), certParams.getSubjST());
				break;
			case RIC:
				icpAdmin.emitirCertRic(acEmissora, certParams.getKeyPassword(), certParams.getCaPassword(),
						certParams.getNome(), certParams.getRic(), certParams.getCpf(), certParams.getEmail(),
						certParams.getNascimento(), certParams.getPisPasep(), certParams.getRg(),
						certParams.getRgOrgEmissor(), certParams.getRgUF(), certParams.getCei(),
						certParams.getTitulo(), certParams.getTituloZona(), certParams.getTituloSecao(),
						certParams.getTituloMunicipio(), certParams.getTituloUF(), certParams.getLogin(),
						certParams.getSubjC(), certParams.getSubjO(), certParams.getSubjOU());
				break;
			case ECODIGO:
				icpAdmin.emitirCertEcodigo(acEmissora, certParams.getKeyPassword(), certParams.getCaPassword(),
						certParams.getNomePJ(), certParams.getNome(), certParams.getCpf(), certParams.getEmail(),
						certParams.getNascimento(), certParams.getPisPasep(), certParams.getRg(),
						certParams.getRgOrgEmissor(), certParams.getRgUF(), certParams.getCnpj(),
						certParams.getSubjC(), certParams.getSubjO(), certParams.getSubjOU());
				break;
			case ESERVIDOR:
				icpAdmin.emitirCertEservidor(acEmissora, certParams.getKeyPassword(), certParams.getCaPassword(),
						certParams.getNomeDNS(), certParams.getNomePJ(), certParams.getGuidServer(),
						certParams.getNome(), certParams.getCpf(), certParams.getEmail(),
						certParams.getNascimento(), certParams.getPisPasep(), certParams.getRg(),
						certParams.getRgOrgEmissor(), certParams.getRgUF(), certParams.getCnpj(),
						certParams.getSubjC(), certParams.getSubjO(), certParams.getSubjOU());
				break;
			case EAPLICACAO:
				icpAdmin.emitirCertEaplicacao(acEmissora, certParams.getKeyPassword(), certParams.getCaPassword(),
						certParams.getNomeApp(), certParams.getNomePJ(), certParams.getNome(),
						certParams.getCpf(), certParams.getEmail(), certParams.getNascimento(),
						certParams.getPisPasep(), certParams.getRg(), certParams.getRgOrgEmissor(),
						certParams.getRgUF(), certParams.getCnpj(),
						certParams.getSubjC(), certParams.getSubjO(), certParams.getSubjOU());
				break;
			default:
				break;
		}
	}
}
