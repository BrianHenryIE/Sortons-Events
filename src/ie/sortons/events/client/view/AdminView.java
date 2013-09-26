package ie.sortons.events.client.view;

import ie.sortons.events.client.presenter.AdminPresenter;
import ie.sortons.events.client.view.overlay.FbPageOverlay;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class AdminView extends Composite implements AdminPresenter.Display {

	private static AdminViewUiBinder uiBinder = GWT
			.create(AdminViewUiBinder.class);

	interface AdminViewUiBinder extends UiBinder<Widget, AdminView> {
	}

	public AdminView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Button addPageButton;
	
	@UiField
	FlowPanel suggestedPages;

	
	@Override
	public HasClickHandlers getAddButton() {
		return addPageButton;
	}

	public void setSuggestedPages(List<FbPageOverlay> suggestionsList){
		// loop through entries in suggestions panel
		// remove if they're not in suggestionsList
		// loop through suggestionsList
		// add if they're not already in suggestions panel
	}

	@Override
	public void setIncludedPages(List<FbPageOverlay> includedPagesList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIgnoredPages(List<FbPageOverlay> ignoredPagesList) {
		// TODO Auto-generated method stub
		
	}

}
