/*
 * Copyright (c) 2014 wise-apps.com
 * 
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 20.03.2014
 */

import bb.cascades 1.2

Page {

    // ========= Menu =========
    actions: [
        ActionItem {
            id: actionAddRecord
            title: qsTr("Add record")
            imageSource: "asset:///images/ic/ic_add.png"
            ActionBar.placement: ActionBarPlacement.OnBar

            onTriggered: {
                _mainView.onActionAddRecord();
            }
        },

        ActionItem {
            id: actionClearAll
            title: qsTr("Clear all")
            imageSource: "asset:///images/ic/ic_clear.png"
            ActionBar.placement: ActionBarPlacement.OnBar
            enabled: false

            onTriggered: {
                _mainView.onActionClearAll();
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
            leftMargin: 0.0
            rightMargin: 0.0
            preferredHeight: 300.0
            minHeight: 300.0
            maxHeight: 300.0

            verticalAlignment: VerticalAlignment.Fill
            horizontalAlignment: HorizontalAlignment.Fill
            ImageToggleButton {
                id: btn_play
                imageSourceDefault: "images/play.png"
                imageSourceDisabledUnchecked: "images/play.png"
                imageSourceChecked: "images/stop.png"
                imageSourceDisabledChecked: "images/stop.png"
                imageSourcePressedUnchecked: "images/play.png"
                imageSourcePressedChecked: "images/stop.png"
                topMargin: 0.0
                leftMargin: 0.0
                verticalAlignment: VerticalAlignment.Center
                accessibility.name: "TODO: Add property content"

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
                        text: "0.0"
                        textFit.minFontSizeValue: 12.0
                        textFit.maxFontSizeValue: 16.0
                        textStyle.color: Color.Black

                    }

                }

            }

        }

        ProgressIndicator {
            horizontalAlignment: HorizontalAlignment.Center
            topMargin: 5
            bottomMargin: 20

            // Show the progress bar only when computation is running
            opacity: _mainView.active ? 1.0 : 0.0

            fromValue: _mainView.progressMinimum
            toValue: _mainView.progressMaximum
            value: _mainView.progressValue
        }

        Container {
            ListView {
                dataModel: XmlDataModel {
                    source: "models/tracks_list.xml"

                }

            }

        }

    }

}
