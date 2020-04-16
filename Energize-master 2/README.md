# Energize - Monitor your Battery
This application is designed to monitor your battery state and provide useful information about the current charging level, the estimated time to charge or discharge and some more useful information.

## Download a pre-build version
You can download a pre-build version of Energize from the [Google Play][8] store.

## Build the application with [Eclipse][7]
For building the application by yourself follow the following steps:

  1. Clone the application repository

          git clone https://github.com/thuetz/Energize.git

  2. Change into the cloned directory

          cd Energize

  3. Initialize and update the submodules

          git submodule init
          git submodule update

  4. Replace the Android Support Library used in the `ViewPagerIndicator` submodule (directory libs/) with the latest version of the Android Support Library (at least version 10).

  5. Create an Android project for each submodule using the `Android Project from Existing Code` function of Eclipse
  6. Create an Android project for the actual application (using the `Android Project from Existing Code` function of Eclipse)
  7. Build the application :)

### Updating the used submodules
In some cases you might want to use a newer version of one of the submodule the application is using. For doing this follow the upcoming steps:

  1. Go into the folder of the submodule you want to update
  2. Pull the changes from their repository by typing:

          git pull

  3. (Just for developers): Adjust the reference to the new version of the library by typing:

          TODO

## Translations
As soon as the used strings is nearly fixed, you can help us on our project page of [GetLocalization][1] to translate the app in many languages.

## Application icon
The application icon is based upon the [Android Robot][3] logo made by Google Inc. This logo is licensed under the terms of the [Creative Commons Attribution license][4]. The battery icon was designed by the authors of the [GNOME High contrast icon set][5] and is licensed under the terms of the [GNU Lesser General Public License][6].

## Changelog
### Version 2.3 (Code: *230*, Released on: ***2018-04-XX***) - [Changes][42]
* Fix the bug that the animation in landscape mode hid all relevant information (issue no. 45)
* Fix the bug that when changing the device orientation the current menu selection was lost (issue no. 46)
* Update the used support libraries

### Version 2.2.1 (Code: *221*, Released on: ***2018-01-09***) - [Changes][41]
* Update the app to the latest Android 8.1 release
* Update the used support libraries

### Version 2.2 (Code: *220*, Released on: ***2017-09-09***) - [Changes][40]
* Update the app to the latest Android 8.0 release
* Add information about used OSS libraries
* Updated the library for drawing the graphs
* Add the TIMBER logging library

### Version 2.1.1 (Code: *211*, Released on: ***2017-06-16***) - [Changes][39]
* Fix an ArrayOutOfBounds error

### Version 2.1 (Code: *210*, Released on: ***2017-06-16***) - [Changes][38]
* Fix the back arrow in the settings categories to go back to the correct page
* Fix the axes of the battery graph to always show 0% - 100%
* Fix the logic to as the user for the required permissions
* Add app shortcuts for devices running Android 7.1 and higher
* Add a new animation to the main activity representing the current battery state
* Add the missing 'About this app' dialog in the settings
* Refined the used colors for the theme of the app
* Redesigned the application icon according to the new color scheme
* Minified the build APK

### Version 2.0.1 (Code: *203*, Released on: ***2017-06-07***) - [Changes][37]
* Fix a bug which caused that the battery estimator was not set correctly on initial install
* Update the license file

### Version 2.0 (Code: *200*, Released on: ***2017-06-06***) - [Changes][36]
* Switched to the latest version of the Android build tools and SDKs
* Updated the design of the app to Material Design
* Removed a lot of old and now unused resources
* Remove permissions which are not required anymore
* Lowered the required Android version to Android 4.1 (Jelly Bean)

### Version 1.0 (Code: *112*, Released on: ***2014-06-12***) - [Changes][35]
* Complete redesign of the application
* Removed the special functions for ASUS Tablets
* Fixed bugs which caused an application crash
* Changed the database access method for better performance
* Removed a lot of old code and dependencies to 3rd party libraries
* A few optimizations for Android 4.4

### Version 0.9.2 (Code: *106*, Released on: ***2013-11-04***) - [Changes][34]
* Updated the GraphView library from version 3.0 to version 3.1
* The app is now build against the Android 4.4 SDK
* Fixed some possible app crashes

### Version 0.9.1 (Code: *102*, Released on: ***2013-07-28***) - [Changes][33]
* Modified the code-base to be compiled with the Android Studio
* Updated the used version of the ViewPagerIndicator library

### Version 0.9 (Code: *97*, Released on: ***2013-05-21***) - [Changes][32]
* Added support for the DashClock widget
* Fixed a bug which caused that the fragments were not updated if the application was paused and then resumed
* Fixed some minor performance issues

### Version 0.8.9 (Code: *89*, Released on: ***2013-04-11***) - [Changes][31]
* Added support for Kelvin as a temperature in the drawn graph (requested feature)
* Updated the library which is used for drawing the graphs
* Minor bug fixes for the backup agent

### Version 0.8.8 (Code: *88*, Released on: ***2012-12-26***) - [Changes][30]
* Added a semi-downgrade option for the database to fix an application crash

### Version 0.8.7 (Code: *87*, Released on: ***2012-12-05***) - [Changes][29]
* Made use of the ACRA library for bug reporting
* Switched to a new process for maintaining the change log
* Fixed a bug which caused that the user could click on the widgets only once after the device has booted

### Version 0.8.6 (Code: *86*, Released on: ***2012-11-20***) - [Changes][28]
* Fixed a bug which caused that the widget was not updated if the status bar notification was disabled

### Version 0.8.3 - 0.8.5 (Code: *83,84,85*, Released on: ***2012-11-16***) - [Changes][27]
* Added support for lock screen widgets on Android 4.2+
* Added support for high-resolution devices (like the Nexus 10)
* Added a new estimation algorithm which is a bit better than the old one (but still not accurate enough)
* Fixed a smaller bug in the old estimation algorithm
* Fixed a bug that the widget was requesting more space then required
* Removed the option to turn-off the automatic service start after the device has finished booting (because it is essential)
* Removed the theme support because it was buggy not not really useful
* Fixed two bugs which caused the application to crash with a null-pointer exception

### Version 0.8.2 (Code: *82*, Released on: ***2012-11-07***) - [Changes][26]
* Fixed a bug which caused an application crash on tablets while pressing the lock-button
* Changed the icon of the lock-button
* Optimized the layout for 7 inch tablets (like the Nexus 7)
* Optimized the layout of the "What's new?" dialog

### Version 0.8.1 (Code: *81*, Released on: ***2012-11-01***) - [Changes][25]
* Fixed a bug which caused an immediate application crash on the Nexus 7 when the application was closed by hitting the back key

### Version 0.8.0 (Code: *80*, Released on: ***2012-11-01***) - [Changes][24]
* The remaining time will now also be displayed on the main activity of the application
* Updated the code to query the service for each battery depending question (remaining time, time on battery, etc.)
* Added a first (but still ugly) widget
* Added an option to lock the app to the current screen to be able to scroll through the battery graphs
* Fixed a bug that not all tables in the database were cleared when requested
* Added an option to copy the database to the SD card of the device

### Version 0.7.3 (Code: *73*, Released on: ***2012-10-19***) - [Changes][23]
* Since a bug which caused an application crash while accessing the preferences on a device running an Android version below 4.0 cannot be fixed easily we had to drop Android 3.1 - 3.2 support again. SORRY guys!
* Changed the text of some labels
* Fixed a bug which caused that no notification icon was displayed

### Version 0.7.2 (Code: *72*, Released on: ***2012-10-08***) - [Changes][22]
* Added support for Android 3.1 and higher
* Updated the version of the used GraphView library
* Updated the version of the used ViewPagerIndicator library
* Fixed a minor bug which caused sometimes the application to crash

### Version 0.7 (Code: *70*, Released on: ***2012-09-19***) - [Changes][21]
* Basic support for Asus Transformer Pad Prime/TF300/Infinity dock battery capacity (showing the current capacity; not used for estimation)
* Added a basic first version of a separate tablet layout which uses the display more efficiently
* Rewrote the architecture for estimating the remaining time on battery to support more sophisticated algorithms in the future
* On Jelly Bean (Android 4.1) devices the status message in the notification bar gets an higher priority as soon as the capacity decreases below 15%
* Fixed some design issues on devices with a smaller display
* Fixed several smaller performance issues detected by an code analysis via Lint
* Fixed a bug which caused that sometimes the "no estimation available" message was displayed even if it was possible to estimate the remaining time
* Extended the statistics database with a table which logs the AC plugging events (power cord plugged in or unplugged)
* Added a label to the overview which displays the time the device is on battery power

### Version 0.6.4 (Code: *64*, Released on: ***2012-08-27***) - [Changes][20]
* Disabled the background color of the battery capacity graph (it caused a graphical glitch)
* The battery capacity graph and the temperature graph will just show the last 24 hours of the logged information
* Done an automated code cleanup to be more resource efficient

### Version 0.6.3 (Code: *63*, Released on: ***2012-08-22***) - [Changes][19]
* Fixed a bug which caused an application crash while rotating the view
* Fixed a bug which caused that the battery temperature was not logged
* Added a tab where a temperature graph is displayed
* Improved the design of the battery capacity graph

### Version 0.6 (Code: *60*, Released on: ***2012-08-19***) - [Changes][18]
* The main screen shows the temperature of the battery
* Added an option to chose between Celsius and Fahrenheit
* Added several pages to the main screen to clean up the UI and prepare for a Tablet layout
* Resized the graph to make it better readable
* Updated the themes to optimize the look of the app
* Cleaned up the preference of the app
* Done a important code cleanup to remove unused code, optimize the speed and the power consumption of the app and make it easier to implement new features

### Version 0.5 (Code: *50*, Released on: ***2012-08-12***) - [Changes][17]
* Redesigned the main screen of the application
* Changed the way battery change information are logged
* Updated the statistics database to store the battery temperature
* The main screen will show a graph about the charging and discharging process of the battery
* Done some more internal code cleanup

### Version 0.4.1 (Code: *41*, Released on: ***2012-08-10***) - [Changes][16]
* Added license information for the application icon
* Applied a fix for the -1 estimation bug
* Fixed the bug that clicking the notification item won't show the main activity
* Added support for API level 14 (which includes plain Android 4.0.0)
* The notification bar item will now show if the battery is charges or discharges

### Version 0.4 (Code: *40*, Released on: ***2012-08-07***) - [Changes][15]
* Basic code cleanup
* Cleanup of the preference screens
* The remaining time will show hours and minutes instead of just minutes
* Added an option to disable the automatic startup of the boot service

### Version 0.3 (Code: *30*, Released on: ***2012-08-05***) - [Changes][14]
* Added the first (but simple) remaining time estimation
* Added the license text to the application (see About settings)
* Updated the application icon
* First version which is available on the Google Play store for everyone
* Prepared the code to be able to handle in-app billing stuff (for easy donations)

### Version 0.2 (Code: *20*, Released on: ***2012-07-13***) - [Changes][13]
* Added a preference dialog for clearing the obtained battery statistics
* Added a status bar indicator for the current charging level of the battery
* Added a "What's new" dialog on the first startup of a new version

### Version 0.1.3 (Code: *13*, Released on: ***2012-07-03***) - [Changes][12]
* Changed the layout of the battery statistics database

### Version 0.1.2 (Code: *12*, Released on: ***2012-07-02***) - [Changes][11]
* Fixed a bug which caused that the preferences were not accessible
* Added a first debug option to send the battery statistic database via mail
* Updated the infrastructure to establish communication between the service and the app

### Version 0.1 (Code: *10*, Released on: ***2012-06-29***) - [Changes][10]
* Added code to obtain statistics about the battery charging
* Added GPL header to all source files
* Moved all strings to the resource file (for supporting localization)

### Version 0.0.2 (Code: *2*, Released on: ***2012-04-22***) - [Changes][9]
* Added a preference dialog for changing the used theme of the app
* Added an dialog to show information about the application

### Version 0.0.1 (Code: *1*, Released on: ***2012-04-17***)
* Initial version which is available in the Google Play Store

 [1]: http://www.getlocalization.com/energize
 [2]: http://www.mentalrey.it
 [3]: http://developer.android.com/distribute/googleplay/promote/brand.html
 [4]: http://developer.android.com/license.html#attribution
 [5]: http://commons.wikimedia.org/wiki/GNOME_High_contrast_icons/authors
 [6]: http://www.gnu.org/licenses/lgpl.html
 [7]: http://www.eclipse.org/
 [8]: https://play.google.com/store/apps/details?id=com.halcyonwaves.apps.energize
 [9]: https://github.com/thuetz/Energize/compare/v0.0.1...v0.0.2
 [10]: https://github.com/thuetz/Energize/compare/v0.0.2...v0.1
 [11]: https://github.com/thuetz/Energize/compare/v0.1...v0.1.2
 [12]: https://github.com/thuetz/Energize/compare/v0.1.2...v0.1.3
 [13]: https://github.com/thuetz/Energize/compare/v0.1.3...v0.2
 [14]: https://github.com/thuetz/Energize/compare/v0.2...v0.3
 [15]: https://github.com/thuetz/Energize/compare/v0.3...v0.4
 [16]: https://github.com/thuetz/Energize/compare/v0.4...v0.4.1
 [17]: https://github.com/thuetz/Energize/compare/v0.4.1...v0.5
 [18]: https://github.com/thuetz/Energize/compare/v0.5...v0.6
 [19]: https://github.com/thuetz/Energize/compare/v0.6...v0.6.3
 [20]: https://github.com/thuetz/Energize/compare/v0.6.3...v0.6.4
 [21]: https://github.com/thuetz/Energize/compare/v0.6.4...v0.7
 [22]: https://github.com/thuetz/Energize/compare/v0.7...v0.7.2
 [23]: https://github.com/thuetz/Energize/compare/v0.7.2...v0.7.3
 [24]: https://github.com/thuetz/Energize/compare/v0.7.3...v0.8
 [25]: https://github.com/thuetz/Energize/compare/v0.8...v0.8.1
 [26]: https://github.com/thuetz/Energize/compare/v0.8.1...v0.8.2
 [27]: https://github.com/thuetz/Energize/compare/v0.8.2...v0.8.5
 [28]: https://github.com/thuetz/Energize/compare/v0.8.5...v0.8.6
 [29]: https://github.com/thuetz/Energize/compare/v0.8.6...v0.8.7
 [30]: https://github.com/thuetz/Energize/compare/v0.8.7...v0.8.8
 [31]: https://github.com/thuetz/Energize/compare/v0.8.8...v0.8.9
 [32]: https://github.com/thuetz/Energize/compare/v0.8.9...v0.9
 [33]: https://github.com/thuetz/Energize/compare/v0.9...v0.9.1
 [34]: https://github.com/thuetz/Energize/compare/v0.9.1...v0.9.2
 [35]: https://github.com/thuetz/Energize/compare/v0.9.1...v1.0
 [36]: https://github.com/thuetz/Energize/compare/v1.0...v2.0
 [37]: https://github.com/thuetz/Energize/compare/v2.0...v2.0.1
 [38]: https://github.com/thuetz/Energize/compare/v2.0.1...v2.1
 [39]: https://github.com/thuetz/Energize/compare/v2.1...v2.1.1
 [40]: https://github.com/thuetz/Energize/compare/v2.1.1...v2.2
 [41]: https://github.com/thuetz/Energize/compare/v2.2...v2.2.1
 [42]: https://github.com/thuetz/Energize/compare/v2.2.1...v2.3
