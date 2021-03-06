/*
 * RHQ Management Platform
 * Copyright (C) 2005-2009 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.rhq.plugins.iptables;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.augeas.node.AugeasNode;
import org.rhq.augeas.tree.AugeasTree;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
/**
 * 
 * @author Filip Drabek
 *
 */
public class IptablesChainDiscoveryComponent implements ResourceDiscoveryComponent<IptablesTableComponent>{
       private final Log log = LogFactory.getLog(this.getClass());
       
       public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<IptablesTableComponent> context)
                     throws InvalidPluginConfigurationException, Exception {
              
                   AugeasTree tree = context.getParentResourceComponent().getAugeasTree();
                      String tableName = context.getParentResourceComponent().getTableName();
              
                      List<AugeasNode> nodes = tree.matchRelative(tree.getRootNode(), File.separatorChar+tableName+File.separatorChar+"settings"+File.separatorChar+"chain");
                      ResourceType resourceType = context.getResourceType();
                     
                      Set<DiscoveredResourceDetails> resources = new HashSet<DiscoveredResourceDetails>();
                 
                     for (AugeasNode nd : nodes){
                            String value = nd.getValue();
                            resources.add(new DiscoveredResourceDetails(resourceType,value, value, "", "Chain", null, null));
                  }
              
              return resources;
       }

}
