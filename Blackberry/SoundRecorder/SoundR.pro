APP_NAME = SoundR

CONFIG += qt warn_on cascades10 -Winvalid-pch

include(config.pri)

LIBS +=  -lbb -lbbmultimedia -lmmrndclient -laudio_manager -lbbsystem

#-lMySharedLibrary
