package cc.clv;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class LibgdxTest extends ApplicationAdapter {
    SpriteBatch batch;
    BitmapFont font;
    Texture img;
    Sprite sprite;
    Vector2 pos;
    float scale;
    float angle;

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
        font = new BitmapFont();
        sprite = new Sprite(img);
        pos = new Vector2();
        scale = 1.0f;
        angle = 0.0f;
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            pos.y += 1.0;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            pos.x -= 1.0;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            pos.y -= 1.0;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            pos.x += 1.0;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            angle += 0.5;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            angle -= 0.5;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            scale += 0.01;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            scale -= 0.01;
        }

        String info = String.format("pos(%f,%f)", pos.x, pos.y);
        sprite.setPosition(pos.x, pos.y);
        sprite.setScale(scale);
        sprite.setRotation(angle);

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        sprite.draw(batch);
        font.draw(batch, info, 0, 420);
        font.draw(batch, "Hello LibGDX", 200, 400);
        batch.end();
    }

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
        img.dispose();
    }
}
