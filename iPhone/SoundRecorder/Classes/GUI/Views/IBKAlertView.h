//
//  IBKAlertView.h
//  iCTA
//
//  Created by Igor on 10.01.13.
//  Copyright (c) 2013 PJ-Software. All rights reserved.
//

typedef void (^TIBKAlertViewCompletion)(NSInteger buttonIndex);
typedef void (^TIBKAlertViewTextCompletion)(NSInteger buttonIndex, NSString* text);

@interface IBKAlertView : UIAlertView
- (void)showWithPlaceholder:(NSString*)placeholder textCompletion:(TIBKAlertViewTextCompletion)completion;
- (void)showWithCompletion:(TIBKAlertViewCompletion)completion;
@end
