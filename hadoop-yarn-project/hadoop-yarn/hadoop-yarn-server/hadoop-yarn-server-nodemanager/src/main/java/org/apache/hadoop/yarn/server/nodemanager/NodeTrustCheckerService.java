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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.CompositeService;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
public class NodeTrustCheckerService extends CompositeService{

  private boolean attestNodeTrust = false;	

  public NodeTrustCheckerService() {
    super(NodeTrustCheckerService.class.getName());
  }

  @Override
  protected void serviceInit(Configuration conf) throws Exception {
    super.serviceInit(conf);
    this.attestNodeTrust = conf.getBoolean(YarnConfiguration.NM_TRUST_CHECK_ENABLE, true);
  }

  /**
   * @return the reporting string of health of the node
   */
  String getTrusthReport() {
	  return "Trust status now is not implemented";
  }

  /**
   * @return <em>true</em> if the node is healthy
   */
  boolean isTrust(String hostname) {
    if(!this.attestNodeTrust)
	return true;
    return AttestationService.testHost(hostname);
  }

  /**
   * @return when the last time the node health status is reported
   */
  long getLastTrustReportTime() {
    //Not implements
    long lastReportTime = 0;
    return lastReportTime;
  }

}
