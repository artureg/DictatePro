#include "StatusEventHandler.h"

#include <bps/bps.h>
#include <bps/mmrenderer.h>
#include <bps/navigator.h>

#include <bb/cascades/Application>

StatusEventHandler::StatusEventHandler() {
//    subscribe(netstatus_get_domain());
//    subscribe(locale_get_domain());

	subscribe(mmrenderer_get_domain());

	qDebug() << "  BPS READY BPS  w";

	subscribe(navigator_get_domain());

    bps_initialize();

    	intptr_t userdata = (intptr_t)malloc(sizeof(intptr_t));
    	mmrenderer_request_events("wise-apps.com", 0, userdata);
    	navigator_request_events(0);
    	mmrenderer_request_events("", 0, NULL);

//    netstatus_request_events(0);
//    locale_request_events(0);
}

StatusEventHandler::~StatusEventHandler() {


	qDebug() << "  BPS shutdown";

    bps_shutdown();
}

void StatusEventHandler::event(bps_event_t *event) {

	int domain = bps_event_get_domain(event);

	qDebug() << "  STATUSSSSS BPS  w " << bps_event_get_code(event);

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

//		if (MMRENDERER_STATE_CHANGE == bps_event_get_code(event)) {
//
//			mmrenderer_event_get_state(event);

//			MMR_DESTROYED
//			      MMR_IDLE
//			      MMR_STOPPED
//			      MMR_PLAYING


			qDebug() << "  STATUSSSSS BPS renderer" << code;


			emit audioStatusUpdate();
//		}
	}
}
