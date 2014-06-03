/*
 * StatusEventHandler.hpp
 *
 *  Created on: 08.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */
#include "StatusEventHandler.h"

#include <bps/bps.h>
#include <bps/mmrenderer.h>
#include <bps/navigator.h>

#include <bb/cascades/Application>

StatusEventHandler::StatusEventHandler() {

	subscribe(mmrenderer_get_domain());

	qDebug() << "  BPS READY BPS  w";

	subscribe(navigator_get_domain());

    bps_initialize();

	intptr_t userdata = (intptr_t)malloc(sizeof(intptr_t));
	mmrenderer_request_events("wise-apps.com", 0, userdata);
	navigator_request_events(0);
	mmrenderer_request_events("", 0, NULL);

}

StatusEventHandler::~StatusEventHandler() {


	qDebug() << "  BPS shutdown";

    bps_shutdown();
}

void StatusEventHandler::event(bps_event_t *event) {

	int domain = bps_event_get_domain(event);

	if (domain == navigator_get_domain()) {

	        int code = bps_event_get_code(event);
	        switch(code) {
	            case NAVIGATOR_WINDOW_STATE:
	            	qDebug() << "BPS" << " NAVIGATOR_WINDOW_STATE ";
	                break;
	        }
	    }

	if (domain == mmrenderer_get_domain()) {

		int code = bps_event_get_code(event);


			emit audioStatusUpdate();
	}
}
