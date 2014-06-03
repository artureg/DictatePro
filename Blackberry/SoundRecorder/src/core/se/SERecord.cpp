/*
 * SERecord.cpp
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 */

#include "SERecord.h"

SERecord::SERecord():soundUrl("") {
	soundRange.start = 0;
	soundRange.duration = 0;
    m_audio_stream = NULL;


}

SERecord::~SERecord() {
    free(m_audio_stream);
}

SERecordAudioStream* SERecord::audioStream() {

//	SERecordAudioStream stream;
//	stream.initWithRecord(this);
//	m_audio_stream = stream;
    if(m_audio_stream == NULL) {
        m_audio_stream = new SERecordAudioStream();
        //m_audio_stream->initWithRecord(this);
    }
    m_audio_stream->initWithRecord(this);
    return m_audio_stream;
}
