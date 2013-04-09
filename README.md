Android-OSGi-Spike
==================

Purpose
-------

This is a technology spike for an Android application with browser-based UI and underlying [OSGi architecture](http://www.osgi.org/Technology/WhyOSGi).

It served as a feasibility study for migrating an existing, commercial Java application to the Android platform.  

Goals 
-----

The primary goal was to demonstrate deployment of modular, dynamically composed applications on Android tablets. 

A secondary goal was to demonstrate that the development team's existing skill set can be applied to the Android environment with minimal retraining. Our current applications have browser-based user interfaces, generated with [Google Web Toolkit](https://developers.google.com/web-toolkit/), and accessing relational databases via [Hibernate](http://www.hibernate.org/). Builds are automated via [Maven](http://maven.apache.org), and [Log4J](http://logging.apache.org/) is used extensively.

The technology spike was expected to show:

- the ability to embed, launch, and manage an OSGi container within an Android APK;

- build processes that automatically generate bundles for both the JVM and Dalvik from a single source, without manual intervention;

- interaction with the Android application via the tablet's web browser.

To date, these goals have been partially achieved. Database access has not been attempted as a lightweight alternative to Hibernate remains to be investigated. Google Web Toolkit integration is also pending.

Approach
--------

[Apache Felix](http://felix.apache.org) was selected due to its small footprint and [previously demonstrated](http://felix.apache.org/site/apache-felix-framework-and-google-android.html) deployment on Android.

To preserve a common logging capability on both Android and Java, [SLF4J](http://www.slf4j.org) was selected. Android's standard logging/debugging capabilities are exposed to SLF4j via [logback-android](http://tony19.github.io/logback-android/). Logs are written to logcat and to the SD card.

To serve the application's web interface, Felix's standard [HTTP Service](http://felix.apache.org/documentation/subprojects/apache-felix-http-service.html) implementation is deployed.

Builds are fully automated using Maven 3. 

Building the system
-------------------

In the hello-world folder, build the demonstration servlet by running:

    mvn clean install
    
Then plug your phone/tablet into the USB port, switch to the android-osgi-spike folder, and run:

    mvn clean install android:deploy
    
This builds the code, converts the included bundles to Dalvik format, assembles the APK, and deploys it to the device.

Running the application
-----------------------

- launch the "OSGi Test" application
- start the embedded OSGi server
- click either button to launch the Felix web console or the Hello world servlet in the Android browser

Discussion
----------

- Multiple warnings about ignoring InnerClasses attribute during the build are due to Felix jars targeting older Java versions. These have no runtime impact.
- various other issues and rationale behind the approaches used are discussed in source code comments

Limitations / To-dos
--------------------

- Testing a GWT-driven HTML UI on Android is the most pressing item remaining for this proof of concept.

- I'd eventually like to demonstrate downloading and installing a bundle from a remote web site at runtime, as this is an important use case for our future development.

- The demonstration servlet was written before I learned about Declarative Services. I'd like to upgrade it to use DS annotations at some point.

- It's a waste of time running the Dalvik conversion on every build. The build process can definitely be improved to eliminate this.

- There's probably a more elegant way to identify and include the standard bundles as Android assets than the ANT script used here. It would be nice to identify them once in the POM rather than repeating them again in build.xml. A Maven plugin for the DX compiler could be useful here.

- I'd like to strip the Java class files from the Dalvik bundles to save space, but Felix (as of v4.1.0) doesn't like that - it checks the presence of the .class file in the bundle, even though it won't load it. I haven't checked if that's still the case in v4.2.1. A patch for this would be valuable.

- The BundleProvider / SimpleBundleProvider / AndroidBundleProvider hierarchy is overkill if you only care about running on Android. I went to the trouble because I wanted to keep the framework launcher common to both Dalvik and Java. As structured, you could move classes containing Android-specific code into a separate JAR and keep a "pure" runtime for either platform. That level of sophistication wasn't needed for this proof-of-concept.

android-osgi-spike
==================

Technology demonstrator for building and deploying OSGi-based apps with browser-based user interfaces on Android.