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

if test $SDK_VERSION -gt 19;then
        echo sdk_vserion:$SDK_VERSION
        SHARE_USER_ID=android.uid.media
else
        SHARE_USER_ID=android.uid.system
fi

cat $1 | sed s/android:versionName=\"1.0\"/android:versionName=\"$APKVERSION\"/ | sed s/android:sharedUserId=\"android.uid.system\"/android:sharedUserId=\"$SHARE_USER_ID\"/ >$2
