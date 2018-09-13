package com.wcg.firstaademo;

import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;

public class GameActivity extends BaseGameActivity {

    public static final int CAMERA_WIDTH = 800;
    public static final int CAMERA_HEIGHT = 480;

    BitmapTextureAtlas playerTextureAtlas;
    TextureRegion playerTextureRegion;
    Scene scene;
    PhysicsWorld physicsWorld;
    SceneManager sceneManager;
    Camera mCamera;

    @Override
    public EngineOptions onCreateEngineOptions() {
        mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        EngineOptions options = new EngineOptions(true,
                ScreenOrientation.LANDSCAPE_FIXED,
                new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
        return options;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        playerTextureAtlas = new BitmapTextureAtlas(getTextureManager(), 64, 64);
        playerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                playerTextureAtlas, this,
                "player.png", 0, 0);
        playerTextureAtlas.load();
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }


    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) {
//        loadGfx();
//        createWalls();
        sceneManager = new SceneManager(this, mEngine, mCamera);
        sceneManager.loadSplashResources();

//        pOnCreateSceneCallback.onCreateSceneFinished(scene);
        pOnCreateSceneCallback.onCreateSceneFinished(sceneManager.createSplashScene());
    }

    private void createWalls() {
        FixtureDef WALL_FIX = PhysicsFactory.createFixtureDef(0.0f, 0.0f, 0.0f);
        Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 15, CAMERA_WIDTH, 15, mEngine.getVertexBufferObjectManager());
        ground.setColor(new Color(15, 50, 0));
        PhysicsFactory.createBoxBody(physicsWorld, ground, BodyDef.BodyType.StaticBody, WALL_FIX);
        scene.attachChild(ground);
    }

    private void loadGfx() {
        scene = new Scene();
        scene.setBackground(new Background(211, 211, 211));
        physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
        scene.registerUpdateHandler(physicsWorld);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) {
        /*Sprite sPlayer = new Sprite(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2, playerTextureRegion, mEngine.getVertexBufferObjectManager());
//        sPlayer.setRotation(45.0f);
        final FixtureDef PLAYER_FIX = PhysicsFactory.createFixtureDef(10.0f, 1.0f, 0.0f);
        Body body = PhysicsFactory.createCircleBody(physicsWorld, sPlayer, BodyDef.BodyType.DynamicBody, PLAYER_FIX);
        scene.attachChild(sPlayer);
        physicsWorld.registerPhysicsConnector(new PhysicsConnector(sPlayer, body, true, false));*/
        mEngine.registerUpdateHandler(new TimerHandler(3f, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                mEngine.unregisterUpdateHandler(pTimerHandler);
                sceneManager.loadMenuResources();
                sceneManager.createMenuScene();
                sceneManager.setCurrentScene(SceneManager.AllScenes.MENU);
            }
        }));
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }
}
