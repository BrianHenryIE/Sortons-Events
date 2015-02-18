package ie.sortons.events.client;

import ie.sortons.events.client.appevent.LoginEvent;
import ie.sortons.events.client.appevent.NotLoggedInEvent;
import ie.sortons.events.client.appevent.PermissionsEvent;
import ie.sortons.gwtfbplus.client.api.FBCore;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

public class LoginController {

	private static FBCore fbCore = GWT.create(FBCore.class);

	interface MyEventBinder extends EventBinder<LoginController> {}
	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	
	private static EventBus eventBus;

	
	LoginController(EventBus eventBus) {
		LoginController.eventBus = eventBus;
		eventBinder.bindEventHandlers(this, eventBus);

		// Check login status. 
		getLoginStatus();
	}


	@EventHandler
	void onLoginEvent(LoginEvent event) {
		System.out.println("LoginEvent seen by Login Controller");

		if(event.getLoginObject().isConnected() == false) {
			System.out.println("We are not connected");

			eventBus.fireEvent(new NotLoggedInEvent());

		} else {
			System.out.println("We are connected");

			// check what permissions we've got
			fbCore.api ( "/me/permissions", new AsyncCallback<JavaScriptObject>(){
				public void onSuccess ( JavaScriptObject response ) {
					eventBus.fireEvent(new PermissionsEvent(response));
				}
				public void onFailure(Throwable caught) {
					// Print something on the screen about no response from fb.
					throw new RuntimeException ( caught );
				}
			} );
		}
	}


	// I think this checks are we logged in without any UI being displayed,
	public void getLoginStatus() {
		fbCore.getLoginStatus( new AsyncCallback<JavaScriptObject>(){
			public void onSuccess ( JavaScriptObject response ) {
				System.out.println("firing logged in event");
				eventBus.fireEvent(new LoginEvent(response));
			}
			public void onFailure(Throwable caught) {
				// Print something on the screen about no response from fb.
				System.out.println("LoginController.getLoginStatus onFailure");
				throw new RuntimeException ( caught );
			}
		} );

	}


	// Throws up UI for login/permissions
	public static void login(String requiredPermissions) {
		fbCore.login(new AsyncCallback<JavaScriptObject>(){
			public void onSuccess ( JavaScriptObject response ) {
				eventBus.fireEvent(new LoginEvent(response));
			}
			public void onFailure(Throwable caught) {
				// TODO Print something on the screen about no response from fb.
				throw new RuntimeException ( caught );
			}
		}, requiredPermissions);
	}

}
