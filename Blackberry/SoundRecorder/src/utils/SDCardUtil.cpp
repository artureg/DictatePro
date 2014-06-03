/*
 * SDCardUtil.cpp
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SDCardUtil.h"

#include "SEProject.h"
#include "SERecord.h"

#include <QFile>
#include <QXmlStreamWriter>
#include <QXmlStreamReader>

#include <bb/cascades/Application>


static const QString PROJECT_NAME_DEFAULT = "project.xml";

static const QString TAG_DIST = "dist";
static const QString TAG_IS_CHANGED = "isChanged";
static const QString TAG_RECORD = "record";
static const QString TAG_DURATION = "duration";
static const QString TAG_SOUND_PATH = "soundPath";
static const QString TAG_START = "start";

/**
 * Method to return the directory path where
 * the recorded tracks are stored.
 */
static QString fileStorageLocation()
{
	QString workingDir = QDir::currentPath();
	return QString(workingDir +  "/data/devacon/" + PROJECT_NAME_DEFAULT);
}

SDCardUtil::SDCardUtil() {
}

SDCardUtil::~SDCardUtil() {
}

SEProject SDCardUtil::readProject() {

	SEProject project;

	QFile file(fileStorageLocation());

	qDebug() << " SD CARD  OPEN read file path " << file.fileName();

	if (file.open(QFile::ReadOnly)) {

		qDebug() << " SD CARD  OPEN read file";

		QXmlStreamReader xmlReader;

		xmlReader.setDevice(&file);
		xmlReader.readNext();

		while (!xmlReader.atEnd() && !xmlReader.hasError())
		{
			qDebug() << " AAAA ";

			if (xmlReader.isStartElement())
			{
				QString name = xmlReader.name().toString();

				qDebug() << " root read = " << name;

				if(name == TAG_IS_CHANGED) {
					//project.changed = xmlReader.text();
				}

				project.changed?"false":"true";
			}
			xmlReader.readNext();
		}

	} else {
		qDebug() << " SD CARD read ERROR " << file.errorString();
	}

	file.close();


	return project;




//			if(xmlReader.isStartElement())
//			{
//				if(xmlReader.name() == TAG_DIST)
//				{
//					READNEXT();
//				}
//				else if(xmlReader.name() == TAG_RECORD)
//				{
//					while(!xmlReader.atEnd())
//					{
//					 if(xmlReader.isEndElement())
//					 {
//						 READNEXT();
//						 break;
//					 }
//					 else if(xmlReader.isCharacters())
//					 {
//						 READNEXT();
//					 }
//					 else if(xmlReader.isStartElement())
//					 {
//						 if(xmlReader.name() == TAG_DURATION)
//						 {
//						  ReadStateElement();
//						 }
//						 else if(xmlReader.name() == TAG_START)
//						 {
//						  ReadRoomElement();
//						 }
//						 else if(xmlReader.name() == TAG_SOUND_PATH)
//						 {
//							  ReadPotencialElement();
//						 }
//						 READNEXT();
//					 }
//					 else
//					 {
//					 READNEXT();
//					 }
//				}
//		        }
//	        }
//	}
//	else
//	{
//		READNEXT();
//	}
//
//	file.close();
//
//	if (xmlReader.hasError())
//	{
//	std::cerr << "Error: Failed to parse file "
//			 << qPrintable(filename) << ": "
//			 << qPrintable(xmlReader.errorString()) << std::endl;
//	}
//	else if (file.error() != QFile::NoError)
//	{
//	std::cerr << "Error: Cannot read file " << qPrintable(filename)
//			  << ": " << qPrintable(file.errorString())
//			  << std::endl;
//	}

}


//	void MyXMLClass::ReadElement()
//	{
//		while(!Rxml.atEnd())
//		{
//			if(Rxml.isEndElement())
//			{
//				READNEXT();
//				break;
//			}
//			else if(Rxml.isStartElement())
//			{
//				QString roomelement = Rxml.readElementText();   //Get the xml value
//				READNEXT();
//				break;
//			}
//			else if(Rxml.isCharacters())
//			{
//				READNEXT();
//			}
//			else
//			{
//				READNEXT();
//			}
//		}
//
//	}


/**
 * Save project to xml
 */
bool SDCardUtil::writeProject(SEProject project) {

	QFile file(fileStorageLocation());
	qDebug() << " SD CARD  OPEN write file path " << file.fileName();

	if(file.open(QIODevice::ReadWrite)) {

		qDebug() << " SD CARD  OPEN write file";

		QXmlStreamWriter xmlWriter(&file);
		xmlWriter.setAutoFormatting(true);
		xmlWriter.writeStartDocument();

		xmlWriter.writeStartElement( TAG_DIST );
		xmlWriter.writeTextElement( TAG_IS_CHANGED, project.changed?"false":"true" );

		// Populate each SERecord object with data
		for(int i=0; i<project.records.size(); i++) {
			xmlWriter.writeStartElement( TAG_RECORD );
//			xmlWriter.writeTextElement( TAG_DURATION, QString::number( project.records.at(i).duration ) );
//			xmlWriter.writeTextElement( TAG_START, QString::number( project.records.at(i).start ) );
//			xmlWriter.writeTextElement( TAG_SOUND_PATH, project.records.at(i).soundPath );
			xmlWriter.writeEndElement(); // </TAG_RECORD>
		}

		xmlWriter.writeEndElement(); // </TAG_DIST>
		xmlWriter.writeEndDocument();

		file.close();

	} else {
		qDebug() << " SD CARD write ERROR " << file.errorString();
		file.close();
		return false;
	}

	return true;

}
