package com.wcg.firstaademo;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;

import org.andengine.input.touch.controller.MultiTouch;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.math.MathUtils;

import android.opengl.GLES20;
import android.widget.Toast;

public class Pong extends SimpleBaseGameActivity {
    // ===========================================================
    // Constants
    // ===========================================================

    private static final int CAMERA_WIDTH = 480;
    private static final int CAMERA_HEIGHT = 320;

    // ===========================================================
    // Fields
    // ===========================================================

    private Camera mCamera;

    private BitmapTextureAtlas mBitmapTextureAtlas;
    private ITextureRegion mPaddleTextureRegion;
    //private TiledTextureRegion mFaceTextureRegion;
    private TiledTextureRegion mBrontTextureRegion;

    private BitmapTextureAtlas mOnScreenControlTexture;
    private ITextureRegion mOnScreenControlBaseTextureRegion;
    private ITextureRegion mOnScreenControlKnobTextureRegion;

    private boolean mPlaceOnScreenControlsAtDifferentVerticalLocations = false;

    private static final float DEMO_VELOCITY = 100.0f;

    static Sprite paddle;
    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public EngineOptions onCreateEngineOptions() {
        this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
        engineOptions.getTouchOptions().setNeedsMultiTouch(false);
        return engineOptions;
    }

    @Override
    public void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 160, TextureOptions.BILINEAR);
        //this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_circle_tiled.png", 0, 0, 2, 1);
        this.mBrontTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "bront1_tiled.png", 0, 0, 1, 5); //
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 96, TextureOptions.BILINEAR);
        this.mPaddleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "line.jpg", 0, 0);
        this.mBitmapTextureAtlas.load();

        this.mOnScreenControlTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
        this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
        this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
        this.mOnScreenControlTexture.load();
    }

    @Override
    public Scene onCreateScene() {
        this.mEngine.registerUpdateHandler(new FPSLogger());

        final Scene scene = new Scene();
        scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

        final float centerX = (CAMERA_WIDTH - this.mPaddleTextureRegion.getWidth()) / 2;
        final float centerY = (CAMERA_HEIGHT - this.mPaddleTextureRegion.getHeight()) / 2;
        paddle = new Sprite(centerX, centerY, this.mPaddleTextureRegion, this.getVertexBufferObjectManager());
        final PhysicsHandler physicsHandler = new PhysicsHandler(paddle);
        paddle.registerUpdateHandler(physicsHandler);

        scene.attachChild(paddle);

        /* Velocity control (left). */
        final float x1 = CAMERA_WIDTH - this.mOnScreenControlBaseTextureRegion.getWidth();
        final float y1 = CAMERA_HEIGHT / 2;
        final AnalogOnScreenControl velocityOnScreenControl = new AnalogOnScreenControl(x1, y1, this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new IAnalogOnScreenControlListener() {
            @Override
            public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
                physicsHandler.setVelocity(pValueX * 100, pValueY * 100);
            }

            @Override
            public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
                /* Nothing. */
            }
        });
        velocityOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        velocityOnScreenControl.getControlBase().setAlpha(0.5f);

        scene.setChildScene(velocityOnScreenControl);

        // Create a shaking brontosaurus
        final float cX = 0;
        final float cY = (CAMERA_HEIGHT - this.mBrontTextureRegion.getHeight()) / 2;
        final Bront bront = new Bront(cX, cY, this.mBrontTextureRegion, this.getVertexBufferObjectManager());
        bront.registerUpdateHandler(physicsHandler);
        scene.attachChild(bront);


        return scene;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private static class Bront extends AnimatedSprite {
        private final PhysicsHandler mPhysicsHandler;


        public Bront(final float pX, final float pY, final TiledTextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {


            super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
            this.animate(100);
            this.mPhysicsHandler = new PhysicsHandler(this);
            this.registerUpdateHandler(this.mPhysicsHandler);
            this.mPhysicsHandler.setVelocity(DEMO_VELOCITY, DEMO_VELOCITY);

        }

        @Override
        protected void onManagedUpdate(final float pSecondsElapsed) {
            if (this.mX < 0) {
                this.mPhysicsHandler.setVelocityX(DEMO_VELOCITY);
            } else if (this.mX + this.getWidth() > CAMERA_WIDTH) {
                this.mPhysicsHandler.setVelocityX(-DEMO_VELOCITY);
            }

            if (this.mY < 0) {
                this.mPhysicsHandler.setVelocityY(DEMO_VELOCITY);
            } else if (this.mY + this.getHeight() > CAMERA_HEIGHT) {
                this.mPhysicsHandler.setVelocityY(-DEMO_VELOCITY);
            }

            if (paddle.collidesWith(this)) {

                float vx = this.mPhysicsHandler.getVelocityX();
                float vy = this.mPhysicsHandler.getVelocityY();
                this.mPhysicsHandler.setVelocity(-vx, -vy);
            }
            super.onManagedUpdate(pSecondsElapsed);
        }
    }
}
