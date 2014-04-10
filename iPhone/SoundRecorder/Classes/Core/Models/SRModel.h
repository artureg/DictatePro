//
//  SRModel.h
//  SoundRecorder
//
//  Created by Igor Danich
//  Property of UAB "Mobilios Aplikacijos"
//

@interface SRModel : NSObject
- (id)initWithContentsOfFile:(NSString*)filePath;
- (id)initWithDictionary:(NSDictionary*)dictionary;
@end

@interface SRModel(Protected)
- (void)updateToDictionary:(NSDictionary*)dictionary;
- (NSMutableDictionary*)dictionaryRepresentation;
@end
