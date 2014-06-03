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
            bottomMargin: 40.0
            
            Slider {
                id: sliderPlayer
                horizontalAlignment: HorizontalAlignment.Center
                verticalAlignment: VerticalAlignment.Center
                fromValue: _mainController.progressMinimum()
                toValue: _mainController.progressMaximum()
                value: _mainController.progressValue()

            }
        } // end of progress bar container

        Container { // the first row of buttons

            layout: StackLayout {
                orientation: LayoutOrientation.LeftToRight

            }

            horizontalAlignment: HorizontalAlignment.Center
            
            ImageButton {
                id: rewindButton
                enabled: playButton.enabled
                defaultImageSource: "asset:///images/btn_rewind_default.png"
                disabledImageSource: "asset:///images/btn_rewind_disabled.png"
                onClicked: {
                    _mainController.onRewindButtonClick();
                }
                pressedImageSource: "asset:///images/btn_rewind_default.png"
            }

                ImageToggleButton {
                    id: playButton
                    checked: _mainController.isCheckedPlayButton
                    //enabled: _mainController.isEnablePlayButton
                    imageSourceDefault: "asset:///images/btn_play_default.png"
                    imageSourceDisabledUnchecked: "asset:///images/btn_play_disabled.png"
                    imageSourceDisabledChecked: "asset:///images/btn_play_disabled.png"
                    imageSourceChecked: "asset:///images/bnt_pause_default.png"
                    onCheckedChanged: {
                        _mainController.onPlayButtonClick();
                    }
                    imageSourcePressedUnchecked: "asset:///images/btn_play_selected.png"
                    imageSourcePressedChecked: "asset:///images/btn_play_selected.png"
                    layoutProperties: FlowListLayoutProperties {

                    }
                }
                
            ImageButton {
                id: forwardButton
                enabled: playButton.enabled
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
                enabled: playButton.enabled
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
                enabled:  !playButton.checked //_mainController.isEnableRecordButton
                imageSourceDefault: "asset:///images/btn_record_default.png"
                imageSourceChecked: "asset:///images/btn_record_selected.png"
                imageSourceDisabledUnchecked: "asset:///images/btn_record_disabled.png"
                imageSourceDisabledChecked: "asset:///images/btn_record_disabled.png"
                onCheckedChanged: {
                    _mainController.onRecordButtonClick()
                }
                imageSourcePressedUnchecked: "asset:///images/btn_record_default.png"
                imageSourcePressedChecked: "asset:///images/btn_record_default.png"

            }

            ImageButton {
                id: endButton
                enabled: playButton.enabled
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
                enabled: playButton.enabled
                defaultImageSource: "asset:///images/btn_send_default.png"
                disabledImageSource: "asset:///images/btn_send_disabled.png"
                onClicked: {
                    _mainController.onSendButtonClick();
                }
                pressedImageSource: "asset:///images/btn_send_default.png"

            }
            
            ImageButton {
                enabled: false
                disabledImageSource: "asset:///images/btn_pin4_disabled.png"
                pressedImageSource: "asset:///images/btn_pin4_disabled.png"
                defaultImageSource: "asset:///images/btn_pin4_disabled.png"

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
        
        Container { // volume slider
                  
            verticalAlignment: VerticalAlignment.Fill
            horizontalAlignment: HorizontalAlignment.Fill

            topMargin: 40.0
            Slider {
                id: volumeSlider
                verticalAlignment: VerticalAlignment.Center
                horizontalAlignment: HorizontalAlignment.Center

                fromValue: 0 //_mainController.volumeMinimum
                toValue: 100 //_mainController.progressMaximum
                value: 22 //_mainController.volumeValue
                
                onImmediateValueChanged: {
                    // Output the immediateValue to the console
                    console.debug("The slider value is " + immediateValue)
                }

            }

        } // end of volume slider

    } // end of main conteiner

}
