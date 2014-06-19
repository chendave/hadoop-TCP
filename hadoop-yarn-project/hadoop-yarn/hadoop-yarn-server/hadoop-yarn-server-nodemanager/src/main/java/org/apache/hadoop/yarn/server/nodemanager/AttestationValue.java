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

import org.apache.commons.lang3.ObjectUtils;;

public class AttestationValue {

    private String hostName;
    private AttestationResultEnum trustLevel;

    public AttestationValue() {
    }

    public AttestationValue(String hostName, AttestationResultEnum trustLevel) {
        super();
        this.hostName = hostName;
        this.trustLevel = trustLevel;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public AttestationResultEnum getTrustLevel() {
        return trustLevel;
    }

    public void setTrustLevel(AttestationResultEnum trustLevel) {
        this.trustLevel = trustLevel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((hostName == null) ? 0 : hostName.hashCode());
        result = prime * result
                + ((trustLevel == null) ? 0 : trustLevel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || (obj.getClass() != this.getClass()))
            return false;
        AttestationValue other = (AttestationValue) obj;
       return  ObjectUtils.equals(hostName, other.hostName)
    		   && ObjectUtils.equals(trustLevel, other.trustLevel);
    }

}
