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
#import "SRProjectViewController.h"

@implementation SRAppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    SRProjectViewController* ctrl = [[SRProjectViewController alloc] initWithNibName:@"SRProjectViewController" bundle:nil];
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
