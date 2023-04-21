package com.gdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class Game extends ApplicationAdapter {
	World world;
	SpriteBatch batch;
	Sprite player;
	Texture playerTexture;
	Body playerBody;
	List<Body> wallBodyList = new ArrayList<>();

	public static TiledMap tiledMap;
	public static OrthogonalTiledMapRenderer tiledMapRenderer;

	Box2DDebugRenderer debugRenderer;
	Matrix4 debugMatrix;
	OrthographicCamera camera;

	final float PIXELS_TO_METERS = 100f;
	final short PHYSICS_ENTITY = 0x1;    // 0001
	final short WORLD_ENTITY = 0x1 << 1; // 0010 or 0x2 in hex
	@Override
	public void create () {
		camera = new OrthographicCamera();
		camera.setToOrtho(false,420,420);
		debugRenderer = new Box2DDebugRenderer();
		batch = new SpriteBatch();

		playerTexture = new Texture("player.png");
		player = new Sprite(playerTexture);
		player.setPosition(-player.getWidth() / 2, -player.getHeight() / 2);

		world = new World(new Vector2(0f, 0f), true);

		// Player body def
		BodyDef playerDef = new BodyDef();
		playerDef.type = BodyDef.BodyType.DynamicBody;
		playerDef.position.set((player.getX() + player.getWidth()/2) / PIXELS_TO_METERS, (player.getY() + player.getHeight()/2) / PIXELS_TO_METERS);
		playerBody = world.createBody(playerDef); // create body in world


		// Shape
		PolygonShape shape = new PolygonShape(); // create shape
		shape.setAsBox(player.getWidth() / 2 / PIXELS_TO_METERS, player.getHeight() / 2 / PIXELS_TO_METERS);


		// Player
		FixtureDef fixtureDefPlayer = new FixtureDef(); // create fixture
		fixtureDefPlayer.shape = shape; // shape
		fixtureDefPlayer.density = 3f; // density
		fixtureDefPlayer.restitution = 0.5f; // restitution: bounciness
		fixtureDefPlayer.filter.categoryBits = PHYSICS_ENTITY;
		fixtureDefPlayer.filter.maskBits = WORLD_ENTITY;

		playerBody.createFixture(fixtureDefPlayer);

		shape.dispose();

		// Loading Tilemap
		tiledMap = new TmxMapLoader().load("Tilemap.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
		MapObjects objects = tiledMap.getLayers().get("Collision").getObjects();

		wallBodyList = new ArrayList<>();

		for (MapObject object : objects) {
			Shape objShape;

			if (object instanceof PolygonMapObject) {
				objShape = createPolygon((PolygonMapObject)object);
			} else {
				continue;
			}

			Body objBody;
			BodyDef objDef = new BodyDef();
			objDef.type = BodyDef.BodyType.StaticBody;
			objBody = world.createBody(objDef);

			FixtureDef fixtureObjDef = new FixtureDef(); // create fixture
			fixtureObjDef.shape = objShape; // shape
			fixtureObjDef.density = 3.0f; // density
			fixtureObjDef.restitution = 0.5f; // restitution: bounciness
			fixtureObjDef.filter.categoryBits = WORLD_ENTITY;
			fixtureObjDef.filter.maskBits = PHYSICS_ENTITY;

			objBody.createFixture(fixtureObjDef);
			shape.dispose();

			wallBodyList.add(objBody);
		}

		System.out.println(wallBodyList);
	}
	public PolygonShape createPolygon(PolygonMapObject polygon) {
		float[] vertices = polygon.getPolygon().getTransformedVertices();
		Vector2[] worldVertices = new Vector2[vertices.length / 2];

		for (int i = 0; i < worldVertices.length; i++) {
			worldVertices[i] = new Vector2(vertices[i * 2] / PIXELS_TO_METERS, vertices[i * 2 + 1] / PIXELS_TO_METERS);
		}
		PolygonShape shape = new PolygonShape();
		shape.set(worldVertices);
		return shape;
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 1, 1, 1);
		world.step(1/60f, 6, 2);
		camera.position.set(player.getX(), player.getY(), 0);
		camera.update();

		InputHandler();

		playerBody.setFixedRotation(true);

		player.setPosition((playerBody.getPosition().x * PIXELS_TO_METERS) - player.getWidth() / 2 , (playerBody.getPosition().y * PIXELS_TO_METERS) - player.getHeight() / 2);
		player.setRotation((float)Math.toDegrees(playerBody.getAngle()));

		batch.setProjectionMatrix(camera.combined);
		debugMatrix = batch.getProjectionMatrix().cpy().scale(PIXELS_TO_METERS, PIXELS_TO_METERS, 0);

		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();

		batch.begin();

		batch.draw(player, player.getX(), player.getY(), player.getOriginX(), player.getOriginY(), player.getWidth(), player.getHeight(), player.getScaleX(), player.getScaleY(),player.getRotation());

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
