package com.miro.platform.widget.domain.repository.h2;

import com.miro.platform.widget.domain.Widget;
import com.miro.platform.widget.domain.repository.WidgetRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("h2")
public class WidgetH2Repo implements WidgetRepo {
    public final WidgetJpaRepo jpaRepo;
    private static final Logger logger = LoggerFactory.getLogger(WidgetH2Repo.class);

    @Autowired
    public WidgetH2Repo(WidgetJpaRepo jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public List<Widget> findAllSorted() {
        return jpaRepo.findAll(Sort.by(Sort.Direction.ASC, "zIndex"));
    }

    @Override
    public List<Widget> findGreaterThanEqualZindex(Long zIndex) {
        return jpaRepo.findWidgetByzIndexGreaterThanEqual(zIndex);
    }

    @Override
    public Optional<Widget> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public Optional<Widget> findByZindex(Long zIndex) {
        return jpaRepo.findByzIndex(zIndex);
    }

    @Override
    public Widget addWidget(Widget widget) {
        return jpaRepo.save(widget);
    }

    @Override
    public Widget updateWidget(Widget newWidget) {
        return jpaRepo.save(newWidget);
    }

    @Override
    public long getForeground() {
        return jpaRepo.getMaxZindex().orElse(-1L);
    }

    @Override
    public void removeWidget(Long id) {
        jpaRepo.deleteById(id);
    }

}
