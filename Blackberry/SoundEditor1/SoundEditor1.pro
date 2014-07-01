TEMPLATE = app

LIBS += -lbbdata -lbb -lbbcascades
LIBS += -lbbmultimedia -lmmrndclient -laudio_manager -lbbsystem -lasound
QT += declarative xml

SOURCES += \
    src/main.cpp \
    src/Core/Internal/RiffFile.cpp \
    src/Core/Internal/SEAudioStreamPlayer.cpp \
    src/Core/Internal/SERecord.cpp \
    src/Core/Internal/WaveFile.cpp \
    src/Core/SEAudioStream.cpp \
    src/Core/SEAudioStreamEngine.cpp \
    src/Core/SEProject.cpp \
    src/Core/SEProjectAudioStream.cpp \
    src/Core/SEProjectEngine.cpp \
    src/MainController.cpp

HEADERS += \
    src/Core/Internal/RiffFile.h \
    src/Core/Internal/SEAudioStreamPlayer.h \
    src/Core/Internal/SERecord.h \
    src/Core/Internal/WaveFile.h \
    src/Core/SEAudioStream.h \
    src/Core/SEAudioStreamEngine.h \
    src/Core/SEProject.h \
    src/Core/SEProjectAudioStream.h \
    src/Core/SEProjectEngine.h \
    src/MainController.hpp

OTHER_FILES += \
    bar-descriptor.xml \
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
    assets/images/ic/ic_add.png \
    assets/images/ic/ic_clear.png \
    assets/images/ic/ic_cut.png \
    assets/images/ic/ic_delete.png \
    assets/images/ic/ic_microphone.png \
    assets/images/ic/ic_microphone_mute.png \
    assets/images/ic/ic_next.png \
    assets/images/ic/ic_pause.png \
    assets/images/ic/ic_play.png \
    assets/images/ic/ic_previous.png \
    assets/images/ic/ic_stop.png \
    assets/images/pause.png \
    assets/images/play.png \
    assets/images/play_disabled.png \
    assets/images/progress_scale.png \
    assets/images/record.png \
    assets/images/stop.png \
    assets/images/volume_cursor.png \
    assets/main_page.qml \
    assets/models/tracks_list.xml \
    bar-descriptor.xml \



