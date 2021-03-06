/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sentry.provider.common;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.Groups;

import com.google.common.collect.Lists;
import org.apache.sentry.core.common.exception.SentryGroupNotFoundException;

public class HadoopGroupMappingService implements GroupMappingService {

  private static Configuration hadoopConf;
  private final Groups groups;

  public HadoopGroupMappingService(Groups groups) {
    this.groups = groups;
  }

  public HadoopGroupMappingService(Configuration conf, String resource) {
    if (hadoopConf == null) {
      synchronized (HadoopGroupMappingService.class) {
        if (hadoopConf == null) {
          // clone the current config and add resource path
          hadoopConf = new Configuration();
          hadoopConf.addResource(conf);
          if (!StringUtils.isEmpty(resource)) {
            hadoopConf.addResource(resource, true);
          }
        }
      }
    }
    this.groups = Groups.getUserToGroupsMappingService(hadoopConf);
  }

  @Override
  public Set<String> getGroups(String user) throws SentryGroupNotFoundException {
    List<String> groupList = Lists.newArrayList();
    try {
      groupList = groups.getGroups(user);
    } catch (IOException e) {
      throw new SentryGroupNotFoundException("Unable to obtain groups for " + user, e);
    }
    if (groupList == null || groupList.isEmpty()) {
      throw new SentryGroupNotFoundException("Unable to obtain groups for " + user);
    }
    return new HashSet<String>(groupList);
  }
}
