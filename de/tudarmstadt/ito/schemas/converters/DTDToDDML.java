// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Change declaration of byte array constants
// Changes from version 1.01: None

package de.tudarmstadt.ito.schemas.converters;

import de.tudarmstadt.ito.schemas.dtd.*;
import de.tudarmstadt.ito.utils.NSName;
import de.tudarmstadt.ito.utils.XMLOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Converts a DTD object to an OutputStream representing a DDML document.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class DTDToDDML extends XMLOutputStream
{
   //**************************************************************************
   // Constants
   //**************************************************************************

   // 5/24/00 Phil Friedman, Ronald Bourret
   // 1) Initialize the following byte array constants from Strings, not char arrays,
   //    which require an explicit cast to byte.
   // 2) Declare the byte constants to be final.

   static String XMLNSStr = "xmlns",
				 XMLNSDDMLStr = "xmlns:DDML",
				 DDMLNSStr = "http://www.purl.org/NET/ddml/v1",
				 DDMLDTDStr = "ddml.dtd";

   static final byte[] XMLNS = XMLNSStr.getBytes(),
					   XMLNSDDML = XMLNSDDMLStr.getBytes(),
					   DDMLNS = DDMLNSStr.getBytes(),
					   DDMLDTD = DDMLDTDStr.getBytes();

   //**************************************************************************
   // Variables
   //**************************************************************************

   private DTD dtd;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   public DTDToDDML()
   {
   }   

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
	* Convert a DTD object to an OutputStream representing a DDML document.
	*
	* @param dtd The DTD.
	* @param out The OutputStream.
	* @param pretty Whether to perform pretty-printing.
	* @param indent Number of space to indent each element from its parent
	*    when pretty-printing.
	*/
   public void convert(DTD dtd, OutputStream out, boolean pretty, int indent)
	  throws IOException
   {
	  this.dtd = dtd;
	  setOutputStream(out);
	  setPrettyPrinting(pretty, indent);
	  allocateAttrs(6);
	  writeDDMLStart();
	  writeElementTypes();
	  writeNotations();
	  writeUnparsedEntities();
	  writeDDMLEnd();
   }   

   //**************************************************************************
   // Private methods - serialize map
   //**************************************************************************

   private void writeAttribute(Attribute attribute)
	  throws IOException
   {
	  String prefix, uri;
	  int    count;

	  initAttrs();

	  attrs[0] = DDMLConst.ATTR_NAME.getBytes();
	  values[0] = attribute.name.local.getBytes();

	  attrs[1] = DDMLConst.ATTR_TYPE.getBytes();
	  values[1] = getType(attribute.type);

	  attrs[2] = DDMLConst.ATTR_REQUIRED.getBytes();
	  values[2] = getRequired(attribute.required);

	  count = 3;

	  prefix = NSName.getPrefix(attribute.name.prefixed);
	  if (prefix != null)
	  {
		 attrs[count] = DDMLConst.ATTR_PREFIX.getBytes();
		 values[count] = prefix.getBytes();
		 count++;
	  }

	  uri = NSName.getURI(attribute.name.qualified);
	  if (uri != null)
	  {
		 attrs[count] = DDMLConst.ATTR_NS.getBytes();
		 values[count] = uri.getBytes();
		 count++;
	  }

	  if (attribute.defaultValue != null)
	  {
		 // We assume here that the Attribute is constructed correctly and
		 // that default is non-null only if required is _FIXED or _DEFAULT.
		 attrs[count] = DDMLConst.ATTR_ATTVALUE.getBytes();
		 values[count] = attribute.defaultValue.getBytes();
	  }

	  if ((attribute.type == Attribute.TYPE_NOTATION) ||
		  (attribute.type == Attribute.TYPE_ENUMERATED))
	  {
		 writeElementStart(DDMLConst.ELEM_ATTDEF.getBytes(), attrs, values, false);
		 writeEnumeration(attribute.enums);
		 writeElementEnd(DDMLConst.ELEM_ATTDEF.getBytes());
	  }
	  else
	  {
		 writeElementStart(DDMLConst.ELEM_ATTDEF.getBytes(), attrs, values, true);
	  }
   }   

   private void writeAttributes(Hashtable attributes)
	  throws IOException
   {
	  Enumeration e;

	  if (attributes.isEmpty()) return;

	  writeElementStart(DDMLConst.ELEM_ATTGROUP.getBytes(), null, null, false);

	  e = attributes.elements();
	  while (e.hasMoreElements())
	  {
		 writeAttribute((Attribute)e.nextElement());
	  }

	  writeElementEnd(DDMLConst.ELEM_ATTGROUP.getBytes());
   }   

   private void writeChoiceOrSeq(Group group, boolean isRequired, boolean isRepeatable)
	  throws IOException
   {
	  byte[] name;

	  initAttrs();
	  attrs[0] = DDMLConst.ATTR_FREQUENCY.getBytes();
	  values[0] = getFrequency(isRequired, isRepeatable);

	  name = (group.type == Particle.PARTICLE_CHOICE) ?
			 DDMLConst.ELEM_CHOICE.getBytes() : DDMLConst.ELEM_SEQ.getBytes();
	  
	  writeElementStart(name, attrs, values, false);
	  writeGroup(group, group.type, true, false);
	  writeElementEnd(name);
   }   

   private void writeContentModel(int type, Group content)
	  throws IOException
   {
	  writeElementStart(DDMLConst.ELEM_MODEL.getBytes(), null, null, false);
	  switch (type)
	  {
		 case ElementType.CONTENT_EMPTY:
			writeElementStart(DDMLConst.ELEM_EMPTY.getBytes(), null, null, true);
			break;

		 case ElementType.CONTENT_ANY:
			writeElementStart(DDMLConst.ELEM_ANY.getBytes(), null, null, true);
			break;

		 case ElementType.CONTENT_PCDATA:
			writeElementStart(DDMLConst.ELEM_PCDATA.getBytes(), null, null, true);
			break;

		 case ElementType.CONTENT_MIXED:
			writeMixedContent(content);
			break;

		 case ElementType.CONTENT_ELEMENT:
			if (content.members.size() == 1)
			{
			   // We fake out writeGroup here and state that the "parent" 
			   // group type (there is no parent group) is ELEMENTTYPEREF.
			   // This avoids an intervening Model element accidentally
			   // being written.
			   writeGroup(content, Particle.PARTICLE_ELEMENTTYPEREF, true, false);
			}
			else
			{
			   writeChoiceOrSeq(content, content.isRequired, content.isRepeatable);
			}
			break;
	  }
	  writeElementEnd(DDMLConst.ELEM_MODEL.getBytes());
   }   

   private void writeDDMLEnd()
	  throws IOException
   {
	  writeElementEnd(DDMLConst.ELEM_DOCUMENTDEF.getBytes());
   }   

   private void writeDDMLStart()
	  throws IOException
   {
	  initAttrs();

	  writeXMLDecl(null);
	  writeDOCTYPE(DDMLConst.ELEM_DOCUMENTDEF.getBytes(), DDMLDTD, null);
	  attrs[0] = DDMLConst.ATTR_VERSION.getBytes();
	  attrs[1] = XMLNS;
	  attrs[2] = XMLNSDDML;
	  values[0] = DDMLConst.DEF_VERSION.getBytes();
	  values[1] = DDMLNS;
	  values[2] = DDMLNS;
	  writeElementStart(DDMLConst.ELEM_DOCUMENTDEF.getBytes(), attrs, values, false);
   }   

   private void writeElementType(ElementType elementType)
	  throws IOException
   {
	  String prefix, uri;
	  int    count = 1;

	  initAttrs();
	  attrs[0] = DDMLConst.ATTR_NAME.getBytes();
	  values[0] = elementType.name.local.getBytes();

	  prefix = NSName.getPrefix(elementType.name.prefixed);
	  if (prefix != null)
	  {
		 attrs[count] = DDMLConst.ATTR_PREFIX.getBytes();
		 values[count] = prefix.getBytes();
		 count++;
	  }

	  uri = NSName.getURI(elementType.name.qualified);
	  if (uri != null)
	  {
		 attrs[count] = DDMLConst.ATTR_NS.getBytes();
		 values[count] = uri.getBytes();
	  }

	  writeElementStart(DDMLConst.ELEM_ELEMENTDECL.getBytes(), attrs, values, false);
	  writeContentModel(elementType.contentType, elementType.content);
	  writeAttributes(elementType.attributes);
	  writeElementEnd(DDMLConst.ELEM_ELEMENTDECL.getBytes());
   }   

   private void writeElementTypes()
	  throws IOException
   {
	  Enumeration e;
	  ElementType elementType;

	  e = dtd.elementTypes.elements();
	  while (e.hasMoreElements())
	  {
		 elementType = (ElementType)e.nextElement();
		 writeElementType(elementType);
	  }
   }   

   private void writeEnumeration(Vector enums)
	  throws IOException
   {
	  writeElementStart(DDMLConst.ELEM_ENUMERATION.getBytes(), null, null, false);

	  initAttrs();
	  attrs[0] = DDMLConst.ATTR_VALUE.getBytes();
	  for (int i = 0; i < enums.size(); i++)
	  {
		 values[0] = ((String)enums.elementAt(i)).getBytes();
		 writeElementStart(DDMLConst.ELEM_ENUMERATIONVALUE.getBytes(), attrs, values, true);
	  }
	  writeElementEnd(DDMLConst.ELEM_ENUMERATION.getBytes());
   }   

   private void writeGroup(Group content, int parentType, boolean parentRequired, boolean parentRepeatable)
	  throws IOException
   {
	  Particle particle;
	  int      size;
	  boolean  isRequired, isRepeatable;

	  size = content.members.size();

	  for (int i = 0; i < size; i++)
	  {
		 particle = (Particle)content.members.elementAt(i);

		 // Figure out whether the child is required and repeatable,
		 // based on its parent's requiredness and repeatability. This
		 // comes into play only when a group is skipped (see below).
		 // Otherwise, this function is called with parentRequired = true
		 // and parentRepeatable = false so the child values are used.

		 isRequired = (parentRequired) ? particle.isRequired : false;
		 isRepeatable = (parentRepeatable) ? true : particle.isRepeatable;

		 if (particle.type == Particle.PARTICLE_ELEMENTTYPEREF)
		 {
			writeReference((Reference)particle, isRequired, isRepeatable);
		 }
		 else
		 {
			if (size == 1)
			{
			   // DDML requires a Choice or Seq to have at least two
			   // members. If the DTD Group only has one member, we
			   // simply skip it, remembering to pass along
			   // its requiredness and repeatability.

			   writeGroup(content, parentType, content.isRequired, content.isRepeatable);
			}
			else
			{
			   // If the group type is the same as its parent group,
			   // we need to write an intervening Model element, since
			   // DDML doesn't allow a Choice inside a choice or a Seq
			   // inside a Seq. Hacky, but it works.
   
			   if (particle.type == parentType)
			   {
				  writeElementStart(DDMLConst.ELEM_MODEL.getBytes(), null, null, false);
			   }
   
			   // Write the Choice or Sequence
   
			   writeChoiceOrSeq((Group)particle, isRequired, isRepeatable);
   
			   // Close the intervening Model, if any.
   
			   if (particle.type == parentType)
			   {
				  writeElementEnd(DDMLConst.ELEM_MODEL.getBytes());
			   }
			}
		 }
	  }
   }   

   private void writeMixedContent(Group content)
	  throws IOException
   {
	  initAttrs();
	  attrs[0] = DDMLConst.ATTR_FREQUENCY.getBytes();
	  values[0] = DDMLConst.ENUM_ZEROORMORE.getBytes();
	  writeElementStart(DDMLConst.ELEM_MIXED.getBytes(), attrs, values, false);

	  for (int i = 0; i < content.members.size(); i++)
	  {
		 writeReference((Reference)content.members.elementAt(i), true, false);
	  }

	  writeElementEnd(DDMLConst.ELEM_MIXED.getBytes());
   }   

   private void writeNotation(Notation notation)
	  throws IOException
   {
	  int count = 1;

	  initAttrs();
	  attrs[0] = DDMLConst.ATTR_NAME.getBytes();
	  values[0] = notation.name.getBytes();

	  if (notation.systemID != null)
	  {
		 attrs[count] = DDMLConst.ATTR_SYSTEMLITERAL.getBytes();
		 values[count] = notation.systemID.getBytes();
		 count++;
	  }
	  if (notation.publicID != null)
	  {
		 attrs[count] = DDMLConst.ATTR_PUBIDLITERAL.getBytes();
		 values[count] = notation.publicID.getBytes();
	  }
	  writeElementStart(DDMLConst.ELEM_NOTATION.getBytes(), attrs, values, true);
	  
   }   

   private void writeNotations()
	  throws IOException
   {
	  Enumeration e;
	  Notation    notation;

	  e = dtd.notations.elements();
	  while (e.hasMoreElements())
	  {
		 notation = (Notation)e.nextElement();
		 writeNotation(notation);
	  }
   }   

   private void writeReference(Reference ref, boolean isRequired, boolean isRepeatable)
	  throws IOException
   {
	  String uri;

	  initAttrs();

	  attrs[0] = DDMLConst.ATTR_ELEMENT.getBytes();
	  values[0] = ref.elementType.name.local.getBytes();

	  attrs[1] = DDMLConst.ATTR_FREQUENCY.getBytes();
	  values[1] = getFrequency(isRequired, isRepeatable);

	  uri = NSName.getURI(ref.elementType.name.qualified);
	  if (uri != null)
	  {
		 attrs[2] = DDMLConst.ATTR_ELEMENTNS.getBytes();
		 values[2] = uri.getBytes();
	  }

	  writeElementStart(DDMLConst.ELEM_REF.getBytes(), attrs, values, true);
   }   

   private void writeUnparsedEntities()
	  throws IOException
   {
	  Enumeration    e;
	  UnparsedEntity entity;

	  e = dtd.unparsedEntities.elements();
	  while (e.hasMoreElements())
	  {
		 entity = (UnparsedEntity)e.nextElement();
		 writeUnparsedEntity(entity);
	  }
   }   

   private void writeUnparsedEntity(UnparsedEntity entity)
	  throws IOException
   {
	  initAttrs();

	  attrs[0] = DDMLConst.ATTR_NAME.getBytes();
	  values[0] = entity.name.getBytes();
	  attrs[1] = DDMLConst.ATTR_SYSTEMLITERAL.getBytes();
	  values[1] = entity.systemID.getBytes();
	  attrs[2] = DDMLConst.ATTR_NOTATION.getBytes();
	  values[2] = entity.notation.getBytes();
	  if (entity.publicID != null)
	  {
		 attrs[3] = DDMLConst.ATTR_PUBIDLITERAL.getBytes();
		 values[3] = entity.publicID.getBytes();
	  }
	  writeElementStart(DDMLConst.ELEM_UNPARSEDENTITY.getBytes(), attrs, values, true);
   }   

   //**************************************************************************
   // Private methods - utility
   //**************************************************************************

   private byte[] getFrequency(boolean isRequired, boolean isRepeatable)
   {
	  String freq;

	  if (isRequired)
	  {
		 freq = (isRepeatable) ? DDMLConst.ENUM_ONEORMORE : DDMLConst.ENUM_REQUIRED;
	  }
	  else
	  {
		 freq = (isRepeatable) ? DDMLConst.ENUM_ZEROORMORE : DDMLConst.ENUM_OPTIONAL;
	  }

	  return freq.getBytes();
   }   

   private byte[] getType(int type)
   {
	  String name = null;

	  switch (type)
	  {
		 case Attribute.TYPE_CDATA:
			name = DDMLConst.ENUM_CDATA;
			break;

		 case Attribute.TYPE_ID:
			name = DDMLConst.ENUM_ID;
			break;

		 case Attribute.TYPE_IDREF:
			name = DDMLConst.ENUM_IDREF;
			break;

		 case Attribute.TYPE_IDREFS:
			name = DDMLConst.ENUM_IDREFS;
			break;

		 case Attribute.TYPE_ENTITY:
			name = DDMLConst.ENUM_ENTITY;
			break;

		 case Attribute.TYPE_ENTITIES:
			name = DDMLConst.ENUM_ENTITIES;
			break;

		 case Attribute.TYPE_NMTOKEN:
			name = DDMLConst.ENUM_NMTOKEN;
			break;

		 case Attribute.TYPE_NMTOKENS:
			name = DDMLConst.ENUM_NMTOKENS;
			break;

		 case Attribute.TYPE_NOTATION:
			name = DDMLConst.ENUM_NOTATION;
			break;

		 case Attribute.TYPE_ENUMERATED:
			name = DDMLConst.ENUM_ENUMERATED;
			break;
	  }
	  return name.getBytes();
   }   

   private byte[] getRequired(int required)
   {
	  String req = null;

	  switch (required)
	  {
		 case Attribute.REQUIRED_REQUIRED:
		 case Attribute.REQUIRED_FIXED:
			req = DDMLConst.ENUM_YES;
			break;

		 case Attribute.REQUIRED_OPTIONAL:
		 case Attribute.REQUIRED_DEFAULT:
			req = DDMLConst.ENUM_NO;
			break;
	  }
	  return req.getBytes();
   }   
}