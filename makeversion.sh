#!/bin/bash

DVBDIR=$ANDROID_BUILD_TOP/external/dvb

if test -d $DVBDIR; then
	pushd $DVBDIR
	DVBVERSION=`git show HEAD|grep ^commit|sed 's/^commit //'`
	popd
fi

pushd $LOCAL_PATH
DTVPLAYERVERSION=`git show HEAD|grep ^commit|sed 's/^commit //'`
popd

APKVERSION=$DVBVERSION-$DTVPLAYERVERSION
echo version: $APKVERSION

cat $1 | sed s/android:versionName=\"1.0\"/android:versionName=\"$APKVERSION\"/ > $2

