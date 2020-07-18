package com.miro.platform.widget.domain.repository;

import com.miro.platform.widget.domain.Widget;
import com.miro.platform.widget.domain.exceptions.ResourceNotFoundException;
import com.miro.platform.widget.domain.utils.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

@Repository("custom")
public class WidgetCustomRepo implements WidgetRepo {
    private final Map<Long, Widget> id2Widget;
    private final Map<Long, Widget> zIndex2Widget;
    //Maintaining a sorted list so that no need to sort when findAll
    private List<Widget> widgetList;
    private long foreground;
    private final StampedLock stampedLock;
    private final IdGenerator idGenerator;
    private static final Logger logger = LoggerFactory.getLogger(WidgetCustomRepo.class);

    @Autowired
    public WidgetCustomRepo(IdGenerator idGenerator) {
        this(idGenerator, new ArrayList<>());
    }

    public WidgetCustomRepo(IdGenerator idGenerator, List<Widget> toCopyList) {
        this.idGenerator = idGenerator;
        id2Widget = new HashMap<>();
        zIndex2Widget = new HashMap<>();
        stampedLock = new StampedLock();
        foreground = -1;
        initFromList(toCopyList);
    }

    private void initFromList(List<Widget> toCopyList) {
        //Ensure that list is sorted
        widgetList = new ArrayList(toCopyList);
        Collections.sort(widgetList);
        for (Widget widget : widgetList) {
            if (id2Widget.containsKey(widget.getId()))
                throw new IllegalStateException("List is initialized with non-unique ids");
            if (zIndex2Widget.containsKey(widget.getzIndex()))
                throw new IllegalStateException("List is initialized with non-unique z-indexes");

            if (widget.getId() == null)
                widget.setId(idGenerator.getNextId());

            id2Widget.put(widget.getId(), widget);
            zIndex2Widget.put(widget.getzIndex(), widget);
        }

        if (!widgetList.isEmpty())
            foreground = widgetList.get(widgetList.size() - 1).getzIndex();
    }

    @Override
    public long getForeground() {
        return foreground;
    }

    @Override
    public List<Widget> findAllSorted() {
        long stamp = stampedLock.tryOptimisticRead();
        List<Widget> list = new ArrayList<>(widgetList);
        if(!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                list = new ArrayList<>(widgetList);
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return list;
    }

    @Override
    public List<Widget> findGreaterThanEqualZindex(Long zIndex) {
        return widgetList.stream()
                .filter(widget -> widget.getzIndex() >= zIndex)
                .map(this::getCopy)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Widget> findById(Long id) {
        if (!id2Widget.containsKey(id))
            return Optional.empty();

        long stamp = stampedLock.tryOptimisticRead();
        Widget result = getCopy(id2Widget.get(id));
        if(!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                result = getCopy(id2Widget.get(id));
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return Optional.of(result);
    }

    /**
     * Same as widgetById, trying optimistic lock
     * */
    @Override
    public Optional<Widget> findByZindex(Long zIndex) {
        if (!zIndex2Widget.containsKey(zIndex))
            return Optional.empty();

        long stamp = stampedLock.tryOptimisticRead();
        Widget result = getCopy(zIndex2Widget.get(zIndex));
        if(!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                result = getCopy(zIndex2Widget.get(zIndex));
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return Optional.of(result);
    }

    private Widget getCopy(Widget widget) {
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

    private Widget internalAddWidget(Widget widget) {
        widget.validForOperationOrThrow();

        if (id2Widget.containsKey(widget.getId())) {
            throw new DuplicateKeyException("Trying to insert duplicate Id");
        }

        if (widget.getId() == null) {
            widget.setId(idGenerator.getNextId());
        }

        id2Widget.put(widget.getId(), widget);

        //Adding one to current foreground to bring the widget to the top
        if (widget.getzIndex() == null) {
            foreground++;
            widget.setzIndex(foreground);
            widgetList.add(widget);
            zIndex2Widget.put(widget.getzIndex(), widget);
            return widget;
        }

        if (zIndex2Widget.containsKey(widget.getzIndex())) {
            addExistingZindexWidget(widget);
        } else {
            addNonExistingZindexWidget(widget);
        }
        return widget;
    }

    @Override
    public Widget addWidget(Widget widget) {
        long stamp = stampedLock.writeLock();
        try {
            return getCopy(internalAddWidget(getCopy(widget)));
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }


    /**
     * From binary search documentation
     * The index of the search key, if it is contained in the list
     * otherwise, (-(insertion point) - 1).
     * The insertion point is defined as the point at which the key
     * would be inserted into the list.
     * As widget implements Comparable interface based on zIndex and the list will maintain
     * the order when we insert a new zIndex we can do this here
     */
    private void addNonExistingZindexWidget(Widget widget) {
        int index = Collections.binarySearch(widgetList, widget);
        widgetList.add(-index - 1, widget);
        zIndex2Widget.put(widget.getzIndex(), widget);
        foreground = Math.max(foreground, widget.getzIndex());
    }

    //Need to shift all greater zIndex widgets, update zIndexMap, update foreground
    private void addExistingZindexWidget(Widget widget) {
        //Can also use indexOf directly but this will be faster if widgets size grows big
        int index = Collections.binarySearch(widgetList, widget);
        widgetList.add(index, widget);
        zIndex2Widget.put(widget.getzIndex(), widget);
        Widget tmpWidget;
        //Shifting all larger elements' zIndex
        for (int i = index + 1; i < widgetList.size(); i++) {
            tmpWidget = widgetList.get(i);
            tmpWidget.incrementZindex();
            //Updating map
            zIndex2Widget.put(tmpWidget.getzIndex(), tmpWidget);
        }

        foreground = Math.max(foreground, widgetList.get(widgetList.size() - 1).getzIndex());
    }

    private Widget internalUpdateWidget(Widget newWidget) {
        newWidget.validForOperationOrThrow();

        Long id = newWidget.getId();
        if (!id2Widget.containsKey(id))
            throw new ResourceNotFoundException("No widget with id: " + id);

        Widget oldWidget = id2Widget.get(id);
        //Not changing zIndex, then update old reference directly
        if (newWidget.getzIndex().equals(oldWidget.getzIndex())) {
            oldWidget.setX(newWidget.getX());
            oldWidget.setY(newWidget.getY());
            oldWidget.setWidth(newWidget.getWidth());
            oldWidget.setHeight(newWidget.getHeight());
            oldWidget.setLastUpdate(LocalDateTime.now());
            return oldWidget;
        } else {
            internalRemoveWidget(id);
            return internalAddWidget(newWidget);
        }
    }

    @Override
    public Widget updateWidget(Widget newWidget) {
        long stamp = stampedLock.writeLock();
        try {
            //Adding the getCopy here for clarity and consistency, but it's considered redundant for some cases
            return getCopy(internalUpdateWidget(getCopy(newWidget)));
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }



    private void internalRemoveWidget(Long id) {
        if (!id2Widget.containsKey(id))
            throw new ResourceNotFoundException("No widget with id: " + id);

        Widget widget = id2Widget.get(id);
        int widgetIndex = Collections.binarySearch(widgetList, widget);
        //Removing top element
        if (widgetIndex == widgetList.size() - 1) {
            if (widgetIndex - 1 >= 0) {
                foreground = widgetList.get(widgetIndex - 1).getzIndex();
            } else {
                //List will become empty after delete, reset foreground
                foreground = -1;
            }
        }

        widgetList.remove(widgetIndex);
        zIndex2Widget.remove(widget.getzIndex());
        id2Widget.remove(id);
    }
    @Override
    public void removeWidget(Long id) {
        long stamp = stampedLock.writeLock();
        try {
            internalRemoveWidget(id);
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

}
