//
//  SEAudioStream.m
//  SoundRecorder
//
//  Created by Igor on 11.04.14.

//

#import "SEAudioStream.h"

typedef enum {
    kSEAudioStreamSourceNone        = 0,
    kSEAudioStreamSourceFile        = 1,
    kSEAudioStreamSourceURL         = 2,
    kSEAudioStreamSourceOtherStream = 3
}TSEAudioStreamSourceType;

typedef enum {
    kSEAudioStreamSuccess,
    kSEAudioStreamUnknownMode,
    kSEAudioStreamCloseBeforeOpen,
    kSEAudioStreamCantOpenStream,
    kSEAudioStreamCantReadAudioDescription,
    kSEAudioStreamCantReadAudioFile,
    kSEAudioStreamCantReadWithCurrentMode,
    kSEAudioStreamCantWriteWithCurrentMode,
    kSEAudioStreamCantWriteStream
}TSEAudioStreamStatusType;

@interface SEAudioStream()
@property(nonatomic,assign) TSEAudioStreamMode          pv_mode;
@property(nonatomic,assign) TSEAudioStreamSourceType    pv_type;
@property(nonatomic,strong) NSMutableData*              pv_data;
@property(nonatomic,strong) SEAudioStream*              pv_otherAudioStream;
@property(nonatomic,strong) NSURL*                      pv_url;
@property(nonatomic,strong) NSError*                    pv_error;
@property(nonatomic,assign) AudioFileStreamID           pv_audioFileStream;
@property(nonatomic,strong) NSFileHandle*               pv_fileHandle;
@property(nonatomic,assign) AudioStreamBasicDescription pv_audioDescription;
@property(nonatomic,assign) NSUInteger                  pv_dataLength;
@property(nonatomic,assign) NSUInteger                  pv_dataOffset;
@property(nonatomic,assign) AudioFileTypeID             pv_hint;
@property(nonatomic,assign) AudioFileID                 pv_writeFile;

+ (AudioFileTypeID)pm_hintForFileExtension:(NSString*)fileExtension;

- (void)pm_performError:(TSEAudioStreamStatusType)statusType;

- (void)pm_handlePropertyChangeForFileStream:(AudioFileStreamID)inAudioFileStream
    fileStreamPropertyID:(AudioFileStreamPropertyID)inPropertyID
    ioFlags:(UInt32*)ioFlags;
- (void)pm_handleAudioPackets:(const void*)inInputData
    numberBytes:(UInt32)inNumberBytes
    numberPackets:(UInt32)inNumberPackets
    packetDescriptions:(AudioStreamPacketDescription*)inPacketDescriptions;

- (BOOL)pm_readHeader;

- (NSTimeInterval)pm_dataDuration;

- (BOOL)pm_flush;

@end

static void SEASPropertyListenerProc(
    void* inClientData,
    AudioFileStreamID inAudioFileStream,
    AudioFileStreamPropertyID inPropertyID,
    UInt32* ioFlags) {
    SEAudioStream* stream = (__bridge SEAudioStream*)inClientData;
    [stream pm_handlePropertyChangeForFileStream:inAudioFileStream fileStreamPropertyID:inPropertyID ioFlags:ioFlags];
}

static void SEASPacketsProc(
    void* inClientData,
    UInt32 inNumberBytes,
    UInt32 inNumberPackets,
    const void* inInputData,
    AudioStreamPacketDescription* inPacketDescriptions) {
    SEAudioStream* stream = (__bridge SEAudioStream*)inClientData;
    [stream pm_handleAudioPackets:inInputData numberBytes:inNumberBytes numberPackets:inNumberPackets packetDescriptions:inPacketDescriptions];
}

@implementation SEAudioStream

+ (AudioFileTypeID)pm_hintForFileExtension:(NSString*)fileExtension {
	AudioFileTypeID fileTypeHint = kAudioFileAAC_ADTSType;
	if ([fileExtension isEqual:@"mp3"]) {
		fileTypeHint = kAudioFileMP3Type;
	} else if ([fileExtension isEqual:@"wav"]) {
		fileTypeHint = kAudioFileWAVEType;
	} else if ([fileExtension isEqual:@"aifc"]) {
		fileTypeHint = kAudioFileAIFCType;
	} else if ([fileExtension isEqual:@"aiff"]) {
		fileTypeHint = kAudioFileAIFFType;
	} else if ([fileExtension isEqual:@"m4a"]) {
		fileTypeHint = kAudioFileM4AType;
	} else if ([fileExtension isEqual:@"mp4"]) {
		fileTypeHint = kAudioFileMPEG4Type;
	} else if ([fileExtension isEqual:@"caf"]) {
		fileTypeHint = kAudioFileCAFType;
    } else if ([fileExtension isEqual:@"aac"]) {
		fileTypeHint = kAudioFileAAC_ADTSType;
	}
	return fileTypeHint;
}

- (void)dealloc {
    [self close];
}

#pragma mark - Initialization

- (instancetype)init {
    if (self = [super init]) {
        self.pv_type = kSEAudioStreamSourceNone;
        self.pv_mode = kSEAudioStreamModeUnknown;
    }
    return self;
}

- (instancetype)initWithAudioStream:(SEAudioStream*)stream {
    if (self = [super init]) {
        self.pv_type = kSEAudioStreamSourceOtherStream;
        self.pv_otherAudioStream = stream;
        self.pv_mode = kSEAudioStreamModeUnknown;
    }
    return self;
}

- (instancetype)initWithURL:(NSURL*)url {
    if (self = [super init]) {
        self.pv_type = kSEAudioStreamSourceURL;
        self.pv_url = url;
        self.pv_mode = kSEAudioStreamModeUnknown;
    }
    return self;
}

- (instancetype)initWithContentsOfFile:(NSString*)file {
    if (self = [super init]) {
        self.pv_type = kSEAudioStreamSourceFile;
        self.pv_url = [NSURL fileURLWithPath:file];
        self.pv_mode = kSEAudioStreamModeUnknown;
        if ([[NSFileManager defaultManager] fileExistsAtPath:file]) {
            [self openWithMode:kSEAudioStreamModeRead];
            [self close];
        }
    }
    return self;
}

#pragma mark - Audio Info

- (NSTimeInterval)pm_dataDuration {
    double bytesPerSecond = self.audioDescription.mBytesPerFrame*self.audioDescription.mSampleRate/self.audioDescription.mChannelsPerFrame;
    return (double)[self.pv_data length]/bytesPerSecond;
}

- (NSTimeInterval)duration {
    switch (self.pv_type) {
        case kSEAudioStreamSourceNone: {
            return [self pm_dataDuration];
        }break;
        case kSEAudioStreamSourceFile:case kSEAudioStreamSourceURL: {
            double bytesPerSecond = self.audioDescription.mBytesPerFrame*self.audioDescription.mSampleRate/self.audioDescription.mChannelsPerFrame;
            return (double)self.pv_dataLength/bytesPerSecond;
        }break;
        case kSEAudioStreamSourceOtherStream: {
            return self.pv_otherAudioStream.duration;
        }break;
        default:
            return 0;
    }
}

- (AudioStreamBasicDescription)audioDescription {
    if (self.pv_type == kSEAudioStreamSourceOtherStream) {
        return self.pv_otherAudioStream.audioDescription;
    } else {
        return self.pv_audioDescription;
    }
}

- (NSURL*)URL {
    return self.pv_url;
}

- (NSError*)error {
    return self.pv_error;
}

- (NSUInteger)length {
    return self.pv_dataLength;
}

#pragma mark - Open/Close/Clear

- (TSEAudioStreamMode)mode {
    return self.pv_mode;
}

- (BOOL)openWithMode:(TSEAudioStreamMode)mode {
    if (self.pv_mode != kSEAudioStreamModeUnknown) {
        [self pm_performError:kSEAudioStreamCloseBeforeOpen];
        return NO;
    }
    self.pv_error = nil;
    self.pv_mode = mode;
    switch (mode) {
        case kSEAudioStreamModeRead: {
            switch (self.pv_type) {
                case kSEAudioStreamSourceNone: {
                    self.pv_data = [NSMutableData data];
                    return YES;
                }break;
                case kSEAudioStreamSourceFile:case kSEAudioStreamSourceURL: {
                    NSError* error = nil;
                    self.pv_fileHandle = [NSFileHandle fileHandleForReadingFromURL:self.pv_url error:&error];
                    if (error) {
                        self.pv_error = error;
                        [self close];
                        return NO;
                    }
                    AudioFileTypeID hint = [SEAudioStream pm_hintForFileExtension:[self.pv_url pathExtension]];
                    self.pv_hint = hint;
                    OSStatus status = AudioFileStreamOpen((__bridge void*)self, SEASPropertyListenerProc, SEASPacketsProc, hint, &_pv_audioFileStream);
                    if (status) {
                        [self pm_performError:kSEAudioStreamCantOpenStream];
                        [self close];
                        return NO;
                    }
                    if (![self pm_readHeader]) {
                        [self pm_performError:kSEAudioStreamCantReadAudioDescription];
                        [self close];
                        return NO;
                    }
                    self.pv_data = [NSMutableData data];
                    return YES;
                }break;
                default:
                    return NO;
            }
        }break;
        case kSEAudioStreamModeWrite: {
            switch (self.pv_type) {
                case kSEAudioStreamSourceNone: {
                    self.pv_data = [NSMutableData data];
                    return YES;
                }break;
                case kSEAudioStreamSourceFile: {
                    NSError* error = nil;
                    NSString* path = [[self.pv_url resourceSpecifier] stringByReplacingOccurrencesOfString:@"%20" withString:@" "];
                    if ([[NSFileManager defaultManager] fileExistsAtPath:path]) {
                        if ([[NSFileManager defaultManager] removeItemAtPath:path error:&error]) {
                            if (error) {
                                self.pv_error = error;
                                return NO;
                            }
                        }
                    } else {
                        if ([[NSFileManager defaultManager] createFileAtPath:path contents:nil attributes:nil]) {
                            [[NSFileManager defaultManager] removeItemAtPath:path error:nil];
                            return YES;
                        } else {
                            [self pm_performError:kSEAudioStreamCantOpenStream];
                            return NO;
                        }
                    }
                    return YES;
                }break;
                default:
                    return NO;
            }
            return NO;
        }break;
        default:
            return NO;
    }
}

- (BOOL)close {
    [self.pv_fileHandle closeFile];
    self.pv_fileHandle = nil;
    if (self.pv_audioFileStream) {
        AudioFileStreamClose(self.pv_audioFileStream);
        self.pv_audioFileStream = NULL;
    }
    if (![self pm_flush]) {
        self.pv_data = nil;
        self.pv_mode = kSEAudioStreamModeUnknown;
        [self pm_performError:kSEAudioStreamCantWriteStream];
        return NO;
    }
    self.pv_data = nil;
    self.pv_mode = kSEAudioStreamModeUnknown;
    return YES;
}

- (BOOL)clear {
    return YES;
}

#pragma mark - Writing

- (void)adjustToAudioDescription:(AudioStreamBasicDescription)aInfo {
    self.pv_audioDescription = aInfo;
}

- (void)writeData:(NSData*)samples {
    if (!self.pv_data) {
        self.pv_data = [NSMutableData data];
    }
    self.pv_dataLength += [samples length];
    [self.pv_data appendData:samples];
}

- (BOOL)pm_flush {
    if ((self.mode == kSEAudioStreamModeWrite)&&([self.pv_data length] > 0)) {
        AudioFileID file;
        OSStatus status = AudioFileCreateWithURL(
            (__bridge CFURLRef)self.pv_url,
            kAudioFileCAFType,
            &_pv_audioDescription,
            kAudioFileFlags_EraseFile,
            &file
        );
        if (status) {
            return NO;
        }
        UInt32 length = [self.pv_data length];
        AudioFileWriteBytes(file, false, 0, &length, [self.pv_data bytes]);
        AudioFileClose(file);
    }
    return YES;
}

#pragma mark - Reading

- (BOOL)readData:(NSMutableData*)data position:(NSUInteger)position duration:(NSUInteger)duration {
    if (self.pv_mode != kSEAudioStreamModeRead) {
        [self pm_performError:kSEAudioStreamCantWriteWithCurrentMode];
        return NO;
    }
    NSUInteger offset = self.pv_audioDescription.mSampleRate*position*
        self.pv_audioDescription.mBytesPerFrame/(self.pv_audioDescription.mChannelsPerFrame*1000);
    offset += self.pv_dataOffset;
    [self.pv_fileHandle seekToFileOffset:offset];
    self.pv_data = [NSMutableData data];
    NSUInteger bytes = self.pv_audioDescription.mSampleRate*duration
        *self.pv_audioDescription.mBytesPerFrame/(self.pv_audioDescription.mChannelsPerFrame*1000);
    NSData* tmpData = [self.pv_fileHandle readDataOfLength:bytes];
    if ([tmpData length] == 0) {
        return NO;
    }
    OSStatus error = AudioFileStreamParseBytes(self.pv_audioFileStream, [tmpData length], [tmpData bytes], 0);
    if (error) {
        self.pv_data = nil;
        return NO;
    }
    [data appendData:tmpData];
    self.pv_data = nil;
    return YES;
}

#pragma mark - Export

- (void)exportToFile:(NSString*)filePath completion:(void(^)(NSError* error))completion {
}

#pragma mark - Private

- (void)pm_performError:(TSEAudioStreamStatusType)statusType {
    NSString* errorString = @"";
    switch (statusType) {
        case kSEAudioStreamSuccess:
            errorString = nil;
            break;
        case kSEAudioStreamCantOpenStream:
            errorString = @"Failed to open stream";
            break;
        case kSEAudioStreamCloseBeforeOpen:
            errorString = @"Close stream before open again";
            break;
        case kSEAudioStreamCantReadAudioDescription:
            errorString = @"Failed to read audio description";
            break;
        case kSEAudioStreamCantReadAudioFile:
            errorString = @"Failed to read audio file";
            break;
        case kSEAudioStreamCantReadWithCurrentMode:
            errorString = @"Failed to read with current mode";
            break;
        case kSEAudioStreamCantWriteWithCurrentMode:
            errorString = @"Failed to write data with current mode";
            break;
        default:
            errorString = nil;
            break;
    }
    if (errorString) {
        self.pv_error = [NSError errorWithDomain:@"com.seaudiostream" code:statusType userInfo:@{NSLocalizedDescriptionKey: errorString}];
    } else {
        self.pv_error = nil;
    }
}

- (BOOL)pm_readHeader {
    NSData* data = [self.pv_fileHandle readDataOfLength:1024];
    OSStatus status = AudioFileStreamParseBytes(self.pv_audioFileStream, [data length], [data bytes], 0);
    if (status) {
        return NO;
    }
    return (self.pv_audioDescription.mSampleRate != 0);
}

- (void)pm_handlePropertyChangeForFileStream:(AudioFileStreamID)inAudioFileStream
    fileStreamPropertyID:(AudioFileStreamPropertyID)inPropertyID
    ioFlags:(UInt32*)ioFlags {
	@synchronized(self) {
        OSStatus error;
        switch (inPropertyID) {
            case kAudioFileStreamProperty_DataOffset: {
                SInt64 offset;
                UInt32 offsetSize = sizeof(offset);
                error = AudioFileStreamGetProperty(inAudioFileStream, kAudioFileStreamProperty_DataOffset, &offsetSize, &offset);
                if (error) {
                    [self pm_performError:kSEAudioStreamCantReadAudioFile];
                    [self close];
                    return;
                }
                self.pv_dataOffset = (NSUInteger)offset;
            }break;
            case kAudioFileStreamProperty_AudioDataByteCount: {
                UInt64 dataSize;
                UInt32 byteCountSize = sizeof(UInt64);
                error = AudioFileStreamGetProperty(inAudioFileStream, kAudioFileStreamProperty_AudioDataByteCount, &byteCountSize, &dataSize);
                if (error) {
                    [self pm_performError:kSEAudioStreamCantReadAudioFile];
                    return;
                }
                self.pv_dataLength = (NSUInteger)dataSize;
            }break;
            case kAudioFileStreamProperty_DataFormat: {
                if (self.audioDescription.mSampleRate == 0) {
                    UInt32 size = sizeof(self.pv_audioDescription);
                    AudioStreamBasicDescription desc;
                    
                    error = AudioFileStreamGetProperty(inAudioFileStream, kAudioFileStreamProperty_DataFormat, &size, &desc);
                    self.pv_audioDescription = desc;
                    if (error) {
                        [self pm_performError:kSEAudioStreamCantReadAudioDescription];
                        [self close];
                        return;
                    }
                }
            }break;
            case kAudioFileStreamProperty_FormatList: {
                Boolean outWriteable;
                UInt32 formatListSize;
                error = AudioFileStreamGetPropertyInfo(inAudioFileStream, kAudioFileStreamProperty_FormatList, &formatListSize, &outWriteable);
                if (error) {
                    [self pm_performError:kSEAudioStreamCantReadAudioDescription];
                    return;
                }
                
                AudioFormatListItem *formatList = malloc(formatListSize);
                error = AudioFileStreamGetProperty(inAudioFileStream, kAudioFileStreamProperty_FormatList, &formatListSize, formatList);
                if (error) {
                    free(formatList);
                    [self pm_performError:kSEAudioStreamCantReadAudioDescription];
                    return;
                }
                
                for (int i = 0; i * sizeof(AudioFormatListItem) < formatListSize; i += sizeof(AudioFormatListItem)) {
                    AudioStreamBasicDescription pasbd = formatList[i].mASBD;
                    if (pasbd.mFormatID == kAudioFormatMPEG4AAC_HE ||
                        pasbd.mFormatID == kAudioFormatMPEG4AAC_HE_V2) {
                        self.pv_audioDescription = pasbd;
                        break;
                    }                                
                }
                free(formatList);
            }break;
            default:
                break;
        }
	}
}

- (void)pm_handleAudioPackets:(const void*)inInputData
    numberBytes:(UInt32)inNumberBytes
    numberPackets:(UInt32)inNumberPackets
    packetDescriptions:(AudioStreamPacketDescription*)inPacketDescriptions {
	@synchronized(self) {
        [self.pv_data appendBytes:inInputData length:inNumberBytes];
	}
}

@end
