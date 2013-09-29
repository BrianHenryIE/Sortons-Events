package ie.sortons.events.client.view.widgets;

import ie.sortons.events.client.view.overlay.FbPageOverlay;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class AdminPageItem extends Composite {

	private static AdminPageItemUiBinder uiBinder = GWT
			.create(AdminPageItemUiBinder.class);

	interface AdminPageItemUiBinder extends UiBinder<Widget, AdminPageItem> {
	}

	public AdminPageItem() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Image pagePic;

	public AdminPageItem(FbPageOverlay page) {
		
		initWidget(uiBinder.createAndBindUi(this));
		
		pagePic.setUrl("https://graph.facebook.com/" + page.getPageId() + "/picture");
		
	}



}
