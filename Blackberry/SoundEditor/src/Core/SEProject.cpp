#include "SEProject.h"

#include <QFile>
#include <QDir>
#include <QSettings>
#include <QDebug>
#include "SEProjectAudioStream.h"
#include "Internal/SERecord.h"

static const QString kSEProjectName = "/project.txt";

SEProject::SEProject(QString projectFolderName, QObject *parent) :
		QObject(parent) {
    projectPath = QDir::currentPath() + "/data/" + projectFolderName;
    QDir dir(projectPath);
    if (!dir.exists()) {
        qDebug() << projectPath;
        dir.mkdir(projectPath);
        dir.mkdir(getProjectSoundPath());
        saveProject();
    } else {
        load();
    }
	stream = new SEProjectAudioStream(this, this);
}

void SEProject::load() {
	QFile file(projectPath + kSEProjectName);
	file.open(QIODevice::ReadOnly);
	QString string(file.readAll());
	file.close();
    if (string.length() == 0) {
        return;
    }
	QStringList list = string.split("\n");
    if (list.count() == 0) {
        return;
    }
    name = list[0];
    records.clear();
    for (int i = 1; i < list.count(); i++) {
        QString rInfo = list[i];
        if (rInfo.length() == 0) {
            continue;
        }
        QStringList info = rInfo.split(";");
        SERecordRange range;
        range.position = info[1].toLong();
        range.duration = info[2].toLong();
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
    records.clear();
    saveProject();
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

QList<SERecord*> SEProject::getRecords() {
    return records;
}

void SEProject::addRecord(SERecord *record) {
    records.append(record);
    saveProject();
}

void SEProject::removeRecord(SERecord *record) {
    records.removeOne(record);
    saveProject();
}

void SEProject::insertRecord(SERecord* record, int index) {
	if (index < records.count()) {
		records.insert(index, record);
	} else {
		records.append(record);
	}
	saveProject();
}

SERecord* SEProject::splitRecordInPosition(long position) {
	if ((records.count() == 0)||(position == stream->getDuration())) {
	    SERecord* record = new SERecord(this, this);
	    addRecord(record);
	    return record;
	}
	long pos = 0;
	long splitPos = position;
	long sTime = 0;
	long index = 0;
	SERecord* cRecord = NULL;
	bool split = true;
	for (int i = 0; i < records.count(); i++) {
		SERecord* record = records[i];
	    cRecord = record;
	    if (pos + record->getRange().duration == splitPos) {
	    	split = false;
	        index = i + 1;
	        break;
	    } else if (pos + record->getRange().duration > splitPos) {
	    	index = i;
	        split = true;
	        sTime = splitPos - pos;
	        break;
	    }
	    pos += record->getRange().duration;
	}
	SERecord* record = new SERecord(this, this);
	if (split) {
		records.removeAt(index);
		SERecordRange range = SERecordRangeMake(cRecord->getRange().position, sTime);
	    SERecord* pRec = new SERecord(cRecord->getSoundFile(), range, this);
	    insertRecord(pRec, index);
	    index++;
	    insertRecord(record, index);
	    index++;
	    range = SERecordRangeMake(cRecord->getRange().position + sTime, cRecord->getRange().duration - sTime);
	    SERecord* nRec = new SERecord(cRecord->getSoundFile(), range, this);
	    insertRecord(nRec, index);
	} else {
		insertRecord(record, index);
	}
	return record;
}


