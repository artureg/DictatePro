/**
 * Copyright (c) 2014 wise-apps.com
 * 
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 23.03.2014
 */

import bb.cascades 1.2

// managed by MainController.cpp
Page {

    Container {
        
        background: Color.Gray

        verticalAlignment: VerticalAlignment.Fill
        horizontalAlignment: HorizontalAlignment.Fill

        Container { // progress bar container

            horizontalAlignment: HorizontalAlignment.Fill
            minHeight: 30.0
            leftPadding: 10.0
            rightPadding: 10.0
            topPadding: 40.0
            topMargin: 40.0
            bottomMargin: 10.0
            
            Slider {
                id: sliderPlayer
                horizontalAlignment: HorizontalAlignment.Center
                verticalAlignment: VerticalAlignment.Center
                fromValue: 0
                toValue: 1
                value: _mainController.progressValue
                property url valueBackground: "asset:///images/progress_scale.png"
            }

        } // end of progress bar container
        Container {
            leftPadding: 70.0
            bottomPadding: 30
            Label {
                text: _mainController.labelText
            }
        }
        Container { // the first row of buttons

            layout: StackLayout {
                orientation: LayoutOrientation.LeftToRight

            }

            horizontalAlignment: HorizontalAlignment.Center
            
            ImageButton {
                id: rewindButton
                enabled: _mainController.isEnableRewindButton
                defaultImageSource: "asset:///images/btn_rewind_default.png"
                disabledImageSource: "asset:///images/btn_rewind_disabled.png"
                onClicked: {
                    _mainController.onRewindButtonClick();
                }
                pressedImageSource: "asset:///images/btn_rewind_default.png"
            }

                ImageToggleButton {
                    id: playButton
//                    checked: _mainController.isCheckedPlayButton
                    enabled: _mainController.isEnablePlayButton
                    imageSourceDefault: "asset:///images/btn_play_default.png"
                    imageSourceDisabledUnchecked: "asset:///images/btn_play_disabled.png"
                    imageSourceDisabledChecked: "asset:///images/btn_play_disabled.png"
                    imageSourceChecked: "asset:///images/bnt_pause_default.png"
                    onCheckedChanged: {
                        if (playButton.checked) {
                            _mainController.onPlayButtonClick();
                        } else {
                            _mainController.onPauseButtonClick();
                        }
                    }
                    imageSourcePressedUnchecked: "asset:///images/btn_play_selected.png"
                    imageSourcePressedChecked: "asset:///images/btn_play_selected.png"
                    layoutProperties: FlowListLayoutProperties {

                    }
                }
                
            ImageButton {
                id: forwardButton
                enabled: _mainController.isEnableForwardButton
                defaultImageSource: "asset:///images/btn_forward_default.png"
                disabledImageSource: "asset:///images/btn_forward_disabled.png"
                onClicked: {
                    _mainController.onForwardButtonClick();
                }
                pressedImageSource: "asset:///images/btn_forward_default.png"
            }
        } // end of first row of buttons

        Container { // the second row of buttons

            layout: StackLayout {
                orientation: LayoutOrientation.LeftToRight

            }

            topMargin: 20.0
            horizontalAlignment: HorizontalAlignment.Center
            
            ImageButton {
                id: startButton
                enabled: _mainController.isEnableToStartButton
                defaultImageSource: "asset:///images/btn_start_default.png"
                disabledImageSource: "asset:///images/btn_start_disabled.png"
                onClicked: {
                    _mainController.onToStartButtonClick();
                }
                pressedImageSource: "asset:///images/btn_start_default.png"
            }
            
            ImageToggleButton {
                id: recordButton
                checked: _mainController.isCheckedRecordButton
                enabled: _mainController.isEnableRecordButton
                imageSourceDefault: "asset:///images/btn_record_default.png"
                imageSourceChecked: "asset:///images/btn_record_selected.png"
                imageSourceDisabledUnchecked: "asset:///images/btn_record_disabled.png"
                imageSourceDisabledChecked: "asset:///images/btn_record_disabled.png"
                onCheckedChanged: {
                    if (recordButton.checked) {
                        _mainController.onRecordButtonClick();
                    } else {
                        _mainController.onStopRecordingButtonClick();
                    }
                }
                imageSourcePressedUnchecked: "asset:///images/btn_record_default.png"
                imageSourcePressedChecked: "asset:///images/btn_record_default.png"
            }

            ImageButton {
                id: endButton
                enabled: _mainController.isEnableToEndButton
                defaultImageSource: "asset:///images/btn_end_default.png"
                disabledImageSource: "asset:///images/btn_end_disabled.png"
                onClicked: {
                    _mainController.onToEndButtonClick();
                }
                pressedImageSource: "asset:///images/btn_end_default.png"
            }

        } // end of the second row of buttons
        
        Container { // the third row of buttons
            
            layout: StackLayout {
                orientation: LayoutOrientation.LeftToRight
            
            }
            
            topMargin: 20.0
            horizontalAlignment: HorizontalAlignment.Center
            
            ImageButton {
                enabled: _mainController.isEnableMailButton
                defaultImageSource: "asset:///images/btn_send_default.png"
                disabledImageSource: "asset:///images/btn_send_disabled.png"
                onClicked: {
                    _mainController.onMailButtonClick();
                }
                pressedImageSource: "asset:///images/btn_send_default.png"

            }
            
            ImageButton {
                enabled: _mainController.isEnableDeleteButton
                disabledImageSource: "asset:///images/btn_pin4_disabled.png"
                pressedImageSource: "asset:///images/btn_pin4_disabled.png"
                defaultImageSource: "asset:///images/btn_pin4_disabled.png"
                onClicked: {
                    _mainController.onDeleteButtonClick();
                }
            }
            
            ImageButton {
                enabled: _mainController.isEnableSDButton
                defaultImageSource: "asset:///images/btn_sd_default.png"
                disabledImageSource: "asset:///images/btn_sd_disabled.png"
                onClicked: {
                    _mainController.onSDButtonClick();
                }
                pressedImageSource: "asset:///images/btn_sd_default.png"
            }
        
        } // end of the third row of buttons
        
        Container { // the fourth row of buttons
            
            layout: StackLayout {
                orientation: LayoutOrientation.LeftToRight
            
            }
            
            topMargin: 20.0
            horizontalAlignment: HorizontalAlignment.Center
            
            ImageButton {
                enabled: false
                disabledImageSource: "asset:///images/btn_pin1_disabled.png"
            }
            
            ImageButton {
                enabled: false
                disabledImageSource: "asset:///images/btn_pin2_disabled.png"
            
            }
            
            ImageButton {
                enabled: false
                disabledImageSource: "asset:///images/btn_pin3_disabled.png"

            }
        
        } // end of the fourth row of buttons
    } // end of main conteiner

}
