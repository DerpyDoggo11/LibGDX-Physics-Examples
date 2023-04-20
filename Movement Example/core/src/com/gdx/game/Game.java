package com.gdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;

public class Game extends ApplicationAdapter {

	World world;
	SpriteBatch batch;
	Sprite player;
	Texture playerTexture;
	Body body;
	Box2DDebugRenderer debugRenderer;
	Matrix4 debugMatrix;
	OrthographicCamera camera;
	boolean smoothMovement = true;
	final float PIXELS_TO_METERS = 100f;
	@Override
	public void create () {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		debugRenderer = new Box2DDebugRenderer();

		batch = new SpriteBatch();
		playerTexture = new Texture("player.png");
		player = new Sprite(playerTexture);

		player.setPosition(-player.getWidth() / 2, -player.getHeight() / 2);

		world = new World(new Vector2(0f, 0f), true);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(0,0);

		bodyDef.position.set((player.getX() + player.getWidth()/2) / PIXELS_TO_METERS, (player.getY() + player.getHeight()/2) / PIXELS_TO_METERS);

		body = world.createBody(bodyDef); // create body in world
		PolygonShape shape = new PolygonShape(); // create shape
		shape.setAsBox(player.getWidth() / 2 / PIXELS_TO_METERS, player.getHeight() / 2 / PIXELS_TO_METERS);

		FixtureDef fixtureDef = new FixtureDef(); // create fixture
		fixtureDef.shape = shape; // shape
		fixtureDef.density = 3f; // density
		Fixture fixture = body.createFixture(fixtureDef);

		shape.dispose();
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 1, 1, 1);
		world.step(1/60f, 6, 2);

		body.applyTorque(0.0f,true);
		InputHandler();

		player.setPosition((body.getPosition().x * PIXELS_TO_METERS) - player.getWidth() / 2 , (body.getPosition().y * PIXELS_TO_METERS) - player.getHeight() / 2);
		player.setRotation((float)Math.toDegrees(body.getAngle()));

		batch.setProjectionMatrix(camera.combined);
		debugMatrix = batch.getProjectionMatrix().cpy().scale(PIXELS_TO_METERS, PIXELS_TO_METERS, 0);

		batch.begin();
		batch.draw(player, player.getX(), player.getY(), player.getOriginX(), player.getOriginY(), player.getWidth(), player.getHeight(), player.getScaleX(), player.getScaleY(),player.getRotation());

		batch.end();

		debugRenderer.render(world, debugMatrix);
	}

	public void InputHandler() {

		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && smoothMovement) {
			smoothMovement = false;

		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && !smoothMovement) {
			smoothMovement = true;

		}

		if (smoothMovement) {
			if (Gdx.input.isKeyPressed(Input.Keys.W)) {
				body.applyForceToCenter(0f,1f,true);

			}
			if (Gdx.input.isKeyPressed(Input.Keys.S)) {
				body.applyForceToCenter(0f,-1f,true);

			}
			if (Gdx.input.isKeyPressed(Input.Keys.A)) {
				body.applyForceToCenter(-1f,0f,true);

			}
			if (Gdx.input.isKeyPressed(Input.Keys.D)) {
				body.applyForceToCenter(1f,0f,true);

			}
		}

		if (!smoothMovement) {

			if (Gdx.input.isKeyPressed(Input.Keys.W)) {
				body.setLinearVelocity(0f, 5f);

			}

			if (Gdx.input.isKeyPressed(Input.Keys.S)) {
				body.setLinearVelocity(0f, -5f);

			}
			if (Gdx.input.isKeyPressed(Input.Keys.A)) {
				body.setLinearVelocity(-5f, 0f);

			}
			if (Gdx.input.isKeyPressed(Input.Keys.D)) {
				body.setLinearVelocity(5f, 0f);

			}

			if (!Gdx.input.isKeyPressed(Input.Keys.W) && !Gdx.input.isKeyPressed(Input.Keys.S) && !Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.D)) {
				body.setLinearVelocity(0f, 0f);
			}

		}



		if (Gdx.input.isKeyPressed(Input.Keys.LEFT_BRACKET)) {
			body.applyTorque(0.1f, true); // rotate left

		} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT_BRACKET)) {
			body.applyTorque(-0.1f, true); // rotate right

		}

	}

	@Override
	public void dispose () {
		batch.dispose();
		world.dispose();
		playerTexture.dispose();

	}

}