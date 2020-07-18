package com.miro.platform.widget.domain.service;

import com.miro.platform.widget.domain.Widget;

import java.util.List;

public interface WidgetService {
    List<Widget> findAll();

    List<Widget> findAll(int page, int size);

    Widget findById(Long id);

    Widget addWidget(Widget widget);

    Widget updateWidget(Long id, Widget newWidget);

    void removeWidget(Long id);
}
