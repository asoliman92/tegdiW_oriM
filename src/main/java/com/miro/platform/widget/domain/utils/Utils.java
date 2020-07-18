package com.miro.platform.widget.domain.utils;

import com.miro.platform.widget.domain.Widget;

public class Utils {
    public Widget getCopy(Widget widget) {
        return Widget.Builder
                .emptyWidget()
                .withId(widget.getId())
                .withZIndex(widget.getzIndex())
                .withWidth(widget.getWidth())
                .withHeight(widget.getHeight())
                .withX(widget.getX())
                .withY(widget.getY())
                .withLastUpdate(widget.getLastUpdate())
                .build();
    }
}
