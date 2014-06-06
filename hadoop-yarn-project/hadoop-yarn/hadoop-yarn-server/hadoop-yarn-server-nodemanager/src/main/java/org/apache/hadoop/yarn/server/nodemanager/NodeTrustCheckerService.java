package org.apache.hadoop.yarn.server.nodemanager;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.CompositeService;
public class NodeTrustCheckerService extends CompositeService{

  public NodeTrustCheckerService() {
    super(NodeTrustCheckerService.class.getName());
  }

  @Override
  protected void serviceInit(Configuration conf) throws Exception {
    super.serviceInit(conf);
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
