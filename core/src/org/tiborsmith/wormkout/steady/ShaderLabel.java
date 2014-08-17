package org.tiborsmith.wormkout.steady;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by tibor on 17.8.14.
 */
public class ShaderLabel  extends Label{

    private ShaderProgram shader;
    private float scale;


    public ShaderLabel (CharSequence text, LabelStyle style) {
        super(text,style);
        this.shader = MyShaders.fontShader;
        scale = 1;
        super.setScale(scale);
    }

    public ShaderLabel(CharSequence text, Skin skin) {
        super(text, skin);
        this.shader = MyShaders.fontShader;
        scale = 1;
    }

    public ShaderLabel(CharSequence text, Skin skin,float scale) {
        super(text, skin);
        this.shader = MyShaders.fontShader;
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

    public void setShaderProgram(ShaderProgram shader) {
        this.shader = shader;
    }
}
