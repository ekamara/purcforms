package org.openrosa.client.view;

import org.openrosa.client.controller.IFileListener;
import org.purc.purcforms.client.Context;
import org.purc.purcforms.client.PurcConstants;
import org.purc.purcforms.client.controller.IFormSelectionListener;
import org.purc.purcforms.client.model.FormDef;
import org.purc.purcforms.client.model.ItextModel;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.ItextBuilder;
import org.purc.purcforms.client.util.ItextParser;
import org.purc.purcforms.client.xforms.XformParser;
import org.purc.purcforms.client.xforms.XhtmlBuilder;
import org.purc.purcforms.client.xforms.XmlUtil;

import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.xml.client.Document;


/**
 * 
 * @author daniel
 *
 */
public class CenterWidget extends Composite implements IFileListener,IFormSelectionListener {

	private static final int TAB_INDEX_DESIGN = 0;
	private static final int TAB_INDEX_XFORMS = 1;
	private static final int TAB_INDEX_ITEXT = 2;
	
	/**
	 * Tab widget housing the contents.
	 */
	private DecoratedTabPanel tabs = new DecoratedTabPanel();

	private XformsTabWidget xformsWidget = new XformsTabWidget();
	private DesignTabWidget designWidget = new DesignTabWidget();
	private TextTabWidget itextWidget = new TextTabWidget();

	private FormDef formDef;
	
	
	public CenterWidget() {		
		initDesignTab();
		initXformsTab();
		initItextTab();

		FormUtil.maximizeWidget(tabs);

		tabs.selectTab(TAB_INDEX_DESIGN);
		initWidget(tabs);
		//tabs.addSelectionHandler(this);

		FormUtil.maximizeWidget(tabs);
		FormUtil.maximizeWidget(this);
		
		designWidget.addFormSelectionListener(this);
	}

	private void initDesignTab(){
		tabs.add(designWidget, "Design");
	}

	private void initXformsTab(){
		tabs.add(xformsWidget, "Xforms");
	}

	private void initItextTab(){
		tabs.add(itextWidget, "Internationalization");
	}

	public void onWindowResized(int width, int height){
		int shortcutHeight = height - getAbsoluteTop();
		if(shortcutHeight > 50){
			xformsWidget.adjustHeight(shortcutHeight-130 + PurcConstants.UNITS);
			itextWidget.adjustHeight(shortcutHeight-50 + PurcConstants.UNITS);
		}

		designWidget.onWindowResized(width, height);
	}

	public void onNew(){
		designWidget.addNewForm();
	}

	public void onOpen(){
		FormUtil.dlg.setText("Opening...");
		FormUtil.dlg.show();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				try{
					openFile();
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}
	
	private void openFile(){
		String xml = xformsWidget.getXform();
		if(xml == null || xml.trim().length() == 0){
			tabs.selectTab(TAB_INDEX_XFORMS);
			return;
		}
		
		ListStore<ItextModel> list = new ListStore<ItextModel>();
		FormDef formDef = XformParser.getFormDef(ItextParser.parse(xml,list));
		formDef.setXformXml(xml);
		designWidget.loadForm(formDef);
		itextWidget.loadItext(list);
		tabs.selectTab(TAB_INDEX_DESIGN);
	}

	public void onSave(){
		FormUtil.dlg.setText("Saving...");
		FormUtil.dlg.show();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				try{
					saveFile();
					FormUtil.dlg.hide();
				}
				catch(Exception ex){
					FormUtil.displayException(ex);
				}
			}
		});
	}
	
	private void saveFile(){
		FormDef formDef = Context.getFormDef();
		
		String xml = null;
		
		if(tabs.getTabBar().getSelectedTab() == TAB_INDEX_ITEXT){
			if(formDef == null || formDef.getDoc() == null)
				return;
			
			ItextBuilder.updateItextBlock(formDef.getDoc(), formDef, itextWidget.getItext());
			xml = FormUtil.formatXml(XmlUtil.fromDoc2String(formDef.getDoc()));
		}
		else if(formDef != null){
			designWidget.commitChanges();
			
			//TODO need to solve bug when opened forms do not reflect changes in the instance data node names.
			//This is caused by copying a new model during the xhtml conversion
			Document doc = formDef.getDoc();
			
			if(doc != null)
				formDef.updateDoc(false);
			else{
				doc = XhtmlBuilder.fromFormDef2XhtmlDoc(formDef);
				formDef.setDoc(doc);
				formDef.setXformsNode(doc.getDocumentElement());
			}
			
			ListStore<ItextModel> list = new ListStore<ItextModel>();
			ItextBuilder.updateItextBlock(doc,formDef,list);
			itextWidget.loadItext(list);
			
			doc.getDocumentElement().setAttribute("xmlns:jr", "http://openrosa.org/javarosa");
			doc.getDocumentElement().setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
			
			xml = FormUtil.formatXml(XmlUtil.fromDoc2String(doc));
		}

		if(formDef != null)
			formDef.setXformXml(xml);
		else
			itextWidget.loadItext(new ListStore<ItextModel>());
		
		xformsWidget.setXform(xml);
		tabs.selectTab(TAB_INDEX_XFORMS);
	}
	
	public void onFormItemSelected(Object formItem){
		if(!(formItem instanceof FormDef))
			return;
		
		FormDef formDef = (FormDef)formItem;
		
		if(this.formDef == null || this.formDef != formItem){
			xformsWidget.setXform(formDef == null ? null : formDef.getXformXml());
			
			ListStore<ItextModel> list = new ListStore<ItextModel>();
			
			if(formDef != null && formDef.getDoc() != null)
				ItextBuilder.updateItextBlock(formDef.getDoc(),formDef,list);
			
			itextWidget.loadItext(list);
		}
		
		this.formDef = formDef;
	}
}