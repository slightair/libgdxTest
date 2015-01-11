package cc.clv;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Created by slightair on 15/01/03.
 */
public class TestShader implements Shader {
    ShaderProgram program;
    Camera camera;
    RenderContext context;
    int u_projViewTrans;
    int u_worldTrans;
    int u_color;
    int u_lightPos;

    @Override
    public void init() {
        String vertexShaderString = Gdx.files.internal("shaders/test.vertex.glsl").readString();
        String fragmentShaderString = Gdx.files.internal("shaders/test.fragment.glsl").readString();

        program = new ShaderProgram(vertexShaderString, fragmentShaderString);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }

        u_projViewTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        u_color = program.getUniformLocation("u_color");
        u_lightPos = program.getUniformLocation("u_lightPos");
    }

    @Override
    public void dispose() {
        program.dispose();
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;

        program.begin();
        program.setUniformMatrix(u_projViewTrans, camera.combined);
        program.setUniformf(u_lightPos, -24.0f, 60.0f, 24.0f);
        context.setDepthMask(true);
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_BACK);
    }

    @Override
    public void render(Renderable renderable) {
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        Color color = ((ColorAttribute)renderable.material.get(ColorAttribute.Diffuse)).color;
        program.setUniformf(u_color, color.r, color.g, color.b, color.a);
        renderable.mesh.render(program,
                renderable.primitiveType,
                renderable.meshPartOffset,
                renderable.meshPartSize);
    }

    @Override
    public void end() {
        program.end();
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return instance.material.has(ColorAttribute.Diffuse);
    }
}
