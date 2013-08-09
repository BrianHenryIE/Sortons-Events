package ie.sortons.events.client.widgets;
import ie.sortons.gwtfbplus.client.widgets.popups.ToolTipPanel;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class Link extends SimplePanel {
	
	ToolTipPanel popup = new ToolTipPanel("", this);
	
	public Link(String href, Widget widget, String helpText) {
		super(DOM.createAnchor());
		setHref(href);
		this.add(widget);
		System.out.println("setting: " + helpText);
		popup.getPopUp().setText(helpText);
	}

	public void setHref(String href) {
		getElement().setAttribute("href", href);
	}

	public String getHref() {
		return getElement().getAttribute("href");
	}

	public void setTarget(String frameName) {
		getElement().setAttribute("target", frameName);
	}
}