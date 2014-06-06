package org.apache.hadoop.yarn.server.api.records.impl.pb;

import org.apache.hadoop.yarn.proto.YarnServerCommonProtos.NodeTrustStatusProto;
import org.apache.hadoop.yarn.proto.YarnServerCommonProtos.NodeTrustStatusProtoOrBuilder;
import org.apache.hadoop.yarn.server.api.records.NodeTrustStatus;

import com.google.protobuf.TextFormat;

public class NodeTrustStatusPBImpl extends NodeTrustStatus {

  private NodeTrustStatusProto.Builder builder;
  private boolean viaProto = false;
  private NodeTrustStatusProto proto = NodeTrustStatusProto
      .getDefaultInstance();

  public NodeTrustStatusPBImpl() {
    this.builder = NodeTrustStatusProto.newBuilder();
  }

  public NodeTrustStatusPBImpl(NodeTrustStatusProto proto) {
    this.proto = proto;
    this.viaProto = true;
  }

  public NodeTrustStatusProto getProto() {
    mergeLocalToProto();
    this.proto = this.viaProto ? this.proto : this.builder.build();
    this.viaProto = true;
    return this.proto;
  }

  @Override
  public int hashCode() {
    return getProto().hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == null)
      return false;
    if (other.getClass().isAssignableFrom(this.getClass())) {
      return this.getProto().equals(this.getClass().cast(other).getProto());
    }
    return false;
  }

  @Override
  public String toString() {
    return TextFormat.shortDebugString(getProto());
  }

  private void mergeLocalToProto() {
    if (this.viaProto)
      maybeInitBuilder();
    this.proto = this.builder.build();

    this.viaProto = true;
  }

  private void maybeInitBuilder() {
    if (this.viaProto || this.builder == null) {
      this.builder = NodeTrustStatusProto.newBuilder(this.proto);
    }
    this.viaProto = false;
  }

  @Override
  public boolean getIsNodeTrust() {
    NodeTrustStatusProtoOrBuilder p =
        this.viaProto ? this.proto : this.builder;
    return p.getIsNodeTrust();
  }

  @Override
  public void setIsNodeTrust(boolean isNodeTrusty) {
    maybeInitBuilder();
    this.builder.setIsNodeTrust(isNodeTrusty);
  }

  @Override
  public String getTrustReport() {
    NodeTrustStatusProtoOrBuilder p =
        this.viaProto ? this.proto : this.builder;
    if (!p.hasTrustReport()) {
      return null;
    }
    return (p.getTrustReport());
  }

  @Override
  public void setTrustReport(String healthReport) {
    maybeInitBuilder();
    if (healthReport == null) {
      this.builder.clearTrustReport();
      return;
    }
    this.builder.setTrustReport((healthReport));
  }

  @Override
  public long getLastTrustReportTime() {
    NodeTrustStatusProtoOrBuilder p =
        this.viaProto ? this.proto : this.builder;
    return (p.getLastTrustReportTime());
  }

  @Override
  public void setLastTrustReportTime(long lastTrustReport) {
    maybeInitBuilder();
    this.builder.setLastTrustReportTime((lastTrustReport));
  }


}
