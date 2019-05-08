package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.cinematic.Cinematic;
import com.jme3.collision.CollisionResults;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.StripBox;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.util.SkyFactory;
import com.jme3.water.SimpleWaterProcessor;

public class fps_appstate extends AbstractAppState {

    private SimpleApplication app;
    private BulletAppState bulletAppState;
    private final Node rootNode = new Node("Root node");
    private final Node spinningNode = new Node("Sphere Node");
    private final Node monsterNode = new Node("Monster Node");
    private final Node cylinderNode = new Node("cylinder Node");
    public Node playerNode = new Node("player Node");
    public BetterCharacterControl playerControl;
    public int hit_count = 0;
    public int score= 0;
    public Material d_mat;
    public boolean lose =  false;
    public ViewPort viewPort;
    Geometry c1_geom;
    Cinematic cinematic_mon;
    DirectionalLightShadowRenderer shadow;
    Vector3f walkDirection = new Vector3f(0,0,0);
    Vector3f location = new Vector3f(0,0,0);
    Vector3f camDir = new Vector3f(0, 0, 0);
    Vector3f camLeft = new Vector3f(0, 0, 0);
    boolean left = false;
    boolean right = false;
    boolean up = false;
    boolean down = false;


    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        viewPort = app.getRenderManager().createMainView("vp",app.getCamera());
        viewPort.attachScene(rootNode);
        viewPort.attachScene(spinningNode);
        viewPort.attachScene(monsterNode);
        bulletAppState = new BulletAppState();
        //bulletAppState.setDebugEnabled(true);
        playerControl = new BetterCharacterControl(1.5f, 6f, 1f);
        stateManager.attach(bulletAppState);
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        d_mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        //createFloor();
        createTerrain();
        createSkyBox();
        createWater();
        createObjects();
        createPhysicalPlayer();
        createCollisionObjects();
        createlightandShadow();
        createMonster();
        this.app.getRootNode().attachChild(rootNode);
    }

    @Override
    public void update(float tpf) {
        spinningNode.rotate(0, .2f * FastMath.DEG_TO_RAD, 0);
        shadow = new DirectionalLightShadowRenderer(app.getAssetManager(), 512, 2);
        //monsterNode.lookAt(app.getCamera().getLocation(), new Vector3f(0,1,0));
        playerUpdate();
        if(score >= 100){
            removeMonster();
        }else if(score < 0){
            lose = true;
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.app.getRootNode().detachChild(rootNode);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            this.app.getRootNode().attachChild(rootNode);
        } else {
            this.app.getRootNode().detachChild(rootNode);
        }
    }

    public void playerUpdate() {
        camDir.normalizeLocal();
        camLeft.normalizeLocal();
        float walkSpeed = 15;
        camDir.set(app.getCamera().getDirection());
        camLeft.set(app.getCamera().getLeft());
        camDir.y = 0;
        camLeft.y = 0;
        walkDirection.set(0, 0, 0);
        if (left) {walkDirection.addLocal(camLeft); }
        if (right) {walkDirection.addLocal(camLeft.negate());}
        if (up) {walkDirection.addLocal(camDir); }
        if (down) {walkDirection.addLocal(camDir.negate()); }
        playerControl.setWalkDirection(walkDirection.mult(walkSpeed));
        location.set(playerNode.getLocalTranslation());
        System.out.println(playerNode.getLocalTranslation());
        app.getCamera().setLocation(location.add(0,3.5f,0));
    }
    public void createlightandShadow() {
        rootNode.setShadowMode(ShadowMode.CastAndReceive);
        spinningNode.setShadowMode(ShadowMode.CastAndReceive);
        monsterNode.setShadowMode(ShadowMode.CastAndReceive);
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.Gray);
        rootNode.addLight(al);
        DirectionalLight sun = new DirectionalLight(new Vector3f(-50,-25,-50));
        shadow = new DirectionalLightShadowRenderer(app.getAssetManager(), 512, 2);
        shadow.setLight(sun);
        viewPort.addProcessor(shadow);
        rootNode.addLight(sun);
    }

    public void displayHit(Vector3f location, Geometry geom) {
        Sphere hitmarker = new Sphere(10, 10, .1f);//creating a small pink hitmarker sphere
        Geometry hitmarker_geom = new Geometry("Hit", hitmarker);
        Material hitmarker_mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        hitmarker_mat.setColor("Color", ColorRGBA.Pink);
        hitmarker_geom.setMaterial(hitmarker_mat);
        hitmarker_geom.setLocalTranslation(location);
        if(!geom.getName().equals("Hit")){
            if(spinningNode.getChildren().contains(geom)) {
                spinningNode.attachChild(hitmarker_geom);
                score+=5;
                hit_count++;
            }else if (monsterNode.getChildren().contains(geom)) {
                monsterNode.attachChild(hitmarker_geom);
                score+=10;
                hit_count++;
            }else if(cylinderNode.getChildren().contains(geom)){
                score+=5;
                cylinderNode.attachChild(hitmarker_geom);
                hit_count++;
            }else if (rootNode.getChildren().contains(geom)){
                if(geom.getName().equals("Dome")){
                    lose = true;
                }
                score-=2;
                rootNode.attachChild(hitmarker_geom);
                hit_count++;
            }
            RigidBodyControl ball_phy = new RigidBodyControl(1f);
            hitmarker_geom.addControl(ball_phy);
            bulletAppState.getPhysicsSpace().add(ball_phy);
            ball_phy.setLinearVelocity(app.getCamera().getDirection().mult(25));
        }
    }

    public void collision_detection() {
        CollisionResults results = new CollisionResults();//getting new collison results each shot
        Ray ray = new Ray(app.getCamera().getLocation(), app.getCamera().getDirection());//creating ray from middle of cams frustrum
        rootNode.collideWith(ray, results);//built in, detects each collison in rootNode
        for (int i = 0; i < results.size(); i++) {
            float dist = results.getCollision(i).getDistance();
            Vector3f pt = results.getCollision(i).getContactPoint();
            String geom = results.getCollision(i).getGeometry().getName();
            System.out.println("Selection #" + (i + 1) + ": " + geom + " at " + pt + ", " + dist + " WU away.");
        }
        if (results.size() > 0) {//checking if it hit anything
            Geometry collison = results.getClosestCollision().getGeometry();
            Vector3f vector = new Vector3f();
            results.getClosestCollision().getGeometry().getParent().worldToLocal(
                    results.getClosestCollision().getContactPoint(), vector);
            displayHit(vector, collison);//draws the hitmarker sphere where hit
        }
    }
    
    private void createFloor() {
        Quad floor = new Quad(50f, 50f);//crating new quad to make a 2d floor space
        Geometry floor_geom = new Geometry("Floor", floor);
        //Material f_mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        Material f_mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        f_mat.setTexture("DiffuseMap", app.getAssetManager().loadTexture("Textures/Terrain/Rocky/RockyTexture.jpg"));
        f_mat.setTexture("NormalMap", app.getAssetManager().loadTexture("Textures/Terrain/Rocky/RockyNormals.jpg"));
        floor_geom.setMaterial(f_mat);//setting the color and whatnot for the geometry
        floor_geom.setLocalTranslation(-25, 0, 25);
        floor_geom.getLocalRotation().fromAngles(-3.14f / 2, 0, 0);
        rootNode.attachChild(floor_geom);//attaching to the node
    }
    
    public void createPhysicalPlayer(){
        app.getRootNode().attachChild(playerNode);
        playerControl.setJumpForce(new Vector3f(0,15,0));
        playerNode.addControl(playerControl);
        bulletAppState.getPhysicsSpace().add(playerControl);
        playerControl.warp(new Vector3f(0,15,0));
        
    }
    
    private void createCollisionObjects() {
        makeCollisonBox();
        //makeCollisionFloor();
        
    }
        
    private void makeCollisonBox() {
        StripBox b = new StripBox(1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", app.getAssetManager().loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
        mat.setColor("Ambient", ColorRGBA.Blue);
        mat.setColor("Diffuse", ColorRGBA.Blue);
        geom.setMaterial(mat);
        geom.setLocalTranslation(-10, 1, 4);
        rootNode.attachChild(geom);
        RigidBodyControl b_phys = new RigidBodyControl(2f);
        geom.addControl(b_phys);
        bulletAppState.getPhysicsSpace().add(b_phys);
    }
    
    private void makeCollisionFloor() {
        Quad floor = new Quad(50f, 50f);//crating new quad to make a 2d floor space
        Geometry floor_geom = new Geometry("Floor", floor);
        //Material f_mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        Material f_mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        f_mat.setTexture("DiffuseMap", app.getAssetManager().loadTexture("Textures/Terrain/Rocky/RockyTexture.jpg"));
        f_mat.setTexture("NormalMap", app.getAssetManager().loadTexture("Textures/Terrain/Rocky/RockyNormals.jpg"));
        floor_geom.setMaterial(f_mat);//setting the color and whatnot for the geometry
        floor_geom.setLocalTranslation(-25, 0, 25);
        floor_geom.getLocalRotation().fromAngles(-3.14f / 2, 0, 0);
        rootNode.attachChild(floor_geom);//attaching to the node
        RigidBodyControl floor_phys = new RigidBodyControl(0);
        floor_geom.addControl(floor_phys);
        bulletAppState.getPhysicsSpace().add(floor_phys);
    }

    private void createObjects() {
        //creates each sperate object and location, then color and attached it to the node
        makeBox();
        makeDome();
        makeCylinder();
        makeSphere();
    }

    private void makeBox() {
        StripBox b = new StripBox(1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", app.getAssetManager().loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
        mat.setColor("Ambient", ColorRGBA.Blue);
        mat.setColor("Diffuse", ColorRGBA.Blue);
        geom.setMaterial(mat);
        geom.setLocalTranslation(-5, 1, -2);
        rootNode.attachChild(geom);
    }

    private void makeDome() {
        Dome d = new Dome(new Vector3f(10, 0, 0), 20, 20, 5f, false);
        Geometry d_geom = new Geometry("Dome", d);
        d_mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        d_mat.setBoolean("UseMaterialColors", true);
        d_mat.setColor("Ambient", ColorRGBA.Orange);
        d_mat.setColor("Diffuse", ColorRGBA.Orange);
        d_geom.setMaterial(d_mat);
        rootNode.attachChild(d_geom);
    }

    private void makeCylinder() {
        Cylinder c = new Cylinder(20, 20, 2, 5, true);
        c1_geom = new Geometry("Cylinder", c);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", app.getAssetManager().loadTexture("Textures/Terrain/Pond/Pond.jpg"));
        c1_geom.setMaterial(mat);
        c1_geom.setLocalTranslation(-18, 2, -15);
        cylinderNode.attachChild(c1_geom);
        rootNode.attachChild(cylinderNode);
    }

    private void makeSphere() {
        Sphere s = new Sphere(30, 30, 2f);
        Geometry sn_geom = new Geometry("Target_sphere", s);
        Material s_mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        s_mat.setFloat("Shininess", 100f);
        s_mat.setBoolean("UseMaterialColors", true);
        s_mat.setColor("Specular", ColorRGBA.White);
        s_mat.setColor("Diffuse", ColorRGBA.White);
        sn_geom.setMaterial(s_mat);
        sn_geom.setLocalTranslation(0, 5, 0);
        spinningNode.attachChild(sn_geom);//attaching sphere to its own spinning node then attaching that to the rootNode
        rootNode.attachChild(spinningNode);
    }
    
    private void createMonster(){
        makeHead();//head made of dome object
        makeArms();//two arms made of long cylinders
        makeTorso();//torso made of rectangle
        makelegs();//two legs made of long cylinders
        makeButt();//big butt made of sphere
        makehorn();//gig unicorn horn made of a cylinder
        monsterNode.setLocalTranslation(new Vector3f (0,-12,19));
        monsterNode.lookAt(new Vector3f(0,-10,-20), new Vector3f(0,1,0));
        rootNode.attachChild(monsterNode);//attaching monster node to root
    }
    private void makeHead() {
        Dome d = new Dome(new Vector3f(0, 0, 0), 20, 20, .5f, false);
        Geometry head_geom = new Geometry("Dome", d);
        Material head_mat = app.getAssetManager().loadMaterial("Materials/red_matte.j3m");
        head_mat.setBoolean("UseMaterialColors", true);
        head_mat.setColor("Specular", ColorRGBA.White);
        head_mat.setColor("Diffuse", ColorRGBA.White);
        head_geom.setMaterial(head_mat);
        head_geom.setLocalTranslation(0, 16, 0);
        monsterNode.attachChild(head_geom);
    }
    private void makeArms() {
        Cylinder c = new Cylinder(20, 20, .2f, 1.5f, true);
        Geometry c_geom = new Geometry("Cylinder", c);
        Material c_mat =  app.getAssetManager().loadMaterial("Materials/red_matte.j3m");
        c_mat.setBoolean("UseMaterialColors", true);
        c_mat.setColor("Specular", ColorRGBA.White);
        c_mat.setColor("Diffuse", ColorRGBA.White);
        c_geom.setMaterial(c_mat);
        c_geom.setLocalTranslation(-.5f, 15.5f, .75f);
        monsterNode.attachChild(c_geom);
        Cylinder c2 = new Cylinder(20, 20, .2f, 1.5f, true);
        Geometry c2_geom = new Geometry("Cylinder", c2);
        Material c2_mat =  app.getAssetManager().loadMaterial("Materials/red_matte.j3m");
        c2_mat.setBoolean("UseMaterialColors", true);
        c2_mat.setColor("Specular", ColorRGBA.White);
        c2_mat.setColor("Diffuse", ColorRGBA.White);
        c2_geom.setMaterial(c2_mat);
        c2_geom.setLocalTranslation(.5f, 15.5f, .75f);
        monsterNode.attachChild(c2_geom);
    }
    private void makeTorso() {
        StripBox b = new StripBox(.3f, 1, .1f);
        Geometry geom = new Geometry("Box", b);
        Material mat =  app.getAssetManager().loadMaterial("Materials/red_matte.j3m");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Specular", ColorRGBA.White);
        mat.setColor("Diffuse", ColorRGBA.White);
        geom.setMaterial(mat);
        geom.setLocalTranslation(0, 15, 0);
        monsterNode.attachChild(geom);
    }
    private void makelegs() {
        Cylinder c = new Cylinder(20, 20, .2f, 1.5f, true);
        Geometry c_geom = new Geometry("Cylinder", c);
        Material c_mat =  app.getAssetManager().loadMaterial("Materials/red_matte.j3m");
        c_mat.setBoolean("UseMaterialColors", true);
        c_mat.setColor("Specular", ColorRGBA.White);
        c_mat.setColor("Diffuse", ColorRGBA.White);
        c_geom.setMaterial(c_mat);
        c_geom.setLocalTranslation(-.3f, 13.2f, 0);
        c_geom.lookAt(new Vector3f(0, -50f, 0), new Vector3f(0f,1f,0f));
        monsterNode.attachChild(c_geom);
        Cylinder c2 = new Cylinder(20, 20, .2f, 1.5f, true);
        Geometry c2_geom = new Geometry("Cylinder", c2);
        Material c2_mat =  app.getAssetManager().loadMaterial("Materials/red_matte.j3m");
        c2_mat.setBoolean("UseMaterialColors", true);
        c2_mat.setColor("Specular", ColorRGBA.White);
        c2_mat.setColor("Diffuse", ColorRGBA.White);
        c2_geom.setMaterial(c2_mat);
        c2_geom.setLocalTranslation(.3f, 13.2f, 0);
        c2_geom.lookAt(new Vector3f(0, -50f, 0), new Vector3f(0f,1f,0f));
        monsterNode.attachChild(c2_geom);
    }
    private void makeButt() {
        Sphere s = new Sphere(30, 30, 1.3f);
        Geometry s_geom = new Geometry("Butt1", s);
        Material s_mat =  app.getAssetManager().loadMaterial("Materials/red_matte.j3m");
        s_mat.setBoolean("UseMaterialColors", true);
        s_mat.setColor("Specular", ColorRGBA.White);
        s_mat.setColor("Diffuse", ColorRGBA.White);
        s_geom.setMaterial(s_mat);
        s_geom.setLocalTranslation(0, 14, -.95f);
        monsterNode.attachChild(s_geom);//attaching sphere to its own spinning node then attaching that to the rootNode
        Sphere s2 = new Sphere(30, 30, .8f);
        Geometry s2_geom = new Geometry("Butt2", s2);
        Material s2_mat =  app.getAssetManager().loadMaterial("Materials/red_matte.j3m");
        s2_mat.setBoolean("UseMaterialColors", true);
        s2_mat.setColor("Specular", ColorRGBA.White);
        s2_mat.setColor("Diffuse", ColorRGBA.White);
        s2_geom.setMaterial(s2_mat);
        s2_geom.setLocalTranslation(0, 14, -2.2f);
        monsterNode.attachChild(s2_geom);
    }
    private void makehorn() {
        Cylinder c = new Cylinder(20, 20, .05f, .8f, true);
        Geometry c_geom = new Geometry("Cylinder", c);
        Material c_mat =  app.getAssetManager().loadMaterial("Materials/red_matte.j3m");
        c_mat.setBoolean("UseMaterialColors", true);
        c_mat.setColor("Specular", ColorRGBA.White);
        c_mat.setColor("Diffuse", ColorRGBA.White);
        c_geom.setMaterial(c_mat);
        c_geom.setLocalTranslation(0, 16.5f, .4f);
        c_geom.lookAt(new Vector3f(0, 22f, 5f), new Vector3f(0f,1f,0f));
        monsterNode.attachChild(c_geom);
    }
    private void removeMonster(){
        cinematic_mon.stop();
        monsterNode.setLocalTranslation(new Vector3f (0,-250,0));
    }

    private void createTerrain(){
        Spatial terra = app.getAssetManager().loadModel("Scenes/Terrain.j3o");
        terra.setLocalTranslation(0, -30, 50);
        rootNode.attachChild(terra);
        RigidBodyControl floor_phys = new RigidBodyControl(0);
        terra.addControl(floor_phys);
        bulletAppState.getPhysicsSpace().add(floor_phys);
        
    }
    private void createSkyBox(){
        rootNode.attachChild(SkyFactory.createSky(app.getAssetManager(),
                "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
    }
    private void createWater(){
        SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(app.getAssetManager());
        waterProcessor.setReflectionScene(app.getRootNode());
        Vector3f waterLocation=new Vector3f(0,-6,0);
        waterProcessor.setPlane(new Plane(Vector3f.UNIT_Y, waterLocation.dot(Vector3f.UNIT_Y)));
        viewPort.addProcessor(waterProcessor);
        waterProcessor.setWaterDepth(40);
        waterProcessor.setDistortionScale(0.05f);
        waterProcessor.setWaveSpeed(0.05f); 
        Quad quad = new Quad(400,400);
        quad.scaleTextureCoordinates(new Vector2f(6f,6f));
        Geometry water = new Geometry("water", quad);
        water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        water.setLocalTranslation(-200, -6, 250);
        water.setShadowMode(ShadowMode.Receive);
        water.setMaterial(waterProcessor.getMaterial());
        rootNode.attachChild(water);
    }
}
