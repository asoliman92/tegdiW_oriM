package com.miro.platform.widget.domain.service;

import com.miro.platform.widget.domain.Widget;
import com.miro.platform.widget.domain.exceptions.ResourceNotFoundException;
import com.miro.platform.widget.domain.repository.WidgetRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("customService")
public class WidgetCustomService implements WidgetService {
    WidgetRepo widgetRepo;
    private static final Logger logger = LoggerFactory.getLogger(WidgetCustomService.class);
    @Autowired
    public WidgetCustomService(WidgetRepo widgetRepo) {
        this.widgetRepo = widgetRepo;
    }

    @Override
    public List<Widget> findAll() {
        return widgetRepo.findAllSorted();
    }

    @Override
    public List<Widget> findAll(int page, int size) {
        if(size > 500)
            throw new IllegalArgumentException("size can't exceed 500");

        List<Widget> widgetList = widgetRepo.findAllSorted();
        int rangeStart = (page-1)*size;
        if(rangeStart >= widgetList.size())
            new ArrayList();

        int rangeEnd = Math.min(rangeStart+size, widgetList.size());
        return widgetList.subList(rangeStart, rangeEnd);
    }

    @Override
    public Widget findById(Long id) {
        return widgetRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("No widget with id: " + id));
    }

    @Override
    public Widget addWidget(Widget widget) {
        return widgetRepo.addWidget(widget);
    }

    @Override
    public Widget updateWidget(Long id, Widget newWidget) {
        if(newWidget.getId() == null)
            newWidget.setId(id);
        if(!newWidget.getId().equals(id))
            throw new IllegalArgumentException("Trying to change widget's id");
        return widgetRepo.updateWidget(newWidget);
    }

    @Override
    public void removeWidget(Long id) {
        widgetRepo.removeWidget(id);
    }
}
