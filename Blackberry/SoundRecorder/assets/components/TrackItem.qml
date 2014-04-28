/**
 * Copyright (c) 2014 wise-apps.com
 * 
 * Created by Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 * 26.03.2014
 */
import bb.cascades 1.2

/// Item component for the item list presenting available tracks
CustomListItem {

    dividerVisible: true
    highlightAppearance: HighlightAppearance.Frame
    
    Container {

        id: itemRoot

        layout: DockLayout {

        }

        preferredWidth: 768
        preferredHeight: 120

        horizontalAlignment: HorizontalAlignment.Center
        verticalAlignment: VerticalAlignment.Center

        leftPadding: 10.0
        rightPadding: 10.0

        Label {
            text: ListItemData.title
            verticalAlignment: VerticalAlignment.Center
            textFit.minFontSizeValue: 11.0
            textFit.maxFontSizeValue: 11.0
            textStyle.color: Color.Black
        }

        ImageView {
            imageSource: "asset:///images/ic/ic_next.png"
            horizontalAlignment: HorizontalAlignment.Right
            leftMargin: 20.0
            verticalAlignment: VerticalAlignment.Center
            preferredHeight: 50.0
            preferredWidth: 50.0
        }

    }
}
