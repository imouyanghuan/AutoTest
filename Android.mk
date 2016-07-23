LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# LOCAL_MODULE_TAGS := eng

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := AutoTest

#LOCAL_JAVA_LIBRARIES := javax.obex
#add by czq
LOCAL_JAVA_LIBRARIES += javax.obex
LOCAL_JAVA_LIBRARIES += telephony-common
LOCAL_JAVA_LIBRARIES += mediatek-telephony-common
#end

LOCAL_STATIC_JAVA_LIBRARIES += user_mode

LOCAL_CERTIFICATE := platform
include $(BUILD_PACKAGE)

include $(CLEAR_VARS) 

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := user_mode:user_mode.jar

include $(BUILD_MULTI_PREBUILT)
