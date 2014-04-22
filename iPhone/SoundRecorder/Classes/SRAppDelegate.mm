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

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
//    SEAudioStream* stream = [[SEAudioStream alloc] initWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"1" ofType:@"wav"]];
//    self.pv_engine = [[SEAudioStreamEngine alloc] initWithStream:stream];
//    [self.pv_engine startPlaying];
//    return YES;
    SRProjectViewController* ctrl = [[SRProjectViewController alloc] initWithNibName:@"SRProjectViewController" bundle:nil];
    
//    SRPlayerViewController* ctrl = [[SRPlayerViewController alloc] initWithNibName:@"SRPlayerViewController" bundle:nil];
//    ctrl.filePath = [[NSBundle mainBundle] pathForResource:@"rec_spx.wav" ofType:nil];
    
//    WaveSpeexFile* file = new WaveSpeexFile();
    
//    NSString* test = [[NSHomeDirectory() stringByAppendingPathComponent:@"Documents"] stringByAppendingPathComponent:@"test_spx.wav"];
//    NSString* spx = [[NSHomeDirectory() stringByAppendingPathComponent:@"Documents"] stringByAppendingPathComponent:@"spx.wav"];
//    
//    file->openRead([spx cStringUsingEncoding:NSASCIIStringEncoding]);
//    file->decodeToWavFile([test cStringUsingEncoding:NSASCIIStringEncoding]);
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
