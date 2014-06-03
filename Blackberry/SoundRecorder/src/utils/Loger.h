/*
 * Loger.h
 *
 *  Created on: 6.05.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef LOGER_H_
#define LOGER_H_

#define PREFIX "[SR]"
#define MSG_SIZE 1024

using namespace std;

class Loger {
public:
	Loger();
	virtual ~Loger();

	static char msgLogger[1024];

	static void Debug(const char *tag, const char *message);
	static void Error(const char *tag, const char *message);
	static void Warning(const char *tag, const char *message);

private:
	static bool const ENABLED = true;

};

#endif /* LOGER_H_ */
