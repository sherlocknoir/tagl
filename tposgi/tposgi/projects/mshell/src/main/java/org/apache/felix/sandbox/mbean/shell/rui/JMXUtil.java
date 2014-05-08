/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.sandbox.mbean.shell.rui;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.QueryExp;

public class JMXUtil {

	public static String listMBeans(MBeanServerConnection  mbsc, String objectNameStr) throws Exception{
		StringBuffer sb=new StringBuffer();
	    ObjectName name=objectNameStr==null?null:new ObjectName(objectNameStr);
		QueryExp query=null;
		Set objectNameSet = mbsc.queryNames(name, query);
	    Iterator it = objectNameSet.iterator();
	    while (it.hasNext()) {
	        ObjectName on = (ObjectName) it.next();
	        sb.append("-----------------");
	        sb.append("CanonicalName: "+on.getCanonicalName());
	        sb.append("Domain       : "+on.getDomain());
	        sb.append("KeyPropList  : "+on.getKeyPropertyListString());
	        sb.append("Properties   : ");
	        Hashtable properties=on.getKeyPropertyList();
	        Enumeration enumeration=properties.keys();
	        while(enumeration.hasMoreElements()){
	        	Object key=enumeration.nextElement();
	        	sb.append(key+"="+properties.get(key));
	        }
	    }
	    return sb.toString();
	}

	
	public static String listMBeans(MBeanServerConnection  mbsc)throws Exception {
		return listMBeans(mbsc,null);
	}
}
