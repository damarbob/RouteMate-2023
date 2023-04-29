package id.my.dsm.routemate.ui.recyclerview;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;

// TODO: Add condition
public class MainMenuItem {

    public enum Style {
        DEFAULT,
        DEFAULT_COUNT,
        OPAQUE,
        OPAQUE_COUNT,
        OUTLINE,
        OUTLINE_COUNT,
        OPAQUE_GREEN,
        OPAQUE_GREEN_COUNT,
    }

    public enum Title {
        Places,
        Location,
        Fleet,
        Matrix,
        Optimize
    }

    private Title title;
    private String description;
    private String label;
    private int countable;
    private int drawableId;
    private int actionId;
    private Style style;

    private MainMenuItem(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.label = builder.label;
        this.countable = builder.countable;
        this.drawableId = builder.drawableId;
        this.actionId = builder.actionId;
        this.style = builder.style;
    }

    public static class Builder {

        private final Title title;
        private final String description;
        private String label;
        private int countable;
        private final int drawableId;
        private int actionId;
        private Style style = Style.DEFAULT_COUNT;

        public Builder(Title title, String description, int drawableId) {
            this.title = title;
            this.description = description;
            this.drawableId = drawableId;
        }

        public Builder withLabel(@NonNull String label) {
            this.label = label;
            return this;
        }

        public Builder withCountable(@NonNull int countable) {
            this.countable = countable;
            return this;
        }

        public Builder withActionId(int actionId) {
            this.actionId = actionId;
            return this;
        }

        public Builder withStyle(Style style) {
            this.style = style;
            return this;
        }

        public MainMenuItem build() {
            return new MainMenuItem(this);
        }

    }

    public int getCountable() {
        return countable;
    }

    public void setCountable(int countable) {
        this.countable = countable;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    @DrawableRes
    public int getDrawableId() {
        return drawableId;
    }

    public int getActionId() {
        return actionId;
    }

    public Style getStyle() {
        return style;
    }
}
