//  SRModel.h
//  SoundRecorder//  Created by Igor Danich igor.danich@wise-apps.com Danich

@interface SEModel : NSObject
- (instancetype)initWithContentsOfFile:(NSString*)filePath;
- (instancetype)initWithDictionary:(NSDictionary*)dictionary;
@end

@interface SEModel(Protected)
- (void)updateToDictionary:(NSDictionary*)dictionary;
- (NSMutableDictionary*)dictionaryRepresentation;
@end
