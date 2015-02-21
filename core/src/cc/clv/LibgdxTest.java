package cc.clv;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;

public class LibgdxTest extends ApplicationAdapter {

    static class GameObject extends ModelInstance implements Disposable {

        public final btCollisionObject body;
        public boolean moving;

        public GameObject(Model model, String node, btCollisionShape shape) {
            super(model, node);
            body = new btCollisionObject();
            body.setCollisionShape(shape);
        }

        @Override
        public void dispose() {
            body.dispose();
        }

        static class Constructor implements Disposable {

            public final Model model;
            public final String node;
            public final btCollisionShape shape;

            public Constructor(Model model, String node, btCollisionShape shape) {
                this.model = model;
                this.node = node;
                this.shape = shape;
            }

            public GameObject construct() {
                return new GameObject(model, node, shape);
            }

            @Override
            public void dispose() {
                shape.dispose();
            }
        }
    }

    class MyContactListener extends ContactListener {

        @Override
        public boolean onContactAdded(int userValue0, int partId0, int index0, int userValue1,
                int partId1, int index1) {
            instances.get(userValue0).moving = false;
            instances.get(userValue1).moving = false;
            return true;
        }
    }

    private PerspectiveCamera camera;
    private CameraInputController cameraInputController;
    private ModelBatch modelBatch;
    private Array<GameObject> instances = new Array<GameObject>();
    private ArrayMap<String, GameObject.Constructor> constructors;
    private Environment environment;
    private Model model;
    private btCollisionConfiguration collisionConfiguration;
    private btDispatcher dispatcher;
    private float spawnTimer;
    private MyContactListener contactListener;
    private btBroadphaseInterface broadphaseInterface;
    private btCollisionWorld collisionWorld;

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
        mb.part("ground", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.RED)))
                .box(5f, 1f, 5f);
        mb.node().id = "sphere";
        mb.part("sphere", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.GREEN)))
                .sphere(1f, 1f, 1f, 10, 10);
        mb.node().id = "box";
        mb.part("box", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.BLUE)))
                .box(1f, 1f, 1f);
        mb.node().id = "cone";
        mb.part("cone", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW)))
                .cone(1f, 2f, 1f, 10);
        mb.node().id = "capsule";
        mb.part("capsule", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.CYAN)))
                .capsule(0.5f, 2f, 10);
        mb.node().id = "cylinder";
        mb.part("cylinder", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.MAGENTA)))
                .cylinder(1f, 2f, 1f, 10);
        model = mb.end();

        constructors = new ArrayMap<String, GameObject.Constructor>(String.class,
                GameObject.Constructor.class);
        constructors.put("ground", new GameObject.Constructor(model, "ground",
                new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f))));
        constructors.put("sphere",
                new GameObject.Constructor(model, "sphere", new btSphereShape(0.5f)));
        constructors.put("box", new GameObject.Constructor(model, "box",
                new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f))));
        constructors
                .put("cone", new GameObject.Constructor(model, "cone", new btConeShape(0.5f, 2f)));
        constructors.put("capsule",
                new GameObject.Constructor(model, "capsule", new btCapsuleShape(.5f, 1f)));
        constructors.put("cylinder", new GameObject.Constructor(model, "cylinder",
                new btCylinderShape(new Vector3(.5f, 1f, .5f))));

        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphaseInterface = new btDbvtBroadphase();
        collisionWorld = new btCollisionWorld(dispatcher, broadphaseInterface,
                collisionConfiguration);
        contactListener = new MyContactListener();

        GameObject ground = constructors.get("ground").construct();
        instances.add(ground);
        collisionWorld.addCollisionObject(ground.body);
    }

    public void spawn() {
        GameObject obj = constructors.values[1 + MathUtils.random(constructors.size - 2)]
                .construct();
        obj.moving = true;
        obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f),
                MathUtils.random(360f));
        obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f));
        obj.body.setWorldTransform(obj.transform);
        obj.body.setUserValue(instances.size);
        obj.body.setCollisionFlags(obj.body.getCollisionFlags()
                | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        instances.add(obj);
        collisionWorld.addCollisionObject(obj.body);
    }

    @Override
    public void render() {
        final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        for (GameObject obj : instances) {
            if (obj.moving) {
                obj.transform.trn(0f, -delta * 10, 0f);
                obj.body.setWorldTransform(obj.transform);
            }
        }

        collisionWorld.performDiscreteCollisionDetection();

        if ((spawnTimer -= delta) < 0) {
            spawn();
            spawnTimer = 1.0f;
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
        for (GameObject obj : instances) {
            obj.dispose();
        }
        instances.clear();

        for (GameObject.Constructor constructor : constructors.values()) {
            constructor.dispose();
        }
        constructors.clear();

        collisionWorld.dispose();
        broadphaseInterface.dispose();
        contactListener.dispose();

        dispatcher.dispose();
        collisionConfiguration.dispose();

        modelBatch.dispose();
        model.dispose();
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
