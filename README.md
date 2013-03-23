Run your Pure Data patches on Android with native GUIs emulated.

http://droidparty.net/

Building
--------

Run these on a fresh checkout to get the libpd dependencies and get set up:

	# get the latest version of pd-for-android
	git submodule init
	git submodule update
	# get pd-for-android's own submodules
	cd pd-for-android
	git submodule init
	git sudmodule update
	# make the PdCore component build with ant
	cd PdCore
	android update project --path .
	# now go back to the top level and configure out project
	cd ..
	cd ..
	android update project --name PdDroidParty --path .

Then to build simply:

	ant debug install

You can push the existing test suite and the demos to your device's sdcard using adb:

	./push-tests-to-sdcard
	./push-demos-to-sdcard

Copyright
---------

Copyright Chris McCormick, 2011-2013.

With contributions from: Kishan Muddu, Antoine Rousseau.

GPLv3 Licensed - see LICENSE for details.

Contains libpd by Peter Brinkmann and Pure Data by Miller S. Puckette
They are BSD.

