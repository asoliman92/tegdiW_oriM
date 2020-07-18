package com.miro.platform.widget.application.rest;

import com.miro.platform.widget.domain.Widget;
import com.miro.platform.widget.domain.service.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WidgetController {
    WidgetService widgetService;

    @Autowired
    public WidgetController(WidgetService widgetService) {
        this.widgetService = widgetService;
    }

    @GetMapping("/widgets/{id}")
    Widget getWidget(@PathVariable Long id) {
        return widgetService.findById(id);
    }

    @GetMapping("/widgets")
    List<Widget> getWidgetList(@RequestParam(value = "page") int page,
                                     @RequestParam(value = "size", defaultValue = "10") int size) {
        return widgetService.findAll(page, size);
    }

    @PostMapping("/widgets")
    Widget createWidget(@RequestBody Widget widget) {
        return widgetService.addWidget(widget);
    }

    @PutMapping("/widgets/{id}")
    Widget updateWidget(@RequestBody Widget widget, @PathVariable Long id) {
        return widgetService.updateWidget(id, widget);
    }

    @DeleteMapping("/widgets/{id}")
    ResponseEntity<?> deleteWidget(@PathVariable Long id) {
        widgetService.removeWidget(id);
        return ResponseEntity.noContent().build();
    }

}
