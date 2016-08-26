package de.tudarmstadt.ito.xmldbms.gui.utils;

/**
 * This type was created in VisualAge.
 */

import java.awt.event.*;
import java.awt.*;

import java.util.*;

public class WindowPositioner {
	static Component applicationWindow;
	static Component currentWindow;
	static Point lastBounds;
	static WindowAdapter winListener;
	static Vector openWindows;

	public static final int CENTRE = 0;
	public static final int CASCADE = 1;

/**
 * WindowPositioner constructor comment.
 */
public WindowPositioner() {
	super();
}
public static synchronized void addApplicationWindow(Window w) {
	if (winListener != null)
		w.addWindowListener( winListener );
}
public static void cascadeWindow(Component c) {

	int x = lastBounds.x + 20;
	int y = lastBounds.y + 20;
	c.setLocation(x, y);
}
public static void cascadeWindow(Component c, Component on) {

	int x = on.getLocation().x + 20;
	int y = on.getLocation().y + 20;
	c.setLocation(x, y);
}
public static synchronized void currentApplicationWindowToFront() {
	getCurrentApplicationWindow().toFront();
}
public static void disposeHourglass() {
	setComponentCursor( getApplicationWindow(), Cursor.getDefaultCursor()  );
}
public static Rectangle getApplicationBounds() {
	return applicationWindow.getBounds();
}
public static Frame getApplicationWindow() {
	return (Frame)applicationWindow;
}
public static synchronized Window getCurrentApplicationWindow() {
	return (Window)openWindows.elementAt( openWindows.size()-1 );
}
public static void positionWindow(Component c) {
	Rectangle b = getApplicationBounds();
	lastBounds = c.getLocation();
	int x,y;

	
	if (b != null) {
		x = (b.x + (b.width/2)) - (c.getBounds().width)/2;
		y = (b.y + (b.height/2)) - (c.getBounds().height)/2;
	} else {
 		x = 0;
 		y = 0;
	}
		
	c.setLocation(x, y);
}
public static void positionWindowOnScreen(Component c) {
	lastBounds = c.getLocation();
	
	Dimension b = c.getToolkit().getScreenSize();
	int x = (b.width/2) - (c.getBounds().width/2);
	int y = (b.height/2) - (c.getBounds().height/2);

	//System.out.println(b.width+" "+b.height);

	if (x<0 || y<0) {
		x = y = 0;
	}
	c.setLocation(x, y);
}
public static void setApplicationWindow(Component w) {
	applicationWindow = w;
	lastBounds = w.getLocation();

	openWindows = new Vector();

	winListener = new WindowAdapter() {
		public void windowOpened(WindowEvent e) {
			openWindows.addElement( e.getSource() );
			//System.out.println("Current "+getCurrentApplicationWindow()+" "+openWindows.size());
		}
		public void windowClosed(WindowEvent e) {
			openWindows.removeElement( e.getSource() );
			//System.out.println("Current "+getCurrentApplicationWindow()+" "+openWindows.size());
			getCurrentApplicationWindow().toFront();
		}
	};
	((Frame)w).addWindowListener( winListener );
}
public static void setComponentCursor(Container c, Cursor pointer) {
	Component[] contents = c.getComponents();

	if (contents == null)
		return;

	c.setCursor( pointer );

	for (int i=0; i<contents.length; i++) {

		if (contents[i] instanceof Container)
			setComponentCursor( (Container)contents[i], pointer );
			
		contents[i].setCursor( pointer );
	}
		
}
public static void showHourglass() {
	setComponentCursor( getApplicationWindow(), Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR )  );
}
}
