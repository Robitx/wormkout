package org.tiborsmith.wormkout.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by tibor on 17.8.14.
 */
public class sLabel extends Label{

    private static ShaderProgram shader;
    private float scale;


    public sLabel(CharSequence text, LabelStyle style) {
        super(text,style);
        scale = 1;
        super.setScale(scale);
    }

    public sLabel(CharSequence text, Skin skin) {
        super(text, skin);
        scale = 1;
    }

    public sLabel(CharSequence text, Skin skin, float scale) {
        super(text, skin);
        this.scale = scale;
        super.setFontScale(scale);
    }

    public void setScale(float scale){
        this.scale = scale;
        this.setFontScale(scale);
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shader != null) {
            batch.setShader(shader);
            shader.setUniformf("u_smoothing", 0.75f / (4 * scale));
        }
        BitmapFontCache cache = super.getBitmapFontCache();
        cache.setPosition(getX(),getY()+5*scale);
        cache.draw(batch, parentAlpha);
       // super.draw(batch, parentAlpha);

        batch.setShader(null);
    }

    public static void setShader(ShaderProgram shader){
        sLabel.shader = shader;
    }
}
