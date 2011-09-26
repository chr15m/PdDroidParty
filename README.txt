Run your Pure Data patches on Android with native GUIs emulated.

http://mccormick.cx/projects/PdDroidParty

Running your patches
--------------------

See the website for instructions.

Building
--------

Run these on a fresh checkout:

	android update project --path .
	ant install

See the website for more details.

You can push the existing test suite and the demos to your device:

	./push-tests-to-sdcard
	./push-demos-to-sdcard

Copyright
---------

Copyright Chris McCormick, 2011

GPLv3 Licensed - see LICENSE for details.

Contains libpd by Peter Brinkmann and Pure Data by Miller S. Puckette
They are BSD.

