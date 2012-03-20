Demo Mode
=========

Free kiosk or demo mode for Android.

Demo Mode is an Android homescreen replacement that lets you deploy devices in
the field without worrying about people being distracted by the other apps or
other features on the phone. A restricted set of apps can easily be placed on
the homescreen and protected by an unlock code.

Provisioning
------------

One device can be configured and its configuration can be shared to provision
multiple devices by way of a QR code. This allows for provisioning in
environments without a network connection and without the need for a central
provisioning server.

Demo Mode and the [ZXing barcode reader][zxing] must first be installed on each
device to use this functionality.

Disclaimer
----------

While Demo Mode makes every attempt to restrict the device, Android is an open
ecosystem and individual devices will be more or less possible to restrict. For
example, HTC's Sense UI has a bug in it that make it possible to get at a
standard homescreen when the device reboots. This is not something that one can
easily workaround, but is also not something that a person using the device
for a short period of time would commonly encounter.

This tool is designed to help steer the focus of the device's usage toward a
single set of applications and is not designed to be an entirely foolproof
lock-down mechanism.

License
-------
Android Demo Mode  
Copyright (C) 2012 [MIT Mobile Experience Lab][mel]

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

[zxing]: http://code.google.com/p/zxing/
[mel]: http://mobile.mit.edu/
