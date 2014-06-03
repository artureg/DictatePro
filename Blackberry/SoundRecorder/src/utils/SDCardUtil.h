/*
 * SDCardUtil.h
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SDCARDUTIL_H_
#define SDCARDUTIL_H_

#include "SEProject.h"

//using namespace bb::cascades;


class SDCardUtil {
public:

	SDCardUtil();
	virtual ~SDCardUtil();

	SEProject readProject();
	bool writeProject(SEProject project);

};

#endif /* SDCARDUTIL_H_ */
