// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.schemas.converters;

import de.tudarmstadt.ito.schemas.dtd.Attribute;
import de.tudarmstadt.ito.schemas.dtd.DTD;
import de.tudarmstadt.ito.schemas.dtd.DTDException;
import de.tudarmstadt.ito.schemas.dtd.ElementType;
import de.tudarmstadt.ito.schemas.dtd.Group;
import de.tudarmstadt.ito.schemas.dtd.Notation;
import de.tudarmstadt.ito.schemas.dtd.Particle;
import de.tudarmstadt.ito.schemas.dtd.Reference;
import de.tudarmstadt.ito.schemas.dtd.UnparsedEntity;
import de.tudarmstadt.ito.utils.NSName;
import de.tudarmstadt.ito.utils.TokenList;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

/**
 * Convert a DDML document to a DTD object.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class DDMLToDTD extends HandlerBase
{
   // This class assumes that the DDML file is correct.

   //*************************************************************************
   // Private variables
   //*************************************************************************

   private int            moreLevel, docLevel;
   private boolean        inElementDecl, inMixed, inGroup;
   private DTD            dtd;
   private ElementType    elementType;
   private Attribute      attribute;
   private String         globalURI, globalPrefix, globalRefURI,
						  elementURI, elementPrefix,
						  mixedRefURI, groupRefURI,
						  attrGroupURI, attrGroupPrefix;
   private Stack          attrGroupURIStack = new Stack(),
						  attrGroupPrefixStack = new Stack(),
						  groupStack = new Stack(),
						  groupURIStack = new Stack();
   private Group          group;
   private Parser         parser;
   TokenList              elementTokens, enumTokens;

   //*************************************************************************
   // Constructors
   //*************************************************************************

   /** Create a new DDMLToDTD object. */
   public DDMLToDTD()
   {
	  initTokens();
   }   

   /** Create a new DDMLToDTD object and set the SAX Parser. */
   public DDMLToDTD(Parser parser)
   {
	  this.parser = parser;
	  initTokens();
   }   

   //*************************************************************************
   // Public methods
   //*************************************************************************

   /**
	* Get the current SAX Parser.
	*
	* @return The current SAX Parser.
	*/
   public Parser getParser()
   {
	  return parser;
   }   

   /**
	* Set the current SAX Parser.
	*
	* @param parser The current SAX Parser.
	*/
   public void setParser(Parser parser)
   {
	  this.parser = parser;
   }   

   /**
	* Set the SAX parser, then convert the DDML document to a DTD object.
	*
	* @param parser The current SAX Parser.
	* @param src A SAX InputSource for the DDML document.
	* @return A DTD object.
	*/
   public DTD convert(Parser parser, InputSource src)
	  throws SAXException, IOException
   {
	  setParser(parser);
	  return convert(src);
   }   

   /**
	* Convert the DDML document to a DTD object.
	*
	* @param src A SAX InputSource for the DDML document.
	* @return A DTD object.
	*/
   public DTD convert(InputSource src)
	  throws SAXException, IOException
   {
	  if (src == null)
		 throw new IllegalArgumentException("The src argument must not be null.");
	  if (parser == null)
		 throw new IllegalStateException("You must set the parser before creating a DTD object.");

	  // Initialize the global variables.

	  initGlobals();

	  // Parse the DDML document.

	  parser.setDocumentHandler(this);
	  parser.parse(src);

	  // Post-process the DTD object
	  postProcessDTD();

	  // Return the DTD object.

	  return dtd;
   }   

   //*************************************************************************
   // SAX methods
   //*************************************************************************

   /**
	* Implementation of startDocument in SAX' DocumentHandler interface.
	* This method is called by the SAX Parser and should not be called by
	* programmers using DDMLToDTD.
	*/
   public void startDocument() throws SAXException
   {
   }   

   /**
	* Implementation of startElement in SAX' DocumentHandler interface.
	* This method is called by the SAX Parser and should not be called by
	* programmers using DDMLToDTD.
	*/
   public void startElement(String name, AttributeList attrs)
	 throws SAXException
   {
	  int    token;
	  
	  token = elementTokens.getToken(name);
	  
//      // Debugging code.
//      if (debug)
//      {
//         indent();
//         System.out.println(name + " (start)");
//         indent += 3;
//      }

	  // If we are not inside an element type declaration, then the only
	  // elements we are interested in are Notation, DocumentDef,
	  // and ElementDecl.

	  if (!inElementDecl)
	  {
		 if ((token != DDMLConst.ELEM_TOKEN_DOCUMENTDEF) &&
			 (token != DDMLConst.ELEM_TOKEN_ELEMENTDECL) &&
			 (token != DDMLConst.ELEM_TOKEN_NOTATION))
		 {
			return;
		 }
	  }

	  // If we are inside a More element, ignore all elements and return.
	  // Note that we keep track of the level of More elements in case they
	  // are nested inside each other. Thus, we know when we have exited
	  // the outermost More element.

	  if (moreLevel > 0)
	  {
		 if (token == DDMLConst.ELEM_TOKEN_MORE)
		 {
			moreLevel++;
		 }
		 return;
	  }

	  // If we are inside a Doc element, ignore all elements and return.
	  // Note that we keep track of the level of Doc elements in case they
	  // are nested inside each other. Thus, we know when we have exited
	  // the outermost Doc element. Note that Doc elements inside More
	  // elements are ignored.

	  if (docLevel > 0)
	  {
		 if (token == DDMLConst.ELEM_TOKEN_DOC)
		 {
			docLevel++;
		 }
		 return;
	  }

	  switch (token)
	  {
		 case DDMLConst.ELEM_TOKEN_ANY:
			processAny();
			break;

		 case DDMLConst.ELEM_TOKEN_ATTDEF:
			processAttDef(attrs);
			break;

		 case DDMLConst.ELEM_TOKEN_ATTGROUP:
			processAttGroupStart(attrs);
			break;

		 case DDMLConst.ELEM_TOKEN_CHOICE:
		 case DDMLConst.ELEM_TOKEN_SEQ:
			processChoiceOrSeqStart(attrs, token);
			break;

		 case DDMLConst.ELEM_TOKEN_DOC:
			docLevel++;
			break;

		 case DDMLConst.ELEM_TOKEN_DOCUMENTDEF:
			processDocumentDef(attrs);
			break;

		 case DDMLConst.ELEM_TOKEN_ELEMENTDECL:
			processElementDeclStart(attrs);
			break;

		 case DDMLConst.ELEM_TOKEN_EMPTY:
			processEmpty();
			break;

		 case DDMLConst.ELEM_TOKEN_ENUMERATION:
			processEnumeration();
			break;

		 case DDMLConst.ELEM_TOKEN_ENUMERATIONVALUE:
			processEnumerationValue(attrs);
			break;

		 case DDMLConst.ELEM_TOKEN_MIXED:
			processMixedStart(attrs);
			break;

		 case DDMLConst.ELEM_TOKEN_MODEL:
			// Container element -- nothing to do.
			break;

		 case DDMLConst.ELEM_TOKEN_MORE:
			moreLevel++;
			break;

		 case DDMLConst.ELEM_TOKEN_NOTATION:
			processNotation(attrs);
			break;

		 case DDMLConst.ELEM_TOKEN_PCDATA:
			processPCData();
			break;

		 case DDMLConst.ELEM_TOKEN_REF:
			processRef(attrs);
			break;

		 case DDMLConst.ELEM_TOKEN_UNPARSEDENTITY:
			processUnparsedEntity(attrs);
			break;

		 case DDMLConst.ELEM_TOKEN_UNKNOWN:
			throw new SAXException("Unknown element: " + name);
	  }
   }   

   /**
	* Implementation of endElement in SAX' DocumentHandler interface.
	* This method is called by the SAX Parser and should not be called by
	* programmers using DDMLToDTD.
	*/
   public void endElement (String name) throws SAXException
   {
	  int    token;
	  
	  token = elementTokens.getToken(name);

	  // Debugging code.
//      if (debug)
//      {
//         indent -= 3;
//         indent();
//         System.out.println(name + " (end)");
//      }

	  // If we are not inside an element declaration, nothing is of
	  // interest, so just return.

	  if (!inElementDecl) return;

	  // If we are inside a More element, check if we are ending that
	  // More element.

	  if (moreLevel > 0)
	  {
		 if (token == DDMLConst.ELEM_TOKEN_MORE)
		 {
			moreLevel--;
		 }
		 return;
	  }

	  // If we are inside a Doc element, check if we are ending that
	  // Doc element. Note that Doc elements inside More elements are
	  // ignored.

	  if (docLevel > 0)
	  {
		 if (token == DDMLConst.ELEM_TOKEN_DOC)
		 {
			docLevel--;
		 }
		 return;
	  }

	  switch (token)
	  {
		 case DDMLConst.ELEM_TOKEN_ATTGROUP:
			processAttGroupEnd();
			break;

		 case DDMLConst.ELEM_TOKEN_CHOICE:
		 case DDMLConst.ELEM_TOKEN_SEQ:
			processChoiceOrSeqEnd();
			break;

		 case DDMLConst.ELEM_TOKEN_ELEMENTDECL:
			processElementDeclEnd();
			break;

		 case DDMLConst.ELEM_TOKEN_MIXED:
			processMixedEnd();
			break;
			
		 case DDMLConst.ELEM_TOKEN_ANY:
		 case DDMLConst.ELEM_TOKEN_ATTDEF:
		 case DDMLConst.ELEM_TOKEN_DOC:
		 case DDMLConst.ELEM_TOKEN_DOCUMENTDEF:
		 case DDMLConst.ELEM_TOKEN_EMPTY:
		 case DDMLConst.ELEM_TOKEN_ENUMERATION:
		 case DDMLConst.ELEM_TOKEN_ENUMERATIONVALUE:
		 case DDMLConst.ELEM_TOKEN_MODEL:
		 case DDMLConst.ELEM_TOKEN_MORE:
		 case DDMLConst.ELEM_TOKEN_NOTATION:
		 case DDMLConst.ELEM_TOKEN_PCDATA:
		 case DDMLConst.ELEM_TOKEN_REF:
		 case DDMLConst.ELEM_TOKEN_UNPARSEDENTITY:
		 case DDMLConst.ELEM_TOKEN_UNKNOWN:
			// There is nothing to do for these elements.  Either they are
			// not part of the DTD (e.g. More, DocumentDef) or they
			// are completely handled elsewhere (usually by startElement).
			break;
	  }
   }   

   /**
	* Implementation of characters in SAX' DocumentHandler interface.
	* This method is called by the SAX Parser and should not be called by
	* programmers using DDMLToDTD.
	*/
   public void characters (char ch[], int start, int length)
	 throws SAXException
   {
	  // DDML documents only have character data inside More and Doc
	  // elements, both of which we ignore.
   }   

   /**
	* Implementation of endDocument in SAX' DocumentHandler interface.
	* This method is called by the SAX Parser and should not be called by
	* programmers using DDMLToDTD.
	*/
   public void endDocument() throws SAXException
   {
   }   

   //*************************************************************************
   // Private methods -- element processing
   //*************************************************************************

   private void processAny()
   {
	  elementType.contentType = ElementType.CONTENT_ANY;
   }   

   private void processAttDef(AttributeList attrs)
   {
	  String name, prefix, uri;

	  // Get the prefix and namespace of the attribute.
	  //
	  // There is a flaw in DDML with respect to namespaces.
	  //
	  // According to the namespaces spec, an unprefixed attribute name does
	  // not exist in any XML namespace. (Instead, it exists in a "traditional
	  // namespace" local to the element type.) This is the most common
	  // situation. However, when we were designing DDML, we did not understand
	  // this and thought it belonged to the same namespace as the element type.
	  // This can be seen in the inheritence of ns and prefix attributes from
	  // ElementDecl to AttGroup to AttDef.
	  //
	  // To remedy this situation, we check if they are the same as those of
	  // the element type, we set them to null. Note that this action is
	  // probably contrary to section 3.2 of the DDML spec.

	  prefix = getAttrValue(attrs, DDMLConst.ATTR_PREFIX);
	  if (prefix == null) prefix = attrGroupPrefix;
	  if (prefix != null)
	  {
		 if (prefix.equals(elementPrefix)) prefix = null;
	  }

	  uri = getAttrValue(attrs, DDMLConst.ATTR_NS);
	  if (uri == null) uri = attrGroupURI;
	  if (uri != null)
	  {
		 if (uri.equals(elementURI)) uri = null;
	  }

	  // Get the attribute's name and create a new Attribute
	  name = getAttrValue(attrs, DDMLConst.ATTR_NAME);
	  attribute = new Attribute(name, prefix, uri);

	  // Set the type, default value, etc. of the attribute.

	  attribute.type = getAttrType(getAttrValue(attrs, DDMLConst.ATTR_TYPE));
	  attribute.defaultValue = getAttrValue(attrs, DDMLConst.ATTR_ATTVALUE);
	  attribute.required = getAttrRequired(attribute.defaultValue,
								  getAttrValue(attrs, DDMLConst.ATTR_REQUIRED));

	  // Add the attribute to the element type.

	  elementType.attributes.put(attribute.name.qualified, attribute);
   }   

   private void processAttGroupStart(AttributeList attrs)
   {
	  attrGroupURIStack.push(attrGroupURI);
	  attrGroupPrefixStack.push(attrGroupPrefix);
	  attrGroupURI = getAttrValue(attrs, DDMLConst.ATTR_NS);
	  if (attrGroupURI == null)
	  {
		 attrGroupURI = elementURI;
	  }
	  attrGroupPrefix = getAttrValue(attrs, DDMLConst.ATTR_PREFIX);
	  if (attrGroupPrefix == null)
	  {
		 attrGroupPrefix = elementPrefix;
	  }
   }   

   private void processAttGroupEnd()
   {
	  attrGroupURI = (String)attrGroupURIStack.pop();
	  attrGroupPrefix = (String)attrGroupPrefixStack.pop();
   }   

   private void processChoiceOrSeqStart(AttributeList attrs, int token)
   {
	  Group  parent;
	  String uri;

	  // Push the current Group (which is null if there is no group)
	  // onto the stack and create a new one.

	  parent = group;
	  groupStack.push(parent);
	  group = new Group();

	  // Get the namespace URI, if any. If it is null and we are in the
	  // top level group, use the global URI. Otherwise, use the parent
	  // URI. Then push the parent URI on the stack.

	  uri = getAttrValue(attrs, DDMLConst.ATTR_ELEMENTNS);
	  if (uri == null)
	  {
		 uri = (inGroup) ? groupRefURI : globalRefURI;
	  }
	  groupURIStack.push(groupRefURI);
	  groupRefURI = uri;

	  // Set the frequency and type of the new group.

	  setFrequency(group, getAttrValue(attrs, DDMLConst.ATTR_FREQUENCY));
	  if (token == DDMLConst.ELEM_TOKEN_CHOICE)
	  {
		 group.type = Particle.PARTICLE_CHOICE;
	  }
	  else
	  {
		 group.type = Particle.PARTICLE_SEQUENCE;
	  }

	  // Add the new group to its parent. If there is no parent, then this
	  // is the content for the element type.

	  if (parent != null)
	  {
		 parent.members.addElement(group);
	  }
	  else
	  {
		 elementType.content = group;
	  }

	  // Set a flag saying we are in a group, so we know how to
	  // process Ref elements.

	  inGroup = true;
   }   

   private void processChoiceOrSeqEnd()
   {
	  // Pop the previous group and URI off the stack. If there was no
	  // previous group, we get a null.

	  group = (Group)groupStack.pop();
	  groupRefURI = (String)groupURIStack.pop();
	  inGroup = (group != null);
   }   

   private void processDocumentDef(AttributeList attrs)
	  throws SAXException
   {
	  String version;

	  version = getAttrValue(attrs, DDMLConst.ATTR_VERSION);
	  if (version != null)
	  {
		 if (!version.equals(DDMLConst.DEF_VERSION))
			throw new SAXException("Invalid DDML version number: " + version);
	  }

	  globalURI = getAttrValue(attrs, DDMLConst.ATTR_NS);
	  globalPrefix = getAttrValue(attrs, DDMLConst.ATTR_PREFIX);
	  globalRefURI = getAttrValue(attrs, DDMLConst.ATTR_ELEMENTNS);
   }   

   private void processElementDeclStart(AttributeList attrs) throws SAXException
   {
	  String name;

	  // Get the ElementType. Note that this might already have been created,
	  // for example, when the element type was referred to in the content model
	  // of a different element type.

	  name = getAttrValue(attrs, DDMLConst.ATTR_NAME);
	  elementPrefix = getAttrValue(attrs, DDMLConst.ATTR_PREFIX);
	  if (elementPrefix == null)
	  {
		 // If no prefix is defined here, use the prefix from the
		 // DocumentDef element.
		 elementPrefix = globalPrefix;
	  }
	  elementURI = getAttrValue(attrs, DDMLConst.ATTR_NS);
	  if (elementURI == null)
	  {
		 // If no namespace URI is defined here, use the URI from the
		 // DocumentDef element.
		 elementURI = globalURI;
	  }
	  try
	  {
		 elementType = dtd.addElementType(name, elementPrefix, elementURI);
	  }
	  catch (DTDException e)
	  {
		 throw new SAXException(e);
	  }

	  // Set the prefixed name. If the ElementType had already been created,
	  // this wouldn't have been set because the prefix was not known at that
	  // time.

	  elementType.name.prefixed = NSName.getPrefixedName(name, elementPrefix);

	  // Set the content model type to CONTENT_ELEMENT. This is overridden by
	  // the PCData, Any, Empty, and Mixed elements.

	  elementType.contentType = ElementType.CONTENT_ELEMENT;

	  // Set the state.

	  inElementDecl = true;
   }   

   private void processElementDeclEnd()
   {
	  inElementDecl = false;
   }   

   private void processEmpty()
   {
	  elementType.contentType = ElementType.CONTENT_EMPTY;
   }   

   private void processEnumeration()
   {
	  attribute.enums = new Vector();
   }   

   private void processEnumerationValue(AttributeList attrs)
   {
	  attribute.enums.addElement(getAttrValue(attrs, DDMLConst.ATTR_VALUE));
   }   

   private void processMixedStart(AttributeList attrs)
   {
	  elementType.contentType = ElementType.CONTENT_MIXED;
	  elementType.content = new Group();
	  elementType.content.type = Particle.PARTICLE_CHOICE;
	  elementType.content.isRequired = false;
	  elementType.content.isRepeatable = true;
	  mixedRefURI = getAttrValue(attrs, DDMLConst.ATTR_ELEMENTNS);
	  if (mixedRefURI == null)
	  {
		 mixedRefURI = globalRefURI;
	  }
	  inMixed = true;
   }   

   private void processMixedEnd()
   {
	  inMixed = false;
   }   

   private void processNotation(AttributeList attrs)
   {
	  Notation n = new Notation();
	  n.name = getAttrValue(attrs, DDMLConst.ATTR_NAME);
	  n.publicID = getAttrValue(attrs, DDMLConst.ATTR_PUBIDLITERAL);
	  n.systemID = getAttrValue(attrs, DDMLConst.ATTR_SYSTEMLITERAL);
	  dtd.notations.put(n.name, n);
   }   

   private void processPCData()
   {
	  elementType.contentType = ElementType.CONTENT_PCDATA;
   }   

   private void processRef(AttributeList attrs)
   {
	  String      refURI, refName;
	  Reference   ref;
	  ElementType child;

	  // Get the name and namespace URI of the referenced element
	  // type, then get the ElementType for it.

	  refURI = getAttrValue(attrs, DDMLConst.ATTR_ELEMENTNS);
	  if (refURI == null)
	  {
		 if (inMixed)
		 {
			refURI = mixedRefURI;
		 }
		 else if (inGroup)
		 {
			refURI = groupRefURI;
		 }
		 else
		 {
			refURI = globalRefURI;
		 }
	  }
	  refName = getAttrValue(attrs, DDMLConst.ATTR_ELEMENT);
	  child = dtd.getElementType(refName, null, refURI);

	  // Add the referenced ElementType to the children of the
	  // current ElementType and add the current ElementType to
	  // the parents of the referenced ElementType.

	  elementType.children.put(child.name.qualified, child);
	  child.parents.put(elementType.name.qualified, elementType);

	  // Process the reference.

	  ref = new Reference(child);

	  if (inMixed)
	  {
		 // Add the Reference to the members of the content Group.

		 elementType.content.members.addElement(ref);
	  }
	  else
	  {
		 setFrequency(ref, getAttrValue(attrs, DDMLConst.ATTR_FREQUENCY));
		 if (inGroup) // In Choice or Seq element
		 {
			// Add the Reference as a member of the current choice
			// or sequence Group.

			group.members.addElement(ref);
		 }
		 else // Stand-alone Ref element
		 {
			// For a stand-alone Ref element, create a new Group for
			// the content and add the Reference to its members.

			elementType.content = new Group();
			elementType.content.type = Particle.PARTICLE_SEQUENCE;
			elementType.content.members.addElement(ref);
		 }
	  }
   }   

   private void processUnparsedEntity(AttributeList attrs)
   {
	  UnparsedEntity entity = new UnparsedEntity();

	  entity.name = getAttrValue(attrs, DDMLConst.ATTR_NAME);
	  entity.notation = getAttrValue(attrs, DDMLConst.ATTR_NOTATION);
	  entity.systemID = getAttrValue(attrs, DDMLConst.ATTR_SYSTEMLITERAL);
	  entity.publicID = getAttrValue(attrs, DDMLConst.ATTR_PUBIDLITERAL);
   }   

   //*************************************************************************
   // Private methods -- general processing
   //*************************************************************************

   private void initGlobals()
   {
	  // Initialize state variables.
//      indent = 0;
	  moreLevel = 0;
	  docLevel = 0;
	  inElementDecl = false;
	  inMixed = false;
	  inGroup = false;
	  attrGroupURI = null;
	  attrGroupPrefix = null;
	  group = null;
	  groupRefURI = null;

	  // Create a new DTD object.
	  dtd = new DTD();
   }   

   private void initTokens()
   {
	  elementTokens = new TokenList(DDMLConst.ELEMS, DDMLConst.ELEM_TOKENS, DDMLConst.ELEM_TOKEN_UNKNOWN);
	  enumTokens = new TokenList(DDMLConst.ENUMS, DDMLConst.ENUM_TOKENS, DDMLConst.ENUM_TOKEN_UNKNOWN);
   }   

   private void postProcessDTD()
	  throws SAXException
   {
	  dtd.updateANYParents();
	  try
	  {
		 dtd.checkElementTypeReferences();
		 dtd.checkNotationReferences();
	  }
	  catch (DTDException e)
	  {
		 throw new SAXException(e);
	  }
   }   

   private int getAttrType(String type)
   {
	  if (type == null) type = DDMLConst.DEF_TYPE;

	  switch (enumTokens.getToken(type))
	  {
		 case DDMLConst.ENUM_TOKEN_CDATA:
			return Attribute.TYPE_CDATA;

		 case DDMLConst.ENUM_TOKEN_ENUMERATED:
			return Attribute.TYPE_ENUMERATED;

		 case DDMLConst.ENUM_TOKEN_NMTOKEN:
			return Attribute.TYPE_NMTOKEN;

		 case DDMLConst.ENUM_TOKEN_ID:
			return Attribute.TYPE_ID;

		 case DDMLConst.ENUM_TOKEN_IDREF:
			return Attribute.TYPE_IDREF;

		 case DDMLConst.ENUM_TOKEN_IDREFS:
			return Attribute.TYPE_IDREFS;

		 case DDMLConst.ENUM_TOKEN_ENTITY:
			return Attribute.TYPE_ENTITY;

		 case DDMLConst.ENUM_TOKEN_ENTITIES:
			return Attribute.TYPE_ENTITIES;

		 case DDMLConst.ENUM_TOKEN_NMTOKENS:
			return Attribute.TYPE_NMTOKENS;

		 case DDMLConst.ENUM_TOKEN_NOTATION:
			return Attribute.TYPE_NOTATION;

		 default:
			throw new IllegalArgumentException("Invalid attribute type: " + type);
	  }
   }   

   private void setFrequency(Particle p, String freq)
   {
	  if (freq == null) freq = DDMLConst.DEF_OTHER_FREQUENCY;
	  switch (enumTokens.getToken(freq))
	  {
		 case DDMLConst.ENUM_TOKEN_REQUIRED:
			p.isRequired = true;
			p.isRepeatable = false;
			break;

		 case DDMLConst.ENUM_TOKEN_OPTIONAL:
			p.isRequired = false;
			p.isRepeatable = false;
			break;

		 case DDMLConst.ENUM_TOKEN_ZEROORMORE:
			p.isRequired = false;
			p.isRepeatable = true;
			break;

		 case DDMLConst.ENUM_TOKEN_ONEORMORE:
			p.isRequired = true;
			p.isRepeatable = true;
			break;

		 default:
			throw new IllegalArgumentException("Invalid frequency: " + freq);
	  }
   }   

   private int getAttrRequired(String defaultValue, String required)
   {
	  if (required == null) required = DDMLConst.DEF_REQUIRED;
	  if (required.equals("No"))
	  {
		 return (defaultValue == null) ?
				Attribute.REQUIRED_OPTIONAL : Attribute.REQUIRED_DEFAULT;
	  }
	  else // if (required.equals("Yes"))
	  {
		 return (defaultValue == null) ?
				Attribute.REQUIRED_REQUIRED : Attribute.REQUIRED_FIXED;
	  }
   }   

   private String getAttrValue(AttributeList attrs, String name)
   {
	  String value;

	  value = attrs.getValue(name);

	  // Work-around for parsers that incorrectly return an empty
	  // string instead of a null when the attribute is not found.

	  if (value != null)
	  {
		 if (value.length() == 0)
		 {
			value = null;
		 }
	  }

	  return value;
   }   

   // Debugging tools
//   private boolean       debug = false;
//   private int           indent;
//   private static String SPACE = " ";
//   
//   private void indent()
//   {
//      for (int i = 0; i < indent; i++)
//      {
//         System.out.print(SPACE);
//      }
//   }
}