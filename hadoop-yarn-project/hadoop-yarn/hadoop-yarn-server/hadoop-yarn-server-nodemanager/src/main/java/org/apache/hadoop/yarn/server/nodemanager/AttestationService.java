/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.hadoop.yarn.server.nodemanager;
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
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;


public class AttestationService {
    //some variables are used for formating to jason style 
    private static final String HEADER_HOSTS = "hosts";
    private static final String HEADER_HOST_NAME = "host_name";
    private static final String HEADER_RESULT = "trust_lvl";
    private static final String HEADER_VTIME = "vtime";
    private static final String CONTENT_TYPE = "application/json";
   
    //some variables of OAT service 
    private static final String DataDir = "/opt/intel/cloudsecurity/trustagent/cert/";
    private static final String AttestationTruststore= "hadoop-oat.jks";
    private static final String truststorePassword = "password";
    //OAT server 
    private static final String attestationServer = "hadoop-node2.sh.intel.com";
    private static final String TRUSTED = "TRUSTED";

    private static final AttestationService instance = new AttestationService();
    private static final Log log = LogFactory.getLog(AttestationService.class);

    public static boolean testHost(String hostname){
    if(hostname == null)
	return false;
    List<String> hosts = new ArrayList();
    hosts.add(hostname);
    log.info(
	     "Return the TRUST status of " + hostname + " from OAT server."	
	    );
    if(AttestationService.getInstance().attestHosts(hosts) == null)
	return false;
    return AttestationService
		.getInstance()
		.attestHosts(hosts)
		.get(0)
		.getTrustLevel()
		.toString()
		.contains(TRUSTED);
    }
    public static HttpClient getClient(Configuration conf)throws Exception {
        HttpClient httpClient = new HttpClient();
        URL trustStoreUrl;
           // try {
                int port = 8181;
               // trustStoreUrl = new URL("file://" + DataDir + AttestationTruststore);
                trustStoreUrl = new URL("file://"
                                + conf.getTrimmed(YarnConfiguration.DATADIR,DataDir)
                                + conf.getTrimmed(YarnConfiguration.ATTESTATIONTRUSTSTORE,AttestationTruststore));
                // registering the https protocol with a socket factory that
                // provides client authentication.
                ProtocolSocketFactory factory = new AuthSSLProtocolSocketFactory(
						getTrustStore(trustStoreUrl.getPath(),
								conf.getTrimmed(YarnConfiguration.TRUSTSTOREPASSWORD,truststorePassword)
							     )
						);
                Protocol clientAuthHTTPS = new Protocol("https", factory, port);
                httpClient.getHostConfiguration().setHost(conf.getTrimmed(YarnConfiguration.ATTESTATIONSERVER, attestationServer),
                        port, clientAuthHTTPS);
            //} catch (Exception e) {
              //  log.fatal(
                //        "Failed to init AuthSSLProtocolSocketFactory. SSL connections will not work",
                  //      e);
           // }
        
        return httpClient;
    }

    public static KeyStore getTrustStore(String filePath, String password) throws IOException,
            									KeyStoreException,
										CertificateException,
										NoSuchAlgorithmException
    {
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
	Configuration conf = new YarnConfiguration();
        try {
            postMethod.setRequestEntity(new StringRequestEntity(
                    writeListJson(hosts)));
            postMethod.addRequestHeader("Accept", CONTENT_TYPE);
            postMethod.addRequestHeader("Content-type", CONTENT_TYPE);
            //HttpClient httpClient = getClient();
            HttpClient httpClient;
	    try{
	    httpClient = getClient(conf);
	    }catch(Exception e){
		return null;
	    }
            int statusCode = httpClient.executeMethod(postMethod);
            String strResponse = postMethod.getResponseBodyAsString();
            log.debug("return attested result:" + strResponse);
            if (statusCode == 200) {
                values = parsePostedResp(strResponse);
            } else {
                log.error("attestation error:" + strResponse);
            }
        } catch (JsonParseException e) {
            log.error(
                    String.format("Failed to parse result: [%s]",
                            e.getMessage()), e);
        } catch (IOException e) {
            log.error(
                    String.format(
                            "Failed to attest hosts: [%s], make sure hosts are up and reachable",
                            e.getMessage()), e);
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
