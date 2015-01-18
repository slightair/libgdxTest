package cc.clv;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class LibgdxTest extends ApplicationAdapter {
    public Environment environment;
    public DirectionalShadowLight shadowLight;
    public PerspectiveCamera camera;
    public CameraInputController cameraInputController;
    public ModelBatch modelBatch;
    public ModelBatch shadowBatch;
    public AssetManager assetManager;
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    public Array<AnimationController> animationControllers = new Array<AnimationController>();
    public boolean loading;

    @Override
    public void create() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        DirectionalLight directionalLight = new DirectionalLight().set(0.8f, 0.8f, 0.8f, 8f, -32f, -32f);
        environment.add(directionalLight);
        shadowLight = new DirectionalShadowLight(1024, 1024, 1024, 1024, 1f, 500f);
        shadowLight.set(directionalLight);
        environment.shadowMap = shadowLight;

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(50f, 50f, 50f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        cameraInputController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraInputController);

        assetManager = new AssetManager();
        assetManager.load("models/mushroom.g3db", Model.class);
        loading = true;

        ModelBuilder modelBuilder = new ModelBuilder();
        Model ground = modelBuilder.createBox(100f, 1f, 100f,
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                Usage.Position | Usage.Normal);
        ModelInstance groundInstance = new ModelInstance(ground);
        instances.add(groundInstance);

        modelBatch = new ModelBatch();
        shadowBatch = new ModelBatch(new DepthShaderProvider());
    }

    private void doneLoading() {
        Model mushroom = assetManager.get("models/mushroom.g3db", Model.class);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                ModelInstance mushroomInstance = new ModelInstance(mushroom);
                mushroomInstance.transform.setToTranslation(x * 20f, 0, z * 20f);
                instances.add(mushroomInstance);

                AnimationController animationController = new AnimationController(mushroomInstance);
                animationController.setAnimation("mushroom|mushroomAction", -1);
                animationControllers.add(animationController);
            }
        }
        loading = false;
    }

    @Override
    public void render() {
        if (loading && assetManager.update()) {
            doneLoading();
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        cameraInputController.update();

        float deltaTime = Gdx.graphics.getDeltaTime();
        for (AnimationController animationController: animationControllers) {
            animationController.update(deltaTime);
        }

        shadowLight.begin(Vector3.Zero, camera.direction);
        shadowBatch.begin(shadowLight.getCamera());
        shadowBatch.render(instances);
        shadowBatch.end();
        shadowLight.end();

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        instances.clear();
        animationControllers.clear();
        assetManager.dispose();
    }

    @Override
    public void resume() {
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }
}
