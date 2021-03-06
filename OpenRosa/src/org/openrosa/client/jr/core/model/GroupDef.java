/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.openrosa.client.jr.core.model;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.openrosa.client.java.io.DataInputStream;
import org.openrosa.client.java.io.DataOutputStream;
import org.openrosa.client.jr.core.model.utils.DateUtils;
import org.openrosa.client.jr.core.services.locale.Localizable;
import org.openrosa.client.jr.core.services.locale.Localizer;
import org.openrosa.client.jr.core.util.externalizable.DeserializationException;
import org.openrosa.client.jr.core.util.externalizable.ExtUtil;
import org.openrosa.client.jr.core.util.externalizable.ExtWrapListPoly;
import org.openrosa.client.jr.core.util.externalizable.ExtWrapNullable;
import org.openrosa.client.jr.core.util.externalizable.ExtWrapTagged;
import org.openrosa.client.jr.core.util.externalizable.PrototypeFactory;

/** The definition of a group in a form or questionaire. 
 * 
 * @author Daniel Kayiwa
 *
 */
public class GroupDef implements IFormElement, Localizable {
	private Vector children;	/** A list of questions on a group. */	
	private boolean repeat;  /** True if this is a "repeat", false if it is a "group" */
	private int id;	/** The group number. */
	private IDataReference binding;	/** reference to a location in the model to store data in */


	private String labelInnerText;
	private String appearanceAttr;
	private String textID;
	
	Vector observers;
	
	public boolean noAddRemove = false;
	public IDataReference count = null;
	
	public GroupDef () {
		this(Constants.NULL_ID, null, false);
	}
	
	public GroupDef(int id, Vector children, boolean repeat) {
		setID(id);
		setChildren(children);
		setRepeat(repeat);
		observers = new Vector();
	}
	
	public int getID () {
		return id;
	}
	
	public void setID (int id) {
		this.id = id;
	}
	
	public IDataReference getBind() {
		return binding;
	}
	
	public void setBind(IDataReference binding) {
		this.binding = binding;
	}
	
	public Vector getChildren() {
		return children;
	}

	public void setChildren (Vector children) {
		this.children = (children == null ? new Vector() : children);
	}
	
	public void addChild (IFormElement fe) {
		children.addElement(fe);
	}
	
	public IFormElement getChild (int i) {
		if (children == null || i >= children.size()) {
			return null;
		} else {
			return (IFormElement)children.elementAt(i);
		}
	}
	
	/**
	 * @return true if this represents a <repeat> element
	 */
	public boolean getRepeat () {
		return repeat;
	}
	
	public void setRepeat (boolean repeat) {
		this.repeat = repeat;
	}

	public String getLabelInnerText() {
		return labelInnerText;
	}
	
	public void setLabelInnerText(String lit){
		labelInnerText = lit;
	}


	public String getAppearanceAttr () {
		return appearanceAttr;
	}
	
	public void setAppearanceAttr (String appearanceAttr) {
		this.appearanceAttr = appearanceAttr;
	}	
        
    public void localeChanged(String locale, Localizer localizer) {
    	for (Enumeration e = children.elements(); e.hasMoreElements(); ) {
    		((IFormElement)e.nextElement()).localeChanged(locale, localizer);
    	}
    }
    
    public IDataReference getCountReference() {
    	return count;
    }
	
	public String toString() {
		return "<group>";
	}
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IFormElement#getDeepChildCount()
	 */
	public int getDeepChildCount() {
		int total = 0;
		Enumeration e = children.elements();
		while(e.hasMoreElements()) {
			total += ((IFormElement)e.nextElement()).getDeepChildCount();
		}
		return total;
	}

	/** Reads a group definition object from the supplied stream. */
	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		setID(ExtUtil.readInt(dis));
		setAppearanceAttr((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setBind((IDataReference)ExtUtil.read(dis, new ExtWrapTagged(), pf));
		setTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setLabelInnerText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setRepeat(ExtUtil.readBool(dis));
		setChildren((Vector)ExtUtil.read(dis, new ExtWrapListPoly(), pf));
		
		noAddRemove = ExtUtil.readBool(dis);
		count = (IDataReference)ExtUtil.read(dis, new ExtWrapNullable(new ExtWrapTagged()), pf);
	}

	/** Write the group definition object to the supplied stream. */
	public void writeExternal(DataOutputStream dos) throws IOException {
		ExtUtil.writeNumeric(dos, getID());
		ExtUtil.write(dos, new ExtWrapNullable(getAppearanceAttr()));
		ExtUtil.write(dos, new ExtWrapTagged(getBind()));
		ExtUtil.write(dos, new ExtWrapNullable(getTextID()));
		ExtUtil.write(dos, new ExtWrapNullable(getLabelInnerText()));	
		ExtUtil.writeBool(dos, getRepeat());
		ExtUtil.write(dos, new ExtWrapListPoly(getChildren()));

		ExtUtil.writeBool(dos, noAddRemove);
		ExtUtil.write(dos, new ExtWrapNullable(count != null ? new ExtWrapTagged(count) : null));
		
	}
	
	public void registerStateObserver (FormElementStateListener qsl) {
		if (!observers.contains(qsl)) {
			observers.addElement(qsl);
		}
	}
	
	public void unregisterStateObserver (FormElementStateListener qsl) {
		observers.removeElement(qsl);
	}
	
	public String getTextID() {
		return textID;
	}

	public void setTextID(String textID) {
		if(textID==null){
			this.textID = null;
			return;
		}
		if(DateUtils.stringContains(textID,";")){
			System.err.println("Warning: TextID contains ;form modifier:: \""+textID.substring(textID.indexOf(";"))+"\"... will be stripped.");
			textID=textID.substring(0, textID.indexOf(";")); //trim away the form specifier
		}
		this.textID = textID;
	}
}
