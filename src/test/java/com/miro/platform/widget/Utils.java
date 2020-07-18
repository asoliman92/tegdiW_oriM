package com.miro.platform.widget;

import com.miro.platform.widget.domain.Widget;

public class Utils {

    public static Widget.Builder getDummyBuilderNoIdNoZindex() {
        Long dummyAttribute = 1L;
        return Widget.Builder
                .emptyWidget()
                .withX(dummyAttribute)
                .withY(dummyAttribute)
                .withHeight(dummyAttribute)
                .withWidth(dummyAttribute);
    }

    public static Widget.Builder getDummyBuilderNoIdNoZindex(Long dummyAttribute) {
        return Widget.Builder
                .emptyWidget()
                .withX(dummyAttribute)
                .withY(dummyAttribute)
                .withHeight(dummyAttribute)
                .withWidth(dummyAttribute);
    }
}
