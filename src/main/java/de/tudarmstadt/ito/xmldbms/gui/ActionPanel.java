package de.tudarmstadt.ito.xmldbms.gui;

// 3/23/2001, Ronald Bourret
// Replaced explicit strings with XMLDBMSProps.XXXXX values
/**
 * Insert the type's description here.
 * Creation date: (02/11/00 15:48:30)
 * @author: Adam
 */

import java.awt.*; 
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javax.swing.filechooser.*;
import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;
 
 
import java.util.StringTokenizer;

public class ActionPanel extends JPanel {
	private JLabel ivjJLabel1 = null;
	private JLabel ivjJLabel11 = null;
	private JLabel ivjJLabel12 = null;
	private JLabel ivjJLabel13 = null;

	private JPanel ivjJPanel1 = null;
	private JScrollPane ivjJScrollPane1 = null;
	private JPanel ivjChoicePanel = null;
	private JRadioButton ivjJRadioButton1 = null;
	private GridLayout ivjChoicePanelGridLayout = null;
	private JRadioButton ivjJRadioButton2 = null;
	private JPanel ivjJPanel2 = null;
	private JButton ivjJButton1 = null;
	private JButton ivjJButton11 = null;
	private JFileChooser ivjJFileChooser1 = null;

	public static int button;
	private JTextField ivjMapfile = null;
	private JTextField ivjXmlfile = null;
	IvjEventHandler ivjEventHandler = new IvjEventHandler();


	private JRadioButton ivjJRadioButton3 = null;
	private JRadioButton ivjJRadioButton4 = null;
	private JRadioButton ivjJRadioButton5 = null;
	private JRadioButton ivjJRadioButton6 = null;
	private JRadioButton ivjJRadioButton7 = null;
	private JButton ivjJButton111 = null;
	private JTextField ivjSchemaFile = null;

class IvjEventHandler implements java.awt.event.ActionListener, java.awt.event.WindowListener, java.beans.PropertyChangeListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == ActionPanel.this.getJButton1()) 
				connEtoM1(e);
			if (e.getSource() == ActionPanel.this.getJButton11()) 
				connEtoM2(e);
			if (e.getSource() == ActionPanel.this.getJButton1()) 
				connEtoM3(e);
			if (e.getSource() == ActionPanel.this.getJButton11()) 
				connEtoM4(e);
			if (e.getSource() == ActionPanel.this.getJButton111()) 
				connEtoM9(e);
			if (e.getSource() == ActionPanel.this.getJButton111()) 
				connEtoM10(e);
			if (e.getSource() == ActionPanel.this.getJButton21()) 
				connEtoM12(e);
			if (e.getSource() == ActionPanel.this.getJButton22()) 
				connEtoM16(e);
			if (e.getSource() == ActionPanel.this.getJButton22()) 
				connEtoM8(e);
			if (e.getSource() == ActionPanel.this.getJRadioButton2()) 
				connEtoM15(e);
			if (e.getSource() == ActionPanel.this.getJRadioButton5()) 
				connEtoM17(e);
			if (e.getSource() == ActionPanel.this.getJRadioButton6()) 
				connEtoM7(e);
			if (e.getSource() == ActionPanel.this.getJRadioButton7()) 
				connEtoM18(e);
			if (e.getSource() == ActionPanel.this.getJButton21()) 
				connEtoM19(e);
			if (e.getSource() == ActionPanel.this.getJButton11121()) 
				connEtoM20(e);
			if (e.getSource() == ActionPanel.this.getJButton11121()) 
				connEtoM22(e);
			if (e.getSource() == ActionPanel.this.getJButton23()) 
				connEtoM23(e);
			if (e.getSource() == ActionPanel.this.getJButton1112()) 
				connEtoM24(e);
			if (e.getSource() == ActionPanel.this.getJButton1111()) 
				connEtoM25(e);
			if (e.getSource() == ActionPanel.this.getJButton2()) 
				connEtoM27(e);
		};
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getSource() == ActionPanel.this.getBaseProp1()) 
				connEtoM26(evt);
		};
		public void windowActivated(java.awt.event.WindowEvent e) {};
		public void windowClosed(java.awt.event.WindowEvent e) {};
		public void windowClosing(java.awt.event.WindowEvent e) {};
		public void windowDeactivated(java.awt.event.WindowEvent e) {};
		public void windowDeiconified(java.awt.event.WindowEvent e) {};
		public void windowIconified(java.awt.event.WindowEvent e) {};
		public void windowOpened(java.awt.event.WindowEvent e) {
			if (e.getSource() == ActionPanel.this.getJDialog2()) 
				connEtoM13(e);
			if (e.getSource() == ActionPanel.this.getJDialog21()) 
				connEtoM14(e);
		};
	};
/**
 * TransferPanel constructor comment.
 */
public ActionPanel() {
	super();
	initialize();
}
/**
 * TransferPanel constructor comment.
 * @param layout java.awt.LayoutManager
 */
public ActionPanel(java.awt.LayoutManager layout) {
	super(layout);
}
/**
 * TransferPanel constructor comment.
 * @param layout java.awt.LayoutManager
 * @param isDoubleBuffered boolean
 */
public ActionPanel(java.awt.LayoutManager layout, boolean isDoubleBuffered) {
	super(layout, isDoubleBuffered);
}
/**
 * TransferPanel constructor comment.
 * @param isDoubleBuffered boolean
 */
public ActionPanel(boolean isDoubleBuffered) {
	super(isDoubleBuffered);
}
/**
 * Comment
 */
public void ChooseFile(int arg1) {
	int i = arg1;
	// System.out.println(i);
	if ( i == JFileChooser.APPROVE_OPTION) {
		File file = ivjJFileChooser1.getSelectedFile();
	//	String S = file.getName();
		String S = file.getAbsolutePath();
				switch(button) {
			case 0: ivjMapfile.setText(S);
				break;				
			case 1: ivjXmlfile.setText(S);
				break;
			case 2: ivjSchemaFile.setText(S);
				break;	
				;
			}
//		System.out.println(S);
//		String S1 = file.getAbsolutePath();
//		System.out.println(S1);
		}
		//else {System.out.println("Canceled by User");}
	return;
}
/**
 * connEtoM1:  (JButton1.action.actionPerformed(java.awt.event.ActionEvent) --> TransferPanel.setButton(I)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM1(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		de.tudarmstadt.ito.xmldbms.gui.ActionPanel.setButton(0);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM10:  (JButton111.action.actionPerformed(java.awt.event.ActionEvent) --> JFileChooser1.showOpenDialog(Ljava.awt.Component;)I)
 * @return int
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private int connEtoM10(java.awt.event.ActionEvent arg1) {
	int connEtoM10Result = 0;
	try {
		// user code begin {1}
		// user code end
		connEtoM10Result = getJFileChooser1().showOpenDialog(getJPanel1());
		connEtoM11(connEtoM10Result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
	return connEtoM10Result;
}
/**
 * connEtoM11:  ( (JButton111,action.actionPerformed(java.awt.event.ActionEvent) --> JFileChooser1,showOpenDialog(Ljava.awt.Component;)I).normalResult --> ActionPanel.ChooseFile(I)V)
 * @param result int
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM11(int result) {
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
 * connEtoM2:  (JButton11.action.actionPerformed(java.awt.event.ActionEvent) --> TransferPanel.setButton(I)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM2(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		de.tudarmstadt.ito.xmldbms.gui.ActionPanel.setButton(1);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM3:  (JButton1.action.actionPerformed(java.awt.event.ActionEvent) --> JFileChooser1.showOpenDialog(Ljava.awt.Component;)I)
 * @return int
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private int connEtoM3(java.awt.event.ActionEvent arg1) {
	int connEtoM3Result = 0;
	try {
		// user code begin {1}
		// user code end
		connEtoM3Result = getJFileChooser1().showOpenDialog(getJScrollPane1());
		connEtoM5(connEtoM3Result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
	return connEtoM3Result;
}
/**
 * connEtoM4:  (JButton11.action.actionPerformed(java.awt.event.ActionEvent) --> JFileChooser1.showOpenDialog(Ljava.awt.Component;)I)
 * @return int
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private int connEtoM4(java.awt.event.ActionEvent arg1) {
	int connEtoM4Result = 0;
	try {
		// user code begin {1}
		// user code end
		connEtoM4Result = getJFileChooser1().showOpenDialog(getJPanel1());
		connEtoM6(connEtoM4Result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
	return connEtoM4Result;
}
/**
 * connEtoM5:  ( (JButton1,action.actionPerformed(java.awt.event.ActionEvent) --> JFileChooser1,showOpenDialog(Ljava.awt.Component;)I).normalResult --> TransferPanel.ChooseFile(I)V)
 * @param result int
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM5(int result) {
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
 * connEtoM6:  ( (JButton11,action.actionPerformed(java.awt.event.ActionEvent) --> JFileChooser1,showOpenDialog(Ljava.awt.Component;)I).normalResult --> TransferPanel.ChooseFile(I)V)
 * @param result int
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM6(int result) {
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
 * connEtoM9:  (JButton111.action.actionPerformed(java.awt.event.ActionEvent) --> ActionPanel.setButton(I)V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM9(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		de.tudarmstadt.ito.xmldbms.gui.ActionPanel.setButton(2);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (02/11/00 19:10:01)
 * @return int
 */
public static int getButton() {
	return button;
}
/**
 * Return the ChoicePanel property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getChoicePanel() {
	if (ivjChoicePanel == null) {
		try {
			ivjChoicePanel = new javax.swing.JPanel();
			ivjChoicePanel.setName("ChoicePanel");
			ivjChoicePanel.setBorder(new javax.swing.border.EtchedBorder());
			ivjChoicePanel.setLayout(getChoicePanelGridLayout());
			ivjChoicePanel.setBounds(8, 24, 206, 176);
			getChoicePanel().add(getJRadioButton1(), getJRadioButton1().getName());
			getChoicePanel().add(getJRadioButton2(), getJRadioButton2().getName());
			getChoicePanel().add(getJRadioButton3(), getJRadioButton3().getName());
			getChoicePanel().add(getJRadioButton4(), getJRadioButton4().getName());
			getChoicePanel().add(getJRadioButton5(), getJRadioButton5().getName());
			getChoicePanel().add(getJRadioButton6(), getJRadioButton6().getName());
			getChoicePanel().add(getJRadioButton7(), getJRadioButton7().getName());
			// user code begin {1}
			/*JRadioButton toXmlButton = new JRadioButton("To XML");
			toXmlButton.setMnemonic(KeyEvent.VK_X);
			toXmlButton.setActionCommand("To XML");
			toXmlButton.setSelected(true);
			
			JRadioButton toDbButton = new JRadioButton("To DB");
			toXmlButton.setMnemonic(KeyEvent.VK_D);
			toXmlButton.setActionCommand("To DB");*/

			ButtonGroup bg = new ButtonGroup();
			bg.add(getJRadioButton1());
			bg.add(getJRadioButton2());
			bg.add(getJRadioButton3());
			bg.add(getJRadioButton4());
			bg.add(getJRadioButton5());
			bg.add(getJRadioButton6());
			bg.add(getJRadioButton7());

			/*ivjChoicePanel.add(toXmlButton);
			ivjChoicePanel.add(toDbButton); */
			

			
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjChoicePanel;
}

/**
 * Return the ChoicePanelGridLayout property value.
 * @return java.awt.GridLayout
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.GridLayout getChoicePanelGridLayout() {
	java.awt.GridLayout ivjChoicePanelGridLayout = null;
	try {
		/* Create part */
		ivjChoicePanelGridLayout = new java.awt.GridLayout(0, 1);
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	};
	return ivjChoicePanelGridLayout;
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
			ivjJButton1.setText("Choose File");
			ivjJButton1.setBounds(483, 5, 92, 20);
			ivjJButton1.setEnabled(false);
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
			ivjJButton11.setText("Choose File");
			ivjJButton11.setBounds(483, 27, 92, 20);
			ivjJButton11.setEnabled(false);
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
 * Return the JButton111 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton111() {
	if (ivjJButton111 == null) {
		try {
			ivjJButton111 = new javax.swing.JButton();
			ivjJButton111.setName("JButton111");
			ivjJButton111.setFont(new java.awt.Font("dialog", 0, 10));
			ivjJButton111.setText("Choose File");
			ivjJButton111.setBounds(483, 49, 92, 20);
			ivjJButton111.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton111;
}

/**
 * Return the JFileChooser1 property value.
 * @return javax.swing.JFileChooser
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JFileChooser getJFileChooser1() {
	if (ivjJFileChooser1 == null) {
		try {
			ivjJFileChooser1 = new javax.swing.JFileChooser();
			ivjJFileChooser1.setName("JFileChooser1");
			ivjJFileChooser1.setBounds(727, 408, 500, 316);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJFileChooser1;
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
			ivjJLabel1.setText("Actions");
			ivjJLabel1.setBounds(8, 8, 66, 14);
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
 * Return the JLabel11 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel11() {
	if (ivjJLabel11 == null) {
		try {
			ivjJLabel11 = new javax.swing.JLabel();
			ivjJLabel11.setName("JLabel11");
			ivjJLabel11.setText("Map File:");
			ivjJLabel11.setBounds(10, 8, 74, 14);
			ivjJLabel11.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel11;
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
			ivjJLabel12.setText("XML File:");
			ivjJLabel12.setBounds(10, 30, 74, 14);
			ivjJLabel12.setEnabled(false);
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
 * Return the JLabel13 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel13() {
	if (ivjJLabel13 == null) {
		try {
			ivjJLabel13 = new javax.swing.JLabel();
			ivjJLabel13.setName("JLabel13");
			ivjJLabel13.setText("Schema / DTD File");
			ivjJLabel13.setBounds(10, 52, 108, 14);
			ivjJLabel13.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel13;
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
			ivjJPanel1.setLayout(null);
			ivjJPanel1.setLocation(0, 0);
			ivjJPanel1.setMinimumSize(new java.awt.Dimension(600, 447));
			getJPanel1().add(getChoicePanel(), getChoicePanel().getName());
			getJPanel1().add(getJPanel2(), getJPanel2().getName());
			getJPanel1().add(getJPanel3(), getJPanel3().getName());
			getJPanel1().add(getJPanel4(), getJPanel4().getName());
			getJPanel1().add(getJLabel11441(), getJLabel11441().getName());
			getJPanel1().add(getJButton21(), getJButton21().getName());
			getJPanel1().add(getJButton1111(), getJButton1111().getName());
			getJPanel1().add(getJButton1112(), getJButton1112().getName());
			getJPanel1().add(getJButton2(), getJButton2().getName());
			getJPanel1().add(getJLabel1(), getJLabel1().getName());
			getJPanel1().add(getJPanel5(), getJPanel5().getName());
			getJPanel1().add(getJButton11121(), getJButton11121().getName());
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
 * Return the JPanel2 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel2() {
	if (ivjJPanel2 == null) {
		try {
			ivjJPanel2 = new javax.swing.JPanel();
			ivjJPanel2.setName("JPanel2");
			ivjJPanel2.setBorder(new javax.swing.border.EtchedBorder());
			ivjJPanel2.setLayout(null);
			ivjJPanel2.setBounds(8, 202, 581, 75);
			getJPanel2().add(getMapfile(), getMapfile().getName());
			getJPanel2().add(getJLabel11(), getJLabel11().getName());
			getJPanel2().add(getJLabel12(), getJLabel12().getName());
			getJPanel2().add(getJLabel13(), getJLabel13().getName());
			getJPanel2().add(getJButton1(), getJButton1().getName());
			getJPanel2().add(getXmlfile(), getXmlfile().getName());
			getJPanel2().add(getJButton11(), getJButton11().getName());
			getJPanel2().add(getSchemaFile(), getSchemaFile().getName());
			getJPanel2().add(getJButton111(), getJButton111().getName());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel2;
}

/**
 * Return the JRadioButton1 property value.
 * @return javax.swing.JRadioButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JRadioButton getJRadioButton1() {
	if (ivjJRadioButton1 == null) {
		try {
			ivjJRadioButton1 = new javax.swing.JRadioButton();
			ivjJRadioButton1.setName("JRadioButton1");
			ivjJRadioButton1.setMnemonic('x');
			ivjJRadioButton1.setText("Create Map from XML Schema");
			ivjJRadioButton1.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJRadioButton1;
}



/**
 * Return the JRadioButton2 property value.
 * @return javax.swing.JRadioButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JRadioButton getJRadioButton2() {
	if (ivjJRadioButton2 == null) {
		try {
			ivjJRadioButton2 = new javax.swing.JRadioButton();
			ivjJRadioButton2.setName("JRadioButton2");
			ivjJRadioButton2.setSelected(false);
			ivjJRadioButton2.setMnemonic('d');
			ivjJRadioButton2.setText("Create Map From DTD");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJRadioButton2;
}

/**
 * Return the JRadioButton3 property value.
 * @return javax.swing.JRadioButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JRadioButton getJRadioButton3() {
	if (ivjJRadioButton3 == null) {
		try {
			ivjJRadioButton3 = new javax.swing.JRadioButton();
			ivjJRadioButton3.setName("JRadioButton3");
			ivjJRadioButton3.setMnemonic('t');
			ivjJRadioButton3.setText("Create Map From Table(s)");
			ivjJRadioButton3.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJRadioButton3;
}

/**
 * Return the JRadioButton4 property value.
 * @return javax.swing.JRadioButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JRadioButton getJRadioButton4() {
	if (ivjJRadioButton4 == null) {
		try {
			ivjJRadioButton4 = new javax.swing.JRadioButton();
			ivjJRadioButton4.setName("JRadioButton4");
			ivjJRadioButton4.setText("Create Map From Select");
			ivjJRadioButton4.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJRadioButton4;
}

/**
 * Return the JRadioButton5 property value.
 * @return javax.swing.JRadioButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JRadioButton getJRadioButton5() {
	if (ivjJRadioButton5 == null) {
		try {
			ivjJRadioButton5 = new javax.swing.JRadioButton();
			ivjJRadioButton5.setName("JRadioButton5");
			ivjJRadioButton5.setMnemonic('s');
			ivjJRadioButton5.setText("Store Document");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJRadioButton5;
}

/**
 * Return the JRadioButton10 property value.
 * @return javax.swing.JRadioButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JRadioButton getJRadioButton6() {
	if (ivjJRadioButton6 == null) {
		try {
			ivjJRadioButton6 = new javax.swing.JRadioButton();
			ivjJRadioButton6.setName("JRadioButton6");
			ivjJRadioButton6.setMnemonic('r');
			ivjJRadioButton6.setText("Retrieve Document by SQL");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJRadioButton6;
}

/**
 * Return the JRadioButton7 property value.
 * @return javax.swing.JRadioButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JRadioButton getJRadioButton7() {
	if (ivjJRadioButton7 == null) {
		try {
			ivjJRadioButton7 = new javax.swing.JRadioButton();
			ivjJRadioButton7.setName("JRadioButton7");
			ivjJRadioButton7.setMnemonic('k');
			ivjJRadioButton7.setText("Retrieve Document by Key(s)");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJRadioButton7;
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
			getJScrollPane1().setViewportView(getJPanel1());
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
 * Return the JTextField1 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getMapfile() {
	if (ivjMapfile == null) {
		try {
			ivjMapfile = new javax.swing.JTextField();
			ivjMapfile.setName("Mapfile");
			ivjMapfile.setBounds(113, 5, 366, 20);
			ivjMapfile.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjMapfile;
}

/**
 * Return the JTextField111 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getSchemaFile() {
	if (ivjSchemaFile == null) {
		try {
			ivjSchemaFile = new javax.swing.JTextField();
			ivjSchemaFile.setName("SchemaFile");
			ivjSchemaFile.setBounds(113, 48, 366, 20);
			ivjSchemaFile.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjSchemaFile;
}

/**
 * Return the JTextField11 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getXmlfile() {
	if (ivjXmlfile == null) {
		try {
			ivjXmlfile = new javax.swing.JTextField();
			ivjXmlfile.setName("Xmlfile");
			ivjXmlfile.setBounds(113, 27, 366, 20);
			ivjXmlfile.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjXmlfile;
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
	getJButton11().addActionListener(ivjEventHandler);
	getJButton111().addActionListener(ivjEventHandler);
	getJButton21().addActionListener(ivjEventHandler);
	getJDialog2().addWindowListener(ivjEventHandler);
	getJButton22().addActionListener(ivjEventHandler);
	getJRadioButton2().addActionListener(ivjEventHandler);
	getJRadioButton5().addActionListener(ivjEventHandler);
	getJRadioButton6().addActionListener(ivjEventHandler);
	getJRadioButton7().addActionListener(ivjEventHandler);
	getJDialog21().addWindowListener(ivjEventHandler);
	getJButton11121().addActionListener(ivjEventHandler);
	getJButton23().addActionListener(ivjEventHandler);
	getJButton1112().addActionListener(ivjEventHandler);
	getJButton1111().addActionListener(ivjEventHandler);
	getBaseProp1().addPropertyChangeListener(ivjEventHandler);
	getJButton2().addActionListener(ivjEventHandler);
}

/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("TransferPanel");
		setPreferredSize(new java.awt.Dimension(600, 510));
		setLayout(new java.awt.BorderLayout());
		setSize(600, 472);
		add(getJScrollPane1(), "Center");
		initConnections();
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}
	// user code end
}

/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		JFrame frame = new javax.swing.JFrame();
		ActionPanel aActionPanel;
		aActionPanel = new ActionPanel();
		frame.setContentPane(aActionPanel);
		frame.setSize(aActionPanel.getSize());
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
 * Creation date: (02/11/00 19:10:01)
 * @param newButton int
 */
public static void setButton(int newButton) {
	button = newButton;
}
;
	private JTextField ivjBasenameField = null;	private BaseProp ivjBaseProp1 = null;	private JTextField ivjCatalogField = null;	private JPanel ivjCoMode = null;	private de.tudarmstadt.ito.xmldbms.tools.GenerateMap ivjGenerateMap1 = null;	private JButton ivjJButton1111 = null;	private JButton ivjJButton1112 = null;	private JButton ivjJButton11121 = null;	private JButton ivjJButton2 = null;	private JButton ivjJButton21 = null;	private JButton ivjJButton22 = null;	private JButton ivjJButton23 = null;	private JCheckBox ivjJCheckBox1 = null;	private JDialog ivjJDialog2 = null;	private JDialog ivjJDialog21 = null;	private JPanel ivjJDialogContentPane1 = null;	private JPanel ivjJDialogContentPane11 = null;	private JLabel ivjJLabel111 = null;	private JLabel ivjJLabel112 = null;	private JLabel ivjJLabel113 = null;	private JLabel ivjJLabel114 = null;	private JLabel ivjJLabel1141 = null;	private JLabel ivjJLabel1142 = null;	private JLabel ivjJLabel1143 = null;	private JLabel ivjJLabel1144 = null;	private JLabel ivjJLabel11441 = null;	private JLabel ivjJLabel1145 = null;	private JLabel ivjJLabel115 = null;	private JLabel ivjJLabel116 = null;	private JPanel ivjJPanel3 = null;	private JPanel ivjJPanel4 = null;	private JPanel ivjJPanel41 = null;	private JPanel ivjJPanel42 = null;	private JPanel ivjJPanel5 = null;	private JPanel ivjJPanel6 = null;	private JPanel ivjJPanel61 = null;	private JRadioButton ivjJRadioButton8 = null;	private JRadioButton ivjJRadioButton9 = null;	private JScrollPane ivjJScrollPane11 = null;	private JScrollPane ivjJScrollPane111 = null;	private JScrollPane ivjJScrollPane12 = null;	private JScrollPane ivjJScrollPane3 = null;	private JScrollPane ivjJScrollPane31 = null;	private JTextArea ivjJTextArea1 = null;	private JTextArea ivjJTextArea12 = null;	private JTextField ivjKeyField = null;	private JTextField ivjKGCField = null;	private JTextField ivjNSField = null;	private JTextField ivjPrefixField = null;	private JTextField ivjSchemaField = null;	private JTextArea ivjSelect = null;	private JTextField ivjSQLSepField = null;	private JTextField ivjTableField = null;	private de.tudarmstadt.ito.xmldbms.tools.Transfer ivjTransfer1 = null;	private de.tudarmstadt.ito.xmldbms.gui.utils.WindowPositioner ivjWindowPositioner1 = null;/**
 * Insert the method's description here.
 * Creation date: (14/02/01 17:10:30)
 */
public void choice_CreateMapFromDTD() {
	
	
	try {

		//Turn off all GUI elements
		disableGUIElements();
		//Turn on the elements needed
		
		//Labels

		getJLabel13().setEnabled(true);
		getJLabel113().setEnabled(true);
		getJLabel115().setEnabled(true);
		
		getJLabel1141().setEnabled(true);
		getJLabel1142().setEnabled(true);
		getJLabel1143().setEnabled(true);
		getJLabel1144().setEnabled(true);


		//Buttons

		getJButton111().setEnabled(true);

		//RadioButtons


		//CheckBox

		getJCheckBox1().setEnabled(true);

		//TextFields
		getCatalogField().setEnabled(true);
		getSchemaField().setEnabled(true);
		getSQLSepField().setEnabled(true);
//		getBasenameField().setEnabled(false);
		getSchemaFile().setEnabled(true);
		getPrefixField().setEnabled(true);
		getNSField().setEnabled(true);

		//SQLTextArea


	} catch (java.lang.Throwable e) {

		handleException(e);
	}
	
	
	}/**
 * Insert the method's description here.
 * Creation date: (14/02/01 17:13:53)
 */ 
public void choice_RetrieveDocByKey() {
	
	
	try {
		//Turn off all GUI elements
		disableGUIElements();
		//Turn on the elements needed
		
		//Labels
		getJLabel11().setEnabled(true);
		getJLabel12().setEnabled(true);
		getJLabel112().setEnabled(true);
		getJLabel116().setEnabled(true);

		//Buttons
		getJButton1().setEnabled(true);
		getJButton11().setEnabled(true);

		//RadioButtons


		//CheckBox


		//TextFields

		getMapfile().setEnabled(true);
		getXmlfile().setEnabled(true);

		getTableField().setEnabled(true);
		getKeyField().setEnabled(true);


		//SQLTextArea


	} catch (java.lang.Throwable e) {

		handleException(e);
	}
	
	
	
	
	
	
	
	}/**
 * Insert the method's description here.
 * Creation date: (14/02/01 17:13:20)
 */ 
public void choice_RetrieveDocBySQL() {
	
	
	try {


		//Turn off all GUI elements
		disableGUIElements();
		//Turn on the elements needed
			
		//Labels
		getJLabel11().setEnabled(true);
		getJLabel12().setEnabled(true);

		getJLabel11441().setEnabled(true);

		//Buttons
		getJButton21().setEnabled(true);
		getJButton1().setEnabled(true);
		getJButton11().setEnabled(true);

		//RadioButtons


		//CheckBox


		//TextFields
		getMapfile().setEnabled(true);
		getXmlfile().setEnabled(true);


		//SQLTextArea
		getSelect().setEnabled(true);

	} catch (java.lang.Throwable e) {

		handleException(e);
	}
	
	
	
	
	
	
	
	}/**
 * Insert the method's description here.
 * Creation date: (14/02/01 17:12:06)
 */ 
public void choice_StoreDocument() {

	try {

		//Turn off all GUI elements
		disableGUIElements();
		//Turn on the elements needed		
		//Labels
		getJLabel11().setEnabled(true);
		getJLabel12().setEnabled(true);
		getJLabel111().setEnabled(true);
		getJLabel114().setEnabled(true);


		//Buttons
		getJButton1().setEnabled(true);
		getJButton11().setEnabled(true);

		//RadioButtons

		getJRadioButton8().setEnabled(true);
		getJRadioButton9().setEnabled(true);

		//CheckBox


		//TextFields
		getMapfile().setEnabled(true);
		getXmlfile().setEnabled(true);
		getKGCField().setEnabled(true);


		//SQLTextArea


	} catch (java.lang.Throwable e) {

		handleException(e);
	}
	
	}/**
 * connEtoM12:  (JButton21.action.actionPerformed(java.awt.event.ActionEvent) --> JDialog2.show()V)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM12(java.awt.event.ActionEvent arg1) {
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
}/**
 * connEtoM13:  (JDialog2.window.windowOpened(java.awt.event.WindowEvent) --> WindowPositioner1.positionWindowOnScreen(Ljava.awt.Component;)V)
 * @param arg1 java.awt.event.WindowEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM13(java.awt.event.WindowEvent arg1) {
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
}/**
 * connEtoM14:  (JDialog21.window.windowOpened(java.awt.event.WindowEvent) --> WindowPositioner1.positionWindowOnScreen(Ljava.awt.Component;)V)
 * @param arg1 java.awt.event.WindowEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM14(java.awt.event.WindowEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		de.tudarmstadt.ito.xmldbms.gui.utils.WindowPositioner.positionWindowOnScreen(getJDialog21());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}/**
 * connEtoM15:  (JRadioButton2.action.actionPerformed(java.awt.event.ActionEvent) --> ActionPanel.choice_CreateMapFromDTD()V)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM15(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.choice_CreateMapFromDTD();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}/**
 * connEtoM16:  (JButton22.action.actionPerformed(java.awt.event.ActionEvent) --> JTextArea11.text)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM16(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getSelect().setText(getJTextArea1().getText());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM17:  (JRadioButton5.action.actionPerformed(java.awt.event.ActionEvent) --> ActionPanel.choice_StoreDocument()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM17(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.choice_StoreDocument();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}/**
 * connEtoM18:  (JRadioButton7.action.actionPerformed(java.awt.event.ActionEvent) --> ActionPanel.choice_RetrieveDocByKey()V)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM18(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.choice_RetrieveDocByKey();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}/**
 * connEtoM19:  (JButton21.action.actionPerformed(java.awt.event.ActionEvent) --> JTextArea1.text)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM19(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getJTextArea1().setText(getSelect().getText());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM20:  (JButton11121.action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1.propToString()Ljava.lang.String;)
 * @return java.lang.String
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.lang.String connEtoM20(java.awt.event.ActionEvent arg1) {
	String connEtoM20Result = null;
	try {
		// user code begin {1}
		// user code end
		connEtoM20Result = getBaseProp1().propToString();
		connEtoM21(connEtoM20Result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
	return connEtoM20Result;
}/**
 * connEtoM21:  ( (JButton11121,action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1,propToString()Ljava.lang.String;).normalResult --> JTextArea12.text)
 * @param result java.lang.String
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM21(java.lang.String result) {
	try {
		// user code begin {1}
		// user code end
		getJTextArea12().setText(result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}/**
 * connEtoM22:  (JButton11121.action.actionPerformed(java.awt.event.ActionEvent) --> JDialog21.show()V)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM22(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getJDialog21().show();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}/**
 * connEtoM23:  (JButton23.action.actionPerformed(java.awt.event.ActionEvent) --> JDialog21.dispose()V)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM23(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		getJDialog21().dispose();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}/**
 * connEtoM24:  (JButton1112.action.actionPerformed(java.awt.event.ActionEvent) --> BaseProp1.writeProps(Ljava.util.Properties;)V)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM24(java.awt.event.ActionEvent arg1) {
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
}/**
 * connEtoM25:  (JButton1111.action.actionPerformed(java.awt.event.ActionEvent) --> ActionPanel.setProp()V)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM25(java.awt.event.ActionEvent arg1) {
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
}/**
 * connEtoM26:  (BaseProp1.propertyChange.propertyChange(java.beans.PropertyChangeEvent) --> ActionPanel.getPropVal()V)
 * @param arg1 java.beans.PropertyChangeEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM26(java.beans.PropertyChangeEvent arg1) {
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
}/**
 * connEtoM27:  (JButton2.action.actionPerformed(java.awt.event.ActionEvent) --> ActionPanel.dispatcher()V)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM27(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.dispatcher();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}/**
 * connEtoM7:  (JRadioButton6.action.actionPerformed(java.awt.event.ActionEvent) --> ActionPanel.choice_RetrieveDocBySQL()V)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM7(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.choice_RetrieveDocBySQL();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}/**
 * connEtoM8:  (JButton22.action.actionPerformed(java.awt.event.ActionEvent) --> JDialog2.dispose()V)
 * @param arg1 java.awt.event.ActionEvent
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM8(java.awt.event.ActionEvent arg1) {
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
}/**
 * Insert the method's description here.
 * Creation date: (14/02/01 16:12:21)
 */ 
public void disableGUIElements() {

		try {
		//Labels
		getJLabel11().setEnabled(false);
		getJLabel12().setEnabled(false);
		getJLabel13().setEnabled(false);
		getJLabel111().setEnabled(false);
		getJLabel112().setEnabled(false);
		getJLabel113().setEnabled(false);
		getJLabel114().setEnabled(false);
		getJLabel115().setEnabled(false);
		getJLabel116().setEnabled(false);
		
		getJLabel1141().setEnabled(false);
		getJLabel1142().setEnabled(false);
		getJLabel1143().setEnabled(false);
		getJLabel1144().setEnabled(false);
		getJLabel1145().setEnabled(false);

		getJLabel11441().setEnabled(false);

		//Buttons
		getJButton21().setEnabled(false);
		getJButton1().setEnabled(false);
		getJButton11().setEnabled(false);
		getJButton111().setEnabled(false);

		//RadioButtons

		getJRadioButton8().setEnabled(false);
		getJRadioButton9().setEnabled(false);

		//CheckBox

		getJCheckBox1().setEnabled(false);

		//TextFields
		getCatalogField().setEnabled(false);
		getSchemaField().setEnabled(false);
		getSQLSepField().setEnabled(false);
		getBasenameField().setEnabled(false);
		getMapfile().setEnabled(false);
		getXmlfile().setEnabled(false);
		getSchemaFile().setEnabled(false);
		getKGCField().setEnabled(false);
		getTableField().setEnabled(false);
		getKeyField().setEnabled(false);
		getPrefixField().setEnabled(false);
		getNSField().setEnabled(false);

		//SQLTextArea
		getSelect().setEnabled(false);

	} catch (java.lang.Throwable e) {

		handleException(e);
	}
	
	
	
	
	}/**
 * Insert the method's description here.
 * Creation date: (19/02/01 15:14:06)
 */ 
public void dispatcher() {
	
	//System.out.println("Dispatching");
	
	try {

		
		if (getJRadioButton1().isSelected() ) {
		getGenerateMap1().dispatch(getBaseProp1().getProp());
		}
		if (getJRadioButton2().isSelected() ) {
		getGenerateMap1().dispatch(getBaseProp1().getProp());
//		System.out.println("GMFromDTD");
		}
		if (getJRadioButton3().isSelected() ) {
		getGenerateMap1().dispatch(getBaseProp1().getProp());
		}
		if (getJRadioButton4().isSelected() ) {
		getGenerateMap1().dispatch(getBaseProp1().getProp());
		}

//Transfer
		if (getJRadioButton5().isSelected() ) {
		getTransfer1().dispatch(getBaseProp1().getProp());
		}		
		if (getJRadioButton6().isSelected() ) {
		getTransfer1().dispatch(getBaseProp1().getProp());
		}
		if (getJRadioButton7().isSelected() ) {
		getTransfer1().dispatch(getBaseProp1().getProp());
		}	

//		System.out.println("Dispatching");	
	} catch (java.lang.Throwable ivjExc) {

		handleException(ivjExc);
	}

	
			
	
	}/**
 * Return the JTextField11223 property value.
 * @return javax.swing.JTextField
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getBasenameField() {
	if (ivjBasenameField == null) {
		try {
			ivjBasenameField = new javax.swing.JTextField();
			ivjBasenameField.setName("BasenameField");
			ivjBasenameField.setBounds(106, 147, 258, 20);
			ivjBasenameField.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjBasenameField;
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
}/**
 * Return the JTextField1122 property value.
 * @return javax.swing.JTextField
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getCatalogField() {
	if (ivjCatalogField == null) {
		try {
			ivjCatalogField = new javax.swing.JTextField();
			ivjCatalogField.setName("CatalogField");
			ivjCatalogField.setBounds(106, 63, 258, 20);
			ivjCatalogField.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjCatalogField;
}
/**
 * Return the CoMode property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getCoMode() {
	if (ivjCoMode == null) {
		try {
			ivjCoMode = new javax.swing.JPanel();
			ivjCoMode.setName("CoMode");
			ivjCoMode.setLayout(new java.awt.BorderLayout());
			ivjCoMode.setBounds(106, 7, 201, 32);
			getCoMode().add(getJRadioButton9(), "Center");
			getCoMode().add(getJRadioButton8(), "North");
			// user code begin {1}
			ButtonGroup bg1 = new ButtonGroup();
			bg1.add(getJRadioButton8());
			bg1.add(getJRadioButton9());

			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjCoMode;
}
/**
 * Return the GenerateMap1 property value.
 * @return de.tudarmstadt.ito.xmldbms.tools.GenerateMap
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private de.tudarmstadt.ito.xmldbms.tools.GenerateMap getGenerateMap1() {
	if (ivjGenerateMap1 == null) {
		try {
			ivjGenerateMap1 = new de.tudarmstadt.ito.xmldbms.tools.GenerateMap();
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjGenerateMap1;
}/**
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
			ivjJButton1111.setBounds(217, 7, 77, 17);
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
			ivjJButton1112.setBounds(362, 7, 83, 17);
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
			ivjJButton11121.setBounds(492, 7, 98, 17);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton11121;
}/**
 * Return the JButton2 property value.
 * @return javax.swing.JButton
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton2() {
	if (ivjJButton2 == null) {
		try {
			ivjJButton2 = new javax.swing.JButton();
			ivjJButton2.setName("JButton2");
			ivjJButton2.setText("Go!");
			ivjJButton2.setBounds(138, 7, 76, 17);
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
 * Return the JButton21 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton21() {
	if (ivjJButton21 == null) {
		try {
			ivjJButton21 = new javax.swing.JButton();
			ivjJButton21.setName("JButton21");
			ivjJButton21.setText("Show SQL Window");
			ivjJButton21.setBounds(66, 405, 150, 17);
			ivjJButton21.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton21;
}
/**
 * Return the JButton22 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton22() {
	if (ivjJButton22 == null) {
		try {
			ivjJButton22 = new javax.swing.JButton();
			ivjJButton22.setName("JButton22");
			ivjJButton22.setMnemonic('o');
			ivjJButton22.setText("OK");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton22;
}
/**
 * Return the JButton23 property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getJButton23() {
	if (ivjJButton23 == null) {
		try {
			ivjJButton23 = new javax.swing.JButton();
			ivjJButton23.setName("JButton23");
			ivjJButton23.setMnemonic('d');
			ivjJButton23.setText("Dismiss");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJButton23;
}/**
 * Return the JCheckBox1 property value.
 * @return javax.swing.JCheckBox
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getJCheckBox1() {
	if (ivjJCheckBox1 == null) {
		try {
			ivjJCheckBox1 = new javax.swing.JCheckBox();
			ivjJCheckBox1.setName("JCheckBox1");
			ivjJCheckBox1.setText("");
			ivjJCheckBox1.setBounds(106, 36, 20, 22);
			ivjJCheckBox1.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJCheckBox1;
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
			ivjJDialog2.setBounds(865, 77, 530, 240);
			ivjJDialog2.setTitle("SQL Window");
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
 * Return the JDialog21 property value.
 * @return javax.swing.JDialog
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JDialog getJDialog21() {
	if (ivjJDialog21 == null) {
		try {
			ivjJDialog21 = new javax.swing.JDialog();
			ivjJDialog21.setName("JDialog21");
			ivjJDialog21.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
			ivjJDialog21.setBounds(129, 539, 530, 240);
			ivjJDialog21.setTitle("Properties File");
			getJDialog21().setContentPane(getJDialogContentPane11());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJDialog21;
}/**
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
}/**
 * Return the JDialogContentPane11 property value.
 * @return javax.swing.JPanel
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJDialogContentPane11() {
	if (ivjJDialogContentPane11 == null) {
		try {
			ivjJDialogContentPane11 = new javax.swing.JPanel();
			ivjJDialogContentPane11.setName("JDialogContentPane11");
			ivjJDialogContentPane11.setLayout(new java.awt.BorderLayout());
			getJDialogContentPane11().add(getJScrollPane31(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJDialogContentPane11;
}/**
 * Return the JLabel111 property value.
 * @return javax.swing.JLabel
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel111() {
	if (ivjJLabel111 == null) {
		try {
			ivjJLabel111 = new javax.swing.JLabel();
			ivjJLabel111.setName("JLabel111");
			ivjJLabel111.setText("Commit Mode:");
			ivjJLabel111.setBounds(7, 13, 82, 14);
			ivjJLabel111.setEnabled(false);
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
 * Return the JLabel112 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel112() {
	if (ivjJLabel112 == null) {
		try {
			ivjJLabel112 = new javax.swing.JLabel();
			ivjJLabel112.setName("JLabel112");
			ivjJLabel112.setText("Key(s):");
			ivjJLabel112.setBounds(8, 55, 56, 14);
			ivjJLabel112.setEnabled(false);
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
 * Return the JLabel113 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel113() {
	if (ivjJLabel113 == null) {
		try {
			ivjJLabel113 = new javax.swing.JLabel();
			ivjJLabel113.setName("JLabel113");
			ivjJLabel113.setText("Order Columns?");
			ivjJLabel113.setBounds(7, 40, 96, 14);
			ivjJLabel113.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel113;
}
/**
 * Return the JLabel114 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel114() {
	if (ivjJLabel114 == null) {
		try {
			ivjJLabel114 = new javax.swing.JLabel();
			ivjJLabel114.setName("JLabel114");
			ivjJLabel114.setText("Key Generator Class:");
			ivjJLabel114.setBounds(8, 9, 128, 14);
			ivjJLabel114.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel114;
}
/**
 * Return the JLabel1141 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1141() {
	if (ivjJLabel1141 == null) {
		try {
			ivjJLabel1141 = new javax.swing.JLabel();
			ivjJLabel1141.setName("JLabel1141");
			ivjJLabel1141.setText("Schema:");
			ivjJLabel1141.setBounds(7, 94, 63, 14);
			ivjJLabel1141.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1141;
}
/**
 * Return the JLabel1142 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1142() {
	if (ivjJLabel1142 == null) {
		try {
			ivjJLabel1142 = new javax.swing.JLabel();
			ivjJLabel1142.setName("JLabel1142");
			ivjJLabel1142.setText("Prefix(es):");
			ivjJLabel1142.setBounds(8, 78, 63, 14);
			ivjJLabel1142.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1142;
}
/**
 * Return the JLabel1143 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1143() {
	if (ivjJLabel1143 == null) {
		try {
			ivjJLabel1143 = new javax.swing.JLabel();
			ivjJLabel1143.setName("JLabel1143");
			ivjJLabel1143.setText("Catalog:");
			ivjJLabel1143.setBounds(7, 67, 56, 14);
			ivjJLabel1143.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1143;
}
/**
 * Return the JLabel1144 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1144() {
	if (ivjJLabel1144 == null) {
		try {
			ivjJLabel1144 = new javax.swing.JLabel();
			ivjJLabel1144.setName("JLabel1144");
			ivjJLabel1144.setText("NameSpace(s):");
			ivjJLabel1144.setBounds(8, 101, 97, 14);
			ivjJLabel1144.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1144;
}
/**
 * Return the JLabel11441 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel11441() {
	if (ivjJLabel11441 == null) {
		try {
			ivjJLabel11441 = new javax.swing.JLabel();
			ivjJLabel11441.setName("JLabel11441");
			ivjJLabel11441.setText("Select:");
			ivjJLabel11441.setBounds(8, 406, 60, 14);
			ivjJLabel11441.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel11441;
}
/**
 * Return the JLabel1145 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1145() {
	if (ivjJLabel1145 == null) {
		try {
			ivjJLabel1145 = new javax.swing.JLabel();
			ivjJLabel1145.setName("JLabel1145");
			ivjJLabel1145.setText("Basename:");
			ivjJLabel1145.setBounds(7, 148, 74, 14);
			ivjJLabel1145.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1145;
}
/**
 * Return the JLabel115 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel115() {
	if (ivjJLabel115 == null) {
		try {
			ivjJLabel115 = new javax.swing.JLabel();
			ivjJLabel115.setName("JLabel115");
			ivjJLabel115.setText("SQL Separator:");
			ivjJLabel115.setBounds(7, 121, 96, 14);
			ivjJLabel115.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel115;
}
/**
 * Return the JLabel116 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel116() {
	if (ivjJLabel116 == null) {
		try {
			ivjJLabel116 = new javax.swing.JLabel();
			ivjJLabel116.setName("JLabel116");
			ivjJLabel116.setText("Table(s):");
			ivjJLabel116.setBounds(8, 32, 59, 14);
			ivjJLabel116.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel116;
}
/**
 * Return the JPanel3 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel3() {
	if (ivjJPanel3 == null) {
		try {
			ivjJPanel3 = new javax.swing.JPanel();
			ivjJPanel3.setName("JPanel3");
			ivjJPanel3.setBorder(new javax.swing.border.EtchedBorder());
			ivjJPanel3.setLayout(null);
			ivjJPanel3.setBounds(217, 24, 373, 176);
			getJPanel3().add(getJLabel111(), getJLabel111().getName());
			getJPanel3().add(getJLabel113(), getJLabel113().getName());
			getJPanel3().add(getJLabel115(), getJLabel115().getName());
			getJPanel3().add(getJLabel1141(), getJLabel1141().getName());
			getJPanel3().add(getJLabel1143(), getJLabel1143().getName());
			getJPanel3().add(getJLabel1145(), getJLabel1145().getName());
			getJPanel3().add(getCatalogField(), getCatalogField().getName());
			getJPanel3().add(getSchemaField(), getSchemaField().getName());
			getJPanel3().add(getSQLSepField(), getSQLSepField().getName());
			getJPanel3().add(getBasenameField(), getBasenameField().getName());
			getJPanel3().add(getJCheckBox1(), getJCheckBox1().getName());
			getJPanel3().add(getCoMode(), getCoMode().getName());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel3;
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
			ivjJPanel4.setBorder(new javax.swing.border.EtchedBorder());
			ivjJPanel4.setLayout(null);
			ivjJPanel4.setBounds(8, 280, 580, 125);
			getJPanel4().add(getJLabel1144(), getJLabel1144().getName());
			getJPanel4().add(getJLabel114(), getJLabel114().getName());
			getJPanel4().add(getJLabel116(), getJLabel116().getName());
			getJPanel4().add(getJLabel112(), getJLabel112().getName());
			getJPanel4().add(getJLabel1142(), getJLabel1142().getName());
			getJPanel4().add(getKGCField(), getKGCField().getName());
			getJPanel4().add(getTableField(), getTableField().getName());
			getJPanel4().add(getKeyField(), getKeyField().getName());
			getJPanel4().add(getPrefixField(), getPrefixField().getName());
			getJPanel4().add(getNSField(), getNSField().getName());
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
 * Return the JPanel41 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel41() {
	if (ivjJPanel41 == null) {
		try {
			ivjJPanel41 = new javax.swing.JPanel();
			ivjJPanel41.setName("JPanel41");
			ivjJPanel41.setLayout(new java.awt.BorderLayout());
			getJPanel41().add(getJScrollPane11(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel41;
}/**
 * Return the JPanel42 property value.
 * @return javax.swing.JPanel
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel42() {
	if (ivjJPanel42 == null) {
		try {
			ivjJPanel42 = new javax.swing.JPanel();
			ivjJPanel42.setName("JPanel42");
			ivjJPanel42.setLayout(new java.awt.BorderLayout());
			getJPanel42().add(getJScrollPane12(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel42;
}/**
 * Return the JPanel5 property value.
 * @return javax.swing.JPanel
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel5() {
	if (ivjJPanel5 == null) {
		try {
			ivjJPanel5 = new javax.swing.JPanel();
			ivjJPanel5.setName("JPanel5");
			ivjJPanel5.setLayout(new java.awt.BorderLayout());
			ivjJPanel5.setBounds(6, 426, 580, 40);
			getJPanel5().add(getJScrollPane111(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel5;
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
			getJPanel6().add(getJButton22(), "South");
			getJPanel6().add(getJPanel41(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel6;
}/**
 * Return the JPanel61 property value.
 * @return javax.swing.JPanel
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel61() {
	if (ivjJPanel61 == null) {
		try {
			ivjJPanel61 = new javax.swing.JPanel();
			ivjJPanel61.setName("JPanel61");
			ivjJPanel61.setLayout(new java.awt.BorderLayout());
			ivjJPanel61.setLocation(0, 0);
			getJPanel61().add(getJButton23(), "South");
			getJPanel61().add(getJPanel42(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel61;
}/**
 * Return the JRadioButton8 property value.
 * @return javax.swing.JRadioButton
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JRadioButton getJRadioButton8() {
	if (ivjJRadioButton8 == null) {
		try {
			ivjJRadioButton8 = new javax.swing.JRadioButton();
			ivjJRadioButton8.setName("JRadioButton8");
			ivjJRadioButton8.setText("After Insert");
			ivjJRadioButton8.setMaximumSize(new java.awt.Dimension(10, 22));
			ivjJRadioButton8.setPreferredSize(new java.awt.Dimension(10, 15));
			ivjJRadioButton8.setMinimumSize(new java.awt.Dimension(10, 10));
			ivjJRadioButton8.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJRadioButton8;
}
/**
 * Return the JRadioButton9 property value.
 * @return javax.swing.JRadioButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JRadioButton getJRadioButton9() {
	if (ivjJRadioButton9 == null) {
		try {
			ivjJRadioButton9 = new javax.swing.JRadioButton();
			ivjJRadioButton9.setName("JRadioButton9");
			ivjJRadioButton9.setFont(new java.awt.Font("Arial", 1, 12));
			ivjJRadioButton9.setText("After Document");
			ivjJRadioButton9.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJRadioButton9;
}
/**
 * Return the JScrollPane11 property value.
 * @return javax.swing.JScrollPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JScrollPane getJScrollPane11() {
	if (ivjJScrollPane11 == null) {
		try {
			ivjJScrollPane11 = new javax.swing.JScrollPane();
			ivjJScrollPane11.setName("JScrollPane11");
			getJScrollPane11().setViewportView(getJTextArea1());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJScrollPane11;
}/**
 * Return the JScrollPane111 property value.
 * @return javax.swing.JScrollPane
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JScrollPane getJScrollPane111() {
	if (ivjJScrollPane111 == null) {
		try {
			ivjJScrollPane111 = new javax.swing.JScrollPane();
			ivjJScrollPane111.setName("JScrollPane111");
			getJScrollPane111().setViewportView(getSelect());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJScrollPane111;
}
/**
 * Return the JScrollPane12 property value.
 * @return javax.swing.JScrollPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JScrollPane getJScrollPane12() {
	if (ivjJScrollPane12 == null) {
		try {
			ivjJScrollPane12 = new javax.swing.JScrollPane();
			ivjJScrollPane12.setName("JScrollPane12");
			getJScrollPane12().setViewportView(getJTextArea12());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJScrollPane12;
}/**
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
}/**
 * Return the JScrollPane31 property value.
 * @return javax.swing.JScrollPane
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JScrollPane getJScrollPane31() {
	if (ivjJScrollPane31 == null) {
		try {
			ivjJScrollPane31 = new javax.swing.JScrollPane();
			ivjJScrollPane31.setName("JScrollPane31");
			getJScrollPane31().setViewportView(getJPanel61());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJScrollPane31;
}/**
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
}/**
 * Return the JTextArea12 property value.
 * @return javax.swing.JTextArea
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextArea getJTextArea12() {
	if (ivjJTextArea12 == null) {
		try {
			ivjJTextArea12 = new javax.swing.JTextArea();
			ivjJTextArea12.setName("JTextArea12");
			ivjJTextArea12.setLineWrap(true);
			ivjJTextArea12.setWrapStyleWord(true);
			ivjJTextArea12.setBounds(0, 0, 526, 66);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJTextArea12;
}/**
 * Return the JTextField11212 property value.
 * @return javax.swing.JTextField
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getKeyField() {
	if (ivjKeyField == null) {
		try {
			ivjKeyField = new javax.swing.JTextField();
			ivjKeyField.setName("KeyField");
			ivjKeyField.setBounds(131, 52, 442, 20);
			ivjKeyField.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjKeyField;
}
/**
 * Return the JTextField1121 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getKGCField() {
	if (ivjKGCField == null) {
		try {
			ivjKGCField = new javax.swing.JTextField();
			ivjKGCField.setName("KGCField");
			ivjKGCField.setBounds(131, 7, 442, 20);
			ivjKGCField.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjKGCField;
}
/**
 * Return the JTextField11214 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getNSField() {
	if (ivjNSField == null) {
		try {
			ivjNSField = new javax.swing.JTextField();
			ivjNSField.setName("NSField");
			ivjNSField.setBounds(131, 98, 442, 20);
			ivjNSField.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjNSField;
}
/**
 * Return the JTextField11213 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getPrefixField() {
	if (ivjPrefixField == null) {
		try {
			ivjPrefixField = new javax.swing.JTextField();
			ivjPrefixField.setName("PrefixField");
			ivjPrefixField.setBounds(131, 75, 442, 20);
			ivjPrefixField.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjPrefixField;
}
/**
 * Insert the method's description here.
 * Creation date: (13/02/01 16:38:56)
 */
public void getPropVal() {
	

	try {
		getCatalogField().setText(getBaseProp1().getPropVal(getBaseProp1().getCatalog()));
		getSchemaField().setText(getBaseProp1().getPropVal(getBaseProp1().getSchema()));		
		getSQLSepField().setText(getBaseProp1().getPropVal(getBaseProp1().getSQLSeparator()));

		
		getBasenameField().setText(getBaseProp1().getPropVal(getBaseProp1().getBasename()));
		getMapfile().setText(getBaseProp1().getPropVal(getBaseProp1().getMapFile()));
		getXmlfile().setText(getBaseProp1().getPropVal(getBaseProp1().getXMLFile()));
		
		getSchemaFile().setText(getBaseProp1().getPropVal(getBaseProp1().getSchemaFile()));
		getKGCField().setText(getBaseProp1().getPropVal(getBaseProp1().getKeyGeneratorClass()));
		getSelect().setText(getBaseProp1().getPropVal(getBaseProp1().getSelect()));


		//OrderColumns

		getJCheckBox1().setSelected(getBaseProp1().getYesNo(getBaseProp1().getPropVal(getBaseProp1().getOrderColumns())));

		//CommitMode
		/*
		if (getBaseProp1().getPropVal(getBaseProp1().getCommitMode()).equals(getBaseProp1().getAfterInsert()))
		{getJRadioButton8().setSelected(true);}
		*/


		if (getBaseProp1().getPropVal(getBaseProp1().getCommitMode()) != null)
		{if (getBaseProp1().getPropVal(getBaseProp1().getCommitMode()).equals(getBaseProp1().getAfterInsert())) 
		{getJRadioButton8().setSelected(true);}
		else
		if (getBaseProp1().getPropVal(getBaseProp1().getCommitMode()).equals(getBaseProp1().getAfterDocument())) 
		{getJRadioButton9().setSelected(true);}
		} 

		//Actions

//		System.out.println("Action = "+getBaseProp1().getPropVal(getBaseProp1().getAction()));

		if (getBaseProp1().getPropVal(getBaseProp1().getAction()).equals(XMLDBMSProps.CREATEMAPFROMXMLSCHEMA))
 		{getJRadioButton1().setSelected(true);}
		
		if (getBaseProp1().getPropVal(getBaseProp1().getAction()).equals(XMLDBMSProps.CREATEMAPFROMDTD))
		{getJRadioButton2().setSelected(true);
			choice_CreateMapFromDTD();}
		
		if (getBaseProp1().getPropVal(getBaseProp1().getAction()).equals(XMLDBMSProps.CREATEMAPFROMTABLE))
 		{getJRadioButton3().setSelected(true);}

 		if (getBaseProp1().getPropVal(getBaseProp1().getAction()).equals(XMLDBMSProps.CREATEMAPFROMTABLES))
 		{getJRadioButton3().setSelected(true);}

 		if (getBaseProp1().getPropVal(getBaseProp1().getAction()).equals(XMLDBMSProps.CREATEMAPFROMSELECT))
 		{getJRadioButton4().setSelected(true);}

 		if (getBaseProp1().getPropVal(getBaseProp1().getAction()).equals(XMLDBMSProps.STOREDOCUMENT))
 		{getJRadioButton5().setSelected(true);
	 		choice_StoreDocument();}

 		if (getBaseProp1().getPropVal(getBaseProp1().getAction()).equals(XMLDBMSProps.RETRIEVEDOCUMENTBYSQL))
 		{getJRadioButton6().setSelected(true);
	 		choice_RetrieveDocBySQL();}

 		if (getBaseProp1().getPropVal(getBaseProp1().getAction()).equals(XMLDBMSProps.RETRIEVEDOCUMENTBYSQL))
 		{getJRadioButton7().setSelected(true);
	 		choice_RetrieveDocByKey();}

 		if (getBaseProp1().getPropVal(getBaseProp1().getAction()).equals(XMLDBMSProps.RETRIEVEDOCUMENTBYSQL))
 		{getJRadioButton7().setSelected(true);}


 		  
/*
			
		getJRadioButton1().setSelected(getBaseProp1().getPropVal(getBaseProp1().getAction()).equals("createMapFromXMLSchema"));
		getJRadioButton2().setSelected(getBaseProp1().getPropVal(getBaseProp1().getAction()).equals("createMapFromDTD"));
		getJRadioButton3().setSelected(getBaseProp1().getPropVal(getBaseProp1().getAction()).equals("createMapFromTable"));
		getJRadioButton3().setSelected(getBaseProp1().getPropVal(getBaseProp1().getAction()).equals("createMapFromTables"));
		getJRadioButton4().setSelected(getBaseProp1().getPropVal(getBaseProp1().getAction()).equals("createMapFromSelect"));
		getJRadioButton5().setSelected(getBaseProp1().getPropVal(getBaseProp1().getAction()).equals("storeDocument"));
		getJRadioButton6().setSelected(getBaseProp1().getPropVal(getBaseProp1().getAction()).equals("retrieveDocumentBySQL"));
		getJRadioButton7().setSelected(getBaseProp1().getPropVal(getBaseProp1().getAction()).equals("retrieveDocumentByKey"));
		getJRadioButton7().setSelected(getBaseProp1().getPropVal(getBaseProp1().getAction()).equals("retrieveDocumentByKeys"));
*/		
		
		

		
	} catch (java.lang.Throwable e) {

		handleException(e);
	}
	
	
	
	}/**
 * Return the JTextField11221 property value.
 * @return javax.swing.JTextField
 */ 
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getSchemaField() {
	if (ivjSchemaField == null) {
		try {
			ivjSchemaField = new javax.swing.JTextField();
			ivjSchemaField.setName("SchemaField");
			ivjSchemaField.setBounds(106, 89, 258, 20);
			ivjSchemaField.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjSchemaField;
}
/**
 * Return the JTextArea11 property value.
 * @return javax.swing.JTextArea
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextArea getSelect() {
	if (ivjSelect == null) {
		try {
			ivjSelect = new javax.swing.JTextArea();
			ivjSelect.setName("Select");
			ivjSelect.setLineWrap(true);
			ivjSelect.setWrapStyleWord(true);
			ivjSelect.setBounds(0, 0, 160, 120);
			ivjSelect.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjSelect;
}
/**
 * Return the JTextField11222 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getSQLSepField() {
	if (ivjSQLSepField == null) {
		try {
			ivjSQLSepField = new javax.swing.JTextField();
			ivjSQLSepField.setName("SQLSepField");
			ivjSQLSepField.setBounds(106, 116, 258, 20);
			ivjSQLSepField.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjSQLSepField;
}
/**
 * Return the JTextField11211 property value.
 * @return javax.swing.JTextField
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextField getTableField() {
	if (ivjTableField == null) {
		try {
			ivjTableField = new javax.swing.JTextField();
			ivjTableField.setName("TableField");
			ivjTableField.setBounds(131, 29, 442, 20);
			ivjTableField.setEnabled(false);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjTableField;
}
/**
 * Return the Transfer1 property value.
 * @return de.tudarmstadt.ito.xmldbms.tools.Transfer
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private de.tudarmstadt.ito.xmldbms.tools.Transfer getTransfer1() {
	if (ivjTransfer1 == null) {
		try {
			ivjTransfer1 = new de.tudarmstadt.ito.xmldbms.tools.Transfer();
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjTransfer1;
}/**
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
}/**
 * Insert the method's description here.
 * Creation date: (18/02/01 19:05:42)
 */ 
public void setKeys() {

	try{
	
	int i =	getBaseProp1().getNumTokens(getTableField().getText(),",");
//	System.out.println("Number of tokens ="+i);
	if (i <= 1)
	{getBaseProp1().addMultipleVal(getBaseProp1().getKey(),getKeyField().getText());}
	if (i > 1) {	
	String x1 = "";
	StringTokenizer stk = new StringTokenizer(getKeyField().getText(),",");
//	System.out.println("Number of tokens ="+ st1.countTokens());
	while (stk.hasMoreTokens()) {
						x1 = stk.nextToken();
						getBaseProp1().addKeys(x1); }
				}
	} catch (Throwable e) {
		System.out.println(e);
		}
	}				/**
 * Insert the method's description here.
 * Creation date: (13/02/01 16:23:42)
 */ 
public void setProp() {
	try {

		if (getCatalogField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getCatalog(), getCatalogField().getText());
		}
		
		if (getSchemaField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getSchema(), getSchemaField().getText());
		}
		
		if (getSQLSepField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getSQLSeparator(), getSQLSepField().getText());
		}
		if (getBasenameField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getBasename(), getBasenameField().getText());
		}
		if (getMapfile().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getMapFile(), getMapfile().getText());
		}
		if (getXmlfile().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getXMLFile(), getXmlfile().getText());
		}
		
		if (getSchemaFile().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getSchemaFile(), getSchemaFile().getText());
		}
		if (getKGCField().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getKeyGeneratorClass(), getKGCField().getText());
		}

		if (getSelect().getText().length() > 0) {
		getBaseProp1().put(getBaseProp1().getSelect(), getSelect().getText());				
		}
		//Order Columns CheckBox
		getBaseProp1().put(getBaseProp1().getOrderColumns(), getBaseProp1().bool2String(getJCheckBox1().isSelected()));		

		//Commit Mode Buttons
		if (getJRadioButton8().isSelected() ) {
		getBaseProp1().put(getBaseProp1().getCommitMode(), getBaseProp1().getAfterInsert());				
		}
		if (getJRadioButton9().isSelected() ) {
		getBaseProp1().put(getBaseProp1().getCommitMode(), getBaseProp1().getAfterDocument());				
		}

		//MultipleVal Fields
		if (getTableField().getText().length() > 0) {
			getBaseProp1().addMultipleVal(getBaseProp1().getTable(),getTableField().getText());
		}
		if (getPrefixField().getText().length() > 0) {
			getBaseProp1().addMultipleVal(getBaseProp1().getPrefix(),getPrefixField().getText());
		}
		if (getNSField().getText().length() > 0) {
			getBaseProp1().addMultipleVal(getBaseProp1().getNameSpaceURI(),getNSField().getText());
		}
		if (getKeyField().getText().length() > 0) {
			setKeys(); }
		
		//Actions
		if (getJRadioButton1().isSelected() ) {
		getBaseProp1().put(getBaseProp1().getAction(), XMLDBMSProps.CREATEMAPFROMXMLSCHEMA);				
		}
		if (getJRadioButton2().isSelected() ) {
		getBaseProp1().put(getBaseProp1().getAction(), XMLDBMSProps.CREATEMAPFROMDTD);
	
		}
		
		if (getJRadioButton3().isSelected() ) {
			if (getBaseProp1().getNumTokens(getTableField().getText(),",") <= 1){
		getBaseProp1().put(getBaseProp1().getAction(), XMLDBMSProps.CREATEMAPFROMTABLE);}
			if (getBaseProp1().getNumTokens(getTableField().getText(),",") > 1){
		getBaseProp1().put(getBaseProp1().getAction(), XMLDBMSProps.CREATEMAPFROMTABLES);}	
		}
			
		if (getJRadioButton4().isSelected() ) {
		getBaseProp1().put(getBaseProp1().getAction(), XMLDBMSProps.CREATEMAPFROMSELECT);				
		}		
		if (getJRadioButton5().isSelected() ) {
		getBaseProp1().put(getBaseProp1().getAction(), XMLDBMSProps.STOREDOCUMENT);				
		}
		if (getJRadioButton6().isSelected() ) {
		getBaseProp1().put(getBaseProp1().getAction(), XMLDBMSProps.RETRIEVEDOCUMENTBYSQL);				
		}

		if (getJRadioButton7().isSelected() ) {
			if (getBaseProp1().getNumTokens(getKeyField().getText(),",") <= 1){
		getBaseProp1().put(getBaseProp1().getAction(), XMLDBMSProps.RETRIEVEDOCUMENTBYKEY);}
			if (getBaseProp1().getNumTokens(getKeyField().getText(),",") > 1){
		getBaseProp1().put(getBaseProp1().getAction(), XMLDBMSProps.RETRIEVEDOCUMENTBYKEYS);}	
		}
		

	} catch (java.lang.Throwable e) {

		handleException(e);
	}
	
	
	
	
	
	};
}