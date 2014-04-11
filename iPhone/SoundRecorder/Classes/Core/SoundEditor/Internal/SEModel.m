//
//  SRModel.m
//  SoundRecorder
//
//  Created by Igor Danich
//  Property of UAB "Mobilios Aplikacijos"
//

#import "SEModel.h"

@implementation SEModel

- (id)initWithContentsOfFile:(NSString*)filePath {
    return [self initWithDictionary:[NSDictionary dictionaryWithContentsOfFile:filePath]];
}

- (id)initWithDictionary:(NSDictionary*)dictionary {
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
