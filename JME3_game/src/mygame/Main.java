package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.system.AppSettings;

public class Main extends SimpleApplication {
    Material d_mat = new Material();
    public BitmapText hitcount;
    public BitmapText Lose_game;
    public BitmapText Win_game;
    public BitmapText score;
    static Main app = new Main();
    private Camera cam2;
    fps_appstate fps;
    

    
    
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("FPS GAME (lab 18)");//setting the title of the window
        app.setSettings(settings);
        app.setShowSettings(false);//turning of the default settings and not showing first window popup
        app.setDisplayFps(false);
        app.setDisplayStatView(false);
        app.start();//start the game
    }
    
    private void soundOff(){
    AudioNode background = new AudioNode(assetManager,"Sound/Environment/Ocean Waves.ogg");
    background.setVolume(0.1f);
    background.setLooping(true);//adding a loop of the background ocean noise
    rootNode.attachChild(background);
    background.play();    //play the sound
}

    private void displayHUD(){
    //displays the text and location of the hud
    Lose_game = new BitmapText(guiFont, false);
    Win_game = new BitmapText(guiFont, false);
    BitmapText Health = new BitmapText(guiFont, false);
    BitmapText Inventory = new BitmapText(guiFont, false);
    BitmapText Location = new BitmapText(guiFont, false);
    BitmapText crosshair = new BitmapText(guiFont, false);
    BitmapText score_info = new BitmapText(guiFont, false);
    hitcount = new BitmapText(guiFont, false);
    score = new BitmapText(guiFont, false);
    
    LoseGame();
    WinGame();
    healthHUD(Health);
    inventoryHUD(Inventory);
    locationHUD(Location);
    score_infoHUD(score_info);
    hitcountHUD();
    scorecountHUD();
    crosshairHUD(crosshair); 
    
}
    public void LoseGame(){
        Lose_game.setColor(ColorRGBA.Red);
        Lose_game.setText("Do Not Shoot the Dome or get >100pts");
        Lose_game.setLocalTranslation(10, settings.getHeight()-10, 0);
        guiNode.attachChild(Lose_game);
    }
    public void WinGame(){
        Win_game.setColor(ColorRGBA.White);
        Win_game.setText("Get 100 points to kill the moster");
        Win_game.setLocalTranslation(10, settings.getHeight()-30, 0);
        guiNode.attachChild(Win_game);
    }
    public void winLose(){
        if(fps.lose){
            Lose_game.setText("Game Over");
            Lose_game.setSize(40);
            Lose_game.setLocalTranslation(settings.getWidth()/2, settings.getHeight()/2, 0);
            flyCam.setEnabled(false);
        }else if(fps.score >= 100){
            Win_game.setText("Game Won");
            Win_game.setSize(40);
            Win_game.setLocalTranslation(settings.getWidth()/2, settings.getHeight()/2, 0);
            flyCam.setEnabled(false);
        }else{
            Lose_game.setText("Don't shoot dome or get >100pts");
            Lose_game.setColor(ColorRGBA.Red);
            Lose_game.setSize(hitcount.getSize());
            Lose_game.setLocalTranslation(10, settings.getHeight()-10, 0);
            guiNode.attachChild(Lose_game);
        }
    }
    
    public void healthHUD(BitmapText Health){
        Health.setColor(ColorRGBA.Red);
        Health.setText("Health: ");
        Health.setLocalTranslation(10, 70, 0);
        guiNode.attachChild(Health);
    }
    public void inventoryHUD(BitmapText Inventory){
        Inventory.setColor(ColorRGBA.White);
        Inventory.setText("Inventory: ");
        Inventory.setLocalTranslation(10, 50, 0);
        guiNode.attachChild(Inventory);
    }
    public void locationHUD(BitmapText Location){
        Location.setColor(ColorRGBA.Blue);
        Location.setText("Location: ");
        Location.setLocalTranslation(10, 30, 0);
        guiNode.attachChild(Location);
    } 
    public void hitcountHUD(){
        hitcount.setColor(ColorRGBA.Cyan);
        hitcount.setText("Hit Count: "+ fps.hit_count);
        hitcount.setLocalTranslation(10, 90, 0);
        guiNode.attachChild(hitcount);
    }
    public void scorecountHUD(){
        score.setColor(ColorRGBA.Magenta);
        score.setText("Score: "+ fps.score);
        score.setLocalTranslation(10, 110, 0);
        guiNode.attachChild(score);
    }
    public void crosshairHUD(BitmapText crosshair){
        crosshair.setColor(ColorRGBA.LightGray);
        crosshair.setSize(20f);
        crosshair.setText("+");
        crosshair.setLocalTranslation(settings.getWidth()/2-crosshair.getLineWidth()/2,
        settings.getHeight()/2+crosshair.getLineHeight()/2, 0);
        guiNode.attachChild(crosshair);
    }
    public void score_infoHUD(BitmapText score_info){
        score_info.setColor(ColorRGBA.White);
        score_info.setText("Monster: 10pts  Moving Objects: 5pts Everything else: -2pts");
        score_info.setSize(15f);
        score_info.setLocalTranslation(settings.getWidth()-530, 20, 0);
        guiNode.attachChild(score_info);
    }
    
    private void situationalSound(){
        AudioNode shot = new AudioNode(assetManager, "Sound/Effects/Gun.wav");
        shot.setPositional(false);
        shot.setLooping(false);
        shot.setVolume(.1f);
        shot.playInstance();
        rootNode.attachChild(shot);
    }
    private void setupCamera(){
        cam2 = cam.clone();
        cam2.setViewPort(.4f, .6f, 0.8f, 1f);
        cam2.setLocation(new Vector3f(-0.10f, 1.57f, 4.81f));
        ViewPort viewPort2 = renderManager.createMainView("small view", cam2);
        viewPort2.setClearFlags(true, true, true);
        viewPort2.attachScene(rootNode);
    }
    private ActionListener listener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            switch(name){
                case "Green":
                    fps.d_mat.setBoolean("UseMaterialColors", true );
                    fps.d_mat.setColor("Ambient", ColorRGBA.Green);
                    fps.d_mat.setColor("Diffuse", ColorRGBA.Green);
                    break;
                case "Red":
                    fps.d_mat.setBoolean("UseMaterialColors", true );
                    fps.d_mat.setColor("Ambient", ColorRGBA.Red);
                    fps.d_mat.setColor("Diffuse", ColorRGBA.Red);
                    break;
                case "Blue":
                    fps.d_mat.setBoolean("UseMaterialColors", true );
                    fps.d_mat.setColor("Ambient", ColorRGBA.Blue);
                    fps.d_mat.setColor("Diffuse", ColorRGBA.Blue);
                    break;
                case "Shoot":
                    if(!isPressed){
                        fps.collision_detection();
                        situationalSound();
                        break;
                    }
                case "Left":
                    fps.left=true;
                    if(!isPressed){ 
                        fps.left = false;
                    }
                    break;
                case "Right":
                    fps.right=true;
                    if(!isPressed){ 
                        fps.right = false;
                    }
                    break;
                case "Up":
                    fps.up=true;
                    if(!isPressed){ 
                        fps.up = false;
                    }
                    break;
                case "Down":
                    fps.down=true;
                    if(!isPressed){ 
                        fps.down = false;
                    }
                    break;
                case "Jump":
                    fps.playerControl.jump();
                    break;
            }
            
        }
    };
    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(listener, "Left");
        inputManager.addListener(listener, "Right");
        inputManager.addListener(listener, "Up");
        inputManager.addListener(listener, "Down");
        inputManager.addListener(listener, "Jump");
    }

    @Override
    public void simpleInitApp() {
        cam.setViewPort(0f, 1f, 0f, 1f);
        cam.setLocation(new Vector3f(3.32f, 4.48f, 4.28f));
        cam.setRotation(new Quaternion(-0.07f, 0.92f, -0.25f, -0.27f));
        setupCamera();
        flyCam.setMoveSpeed(50);//increasing camera speed
        cam.setLocation(new Vector3f(0,3,11));//setting the location of the camera
        fps = new fps_appstate();
        stateManager.attach(fps);
        setUpKeys();
        //inputs listening for the specific input
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(listener, "Shoot");
        
        inputManager.addMapping("Green", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addListener(listener, "Green");
        
        inputManager.addMapping("Red", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addListener(listener, "Red");
        
        inputManager.addMapping("Blue", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addListener(listener, "Blue");
        
        inputManager.addMapping("Blue", new KeyTrigger(KeyInput.KEY_F3));
        inputManager.addListener(listener, "Blue");
        
        displayHUD();
        soundOff();//method for adding background sound
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        hitcount.setText("Hit Count: "+ fps.hit_count);//updating the hit count
        score.setText("Score: "+ fps.score);
       // winLose();
        setupCamera();
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        
    }
}
