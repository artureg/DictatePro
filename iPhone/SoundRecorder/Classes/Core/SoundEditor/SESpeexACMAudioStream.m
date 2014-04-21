//  SESpeexACMAudioStream.m
//  SoundRecorder
//  Created by Igor Danich igor.danich@wise-apps.com on 11.04.14.


#import "SESpeexACMAudioStream.h"
#import "SESpeexACMWrapper.h"
#import "SEAudioStream+Internal.h"

@interface SESpeexACMAudioStream()
@property(nonatomic,strong) SESpeexACMWrapper*  pv_wrapper;
@property(nonatomic,strong) NSMutableData*      pv_data;
- (void)pm_checkAndFlush;
@end

@implementation SESpeexACMAudioStream

- (id)initWithContentsOfFile:(NSString*)file {
    if (self = [super initWithContentsOfFile:file]) {
        self.pv_wrapper = [SESpeexACMWrapper new];
        if ([[NSFileManager defaultManager] fileExistsAtPath:file]) {
            [self openWithMode:kSEAudioStreamModeRead];
            [self close];
        }
    }
    return self;
}

- (BOOL)openWithMode:(TSEAudioStreamMode)mode {
    if (self.mode != kSEAudioStreamModeUnknown) {
        return NO;
    }
    if (mode == kSEAudioStreamModeUnknown) {
        return [self close];
    }
    NSString* file = [[self.URL resourceSpecifier] stringByReplacingOccurrencesOfString:@"%20" withString:@" "];
    BOOL e = NO;
    switch (mode) {
        case kSEAudioStreamModeRead: {
            e = [self.pv_wrapper openRead:file];
            AudioStreamBasicDescription aInfo;
            aInfo.mSampleRate = 16000;
            aInfo.mChannelsPerFrame = 1;
            aInfo.mFramesPerPacket = 1;
            aInfo.mBitsPerChannel = 16;
            aInfo.mBytesPerFrame = 2;
            aInfo.mBytesPerPacket =  2;
            aInfo.mFormatID = kAudioFormatLinearPCM;
            aInfo.mFormatFlags = kLinearPCMFormatFlagIsSignedInteger|kLinearPCMFormatFlagIsPacked;
            [self adjustToAudioDescription:aInfo];
        }break;
        case kSEAudioStreamModeWrite: {
            if ([[NSFileManager defaultManager] fileExistsAtPath:file]) {
                [[NSFileManager defaultManager] removeItemAtPath:file error:nil];
            }
            e = [self.pv_wrapper openWrite:file];
        }break;
        default:
            break;
    }
    if (e) {
        [self setMode:mode];
    }
    return e;
}

- (BOOL)close {
    if (self.mode == kSEAudioStreamModeWrite) {
        [self pm_checkAndFlush];;
    }
    [self.pv_wrapper close];
    [self setMode:kSEAudioStreamModeUnknown];
    return YES;
}

- (NSTimeInterval)duration {
    return self.pv_wrapper.duration;
}

- (void)adjustToAudioDescription:(AudioStreamBasicDescription)aInfo {
    [super adjustToAudioDescription:aInfo];
    if (self.mode == kSEAudioStreamModeWrite) {
        [self.pv_wrapper adjustToSampleRate:aInfo.mSampleRate bytesPerSample:aInfo.mBytesPerFrame quality:8];
    }
}

- (BOOL)writeData:(NSData*)samples {
    if (self.mode != kSEAudioStreamModeWrite) {
        return NO;
    }
    if (!self.pv_data) {
        self.pv_data = [NSMutableData data];
    }
    [self.pv_data appendData:samples];
//    [self pm_checkAndFlush];
    return YES;
}

- (void)pm_checkAndFlush {
    NSUInteger packetSize = self.pv_wrapper.expectedPacketSize;
    NSInteger numberOfPackets = [self.pv_data length]/packetSize;
    if (numberOfPackets > 0) {
        NSRange range = NSMakeRange(0, numberOfPackets*packetSize);
        NSData* data = [self.pv_data subdataWithRange:range];
        [self.pv_wrapper writeData:data];
        [self.pv_data replaceBytesInRange:range withBytes:nil length:0];
    }
}

- (BOOL)readData:(NSMutableData*)data position:(NSUInteger)position duration:(NSUInteger)duration {
    if (self.otherStream) {
        return [self.otherStream readData:data position:position duration:duration];
    } else {
        [self.pv_wrapper readData:data position:position/1000.0f duration:duration/1000.0f];
        return YES;
    }
}

//- (NSTimeInterval)durationForBufferWithSize:(NSUInteger)size {
//    return [self.pv_wrapper durationForBufferWithSize:size];
//}

@end
