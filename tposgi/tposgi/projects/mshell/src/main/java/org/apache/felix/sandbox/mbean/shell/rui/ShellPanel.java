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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.apache.felix.sandbox.mbean.shell.ShellMXBean;

// TODO add a Clear button in the panel
// TODO remove notification handler when the connection is closed


public class ShellPanel extends JPanel {
	
	private final static String LOCAL="LOCAL";
	
	private ColoredTextPane console = new ColoredTextPane();
	private JTextField command = new JTextField();
	private JMXConnector jmxc;
	private MBeanServerConnection mbsc;
	RemoteGUI remoteGUI;
	private ObjectName shellObjectName;
	
	
	private String prompt="> "; // default
	
	public ShellPanel(RemoteGUI gui) {
		this.remoteGUI=gui;
		this.initGUI(null);
	}
		
	private void initGUI(String message) {
		JScrollPane scroll = new JScrollPane(console);
		console.setFont(new Font("Monospaced",Font.PLAIN,12));
		console.setEditable(false);
		command.addKeyListener(new CommandListener());
		this.setLayout(new BorderLayout());
		scroll.setBorder(new TitledBorder("Output"));
		command.setBorder(new TitledBorder("Command"));
		scroll.setAutoscrolls(true);
		this.add(scroll,BorderLayout.CENTER);
		this.add(command,BorderLayout.SOUTH);
		console.setText("\n");
		if(message!=null) {
			enqueueEvent(LOCAL,message,null);
		}
	}
	
	private void executeAll(String commandLine) {
		RemoteGUI.executeAll(commandLine);		
	}

	void execute(String commandLine) {
		if(mbsc==null){
			JOptionPane.showMessageDialog(this, "Not connected", "MShell", JOptionPane.WARNING_MESSAGE);			
		} else 
			try { 			
				mbsc.invoke(shellObjectName ,
						"executeCommand", 
						 new Object[] { commandLine }, new String[]{"java.lang.String"});
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getClass().getName() + " :\n " + e.getMessage());
			}
	}
	
	private void execute() {
		if(RemoteGUI.isBroadcastCommandLineMode()){
			executeAll(command.getText());
		} else {
			execute(command.getText());
		}
	}

	public void openConnection(String jmxServiceUrlStr) throws Exception {
        if(jmxc!=null) {
        	closeConnection();
        }
        
        JMXServiceURL url = new JMXServiceURL(jmxServiceUrlStr);        
        jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        openConnection(mbsc, jmxServiceUrlStr);
	}		

	public void openConnection(MBeanServerConnection mbsc, String connectionName) throws Exception {
		this.mbsc = mbsc;
		// search for the shell ObjectName
		ObjectName scope =  new ObjectName(ShellMXBean.SHELL_OBJECTNAME_QUERY); 
		Set results = mbsc.queryNames(scope,null);
		Iterator iter = results.iterator();
		shellObjectName = null;
		while(iter.hasNext()) {
			if(shellObjectName==null) {
				shellObjectName = (ObjectName)iter.next();
				enqueueEvent(LOCAL,"Shell is "+shellObjectName.getCanonicalName(),null);
			} else {
				enqueueEvent(LOCAL,"But there is also "+ ((ObjectName)iter.next()).getCanonicalName(),null);				
			}
		}
		initNotificationListener();
		remoteGUI.setTitle("MShell: connected to "+connectionName);
	}
	
	public void openConnectionPlatform() throws Exception {
		MBeanServer mBeanServer=ManagementFactory.getPlatformMBeanServer();
		openConnection((MBeanServerConnection)mBeanServer, "Platform");
	}
	
	public void closeConnection() throws Exception {
		finiNotificationListener();
		if(mbsc!=null) {
	        mbsc=null;
		}
        if(jmxc!=null) {
        	jmxc.close();
	        jmxc=null;
        }
	    remoteGUI.setTitle("MShell: not connected");
	}
	
	private NotificationListener notificationListener;
	
	private void initNotificationListener() {
		if(mbsc!=null) {	
			try {
				NotificationListener notificationListener=new NotificationListener() {
					public void handleNotification(
							Notification notification, Object handback) {
						enqueueEvent(notification.getType(), notification.getMessage(), notification.getUserData());
					}
				};

				mbsc.addNotificationListener(
						shellObjectName,
						notificationListener, null, null);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	private void finiNotificationListener() {
		if(mbsc!=null && notificationListener!=null) {	
			try {
				mbsc.removeNotificationListener(shellObjectName, notificationListener);
			} catch (Exception e) {
			}
			notificationListener=null;
		}
	}

	private void enqueueEvent(final String type, final String message, final Object userdata) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				console.setEditable(true);
				if (ShellMXBean.ERROR.equals(type)) {
					console.append(Color.RED,message);
				} else if (ShellMXBean.CMD_STARTED.equals(type)) {
					console.append(Color.CYAN,"\n"+prompt+message+"\n");
				} else if (ShellMXBean.CMD_ERROR.equals(type)) {
					console.append(Color.RED,userdata.toString());
				} else if (ShellMXBean.CMD_SUCCESSFUL.equals(type)) {
					// do nothing
				} else if (LOCAL.equals(type)) {
					console.append(Color.DARK_GRAY,message);
				} else {
					console.append(Color.BLACK,message);
				}
				console.setCaretPosition(console.getText().length() - 1);
				console.setEditable(false);
			}
		});
	}
	
	private class CommandListener extends KeyAdapter {
		private ArrayList<String> commandBuffer = new ArrayList<String>();
		private int position = -1;
		public void keyPressed(KeyEvent event)    {
			int keyCode = event.getKeyCode();
            
			switch (keyCode) {
            case KeyEvent.VK_ENTER:
            	if ("".equals(command.getText().trim())) {
                	return;
                }
            	commandBuffer.add(command.getText().trim());
            	execute();
            	command.setText("");
            	break;
            case KeyEvent.VK_UP:
            	if (position < commandBuffer.size() - 1) {
            		position++;
            	}
            	command.setText(commandBuffer.get(position));
            	break;
            case KeyEvent.VK_DOWN:
            	if (position > 0) {
            		position--;
            	}
            	command.setText(commandBuffer.get(position));
            	break;
            } 
        }
	}
}