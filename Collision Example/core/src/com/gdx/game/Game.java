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
	Sprite player, wall;
	Texture playerTexture, wallTexture;
	Body playerBody, wallBody;

	Box2DDebugRenderer debugRenderer;
	Matrix4 debugMatrix;
	OrthographicCamera camera;

	final float PIXELS_TO_METERS = 100f;
	final short PHYSICS_ENTITY = 0x1;    // 0001
	final short WORLD_ENTITY = 0x1 << 1; // 0010 or 0x2 in hex
	@Override
	public void create () {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		debugRenderer = new Box2DDebugRenderer();

		batch = new SpriteBatch();

		playerTexture = new Texture("player.png");
		wallTexture = new Texture("wall.png");

		player = new Sprite(playerTexture);
		wall = new Sprite(wallTexture);

		player.setPosition(-player.getWidth() / 2, -player.getHeight() / 2);
		wall.setPosition(-wall.getWidth() / 2, -wall.getHeight() / 2 - 150);

		world = new World(new Vector2(0f, 0f), true);

		// Player body def
		BodyDef playerDef = new BodyDef();
		playerDef.type = BodyDef.BodyType.DynamicBody;
		playerDef.position.set((player.getX() + player.getWidth()/2) / PIXELS_TO_METERS, (player.getY() + player.getHeight()/2) / PIXELS_TO_METERS);
		playerBody = world.createBody(playerDef); // create body in world

		// Wall body def
		BodyDef wallDef = new BodyDef();
		wallDef.type = BodyDef.BodyType.StaticBody;
		wallDef.position.set((wall.getX() + wall.getWidth()/2) / PIXELS_TO_METERS, (wall.getY() + wall.getHeight()/2) / PIXELS_TO_METERS);
		wallBody = world.createBody(wallDef);

		// Shape
		PolygonShape shape = new PolygonShape(); // create shape
		shape.setAsBox(player.getWidth() / 2 / PIXELS_TO_METERS, player.getHeight() / 2 / PIXELS_TO_METERS);

		PolygonShape wallShape = new PolygonShape(); // create shape
		wallShape.setAsBox(wall.getWidth() / 2 / PIXELS_TO_METERS, wall.getHeight() / 2 / PIXELS_TO_METERS);

		// Player
		FixtureDef fixtureDefPlayer = new FixtureDef(); // create fixture
		fixtureDefPlayer.shape = shape; // shape
		fixtureDefPlayer.density = 3f; // density
		fixtureDefPlayer.restitution = 0.5f; // restitution: bounciness
		fixtureDefPlayer.filter.categoryBits = PHYSICS_ENTITY;
		fixtureDefPlayer.filter.maskBits = WORLD_ENTITY;

		// Wall
		FixtureDef fixtureDefWall = new FixtureDef();
		fixtureDefWall.shape = wallShape;
		fixtureDefWall.density = 0.1f;
		fixtureDefWall.restitution = 0.5f;
		fixtureDefWall.filter.categoryBits = WORLD_ENTITY;
		fixtureDefWall.filter.maskBits = PHYSICS_ENTITY; // sets collision with player

		playerBody.createFixture(fixtureDefPlayer);
		wallBody.createFixture(fixtureDefWall);

		shape.dispose();
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 1, 1, 1);
		world.step(1/60f, 6, 2);

		InputHandler();

		playerBody.setFixedRotation(true);

		player.setPosition((playerBody.getPosition().x * PIXELS_TO_METERS) - player.getWidth() / 2 , (playerBody.getPosition().y * PIXELS_TO_METERS) - player.getHeight() / 2);
		player.setRotation((float)Math.toDegrees(playerBody.getAngle()));

		wall.setPosition((wallBody.getPosition().x * PIXELS_TO_METERS) - wall.getWidth()/2 , (wallBody.getPosition().y * PIXELS_TO_METERS) -wall.getHeight()/2 );
		wall.setRotation((float)Math.toDegrees(wallBody.getAngle()));

		batch.setProjectionMatrix(camera.combined);
		debugMatrix = batch.getProjectionMatrix().cpy().scale(PIXELS_TO_METERS, PIXELS_TO_METERS, 0);

		batch.begin();

		batch.draw(player, player.getX(), player.getY(), player.getOriginX(), player.getOriginY(), player.getWidth(), player.getHeight(), player.getScaleX(), player.getScaleY(),player.getRotation());
		batch.draw(wall, wall.getX(), wall.getY(),wall.getOriginX(), wall.getOriginY(), wall.getWidth(),wall.getHeight(),wall.getScaleX(),wall.getScaleY(),wall.getRotation());

		batch.end();

		debugRenderer.render(world, debugMatrix);
	}

	public void InputHandler() {

		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			playerBody.applyForceToCenter(0f,1f,true);

		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			playerBody.applyForceToCenter(0f,-1f,true);

		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			playerBody.applyForceToCenter(-1f,0f,true);

		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			playerBody.applyForceToCenter(1f,0f,true);

		}

	}

	@Override
	public void dispose () {
		batch.dispose();
		world.dispose();
		playerTexture.dispose();

	}
}
