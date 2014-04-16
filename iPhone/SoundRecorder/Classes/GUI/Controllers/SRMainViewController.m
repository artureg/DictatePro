//
//  SRMainViewController.m
//  SoundRecorder
//
//  Created by Igor Danich
//  Property of UAB "Mobilios Aplikacijos"
//

#import "SRMainViewController.h"
#import "SREditRecordViewController.h"

#import "SRProject.h"

#import "IBKAlertView.h"

@interface SRMainViewController()<UITableViewDataSource,UITableViewDelegate,SRSoundDelegate>
@property(nonatomic,weak) IBOutlet UITableView*             pv_tableView;
@property(nonatomic,weak) IBOutlet UILabel*                 pv_progressLbl;
@property(nonatomic,weak) IBOutlet UIProgressView*          pv_progressView;
@property(nonatomic,weak) IBOutlet UIButton*                pv_playBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_stopBtn;
@property(nonatomic,weak) IBOutlet UIActivityIndicatorView* pv_indicator;
@property(nonatomic,weak) IBOutlet UISegmentedControl*      pv_segmentControl;
@property(nonatomic,weak) IBOutlet UIView*                  pv_playControlView;
@property(nonatomic,strong) SRProject*                      pv_project;
- (IBAction)pm_onPlay:(id)sender;
- (IBAction)pm_onStop:(id)sender;
- (IBAction)pm_clearAll:(id)sender;
- (IBAction)pm_onSegmentControl:(id)sender;
- (void)pm_onAddRecordFromSound:(UIButton*)sender;
- (void)pm_checkPlay;
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
    if (!self.pv_project) {
        self.pv_project = [SRProject new];
    }
    self.navigationItem.leftBarButtonItem.enabled = ([self.pv_project.records count] > 0);
}

- (void)pm_onEdit {
    [self.pv_project.projectSound stop];
    [self pm_checkPlay];
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
    self.navigationItem.leftBarButtonItem.enabled = ([self.pv_project.records count] > 0);
}

- (void)pm_onAddSound {
    [self.pv_project.projectSound stop];
    SREditRecordViewController* ctrl = [[SREditRecordViewController alloc] initWithNibName:@"SREditRecordViewController" bundle:nil];
    ctrl.project = self.pv_project;
    [self.navigationController pushViewController:ctrl animated:YES];
}

- (IBAction)pm_clearAll:(id)sender {
    [self.pv_project.projectSound stop];
    [self.pv_project clearAll];
    [self pm_checkPlay];
    self.pv_playBtn.enabled = (self.pv_project.duration > 0);
    self.pv_progressLbl.text = [NSString stringWithFormat:@"0 / %3.1f", self.pv_project.duration];
    [self.pv_tableView reloadData];
    [self pm_onDone];
}

- (void)pm_checkPlay {
    self.pv_stopBtn.hidden = YES;
    self.pv_playBtn.hidden = NO;
    self.pv_playBtn.enabled = (self.pv_project.duration > 0);
    self.pv_progressView.progress = 0;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"0 / %3.1f", self.pv_project.duration];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationItem.leftBarButtonItem.enabled = ([self.pv_project.records count] > 0);
    self.pv_progressLbl.text = [NSString stringWithFormat:@"0 / %3.1f", self.pv_project.duration];
    [self pm_checkPlay];
    [self pm_onDone];
    [self.pv_tableView reloadData];
}

- (IBAction)pm_onPlay:(id)sender {
    if (self.pv_tableView.isEditing) {
        [self pm_onDone];
    }
    [self.pv_indicator startAnimating];
    self.pv_playBtn.hidden = YES;
    [self.pv_project buildProjectWithCompletion:^(NSError *error) {
        if (error) {
            self.pv_playBtn.hidden = NO;
            [self.pv_indicator stopAnimating];
            UIAlertView* alert = [[UIAlertView alloc]
                initWithTitle:nil
                message:[NSString stringWithFormat:@"%@", error]
                delegate:nil
                cancelButtonTitle:@"OK"
                otherButtonTitles:nil];
            [alert show];
        } else {
            [self.pv_project encodeToSpeexACMWithCompletion:^(NSError *error) {
                if (error) {
                    self.pv_playBtn.hidden = NO;
                    [self.pv_indicator stopAnimating];
                    UIAlertView* alert = [[UIAlertView alloc]
                        initWithTitle:nil
                        message:[NSString stringWithFormat:@"%@", error]
                        delegate:nil
                        cancelButtonTitle:@"OK"
                        otherButtonTitles:nil];
                    [alert show];
                } else {
                    self.pv_project.projectSound.delegate = self;
                    [self.pv_project.projectSound play];
                }
            }];
        }
    }];
}

- (IBAction)pm_onStop:(id)sender {
    [self.pv_project.projectSound stop];
    [self pm_checkPlay];
}

- (IBAction)pm_onSegmentControl:(id)sender {
    [self.pv_tableView reloadData];
    [UIView animateWithDuration:0.4f animations:^{
        if (self.pv_segmentControl.selectedSegmentIndex == 1) {
            self.pv_playControlView.alpha = 0;
            self.pv_tableView.frame = CGRectMake(
                0,
                self.pv_playControlView.frame.origin.y,
                self.view.frame.size.width,
                self.view.frame.size.height - self.pv_playControlView.frame.origin.y
            );
        } else {
            self.pv_playControlView.alpha = 1;
            self.pv_tableView.frame = CGRectMake(
                0,
                self.pv_playControlView.frame.origin.y + self.pv_playControlView.frame.size.height,
                self.view.frame.size.width,
                self.view.frame.size.height - (self.pv_playControlView.frame.origin.y + self.pv_playControlView.frame.size.height)
            );
        }
    }];
    self.pv_progressLbl.text = [NSString stringWithFormat:@"0 / %3.1f", self.pv_project.duration];
    [self pm_onDone];
    [self.pv_project.projectSound stop];
    [self pm_checkPlay];
}

- (void)pm_onAddRecordFromSound:(UIButton*)sender {
    SRSound* sound = self.pv_project.sounds[sender.tag];
    [sound clearTimeRange];
    SRRecord* record = [SRRecord new];
    record.soundPath = sound.filePath;
    record.timeRange = SRSoundRangeMake(0, sound.duration);
    [self.pv_project addRecord:record];
}

#pragma mark - UITableViewDataSource and UITableViewDelegate

- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath {
    static NSString* const cellID = @"CellID";
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:cellID];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellID];
    }
    switch (self.pv_segmentControl.selectedSegmentIndex) {
        case 0: {
            cell.accessoryView = nil;
            cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            SRRecord* record = self.pv_project.records[indexPath.row];
            cell.textLabel.text = [NSString stringWithFormat:@"Duration: %3.1f seconds", record.timeRange.duration];
            cell.detailTextLabel.text = nil;
            cell.selectionStyle = UITableViewCellSelectionStyleBlue;
        }break;
        case 1: {
            UIButton* btn = [UIButton buttonWithType:UIButtonTypeContactAdd];
            btn.tag = indexPath.row;
            [btn addTarget:self action:@selector(pm_onAddRecordFromSound:) forControlEvents:UIControlEventTouchUpInside];
            cell.accessoryView = btn;
            cell.accessoryType = UITableViewCellAccessoryNone;
            SRSound* sound = self.pv_project.sounds[indexPath.row];
            cell.textLabel.text = [sound.filePath lastPathComponent];
            cell.detailTextLabel.text = [NSString stringWithFormat:@"Duration: %3.1f seconds", sound.duration];
            cell.selectionStyle = UITableViewCellSelectionStyleNone;
        }break;
        default:
            break;
    }
    return cell;
}

- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section {
    switch (self.pv_segmentControl.selectedSegmentIndex) {
        case 0:
            return [self.pv_project.records count];
        case 1:
            return [self.pv_project.sounds count];
        default:
            break;
    }
    return 0;
}

- (void)tableView:(UITableView*)tableView didSelectRowAtIndexPath:(NSIndexPath*)indexPath {
    if (self.pv_segmentControl.selectedSegmentIndex == 1) {
        return;
    }
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (self.pv_project.projectSound.isPlaying) {
        return;
    }
    SREditRecordViewController* ctrl = [[SREditRecordViewController alloc] initWithNibName:@"SREditRecordViewController" bundle:nil];
    ctrl.project = self.pv_project;
    ctrl.record = self.pv_project.records[indexPath.row];
    [self.navigationController pushViewController:ctrl animated:YES];
}

- (void)tableView:(UITableView*)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath*)indexPath {
    switch (self.pv_segmentControl.selectedSegmentIndex) {
        case 0: {
            switch (editingStyle) {
                case UITableViewCellEditingStyleDelete: {
                    [self.pv_project deleteRecord:self.pv_project.records[indexPath.row]];
                    [tableView beginUpdates];
                    [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
                    [tableView endUpdates];
                }break;
                default:
                    break;
            }
            self.pv_playBtn.enabled = (self.pv_project.duration > 0);
            self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, self.pv_project.duration];
            if ([self.pv_project.records count] == 0) {
                [self pm_onDone];
            }
        }break;
        case 1: {
            IBKAlertView* aView = [[IBKAlertView alloc] initWithTitle:nil
                message:@"Some of records can be deleted. Proceed?"
                delegate:nil
                cancelButtonTitle:@"Cancel"
                otherButtonTitles:@"OK", nil];
            [aView showWithCompletion:^(NSInteger buttonIndex) {
                if (buttonIndex == 1) {
                    [self.pv_project deleteSound:self.pv_project.sounds[indexPath.row]];
                    [tableView beginUpdates];
                    [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
                    [tableView endUpdates];
                }
            }];
        }break;
        default:
            break;
    }
    [self pm_checkPlay];
}

- (BOOL)tableView:(UITableView*)tableView canMoveRowAtIndexPath:(NSIndexPath*)indexPath {
    return (self.pv_segmentControl.selectedSegmentIndex == 0);
}

- (void)tableView:(UITableView*)tableView moveRowAtIndexPath:(NSIndexPath*)sourceIndexPath toIndexPath:(NSIndexPath*)destinationIndexPath {
    if (sourceIndexPath == destinationIndexPath) {
        return;
    }
    [self.pv_project moveRecord:self.pv_project.records[sourceIndexPath.row] toIndex:destinationIndexPath.row];
}

#pragma mark - SRSoundDelegate

- (void)soundDidStartPlaying:(SRSound*)sound {
    [self.pv_indicator stopAnimating];
    self.pv_stopBtn.hidden = NO;
    self.pv_project.projectSound.delegate = self;
}

- (void)sound:(SRSound*)sound playPosition:(NSTimeInterval)time duration:(NSTimeInterval)duration {
    self.pv_progressView.progress = time/duration;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", time, duration];
}

- (void)soundDidEndPlaying:(SRSound*)sound {
    self.pv_stopBtn.hidden = YES;
    self.pv_playBtn.hidden = NO;
    self.pv_progressView.progress = 0;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"0 / %3.1f", self.pv_project.duration];
}

@end
