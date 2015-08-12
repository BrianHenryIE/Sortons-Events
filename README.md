Sortons Events
==============

Sortons, from the verb sortir (/soɾˈtiɾ/) to go out. Nous sortons – we are going out.

Creates lists of events created or posted by lists of Facebook pages.

App to be added as a Facebook page tab and a list of other Facebook pages be specified which are queried by cron for events they have created or posted.

The notable tech used:
* Java
* Google App Engine
* Google Cloud Endpoints
* Google Web Toolkit
* Maven
* Facebook API
* Bing Maps

You'll also have pull [GwtFB+](https://github.com/BrianHenryIE/GwtFBplus) and [GwtBingMaps](https://github.com/BrianHenryIE/GwtBingMaps) and add them to your local Maven repository. If you're actually going to do that, you should probably get in touch with me to make sure all the latest versions have been pushed to GitHub.



Run with
mvn gwt:run
This doesn't do hot code reloading, as mvn appengine:devserver does, so maybe there's a better way.

Upload with
mvn clean
mvn appengine:update

Local datastore at:
localhost:8080/_ah/admin