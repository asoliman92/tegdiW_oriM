package com.miro.platform.widget.domain.repository.h2;

import com.miro.platform.widget.domain.Widget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WidgetJpaRepo extends JpaRepository<Widget, Long> {
    List<Widget> findWidgetByzIndexGreaterThanEqual(Long zIndex);
    Optional<Widget> findByzIndex(Long zIndex);
    @Query(value = "select max(zIndex) from Widget")
    Optional<Long> getMaxZindex();
}
