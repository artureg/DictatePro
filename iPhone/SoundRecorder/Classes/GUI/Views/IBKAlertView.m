//
//  IBKAlertView.m
//  iCTA
//
//  Created by Igor on 10.01.13.
//  Copyright (c) 2013 PJ-Software. All rights reserved.
//

#import "IBKAlertView.h"

@interface IBKAlertView()<UIAlertViewDelegate>
@property(nonatomic,copy) TIBKAlertViewCompletion     pv_completion;
@property(nonatomic,copy) TIBKAlertViewTextCompletion pv_textCompletion;
@end

@implementation IBKAlertView


- (void)showWithPlaceholder:(NSString*)placeholder textCompletion:(TIBKAlertViewTextCompletion)completion {
    self.alertViewStyle = UIAlertViewStylePlainTextInput;
    UITextField* txtFld = [self textFieldAtIndex:0];
    [txtFld setPlaceholder:placeholder];
    self.pv_textCompletion = completion;
    [self performSelectorOnMainThread:@selector(show) withObject:nil waitUntilDone:NO];
}

- (void)showWithCompletion:(TIBKAlertViewCompletion)completion {
    self.pv_completion = completion;
    [self performSelectorOnMainThread:@selector(show) withObject:nil waitUntilDone:NO];
}

- (void)setDelegate:(id)delegate {
    [super setDelegate:self];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    switch (self.alertViewStyle) {
        case UIAlertViewStylePlainTextInput:
            if (self.pv_textCompletion) {
                self.pv_textCompletion(buttonIndex, [[self textFieldAtIndex:0] text]);
                self.pv_textCompletion = nil;
            }
            break;
        default:
            if (self.pv_completion) {
                self.pv_completion(buttonIndex);
                self.pv_completion = nil;
            }
            break;
    }
    
}

@end
