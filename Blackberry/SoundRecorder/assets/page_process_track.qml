/**
 * Copyright (c) 2014 wise-apps.com
 * 
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 24.03.2014
 */

import bb.cascades 1.2
import bb.multimedia 1.0

/// Page for processing of tracks
Page {

    attachedObjects: [
        MediaPlayer {
            id: player
            sourceUrl: _mainView.getSelectedTrack();
            //_mainView.getTracksPath();
            statusInterval: 20
            
            //repeatMode: RepeatMode.All
            onPositionChanged: {
                if(player.mediaState != MediaState.Stopped) {
                    //console.log(" FFFFF position = " + position + " from " + player.duration);
                    progressBar.setValue(position);
                    var pos = position / 1000.00;
                    labelTimeSpent.text = pos.toFixed(2);
                }
            }
            onDurationChanged: {
                progressBar.toValue = duration;
                progressBar.fromValue = 0;
                labelTimeSum.text = duration / 1000.00;
                //String(Math.ceil( duration/1000.00 ).toPrecision(1));
                console.log(" FFFFF duration = " + duration + " from " + player.duration);
            
            }
            onStatusIntervalChanged: {
                console.log(" FFFFF statusInterval = " + statusInterval + " from " + player.duration);
            }
            onMediaStateChanged: {
                switch (player.mediaState) {
                    case MediaState.Started:
                        {
                            console.log("QML ediaPlayer state Started");
                            break;
                        }
                    case MediaState.Unprepared:
                        {
                            console.log("QML MediaPlayer state Unprepared");
                            break;
                        }
                    case MediaState.Prepared:
                        {
                            console.log("QML MediaPlayer state Prepared");
                            break;
                        }
                    case MediaState.Stopped: 
                        {
                            //progressBar.setValue(3800);
                            console.log("QML MediaPlayer state Stopped!!! = " + player.position);
                            btn_play.checked = false;
                            progressBar.setValue(0);
                            break;
                        }
                    default:
                        {
                            console.log("QML MediaPlayer state default");
                            break;
                        }
                }
            }
        }
    ]



    // ========= Menu =========
    actions: [
        ActionItem {
            id: actionSplit
            title: qsTr("Split")
            enabled: {
                if(btn_play_track.checked && player.duration > 1) {
                    
                    return true;
                } else {
                    
                    return false;
                }
                
            }
            imageSource: "asset:///images/ic/ic_cut.png"
            ActionBar.placement: ActionBarPlacement.OnBar

            onTriggered: {
                _mainView.split(player.duration);

            }
        }
    ]
    paneProperties: NavigationPaneProperties {
        backButton: ActionItem {
            onTriggered: {

                if (btn_record.checked) {
                    _mainView.onStopRecord();
                }
                _mainView.chooseTrack(null);
                _mainView.update();
                navigationPane.pop();

            }
        }
    }

    // ========= UI =========
    Container {
        background: Color.White

        Container {

            layout: StackLayout {
                orientation: LayoutOrientation.LeftToRight

            }

            leftPadding: 10.0
            rightPadding: 10.0
            leftMargin: 0.0
            rightMargin: 0.0
            preferredHeight: 300.0
            minHeight: 300.0
            maxHeight: 300.0

            verticalAlignment: VerticalAlignment.Fill
            horizontalAlignment: HorizontalAlignment.Fill

            ImageToggleButton {
                id: btn_record
                imageSourceDefault: "images/record.png"
                imageSourceDisabledUnchecked: "images/record.png"
                imageSourceChecked: "images/stop.png"
                imageSourceDisabledChecked: "images/stop.png"
                imageSourcePressedUnchecked: "images/record.png"
                imageSourcePressedChecked: "images/stop.png"
                topMargin: 0.0
                leftMargin: 0.0
                verticalAlignment: VerticalAlignment.Center
                accessibility.name: "TODO: Add property content"

                onCheckedChanged: {
                    if (checked) {
                        _mainView.onStartRecord();
                    } else {
                        _mainView.onStopRecord();
                        player.sourceUrl = _mainView.getSelectedTrack();
                    }
                }

            }

            ImageToggleButton {
                id: btn_play_track
                imageSourceDefault: "images/play.png"
                imageSourceDisabledUnchecked: "images/play_disabled.png"
                imageSourceChecked: "images/pause.png"
                imageSourceDisabledChecked: "images/pause.png"
                imageSourcePressedUnchecked: "images/play.png"
                imageSourcePressedChecked: "images/pause.png"
                topMargin: 0.0
                leftMargin: 0.0
                verticalAlignment: VerticalAlignment.Center
                accessibility.name: "TODO: Add property content"
                
                enabled: _mainView.isSelectedTrack

                onCheckedChanged: {
                    if (checked) {
                        player.play();
                    } else {
                        player.pause();
                    }
                }

            }

            Container {
                layout: StackLayout {
                    orientation: LayoutOrientation.TopToBottom
                }

                horizontalAlignment: HorizontalAlignment.Right
                verticalAlignment: VerticalAlignment.Center

                Container {
                    layout: StackLayout {
                        orientation: LayoutOrientation.LeftToRight
                    }
                    horizontalAlignment: HorizontalAlignment.Center

                    Label {
                        id: labelTimeSpent
                        text: "0.0"
                        textFit.minFontSizeValue: 12.0
                        textFit.maxFontSizeValue: 16.0
                        textStyle.color: Color.Black

                    }
                    Label {
                        text: "/"
                        textFit.minFontSizeValue: 12.0
                        textFit.maxFontSizeValue: 16.0
                        textStyle.color: Color.Black

                    }
                    Label {
                        id: labelTimeSum
                        text: "180"
                        textFit.minFontSizeValue: 12.0
                        textFit.maxFontSizeValue: 16.0
                        textStyle.color: Color.Black

                    }

                }

            }

        }

        ProgressIndicator {
            id: progressBar
            horizontalAlignment: HorizontalAlignment.Center
            topMargin: 5
            bottomMargin: 20
            // Show the progress bar only when computation is running
            //opacity: _mainView.active ? 1.0 : 0.0
            fromValue: _mainView.progressMinimum
            toValue: _mainView.progressMaximum
            value: _mainView.progressValue
        }

    }

}
