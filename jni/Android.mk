LOCAL_PATH := $(call my-dir)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := abl_link_tilde
EXTSRC_DIR := $(LOCAL_PATH)/abl_link/external
LINK_INCLUDES := $(EXTSRC_DIR)/link/include
ASIO_INCLUDES := $(EXTSRC_DIR)/link/modules/asio-standalone/asio/include
IFADDRS_INCLUDE := $(EXTSRC_DIR)/android-ifaddrs
LOCAL_C_INCLUDES := $(LINK_INCLUDES) $(ASIO_INCLUDES) $(IFADDRS_INCLUDE)
LOCAL_CPPFLAGS := -DPD -std=c++11 -Wno-unqualified-std-cast-call -fexceptions -frtti -Wno-multichar -DLINK_PLATFORM_LINUX=1 -DANDROID=1 -DABL_LINK_OFFSET_MS=23
LOCAL_SRC_FILES := $(EXTSRC_DIR)/abl_link~.cpp $(EXTSRC_DIR)/abl_link_instance.cpp $(EXTSRC_DIR)/android-ifaddrs/ifaddrs.cpp
LOCAL_SHARED_LIBRARIES = pd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := threadtime
EXTSRC_DIR := $(LOCAL_PATH)/threadtime/
LOCAL_CPPFLAGS := -DPD
LOCAL_SRC_FILES := $(EXTSRC_DIR)/threadtime.c
LOCAL_SHARED_LIBRARIES = pd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

$(call import-module,prefab/pd-core)

