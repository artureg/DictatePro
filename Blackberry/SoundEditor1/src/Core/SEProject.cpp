#include "SEProject.h"

#include <QFile>
#include <QDir>
#include <QSettings>
#include <QDebug>
#include "SEProjectAudioStream.h"

static const QString kSEProjectName = "/project.txt";

SEProject::SEProject(QString projectFolderName, QObject *parent) :
		QObject(parent) {
	projectPath = QDir::currentPath() + "/data/devacon/" + projectFolderName;
//    QDir dir(projectPath);
//    if (!dir.exists()) {
//        dir.mkdir(projectPath);
//        dir.mkdir(getProjectSoundPath());
//        saveProject();
//    } else {
//        load();
//    }
	stream = new SEProjectAudioStream(this, this);
}

void SEProject::load() {
	QFile file(projectPath + kSEProjectName);
	file.open(QIODevice::ReadOnly);
	QString string(file.readAll());
	file.close();
	QStringList list = string.split("\n");
	name = list[0];
	records.clear();
	for (int i = 1; i < list.count(); i++) {
		QStringList info = list[i].split(";");
		SERecordRange range;
		range.position = info[1].toLong();
		range.position = info[2].toLong();
		records << new SERecord(info[0], range, this);
	}
}

SEAudioStream* SEProject::getAudioStream() {
	return stream;
}

void SEProject::setName(QString name) {
	this->name = name;
	saveProject();
}

QString SEProject::getName() {
	return name;
}

QString SEProject::getProjectPath() {
	return projectPath;
}

QString SEProject::getProjectSoundPath() {
	return projectPath + "/Sounds";
}

void SEProject::clearProject() {
}

void SEProject::saveProject() {
	QString string;
	string.append(name + "\n");
	for (int i = 0; i < records.count(); i++) {
		SERecord* record = records[i];
		string.append(record->getSoundFile() + ";");
		string.append(QString::number(record->getRange().position) + ";");
		string.append(QString::number(record->getRange().duration) + ";");
		string.append("\n");
	}
	qDebug() << string;
	QFile file(projectPath + kSEProjectName);
	file.open(QIODevice::WriteOnly);
	file.write(string.toUtf8());
	file.flush();
	file.close();
}
