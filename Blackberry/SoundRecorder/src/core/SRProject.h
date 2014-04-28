/*
 * SRProject.h
 *
 *  Created on: 11.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SRPROJECT_H_
#define SRPROJECT_H_

#include <qobject.h>

namespace bb {
namespace cascades {

class SRProject: public QObject {
public:
	SRProject();
	virtual ~SRProject();

};

} /* namespace cascades */
} /* namespace bb */
#endif /* SRPROJECT_H_ */
