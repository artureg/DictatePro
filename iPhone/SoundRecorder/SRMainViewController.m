//
//  SRMainViewController.m
//  SoundRecorder
//
//  Created by Igor on 12/13/13.
//  Copyright (c) 2013 Igor. All rights reserved.
//

#import "SRMainViewController.h"
#import "SRAddSoundViewController.h"

#import "SRSoundList.h"

@interface SRMainViewController()<UITableViewDataSource,UITableViewDelegate,SRSoundListDelegate>
@property(nonatomic,weak) IBOutlet UITableView*             pv_tableView;
@property(nonatomic,weak) IBOutlet UILabel*                 pv_progressLbl;
@property(nonatomic,weak) IBOutlet UIProgressView*          pv_progressView;
@property(nonatomic,weak) IBOutlet UIButton*                pv_playBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_stopBtn;
@property(nonatomic,weak) IBOutlet UIActivityIndicatorView* pv_indicator;
@property(nonatomic,strong) SRSoundList*                    pv_soundList;
- (IBAction)pm_onPlay:(id)sender;
- (IBAction)pm_onStop:(id)sender;
- (IBAction)pm_clearAll:(id)sender;
- (void)pm_resetPlay;
- (void)pm_onAddSound;
- (void)pm_onEdit;
- (void)pm_onDone;
@end

@implementation SRMainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc]
        initWithBarButtonSystemItem:UIBarButtonSystemItemEdit
        target:self
        action:@selector(pm_onEdit)];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc]
        initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
        target:self
        action:@selector(pm_onAddSound)];
    if (!self.pv_soundList) {
        self.pv_soundList = [SRSoundList new];
        self.pv_soundList.delegate = self;
    }
    self.navigationItem.leftBarButtonItem.enabled = ([self.pv_soundList.sounds count] > 0);
}

- (void)pm_onEdit {
    [self.pv_tableView setEditing:YES animated:YES];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc]
        initWithBarButtonSystemItem:UIBarButtonSystemItemDone
        target:self
        action:@selector(pm_onDone)];
}

- (void)pm_onDone {
    [self.pv_tableView setEditing:NO animated:YES];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc]
        initWithBarButtonSystemItem:UIBarButtonSystemItemEdit
        target:self
        action:@selector(pm_onEdit)];
    self.navigationItem.leftBarButtonItem.enabled = ([self.pv_soundList.sounds count] > 0);
}

- (void)pm_onAddSound {
    SRAddSoundViewController* ctrl = [[SRAddSoundViewController alloc] initWithNibName:@"SRAddSoundViewController" bundle:nil];
    ctrl.soundList = self.pv_soundList;
    [self.navigationController pushViewController:ctrl animated:YES];
}

- (IBAction)pm_clearAll:(id)sender {
    [self.pv_soundList clearAll];
    self.pv_playBtn.enabled = (self.pv_soundList.duration > 0);
    self.pv_progressLbl.text = [NSString stringWithFormat:@"0 / %3.1f", self.pv_soundList.duration];
    [self.pv_tableView reloadData];
}

- (void)pm_resetPlay {
    [self.pv_soundList clearSound];
    self.pv_stopBtn.hidden = YES;
    self.pv_playBtn.hidden = NO;
    self.pv_playBtn.enabled = (self.pv_soundList.duration > 0);
    self.pv_progressView.progress = 0;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"0 / %3.1f", self.pv_soundList.duration];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationItem.leftBarButtonItem.enabled = ([self.pv_soundList.sounds count] > 0);
    [self pm_resetPlay];
    [self.pv_tableView reloadData];
}

- (IBAction)pm_onPlay:(id)sender {
    if (self.pv_tableView.isEditing) {
        [self pm_onDone];
    }
    [self.pv_soundList play];
}

- (IBAction)pm_onStop:(id)sender {
    [self.pv_soundList stop];
}

#pragma mark - UITableViewDataSource and UITableViewDelegate

- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath {
    static NSString* const cellID = @"CellID";
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:cellID];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellID];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    }
    SRSound* sound = self.pv_soundList.sounds[indexPath.row];
    cell.textLabel.text = [NSString stringWithFormat:@"Duration: %3.1f", sound.duration];
    return cell;
}

- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.pv_soundList.sounds count];
}

- (void)tableView:(UITableView*)tableView didSelectRowAtIndexPath:(NSIndexPath*)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (self.pv_soundList.isPlaying) {
        return;
    }
    SRAddSoundViewController* ctrl = [[SRAddSoundViewController alloc] initWithNibName:@"SRAddSoundViewController" bundle:nil];
    ctrl.soundList = self.pv_soundList;
    ctrl.sound = self.pv_soundList.sounds[indexPath.row];
    [self.navigationController pushViewController:ctrl animated:YES];
}

- (void)tableView:(UITableView*)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath*)indexPath {
    [self.pv_soundList clearSound];
    switch (editingStyle) {
        case UITableViewCellEditingStyleDelete: {
            [self.pv_soundList removeSound:self.pv_soundList.sounds[indexPath.row]];
            [tableView beginUpdates];
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
            [tableView endUpdates];
        }break;
        default:
            break;
    }
    self.pv_playBtn.enabled = (self.pv_soundList.duration > 0);
    self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, self.pv_soundList.duration];
    if ([self.pv_soundList.sounds count] == 0) {
        [self pm_onDone];
    }
}

- (BOOL)tableView:(UITableView*)tableView canMoveRowAtIndexPath:(NSIndexPath*)indexPath {
    return YES;
}

- (void)tableView:(UITableView*)tableView moveRowAtIndexPath:(NSIndexPath*)sourceIndexPath toIndexPath:(NSIndexPath*)destinationIndexPath {
    if (sourceIndexPath == destinationIndexPath) {
        return;
    }
    [self.pv_soundList clearSound];
    [self.pv_soundList moveSound:self.pv_soundList.sounds[sourceIndexPath.row] toIndex:destinationIndexPath.row];
}

#pragma mark - SRSoundListDelegate

- (void)soundListDidStartPreparing:(SRSoundList*)sList {
    self.navigationItem.leftBarButtonItem.enabled = NO;
    self.pv_stopBtn.hidden = NO;
    self.pv_stopBtn.enabled = NO;
    self.pv_playBtn.hidden = YES;
    self.pv_playBtn.enabled = NO;
    [self.pv_indicator startAnimating];
}

- (void)soundListDidStartPlaying:(SRSoundList*)sList {
    self.pv_stopBtn.hidden = NO;
    self.pv_stopBtn.enabled = YES;
    self.pv_playBtn.hidden = YES;
    self.pv_playBtn.enabled = NO;
    [self.pv_indicator stopAnimating];
}

- (void)soundList:(SRSoundList*)sList playingTimer:(NSTimeInterval)time duration:(NSTimeInterval)duration {
    self.pv_progressView.progress = time/duration;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", time, duration];
}

- (void)soundListDidEndPlaying:(SRSoundList*)sList {
    self.navigationItem.leftBarButtonItem.enabled = ([self.pv_soundList.sounds count] > 0);
    self.pv_playBtn.hidden = NO;
    self.pv_playBtn.enabled = (self.pv_soundList.duration > 0);
    self.pv_stopBtn.hidden = YES;
    self.pv_progressView.progress = 0;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, self.pv_soundList.duration];
}

- (void)soundListDidBeginSplit:(SRSoundList*)sList {
    [self.pv_soundList clearSound];
    [self.pv_indicator startAnimating];
    self.pv_playBtn.enabled = NO;
    self.navigationItem.leftBarButtonItem.enabled = NO;
    self.navigationItem.rightBarButtonItem.enabled = NO;
}

- (void)soundListDidEndSplit:(SRSoundList*)sList sound1:(SRSound*)sound1 sound2:(SRSound*)sound2 {
    self.navigationItem.leftBarButtonItem.enabled = YES;
    self.navigationItem.rightBarButtonItem.enabled = YES;
    self.pv_playBtn.enabled = (self.pv_soundList.duration > 0);
    [self.pv_indicator stopAnimating];
    [self.pv_tableView reloadData];
}

@end
