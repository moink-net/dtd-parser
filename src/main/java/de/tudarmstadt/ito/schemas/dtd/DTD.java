// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Change declaration of byte array constants
// * Change getQuote to use byte constants instead of char values
// Changes from version 1.01: None

package de.tudarmstadt.ito.schemas.dtd;

import de.tudarmstadt.ito.utils.NSName;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * Class representing a DTD.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class DTD
{
   // ********************************************************************
   // Private variables
   // ********************************************************************

   private Hashtable           addedElementTypes = new Hashtable();
   private static final Object obj = new Object();
   private OutputStream        out = null;
   private boolean             pretty = false;
   private int                 indent = 0, increment = 3;

   // ********************************************************************
   // Constants
   // ********************************************************************

   // 5/24/00 Phil Friedman, Ronald Bourret
   // 1) Initialize the following byte array constants from Strings, not char arrays,
   //    which require an explicit cast to byte.
   // 2) Add byte constants for single and double quotes.

   private static String CHOICESEPARATORStr = " | ",
							   COMMENTENDStr      = " -->",
							   COMMENTSTARTStr    = "<!-- ",
							   DECLENDStr         = ">",
							   DECLSTARTStr       = "<!",
							   GROUPENDStr        = ")",
							   GROUPSTARTStr      = "(",
							   ONEORMOREStr       = "+",
							   OPTIONALStr        = "?",
							   POUNDStr           = "#",
							   SEQSEPARATORStr    = ", ",
							   SPACEStr           = " ",
							   ZEROORMOREStr      = "*";
   private static final byte[] CHOICESEPARATOR = CHOICESEPARATORStr.getBytes(),
							   COMMENTEND      = COMMENTENDStr.getBytes(),
							   COMMENTSTART    = COMMENTSTARTStr.getBytes(),
							   DECLEND         = DECLENDStr.getBytes(),
							   DECLSTART       = DECLSTARTStr.getBytes(),
							   GROUPEND        = GROUPENDStr.getBytes(),
							   GROUPSTART      = GROUPSTARTStr.getBytes(),
							   ONEORMORE       = ONEORMOREStr.getBytes(),
							   OPTIONAL        = OPTIONALStr.getBytes(),
							   POUND           = POUNDStr.getBytes(),
							   RETURN          = System.getProperty("line.separator").getBytes(),
							   SEQSEPARATOR    = SEQSEPARATORStr.getBytes(),
							   SPACE           = SPACEStr.getBytes(),
							   ZEROORMORE      = ZEROORMOREStr.getBytes();
   private static final byte DOUBLEQUOTE = (byte)'"',
							 SINGLEQUOTE = (byte)'\'';

   // ********************************************************************
   // Public variables
   // ********************************************************************

   /**
	* A Hashtable of ElementTypes defined in the DTD, keyed by the qualified
	* element type name (ElementType.name.qualified).
	*/
   public Hashtable elementTypes = new Hashtable();

   /**
	* A Hashtable of Notations defined in the DTD, keyed by the notation name.
	*/
   public Hashtable notations = new Hashtable();

   /**
	* A Hashtable of ParsedGeneralEntities defined in the DTD, keyed by the
	* entity name.
	*/
   public Hashtable parsedGeneralEntities = new Hashtable();

   /**
	* A Hashtable of ParameterEntities defined in the DTD, keyed by the
	* entity name.
	*/
   public Hashtable parameterEntities = new Hashtable();

   /**
	* A Hashtable of UnparsedEntities defined in the DTD, keyed by the
	* entity name.
	*/
   public Hashtable unparsedEntities = new Hashtable();

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new DTD. */
   public DTD()
   {
   }   

   // ********************************************************************
   // Public Methods -- adding element types
   // ********************************************************************

   /**
	* Add a new ElementType. This method checks that the element type has
	* not been added before.
	*
	* @param name The NSName of the element type.
	* @exception DTDException Thrown if addElementType has already been called
	* for the element type.
	*/
   public ElementType addElementType(NSName name)
	  throws DTDException
   {
	  // Check that the qualified name is unique. If so, add it to the DTD.
	  // Note that it might already exist, having been added in a Reference.

	  if (elementTypeDefined(name))
		 throw new DTDException("Duplicate element type declaration: " + name.qualified);
	  addedElementTypes.put(name.qualified, obj);
	  return getElementType(name);
   }   

   /**
	* Get an ElementType. If the ElementType does not exist, a new ElementType
	* is created. Note that addElementType can still be called for ElementTypes
	* created in this manner.
	*
	* @param name The NSName of the element type.
	*/
   public ElementType getElementType(NSName name)
   {
	  // Get an existing ElementType or add a new one if it doesn't exist.
	  //
	  // This method exists because we frequently need to refer to an 
	  // ElementType object before it is formally created. For example, if
	  // element type A is defined before element type B, and the content model
	  // of element type A contains element type B, we need to add the
	  // ElementType object for B (as a reference in the content model of A)
	  // before it is formally defined and created.

	  ElementType elementType;

	  elementType = (ElementType)elementTypes.get(name.qualified);
	  if (elementType == null)
	  {
		 elementType = new ElementType(name);
		 elementTypes.put(name.qualified, elementType);
	  }

	  return elementType;
   }   

   /**
	* Same as addElementType(NSName) except that the local name, prefix, and
	* namespace URI are specified.
	*
	* @param local The local name of the element type.
	* @param prefix The namespace prefix. May be null.
	* @param uri The namespace URI. May be null.
	* @exception DTDException Thrown if addElementType has already been called
	* for the element type.
	*/
   public ElementType addElementType(String local, String prefix, String uri)
	  throws DTDException
   {
	  return addElementType(new NSName(local, prefix, uri));
   }   

   /**
	* Same as getElementType(NSName) except that the local name, prefix, and
	* namespace URI are specified.
	*
	* @param local The local name of the element type.
	* @param prefix The namespace prefix. May be null.
	* @param uri The namespace URI. May be null.
	*/
   public ElementType getElementType(String local, String prefix, String uri)
   {
	  return getElementType(new NSName(local, prefix, uri));
   }   

   // ********************************************************************
   // Public Methods -- post-construction
   // ********************************************************************

   /**
	* For element types with a content model of ANY, constructs the parent
	* and children Hashtables in the ElementType objects in the DTD. Note that
	* operation cannot successfully be performed until after all ElementTypes
	* have been added to the DTD.
	*/
   public void updateANYParents()
   {
	  // A common problem when building a DTD object is that element types
	  // with a content model of ANY do not correctly list parents and children.
	  // This method traverses the list of ElementTypes and, for each element
	  // type with a content model of ANY, adds all other types as children and
	  // this type as a parent.

	  Enumeration parents, children;
	  ElementType parent, child;

	  parents = elementTypes.elements();
	  while (parents.hasMoreElements())
	  {
		 parent = (ElementType)parents.nextElement();
		 if (parent.contentType == ElementType.CONTENT_ANY)
		 {
			children = elementTypes.elements();
			while (children.hasMoreElements())
			{
			   // I think this is the code equivalent of "Who's on first?"

			   child = (ElementType)children.nextElement();
			   parent.children.put(child.name.qualified, child);
			   child.parents.put(parent.name.qualified, parent);
			}
		 }
	  }
   }   

   /**
	* Checks that all element types referred to in the content models of
	* ElementTypes have been added to the DTD through addElementType.
	*
	* @exception DTDException Thrown if an element type has been referred
	*  to but not added.
	*/
   public void checkElementTypeReferences()
	  throws DTDException
   {
	  // Make sure that all referenced element types are defined.

	  Enumeration parents, children;
	  ElementType parent, child;

	  parents = elementTypes.elements();
	  while (parents.hasMoreElements())
	  {
		 parent = (ElementType)parents.nextElement();
		 if (!parent.children.isEmpty())
		 {
			children = parent.children.elements();
			while (children.hasMoreElements())
			{
			   child = (ElementType)children.nextElement();
			   if (!elementTypeDefined(child.name))
				  throw new DTDException("Element type " + child.name.qualified + " is referenced in element type " + parent.name.qualified + " but is never defined.");
			}
		 }
	  }
   }   

   /**
	* Checks that all notations referred to Attributes have been defined.
	*
	* @exception DTDException Thrown if notation has been referred
	*  to but not defined.
	*/
   public void checkNotationReferences()
	  throws DTDException
   {
	  Enumeration    e1, e2;
	  ElementType    elementType;
	  Attribute      attribute;
	  String         notation;
	  UnparsedEntity entity;

	  e1 = elementTypes.elements();
	  while (e1.hasMoreElements())
	  {
		 elementType = (ElementType)e1.nextElement();
		 e2 = elementType.attributes.elements();
		 while (e2.hasMoreElements())
		 {
			attribute = (Attribute)e2.nextElement();
			if (attribute.type == Attribute.TYPE_NOTATION)
			{
			   for (int i = 0; i < attribute.enums.size(); i++)
			   {
				  notation = (String)attribute.enums.elementAt(i);
				  if (!notations.containsKey(notation))
					 throw new DTDException("Notation " + notation + " not defined. Used by the " + attribute.name.qualified + " attribute of the " + elementType.name.qualified + " element type.");
			   }
			}
		 }
	  }

	  e1 = unparsedEntities.elements();
	  while (e1.hasMoreElements())
	  {
		 entity = (UnparsedEntity)e1.nextElement();
		 if (!notations.containsKey(entity.notation))
			throw new DTDException("Notation " + entity.notation + " not defined. Used by the " + entity.name + " unparsed entity.");
	  }
   }   

   /**
	* Checks if an element type has been defined -- that is, added with
	* addElementType.
	*
	* @param name The NSName of the element type.
	*/
   public boolean elementTypeDefined(NSName name)
   {
	  return addedElementTypes.containsKey(name.qualified);
   }   

   // ********************************************************************
   // Public Methods -- serialization
   // ********************************************************************

   /**
	* Serialize a DTD to an OutputStream.
	*
	* @param out The OutputStream.
	* @param pretty Whether to pretty-print the DTD.
	*/
   public void serialize(OutputStream out, boolean pretty)
	  throws IOException
   {
	  this.out = out;
	  this.pretty = pretty;

	  outputEntities();
	  outputElementTypes();
	  outputNotations();
   }   

   // ********************************************************************
   // Private methods -- serialization
   // ********************************************************************

   private void outputElementTypes()
	  throws IOException
   {
	  Enumeration e = elementTypes.elements();
	  while (e.hasMoreElements())
	  {
		 outputElementType((ElementType)e.nextElement());
	  }
   }   

   private void outputElementType(ElementType elementType)
	  throws IOException
   {
	  ElementType child;

	  if (pretty)
	  {
		 out.write(RETURN);
		 out.write(RETURN);
	  }

	  out.write(DECLSTART);
	  out.write(DTDConst.KEYWD_ELEMENT.getBytes());
	  out.write(SPACE);
	  out.write(elementType.name.prefixed.getBytes());
	  out.write(SPACE);

	  switch (elementType.contentType)
	  {
		 case ElementType.CONTENT_EMPTY:
			out.write(DTDConst.KEYWD_EMPTY.getBytes());
			break;

		 case ElementType.CONTENT_ANY:
			out.write(DTDConst.KEYWD_ANY.getBytes());
			break;

		 case ElementType.CONTENT_PCDATA:
			out.write(GROUPSTART);
			out.write(POUND);
			out.write(DTDConst.KEYWD_PCDATA.getBytes());
			out.write(GROUPEND);
			break;

		 case ElementType.CONTENT_MIXED:
			out.write(GROUPSTART);
			out.write(POUND);
			out.write(DTDConst.KEYWD_PCDATA.getBytes());

			// Output the children -- assume there is at least one...

			Enumeration e = elementType.children.elements();
			while (e.hasMoreElements())
			{
			   out.write(CHOICESEPARATOR);
			   child = (ElementType)e.nextElement();
			   out.write(child.name.prefixed.getBytes());
			}

			out.write(GROUPEND);
			out.write(ZEROORMORE);
			break;

		 case ElementType.CONTENT_ELEMENT:
			if (elementType.content.type == Particle.PARTICLE_ELEMENTTYPEREF)
			{
			   // If the content model is a single element type reference,
			   // we need to output the surrounding parentheses ourselves.
			   // Otherwise, these are taken care of by outputGroup().

			   out.write(GROUPSTART);
			   outputParticle(elementType.content);
			   out.write(GROUPEND);
			}
			else
			{
			   outputParticle(elementType.content);
			}
			break;
	  }
	  out.write(DECLEND);

	  outputAttributes(elementType);
   }   

   private void outputParticle(Particle particle)
	  throws IOException
   {
	  if (particle.type == Particle.PARTICLE_ELEMENTTYPEREF)
	  {
		 outputRef((Reference)particle);
	  }
	  else // if (type == Particle.PARTICLE_CHOICE || PARTICLE_SEQUENCE)
	  {
		 outputGroup((Group)particle);
	  }
   }   

   private void outputRef(Reference ref)
	  throws IOException
   {
	  out.write(ref.elementType.name.prefixed.getBytes());
	  outputFrequency(ref);
   }   

   private void outputGroup(Group group)
	  throws IOException
   {
	  byte[] separator;

	  if (group.type == Particle.PARTICLE_CHOICE)
	  {
		 separator = CHOICESEPARATOR;
	  }
	  else // if (group.type == Particle.PARTICLE_CHOICE)
	  {
		 separator = SEQSEPARATOR;
	  }

	  out.write(GROUPSTART);
	  outputParticle((Particle)group.members.elementAt(0));
	  for (int i = 1; i < group.members.size(); i++)
	  {
		 out.write(separator);
		 outputParticle((Particle)group.members.elementAt(i));
	  }
	  out.write(GROUPEND);
	  outputFrequency(group);
   }   

   private void outputFrequency(Particle particle)
	  throws IOException
   {
	  if (particle.isRequired)
	  {
		 if (particle.isRepeatable)
		 {
			out.write(ONEORMORE);
		 }
	  }
	  else if (particle.isRepeatable)
	  {
		 out.write(ZEROORMORE);
	  }
	  else
	  {
		 out.write(OPTIONAL);
	  }
   }   

   private void outputAttributes(ElementType elementType)
	  throws IOException
   {
	  if (elementType.attributes == null) return;
	  if (elementType.attributes.size() == 0) return;

	  if (pretty)
	  {
		 out.write(RETURN);
	  }

	  out.write(DECLSTART);
	  out.write(DTDConst.KEYWD_ATTLIST.getBytes());
	  out.write(SPACE);
	  out.write(elementType.name.prefixed.getBytes());
	  out.write(SPACE);
	  Enumeration e = elementType.attributes.elements();
	  indent += 10;
	  while (e.hasMoreElements())
	  {
		 outputAttribute((Attribute)e.nextElement());
	  }
	  indent -= 10;
	  out.write(DECLEND);
   }   

   private void outputAttribute(Attribute attribute)
	  throws IOException
   {
	  if (pretty)
	  {
		 out.write(RETURN);
		 indent();
	  }

	  out.write(attribute.name.prefixed.getBytes());
	  out.write(SPACE);
	  outputAttributeType(attribute);
	  outputAttributeDefault(attribute);
   }   

   private void outputAttributeType(Attribute attribute)
	  throws IOException
   {
	  switch (attribute.type)
	  {
		 case Attribute.TYPE_CDATA:
			out.write(DTDConst.KEYWD_CDATA.getBytes());
			break;

		 case Attribute.TYPE_ID:
			out.write(DTDConst.KEYWD_ID.getBytes());
			break;

		 case Attribute.TYPE_IDREF:
			out.write(DTDConst.KEYWD_IDREF.getBytes());
			break;

		 case Attribute.TYPE_IDREFS:
			out.write(DTDConst.KEYWD_IDREFS.getBytes());
			break;

		 case Attribute.TYPE_ENTITY:
			out.write(DTDConst.KEYWD_ENTITY.getBytes());
			break;

		 case Attribute.TYPE_ENTITIES:
			out.write(DTDConst.KEYWD_ENTITIES.getBytes());
			break;

		 case Attribute.TYPE_NMTOKEN:
			out.write(DTDConst.KEYWD_NMTOKEN.getBytes());
			break;

		 case Attribute.TYPE_NMTOKENS:
			out.write(DTDConst.KEYWD_NMTOKENS.getBytes());
			break;

		 case Attribute.TYPE_NOTATION:
			outputEnumeration(attribute.enums, true);
			break;

		 case Attribute.TYPE_ENUMERATED:
			outputEnumeration(attribute.enums, false);
			break;
	  }
	  out.write(SPACE);
   }   

   private void outputEnumeration(Vector enum, boolean isNotation)
	  throws IOException
   {
	  if (isNotation)
	  {
		 out.write(DTDConst.KEYWD_NOTATION.getBytes());
		 out.write(SPACE);
	  }

	  out.write(GROUPSTART);
	  out.write(((String)enum.elementAt(0)).getBytes());
	  for (int i = 1; i < enum.size(); i++)
	  {
		 out.write(CHOICESEPARATOR);
		 out.write(((String)enum.elementAt(i)).getBytes());
	  }
	  out.write(GROUPEND);
   }   

   private void outputAttributeDefault(Attribute attribute)
	  throws IOException
   {
	  switch (attribute.required)
	  {
		 case Attribute.REQUIRED_REQUIRED:
			out.write(POUND);
			out.write(DTDConst.KEYWD_REQUIRED.getBytes());
			break;

		 case Attribute.REQUIRED_OPTIONAL:
			out.write(POUND);
			out.write(DTDConst.KEYWD_IMPLIED.getBytes());
			break;

		 case Attribute.REQUIRED_FIXED:
			out.write(POUND);
			out.write(DTDConst.KEYWD_FIXED.getBytes());
			out.write(SPACE);

		 // WARNING! Above case falls through.
		 case Attribute.REQUIRED_DEFAULT:
			writeQuotedValue(attribute.defaultValue.getBytes());
			break;
	  }
   }   

   private void outputNotations()
	  throws IOException
   {
	  Enumeration e = notations.elements();
	  while (e.hasMoreElements())
	  {
		 outputNotation((Notation)e.nextElement());
	  }
   }   

   private void outputNotation(Notation notation)
	  throws IOException
   {
	  if (pretty)
	  {
		 out.write(RETURN);
		 out.write(RETURN);
	  }

	  out.write(DECLSTART);
	  out.write(DTDConst.KEYWD_NOTATION.getBytes());
	  out.write(SPACE);
	  out.write(notation.name.getBytes());
	  out.write(SPACE);
	  if (notation.publicID != null)
	  {
		 out.write(DTDConst.KEYWD_PUBLIC.getBytes());
		 out.write(SPACE);
		 writeQuotedValue(notation.publicID.getBytes());
	  }
	  else
	  {
		 // Assume that the object is constructed correctly and at least one
		 // of publicID and systemID is not null.
		 out.write(DTDConst.KEYWD_SYSTEM.getBytes());
	  }
	  out.write(SPACE);
	  if (notation.systemID != null)
	  {
		 writeQuotedValue(notation.systemID.getBytes());
		 out.write(SPACE);
	  }
	  out.write(DECLEND);
   }   

   private void outputEntities()
	  throws IOException
   {
	  Enumeration e;

	  e = unparsedEntities.elements();
	  while (e.hasMoreElements())
	  {
		 outputEntity((Entity)e.nextElement());
	  }

	  e = parsedGeneralEntities.elements();
	  while (e.hasMoreElements())
	  {
		 outputEntity((Entity)e.nextElement());
	  }

	  e = parameterEntities.elements();
	  while (e.hasMoreElements())
	  {
		 outputEntity((Entity)e.nextElement());
	  }
   }   

   private void outputEntity(Entity entity)
	  throws IOException
   {
	  if (pretty)
	  {
		 out.write(RETURN);
		 out.write(RETURN);
	  }

	  out.write(DECLSTART);
	  out.write(DTDConst.KEYWD_ENTITY.getBytes());
	  out.write(SPACE);
	  if (entity.type == Entity.TYPE_PARAMETER)
	  {
		 out.write('%');
		 out.write(SPACE);
	  }
	  out.write(entity.name.getBytes());
	  out.write(SPACE);
	  if (entity.systemID == null)
	  {
		 // Internal entity

		 if (entity.type == Entity.TYPE_PARAMETER)
		 {
			writeQuotedValue(((ParameterEntity)entity).value.getBytes());
		 }
		 else // if (e.type == Entity.TYPE_PARSEDGENERAL
		 {
			writeQuotedValue(((ParsedGeneralEntity)entity).value.getBytes());
		 }
	  }
	  else
	  {
		 // External entity

		 if (entity.publicID != null)
		 {
			out.write(DTDConst.KEYWD_PUBLIC.getBytes());
			out.write(SPACE);
			writeQuotedValue(entity.publicID.getBytes());
		 }
		 else
		 {
			out.write(DTDConst.KEYWD_SYSTEM.getBytes());
		 }
		 out.write(SPACE);
		 writeQuotedValue(entity.systemID.getBytes());
		 if (entity.type == Entity.TYPE_UNPARSED)
		 {
			out.write(SPACE);
			out.write(DTDConst.KEYWD_NDATA.getBytes());
			out.write(SPACE);
			out.write(((UnparsedEntity)entity).notation.getBytes());
		 }
	  }
	  out.write(SPACE);
	  out.write(DECLEND);
   }   

   void writeQuotedValue(byte[] value)
	  throws IOException
   {
	  byte quote;

	  quote = getQuote(value);
	  out.write(quote);
	  out.write(value);
	  out.write(quote);
   }   

   byte getQuote(byte[] value)
   {
	  // 5/24/00 Phil Friedman, Ronald Bourret
	  // Use byte constants instead of character values.

	  for (int i = 0; i < value.length; i++)
	  {
		 if (value[i] == DOUBLEQUOTE) return SINGLEQUOTE;
	  }
	  return DOUBLEQUOTE;
   }   

   private void indent()
	  throws IOException
   {
	  for (int i = 0; i < indent; i++)
	  {
		 out.write(' ');
	  }
   }   
}