When is the next meeting?
Do we need to keep multiple logins?

## Current Tasks

### Keep login alive while Formulize is open

### Prepare for App Publishing

* Designate master as the release branch and create a dev branch
* Follow steps in the [Publishing Overview](http://developer.android.com/tools/publishing/publishing_overview.html), where the app needs to remove debugging related features.
* Test the software on at least:
	* Api Level 10 Phone (Gingerbread)
	* Api Level 19 Phone (Kitkat)
* High resolution Formulize Icon? Ideally resolutions in:
	* 512 x 512 icon for Play store is required
	* [Guidelines](http://developer.android.com/design/style/iconography.html)
* We don't have to put it up on the Play store, we could load the .api to the Freeform.
* Screen shots, promotional graphics for the Play store

### When the user re-opens Formulize, log back in to their last application
* On every activity, when the application is `onPause()`, save the current activity to the preferences, along with the current connection being used.
* ScreenListActivity needs to be refactored so it doesn't take in a `FormulizeApplicaiton` object. Instead it should be just taking the ConnectionInfo, and application ID
* `applist.php` needs to take additional parameters to just return the screen of a specific application.
* A launcher activity should be added to read from the current preferences, so it returns the proper activity and connection that the user last opened.

* For connections where the username and password are NOT saved, the user should be returned to the connection list when the application has been destroyed

### Use Action Sherlock to add Action Bar support to older devices

### Validate whether user is connecting to an actual Formulize server

### Refactor how asynchronous connections are done
* Create Handler class that Activities could pass themselves into
* Handlers can be passed to asynchronous calls, so once they are complete, they could pass the results into handlers
* Handlers will basically handle the message and manipulate the activity context as needed

### Cache the menu links retrieved from the server for better performance
* When the user logs in, it should first query the contents of the cache for the list of applications
* Then retrieve the results from network asynchronously, and update the list if there are changes compared to the cache.

### Take advantage of Fragments in Android to show a side bar of applications in the screen view on tablets?

### Check if there is internet connection before logging in!

### Handle connection cut offs (e.g. User went to the subway)

## Completed Tasks
* Have a activity for setting up multiple Formulize sites:
	* URL
	* Name
	* Options
	* Usernames, Passwords

### Check for servers that don't have any applications available to the user
* If this happens, politely escort them back to the connection menu, and ask them to contact the webmaster.
* Log out the user

### Add a logout button within the menu, screen
* Create a new Runnable that sends `op=logout` to `user.php`.
* Start it on a separate thread and then go back to the ConnectionActivity screen. 

### Better Information on connection info
* Try showing the username, and URL of the connection in addition to the name

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

### Support Edit/Delete Connections

## Things to be done later

### Change how forms and entry screens are displayed for mobile clients

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