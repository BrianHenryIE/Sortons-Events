Sortons-Events
==============

Creates lists of events created or posted by lists of Facebook pages.

App to be added as a Facebook page tab and a list of other Facebook pages be specified which are queried by cron for events they have created or posted.

This app has been written in Java (Google Web Toolkit) to run on Google App Engine and uses Google Cloud Endpoints.

You'll also have pull [GwtFB+](https://github.com/BrianHenryIE/GwtFBplus) and add it to your local Maven repository.

And GwtBingMaps

Run with
mvn gwt:run-codeserver
mvn appengine:devserver