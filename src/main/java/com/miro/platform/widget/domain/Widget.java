package com.miro.platform.widget.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miro.platform.widget.domain.exceptions.IllegalChangeException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name="Widget")
public class Widget implements Comparable<Widget> {
    @JsonIgnore
    @Id
    @GeneratedValue
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private Long x;
    private Long y;
    private Long width;
    private Long height;
    private Long zIndex;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdate; // This is read only

    public Widget() {
    }

    @JsonCreator
    public Widget(
            Long id,
            @JsonProperty(required = true) Long x,
            @JsonProperty(required = true) Long y,
            @JsonProperty(required = true) Long width,
            @JsonProperty(required = true) Long height,
            Long zIndex,
            LocalDateTime lastUpdate) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zIndex = zIndex;
        this.lastUpdate = lastUpdate == null ? LocalDateTime.now() : lastUpdate;
    }

    public Long getId() {
        return id;
    }

    public Long getzIndex() {
        return zIndex;
    }

    public void incrementZindex() {
        zIndex++;
    }

    public void setId(Long id) {
        if (this.id != null) {
            throw new IllegalChangeException("Trying to change an existing id");
        }
        this.id = id;
    }

    public void setzIndex(Long zIndex) {
        this.zIndex = zIndex;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getX() {
        return x;
    }

    public void setX(Long x) {
        this.x = x;
    }

    public Long getY() {
        return y;
    }

    public void setY(Long y) {
        this.y = y;
    }

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    /**
     * Normally if it's accessed through API it's already guarded by API validation
     * as All those fields are required. This is only to prevent code misuse.
     * It's not checking for zIndex nor Id as they are assignable from endpoint
     */
    public void validForOperationOrThrow() {
        if (height == null ||
                width == null ||
                x == null ||
                y == null)
            throw new IllegalStateException("Missing one or more required attributes. X, Y, Width, Height");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Widget widget = (Widget) o;
        return id.equals(widget.id) &&
                Objects.equals(x, widget.x) &&
                Objects.equals(y, widget.y) &&
                Objects.equals(width, widget.width) &&
                Objects.equals(height, widget.height) &&
                zIndex.equals(widget.zIndex) &&
                Objects.equals(lastUpdate, widget.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, x, y, width, height, zIndex, lastUpdate);
    }

    @Override
    public String toString() {
        return "Widget{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", zIndex=" + zIndex +
                ", lastUpdate=" + lastUpdate +
                '}';
    }

    @Override
    public int compareTo(Widget widget) {
        if (this.zIndex < widget.getzIndex()) {
            return -1;
        } else if (this.zIndex > widget.getzIndex()) {
            return 1;
        }
        return 0;
    }

//    public Widget getCopy() {
//        return Widget.Builder
//                .emptyWidget()
//                .withId(getId())
//                .withZIndex(getzIndex())
//                .withWidth(getWidth())
//                .withHeight(getHeight())
//                .withX(getX())
//                .withY(getY())
//                .withLastUpdate(getLastUpdate())
//                .build();
//    }

    public static final class Builder {
        private Long id;
        private Long x;
        private Long y;
        private Long width;
        private Long height;
        private Long zIndex;
        private LocalDateTime lastUpdate;

        private Builder() {
        }

        public static Builder emptyWidget() {
            return new Builder();
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withX(Long x) {
            this.x = x;
            return this;
        }

        public Builder withY(Long y) {
            this.y = y;
            return this;
        }

        public Builder withWidth(Long width) {
            this.width = width;
            return this;
        }

        public Builder withHeight(Long height) {
            this.height = height;
            return this;
        }

        public Builder withZIndex(Long zIndex) {
            this.zIndex = zIndex;
            return this;
        }

        public Builder withLastUpdate(LocalDateTime lastUpdate) {
            this.lastUpdate = lastUpdate;
            return this;
        }

        public Widget build() {
            return new Widget(id, x, y, width, height, zIndex, lastUpdate);
        }
    }
}
