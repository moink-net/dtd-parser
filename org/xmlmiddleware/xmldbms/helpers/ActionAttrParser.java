package org.xmlmiddleware.xmldbms.helpers;


import java.lang.*;
import org.w3c.dom.*;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.xmldbms.maps.*;



class ActionAttrParser
{
    //*********************************************************************
    // Action Attribute constants. These are set on the actual XML Elements
    //*********************************************************************

    // The namespace and attr name for action attributes
    protected static final String NS_ACTIONS = "http://www.xmlmiddleware.org/xmldbms/actions";
    protected static final String ATTR_ACTION = "Action";

    protected static final String ATTR_ACTION_NONE          = "None";
    protected static final String ATTR_ACTION_INSERT        = "Insert";
    protected static final String ATTR_ACTION_SOFTINSERT    = "SoftInsert";
    protected static final String ATTR_ACTION_INSORUPDATE   = "InsertOrUpdate";
    protected static final String ATTR_ACTION_UPDATE        = "Update";
    protected static final String ATTR_ACTION_DELETE        = "Delete";
    protected static final String ATTR_ACTION_SOFTDELETE    = "SoftDelete";


    public static Action getAction(Element el, ClassMap classMap)
        throws MapException
    {
        Attr attrAction = el.getAttributeNodeNS(NS_ACTIONS, ATTR_ACTION);

        if(attrAction == null)
        {
            return null;
        }
        else
        {
            String s = attrAction.getValue();
            int act;

            if(s.equals(ATTR_ACTION_NONE))
                act = Action.NONE;
            else if(s.equals(ATTR_ACTION_INSERT))
                act = Action.INSERT;
            else if(s.equals(ATTR_ACTION_SOFTINSERT))
                act = Action.SOFTINSERT;
            else if(s.equals(ATTR_ACTION_INSORUPDATE))
                act = Action.UPDATEORINSERT;
            else if(s.equals(ATTR_ACTION_UPDATE))
                act = Action.UPDATE;
            else if(s.equals(ATTR_ACTION_DELETE))
                act = Action.DELETE;
            else if(s.equals(ATTR_ACTION_SOFTDELETE))
                act = Action.SOFTDELETE;
            else
            {   
                // TODO: Get the right exception
                throw new MapException("xmldbms: Invalid action specified on element.");
            }
            
            Action action = new Action(XMLName.create(el.getNamespaceURI(), el.getLocalName()), classMap);
            action.setAction(act);

            return action;
        }
    }

};