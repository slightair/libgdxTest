package cc.clv;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.CollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithmConstructionInfo;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDispatcherInfo;
import com.badlogic.gdx.physics.bullet.collision.btManifoldResult;
import com.badlogic.gdx.physics.bullet.collision.btSphereBoxCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.utils.Array;

public class LibgdxTest extends ApplicationAdapter {

    private PerspectiveCamera camera;
    private CameraInputController cameraInputController;
    private ModelBatch modelBatch;
    private Array<ModelInstance> instances = new Array<ModelInstance>();
    private Environment environment;
    private Model model;
    private ModelInstance ground;
    private ModelInstance ball;
    private boolean collision;
    private btCollisionShape groundShape;
    private btCollisionShape ballShape;
    private btCollisionObject groundObject;
    private btCollisionObject ballObject;
    private btCollisionConfiguration collisionConfiguration;
    private btDispatcher dispatcher;

    @Override
    public void create() {
        Bullet.init();

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(3f, 7f, 10f);
        camera.lookAt(0, 4f, 0);
        camera.update();

        cameraInputController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraInputController);

        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        mb.node().id = "ground";
        mb.part("box", GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.RED)))
                .box(5f, 1f, 5f);
        mb.node().id = "ball";
        mb.part("sphere", GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.GREEN)))
                .sphere(1f, 1f, 1f, 10, 10);
        model = mb.end();

        ground = new ModelInstance(model, "ground");
        ball = new ModelInstance(model, "ball");
        ball.transform.setToTranslation(0, 9f, 0);

        instances.add(ground);
        instances.add(ball);

        ballShape = new btSphereShape(0.5f);
        groundShape = new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f));

        groundObject = new btCollisionObject();
        groundObject.setCollisionShape(groundShape);
        groundObject.setWorldTransform(ground.transform);

        ballObject = new btCollisionObject();
        ballObject.setCollisionShape(ballShape);
        ballObject.setWorldTransform(ball.transform);

        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
    }

    boolean checkCollision() {
        CollisionObjectWrapper collisionObjectWrapperForBall = new CollisionObjectWrapper(
                ballObject);
        CollisionObjectWrapper collisionObjectWrapperForGround = new CollisionObjectWrapper(
                groundObject);

        btCollisionAlgorithmConstructionInfo collisionAlgorithmConstructionInfo
                = new btCollisionAlgorithmConstructionInfo();
        collisionAlgorithmConstructionInfo.setDispatcher1(dispatcher);
        btCollisionAlgorithm collisionAlgorithm = new btSphereBoxCollisionAlgorithm(null,
                collisionAlgorithmConstructionInfo,
                collisionObjectWrapperForBall.wrapper,
                collisionObjectWrapperForGround.wrapper,
                false);

        btDispatcherInfo dispatcherInfo = new btDispatcherInfo();
        btManifoldResult manifoldResult = new btManifoldResult(
                collisionObjectWrapperForBall.wrapper, collisionObjectWrapperForGround.wrapper);

        collisionAlgorithm.processCollision(collisionObjectWrapperForBall.wrapper,
                collisionObjectWrapperForGround.wrapper, dispatcherInfo, manifoldResult);

        boolean r = manifoldResult.getPersistentManifold().getNumContacts() > 0;

        manifoldResult.dispose();
        dispatcherInfo.dispose();
        collisionAlgorithm.dispose();
        collisionAlgorithmConstructionInfo.dispose();
        collisionObjectWrapperForBall.dispose();
        collisionObjectWrapperForGround.dispose();

        return r;
    }

    @Override
    public void render() {
        final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        if (!collision) {
            ball.transform.translate(0f, -delta * 10, 0f);
            ballObject.setWorldTransform(ball.transform);
            collision = checkCollision();
        }

        cameraInputController.update();

        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        groundObject.dispose();
        groundShape.dispose();

        ballObject.dispose();
        ballShape.dispose();

        dispatcher.dispose();
        collisionConfiguration.dispose();

        modelBatch.dispose();
        model.dispose();
        instances.clear();
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
