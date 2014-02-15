package ie.sortons.events.client.presenter;

import ie.sortons.events.client.RpcService;
import ie.sortons.events.shared.ClientPageData;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class SortonsAdminPresenter implements Presenter {

	public interface Display {

		void setClients(List<ClientPageData> clients);

		void setPresenter(SortonsAdminPresenter presenter);

		Widget asWidget();
	}

	private Display display;
	private RpcService rpcService;

	public SortonsAdminPresenter(RpcService rpcService, Display sortonsAdminView) {
		this.display = sortonsAdminView;
		this.rpcService = rpcService;
	}

	@Override
	public void go(HasWidgets container) {
		// bind();
		container.clear();
		container.add(display.asWidget());

	
		rpcService.getAllClients(new AsyncCallback<List<ClientPageData>>() {
			@Override
			public void onSuccess(List<ClientPageData> clients) {
				display.setClients(clients);

			}
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub

			}

		});

	}

}
