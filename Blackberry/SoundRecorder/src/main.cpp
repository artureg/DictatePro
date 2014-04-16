/**
@mainpage Project SoundREcorder
@author Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
*/

#include <bb/cascades/Application>

#include <QLocale>
#include <QTranslator>
#include "ui/MainController.hpp"

#include <Qt/qdeclarativedebug.h>

#define BLAKCBERRY_x86


using namespace bb::cascades;

/// FIXME ===========================================================
/// !!!!! it should be removed when App will be pushed to Production
/// =================================================================
void myMessageOutput(QtMsgType type, const char* msg){
                fprintf(stdout, "%s\n", msg);
                fflush(stdout);
}

Q_DECL_EXPORT int main(int argc, char **argv)
{
    Application app(argc, argv);
    qInstallMsgHandler(myMessageOutput); //FIXME REMOVE IT

    /** Create the Application UI object, this is where the main.qml file
     is loaded and the application scene is set.
     */
    new MainController(&app);

    /// Enter the application main event loop.
    return Application::exec();

}


