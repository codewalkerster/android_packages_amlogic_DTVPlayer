LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := samples

LOCAL_SRC_FILES := $(call all-java-files-under, src)

#LOCAL_SDK_VERSION := current
LOCAL_PACKAGE_NAME := DTVPlayer
LOCAL_STATIC_JAVA_LIBRARIES := tvmiddleware
LOCAL_JNI_SHARED_LIBRARIES := libam_adp libam_mw libjnitvmboxdevice libjnitvdatabase libjnitvdbcheck libjnitvscanner libjnitvsubtitle libjnitvepgscanner libzvbi libjnitvupdater
LOCAL_PROGUARD_ENABLED := disabled

LOCAL_CERTIFICATE := platform

$(LOCAL_PATH)/AndroidManifest.xml: $(LOCAL_PATH)/AndroidManifest.xml.in
	LOCAL_PATH=packages/amlogic/DTVPlayer packages/amlogic/DTVPlayer/makeversion.sh $< $@

.PHONY: $(LOCAL_PATH)/AndroidManifest.xml

include $(BUILD_PACKAGE)

##################################################

include $(call all-makefiles-under,$(LOCAL_PATH))
