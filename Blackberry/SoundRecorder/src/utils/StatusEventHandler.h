/*
 * StatusEventHandler.cpp
 *
 *  Created on: 08.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */
#ifndef STATUSEVENTHANDLER_H_
#define STATUSEVENTHANDLER_H_

#include <QObject>
#include <QString>
#include <bb/AbstractBpsEventHandler>

class StatusEventHandler: public QObject, public bb::AbstractBpsEventHandler {
    Q_OBJECT

public:
    StatusEventHandler();
    virtual ~StatusEventHandler();
    virtual void event(bps_event_t *event);

signals:
	void audioStatusUpdate();
};

#endif /* STATUSEVENTHANDLER_H_ */
