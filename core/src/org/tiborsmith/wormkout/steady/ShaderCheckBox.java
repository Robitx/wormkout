package org.tiborsmith.wormkout.steady;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by tibor on 17.8.14.
 */
public class ShaderCheckBox extends TextButton {
    private Image image;
    private Cell imageCell;
    private CheckBox.CheckBoxStyle style;

    public ShaderCheckBox(String text, Skin skin) {
        super(text, skin.get(CheckBox.CheckBoxStyle.class));
        clearChildren();
        imageCell = add(image = new Image(style.checkboxOff));
        ShaderLabel label = new ShaderLabel(text,skin);
        add(label);
        label.setAlignment(Align.left);
        setSize(getPrefWidth(), getPrefHeight());
    }


    public void setStyle (ButtonStyle style) {
        if (!(style instanceof CheckBox.CheckBoxStyle)) throw new IllegalArgumentException("style must be a CheckBoxStyle.");
        super.setStyle(style);
        this.style = (CheckBox.CheckBoxStyle)style;
    }

    /** Returns the checkbox's style. Modifying the returned style may not have an effect until {@link #setStyle(ButtonStyle)} is
     * called. */
    public CheckBox.CheckBoxStyle getStyle () {
        return style;
    }

    public void draw (Batch batch, float parentAlpha) {
        Drawable checkbox = null;
        if (isDisabled()) {
            if (isChecked() && style.checkboxOnDisabled != null)
                checkbox = style.checkboxOnDisabled;
            else
                checkbox = style.checkboxOffDisabled;
        }
        if (checkbox == null) {
            if (isChecked() && style.checkboxOn != null)
                checkbox = style.checkboxOn;
            else if (isOver() && style.checkboxOver != null && !isDisabled())
                checkbox = style.checkboxOver;
            else
                checkbox = style.checkboxOff;
        }
        image.setDrawable(checkbox);
        super.draw(batch, parentAlpha);


    }

    public Image getImage () {
        return image;
    }

    public Cell getImageCell () {
        return imageCell;
    }


}
