// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.schemas.dtd;

import java.util.Vector;

/**
 * A content particle that is either a choice group or a sequence group.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class Group extends Particle
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   /**
	* The members of the group. Must contain Particles (either Groups
	* or References).
	*/
   public Vector members = new Vector(); // Contains Particles

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new Group. */
   public Group()
   {
   }   
}