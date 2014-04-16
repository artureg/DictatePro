/*
 * SERecord.h
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SERECORD_H_
#define SERECORD_H_

#include <qobject.h>
#include "SEAudioStream.h"

namespace bb {
namespace cascades {
/**
 * This class is a helper class that represents a sub-record
 */
class SERecord: public QObject {

	Q_OBJECT

public:
	SERecord();
	SERecord(double start, double duration, String soundPath);
	virtual ~SERecord();

	double start;
	double duration;
	QString soundPath;

	SEAudioStream getAudioStream(SEProject project);

};

} /* namespace cascades */
} /* namespace bb */
#endif /* SERECORD_H_ */
