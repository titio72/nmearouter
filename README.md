I created the application for a very simple reason: I wanted to use it on my boat.
At the beginning I just wanted to grab data from my boat sensors and push them on WiFI (without spending a fortune in pre-built products), then the thing got out of hand.

Bottom line is that the thing is feature-rich and the design should be reasonable well thought out but testing is legging behind, refactoring as well etc.
Assuming you are a developer is the kind of thing you always complain about your company's software.

If you really want to check it out mind that I wrote some parts during some tough crossing.
It's a sort of eXtreme programming but the extreme part is about coding while heeling 30 degrees and being bounced by waves.

The good thing though is that the software runs successfully on my boat (yes... certified to work on my machine - you heard that before, right?)

The system is organized as container application running "agents". Agents come in different flavors and they can manage serial port input/output, I2C sensors like magnetometers/gyro, log position/meteo on a database etc.
The configuration is through a xml file (which is nasty) so I ended up adding a web interface which is evolving in a navigation dashboard.
To manage the web interface I embedded Jetty (cheap, quick...).
The feature I like more is the "trips" management - if want to know about my last trip to Corsica I can do it and I can see the map (Thanks to Google), the track, statistics about speed and wind and a few other things.
Something I have to say about web interface is that now I respect UI developers a lot more - how the hell can you do that for a living!

The other reason why I wrote this software is to support the development of my other "boating" software: https://play.google.com/store/apps/details?id=com.aboni.boatinga