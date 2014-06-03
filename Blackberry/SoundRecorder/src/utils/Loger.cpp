/*
 * Loger.cpp
 *
 *  Created on: 6.05.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "Loger.h"

#include <bb/cascades/Application>

Loger::Loger() {}

Loger::~Loger() {}

void Loger::Debug(const char *tag, const char *message) {

	if(ENABLED) {
		qDebug() << PREFIX << tag << message;
	}
}

void Loger::Error(const char *tag, const char *message) {
	if(ENABLED) {
	//	qFatal(tag + " " + message);
	}
}

void Loger::Warning(const char *tag, const char *message) {

	if(ENABLED) {
		qWarning() << PREFIX << tag << message;
	}
}
