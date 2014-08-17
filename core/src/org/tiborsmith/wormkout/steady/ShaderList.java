package org.tiborsmith.wormkout.steady;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

/**
 * Created by tibor on 17.8.14.
 */
public class ShaderList<T> extends Widget implements Cullable {
    private ListStyle style;
    private final Array<T> items = new Array();
    private Rectangle cullingArea;
    private float prefWidth, prefHeight;
    private float itemHeight;
    private float textOffsetX, textOffsetY;
    final ArraySelection<T> selection;

    private ShaderProgram shader;
    private float scale;

    public ShaderList(Skin skin) {
        this(skin.get(ListStyle.class));
        shader = MyShaders.fontShader;
        scale = 1;
    }

    public ShaderList(Skin skin, String styleName) {
        this(skin.get(styleName, ListStyle.class));
    }

    public ShaderList(ListStyle style) {
        selection = new ArraySelection(items);
        selection.setActor(this);
        selection.setRequired(true);

        setStyle(style);
        setSize(getPrefWidth(), getPrefHeight());

        addListener(new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == 0 && button != 0) return false;
                if (selection.isDisabled()) return false;
                ShaderList.this.touchDown(y);
                return true;
            }
        });
    }

    void touchDown (float y) {
        if (items.size == 0) return;
        float height = getHeight();
        if (style.background != null) {
            height -= style.background.getTopHeight() + style.background.getBottomHeight();
            y -= style.background.getBottomHeight();
        }
        int index = (int)((height - y) / itemHeight);
        index = Math.max(0, index);
        index = Math.min(items.size - 1, index);
        selection.choose(items.get(index));
    }

    public void setStyle (ListStyle style) {
        if (style == null) throw new IllegalArgumentException("style cannot be null.");
        this.style = style;
        invalidateHierarchy();
    }

    /** Returns the list's style. Modifying the returned style may not have an effect until {@link #setStyle(ListStyle)} is called. */
    public ListStyle getStyle () {
        return style;
    }

    public void layout () {
        final BitmapFont font = style.font;
        final Drawable selectedDrawable = style.selection;

        itemHeight = font.getCapHeight() - font.getDescent() * 2;
        itemHeight += selectedDrawable.getTopHeight() + selectedDrawable.getBottomHeight();

        textOffsetX = selectedDrawable.getLeftWidth();
        textOffsetY = selectedDrawable.getTopHeight() - font.getDescent();

        prefWidth = 0;
        for (int i = 0; i < items.size; i++) {
            BitmapFont.TextBounds bounds = font.getBounds(items.get(i).toString());
            prefWidth = Math.max(bounds.width, prefWidth);
        }
        prefWidth += selectedDrawable.getLeftWidth() + selectedDrawable.getRightWidth();
        prefHeight = items.size * itemHeight;

        Drawable background = style.background;
        if (background != null) {
            prefWidth += background.getLeftWidth() + background.getRightWidth();
            prefHeight += background.getTopHeight() + background.getBottomHeight();
        }
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        validate();

        BitmapFont font = style.font;
        Drawable selectedDrawable = style.selection;
        Color fontColorSelected = style.fontColorSelected;
        Color fontColorUnselected = style.fontColorUnselected;

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        float itemY = height;

        Drawable background = style.background;
        if (background != null) {
            background.draw(batch, x, y, width, height);
            float leftWidth = background.getLeftWidth();
            x += leftWidth;
            itemY -= background.getTopHeight();
            width -= leftWidth + background.getRightWidth();
        }





        font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha);
        for (int i = 0; i < items.size; i++) {
            if (cullingArea == null || (itemY - itemHeight <= cullingArea.y + cullingArea.height && itemY >= cullingArea.y)) {
                T item = items.get(i);
                boolean selected = selection.contains(item);
                if (selected) {
                    selectedDrawable.draw(batch, x, y + itemY - itemHeight, width, itemHeight);
                    font.setColor(fontColorSelected.r, fontColorSelected.g, fontColorSelected.b, fontColorSelected.a * parentAlpha);
                }

                if (shader != null) {
                    batch.setShader(shader);
                    shader.setUniformf("u_smoothing", 0.75f / (4 * scale));
                }
                font.draw(batch, item.toString(), x + textOffsetX, y+6 + itemY - textOffsetY);
                batch.setShader(null);

                if (selected) {
                    font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a
                            * parentAlpha);
                }
            } else if (itemY < cullingArea.y) {
                break;
            }
            itemY -= itemHeight;
        }
    }

    public ArraySelection<T> getSelection () {
        return selection;
    }

    /** Returns the first selected item, or null. */
    public T getSelected () {
        return selection.first();
    }

    /** @return The index of the first selected item. The top item has an index of 0. Nothing selected has an index of -1. */
    public int getSelectedIndex () {
        ObjectSet<T> selected = selection.items();
        return selected.size == 0 ? -1 : items.indexOf(selected.first(), false);
    }

    /** Sets the selection to only the selected index. */
    public void setSelectedIndex (int index) {
        if (index < -1 || index >= items.size)
            throw new IllegalArgumentException("index must be >= -1 and < " + items.size + ": " + index);
        if (index == -1) {
            selection.clear();
        } else {
            selection.set(items.get(index));
        }
    }

    public void setItems (T... newItems) {
        if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");

        items.clear();
        items.addAll(newItems);

        if (selection.getRequired() && items.size > 0)
            selection.set(items.first());
        else
            selection.clear();

        invalidateHierarchy();
    }

    /** Sets the current items, clearing the selection if it is no longer valid. If a selection is
     * {@link ArraySelection#getRequired()}, the first item is selected. */
    public void setItems (Array newItems) {
        if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");

        items.clear();
        items.addAll(newItems);

        T selected = getSelected();
        if (!items.contains(selected, false)) {
            if (selection.getRequired() && items.size > 0)
                selection.set(items.first());
            else
                selection.clear();
        }

        invalidateHierarchy();
    }

    public Array<T> getItems () {
        return items;
    }

    public float getItemHeight () {
        return itemHeight;
    }

    public float getPrefWidth () {
        validate();
        return prefWidth;
    }

    public float getPrefHeight () {
        validate();
        return prefHeight;
    }

    public void setCullingArea (Rectangle cullingArea) {
        this.cullingArea = cullingArea;
    }

    /** The style for a list, see {@link List}.
     * @author mzechner
     * @author Nathan Sweet */
    static public class ListStyle {
        public BitmapFont font;
        public Color fontColorSelected = new Color(1, 1, 1, 1);
        public Color fontColorUnselected = new Color(1, 1, 1, 1);
        public Drawable selection;
        /** Optional. */
        public Drawable background;

        public ListStyle () {
        }

        public ListStyle (BitmapFont font, Color fontColorSelected, Color fontColorUnselected, Drawable selection) {
            this.font = font;
            this.fontColorSelected.set(fontColorSelected);
            this.fontColorUnselected.set(fontColorUnselected);
            this.selection = selection;
        }

        public ListStyle (ListStyle style) {
            this.font = style.font;
            this.fontColorSelected.set(style.fontColorSelected);
            this.fontColorUnselected.set(style.fontColorUnselected);
            this.selection = style.selection;
        }
    }
}
