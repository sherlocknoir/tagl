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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * This class provides the class to the GUI remote console launching command to the Felix Gogo shell 
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class RemoteGUI extends JFrame {

	private static int counter = 0;
	private static boolean isBundle = false;
	private static boolean isBroadcastCommandLineMode = false;
	private static int posX = 50;
	private static int posY = 50;
	private static int incrXY = 50;

	JCheckBoxMenuItem cbMenuItem;
	
	private String url;
	private int x;
	private int y;
	private int w;
	private int h;
	private ShellPanel shellPanel;
	static boolean isBroadcastCommandLineMode() {
		return isBroadcastCommandLineMode;
	}

	private static void setBroadcastCommandLineMode(boolean b) {
		isBroadcastCommandLineMode=b;
		for (RemoteGUI gui : remoteGUIs) {
			gui.cbMenuItem.setSelected(b);
		}
	}
	
	private static List<RemoteGUI> remoteGUIs=new ArrayList<RemoteGUI>();
		
	public RemoteGUI(String hostName, int portNum, int x, int y, int w, int h)
			throws Exception {
		this("service:jmx:rmi:///jndi/rmi://" + hostName + ":" + portNum
				+ "/jmxrmi", x, y, w, h);
	}

	public RemoteGUI(String url, int x, int y, int w, int h) {
		this.url = url;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	void start() {
		counter++;
		shellPanel=new ShellPanel(this);
		add(shellPanel);
		setBounds(x, y, w, h);
		setTitle("MShell: not connected");

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				actionClose();
			}
		});
		addMenu();
		// pack();
		setVisible(true);
		
		if(url!=null) {
			try {
				shellPanel.openConnection(url);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
					    "Can't connect to "+url,
					    "MShell error",
					    JOptionPane.ERROR_MESSAGE);
				
			}
		}
	}

	void stop() {
		dispose();
		remoteGUIs.remove(this);
		counter--;
		if (counter == 0 && !isBundle) {
			System.exit(0);
		}
	}

	static void stopAll() {
		for (RemoteGUI remoteGUI : remoteGUIs) {
			remoteGUI.dispose();
			counter--;
		}
		remoteGUIs=null;
		if (!isBundle) {
			System.exit(0);
		}			
	}

	static RemoteGUI create(String url, int x, int y, int w, int h) {
		RemoteGUI remoteGUI=new RemoteGUI(url, x, y, w, h);
		try {
			remoteGUI.start();
			remoteGUIs.add(remoteGUI);
			return remoteGUI;
		} catch (Exception e) {
			System.err.println("Exception for " + url);
			e.printStackTrace(System.err);
			remoteGUI.stop();
			return null;
		}
	}
	
	public static void main(String[] args) {
		if(args.length==0){
			if(create(null, posX, posY, 400, 300)!=null) {
				posX += incrXY;
				posY += incrXY;
			}
			
		} else {		
			for (int i = 0; i < args.length; i++) {
				if(create(args[i], posX, posY, 400, 300)!=null) {
					posX += incrXY;
					posY += incrXY;
				}
			}
		}
	}

	private void addMenu() {
		// Where the GUI is created:
		JMenuBar menuBar;
		JMenu menu;
		JMenu submenu;
		JMenuItem menuItem;

		// Create the menu bar.
		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		menu = new JMenu("Connect");
		menu.setMnemonic(KeyEvent.VK_C);
		menu.getAccessibleContext().setAccessibleDescription(
				"The menu provides main commands");
		menuBar.add(menu);
				
		menuItem = new JMenuItem("Connect", KeyEvent.VK_C);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5,
				ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Connect the console");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				actionConnect();
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Connect platform", KeyEvent.VK_C);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5,
				ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Connect the console to the platform ");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				actionConnectPlatform();
			}
		});
		menu.add(menuItem);

		
		
		menuItem = new JMenuItem("Disconnect", KeyEvent.VK_D);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6,
				ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Disconnect the console");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				actionDisconnect();
			}
		});
		menu.add(menuItem);

		
		menuItem = new JMenuItem("New", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
				ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Create a new console");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				actionNew();
			}
		});
		menu.add(menuItem);

		menu.addSeparator();
				
		submenu = new JMenu("History");
		submenu.setMnemonic(KeyEvent.VK_S);
		menu.add(submenu);

		menu.addSeparator();

		menuItem = new JMenuItem("Close", KeyEvent.VK_W);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,
				ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Close the console");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				actionClose();
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,
				ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Close all consoles and quit");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				actionQuit();
			}
		});
		menu.add(menuItem);

		menu = new JMenu("Option");
		menu.setMnemonic(KeyEvent.VK_N);
		menu.getAccessibleContext().setAccessibleDescription(
				"This menu configures options");
		cbMenuItem = new JCheckBoxMenuItem("Broadcast command line");
		cbMenuItem.setMnemonic(KeyEvent.VK_B);
		cbMenuItem.setSelected(isBroadcastCommandLineMode);
		cbMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setBroadcastCommandLineMode(!isBroadcastCommandLineMode());
			}
		});
		cbMenuItem.setState(true);
		menu.add(cbMenuItem);
		menuBar.add(menu);

		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menu.getAccessibleContext().setAccessibleDescription(
				"This menu helps you");
		menuBar.add(menu);

		menuItem = new JMenuItem("About", KeyEvent.VK_A);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,
				ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"About the MConsole");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				actionAbout();
			}
		});
		menu.add(menuItem);
	}

	
	private void actionConnect() {
		String jmxServiceUrlStr=JOptionPane.showInputDialog(this, "Enter the JMX Service URL");
		
		try {
			shellPanel.openConnection(jmxServiceUrlStr);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
				    "Can't connect+\n"+e.getLocalizedMessage(),
				    "MShell error",
				    JOptionPane.ERROR_MESSAGE);
			try {
				shellPanel.closeConnection();
			} catch (Exception e1) {
			}
		}
	}

	void actionConnectPlatform() {
		try {
			shellPanel.openConnectionPlatform();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
				    "Can't connect+\n"+e.getLocalizedMessage(),
				    "MShell error",
				    JOptionPane.ERROR_MESSAGE);
			try {
				shellPanel.closeConnection();
			} catch (Exception e1) {
			}
		}
	}

	
	private void actionDisconnect() {
		try {
			shellPanel.closeConnection();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
				    "Error during disconnection+\n"+e.getLocalizedMessage(),
				    "MShell error",
				    JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void actionNew() {
		String jmxServiceUrlStr=JOptionPane.showInputDialog(this, "Enter the JMX Service URL");
		
		if(create(jmxServiceUrlStr, posX, posY, 400, 300)!=null) {
			posX += incrXY;
			posY += incrXY;
		} else {
			JOptionPane.showMessageDialog(this,
				    "Can't create a new console.",
				    "MShell error",
				    JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void actionClose() {
		int n = JOptionPane.showOptionDialog(this,
			    "Would you close this console ?",
			    "MShell",
			    JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				null);
		if(n==0) {
		    stop();
		}
	}

	private void actionQuit(){
		int n = JOptionPane.showOptionDialog(this,
			    "Would you close all the consoles and exit ?",
			    "MShell",
			    JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				null);
		if(n==0) {
		    stopAll();			
		}
	}	

	private void actionAbout(){
		JOptionPane.showMessageDialog(this,
					"JMX-based console for the Apache Felix shell", 
					"MShell",JOptionPane.INFORMATION_MESSAGE);
	}

	static void executeAll(String commandLine) {
		// TODO execute in parallel
		for (RemoteGUI remoteGUI : remoteGUIs) {
			remoteGUI.shellPanel.execute(commandLine);
		}
	}

	static boolean isBundle() {
		return isBundle;
	}

	static void setBundle(boolean isBundle) {
		RemoteGUI.isBundle = isBundle;
	}	
}
