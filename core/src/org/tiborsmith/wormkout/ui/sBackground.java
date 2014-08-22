package org.tiborsmith.wormkout.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

/**
 * Created by tibor on 20.8.14.
 */
public class sBackground extends Widget {

    private Scaling scaling;
    private int align = Align.center;
    private float imageX, imageY, imageWidth, imageHeight;
    private Drawable drawable;
    private ShaderProgram shader;

    /** Creates an image with no region or patch, stretched, and aligned center. */
    public sBackground () {
        this((Drawable)null);
    }

    public void setShader(ShaderProgram shader){
        this.shader = shader;
    }

    /** Creates an image stretched, and aligned center.
     * @param patch May be null. */
    public sBackground (NinePatch patch) {
        this(new NinePatchDrawable(patch), Scaling.stretch, Align.center);
    }

    /** Creates an image stretched, and aligned center.
     * @param region May be null. */
    public sBackground (TextureRegion region) {
        this(new TextureRegionDrawable(region), Scaling.stretch, Align.center);
    }

    /** Creates an image stretched, and aligned center. */
    public sBackground (Texture texture) {
        this(new TextureRegionDrawable(new TextureRegion(texture)));
    }

    /** Creates an image stretched, and aligned center. */
    public sBackground (Skin skin, String drawableName) {
        this(skin.getDrawable(drawableName), Scaling.stretch, Align.center);
    }

    /** Creates an image stretched, and aligned center.
     * @param drawable May be null. */
    public sBackground (Drawable drawable) {
        this(drawable, Scaling.stretch, Align.center);
    }

    /** Creates an image aligned center.
     * @param drawable May be null. */
    public sBackground (Drawable drawable, Scaling scaling) {
        this(drawable, scaling, Align.center);
    }

    /** @param drawable May be null. */
    public sBackground (Drawable drawable, Scaling scaling, int align) {
        if (drawable == null) {
            Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLUE);
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            drawable = new TextureRegionDrawable(new TextureRegion(texture));
            pixmap.dispose();
            texture.dispose();
        }

        setDrawable(drawable);
        this.scaling = scaling;
        this.align = align;
        setSize(getPrefWidth(), getPrefHeight());
        // ShaderProgram.pedantic = false;

    }

    public void layout () {
        if (drawable == null) return;

        float regionWidth = drawable.getMinWidth();
        float regionHeight = drawable.getMinHeight();
        float width = getWidth();
        float height = getHeight();

        Vector2 size = scaling.apply(regionWidth, regionHeight, width, height);
        imageWidth = size.x;
        imageHeight = size.y;

        if ((align & Align.left) != 0)
            imageX = 0;
        else if ((align & Align.right) != 0)
            imageX = (int)(width - imageWidth);
        else
            imageX = (int)(width / 2 - imageWidth / 2);

        if ((align & Align.top) != 0)
            imageY = (int)(height - imageHeight);
        else if ((align & Align.bottom) != 0)
            imageY = 0;
        else
            imageY = (int)(height / 2 - imageHeight / 2);
    }

    public void draw (Batch batch, float parentAlpha, float timer) {
        shader.begin();
        shader.setUniformf("u_time", timer);
        shader.end();
        batch.setShader(shader);
        batch.begin();
        validate();

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        float x = getX();
        float y = getY();
        float scaleX = getScaleX();
        float scaleY = getScaleY();



        //if (drawable != null)
        drawable.draw(batch, x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
        batch.end();
        batch.setShader(null);
    }

    public void setDrawable (Skin skin, String drawableName) {
        setDrawable(skin.getDrawable(drawableName));
    }

    public void setDrawable (Drawable drawable) {
        if (this.drawable == drawable) return;
        if (drawable != null) {
            if (getPrefWidth() != drawable.getMinWidth() || getPrefHeight() != drawable.getMinHeight()) invalidateHierarchy();
        } else
            invalidateHierarchy();
        this.drawable = drawable;
    }

    public Drawable getDrawable () {
        return drawable;
    }

    public void setScaling (Scaling scaling) {
        if (scaling == null) throw new IllegalArgumentException("scaling cannot be null.");
        this.scaling = scaling;
    }

    public void setAlign (int align) {
        this.align = align;
    }

    public float getMinWidth () {
        return 0;
    }

    public float getMinHeight () {
        return 0;
    }

    public float getPrefWidth () {
        if (drawable != null) return drawable.getMinWidth();
        return 0;
    }

    public float getPrefHeight () {
        if (drawable != null) return drawable.getMinHeight();
        return 0;
    }

    public float getImageX () {
        return imageX;
    }

    public float getImageY () {
        return imageY;
    }

    public float getImageWidth () {
        return imageWidth;
    }

    public float getImageHeight () {
        return imageHeight;
    }

}