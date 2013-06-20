UCD-Events
==========

Watches Facebook pages for events.

It has a list of Facebook pages related to University College Dublin (UCD) which it checks
hourly for events they have created or posted to their walls. 

It then presents the list of upcoming events at:
http://apps.facebook.com/ucdevents/


This app has been written in Java to run on Google App Engine.
It relies on GSON and Objectify.


To do:
. Run the FB API calls asynchronously
. Move the list of pages to the datastore and add a management UI
