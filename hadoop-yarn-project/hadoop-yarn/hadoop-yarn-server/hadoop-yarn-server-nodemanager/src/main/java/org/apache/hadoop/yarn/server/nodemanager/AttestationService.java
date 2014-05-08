
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;


public class AttestationService {
    private static final String HEADER_HOSTS = "hosts";
    private static final String HEADER_HOST_NAME = "host_name";
    private static final String HEADER_RESULT = "trust_lvl";
    private static final String HEADER_VTIME = "vtime";
    private static final String CONTENT_TYPE = "application/json";
    private static final AttestationService instance = new AttestationService();
    private static final Log log = LogFactory.getLog(AttestationService.class);
    
    private static final String DataDir = "/opt/intel/cloudsecurity/trustagent/cert/";
    private static final String AttestationTruststore= "hadoop-oat.jks";

    public static void main(String args[]){
    	List<String> hosts = new ArrayList();
    	hosts.add("hadoop-node1");
    	hosts.add("hadoop-node2");
    	AttestationService as = AttestationService.getInstance();
    	List<AttestationValue> values = as.attestHosts(hosts);
    	System.out.println("values:" + values.toString());
    }
    public static HttpClient getClient() {
        HttpClient httpClient = new HttpClient();
            URL trustStoreUrl;
            try {
                int port = 8181;
                trustStoreUrl = new URL("file://" + DataDir + AttestationTruststore);
                String truststorePassword = "password";
                String attestationServer = "10.239.131.234";
                // registering the https protocol with a socket factory that
                // provides client authentication.
                ProtocolSocketFactory factory = new AuthSSLProtocolSocketFactory(getTrustStore(trustStoreUrl.getPath(),truststorePassword));
                Protocol clientAuthHTTPS = new Protocol("https", factory, port);
                httpClient.getHostConfiguration().setHost(attestationServer,
                        port, clientAuthHTTPS);
            } catch (Exception e) {
                log.fatal(
                        "Failed to init AuthSSLProtocolSocketFactory. SSL connections will not work",
                        e);
                        
         
            }
        
        return httpClient;
    }

    public static KeyStore getTrustStore(String filePath, String password) throws IOException,
            KeyStoreException, CertificateException, NoSuchAlgorithmException {
        InputStream in = new FileInputStream(filePath);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(in, password.toCharArray());
        return ks;
    }

    public static AttestationService getInstance() {
        return instance;
    }

    private AttestationService() {
    }

    public List<AttestationValue> attestHosts(List<String> hosts) {
        String pollURI = "AttestationService/resources/PollHosts";
        List<AttestationValue> values = new ArrayList<AttestationValue>();

        PostMethod postMethod = new PostMethod("/" + pollURI);
        try {
            postMethod.setRequestEntity(new StringRequestEntity(
                    writeListJson(hosts)));
            postMethod.addRequestHeader("Accept", CONTENT_TYPE);
            postMethod.addRequestHeader("Content-type", CONTENT_TYPE);
            HttpClient httpClient = getClient();
            System.out.println("hosts:"+writeListJson(hosts));
            System.out.println("#################################");
            int statusCode = httpClient.executeMethod(postMethod);
            String strResponse = postMethod.getResponseBodyAsString();
            System.out.println("return attested result:" + strResponse);
            log.debug("return attested result:" + strResponse);
            //System.out.println("return attested result:");
            if (statusCode == 200) {
                values = parsePostedResp(strResponse);
            } else {
                log.error("attestation error:" + strResponse);
            	//System.out.println("attestation error:");
            }
        } catch (JsonParseException e) {
            log.error(
                    String.format("Failed to parse result: [%s]",
                            e.getMessage()), e);
                           
        	//System.out.println("Failed to parse result:");
        } catch (IOException e) {
            log.error(
                    String.format(
                            "Failed to attest hosts: [%s], make sure hosts are up and reachable",
                            e.getMessage()), e);
                            
        	//System.out.println("Failed to attest hosts: [%s], make sure hosts are up and reachable");
        } finally {
            postMethod.releaseConnection();
        }
        return values;
    }

    public List<AttestationValue> parsePostedResp(String str)
            throws JsonParseException, IOException {
        JsonFactory jfactory = new JsonFactory();
        List<AttestationValue> values = new ArrayList<AttestationValue>();
        JsonParser jParser = jfactory.createJsonParser(str);
        try {
            jParser.nextToken();
            while (jParser.nextToken() != JsonToken.END_OBJECT) {
                if (jParser.getCurrentName().equalsIgnoreCase(HEADER_HOSTS)) {
                    while (jParser.nextToken() != JsonToken.END_ARRAY
                            && jParser.getCurrentToken() != JsonToken.END_OBJECT) {
                        AttestationValue value = new AttestationValue();
                        if (jParser.getCurrentName().equalsIgnoreCase(
                                HEADER_HOST_NAME)) {
                            jParser.nextToken();
                            value.setHostName(jParser.getText());
                            jParser.nextToken();
                        }
                        if (jParser.getCurrentName().equalsIgnoreCase(
                                HEADER_RESULT)) {
                            jParser.nextToken();
                            value.setTrustLevel(AttestationResultEnum
                                    .valueOf(jParser.getText().toUpperCase()));
                            jParser.nextToken();
                        }
                        if (jParser.getCurrentName().equalsIgnoreCase(
                                HEADER_VTIME)) {
                            jParser.nextToken();
                            jParser.nextToken();
                        }
                        if (value.getHostName() != null) {
                            log.debug("host_name:" + value.getHostName()
                                    + ", trustLevel:" + value.getTrustLevel());
                        	//System.out.println("host_name:");
                            values.add(value);
                        }
                        jParser.nextToken();
                    }
                    break;
                }
            }
        } finally {
            jParser.close();
        }
        return values;
    }

    public String writeListJson(List<String> hosts) {
        StringBuilder sb = new StringBuilder("{\"").append(HEADER_HOSTS)
                .append("\":[");
        for (String host : hosts) {
            sb = sb.append("\"").append(host).append("\",");
        }
        String jsonString = sb.substring(0, sb.length() - 1) + "]}";
        return jsonString;
    }
 }