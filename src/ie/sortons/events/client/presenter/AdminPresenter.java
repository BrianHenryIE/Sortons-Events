package ie.sortons.events.client.presenter;

import ie.sortons.events.client.ClientDAO;
import ie.sortons.events.client.LoginController;
import ie.sortons.events.client.appevent.PermissionsEvent;
import ie.sortons.events.client.appevent.ResponseErrorEvent;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphPage;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.kfuntak.gwt.json.serialization.client.Serializer;

public class AdminPresenter implements Presenter {

	interface MyEventBinder extends EventBinder<AdminPresenter> {
	}

	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	Serializer serializer = (Serializer) GWT.create(Serializer.class);

	private final ClientDAO dao;

	private final EventBus eventBus;
	private final Display display;

	private String requiredAdminPermissions = "";

	public interface Display {
		TextBox getAddPageTextBox();

		HasClickHandlers getAddPageButton();

		HasClickHandlers getLoginButton();

		void setIncludedPages(List<FqlPage> includedList);

		void setSuggestedPages(List<FqlPage> suggestionsList);

		void setPresenter(AdminPresenter presenter);

		Widget asWidget();
	}

	public void bind() {

		eventBinder.bindEventHandlers(this, eventBus);

		display.getAddPageButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				processTextBox();
			}
		});

		display.getLoginButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				LoginController.login(requiredAdminPermissions);
			}
		});

		display.getAddPageTextBox().addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				searchSuggestions();
			}
		});

	}

	public AdminPresenter(EventBus eventBus, final ClientDAO dao, Display view) {
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

	private void getClientPageData() {
		dao.refreshClientPageData(this);
	}

	public void displayClientData(ClientPageData clientPageData) {
		display.setIncludedPages(dao.getClientPageData().getIncludedPages());

		if (display.getAddPageTextBox().getText().trim().length() > 0) {
			searchSuggestions();
		} else {
			getSuggestions();
		}
	}

	private void getSuggestions() {
		dao.getSuggestions(this);
	}

	public void setSuggestions(List<FqlPage> suggestionsList) {
		if (suggestionsList != null) {
			display.setSuggestedPages(suggestionsList.subList(0, Math.min(10, suggestionsList.size())));
		}
	}

	private void searchSuggestions() {
		String searchText = display.getAddPageTextBox().getText().toLowerCase();
		if (searchText.trim().length() > 0 && !searchText.toLowerCase().contains("http:") && !searchText.toLowerCase().contains("www.")) {
			List<FqlPage> search = new ArrayList<FqlPage>();
			for (FqlPage page : dao.getSuggestions()) {
				boolean add = true;
				for (String term : searchText.split(" ")) {
					if (!page.getName().toLowerCase().contains(term) && !page.getLocation().friendlyString().toLowerCase().contains(term)) {
						add = false;
					}
				}
				if (add == true || page.getPageId().toString().contains(searchText)
						|| page.getLocation().friendlyString().toLowerCase().contains(searchText)) {
					search.add(page);
				}
			}
			setSuggestions(search);
		} else {
			getSuggestions();
		}
	}

	private void processTextBox() {

		// Get the text from the textbox
		// regex it to a page_id
		// get the page's basic details
		// send it to the server
		// add it to the included pages list
		// get the new page's likes to add to suggestions.

		// Get the text that has been entered and build the graph call

		String textEntered = display.getAddPageTextBox().getText();
		String graphPath = "/";

		// Get rid of anything after ?
		// http://www.facebook.com/pages/The-Comedy-Crunch/83791357330?ref=ts&fref=ts
		if (textEntered.contains("?"))
			textEntered = textEntered.substring(0, textEntered.indexOf("?"));

		// Sometimes http://www.facebook.com/pages/Randals-Rest-UCD/107542286070006
		if (textEntered.matches(".*facebook\\.com/pages/[^/]*/\\d*/?")) {

			graphPath += textEntered.split(".*facebook\\.com/pages/[^/]*/")[1].replace("/", "");

			// Sometimes http://www.facebook.com/UCD.Alumni?ref=stream&hc_location=stream
		} else if (textEntered.matches(".*facebook\\.com/[^/]*/?")) {

			graphPath += textEntered.split(".*facebook\\.com/")[1].replace("/", "");

			// Sometimes 107542286070006
		} else if (textEntered.matches("\\d*")) {

			graphPath += textEntered;

		} else {
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

				FqlPage newPage = new FqlPage(pageDetails.getName(), pageDetails.getLink(), pageDetails.getId());

				addPage(newPage);

				// TODO
				// This should only empty when it's successful
				display.getAddPageTextBox().setText("");

			}

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				System.out.println("ERROR processTextBox");
			}
		});

	}

	public void addPage(FqlPage newPage) {

		System.out.println(serializer.serialize(newPage));

		dao.addPage(newPage, new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				System.out.println("Couldn't retrieve JSON");
			}

			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {

					FqlPage page = (FqlPage) serializer.deSerialize(response.getText(), "ie.sortons.gwtfbplus.shared.domain.fql.FqlPage");

					// TODO return a real error message
					if (page.getPageId() != null) {
						dao.getClientPageData().addPage(page);

						// then update UI
						displayClientData(dao.getClientPageData());
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
				}
			}
		});
	}

	public void ignorePage(FqlPage page) {

		dao.ignorePage(page, new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				System.out.println("Couldn't retrieve JSON: ignorePage/onError");
			}

			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {

					FqlPage page = (FqlPage) serializer.deSerialize(response.getText(), "ie.sortons.gwtfbplus.shared.domain.fql.FqlPage");

					dao.getClientPageData().ignorePage(page);

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