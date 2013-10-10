package ie.sortons.events.client.view.widgets;

import ie.sortons.events.shared.FbPage;
import ie.sortons.gwtfbplus.client.newresources.Resources;
import ie.sortons.gwtfbplus.client.widgets.Link;
import ie.sortons.gwtfbplus.client.widgets.popups.ToolTipPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class AdminPageItem extends Composite {

	private static AdminPageItemUiBinder uiBinder = GWT
			.create(AdminPageItemUiBinder.class);

	interface AdminPageItemUiBinder extends UiBinder<Widget, AdminPageItem> {
	}

	@UiField
	HTMLPanel picPanel;

	@UiField
	Image addButton;

	@UiField
	Image ignoreButton;

	
	private FbPage page;
	
	public AdminPageItem(FbPage page) {
		
		initWidget(uiBinder.createAndBindUi(this));
		
		Resources.INSTANCE.css().ensureInjected(); 
		
		addButton.setResource(Resources.INSTANCE.greenPlus());
		ignoreButton.setResource(Resources.INSTANCE.redX());

		this.page = page;
		
    	Image pageImage = new Image("//graph.facebook.com/" + page.getPageId() + "/picture?type=square");
    	pageImage.setHeight("50px");
    	pageImage.setWidth("50px");
    	
    	ToolTipPanel pageImageToolTip = new ToolTipPanel(page.getName(), pageImage);
    	pageImageToolTip.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
    	
    	Link pageLink = new Link(page.getPageUrl(), pageImageToolTip);
    	pageLink.setTarget("_blank");
    	
    	picPanel.add(pageLink);
		
	}

	public void removeAddButton() {
		addButton.setVisible(false);
	}
	
	public void removeIgnoreButton() {
		ignoreButton.setVisible(false);
	}
	
	
	public FbPage getPage(){
		return page;
	}

}
