Run your Pure Data patches on Android with native GUIs emulated.

How to make patches
-------------------

 1. Create a new Pd patch called droidparty_main.pd in a subdirectory. This patch will contain your GUI objects like sliders, toggles, numberboxes etc. If you have an existing patch you want to run with PdDroidParty you can create the droidparty_main.pd patch in the same directory.
 2. PdDroidParty GUI patches will be scaled to fit the screen of the device. Your patches should have the rough dimensions of a phone/tablet in landscape mode (e.g. 3:2 aspect ratio or e.g. 480x320 should usually work well). If it's not exact it doesn't matter - the GUI elements will be scaled.
 3. All GUI elements should communicate with the main audio patches using send and receive only. You can usually set send and receive for each GUI by right clicking on the object and choosing 'properties' in Pd. Do not directly connect cables to the GUI elements as they won't work. It helps to keep the GUIs on their own in the droidparty_main.pd file and have it include the logic of your patch as an abstraction or subpatch containing senders and receivers for interfacing with GUI elements. This is good patch design practice anyway as it is basically a model-view-controller methodology.
 4. Copy the entire directory containing your droidparty_main.pd somewhere onto the sdcard of your device. You can usually do this over USB with most phones and tablets by mounting your device as a hard drive on your computer. One good place to copy it is to e.g. /sdcard/pd/MyPdPatch
 5. Run PdDroidParty and you will find an entry named after your subdirectory. Tap it to load your patch.

Copyright
---------

Copyright Chris McCormick, 2011

GPLv3 Licensed - see LICENSE for details.

Contains libpd by Peter Brinkmann and Pure Data by Miller S. Puckette
They are BSD.
