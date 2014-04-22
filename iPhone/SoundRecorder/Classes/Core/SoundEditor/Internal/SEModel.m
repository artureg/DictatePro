//  SRModel.m
//  SoundRecorder//  Created by Igor Danich igor.danich@wise-apps.com Danich

#import "SEModel.h"

@implementation SEModel

- (instancetype)initWithContentsOfFile:(NSString*)filePath {
    return [self initWithDictionary:[NSDictionary dictionaryWithContentsOfFile:filePath]];
}

- (instancetype)initWithDictionary:(NSDictionary*)dictionary {
    if (self = [super init]) {
        [self updateToDictionary:dictionary];
    }
    return self;
}

- (void)updateToDictionary:(NSDictionary*)dictionary {
}

- (NSMutableDictionary*)dictionaryRepresentation {
    return [NSMutableDictionary dictionary];
}

@end
