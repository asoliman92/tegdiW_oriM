package com.miro.platform.widget.domain;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class WidgetTest {
    /**
     * Testing Widget zIndex comparator in Widget
     * and that binary search works on widget list that's sorted based on zIndex
     * */
    @Test
    public void binarySearchZindex() {
        List<Widget> widgetList = new LinkedList<>();
        //3
        widgetList.add(Widget.Builder.emptyWidget().withZIndex(0L).withY(6L).build());
        //1
        widgetList.add(Widget.Builder.emptyWidget().withZIndex(-2L).withWidth(3L).build());
        //2
        widgetList.add(Widget.Builder.emptyWidget().withZIndex(-1L).withX(4L).build());
        //6
        widgetList.add(Widget.Builder.emptyWidget().withZIndex(10L).build());
        //4
        widgetList.add(Widget.Builder.emptyWidget().withZIndex(2L).build());
        //0
        widgetList.add(Widget.Builder.emptyWidget().withZIndex(-5L).withHeight(1L).build());
        //5
        widgetList.add(Widget.Builder.emptyWidget().withZIndex(4L).build());

        Collections.sort(widgetList);

        int index;
        index = Collections.binarySearch(widgetList, Widget.Builder.emptyWidget().withZIndex(11L).build());
        Assert.assertEquals(7, -index-1);
        index = Collections.binarySearch(widgetList, Widget.Builder.emptyWidget().withZIndex(-7L).build());
        Assert.assertEquals(0, -index-1);
        index = Collections.binarySearch(widgetList, Widget.Builder.emptyWidget().withZIndex(7L).build());
        Assert.assertEquals(6, -index-1);
        index = Collections.binarySearch(widgetList, Widget.Builder.emptyWidget().withZIndex(2L).build());
        Assert.assertEquals(4, index);
    }

    @Test
    public void givenMissingXWhenValidateWidgetThenThrowException() {
        Widget widget = Widget.Builder.emptyWidget()
                .withWidth(1L)
                .withHeight(1L)
                .withY(1L)
                .build();

        Assert.assertThrows(IllegalStateException.class,
                widget::validForOperationOrThrow);
    }

    @Test
    public void givenMissingYWhenValidateWidgetThenThrowException() {
        Widget widget = Widget.Builder.emptyWidget()
                .withWidth(1L)
                .withHeight(1L)
                .withX(1L)
                .build();

        Assert.assertThrows(IllegalStateException.class,
                widget::validForOperationOrThrow);
    }

    @Test
    public void givenMissingWidthWhenValidateWidgetThenThrowException() {
        Widget widget = Widget.Builder.emptyWidget()
                .withHeight(1L)
                .withX(1L)
                .withY(1L)
                .build();

        Assert.assertThrows(IllegalStateException.class,
                widget::validForOperationOrThrow);
    }

    @Test
    public void givenMissingHeightWhenValidateWidgetThenThrowException() {
        Widget widget = Widget.Builder.emptyWidget()
                .withWidth(1L)
                .withX(1L)
                .withY(1L)
                .build();

        Assert.assertThrows(IllegalStateException.class,
                widget::validForOperationOrThrow);
    }

    @Test
    public void givenFullWidgetWhenValidateThenNoException() {
        Widget widget = Widget.Builder.emptyWidget()
                .withHeight(1L)
                .withWidth(1L)
                .withX(1L)
                .withY(1L)
                .build();

        widget.validForOperationOrThrow();
    }
}
