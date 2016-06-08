LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := samples

#LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES := $(call all-java-files-under, src/com/amlogic/DTVPlayer/cc)
LOCAL_SRC_FILES += $(call all-java-files-under, src/com/amlogic/DTVPlayer/dvbs)
LOCAL_SRC_FILES += $(call all-java-files-under, src/com/amlogic/DTVPlayer/notify)
LOCAL_SRC_FILES += $(call all-java-files-under, src/com/amlogic/DTVPlayer/storage)
LOCAL_SRC_FILES += $(call all-java-files-under, src/com/amlogic/DTVPlayer/vchip)
LOCAL_SRC_FILES += $(call all-java-files-under, src/com/amlogic/DTVPlayer/cc)

#include DTVPlayer/widgit src
#LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/CheckUsbdevice-v4.java
#LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/CheckUsbdevice-v5-v6.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/CustomDialog.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/DigitalClock.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/EpgScrollView.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/FocusScrollListView.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/MutipleChoiseDialog.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/PasswordDialog.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/PasswordLayout.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/PasswordSettingDialog.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/PasswordSettingLayout.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/Rotate3D.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/RotateableTextView.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/SingleChoiseDialog.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/SureDialog.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/TextViewWithCheck.java

#include DTVPlayer src
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/ATSCScanResult.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVActivity.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVBookList.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVBookingManager.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVChannelList.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVDeviceBrowser.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVEpg.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVMainMenu.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVPlayer.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVPlayerApp.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVProgramEdit.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVProgramManager.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVPvrManager.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVPvrPlayer.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVRecManager.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVRecordDevice.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVScanATSC.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVScanDTMB.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVScanDVBC.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVScanDVBS.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVScanDVBT.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVSettings.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVSettingsMenu.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVSettingsUI.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVTimeshifting.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DvbtScanResult.java


#include checkusebdevice dtvRecorddevice dtvdevicebrowser
$(info $(shell ($(LOCAL_PATH)/cpfile.sh $(PLATFORM_SDK_VERSION) $(LOCAL_PATH))))
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/widget/CheckUsbdevice.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVDeviceBrowser.java
LOCAL_SRC_FILES += src/com/amlogic/DTVPlayer/DTVRecordDevice.java



#LOCAL_SDK_VERSION := current
LOCAL_PACKAGE_NAME := DTVPlayer
LOCAL_STATIC_JAVA_LIBRARIES := tvmiddleware
LOCAL_JNI_SHARED_LIBRARIES := libam_adp libam_mw libjnitvmboxdevice libjnitvdatabase libjnitvdbcheck libjnitvscanner libjnitvsubtitle libjnitvepgscanner libzvbi libjnitvupdater

ifeq (1,$(strip $(shell expr $(PLATFORM_SDK_VERSION) \>= 22)))
LOCAL_JNI_SHARED_LIBRARIES += libam_sysfs
endif

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_CERTIFICATE := platform

$(LOCAL_PATH)/AndroidManifest.xml: $(LOCAL_PATH)/AndroidManifest.xml.in
	SDK_VERSION=$(PLATFORM_SDK_VERSION) LOCAL_PATH=$(dir $@) $(dir $@)/makeversion.sh $< $@

.PHONY: $(LOCAL_PATH)/AndroidManifest.xml

include $(BUILD_PACKAGE)

###################################################

include $(call all-makefiles-under,$(LOCAL_PATH))
