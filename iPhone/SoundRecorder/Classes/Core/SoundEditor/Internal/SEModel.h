//
//  SRModel.h
//  SoundRecorder
//
//  Created by Igor Danich
//  Property of UAB "Mobilios Aplikacijos"
//

@interface SEModel : NSObject
- (id)initWithContentsOfFile:(NSString*)filePath;
- (id)initWithDictionary:(NSDictionary*)dictionary;
@end

@interface SEModel(Protected)
- (void)updateToDictionary:(NSDictionary*)dictionary;
- (NSMutableDictionary*)dictionaryRepresentation;
@end
