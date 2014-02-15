package ie.sortons.events.client.presenter;

import ie.sortons.events.client.RpcService;
import ie.sortons.events.client.appevent.PermissionsEvent;
import ie.sortons.events.client.appevent.ResponseErrorEvent;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.FqlPageSearchable;
import ie.sortons.gwtfbplus.client.widgets.suggestbox.FbSearchable;
import ie.sortons.gwtfbplus.client.widgets.suggestbox.FbSingleSuggestbox;
import ie.sortons.gwtfbplus.client.widgets.suggestbox.SelectedItem;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphPage;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
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

public class PageAdminPresenter implements Presenter {

	interface MyEventBinder extends EventBinder<PageAdminPresenter> {
	}

	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	Serializer serializer = (Serializer) GWT.create(Serializer.class);

	private final RpcService dao;

	private final EventBus eventBus;
	private final Display display;

	private String requiredAdminPermissions = "";

	public interface Display {
		FbSingleSuggestbox getSuggestBox();

		void setIncludedPages(List<FqlPage> includedList);

		void setPresenter(PageAdminPresenter presenter);

		Widget asWidget();
	}

	public PageAdminPresenter(EventBus eventBus, final RpcService dao, Display view) {
		this.dao = dao;
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
				addPage((FqlPageSearchable) event.getValue());
			}
		});

		display.getSuggestBox().addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					// TODO Check there's no match in the oracle!
					System.out.println("keydown");
					Timer t = new Timer(){
						@Override
						public void run() {
							if(display.getSuggestBox().getValue()==null){
								System.out.println("asd");
								processTextBox(display.getSuggestBox().getValueBox().getText());
							}
						}
					};
					t.schedule(250);				
					
				}
			}
		});
	}

	private void getClientPageData() {
		dao.refreshClientPageData(this);
	}

	public void displayClientData(ClientPageData clientPageData) {
		display.setIncludedPages(dao.getClientPageData().getIncludedPages());
	}

	private void getSuggestions() {
		dao.getSuggestions(this);
	}

	public void setSuggestions(List<FqlPageSearchable> suggestionsList) {
		System.out.println("setsuggestions");
		if (suggestionsList != null) {

			List<FbSearchable> pages = new ArrayList<FbSearchable>();
			for (FqlPage p : suggestionsList) {
				pages.add((FbSearchable) p);
			}

			display.getSuggestBox().setSuggestions(pages);
		}
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
	

		if (!textEntered.contains(",")) {

			graphPath += textEntered;

		} else {
			
			// We've got a list of them!
			
			// TODO
			// Give feedback when it doesn't match
		}

		graphPath += "?fields=name,id,link";

		dao.graphCall(graphPath, new AsyncCallback<JavaScriptObject>() {
			public void onSuccess(JavaScriptObject response) {

				System.out.println(new JSONObject(response).toString());

				GraphPage pageDetails = (GraphPage) serializer.deSerialize(new JSONObject(response).toString(),
						"ie.sortons.gwtfbplus.shared.domain.graph.GraphPage");

				System.out.println("pageDetails.getName() " + pageDetails.getName());

				FqlPageSearchable newPage = new FqlPageSearchable();

				// TODO Worst case of OO in the project
				newPage.name = pageDetails.getName();
				newPage.page_url = pageDetails.getLink();
				newPage.page_id = pageDetails.getId();

				// TODO
				// This should only empty when it's successful
				display.getSuggestBox().setValue(newPage, true); // This will fire the valuechangehandler

			}

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub

				System.out.println("ERROR processTextBox");
			}
		});

	}

	public void addPage(final FqlPageSearchable newPage) {

		System.out.println("client: adminPresenter addPage");
		System.out.println(serializer.serialize(newPage));

		dao.addPage(newPage, new RequestCallback() {
			public void onError(Request request, Throwable exception) {

				// Set the suggestbox item to xable
				System.out.println("Couldn't retrieve JSON");
			}

			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {

					System.out.println("client: adminPresenter addPage response");
					System.out.println(response.getText());
					
					FqlPageSearchable page = (FqlPageSearchable) serializer.deSerialize(response.getText(),
							"ie.sortons.events.shared.FqlPageSearchable");

					// TODO return a real error message
					if (page.getPageId() != null) {
						dao.getClientPageData().addPage(page);

						// then update UI
						displayClientData(dao.getClientPageData());

						display.getSuggestBox().unSelectItem();
						display.getSuggestBox().removeFromOracle(page);

					} else {
						// TODO Fire error message
						// was page already included?
						// or serious error?
					}

				} else {
					System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ") AdminPresenter.addPage()");
					System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ")");
					System.out.println("Couldn't retrieve JSON (" + response.getText() + ")");

					// TODO: How to know what type of error it is?
					eventBus.fireEvent(new ResponseErrorEvent(response));

					display.getSuggestBox().addSelectedItemToDisplay(newPage, new SelectedItem());
				}
			}
		});
	}

	public void removePage(FqlPage page) {

		dao.removePage(page, new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				System.out.println("Couldn't retrieve JSON: ignorePage/onError");
			}

			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {

					FqlPage page = (FqlPage) serializer.deSerialize(response.getText(), "ie.sortons.gwtfbplus.shared.domain.fql.FqlPage");

					dao.getClientPageData().removePage(page);

					// then update UI
					displayClientData(dao.getClientPageData());

				} else {
					System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ") AdminPresenter.ignorePage()");
					System.out.println(response.getText());
					// System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ")");
					// System.out.println("Couldn't retrieve JSON (" + response.getText() + ")");
				}
			}
		});

		// TODO
		// UI cleanup

	}

	@EventHandler
	void onLoginEvent(PermissionsEvent event) {
		if (event.getPermissionsObject().hasPermission(requiredAdminPermissions)) {
			getSuggestions();
		}
	}

}