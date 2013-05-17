## Auction System
Authored by Chris Watts

### Building
An ANT build file has been given for both compiling and making a runnable JAR.

### Usage
Upon running `main` found in `AuctionSystem`, you should see the main dialog with a navigation panel and a display panel.
Clicking entries in the navigation panel will bring up their full details on the display panel. You can also add search filters that update automatically, and switch to items you are selling and items you are buying.

If `main` is run from the command line with the `-server` flag, server mode is started. The server can be stopped from either the GUI stop button or typing 'stop' into the console.

### Specification
Original spec is (was) found here: https://secure.ecs.soton.ac.uk/notes/comp1206/assn2-2013.html

#### Extensions
The following extensions have been made past the specification:

  * Java Sockets are in use in the Communication Layer
  * Passwords are pre-hashed with a salt, post-hashed with another salt and key-stretched.
  * Secure sessions are implemented (they may time out after 10 minutes of inactivity)
  * Items can have display images, but why would you want to replace my nice little auction hammer 3D render?
  * Text fields have placeholder text
  * Spinner controls work for date selection and currency
  
#### Known caveats

  * Messages are not encrypted! There was simply not enough time to implement AES/RSA combination streams unless I made a utility class to encrypt Messages. But that's not the point of streams, is it?
  * Auction list glitches about on some systems when you switch tabs. Some items may reshow on other tabs. If it happens, switch back and forward again.
  
  * ~ NOT A CAVEAT ~ Closing the GUI in server mode does not shut the server down. To close the server, please type 'stop' into the command console.