package com.miro.platform.widget.domain.service;

import com.miro.platform.widget.domain.Widget;
import com.miro.platform.widget.domain.exceptions.ResourceNotFoundException;
import com.miro.platform.widget.domain.repository.WidgetRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.StampedLock;

@Service("h2Service")
public class WidgetH2Service implements WidgetService {
    private final WidgetRepo widgetRepository;
    private final StampedLock stampedLock;
    private static final Logger logger = LoggerFactory.getLogger(WidgetH2Service.class);

    @Autowired
    public WidgetH2Service(WidgetRepo widgetRepository, StampedLock stampedLock) {
        this.widgetRepository = widgetRepository;
        this.stampedLock = stampedLock;
    }

    /**
     * I'm not sure whether it's worth it to
     * do optimistic read lock here, as we'll need to
     * create another list with all items and then check again
     * for changes. For this I'd like to run more benchmark tests
     * but I'll go simple with normal read lock
     */
    @Override
    public List<Widget> findAll() {
        long stamp = stampedLock.tryOptimisticRead();
        List<Widget> widgetList = widgetRepository.findAllSorted();
        if (!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                widgetList = widgetRepository.findAllSorted();
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return widgetList;
    }

    @Override
    public List<Widget> findAll(int page, int size) {
        List<Widget> widgetList = findAll();
        int rangeStart = (page - 1) * size;
        if (rangeStart >= widgetList.size())
            throw new ResourceNotFoundException("Widgets not found for this page");

        int rangeEnd = Math.min(rangeStart + size, widgetList.size());
        return widgetList.subList(rangeStart, rangeEnd);
    }

    @Override
    public Widget findById(Long id) {
        long stamp = stampedLock.tryOptimisticRead();
        Optional<Widget> widget = widgetRepository.findById(id);
        if (!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                widget = widgetRepository.findById(id);
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return widget.orElseThrow(() -> new ResourceNotFoundException("No widget with id: " + id));
    }

    @Override
    public Widget addWidget(Widget widget) {
        if (widget.getId() != null) {
            throw new IllegalArgumentException("Can't enforce Id on insertion, it's auto generated");
        }
        long stamp = stampedLock.writeLock();
        try {
            if(widget.getId() != null && widgetRepository.findById(widget.getId()).isPresent()) {
                throw new DuplicateKeyException("Trying to insert duplicate id");
            }

            return Objects.requireNonNull(internalAddWidget(widget));
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

    private Widget internalAddWidget(Widget widget) {
        widget.validForOperationOrThrow();
        if (widget.getzIndex() == null) {
            widget.setzIndex(widgetRepository.getForeground()+1);
        } else if (widgetRepository.findByZindex(widget.getzIndex()).isPresent()) {
            shiftLargerOrEqualZindex(widget.getzIndex());
        }

        return widgetRepository.addWidget(widget);
    }


    private void shiftLargerOrEqualZindex(Long zIndex) {
        List<Widget> toUpdate = widgetRepository.findGreaterThanEqualZindex(zIndex);
        for (Widget widget : toUpdate) {
            widget.incrementZindex();
            widgetRepository.updateWidget(widget);
        }
    }

    @Override
    public Widget updateWidget(Long id, Widget widget) {
        widget.validForOperationOrThrow();
        if (widget.getId() != null && !id.equals(widget.getId()))
            throw new IllegalArgumentException("Can't change Id of widget, make sure widget's id is the same as id in parameters");


        if (widget.getId() == null)
            widget.setId(id);

        long stamp = stampedLock.writeLock();
        try {
            widgetRepository.findById(id).orElseThrow(() -> {
                throw new ResourceNotFoundException("No widget with given Id: " + id);
            });
            //If zIndex exists for OTHER widget then we'll delete the widget and insert it again
            //So that shifting and everything happens
            Optional<Widget> currentWidgetWithZindex = widgetRepository.findByZindex(widget.getzIndex());
            if (currentWidgetWithZindex.isPresent() &&
                    !currentWidgetWithZindex.get().getId().equals(widget.getId())) {
                shiftLargerOrEqualZindex(widget.getzIndex());
            }

            return widgetRepository.updateWidget(widget);
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

    @Override
    public void removeWidget(Long id) {
        long stamp = stampedLock.writeLock();
        try {
            widgetRepository.removeWidget(id);
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }
}
