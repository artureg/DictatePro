TEMPLATE = app

LIBS += -lbbdata -lbb -lbbcascades
LIBS += -lbbmultimedia -lmmrndclient -laudio_manager -lbbsystem -lasound
QT += declarative xml

SOURCES += \
    src/main.cpp \
    src/Core/Internal/RiffFile.cpp \
    src/Core/Internal/SEAudioStreamPlayer.cpp \
    src/Core/Internal/SEAudioStreamRecorder.cpp \
    src/Core/Internal/SERecord.cpp \
    src/Core/Internal/SERecordAudioStream.cpp \
    src/Core/Internal/WaveFile.cpp \
    src/Core/Internal/WaveSpeexFile.cpp \
    src/Core/SEAudioStream.cpp \
    src/Core/SEAudioStreamEngine.cpp \
    src/Core/SEProject.cpp \
    src/Core/SEProjectAudioStream.cpp \
    src/Core/SEProjectEngine.cpp \
    src/MainController.cpp \
    src/Core/Internal/CSource/libspeex/bits.c \
    src/Core/Internal/CSource/libspeex/buffer.c \
    src/Core/Internal/CSource/libspeex/cb_search.c \
    src/Core/Internal/CSource/libspeex/exc_10_16_table.c \
    src/Core/Internal/CSource/libspeex/exc_10_32_table.c \
    src/Core/Internal/CSource/libspeex/exc_20_32_table.c \
    src/Core/Internal/CSource/libspeex/exc_5_256_table.c \
    src/Core/Internal/CSource/libspeex/exc_5_64_table.c \
    src/Core/Internal/CSource/libspeex/exc_8_128_table.c \
    src/Core/Internal/CSource/libspeex/fftwrap.c \
    src/Core/Internal/CSource/libspeex/filterbank.c \
    src/Core/Internal/CSource/libspeex/filters.c \
    src/Core/Internal/CSource/libspeex/gain_table.c \
    src/Core/Internal/CSource/libspeex/gain_table_lbr.c \
    src/Core/Internal/CSource/libspeex/hexc_10_32_table.c \
    src/Core/Internal/CSource/libspeex/hexc_table.c \
    src/Core/Internal/CSource/libspeex/high_lsp_tables.c \
    src/Core/Internal/CSource/libspeex/jitter.c \
    src/Core/Internal/CSource/libspeex/kiss_fft.c \
    src/Core/Internal/CSource/libspeex/kiss_fftr.c \
    src/Core/Internal/CSource/libspeex/lpc.c \
    src/Core/Internal/CSource/libspeex/lsp.c \
    src/Core/Internal/CSource/libspeex/lsp_tables_nb.c \
    src/Core/Internal/CSource/libspeex/ltp.c \
    src/Core/Internal/CSource/libspeex/mdf.c \
    src/Core/Internal/CSource/libspeex/modes.c \
    src/Core/Internal/CSource/libspeex/modes_wb.c \
    src/Core/Internal/CSource/libspeex/nb_celp.c \
    src/Core/Internal/CSource/libspeex/preprocess.c \
    src/Core/Internal/CSource/libspeex/quant_lsp.c \
    src/Core/Internal/CSource/libspeex/resample.c \
    src/Core/Internal/CSource/libspeex/sb_celp.c \
    src/Core/Internal/CSource/libspeex/scal.c \
    src/Core/Internal/CSource/libspeex/smallft.c \
    src/Core/Internal/CSource/libspeex/speex.c \
    src/Core/Internal/CSource/libspeex/speex_callbacks.c \
    src/Core/Internal/CSource/libspeex/speex_header.c \
    src/Core/Internal/CSource/libspeex/stereo.c \
    src/Core/Internal/CSource/libspeex/vbr.c \
    src/Core/Internal/CSource/libspeex/vq.c \
    src/Core/Internal/CSource/libspeex/window.c \
    src/Core/Internal/SESpeexAudioStream.cpp

HEADERS += \
    src/Core/Internal/CSource/libspeex/_kiss_fft_guts.h \
    src/Core/Internal/CSource/libspeex/arch.h \
    src/Core/Internal/CSource/libspeex/cb_search.h \
    src/Core/Internal/CSource/libspeex/cb_search_arm4.h \
    src/Core/Internal/CSource/libspeex/cb_search_bfin.h \
    src/Core/Internal/CSource/libspeex/cb_search_sse.h \
    src/Core/Internal/CSource/libspeex/fftwrap.h \
    src/Core/Internal/CSource/libspeex/filterbank.h \
    src/Core/Internal/CSource/libspeex/filters.h \
    src/Core/Internal/CSource/libspeex/filters_arm4.h \
    src/Core/Internal/CSource/libspeex/filters_bfin.h \
    src/Core/Internal/CSource/libspeex/filters_sse.h \
    src/Core/Internal/CSource/libspeex/fixed_arm4.h \
    src/Core/Internal/CSource/libspeex/fixed_arm5e.h \
    src/Core/Internal/CSource/libspeex/fixed_bfin.h \
    src/Core/Internal/CSource/libspeex/fixed_debug.h \
    src/Core/Internal/CSource/libspeex/fixed_generic.h \
    src/Core/Internal/CSource/libspeex/kiss_fft.h \
    src/Core/Internal/CSource/libspeex/kiss_fftr.h \
    src/Core/Internal/CSource/libspeex/lpc.h \
    src/Core/Internal/CSource/libspeex/lpc_bfin.h \
    src/Core/Internal/CSource/libspeex/lsp.h \
    src/Core/Internal/CSource/libspeex/lsp_bfin.h \
    src/Core/Internal/CSource/libspeex/ltp.h \
    src/Core/Internal/CSource/libspeex/ltp_arm4.h \
    src/Core/Internal/CSource/libspeex/ltp_bfin.h \
    src/Core/Internal/CSource/libspeex/ltp_sse.h \
    src/Core/Internal/CSource/libspeex/math_approx.h \
    src/Core/Internal/CSource/libspeex/misc_bfin.h \
    src/Core/Internal/CSource/libspeex/modes.h \
    src/Core/Internal/CSource/libspeex/nb_celp.h \
    src/Core/Internal/CSource/libspeex/os_support.h \
    src/Core/Internal/CSource/libspeex/pseudofloat.h \
    src/Core/Internal/CSource/libspeex/quant_lsp.h \
    src/Core/Internal/CSource/libspeex/quant_lsp_bfin.h \
    src/Core/Internal/CSource/libspeex/resample_sse.h \
    src/Core/Internal/CSource/libspeex/sb_celp.h \
    src/Core/Internal/CSource/libspeex/smallft.h \
    src/Core/Internal/CSource/libspeex/stack_alloc.h \
    src/Core/Internal/CSource/libspeex/vbr.h \
    src/Core/Internal/CSource/libspeex/vorbis_psy.h \
    src/Core/Internal/CSource/libspeex/vq.h \
    src/Core/Internal/CSource/libspeex/vq_arm4.h \
    src/Core/Internal/CSource/libspeex/vq_bfin.h \
    src/Core/Internal/CSource/libspeex/vq_sse.h \
    src/Core/Internal/CSource/speex/speex.h \
    src/Core/Internal/CSource/speex/speex_bits.h \
    src/Core/Internal/CSource/speex/speex_buffer.h \
    src/Core/Internal/CSource/speex/speex_callbacks.h \
    src/Core/Internal/CSource/speex/speex_config_types.h \
    src/Core/Internal/CSource/speex/speex_echo.h \
    src/Core/Internal/CSource/speex/speex_header.h \
    src/Core/Internal/CSource/speex/speex_jitter.h \
    src/Core/Internal/CSource/speex/speex_preprocess.h \
    src/Core/Internal/CSource/speex/speex_resampler.h \
    src/Core/Internal/CSource/speex/speex_stereo.h \
    src/Core/Internal/CSource/speex/speex_types.h \
    src/Core/Internal/CSource/config.h \
    src/Core/Internal/RiffFile.h \
    src/Core/Internal/SEAudioStreamPlayer.h \
    src/Core/Internal/SEAudioStreamRecorder.h \
    src/Core/Internal/SERecord.h \
    src/Core/Internal/SERecordAudioStream.h \
    src/Core/Internal/WaveFile.h \
    src/Core/Internal/WaveSpeexFile.h \
    src/Core/SEAudioStream.h \
    src/Core/SEAudioStreamEngine.h \
    src/Core/SEProject.h \
    src/Core/SEProjectAudioStream.h \
    src/Core/SEProjectEngine.h \
    src/MainController.hpp \
    src/Core/Internal/SESpeexAudioStream.h

OTHER_FILES += \
    bar-descriptor.xml \
    assets/main.qml \
    assets/images/bnt_pause_default.png \
    assets/images/bnt_pause_disabled.png \
    assets/images/btn_end_default.png \
    assets/images/btn_end_disabled.png \
    assets/images/btn_forward_default.png \
    assets/images/btn_forward_disabled.png \
    assets/images/btn_pin1_disabled.png \
    assets/images/btn_pin2_disabled.png \
    assets/images/btn_pin3_disabled.png \
    assets/images/btn_pin4_disabled.png \
    assets/images/btn_play_default.png \
    assets/images/btn_play_disabled.png \
    assets/images/btn_play_selected.png \
    assets/images/btn_record_default.png \
    assets/images/btn_record_disabled.png \
    assets/images/btn_record_selected.png \
    assets/images/btn_rewind_default.png \
    assets/images/btn_rewind_disabled.png \
    assets/images/btn_sd_default.png \
    assets/images/btn_sd_disabled.png \
    assets/images/btn_send_default.png \
    assets/images/btn_send_disabled.png \
    assets/images/btn_start_default.png \
    assets/images/btn_start_disabled.png \
    assets/images/pause.png \
    assets/images/play_disabled.png \
    assets/images/play.png \
    assets/images/progress_scale.png \
    assets/images/record.png \
    assets/images/stop.png \
    assets/images/volume_cursor.png



