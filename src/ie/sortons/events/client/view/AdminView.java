package ie.sortons.events.client.view;

import ie.sortons.events.client.appevent.NotLoggedInEvent;
import ie.sortons.events.client.presenter.AdminPresenter;
import ie.sortons.events.client.view.widgets.AdminPageItem;
import ie.sortons.events.shared.FbPage;
import ie.sortons.gwtfbplus.client.api.Canvas;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventHandler;

public class AdminView extends Composite implements AdminPresenter.Display {

	private static AdminViewUiBinder uiBinder = GWT.create(AdminViewUiBinder.class);

	interface AdminViewUiBinder extends UiBinder<Widget, AdminView> {
	}

	AdminPresenter presenter;

	public void setPresenter(AdminPresenter presenter) {
		this.presenter = presenter;
	}

	public AdminView() {
		initWidget(uiBinder.createAndBindUi(this));

		addPageTextBox.getElement().setAttribute("placeholder", "Enter a Facebook Page URL, Page ID or search suggestions");
	}

	@UiField
	Button addPageButton;

	@UiField
	Button loginButton;

	@UiField
	TextBox addPageTextBox;

	@UiField
	FlowPanel includedPagesPanel;

	@UiField
	FlowPanel suggestedPagesPanel;

	@Override
	public HasClickHandlers getAddPageButton() {
		return addPageButton;
	}

	@Override
	public HasClickHandlers getLoginButton() {
		return loginButton;
	}

	@Override
	public TextBox getAddPageTextBox() {
		return addPageTextBox;
	}

	@Override
	public void setIncludedPages(List<FbPage> includedPagesList) {
		System.out.println("view: setIncludedPages");
		updateList(includedPagesList, includedPagesPanel);
		Canvas.setSize();
	}

	@Override
	public void setSuggestedPages(List<FbPage> suggestionsList) {
		loginButton.getElement().getStyle().setDisplay(Display.NONE);
		updateList(suggestionsList, suggestedPagesPanel);
		Canvas.setSize();
	}

	private void updateList(List<FbPage> pagesList, FlowPanel panel) {

		// TODO
		// Don't empty it each time.

		panel.clear();

		for (FbPage page : pagesList) {

			AdminPageItem api = new AdminPageItem(page, presenter);

			if (panel == includedPagesPanel) {
				api.removeAddButton();
			}

			panel.add(api);
		}

	}

	@EventHandler
	void onLoginEvent(NotLoggedInEvent event) {
		loginButton.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
	}

}
