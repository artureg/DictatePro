#ifndef SEPROJECT_H
#define SEPROJECT_H

#include <QObject>
#include "Internal/SERecord.h"

class SEAudioStream;

class SEProject: public QObject {
	Q_OBJECT
public:
	explicit SEProject(QString projectFolderName, QObject *parent = 0);

	SEAudioStream* getAudioStream();

	void setName(QString name);
	QString getName();

	QString getProjectPath();
	QString getProjectSoundPath();

	void clearProject();
	void saveProject();

private:
	QString name;
	QString projectPath;
	QList<SERecord*> records;
	SEAudioStream* stream;
	void load();

	signals:

public slots:

};

#endif // SEPROJECT_H
