# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := SpeexLib
LOCAL_SRC_FILES := ./bits.c \
./buffer.c \
./cb_search.c \
./exc_10_16_table.c \
./exc_10_32_table.c \
./exc_20_32_table.c \
./exc_5_256_table.c \
./exc_5_64_table.c \
./exc_8_128_table.c \
./fftwrap.c \
./filterbank.c \
./filters.c \
./gain_table.c \
./gain_table_lbr.c \
./hexc_10_32_table.c \
./hexc_table.c \
./high_lsp_tables.c \
./jitter.c \
./kiss_fft.c \
./kiss_fftr.c \
./lpc.c \
./lsp.c \
./lsp_tables_nb.c \
./ltp.c \
./mdf.c \
./modes.c \
./modes_wb.c \
./nb_celp.c \
./preprocess.c \
./quant_lsp.c \
./resample.c \
./sb_celp.c \
./scal.c \
./smallft.c \
./speex.c \
./speex_callbacks.c \
./speex_header.c \
./stereo.c \
./vbr.c \
./vq.c \
./window.c \
SpeexEncode.cpp \
../../SPEEXConverter/RiffFile.cpp \
../../SPEEXConverter/WaveFile.cpp \
../../SPEEXConverter/WaveSpeexFile.cpp \

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/libspeex \
$(LOCAL_PATH)/speex

LOCAL_CFLAGS := -fexceptions -DFIXED_POINT -DUSE_KISS_FFT -DEXPORT="" -UHAVE_CONFIG_H

include $(BUILD_SHARED_LIBRARY)
