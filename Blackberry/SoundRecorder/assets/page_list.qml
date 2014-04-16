/**
 * Copyright (c) 2014 wise-apps.com
 * 
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 23.03.2014
 */

import bb.cascades 1.2
import bb.system 1.2
import "components"

import bb.multimedia 1.0


/// The main page
NavigationPane { 
    id: navigationPane
    Page {

        attachedObjects: [
            // file:///accounts/1000/appdata/com.wiseapps.davacon.blackberry.SoundRecorder.testDev_undRecorder9a02e415/data/tmp/tmp_file.wav
            MediaPlayer {
                id: player
                sourceUrl: _mainView.getTmpFile();
                    //_mainView.getTracksPath();
                statusInterval: 20
        
                //repeatMode: RepeatMode.All
                onPositionChanged: {
                    if(player.mediaState != MediaState.Stopped) {
	                   // listTrack.dataModel.data(0).
	                    console.log(" FFFFF position = " + position + " from " + player.duration);
	                    progressBar.setValue(position);
	                    var pos = position / 1000.00;
	                    //pos.toFixed(1); 
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
            },

            // ======== Dialogs ==========
            SystemDialog {
                id: dialogClearAll
                body: qsTr("Are you sure?")
                confirmButton.label: qsTr("Okay")
                confirmButton.enabled: true
                cancelButton.label: qsTr("Cancel")
                cancelButton.enabled: true
                onFinished: {
                    var x = result;
                    if (x == SystemUiResult.ConfirmButtonSelection) {
                        
                        console.log("dialogClearAll Okey");
                        _mainView.onClearAllTracks();

                    } else if (x == SystemUiResult.CancelButtonSelection) {
                        console.log("dialogClearAll cancel");
                    } else {
                        console.log("dialogClearAll error " + x);
                        console.log(dialogClearAll.error);
                    }
                }
            },
            // ======= Pages =======
            ComponentDefinition {
                id: viewProcess
                source: "asset:///page_process_track.qml"
            }

        ]

        // ========= Menu =========
        actions: [
            ActionItem {
                id: actionAddRecord
                title: qsTr("Add record")
                imageSource: "asset:///images/ic/ic_add.png"
                ActionBar.placement: ActionBarPlacement.OnBar

                onTriggered: {
                   var page = viewProcess.createObject();
                    navigationPane.push(page);
                    
                  //  _mainView.onPlayRecorded;
                    
                }
            },

            ActionItem {
                id: actionClearAll
                title: qsTr("Clear all")
                imageSource: "asset:///images/ic/ic_clear.png"
                ActionBar.placement: ActionBarPlacement.OnBar
                onTriggered: {
                    dialogClearAll.show();
                }
            }

        ]

        // ========= UI =========
        Container {
            background: Color.White

            Container {

                layout: DockLayout {
                }

                leftPadding: 50.0
                rightPadding: 50.0
                preferredHeight: 300.0
                minHeight: 300.0
                maxHeight: 300.0

                verticalAlignment: VerticalAlignment.Fill
                horizontalAlignment: HorizontalAlignment.Fill
                ImageToggleButton {
                    id: btn_play
                    
                    imageSourceDefault: "images/play.png"
                    imageSourceDisabledUnchecked: "images/play_disabled.png"
                    imageSourceChecked: "images/stop.png"
                    imageSourceDisabledChecked: "images/stop.png"
                    imageSourcePressedUnchecked: "images/play.png"
                    imageSourcePressedChecked: "images/stop.png"
                    verticalAlignment: VerticalAlignment.Center   
                   // enabled: _mainView.hasRecordedTracks  
                   // checked: {if(player.mediaState == MediaState.Started) {return true} else {return false}}   
                    onCheckedChanged: {
                        
                      // var dd =  _mainView.startPlaing();
                       
                        console.log("%%%tttt%%%%%% = ");
                        if(checked) {
                            //player.play();
                            _mainView.onPlayRecorded();
                            console.log("%%%%%%%%%% = play " + player.trackCount);
                        } else {
                            console.log("%%%stop%%%%%% = ");
                            _mainView.onPauseRecorded();
                           // player.stop();
                        }
                        
                    }

                }

                Container {
                    layout: StackLayout {
                        orientation: LayoutOrientation.TopToBottom
                    }

                    horizontalAlignment: HorizontalAlignment.Right
                    verticalAlignment: VerticalAlignment.Center

                    Button {
                        id: btnClearAll
                        text: qsTr("Clear All")
                        enabled: false
                        visible: false

                    }

                    Container {
                        layout: StackLayout {
                            orientation: LayoutOrientation.LeftToRight
                        }
                        horizontalAlignment: HorizontalAlignment.Center

                        Label {
                            id: labelTimeSpent
                            text: "0.0"
                            textFit.minFontSizeValue: 12.0
                            textFit.maxFontSizeValue: 12.0
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
                            text: "0.0"
                            textFit.minFontSizeValue: 12.0
                            textFit.maxFontSizeValue: 16.0
                            textStyle.color: Color.Black

                        }

                    }

                }

            }
            
            Container {
                      
                //background: Color.Black

                horizontalAlignment: HorizontalAlignment.Fill
                minHeight: 30.0
                leftPadding: 10.0
                rightPadding: 10.0

                ProgressIndicator {
                    id: progressBar
	                horizontalAlignment: HorizontalAlignment.Center
	                verticalAlignment: VerticalAlignment.Center
	                
                    // Show the progress bar only when computation is running
	                //opacity: _mainView.active ? 1.0 : 0.0
	
	                fromValue: _mainView.progressMinimum
	                toValue: _mainView.progressMaximum
	                value: player.position
	                
	                /*fromValue: 0
	                toValue: 100
	                value: 20*/
               
                }
             } // end of progress bar container

            Container {
                ListView {
                    id: listTrack
                    dataModel: _mainView.model   

                    listItemComponents: [
                        ListItemComponent {
                            type: "item"
                            
                            TrackItem {}

                        }
                    ]

                    onTriggered: {
                        var selectedItem = dataModel.data(indexPath);
                        _mainView.chooseTrack(selectedItem.url);
                        var page = viewProcess.createObject();
                        navigationPane.push(page);

                    }

                    contextActions: [
                        ActionSet {
                            ActionItem {
                                title: qsTr("Delete")
                                imageSource: "asset:///images/ic/ic_delete.png"
                                onTriggered: {
                                    var chosenItem = listTrack.selected();
                                    _mainView.deleteTrack(listTrack.dataModel.data(chosenItem).url);
                                }

                            }

                        } 
                    ] // contextActions

                }

            } // end of listTrack component

        }
        

    }
    
}
