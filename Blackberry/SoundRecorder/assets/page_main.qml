/**
 * Copyright (c) 2014 wise-apps.com
 * 
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 23.03.2014
 */

import bb.cascades 1.2
import bb.system 1.2

Page {

    // ========= UI =========
    Container {
        
        background: Color.Gray

        verticalAlignment: VerticalAlignment.Fill
        horizontalAlignment: HorizontalAlignment.Fill
        
        Container {
            background: Color.Blue
            layout: DockLayout {
            }
            minHeight: 50.0
            preferredHeight: 70.0
            maxHeight: 70.0
            horizontalAlignment: HorizontalAlignment.Fill
        }

        Container {

            horizontalAlignment: HorizontalAlignment.Fill
            minHeight: 30.0
            leftPadding: 10.0
            rightPadding: 10.0

            topMargin: 40.0
            bottomMargin: 40.0
            
            ProgressIndicator {
                id: progressBar
                horizontalAlignment: HorizontalAlignment.Center
                verticalAlignment: VerticalAlignment.Center
                fromValue: _mainController.progressMinimum()
                toValue: _mainController.progressMaximum()
                value: _mainController.progressValue()

            }
        } // end of progress bar container

        Container {

            layout: StackLayout {
                orientation: LayoutOrientation.LeftToRight

            }

            horizontalAlignment: HorizontalAlignment.Center
            
            ImageButton {
                id: buttonRewind
                enabled: playButton.enabled
                defaultImageSource: "asset:///images/new_ui/button06_1_rewind_.png"
                pressedImageSource: "asset:///images/new_ui/button06_1_rewind_.png"
                disabledImageSource: "asset:///images/new_ui/button06_0_rewind_.png"
                onClicked: {
                    _mainController.onRewindButtonClick();
                }
            }

            ImageToggleButton {
                id: recordButton
                checked: _mainController.isCheckedRecordButton
                enabled:  !playButton.checked //_mainController.isEnableRecordButton
                imageSourceDefault: "asset:///images/new_ui/button02_1_stop_for_record_.png"
                imageSourceChecked: "asset:///images/new_ui/button02_2_record_.png"
                imageSourceDisabledUnchecked: "asset:///images/new_ui/button02_1_stop_for_record_.png"
                imageSourceDisabledChecked: "asset:///images/new_ui/button02_1_stop_for_record_.png"
                imageSourcePressedUnchecked: "asset:///images/new_ui/button02_1_stop_for_record_.png"
                imageSourcePressedChecked: "asset:///images/new_ui/button02_2_record_.png"
                onCheckedChanged: {
                	_mainController.onRecordButtonClick()
                }
                
            }

            ImageButton {
                id: buttonForward
                enabled: playButton.enabled
                defaultImageSource: "asset:///images/new_ui/button07_1_forward_.png"
                pressedImageSource: "asset:///images/new_ui/button07_1_forward_.png"
                disabledImageSource: "asset:///images/new_ui/button07_0_forward_.png"
                onClicked: {
                    _mainController.onForwardButtonClick();
                }
            }

        }

        Container {

            layout: StackLayout {
                orientation: LayoutOrientation.LeftToRight

            }

            topMargin: 20.0
            horizontalAlignment: HorizontalAlignment.Center
            
            ImageButton {
                id: buttonToStart
                enabled: playButton.enabled
                defaultImageSource: "asset:///images/new_ui/button05_1_go_to_start_.png"
                pressedImageSource: "asset:///images/new_ui/button05_1_go_to_start_.png"
                disabledImageSource: "asset:///images/new_ui/button05_0_go_to_start_.png"
                onClicked: {
                    _mainController.onToStartButtonClick();
                }

            }

            ImageToggleButton {
                id: playButton
                checked: _mainController.isCheckedPlayButton
                enabled: _mainController.isEnablePlayButton
                imageSourceDefault: "asset:///images/new_ui/button01_1.png"
                imageSourceDisabledUnchecked: "asset:///images/new_ui/button01_0.png"
                imageSourceDisabledChecked: "asset:///images/new_ui/button01_0.png"
                imageSourceChecked: "asset:///images/new_ui/button03_1_pause_.png"
                imageSourcePressedUnchecked: "asset:///images/new_ui/button01_2_play_.png"
                imageSourcePressedChecked: "asset:///images/new_ui/button03_1_pause_.png"
                onCheckedChanged: {
                    _mainController.onPlayButtonClick();
                }

            }

            ImageButton {
                id: buttonToEnd
                enabled: playButton.enabled
                defaultImageSource: "asset:///images/new_ui/button08_1_go_to_end_.png"
                pressedImageSource: "asset:///images/new_ui/button08_1_go_to_end_.png"
                disabledImageSource: "asset:///images/new_ui/button08_0_go_to_end_.png"
                onClicked: {
                    _mainController.onToEndButtonClick();
                }
            }

        }
        
        Container {
            
            layout: StackLayout {
                orientation: LayoutOrientation.LeftToRight
            
            }
            
            topMargin: 20.0
            horizontalAlignment: HorizontalAlignment.Center
            
            ImageButton {
                id: buttonSend
                enabled: playButton.enabled
                defaultImageSource: "asset:///images/new_ui/send_1.png"
                pressedImageSource: "asset:///images/new_ui/send_1.png"
                disabledImageSource: "asset:///images/new_ui/send_0.png"
                onClicked: {
                    _mainController.onSendButtonClick();
                }

            }
            
            Container {
                minWidth: 195.0
                preferredWidth: 195.0
                maxWidth: 195.0

            }
            
            ImageButton {
                id: buttonSD
                enabled: _mainController.isEnableSDButton
                defaultImageSource: "asset:///images/new_ui/save_1.png"
                pressedImageSource: "asset:///images/new_ui/save_1.png"
                disabledImageSource: "asset:///images/new_ui/save_0.png"
                onClicked: {
                    _mainController.onSDButtonClick();
                }
            }
        
        }
        
        Container {
                  
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

        } // end of conteiner for Slider

    } // end of main conteiner

}
