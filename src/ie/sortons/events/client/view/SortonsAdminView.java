package ie.sortons.events.client.view;

import ie.sortons.events.client.presenter.SortonsAdminPresenter;
import ie.sortons.events.client.presenter.SortonsAdminPresenter.Display;
import ie.sortons.events.client.view.widgets.CpdAdminItem;
import ie.sortons.events.shared.ClientPageData;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class SortonsAdminView extends Composite implements Display {

	private static SortonsAdminViewUiBinder uiBinder = GWT.create(SortonsAdminViewUiBinder.class);

	interface SortonsAdminViewUiBinder extends UiBinder<Widget, SortonsAdminView> {
	}


	@UiField
	FlowPanel cpdList;
	private SortonsAdminPresenter presenter;
	
	public SortonsAdminView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public SortonsAdminView(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
		
	}

	@Override
	public void setClients(List<ClientPageData> clients) {
		for(ClientPageData client : clients){
			cpdList.add(new CpdAdminItem(client));
		}
		
	}

	@Override
	public void setPresenter(SortonsAdminPresenter presenter) {
		this.presenter = presenter;
		
	}

}
