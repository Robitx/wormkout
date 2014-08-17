package org.tiborsmith.wormkout.steady;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by tibor on 17.8.14.
 */
public class MyShaders {
    public static ShaderProgram fontShader = new ShaderProgram(Gdx.files.internal("shaders/font.vert"), Gdx.files.internal("shaders/font.frag"));

}
