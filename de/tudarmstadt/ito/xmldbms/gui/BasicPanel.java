package de.tudarmstadt.ito.xmldbms.gui;

/**
 * Insert the type's description here.
 * Creation date: (01/11/00 21:01:58)
 * @author: Adam
 */

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
 
public class BasicPanel extends JPanel {
	private JLabel ivjJLabel1 = null;
	private JLabel ivjJLabel111 = null;
	private JLabel ivjJLabel1111 = null;
	private JLabel ivjJLabel112 = null;
	private JLabel ivjJLabel1121 = null;
	private JLabel ivjJLabel1122 = null;
	private JLabel ivjJLabel1123 = null;
	private JLabel ivjJLabel12 = null;
	private JPanel ivjJPanel1 = null;
	private JPanel ivjJPanel11 = null;
	private JButton ivjJButton1 = null;
	private JLabel ivjJLabel1131 = null;
	private JLabel ivjJLabel11311 = null;
	private JLabel ivjJLabel121 = null;
	private JPanel ivjJPanel111 = null;
	IvjEventHandler ivjEventHandler = new IvjEventHandler();
	private JFileChooser ivjJFileChooser2 = null;
	public static int button;
	private BaseProp ivjBaseProp1 = null;
	private JButton ivjJButton1111 = null;
	private JButton ivjJButton1112 = null;
	private JButton ivjJButton11121 = null;
	private JButton ivjJButton2 = null;
	private JTextArea ivjJTextArea1 = null;
	private JTextField ivjtemplatefile = null;
	private JButton ivjJButton11 = null;
	private JLabel ivjJLabel113111 = null;
	private JPanel ivjJPanel4 = null;
	private JScrollPane ivjJScrollPane1 = null;
	private JDialog ivjJDialog2 = null;
	private JPanel ivjJDialogContentPane1 = null;
	private JPanel ivjJPanel6 = null;
	private JScrollPane ivjJScrollPane3 = null;
	private de.tudarmstadt.ito.xmldbms.gui.utils.WindowPositioner ivjWindowPositioner1 = null;
	private JTextField ivjDFCField = null;
	private JTextField ivjDriverField = null;
	private JTextField ivjNQCField = null;
	private JTextField ivjPasswordField = null;
	private JTextField ivjpropfield = null;
	private JTextField ivjPUCField = null;
	private JTextField ivjURLField = null;
	private JTextField ivjUserField = null;
	private JTextField ivjtemplatefile1 = null;
	private JButton ivjJButton12 = null;

class IvjEventHandler implements java.awt.event.ActionListener, java.awt.event.WindowListener, java.beans.PropertyChangeListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == BasicPanel.this.getJButton1()) 
				connEtoM7(e);
			if (e.getSource() == BasicPanel.this.getJButton1112()) 
				connEtoM4(e);
			if (e.getSource() == BasicPanel.this.getJButton11121()) 
				connEtoM9(e);
			if (e.getSource() == BasicPanel.this.getJButton2()) 
				connEtoM2(e);
			if (e.getSource() == BasicPanel.this.getJButton11121()) 
				connEtoM3(e);
			if (e.getSource() == BasicPanel.this.getJButton1111()) 
				connEtoM5(e);
			if (e.getSource() == BasicPanel.this.getJButton1111()) 
				connEtoM12(e);
			if (e.getSource() == BasicPanel.this.getJButton11()) 
				connEtoM1(e);
			if (e.getSource() == BasicPanel.this.getJButton11()) 
				connEtoM19(e);
			if (e.getSource() == BasicPanel.this.getJButton12()) 
				connEtoM13(e);
			if (e.getSource() == BasicPanel.this.getJButton12()) 
				connEtoM14(e);
		};
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getSource() == BasicPanel.this.getBaseProp1()) 
				connEtoM11();
		};
		public void windowActivated(java.awt.event.WindowEvent e) {};
		public void windowClosed(java.awt.event.WindowEvent e) {};
		public void windowClosing(java.awt.event.WindowEvent e) {};
		public void windowDeactivated(java.awt.event.WindowEvent e) {};
		public void windowDeiconified(java.awt.event.WindowEvent e) {};
		public void windowIconified(java.awt.event.WindowEvent e) {};
		public void windowOpened(java.awt.event.WindowEvent e) {
			if (e.getSource() == BasicPanel.this.getJDialog2()) 
				connEtoM6(e);
		};
	};
/**
 * BasicPanel constructor comment.
 */
public BasicPanel() {
	super();
	initialize();
}
/**
 * BasicPanel constructor comment.
 * @param layout java.awt.LayoutManager
 */
public BasicPanel(java.awt.LayoutManager layout) {
	super(layout);
}
/**
 * BasicPanel constructor comment.
 * @param layout java.awt.LayoutManager
 * @param isDoubleBuffered boolean
 */
public BasicPanel(java.awt.LayoutManager layout, boolean isDoubleBuffered) {
	super(layout, isDoubleBuffered);
}
/**
 * BasicPanel constructor comment.
 * @param isDoubleBuffered boolean
 */
public BasicPanel(boolean isDoubleBuffered) {
	super(isDoubleBuffered);
}
/**
 * Comment
 */
public void ChooseFile(int arg1) {
	int i = arg1;
	// System.out.println(i);
	if ( i == JFileChooser.APPROVE_OPTION) {
		File file = ivjJFileChooser2.getSelectedFile();
	//	String S = file.getName();
		String S = file.getAbsolutePath();
		ivjtemplatefile.setText(S);
	}
	
		/*
		switch(button) {
			case 0: ivjdbconf.setText(S);
				break;				
			case 1: ivjparserconf.setText(S);
				break;
			case 2: ivjoptionconf.setText(S);
				break;
			}
			*/
//		System.out.println(S);
//		String S1 = file.getAbsolutePath();
//		System.out.println(S1);
		
	//	else {System.out.println("Canceled by User");}
	return;
	
}
/**
 * connEtoM1:  (JButton11.action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1.addPropertiesFromFile(Ljava.util.Properties;Ljava.lang.String;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM1(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getBaseProp1().addPropertiesFromFile(getBaseProp1().getProp(), gettemplatefile().getText());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM10:  ( (JButton11121,action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1,propToString()Ljava.lang.String;).normalResult --> JTextArea1.text)
 * @param result java.lang.String
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM10(java.lang.String result) {
	try {
		// user code begin {1}
		// user code end
		getJTextArea1().setText(result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM11:  (BaseProp1.propertyChange. --> BasicPanel.getPropVal()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM11() {
	try {
		// user code begin {1}
		// user code end
		this.getPropVal();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM12:  (JButton1111.action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1.setChange()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM12(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getBaseProp1().setChange();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM13:  (JButton12.action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1.emptyProp()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM13(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getBaseProp1().emptyProp();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM14:  (JButton12.action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1.setChange()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM14(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getBaseProp1().setChange();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM19:  (JButton11.action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1.setChange()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM19(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getBaseProp1().setChange();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM2:  (JButton2.action.actionPerformed(java.awt.event.ActionEvent) --> JDialog2.dispose()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM2(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getJDialog2().dispose();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * Comment
 */
public void connEtoM2_NormalResult(int arg1) {
	int i = arg1;
	//System.out.println(i);
	/* if ( i == JFileChooser.APPROVE_OPTION) {
		File file = ivjJFileChooser2.getSelectedFile();
		String S = file.getName();
				switch(button) {
			case 0: ivjdbconf.setText(S);
				break;				
			case 1: ivjparserconf.setText(S);
				break;
			case 2: ivjoptionconf.setText(S);
				break;
			}
//		System.out.println(S);
//		String S1 = file.getAbsolutePath();
//		System.out.println(S1);
		}
		else {System.out.println("Canceled by User");}
		*/
	return;
}
/**
 * connEtoM3:  (JButton11121.action.actionPerformed(java.awt.event.ActionEvent) --> JDialog2.show()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM3(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getJDialog2().show();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM4:  (JButton1112.action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1.writeProps(Ljava.util.Properties;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM4(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getBaseProp1().writeProps(getBaseProp1().getProp());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM5:  (JButton1111.action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1.put(Ljava.lang.String;Ljava.lang.String;)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM5(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.setProp();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM6:  (JDialog2.window.windowOpened(java.awt.event.WindowEvent) --> WindowPositioner1.positionWindowOnScreen(Ljava.awt.Component;)V)
 * @param arg1 java.awt.event.WindowEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM6(java.awt.event.WindowEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		de.tudarmstadt.ito.xmldbms.gui.utils.WindowPositioner.positionWindowOnScreen(getJDialog2());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM7:  (JButton1.action.actionPerformed(java.awt.event.ActionEvent) --> JFileChooser2.showOpenDialog(Ljava.awt.Component;)I)
 * @return int
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private int connEtoM7(java.awt.event.ActionEvent arg1) {
	int connEtoM7Result = 0;
	try {
		// user code begin {1}
		// user code end
		connEtoM7Result = getJFileChooser2().showOpenDialog(this);
		connEtoM8(connEtoM7Result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
	return connEtoM7Result;
}
/**
 * connEtoM8:  ( (JButton1,action.actionPerformed(java.awt.event.ActionEvent) --> JFileChooser2,showOpenDialog(Ljava.awt.Component;)I).normalResult --> BasicPanel.ChooseFile(I)V)
 * @param result int
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM8(int result) {
	try {
		// user code begin {1}
		// user code end
		this.ChooseFile(result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM9:  (JButton11121.action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1.propToString()Ljava.lang.String;)
 * @return java.lang.String
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.lang.String connEtoM9(java.awt.event.ActionEvent arg1) {
	String connEtoM9Result = null;
	try {
		// user code begin {1}
		// user code end
		connEtoM9Result = getBaseProp1().propToString();
		connEtoM10(connEtoM9Result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
	return connEtoM9Result;
}
/**
 * Return the BaseProp1 property value.
 * @return de.tudarmstadt.ito.xmldbms.gui.BaseProp
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public BaseProp getBaseProp1() {
	if (ivjBaseProp1 == null) {
		try {
			ivjBaseProp1 = new de.tudarmstadt.ito.xmldbms.gui.BaseProp();
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjBaseProp1;
}
/**
 * Insert the method's description here.
 * Creation date: (01/11/00 22:54:57)
 * @return int
 */
public static int getButton() {
	return button;
}
/**
 * Return the JTextField111 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getDFCField() {
	if (ivjDFCField == null) {
		try {
			ivjDFCField = new javax.swing.JTextField();
			ivjDFCField.setName("DFCField");
			ivjDFCField.setBounds(150, 49, 429, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjDFCField;
}
/**
 * Return the JTextField12 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getDriverField() {
	if (ivjDriverField == null) {
		try {
			ivjDriverField = new javax.swing.JTextField();
			ivjDriverField.setName("DriverField");
			ivjDriverField.setBounds(150, 46, 432, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjDriverField;
}
/**
 * Return the JButton1 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton1() {
	if (ivjJButton1 == null) {
		try {
			ivjJButton1 = new javax.swing.JButton();
			ivjJButton1.setName("JButton1");
			ivjJButton1.setFont(new java.awt.Font("dialog", 0, 10));
			ivjJButton1.setMnemonic('c');
			ivjJButton1.setText("Choose Prop");
			ivjJButton1.setBounds(100, 76, 119, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton1;
}
/**
 * Return the JButton11 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton11() {
	if (ivjJButton11 == null) {
		try {
			ivjJButton11 = new javax.swing.JButton();
			ivjJButton11.setName("JButton11");
			ivjJButton11.setFont(new java.awt.Font("dialog", 0, 10));
			ivjJButton11.setMnemonic('l');
			ivjJButton11.setText("Load Prop");
			ivjJButton11.setBounds(463, 76, 119, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton11;
}
/**
 * Return the JButton1111 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton1111() {
	if (ivjJButton1111 == null) {
		try {
			ivjJButton1111 = new javax.swing.JButton();
			ivjJButton1111.setName("JButton1111");
			ivjJButton1111.setFont(new java.awt.Font("dialog", 0, 10));
			ivjJButton1111.setMnemonic('s');
			ivjJButton1111.setText("Set Prop");
			ivjJButton1111.setBounds(100, 6, 119, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton1111;
}
/**
 * Return the JButton1112 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton1112() {
	if (ivjJButton1112 == null) {
		try {
			ivjJButton1112 = new javax.swing.JButton();
			ivjJButton1112.setName("JButton1112");
			ivjJButton1112.setFont(new java.awt.Font("dialog", 0, 10));
			ivjJButton1112.setMnemonic('w');
			ivjJButton1112.setText("Write Prop");
			ivjJButton1112.setBounds(282, 6, 119, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton1112;
}
/**
 * Return the JButton11121 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton11121() {
	if (ivjJButton11121 == null) {
		try {
			ivjJButton11121 = new javax.swing.JButton();
			ivjJButton11121.setName("JButton11121");
			ivjJButton11121.setFont(new java.awt.Font("dialog", 0, 10));
			ivjJButton11121.setMnemonic('h');
			ivjJButton11121.setText("Show Prop");
			ivjJButton11121.setBounds(463, 6, 119, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton11121;
}
/**
 * Return the JButton12 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton12() {
	if (ivjJButton12 == null) {
		try {
			ivjJButton12 = new javax.swing.JButton();
			ivjJButton12.setName("JButton12");
			ivjJButton12.setFont(new java.awt.Font("dialog", 0, 10));
			ivjJButton12.setMnemonic('e');
			ivjJButton12.setText("Empty Prop");
			ivjJButton12.setBounds(282, 76, 119, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton12;
}
/**
 * Return the JButton2 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton2() {
	if (ivjJButton2 == null) {
		try {
			ivjJButton2 = new javax.swing.JButton();
			ivjJButton2.setName("JButton2");
			ivjJButton2.setMnemonic('d');
			ivjJButton2.setText("Dismiss");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton2;
}
/**
 * Return the JDialog2 property value.
 * @return javax.swing.JDialog
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JDialog getJDialog2() {
	if (ivjJDialog2 == null) {
		try {
			ivjJDialog2 = new javax.swing.JDialog();
			ivjJDialog2.setName("JDialog2");
			ivjJDialog2.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
			ivjJDialog2.setBounds(665, 256, 530, 240);
			ivjJDialog2.setTitle("Properties File");
			getJDialog2().setContentPane(getJDialogContentPane1());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJDialog2;
}
/**
 * Return the JDialogContentPane1 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJDialogContentPane1() {
	if (ivjJDialogContentPane1 == null) {
		try {
			ivjJDialogContentPane1 = new javax.swing.JPanel();
			ivjJDialogContentPane1.setName("JDialogContentPane1");
			ivjJDialogContentPane1.setLayout(new java.awt.BorderLayout());
			getJDialogContentPane1().add(getJScrollPane3(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJDialogContentPane1;
}
/**
 * Return the JFileChooser2 property value.
 * @return javax.swing.JFileChooser
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JFileChooser getJFileChooser2() {
	if (ivjJFileChooser2 == null) {
		try {
			ivjJFileChooser2 = new javax.swing.JFileChooser();
			ivjJFileChooser2.setName("JFileChooser2");
			ivjJFileChooser2.setBounds(654, 538, 500, 300);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJFileChooser2;
}
/**
 * Return the JLabel1 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1() {
	if (ivjJLabel1 == null) {
		try {
			ivjJLabel1 = new javax.swing.JLabel();
			ivjJLabel1.setName("JLabel1");
			ivjJLabel1.setFont(new java.awt.Font("Arial", 1, 14));
			ivjJLabel1.setText("Database Info:");
			ivjJLabel1.setBounds(5, 2, 125, 20);
			ivjJLabel1.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1;
}
/**
 * Return the JLabel111 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel111() {
	if (ivjJLabel111 == null) {
		try {
			ivjJLabel111 = new javax.swing.JLabel();
			ivjJLabel111.setName("JLabel111");
			ivjJLabel111.setText("URL:");
			ivjJLabel111.setBounds(5, 24, 91, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel111;
}
/**
 * Return the JLabel1111 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1111() {
	if (ivjJLabel1111 == null) {
		try {
			ivjJLabel1111 = new javax.swing.JLabel();
			ivjJLabel1111.setName("JLabel1111");
			ivjJLabel1111.setText("Document Factory Class:");
			ivjJLabel1111.setBounds(6, 49, 147, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1111;
}
/**
 * Return the JLabel112 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel112() {
	if (ivjJLabel112 == null) {
		try {
			ivjJLabel112 = new javax.swing.JLabel();
			ivjJLabel112.setName("JLabel112");
			ivjJLabel112.setText("Driver:");
			ivjJLabel112.setBounds(5, 46, 91, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel112;
}
/**
 * Return the JLabel1121 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1121() {
	if (ivjJLabel1121 == null) {
		try {
			ivjJLabel1121 = new javax.swing.JLabel();
			ivjJLabel1121.setName("JLabel1121");
			ivjJLabel1121.setText("User");
			ivjJLabel1121.setBounds(5, 68, 91, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1121;
}
/**
 * Return the JLabel1122 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1122() {
	if (ivjJLabel1122 == null) {
		try {
			ivjJLabel1122 = new javax.swing.JLabel();
			ivjJLabel1122.setName("JLabel1122");
			ivjJLabel1122.setText("Password:");
			ivjJLabel1122.setBounds(5, 90, 91, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1122;
}
/**
 * Return the JLabel1123 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1123() {
	if (ivjJLabel1123 == null) {
		try {
			ivjJLabel1123 = new javax.swing.JLabel();
			ivjJLabel1123.setName("JLabel1123");
			ivjJLabel1123.setText("Parser Utils Class:");
			ivjJLabel1123.setBounds(5, 72, 128, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1123;
}
/**
 * Return the JLabel1131 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1131() {
	if (ivjJLabel1131 == null) {
		try {
			ivjJLabel1131 = new javax.swing.JLabel();
			ivjJLabel1131.setName("JLabel1131");
			ivjJLabel1131.setText("Name Qualifier Class:");
			ivjJLabel1131.setBounds(5, 26, 128, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1131;
}
/**
 * Return the JLabel11311 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel11311() {
	if (ivjJLabel11311 == null) {
		try {
			ivjJLabel11311 = new javax.swing.JLabel();
			ivjJLabel11311.setName("JLabel11311");
			ivjJLabel11311.setText("Save As:");
			ivjJLabel11311.setBounds(4, 31, 59, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel11311;
}
/**
 * Return the JLabel113111 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel113111() {
	if (ivjJLabel113111 == null) {
		try {
			ivjJLabel113111 = new javax.swing.JLabel();
			ivjJLabel113111.setName("JLabel113111");
			ivjJLabel113111.setText("Template File:");
			ivjJLabel113111.setBounds(4, 54, 82, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel113111;
}
/**
 * Return the JLabel12 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel12() {
	if (ivjJLabel12 == null) {
		try {
			ivjJLabel12 = new javax.swing.JLabel();
			ivjJLabel12.setName("JLabel12");
			ivjJLabel12.setFont(new java.awt.Font("Arial", 1, 14));
			ivjJLabel12.setText("Parser Info:");
			ivjJLabel12.setBounds(5, 3, 91, 20);
			ivjJLabel12.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel12;
}
/**
 * Return the JLabel121 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel121() {
	if (ivjJLabel121 == null) {
		try {
			ivjJLabel121 = new javax.swing.JLabel();
			ivjJLabel121.setName("JLabel121");
			ivjJLabel121.setFont(new java.awt.Font("Arial", 1, 14));
			ivjJLabel121.setText("General Info:");
			ivjJLabel121.setBounds(4, 6, 91, 20);
			ivjJLabel121.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel121;
}
/**
 * Return the JPanel1 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel1() {
	if (ivjJPanel1 == null) {
		try {
			ivjJPanel1 = new javax.swing.JPanel();
			ivjJPanel1.setName("JPanel1");
			ivjJPanel1.setBorder(new javax.swing.border.EtchedBorder());
			ivjJPanel1.setLayout(null);
			ivjJPanel1.setBounds(7, 112, 587, 114);
			getJPanel1().add(getJLabel1(), getJLabel1().getName());
			getJPanel1().add(getJLabel111(), getJLabel111().getName());
			getJPanel1().add(getJLabel112(), getJLabel112().getName());
			getJPanel1().add(getJLabel1121(), getJLabel1121().getName());
			getJPanel1().add(getJLabel1122(), getJLabel1122().getName());
			getJPanel1().add(getURLField(), getURLField().getName());
			getJPanel1().add(getDriverField(), getDriverField().getName());
			getJPanel1().add(getUserField(), getUserField().getName());
			getJPanel1().add(getPasswordField(), getPasswordField().getName());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel1;
}
/**
 * Return the JPanel11 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel11() {
	if (ivjJPanel11 == null) {
		try {
			ivjJPanel11 = new javax.swing.JPanel();
			ivjJPanel11.setName("JPanel11");
			ivjJPanel11.setBorder(new javax.swing.border.EtchedBorder());
			ivjJPanel11.setLayout(null);
			ivjJPanel11.setBounds(7, 228, 587, 96);
			getJPanel11().add(getJLabel12(), getJLabel12().getName());
			getJPanel11().add(getJLabel1111(), getJLabel1111().getName());
			getJPanel11().add(getJLabel1123(), getJLabel1123().getName());
			getJPanel11().add(getDFCField(), getDFCField().getName());
			getJPanel11().add(getPUCField(), getPUCField().getName());
			getJPanel11().add(getJLabel1131(), getJLabel1131().getName());
			getJPanel11().add(getNQCField(), getNQCField().getName());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel11;
}
/**
 * Return the JPanel111 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel111() {
	if (ivjJPanel111 == null) {
		try {
			ivjJPanel111 = new javax.swing.JPanel();
			ivjJPanel111.setName("JPanel111");
			ivjJPanel111.setBorder(new javax.swing.border.EtchedBorder());
			ivjJPanel111.setLayout(null);
			ivjJPanel111.setBounds(7, 5, 587, 107);
			getJPanel111().add(getJLabel121(), getJLabel121().getName());
			getJPanel111().add(getpropfield(), getpropfield().getName());
			getJPanel111().add(getJLabel11311(), getJLabel11311().getName());
			getJPanel111().add(gettemplatefile(), gettemplatefile().getName());
			getJPanel111().add(getJButton1111(), getJButton1111().getName());
			getJPanel111().add(getJButton1112(), getJButton1112().getName());
			getJPanel111().add(getJButton11121(), getJButton11121().getName());
			getJPanel111().add(getJButton1(), getJButton1().getName());
			getJPanel111().add(getJLabel113111(), getJLabel113111().getName());
			getJPanel111().add(getJButton11(), getJButton11().getName());
			getJPanel111().add(gettemplatefile1(), gettemplatefile1().getName());
			getJPanel111().add(getJButton12(), getJButton12().getName());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel111;
}
/**
 * Return the JPanel4 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel4() {
	if (ivjJPanel4 == null) {
		try {
			ivjJPanel4 = new javax.swing.JPanel();
			ivjJPanel4.setName("JPanel4");
			ivjJPanel4.setLayout(new java.awt.BorderLayout());
			getJPanel4().add(getJScrollPane1(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel4;
}
/**
 * Return the JPanel6 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel6() {
	if (ivjJPanel6 == null) {
		try {
			ivjJPanel6 = new javax.swing.JPanel();
			ivjJPanel6.setName("JPanel6");
			ivjJPanel6.setLayout(new java.awt.BorderLayout());
			ivjJPanel6.setLocation(0, 0);
			getJPanel6().add(getJButton2(), "South");
			getJPanel6().add(getJPanel4(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel6;
}
/**
 * Return the JScrollPane1 property value.
 * @return javax.swing.JScrollPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JScrollPane getJScrollPane1() {
	if (ivjJScrollPane1 == null) {
		try {
			ivjJScrollPane1 = new javax.swing.JScrollPane();
			ivjJScrollPane1.setName("JScrollPane1");
			getJScrollPane1().setViewportView(getJTextArea1());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJScrollPane1;
}
/**
 * Return the JScrollPane3 property value.
 * @return javax.swing.JScrollPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JScrollPane getJScrollPane3() {
	if (ivjJScrollPane3 == null) {
		try {
			ivjJScrollPane3 = new javax.swing.JScrollPane();
			ivjJScrollPane3.setName("JScrollPane3");
			getJScrollPane3().setViewportView(getJPanel6());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJScrollPane3;
}
/**
 * Return the JTextArea1 property value.
 * @return javax.swing.JTextArea
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextArea getJTextArea1() {
	if (ivjJTextArea1 == null) {
		try {
			ivjJTextArea1 = new javax.swing.JTextArea();
			ivjJTextArea1.setName("JTextArea1");
			ivjJTextArea1.setLineWrap(true);
			ivjJTextArea1.setWrapStyleWord(true);
			ivjJTextArea1.setBounds(0, 0, 526, 66);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJTextArea1;
}
/**
 * Return the JTextField141 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getNQCField() {
	if (ivjNQCField == null) {
		try {
			ivjNQCField = new javax.swing.JTextField();
			ivjNQCField.setName("NQCField");
			ivjNQCField.setBounds(150, 26, 429, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjNQCField;
}
/**
 * Return the JTextField132 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getPasswordField() {
	if (ivjPasswordField == null) {
		try {
			ivjPasswordField = new javax.swing.JTextField();
			ivjPasswordField.setName("PasswordField");
			ivjPasswordField.setBounds(150, 90, 432, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjPasswordField;
}
/**
 * Return the JTextField1111 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getpropfield() {
	if (ivjpropfield == null) {
		try {
			ivjpropfield = new javax.swing.JTextField();
			ivjpropfield.setName("propfield");
			ivjpropfield.setBounds(100, 31, 482, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjpropfield;
}
/**
 * Insert the method's description here.
 * Creation date: (13/02/01 16:38:56)
 */
public void getPropVal() {
	

	try {
		getpropfield().setText(getBaseProp1().getPropVal(getBaseProp1().getPropFileOut()));
		gettemplatefile().setText(getBaseProp1().getPropVal(getBaseProp1().getFile()));		
		getURLField().setText(getBaseProp1().getPropVal(getBaseProp1().getURL()));

		
		getDriverField().setText(getBaseProp1().getPropVal(getBaseProp1().getDriver()));
		getUserField().setText(getBaseProp1().getPropVal(getBaseProp1().getUser()));
		getPasswordField().setText(getBaseProp1().getPropVal(getBaseProp1().getPassword()));
		
		getNQCField().setText(getBaseProp1().getPropVal(getBaseProp1().getNameQualifierClass()));
		getDFCField().setText(getBaseProp1().getPropVal(getBaseProp1().getDocumentFactoryClass()));
		getPUCField().setText(getBaseProp1().getPropVal(getBaseProp1().getParserUtilsClass()));	

	} catch (java.lang.Throwable e) {

		handleException(e);
	}
	
	
	
	}
/**
 * Return the JTextField121 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getPUCField() {
	if (ivjPUCField == null) {
		try {
			ivjPUCField = new javax.swing.JTextField();
			ivjPUCField.setName("PUCField");
			ivjPUCField.setBounds(150, 72, 429, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjPUCField;
}
/**
 * Return the parserconf1 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField gettemplatefile() {
	if (ivjtemplatefile == null) {
		try {
			ivjtemplatefile = new javax.swing.JTextField();
			ivjtemplatefile.setName("templatefile");
			ivjtemplatefile.setBounds(100, 54, 482, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjtemplatefile;
}
/**
 * Return the templatefile1 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField gettemplatefile1() {
	if (ivjtemplatefile1 == null) {
		try {
			ivjtemplatefile1 = new javax.swing.JTextField();
			ivjtemplatefile1.setName("templatefile1");
			ivjtemplatefile1.setBounds(103, 54, 482, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjtemplatefile1;
}
/**
 * Return the JTextField11 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getURLField() {
	if (ivjURLField == null) {
		try {
			ivjURLField = new javax.swing.JTextField();
			ivjURLField.setName("URLField");
			ivjURLField.setBounds(150, 24, 432, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjURLField;
}
/**
 * Return the JTextField13 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getUserField() {
	if (ivjUserField == null) {
		try {
			ivjUserField = new javax.swing.JTextField();
			ivjUserField.setName("UserField");
			ivjUserField.setBounds(150, 68, 432, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjUserField;
}
/**
 * Return the WindowPositioner1 property value.
 * @return de.tudarmstadt.ito.xmldbms.gui.utils.WindowPositioner
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private de.tudarmstadt.ito.xmldbms.gui.utils.WindowPositioner getWindowPositioner1() {
	if (ivjWindowPositioner1 == null) {
		try {
			ivjWindowPositioner1 = new de.tudarmstadt.ito.xmldbms.gui.utils.WindowPositioner();
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjWindowPositioner1;
}
/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	 System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	 exception.printStackTrace(System.out);
}
/**
 * Initializes connections
 * @exception java.lang.Exception The exception description.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() throws java.lang.Exception {
	// user code begin {1}
	// user code end
	getJButton1().addActionListener(ivjEventHandler);
	getJButton1112().addActionListener(ivjEventHandler);
	getJButton11121().addActionListener(ivjEventHandler);
	getJButton2().addActionListener(ivjEventHandler);
	getJDialog2().addWindowListener(ivjEventHandler);
	getJButton1111().addActionListener(ivjEventHandler);
	getBaseProp1().addPropertyChangeListener(ivjEventHandler);
	getJButton11().addActionListener(ivjEventHandler);
	getJButton12().addActionListener(ivjEventHandler);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("BasicPanel");
		setLayout(null);
		setSize(600, 473);
		add(getJPanel1(), getJPanel1().getName());
		add(getJPanel11(), getJPanel11().getName());
		add(getJPanel111(), getJPanel111().getName());
		initConnections();
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}
	// user code end
}
/**
 * Comment
 */
public void jFileChooser2_ActionPerformed(java.awt.event.ActionEvent e) {
	
	return;
}
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		javax.swing.JFrame frame = new javax.swing.JFrame();
		BasicPanel aBasicPanel;
		aBasicPanel = new BasicPanel();
		frame.setContentPane(aBasicPanel);
		frame.setSize(aBasicPanel.getSize());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		frame.show();
		java.awt.Insets insets = frame.getInsets();
			// 3/23/2001, Ronald Bourret
			// Commented out the following line. The methods getWidth and
			// getHeight are not supported in Swing 1.1.1
//		frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
		frame.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JPanel");
		exception.printStackTrace(System.out);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (01/11/00 22:54:57)
 * @param newButton int
 */
public static void setButton(int newButton) {
	button = newButton;
}
/**
 * Insert the method's description here.
 * Creation date: (13/02/01 16:23:42)
 */
public void setProp() {
	try {

		if (getpropfield().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getFile(), getpropfield().getText());
		}
		if (getpropfield().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getPropFileOut(), getpropfield().getText());
		}
		if (getURLField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getURL(), getURLField().getText());
		}
		if (getDriverField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getDriver(), getDriverField().getText());
		}
		if (getUserField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getUser(), getUserField().getText());
		}
		if (getPasswordField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getPassword(), getPasswordField().getText());
		}
		if (getNQCField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getNameQualifierClass(), getNQCField().getText());
		}
		if (getDFCField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getDocumentFactoryClass(), getDFCField().getText());
		}
		if (getPUCField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getParserUtilsClass(), getPUCField().getText());				
		}
		

	} catch (java.lang.Throwable e) {

		handleException(e);
	}
	
	
	
	
	
	}
;
}