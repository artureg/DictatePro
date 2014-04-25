//
//  SRAppDelegate.m
//  SoundRecorder
//
//  Created by Igor Danich igor.danich@wise-apps.com Danich

//

#import "SRAppDelegate.h"

#import "SRMainViewController.h"
#import "SRPlayerViewController.h"
#import "SRProjectViewController.h"

#include "SpeexACMConvert.h"

#import "SEAudioStream.h"
#import "SEAudioStreamEngine.h"

#include "WaveSpeexFile.h"

@interface SRAppDelegate()
@property(nonatomic,strong) SEAudioStreamEngine* pv_engine;
@end

@implementation SRAppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions {
    SRProjectViewController* ctrl = [[SRProjectViewController alloc] initWithNibName:@"SRProjectViewController" bundle:nil];
    
//    SRPlayerViewController* ctrl = [[SRPlayerViewController alloc] initWithNibName:@"SRPlayerViewController" bundle:nil];
//    ctrl.filePath = [[NSBundle mainBundle] pathForResource:@"WBQ=1" ofType:@"wav"];
    
//    WaveSpeexFile* file = new WaveSpeexFile();
//    NSString* spx = [[NSHomeDirectory() stringByAppendingPathComponent:@"Documents"] stringByAppendingPathComponent:@"1123.wav"];
//    file->openWrite([spx cStringUsingEncoding:NSASCIIStringEncoding]);
//    file->setupInfo(8000, 2, 8);
//    short data[16000];
//    int size;
//    file->decodeToData(0, 0.02f, (short*)data, &size);
//    file->close();
    
    
    UINavigationController* navCtrl = [[UINavigationController alloc] initWithRootViewController:ctrl];
    navCtrl.navigationBar.barStyle = UIBarStyleBlackOpaque;
    navCtrl.navigationBar.translucent = NO;
    navCtrl.navigationBarHidden = YES;
    self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    self.window.rootViewController = navCtrl;
    [self.window makeKeyAndVisible];
    return YES;
}

- (void)applicationWillResignActive:(UIApplication*)application {
}

- (void)applicationDidEnterBackground:(UIApplication*)application {
}

- (void)applicationWillEnterForeground:(UIApplication*)application {
}

- (void)applicationDidBecomeActive:(UIApplication*)application {
}

- (void)applicationWillTerminate:(UIApplication*)application {
}

@end
