#!/bin/bash

BUILD_TOPDIR=$ANDROID_BUILD_TOP/packages/amlogic/DTVPlayer

CLEAN_PROJECTS=true
CLEAN_ONLY=false
MM_PARAMS='BUILD_DVB_APK=true'
BUILD_UPDATE=false

until [ -z $1 ]
do
	if [ x$1 = 'xnotclean' ]; then
		CLEAN_PROJECTS=false
	elif [ x$1 = 'xclean' ];then
		CLEAN_ONLY=true
	elif [ x$1 = 'xupdate' ]; then
		MM_PARAMS=
		BUILD_UPDATE=true
	fi
	shift
done

function clean {
	cd $BUILD_TOPDIR
	rm -rf .tmp
	rm -rf .update

	if [ $CLEAN_PROJECTS = 'true' ]; then
#		cd $BUILD_TOPDIR/android/test/dvbdemotest
#		mm clean-dvbdemotest
		cd $ANDROID_BUILD_TOP
		
		mm clean-libam_adp clean-libam_mw clean-libjnitvmboxdevice clean-libjnitvdatabase clean-libjnitvdbcheck clean-libjnitvscanner clean-libjnitvsubtitle clean-libjnitvepgscanner clean-libzvbi clean-DTVPlayer clean-TVService
		
		cd $BUILD_TOPDIR
	fi
	return 1
}

if [ $CLEAN_ONLY = 'true' ]; then
	clean
	return 0
fi

cd $BUILD_TOPDIR

mmm ../../../external/libzvbi || clean || return 1

if [ -d ../../../external/dvb ]; then
mmm ../../../external/dvb || clean || return 1
else
mmm ../../../vendor/amlogic/dvb || clean || return 1
fi

mm clean-DTVPlayer
cd ../TvMiddleware; mm ; cd ../DTVPlayer

#cd ../../../external/dvb
#DVBCOMMIT=`git show HEAD | head -1 | grep commit | sed s/commit\ //`
#cd $BUILD_TOPDIR
#COMMIT=`git show HEAD | head -1 | grep commit | sed s/commit\ //`
#sed -i s/versionName=.*/versionName=\"dvb:`echo "$DVBCOMMIT" | cut -c 1-8`\ \ \ \ DVB:`echo "$COMMIT" | cut -c 1-8`\"/ android/test/DVBService/AndroidManifest.xml

mm $MM_PARAMS || clean || return 1




TARGET=android
VERSION=1.0
DATE=`date +%Y.%m.%d-%k.%M|sed s/\ //`

if [ $BUILD_UPDATE = 'true' ];then

PKG=dvb-$TARGET_PRODUCT-$TARGET_BUILD_VARIANT-$VERSION-$DATE.zip

if [ $TARGET_BUILD_VARIANT = 'user' ]; then
	cd $ANDROID_BUILD_TOP/bootable/recovery
	mm
	cd $BUILD_TOPDIR
fi

mkdir .tmp
mkdir .tmp/SYSTEM
mkdir .tmp/SYSTEM/app
mkdir .tmp/SYSTEM/lib
mkdir .tmp/SYSTEM/etc
mkdir .tmp/META

APPS="TVService DTVPlayer"
LIBS="am_adp am_mw jnitvmboxdevice jnitvdatabase jnitvdbcheck jnitvscanner jnitvsubtitle jnitvepgscanner zvbi"
CFG_FILES="tv_default.cfg tv_default.dtd tv_default.xml"

for i in $APPS; do
	cp $ANDROID_BUILD_TOP/out/target/product/$TARGET_PRODUCT/system/app/$i.apk .tmp/SYSTEM/app/
	echo "system/app/$i.apk 0 0 644" >> .tmp/META/filesystem_config.txt
	if [ $TARGET_BUILD_VARIANT = 'user' ]; then
		cp $ANDROID_BUILD_TOP/out/target/product/$TARGET_PRODUCT/system/app/$i.odex .tmp/SYSTEM/app/
		echo "system/app/$i.odex 0 0 644" >> .tmp/META/filesystem_config.txt
	fi
done

for i in $LIBS; do
	cp $ANDROID_BUILD_TOP/out/target/product/$TARGET_PRODUCT/system/lib/lib$i.so .tmp/SYSTEM/lib
	echo "system/lib/lib$i.so 0 0 644" >> .tmp/META/filesystem_config.txt
done

for i in $CFG_FILES; do
	cp $ANDROID_BUILD_TOP/out/target/product/$TARGET_PRODUCT/system/etc/$i .tmp/SYSTEM/etc
	echo "system/etc/$i 0 0 644" >> .tmp/META/filesystem_config.txt
done

$ANDROID_BUILD_TOP/build/tools/releasetools/aml_update_packer .tmp $PKG

else


PKG=AmlogicSTBAndroidApk-$TARGET_PRODUCT-$TARGET_BUILD_VARIANT-$VERSION-$DATE
TPATH=.tmp/$PKG

mkdir -p $TPATH
echo Amlogic Set Top Box > $TPATH/INFO
echo Date: `date` >> $TPATH/INFO
echo Builder: `git config --get user.name` \< `git config --get user.email` \> >> $TPATH/INFO
echo Version: $VERSION >> $TPATH/INFO
echo Target: $TARGET >> $TPATH/INFO
IPADDR=`LANG=en_US ifconfig eth0 | grep inet\ addr| sed s/.*inet\ addr:// | sed s/Bcast.*//`
echo Machine: $IPADDR >> $TPATH/INFO
echo Path: `pwd` >> $TPATH/INFO
echo Branch: `cat .git/HEAD | sed s/ref:\ //` >> $TPATH/INFO
echo Commit: `git show HEAD | head -1 | grep commit | sed s/commit\ //` >> $TPATH/INFO
echo Proguct: $TARGET_PRODUCT >> $TPATH/INFO
echo Variant: $TARGET_BUILD_VARIANT >> $TPATH/INFO

#cp $ANDROID_PRODUCT_OUT/system/app/dvbdemotest.apk $TPATH || clean || return 1

cd .tmp

tar czvf $PKG.tgz $PKG || clean || return 1
cp $PKG.tgz ..

cd ..

fi

clean







