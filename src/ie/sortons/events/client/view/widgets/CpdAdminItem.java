package ie.sortons.events.client.view.widgets;

import ie.sortons.events.shared.ClientPageData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

public class CpdAdminItem extends Composite {

	private static CpdAdminItemUiBinder uiBinder = GWT.create(CpdAdminItemUiBinder.class);

	interface CpdAdminItemUiBinder extends UiBinder<Widget, CpdAdminItem> {
	}

	private ClientPageData client;

	public CpdAdminItem(ClientPageData client) {
		this.client = client;
		initWidget(uiBinder.createAndBindUi(this));
		title.setText(client.getClientPage().getName());
		title.setHref(client.getClientPage().getPageUrl());
	}

	@UiField
	Anchor title;

	@UiHandler("button")
	void onClick(ClickEvent e) {
		Window.alert("Hello!");
	}

}
