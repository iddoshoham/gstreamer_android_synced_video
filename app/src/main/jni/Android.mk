LOCAL_PATH := $(call my-dir)

# Use the prebuilt gstreamer_android shared library (libgstreamer_android.so)
include $(CLEAR_VARS)
LOCAL_MODULE := gstreamer_android
# Specify the prebuilt shared library location
LOCAL_SRC_FILES := ../prebuiltLibs/$(TARGET_ARCH_ABI)/libgstreamer_android.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)

# Build the gstplayer shared library
include $(CLEAR_VARS)
LOCAL_MODULE    := gstplayer

ifeq ($(TARGET_ARCH_ABI), armeabi-v7a)
	GST_ABI := armv7
	else ifeq ($(TARGET_ARCH_ABI), arm64-v8a)
	GST_ABI := arm64
	endif

LOCAL_C_INCLUDES := \
	$(GSTREAMER_ROOT_ANDROID)/$(GST_ABI)/include \
	$(GSTREAMER_ROOT_ANDROID)/$(GST_ABI)/lib/glib-2.0/include \
	$(GSTREAMER_ROOT_ANDROID)/$(GST_ABI)/include/glib-2.0 \
	$(GSTREAMER_ROOT_ANDROID)/$(GST_ABI)/include/gstreamer-1.0 \
	$(GSTREAMER_ROOT_ANDROID)/$(GST_ABI)/lib/gstreamer-1.0/include
LOCAL_SRC_FILES := player.c dummy.cpp

# Link against the prebuilt gstreamer_android shared library
LOCAL_SHARED_LIBRARIES := gstreamer_android
LOCAL_LDLIBS := -llog -landroid


include $(BUILD_SHARED_LIBRARY)

# Ensure GSTREAMER_ROOT_ANDROID is defined
ifndef GSTREAMER_ROOT_ANDROID
    $(error GSTREAMER_ROOT_ANDROID is not defined!)
endif

# Set GSTREAMER_ROOT based on the target ABI
ifeq ($(TARGET_ARCH_ABI),armeabi)
    GSTREAMER_ROOT := $(GSTREAMER_ROOT_ANDROID)/arm
else ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    GSTREAMER_ROOT := $(GSTREAMER_ROOT_ANDROID)/armv7
else ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    GSTREAMER_ROOT := $(GSTREAMER_ROOT_ANDROID)/arm64
else ifeq ($(TARGET_ARCH_ABI),x86)
    GSTREAMER_ROOT := $(GSTREAMER_ROOT_ANDROID)/x86
else ifeq ($(TARGET_ARCH_ABI),x86_64)
    GSTREAMER_ROOT := $(GSTREAMER_ROOT_ANDROID)/x86_64
else
    $(error Target arch ABI not supported: $(TARGET_ARCH_ABI))
endif

# Include GStreamer plugins (not necessary to rebuild libgstreamer_android.so)
GSTREAMER_NDK_BUILD_PATH := $(GSTREAMER_ROOT)/share/gst-android/ndk-build

include $(GSTREAMER_NDK_BUILD_PATH)/plugins.mk

# Define the GStreamer plugins to include
GSTREAMER_PLUGINS := \
    $(GSTREAMER_PLUGINS_CORE) \
    $(GSTREAMER_PLUGINS_PLAYBACK) \
    $(GSTREAMER_PLUGINS_CODECS) \
    $(GSTREAMER_PLUGINS_NET) \
    $(GSTREAMER_PLUGINS_SYS) \
    $(GSTREAMER_PLUGINS_CODECS_RESTRICTED) \
    $(GSTREAMER_PLUGINS_ENCODING) \
    $(GSTREAMER_PLUGINS_VIS) \
    $(GSTREAMER_PLUGINS_EFFECTS) \
    $(GSTREAMER_PLUGINS_NET_RESTRICTED)

# Additional GStreamer dependencies
GSTREAMER_EXTRA_DEPS := gstreamer-player-1.0 gstreamer-video-1.0 glib-2.0 gstreamer-net-1.0

# Include GStreamer build configuration
# include $(GSTREAMER_NDK_BUILD_PATH)/gstreamer-1.0.mk
