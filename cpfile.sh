#!/bin/bash
tmpdir=$2
version=$1
echo $1 $tmpdir

if test $version -gt 19;then
	echo "cp v5 v6 file"
	cp $tmpdir/src/com/amlogic/DTVPlayer/widget/CheckUsbdevice-v5-v6.java $tmpdir/src/com/amlogic/DTVPlayer/widget/CheckUsbdevice.java
	cp $tmpdir/src/com/amlogic/DTVPlayer/DTVDeviceBrowser-v5-v6.java $tmpdir/src/com/amlogic/DTVPlayer/DTVDeviceBrowser.java
    cp $tmpdir/src/com/amlogic/DTVPlayer/DTVRecordDevice-v5-v6.java $tmpdir/src/com/amlogic/DTVPlayer/DTVRecordDevice.java

else
	echo "cp v4 file"
    cp $tmpdir/src/com/amlogic/DTVPlayer/widget/CheckUsbdevice-v4.java $tmpdir/src/com/amlogic/DTVPlayer/widget/CheckUsbdevice.java
	cp $tmpdir/src/com/amlogic/DTVPlayer/DTVDeviceBrowser-v4.java $tmpdir/src/com/amlogic/DTVPlayer/DTVDeviceBrowser.java
    cp $tmpdir/src/com/amlogic/DTVPlayer/DTVRecordDevice-v4.java $tmpdir/src/com/amlogic/DTVPlayer/DTVRecordDevice.java
fi