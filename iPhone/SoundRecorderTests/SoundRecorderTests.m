//
//  SoundRecorderTests.m
//  SoundRecorderTests
//
//  Created by Igor Danich igor.danich@wise-apps.com Danich on 12/12/13.

//

#import <XCTest/XCTest.h>

#import "SEProject.h"
#import "SEProjectEngine.h"

#import "IBKTimer.h"

#import <Foundation/Foundation.h>

static __strong SEProjectEngine* gv_engine = nil;

@interface SoundRecorderTests : XCTestCase<SEAudioStreamEngineDelegate>
@property(nonatomic,strong) NSOperationQueue* pv_queue;
@end

@implementation SoundRecorderTests

- (void)setUp {
    [super setUp];
    if (!gv_engine) {
        gv_engine = [[SEProjectEngine alloc] initWithProject:[[SEProject alloc] initWithFolder:[NSHomeDirectory() stringByAppendingPathComponent:@"Documents"]]];
        gv_engine.delegate = self;
    }
    self.pv_queue = [NSOperationQueue new];
}

- (void)tearDown {
    [super tearDown];
}

- (void)test1 {
    [self.pv_queue addOperationWithBlock:^{
        [gv_engine startRecording];
    }];
    [NSThread sleepForTimeInterval:5];
    [self.pv_queue addOperationWithBlock:^{
        [gv_engine stopRecording];
        if (gv_engine.duration < 3) {
            XCTFail(@"Recording failed for \"%s\"", __PRETTY_FUNCTION__);
        }
    }];
}

- (void)test2 {
    [self.pv_queue addOperationWithBlock:^{
        gv_engine.currentTime = 1;
        [gv_engine startRecording];
    }];
    [NSThread sleepForTimeInterval:5];
    [self.pv_queue addOperationWithBlock:^{
        [gv_engine stopRecording];
        if (gv_engine.duration < 3) {
            XCTFail(@"Recording failed for \"%s\"", __PRETTY_FUNCTION__);
        }
    }];
}

- (void)test3 {
    [self.pv_queue addOperationWithBlock:^{
        [gv_engine startPlaying];
    }];
    [NSThread sleepForTimeInterval:5];
    [self.pv_queue addOperationWithBlock:^{
        [gv_engine stopPlaying];
    }];
}

#pragma mark - SEAudioStreamEngineDelegate

- (void)audioStreamEngine:(SEAudioStreamEngine*)engine didOccurError:(NSError*)error {
    XCTFail(@"\"%s\" Test failed with error '%@'", __PRETTY_FUNCTION__, [error localizedDescription]);
}

@end
