package ie.sortons.events.client.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.kfuntak.gwt.json.serialization.client.Serializer;

import ie.sortons.events.client.RpcService;
import ie.sortons.events.client.appevent.LoginAuthResponseEvent;
import ie.sortons.events.client.appevent.PermissionsEvent;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.SourcePage;
import ie.sortons.gwtfbplus.client.overlay.FbResponse;
import ie.sortons.gwtfbplus.client.widgets.suggestbox.FbSearchable;
import ie.sortons.gwtfbplus.client.widgets.suggestbox.FbSingleSuggestbox;
import ie.sortons.gwtfbplus.client.widgets.suggestbox.SelectedItemWidget;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphPage;


public class PageAdminPresenter implements Presenter {

	private static final Logger log = Logger.getLogger(PageAdminPresenter.class.getName());

	interface MyEventBinder extends EventBinder<PageAdminPresenter> {
	}

	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	Serializer serializer = (Serializer) GWT.create(Serializer.class);

	private final RpcService rpcService;

	private final EventBus eventBus;
	private final Display display;

	public interface Display {
		FbSingleSuggestbox getSuggestBox();

		void setIncludedPages(List<SourcePage> includedList);

		void setPresenter(PageAdminPresenter presenter);

		Widget asWidget();
	}

	public PageAdminPresenter(EventBus eventBus, final RpcService dao, Display view) {
		this.rpcService = dao;
		this.eventBus = eventBus;
		this.display = view;
		getClientPageData();
		view.setPresenter(this);
	}

	@Override
	public void go(HasWidgets container) {
		bind();
		container.clear();
		container.add(display.asWidget());
	}

	public void bind() {

		eventBinder.bindEventHandlers(this, eventBus);

		display.getSuggestBox().addValueChangeHandler(new ValueChangeHandler<FbSearchable>() {
			@Override
			public void onValueChange(ValueChangeEvent<FbSearchable> event) {
				addPage((SourcePage) event.getValue());
			}
		});

		display.getSuggestBox().addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					Timer t = new Timer() {
						@Override
						public void run() {
							if (display.getSuggestBox().getValue() == null) {

								processTextBox(display.getSuggestBox().getValueBox().getText());
							}
						}
					};
					t.schedule(250); // Gives the oracle a moment to set the
										// value
				}
			}
		});
	}

	private void getClientPageData() {
		rpcService.refreshClientPageData(new AsyncCallback<ClientPageData>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(ClientPageData result) {
				display.setIncludedPages(result.getIncludedPages());
				getSuggestions();
			}
		});
	}

	private void getSuggestions() {
		rpcService.getSuggestions(new AsyncCallback<List<SourcePage>>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(List<SourcePage> result) {
				System.out.println("setsuggestions");

				List<FbSearchable> pages = new ArrayList<FbSearchable>();
				for (SourcePage p : result) {
					pages.add((FbSearchable) p);
				}

				display.getSuggestBox().setSuggestions(pages);
			}

		});
	}

	public void processTextBox(String textEntered) {

		// Get the text from the textbox
		// regex it to a page_id
		// get the page's basic details
		// send it to the server
		// add it to the included pages list
		// get the new page's likes to add to suggestions.

		// Get the text that has been entered and build the graph call

		System.out.println("processing text : " + textEntered);

		String graphPath = "/";

		textEntered = textEntered.replaceAll("\\?.*", "");

		textEntered = textEntered.replaceAll(".*facebook.com/", "");

		textEntered = textEntered.replaceAll("pages/.*/", "");

		// For lists from json
		textEntered = textEntered.replaceAll("\"", "");
		textEntered = textEntered.replaceAll("\n", "");
		textEntered = textEntered.replaceAll(" ", "");
		if (textEntered.endsWith(","))
			textEntered.substring(0, textEntered.length() - 1);

		if (!textEntered.contains(",")) {

			graphPath += textEntered;

			graphPath += "?fields=name,id,link";

			rpcService.graphCall(graphPath, new AsyncCallback<FbResponse>() {
				public void onSuccess(FbResponse response) {

					System.out.println(new JSONObject(response).toString());

					try {
						GraphPage pageDetails = (GraphPage) serializer.deSerialize(new JSONObject(response).toString(),
								"ie.sortons.gwtfbplus.shared.domain.graph.GraphPage");

						System.out.println("pageDetails.getName() " + pageDetails.getName());

						SourcePage newPage = new SourcePage(pageDetails.getName(), pageDetails.getId().toString(), pageDetails
								.getLink());

						// TODO
						// This should only empty when it's successful
						display.getSuggestBox().setValue(newPage, true); // This
																			// will
																			// fire
																			// the
																			// valuechangehandler

					} catch (Exception e) {

						log.info(e.getMessage());

					}

				}

				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub

					System.out.println("ERROR processTextBox");
				}
			});

		} else {

			// We've got a list of them!

			// TODO
			// Waiting indicator

			rpcService.addPagesList(textEntered, new AsyncCallback<List<SourcePage>>() {

				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onSuccess(List<SourcePage> result) {

					System.out.println("pages added: " + result.size());

					for (SourcePage page : result) {
						rpcService.getClientPageData().addPage(page);

						display.getSuggestBox().removeFromOracle(page);
					}

					// then update UI
					getClientPageData();
					display.getSuggestBox().getValueBox().setValue("");
					display.getSuggestBox().unSelectItem();

				}
			});

			// TODO
			// Give feedback when it doesn't match
		}

	}

	public void addPage(final SourcePage newPage) {

		System.out.println("client: adminPresenter addPage");
		System.out.println(serializer.serialize(newPage));

		rpcService.addPage(newPage, new AsyncCallback<SourcePage>() {

			@Override
			public void onFailure(Throwable caught) {

				// I refactored and don't know what's going on here
				display.getSuggestBox().addSelectedItemToDisplay(newPage, new SelectedItemWidget());

			}

			@Override
			public void onSuccess(SourcePage newPage) {

				// then update UI
				getClientPageData();

				display.getSuggestBox().unSelectItem();
				display.getSuggestBox().removeFromOracle(newPage);
			}
		});
	}

	public void removePage(SourcePage page) {

		rpcService.removePage(page, new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				System.out.println("Couldn't retrieve JSON: ignorePage/onError");
			}

			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {

					SourcePage page = (SourcePage) serializer.deSerialize(response.getText(),
							"ie.sortons.gwtfbplus.shared.domain.fql.FqlPage");

					rpcService.getClientPageData().removePage(page);

					// then update UI
					getClientPageData();

				} else {
					System.out.println("Couldn't retrieve JSON (" + response.getStatusText()
							+ ") AdminPresenter.ignorePage()");
					System.out.println(response.getText());
					// System.out.println("Couldn't retrieve JSON (" +
					// response.getStatusCode() + ")");
					// System.out.println("Couldn't retrieve JSON (" +
					// response.getText() + ")");
				}
			}
		});

		// TODO
		// UI cleanup

	}

	// This is for the first time the user authorises the app.
	@EventHandler
	void onLoginEvent(LoginAuthResponseEvent event) {
		getSuggestions();
	}

	// This is because getSuggestions was running before we had permission to
	@EventHandler
	void onLoginEvent(PermissionsEvent event) {
		getSuggestions();
	}

	// TODO clientpagedata hasn't always returned... RACE CONDITION!
	// maybe fixed by addition of getsuggestions on line 120
}