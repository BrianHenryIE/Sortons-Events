package ie.sortons.events.client.view;

import ie.sortons.events.client.presenter.AdminPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
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

	public AdminView(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
		
	}

	
	@Override
	public HasClickHandlers getAddButton() {
		return addPageButton;
	}


}
