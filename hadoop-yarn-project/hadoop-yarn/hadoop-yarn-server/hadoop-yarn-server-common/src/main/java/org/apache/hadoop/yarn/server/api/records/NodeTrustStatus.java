package org.apache.hadoop.yarn.server.api.records;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.yarn.api.ApplicationClientProtocol;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.util.Records;

@Public
@Stable
public abstract class NodeTrustStatus {

  @Private
  public static NodeTrustStatus newInstance(boolean isNodeTrust,
		  String trustReport, long lastTrustReport){
	NodeTrustStatus status = Records.newRecord(NodeTrustStatus.class);
	status.setIsNodeTrust(isNodeTrust);
	status.setTrustReport(trustReport);
	status.setLastTrustReportTime(lastTrustReport);
	return status;
}
  
  @Public
  @Stable
  public abstract boolean getIsNodeTrust();
  
  @Private
  @Unstable
  public abstract void setIsNodeTrust(boolean isNodeTrusted);
  

  /**
   * Get the <em>diagnostic health report</em> of the node.
   * @return <em>diagnostic health report</em> of the node
   */
  @Public
  @Stable
  public abstract String getTrustReport();

  @Private
  @Unstable
  public abstract void setTrustReport(String healthReport);

  /**
   * Get the <em>last timestamp</em> at which the health report was received.
   * @return <em>last timestamp</em> at which the health report was received
   */
  @Public
  @Stable
  public abstract long getLastTrustReportTime();

  @Private
  @Unstable
  public abstract void setLastTrustReportTime(long lastHealthReport);
}
