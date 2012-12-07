LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

#LOCAL_SDK_VERSION := current
LOCAL_PACKAGE_NAME := DTVPlayer
LOCAL_STATIC_JAVA_LIBRARIES := tvmiddleware

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_CERTIFICATE := platform


include $(BUILD_PACKAGE)

##################################################

include $(call all-makefiles-under,$(LOCAL_PATH))
