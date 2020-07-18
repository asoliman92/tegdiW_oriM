package com.miro.platform.widget.domain.repository;

import com.miro.platform.widget.domain.Widget;

import java.util.List;
import java.util.Optional;

public interface WidgetRepo {
    List<Widget> findAllSorted();
    List<Widget> findGreaterThanEqualZindex(Long zIndex);
    Optional<Widget> findById(Long id);
    Optional<Widget> findByZindex(Long zIndex);
    Widget addWidget(Widget widget);
    Widget updateWidget(Widget newWidget);
    void removeWidget(Long id);
    long getForeground();
}
