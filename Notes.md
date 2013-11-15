## Current Tasks

### Check if there is internet connection before logging in!

### Better Information on connection info
* Try showing the username, and URL of the connection in addition to the name
### Validate whether user is connecting to an actual Formulize server

### Support Edit/Delete Connections

### Refactor how asynchronous connections are done
* Create Handler class that Activities could pass themselves into
* Handlers can be passed to asynchronous calls, so once they are complete, they could pass the results into handlers
* Handlers will basically handle the message and manipulate the activity context as needed

### Check for servers that don't have any applications available to the user
* If this happens, politely escort them back to the connection menu, and ask them to contact the webmaster.

### When the user re-opens the application, log back in to their last application

### Cache the menu links retrieved from the server for better performance
* When the user logs in, it should first query the contents of the cache for the list of applications
* Then retrieve the results from network asynchronously, and update the list if there are changes compared to the cache.

## Completed Tasks
* Have a activity for setting up multiple Formulize sites:
	* URL
	* Name
	* Options
	* Usernames, Passwords

### Have working login workflow
* Implement Method to retrieve connections selected from the list
	* When there is now username specified in the login connection, ask for login
	* Otherwise login automatically
	* If there is a bad password/username, prompt for login credentials again

### Create a new PHP file, a new GET handler that returns a JSON/XML representation of the application information on the server.
* This is done with the `app_list.php` file on the server side code.
* The file uses the application_handler object, it basically strips out the unnecessary information from xoops objects and encodes it in JSON.

### Getting user login token from a Formulize Server

In the first Prototype, this was done by using a `WebView`. However, this shouldn't be done anymore since we don't need to load an entire web browser to log in users. 

Simpler HTTP Requests should be made instead. This way it also allows us to get the session token more easily, and detect whether a login has been successful or not.

In Android `HttpURLConnection` is used to [make network requests](http://developer.android.com/reference/java/net/HttpURLConnection.html). 

`HttpUrlConnection`'s `CookieManager` stores any cookies given by the response header at Login (given by `Set-Cookie` header). When a `WebView` is initiated in the `ScreenWebActivity`, it takes all the cookies collected by `HttpUrlConnection` and transfers it to `WebView`'s own `CookieManager`. That way `WebView` would have the session cookies to access the screens requested by the user.

#### Things to consider
* Is there an alternative entry point that mobile devices could use to login and get a token from the Formulize server?
* How to deal with network that redirect users to a sign-in page?
* Currently all testing is done on Formulize running on ICMS, does the sign in process need to change on Joomla, Wordpress etc.?
* It seems like ICMS sets a ICMSSESSION cookie even when the user is not logged in. When I login directly with `HttpURLConnection`, there's I get two `ICMSSESSION` cookies, presumably one for the login, the other for the "unlogged" one.
* When should the list be refreshed once retrieved?

### Create input validations for adding connections

## Things to be done later

### Android AsyncTask and LoginTask
To handle asynchronous tasks such as network calls, Android encourages the use of `AsyncTask` [in its library](https://developer.android.com/reference/android/os/AsyncTask.html). However, the Android Application Lifecycle does not automatically preserve asynchronous tasks when an activity is destroyed (e.g. when the user changes screen orientantion) hence `AsyncTask` objects need to be attached to [Android Fragments](https://developer.android.com/guide/components/fragments.html). Android allows some Fragments to be retained so it can bypass Android's destroy-create cycle, so AsyncTasks can be preserved by being attached to them. [(Source)](http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html)

`LoginTask` should be implemented under this fashion. Currently if you rotate the screen while the system is logging in, the application will crash!

* Async Login Woes
	* Do not access the UI toolkit outside of the UI thread!
	* How do persist an AsyncTask when there is a runtime configuration change (e.g. screen orientation changed)?
		* http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html

### Android Contextual Action Bar
In Android versions 3.0 or later, the use of the [contextual action bar](https://developer.android.com/guide/topics/ui/menus.html#CAB) is encouraged when the users need to be able to perform actions on particular objects in the application. For our case, that would be editing or deleting connections from a list. However, I realized that Android does not support these feature in their APIs before version 3.0. Since Android 2.2, 2.3 still has ~30% of the [Android market share](https://developer.android.com/about/dashboards/index.html), we should still support these older versions. 

One way is to use [ActionBarSherlock](http://actionbarsherlock.com/). It is an external library that backports some features in newer Android versions. I don't know how much time and effort it may take to use this external library though.

### Hashing Passwords

Must be done if we are to save passwords locally.

### Dealing with Disconnects
When application detects that the session has been lost, reprompt for login.
* There might be a way to know when a session is about to be timed out, application can request a new token when that happens!
