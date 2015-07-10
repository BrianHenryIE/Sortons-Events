package ie.sortons.events.client.presenter;

import ie.sortons.events.client.RpcService;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.SourcePage;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class DirectoryPresenter implements Presenter {

	public interface Display {

		void setPages(List<SourcePage> pages);

		Widget asWidget();
	}

	private Display view;

	public DirectoryPresenter(RpcService rpcService, final Display view) {

		this.view = view;
		
		rpcService.refreshClientPageData(new AsyncCallback<ClientPageData>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onSuccess(ClientPageData result) {

				view.setPages(result.getIncludedPages());

			}
		});

	}

	@Override
	public void go(HasWidgets container) {
		container.clear();
		container.add(view.asWidget());
	}

}
