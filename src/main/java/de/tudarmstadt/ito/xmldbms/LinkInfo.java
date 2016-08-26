// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;

// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

/**
 * Provides information needed to link two tables;
 *  <A HREF="../readme.html#NotForUse">not for general use</A>.
 *
 * <P>LinkInfo contains contains the information needed to link two tables:
 * the columns in each key, whether to generate the key, and which key appears
 * in which table. LinkInfo structures occur in RelatedClassMaps and
 * PropertyMaps to link the class table to the related class table or property
 * table. They also occur in RootClassMaps, where only the child key information
 * is used.</P>
 *
 * <P>&quot;Parent&quot; and &quot;child&quot; refer to the relationship between
 * the structures in the XML document, not the relationship between two tables
 * in the database. In a class / property relationship, such as between an
 * element type-as-class and an element type-as-property, PCDATA, or attribute,
 * the class is always the parent. In a class / class relationship, such as
 * that between two element types-as-classes and mapped by a ClassMap and its
 * subordinate RelatedClassMaps, the class referred to in the ClassMap is
 * always the parent and the class referred to in the RelatedClassMap is always
 * the child.</P>
 *
 * <P>Note that the parent / child relationship is independent of where the
 * candidate key is located. That is, it could be in the table of the parent
 * or the child. Which table it is in is stated by the parentKeyIsCandidate
 * member variable.</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class LinkInfo
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   /** Columns in the parent key. */
   public Column[] parentKey = null;

   /** Columns in the child key. */
   public Column[] childKey = null;

   /** Whether the candidate key is to be generated. */
   public boolean generateKey = false;

   /** Whether the parent key is the candidate key. */
   public boolean parentKeyIsCandidate = false;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   public LinkInfo()
   {
   }   
}