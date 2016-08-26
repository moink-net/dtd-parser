package de.tudarmstadt.ito.xmldbms.gui;

/**
 * Insert the type's description here.
 * Creation date: (01/11/00 13:57:06)
 * @author: Adam
 */

import de.tudarmstadt.ito.xmldbms.tools.*;
 
public class Admingui extends javax.swing.JFrame {
	private javax.swing.JPanel ivjJFrameContentPane = null;
	private javax.swing.JScrollPane ivjJScrollPane1 = null;
	private javax.swing.JTabbedPane ivjJTabbedPane1 = null;
	private javax.swing.JPanel ivjPage = null;
	private BasicPanel ivjBasicPanel1 = null;
	private javax.swing.JPanel ivjJPanel1 = null;
	private BasicPanel ivjBasicPanelFactory = null;
	private ActionPanel ivjActionPanelFactory = null;
	private ActionPanel ivjActionPanel1 = null;

class IvjEventHandler implements javax.swing.event.ChangeListener {
		public void stateChanged(javax.swing.event.ChangeEvent e) {
			if (e.getSource() == Admingui.this.getJTabbedPane1()) 
				connEtoM7(e);
		};
	};
	IvjEventHandler ivjEventHandler = new IvjEventHandler();
/**
 * Admingui constructor comment.
 */
public Admingui() {
	super();
	initialize();
}
/**
 * Admingui constructor comment.
 * @param title java.lang.String
 */
public Admingui(String title) {
	super(title);
}
/**
 * connEtoM1:  (Admingui.initialize() --> BasicPanelFactory.BasicPanel())
 * @return de.tudarmstadt.ito.xmldbms.gui.BasicPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private BasicPanel connEtoM1() {
	de.tudarmstadt.ito.xmldbms.gui.BasicPanel connEtoM1Result = null;
	try {
		// user code begin {1}
		// user code end
		connEtoM1Result = new de.tudarmstadt.ito.xmldbms.gui.BasicPanel();
		setBasicPanelFactory(connEtoM1Result);
		connEtoM2(connEtoM1Result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
	return connEtoM1Result;
}
/**
 * connEtoM2:  ( (Admingui,initialize() --> BasicPanelFactory,BasicPanel()).normalResult --> BasicPanel1.this)
 * @param result de.tudarmstadt.ito.xmldbms.gui.BasicPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM2(BasicPanel result) {
	try {
		// user code begin {1}
		// user code end
		setBasicPanel1(result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM3:  (Admingui.initialize() --> Page.add(Ljava.awt.Component;)Ljava.awt.Component;)
 * @return java.awt.Component
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Component connEtoM3() {
	java.awt.Component connEtoM3Result = null;
	try {
		// user code begin {1}
		// user code end
		if ((getBasicPanel1() != null)) {
			connEtoM3Result = getPage().add(getBasicPanel1());
		}
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
 * connEtoM4:  (Admingui.initialize() --> ActionPanelFactory.ActionPanel())
 * @return de.tudarmstadt.ito.xmldbms.gui.ActionPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private ActionPanel connEtoM4() {
	de.tudarmstadt.ito.xmldbms.gui.ActionPanel connEtoM4Result = null;
	try {
		// user code begin {1}
		// user code end
		connEtoM4Result = new de.tudarmstadt.ito.xmldbms.gui.ActionPanel();
		setActionPanelFactory(connEtoM4Result);
		connEtoM5(connEtoM4Result);
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
 * connEtoM5:  ( (Admingui,initialize() --> ActionPanelFactory,ActionPanel()).normalResult --> ActionPanelObject.this)
 * @param result de.tudarmstadt.ito.xmldbms.gui.ActionPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM5(ActionPanel result) {
	try {
		// user code begin {1}
		// user code end
		setActionPanel1(result);
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connEtoM6:  (Admingui.initialize() --> JPanel1.add(Ljava.awt.Component;)Ljava.awt.Component;)
 * @return java.awt.Component
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private java.awt.Component connEtoM6() {
	java.awt.Component connEtoM6Result = null;
	try {
		// user code begin {1}
		// user code end
		if ((getActionPanel1() != null)) {
			connEtoM6Result = getJPanel1().add(getActionPanel1());
		}
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
	return connEtoM6Result;
}
/**
 * connEtoM7:  (JTabbedPane1.change.stateChanged(javax.swing.event.ChangeEvent) --> Admingui.refresh()V)
 * @param arg1 javax.swing.event.ChangeEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoM7(javax.swing.event.ChangeEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.refresh();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * Return the TransferObject property value.
 * @return de.tudarmstadt.ito.xmldbms.gui.TransferPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private ActionPanel getActionPanel1() {
	// user code begin {1}
	// user code end
	return ivjActionPanel1;
}
/**
 * Return the TransferFactory property value.
 * @return de.tudarmstadt.ito.xmldbms.gui.TransferPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private ActionPanel getActionPanelFactory() {
	// user code begin {1}
	// user code end
	return ivjActionPanelFactory;
}
/**
 * Return the BasicPanel1 property value.
 * @return de.tudarmstadt.ito.xmldbms.gui.BasicPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private BasicPanel getBasicPanel1() {
	// user code begin {1}
	// user code end
	return ivjBasicPanel1;
}
/**
 * Return the BasicPanelFactory property value.
 * @return de.tudarmstadt.ito.xmldbms.gui.BasicPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private BasicPanel getBasicPanelFactory() {
	// user code begin {1}
	// user code end
	return ivjBasicPanelFactory;
}
/**
 * Return the JFrameContentPane property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJFrameContentPane() {
	if (ivjJFrameContentPane == null) {
		try {
			ivjJFrameContentPane = new javax.swing.JPanel();
			ivjJFrameContentPane.setName("JFrameContentPane");
			ivjJFrameContentPane.setLayout(new java.awt.BorderLayout());
			ivjJFrameContentPane.setMinimumSize(new java.awt.Dimension(45, 134));
			getJFrameContentPane().add(getJScrollPane1(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJFrameContentPane;
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
 * Return the JScrollPane1 property value.
 * @return javax.swing.JScrollPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JScrollPane getJScrollPane1() {
	if (ivjJScrollPane1 == null) {
		try {
			ivjJScrollPane1 = new javax.swing.JScrollPane();
			ivjJScrollPane1.setName("JScrollPane1");
			ivjJScrollPane1.setPreferredSize(new java.awt.Dimension(610, 500));
			ivjJScrollPane1.setMinimumSize(new java.awt.Dimension(45, 134));
			getJScrollPane1().setViewportView(getJTabbedPane1());
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
 * Return the JTabbedPane1 property value.
 * @return javax.swing.JTabbedPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTabbedPane getJTabbedPane1() {
	if (ivjJTabbedPane1 == null) {
		try {
			ivjJTabbedPane1 = new javax.swing.JTabbedPane();
			ivjJTabbedPane1.setName("JTabbedPane1");
			ivjJTabbedPane1.setPreferredSize(new java.awt.Dimension(610, 500));
			ivjJTabbedPane1.setBounds(0, 0, 886, 593);
			ivjJTabbedPane1.insertTab("Basic Information", null, getPage(), null, 0);
			ivjJTabbedPane1.insertTab("Actions", null, getJPanel1(), null, 1);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJTabbedPane1;
}
/**
 * Return the Page property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getPage() {
	if (ivjPage == null) {
		try {
			ivjPage = new javax.swing.JPanel();
			ivjPage.setName("Page");
			ivjPage.setPreferredSize(new java.awt.Dimension(600, 447));
			ivjPage.setLayout(null);
			ivjPage.setMinimumSize(new java.awt.Dimension(600, 447));
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjPage;
}
/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	// System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	// exception.printStackTrace(System.out);
}
/**
 * Initializes connections
 * @exception java.lang.Exception The exception description.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() throws java.lang.Exception {
	// user code begin {1}
	// user code end
	getJTabbedPane1().addChangeListener(ivjEventHandler);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("Admingui");
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setSize(615, 510);
		setTitle("XML-DBMS Admin");
		setContentPane(getJFrameContentPane());
		initConnections();
		connEtoM1();
		connEtoM3();
		connEtoM4();
		connEtoM6();
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
		Admingui aAdmingui;
		aAdmingui = new Admingui();
		aAdmingui.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		aAdmingui.show();
		java.awt.Insets insets = aAdmingui.getInsets();
		aAdmingui.setSize(aAdmingui.getWidth() + insets.left + insets.right, aAdmingui.getHeight() + insets.top + insets.bottom);
		aAdmingui.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JFrame");
		exception.printStackTrace(System.out);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (18/02/01 16:55:01)
 */
public void refresh() {
	getBasicPanel1().getBaseProp1().setChange();
	getActionPanel1().getBaseProp1().setChange();
	
	}
/**
 * Set the TransferObject to a new value.
 * @param newValue de.tudarmstadt.ito.xmldbms.gui.TransferPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void setActionPanel1(ActionPanel newValue) {
	if (ivjActionPanel1 != newValue) {
		try {
			ivjActionPanel1 = newValue;
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	// user code begin {3}
	// user code end
}
/**
 * Set the TransferFactory to a new value.
 * @param newValue de.tudarmstadt.ito.xmldbms.gui.TransferPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void setActionPanelFactory(ActionPanel newValue) {
	if (ivjActionPanelFactory != newValue) {
		try {
			ivjActionPanelFactory = newValue;
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	// user code begin {3}
	// user code end
}
/**
 * Set the BasicPanel1 to a new value.
 * @param newValue de.tudarmstadt.ito.xmldbms.gui.BasicPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void setBasicPanel1(BasicPanel newValue) {
	if (ivjBasicPanel1 != newValue) {
		try {
			ivjBasicPanel1 = newValue;
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	// user code begin {3}
	// user code end
}
/**
 * Set the BasicPanelFactory to a new value.
 * @param newValue de.tudarmstadt.ito.xmldbms.gui.BasicPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void setBasicPanelFactory(BasicPanel newValue) {
	if (ivjBasicPanelFactory != newValue) {
		try {
			ivjBasicPanelFactory = newValue;
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	// user code begin {3}
	// user code end
}
}
