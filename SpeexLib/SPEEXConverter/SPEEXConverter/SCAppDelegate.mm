//
//  SCAppDelegate.m
//  SPEEXConverter
//
//  Created by Igor on 3/19/14.
//  Copyright (c) 2014 Igor. All rights reserved.
//

#import "SCAppDelegate.h"

#include "SpeexACMConvert.h"

@interface SCAppDelegate()
//- (void)pm_convertToSpeex:(NSString*)inFilePath outFile:(NSString*)outFilePath;
@end

@implementation SCAppDelegate

- (void)pm_action {
    NSString* wavPath = [[NSBundle mainBundle] pathForResource:@"testOut" ofType:@"wav"];
    NSString* spxPath = [[NSBundle mainBundle] pathForResource:@"rec_spx" ofType:@"wav"];
    NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString* outFile = [NSString stringWithFormat:@"%@/testOut.wav", paths[0]];
    
    if (!encodeWavToSpeexACM([wavPath cStringUsingEncoding:NSASCIIStringEncoding], [outFile cStringUsingEncoding:NSASCIIStringEncoding])) {
        NSLog(@"false");
    }
//    if (!decodeSpeexToWav([spxPath cStringUsingEncoding:NSASCIIStringEncoding], [outFile cStringUsingEncoding:NSASCIIStringEncoding])) {
//        NSLog(@"false");
//    }
}

- (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    self.window.backgroundColor = [UIColor whiteColor];
    [self.window makeKeyAndVisible];
    
    UIViewController* ctrl = [[UIViewController alloc] initWithNibName:nil bundle:nil];
    UIButton* btn = [UIButton buttonWithType:UIButtonTypeDetailDisclosure];
    btn.center = ctrl.view.center;
    [btn addTarget:self action:@selector(pm_action) forControlEvents:UIControlEventTouchUpInside];
    [ctrl.view addSubview:btn];
    self.window.rootViewController = ctrl;
    
    
    NSLog(@"");
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

@end
