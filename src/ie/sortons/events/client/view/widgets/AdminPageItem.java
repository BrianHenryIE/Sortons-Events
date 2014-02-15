package ie.sortons.events.client.view.widgets;

import ie.sortons.events.client.presenter.PageAdminPresenter;
import ie.sortons.gwtfbplus.client.resources.GwtFbPlusResources;
import ie.sortons.gwtfbplus.client.widgets.Link;
import ie.sortons.gwtfbplus.client.widgets.buttons.X1Button;
import ie.sortons.gwtfbplus.client.widgets.popups.ToolTipPanel;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class AdminPageItem extends Composite {

	private static AdminPageItemUiBinder uiBinder = GWT.create(AdminPageItemUiBinder.class);

	interface AdminPageItemUiBinder extends UiBinder<Widget, AdminPageItem> {
	}

	@UiField
	FlowPanel flowPanel;
	
	@UiField
	HTMLPanel picPanel;

	@UiField
	X1Button ignoreButton;

	@UiField
	Anchor name;

	@UiField
	Label location;

	private FqlPage page;

	// private AdminPresenter presenter;

	public AdminPageItem(final FqlPage page, final PageAdminPresenter presenter) {
		
		initWidget(uiBinder.createAndBindUi(this));

		GwtFbPlusResources.INSTANCE.css().ensureInjected();
	
		this.page = page;

		Image pageImage = new Image("//graph.facebook.com/" + page.getPageId() + "/picture?type=square");
		pageImage.setHeight("25px");
		pageImage.setWidth("25px");

		name.setText(page.getName());
		name.setHref(page.getPageUrl());
		name.setTarget("_blank");

		if (page.getLocation() != null)
			location.setText(page.getLocation().friendlyString());

		ToolTipPanel pageImageToolTip = new ToolTipPanel(page.getName(), pageImage);
		pageImageToolTip.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);

		Link pageLink = new Link(page.getPageUrl(), pageImageToolTip);
		pageLink.setTarget("_blank");

		picPanel.add(pageLink);

		ignoreButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.removePage(page);
			}
		});				
	}


	public void removeIgnoreButton() {
		ignoreButton.setVisible(false);
	}

	public FqlPage getPage() {
		return page;
	}

}
