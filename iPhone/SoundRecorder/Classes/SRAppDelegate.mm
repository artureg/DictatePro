//
//  SRAppDelegate.m
//  SoundRecorder
//
//  Created by Igor Danich
//  Property of UAB "Mobilios Aplikacijos"
//

#import "SRAppDelegate.h"

#import "SRMainViewController.h"
#import "SRPlayerViewController.h"

@implementation SRAppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    SRPlayerViewController* ctrl = [[SRPlayerViewController alloc] initWithNibName:@"SRPlayerViewController" bundle:nil];
    UINavigationController* navCtrl = [[UINavigationController alloc] initWithRootViewController:ctrl];
    navCtrl.navigationBar.barStyle = UIBarStyleBlackOpaque;
    navCtrl.navigationBar.translucent = NO;
    navCtrl.navigationBarHidden = NO;
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
