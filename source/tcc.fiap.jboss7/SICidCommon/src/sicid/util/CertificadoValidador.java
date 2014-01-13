package sicid.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extension;

/**
 * Implementa metodos para validacao de certificados digitais.
 * @author Robson Martins (robson@robsonmartins.com)
 */
public class CertificadoValidador {

	/**
	 * Verifica a validade (datas de validade/expiracao) de um certificado. 
	 * @param cert Objeto que representa o certificado a ser validado.
	 * @return True se certificado esta' valido (dentro das datas de uso).
	 */
	public static boolean isValidByDate(X509Certificate cert) {
		try {
			cert.checkValidity();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Verifica se um certificado e' auto-assinado.
	 * @param cert Objeto que representa o certificado a ser validado.
	 * @return True se certificado e' auto-assinado.
	 */
	public static boolean isSelfSigned(X509Certificate cert) {
  		try {
           PublicKey key = cert.getPublicKey();
           cert.verify(key);
           return true;
       } catch (Exception e) {
           return false;
       }
	}
   
	/**
	 * Verifica se um certificado tem sua cadeia valida dentro de um
	 *   conjunto de certificados emitentes confiaveis. 
	 * @param cert Objeto que representa o certificado a ser validado.
	 * @param keyStore KeyStore contendo o conjunto de certificados
	 *   emissores confiaveis.
	 * @return True se o certificado foi emitido por um dos emissores
	 *   confiaveis especificados.
	 * @throws Exception
	 */
	public static boolean isValidKeyChain(X509Certificate cert,
           KeyStore keyStore) throws Exception {
       
		X509Certificate[] trustedCerts = new X509Certificate[keyStore.size()];
		int argIdx = 0;
		Enumeration<String> alias = keyStore.aliases();

		while (alias.hasMoreElements()) {
			trustedCerts[argIdx++] =
				(X509Certificate) keyStore.getCertificate(alias.nextElement());
		}

		return isValidKeyChain(cert, trustedCerts);
	}

	/**
	 * Verifica se um certificado tem sua cadeia valida dentro de um
	 *   conjunto de certificados emitentes confiaveis. 
	 * @param cert Objeto que representa o certificado a ser validado.
	 * @param trustedCerts Conjunto de certificados emissores confiaveis.
	 * @return True se o certificado foi emitido por um dos emissores
	 *   confiaveis especificados.
	 * @throws Exception
	 */
	public static boolean isValidKeyChain(X509Certificate cert,
           X509Certificate[] trustedCerts) throws Exception {
       
		if (trustedCerts == null || trustedCerts.length == 0) {
			return true;
		}
		
		boolean found = false;
		int argIdx = trustedCerts.length;
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		CertPathValidator validator = CertPathValidator.getInstance("PKIX");

		TrustAnchor anchor;
		Set<TrustAnchor> anchors;
		CertPath certPath;
		List<Certificate> certList;
		PKIXParameters params;

		while (!found && argIdx > 0) {
			anchor = new TrustAnchor(trustedCerts[--argIdx], null);
			anchors = Collections.singleton(anchor);

			certList = Arrays.asList(new Certificate[] { cert });
			certPath = certFactory.generateCertPath(certList);

			params = new PKIXParameters(anchors);
			params.setRevocationEnabled(false);

			if (cert.getIssuerDN().equals(trustedCerts[argIdx].getSubjectDN())) {
				try {
					validator.validate(certPath, params);
					if (isSelfSigned(trustedCerts[argIdx])) {
						found = true;
					} else if (!cert.equals(trustedCerts[argIdx])) {
						found = isValidKeyChain(trustedCerts[argIdx], trustedCerts);
					}
				} catch (CertPathValidatorException e) { }
			}
		}
		return found;
	}
	
	/**
	 * Vertifica se um certificado foi revogado pelo seu emissor
	 *   (ou um emissor na sua cadeia de certificados), atraves da
	 *   consulta 'as CRLs (Listas de Certificados Revogados).
	 * @param cert Objeto que representa o certificado a ser validado.
	 * @return True se certificado foi revogado.
	 * @throws Exception
	 */
	public static boolean isRevoked(X509Certificate cert) throws Exception {
		
		for (String crlURL : getCrlDistPoints(cert)) {
			try {
				if (downloadCRL(crlURL).isRevoked(cert)) {
					return true;
				}
			} catch (Exception e) { /* erro acessando a CRL */ }
		}
		return false;
	}

	/* Retorna uma lista com os pontos de distribuicao de CRL
	 *   (Lista de Certificados Revogados).
	 * @param cert Objeto que representa o certificado.
	 * @return Lista de pontos de distribuicao de CRL.
	 * @throws Exception
	 */
	private static List<String> getCrlDistPoints(X509Certificate cert) throws Exception {
		
		byte[] crlDistPointExt =
			cert.getExtensionValue(X509Extension.cRLDistributionPoints.getId());
		
		if (crlDistPointExt == null) {
			List<String> emptyList = new ArrayList<String>();
			return emptyList;
		}
		
		ASN1InputStream asnInStream =
			new ASN1InputStream(new ByteArrayInputStream(crlDistPointExt));
		
		ASN1Primitive derObjCrlDistPoint = asnInStream.readObject();
		DEROctetString derOctStrCrlDistPoint = (DEROctetString) derObjCrlDistPoint;
		byte[] crlDistPointBytes = derOctStrCrlDistPoint.getOctets();
		
		ASN1InputStream asnInStream2 =
			new ASN1InputStream(new ByteArrayInputStream(crlDistPointBytes));
		
		ASN1Primitive derObjCrlDistPoint2 = asnInStream2.readObject();
		CRLDistPoint crlDistPoints = CRLDistPoint.getInstance(derObjCrlDistPoint2);
		
		List<String> crlUrls = new ArrayList<String>();
		
		for (DistributionPoint dPoint: crlDistPoints.getDistributionPoints()) {
            DistributionPointName dPointName = dPoint.getDistributionPoint();
            if (dPointName != null) {
                if (dPointName.getType() == DistributionPointName.FULL_NAME) {
                    
                	GeneralName[] genNames =
                    	GeneralNames.getInstance(dPointName.getName()).getNames();
                    for (int j = 0; j < genNames.length; j++) {
                        if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
                            String url =
                            	DERIA5String.getInstance(genNames[j].getName()).getString();
                            crlUrls.add(url);
                        }
                    }
                    
                }
            }
		}
		asnInStream.close();
		asnInStream2.close();
		return crlUrls;
	}

	/* Realiza o download de uma CRL (Lista de Certificados Revogados).
	 * @param crlURL URL da CRL (Lista de Certificados Revogados).
	 * @return Objeto que representa a CRL.
	 * @throws Exception
	 */
	private static X509CRL downloadCRL(String crlURL) throws Exception {
		
		if (crlURL.startsWith("http://" ) ||
			crlURL.startsWith("https://") ||
			crlURL.startsWith("ftp://"  )) {
			
			return downloadCRLFromWeb(crlURL);
			
		} else if (crlURL.startsWith("ldap://")) {
			
			return downloadCRLFromLDAP(crlURL);
			
		} else {
			throw new CRLException(
					String.format(
							"Can not download CRL from certificate distribution point: %s",
							crlURL));
		}
	}

	/* Realiza o download de uma CRL (Lista de Certificados Revogados),
	 *   a partir da web (HTTP, HTTPS, FTP).
	 * @param crlURL URL da CRL (Lista de Certificados Revogados).
	 * @return Objeto que representa a CRL.
	 * @throws Exception
	 */
	private static X509CRL downloadCRLFromWeb(String crlURL) throws Exception {
		
		URL url = new URL(crlURL);
		InputStream crlStream = url.openStream();
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			return (X509CRL) certFactory.generateCRL(crlStream);
		} finally {
			crlStream.close();
		}
	}

	/* Realiza o download de uma CRL (Lista de Certificados Revogados),
	 *   a partir de um servidor LDAP.
	 * @param ldapURL URL da CRL (Lista de Certificados Revogados),
	 *   no servicor LDAP.
	 * @return Objeto que representa a CRL.
	 * @throws Exception
	 */
	private static X509CRL downloadCRLFromLDAP(String ldapURL) throws Exception {
		
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapURL);

        DirContext context = new InitialDirContext(env);
        Attributes attrs = context.getAttributes("");
        Attribute crlAttr = attrs.get("certificateRevocationList;binary");
        byte[] crlValue = (byte[]) crlAttr.get();
        
        if ((crlValue == null) || (crlValue.length == 0)) {
        	
        	throw new CRLException(
        			String.format("Can not download CRL from: %s", ldapURL));
        } else {
        
        	InputStream crlStream = new ByteArrayInputStream(crlValue);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        	X509CRL crl = (X509CRL) certFactory.generateCRL(crlStream);
        	return crl;
        }
	}
}
