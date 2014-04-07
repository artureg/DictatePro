//
//  main.m
//  SoundRecorder
//
//  Created by Igor Danich on 12/12/13.
//  Property of UAB "Mobilios Aplikacijos"
//

#import <UIKit/UIKit.h>

#import "SRAppDelegate.h"

static NSString* sDocumentsFolderPath = nil;
NSString* documentsFolderPath(void) {
	if (!sDocumentsFolderPath) {
		NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		if ([paths count] == 0) {
			return nil;
		}
		
		sDocumentsFolderPath = [paths objectAtIndex:0];
		NSFileManager* fm = [NSFileManager defaultManager];
		if (![fm fileExistsAtPath:sDocumentsFolderPath]) {
			[fm createDirectoryAtPath:sDocumentsFolderPath withIntermediateDirectories:YES attributes:nil error:NULL];
		}
	}
	return sDocumentsFolderPath;
}

int main(int argc, char * argv[])
{
    @autoreleasepool {
        return UIApplicationMain(argc, argv, nil, NSStringFromClass([SRAppDelegate class]));
    }
}
