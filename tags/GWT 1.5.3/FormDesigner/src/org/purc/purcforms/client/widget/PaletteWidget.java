package org.purc.purcforms.client.widget;

import org.purc.purcforms.client.view.FormsTreeView.Images;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerCollection;
import com.google.gwt.user.client.ui.SourcesMouseEvents;


/**
 * This is a widget which can be put on the palette for users to drag and drop
 * on the design surface.
 * 
 * @author daniel
 *
 */
public class PaletteWidget extends Composite implements SourcesMouseEvents{

	/** The name of the widget. e.g TextBox, CheckBox, Label,Button etc */
	private String name;
	
	/** List of mouse listeners. */
	private MouseListenerCollection mouseListeners;


	/**
	 * Creates a new instance of the palette.
	 * 
	 * @param images the palette images.
	 * @param html the name of the widget
	 */
	public PaletteWidget(Images images, HTML html){
		name = html.getText();
		
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setSpacing(5);
		hPanel.add(images.add().createImage());
		hPanel.add(html);
		initWidget(hPanel);

		DOM.sinkEvents(getElement(),DOM.getEventsSunk(getElement()) | Event.MOUSEEVENTS);
		
		DOM.setStyleAttribute(getElement(), "cursor", "pointer");
	}

	@Override
	public void onBrowserEvent(Event event) {
		int type = DOM.eventGetType(event);

		switch (type) {
		case Event.ONMOUSEDOWN:
		case Event.ONMOUSEUP:
		case Event.ONMOUSEOVER:
		case Event.ONMOUSEMOVE:
		case Event.ONMOUSEOUT:
			if (mouseListeners != null) 
				mouseListeners.fireMouseEvent(this, event);
		}
	}

	public void addMouseListener(MouseListener listener) {
		if (mouseListeners == null) {
			mouseListeners = new MouseListenerCollection();
		}
		mouseListeners.add(listener);
	}

	public void removeMouseListener(MouseListener listener) {
		if (mouseListeners != null) {
			mouseListeners.remove(listener);
		}
	}
	
	/**
	 * Gets the name of the widget.
	 * 
	 * @return the name
	 */
	public String getName(){
		return name;
	}
}