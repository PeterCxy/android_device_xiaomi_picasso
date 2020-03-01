#
# Copyright (C) 2020 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_DEVICE),picasso)
subdir_makefiles=$(call first-makefiles-under,$(LOCAL_PATH))
$(foreach mk,$(subdir_makefiles),$(info including $(mk) ...)$(eval include $(mk)))

include $(CLEAR_VARS)

LIBBLUETOOTH_QTI_SYMLINKS := $(TARGET_OUT)/lib64/libbluetooth_qti.so
$(LIBBLUETOOTH_QTI_SYMLINKS): $(LOCAL_INSTALLED_MODULE)
	@echo "Creating libbluetooth_qti symlink"
	$(hide) mkdir -p $(dir $@)
	$(hide) ln -sf /system/lib64/libbluetooth.so $@

ALL_DEFAULT_INSTALLED_MODULES += $(LIBBLUETOOTH_QTI_SYMLINKS)
endif