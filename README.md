Sortons-Events
==============

Creates lists of events created or posted by lists of Facebook pages.

App to be added as a Facebook page tab and a list of other Facebook pages be specified which are queried by cron for events they have created or posted.

This app has been written in Java (Google Web Toolkit) to run on Google App Engine and use Google Cloud Endpoints.

Dependencies:
-------------

• [GSON](https://code.google.com/p/google-gson/) (v2.2.4) for server-side JSON parsing 
• [Objectify](https://code.google.com/p/objectify-appengine/) for datastore operations
• [Guava](https://code.google.com/p/guava-libraries/) for its string utilities
• [Joda-Time](http://www.joda.org/joda-time/) for server-side times/dates
• [GwtFB](https://github.com/olams/gwtfb) for client-side Facebook API calls
• [GwtFB+](https://github.com/BrianHenryIE/GwtFBplus) for Facebook helpers and widgets
• [Apache Commons Codec](http://commons.apache.org/proper/commons-codec/) (v1.5) for Base64 decoding

