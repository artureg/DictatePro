#ifndef SEAUDIOSTREAM_H
#define SEAUDIOSTREAM_H

#include <QObject>

class WaveFile;

typedef enum {
	kSEAudioStreamModeNone = 0,
	kSEAudioStreamModeRead = 1,
	kSEAudioStreamModeWrite = 2
} TSEAudioStreamMode;

typedef struct {
	unsigned short audioFormat;
	unsigned short numberOfChannels;
	unsigned long sampleRate;
	unsigned long bytesPerSecond;
	unsigned short bytesPerSample;
	unsigned short bitsPerSample;
} TSEAudioStreamDesc;

class SEAudioStream: public QObject {
	Q_OBJECT
public:
	explicit SEAudioStream(QString path, QObject *parent = 0);
	~SEAudioStream();

	long getDuration();
	void setDescription(TSEAudioStreamDesc desc);
	TSEAudioStreamDesc getDescription();

	TSEAudioStreamMode getMode();
	bool open(TSEAudioStreamMode mode);
	void close();

	bool readData(QByteArray& byteArray, long position, long duration);

private:
	QString path;
	TSEAudioStreamMode mode;
	WaveFile* file;
	TSEAudioStreamDesc desc;

	void reloadDesc();
};

#endif // SEAUDIOSTREAM_H
