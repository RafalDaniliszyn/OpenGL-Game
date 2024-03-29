package org.game;

import org.game.component.CollisionComponent;
import org.game.component.Component;
import org.game.component.PositionComponent;
import org.game.component.WindComponent;
import org.game.component.mesh.MeshComponent;
import org.game.component.mesh.MeshData;
import org.game.component.mesh.MeshManager;
import org.game.entity.Entity;
import org.game.entity.EntityProperties;
import org.game.entity.LightSourceEntity;
import org.game.entity.MultipleObjectsEntity;
import org.game.entity.PlayerEntity;
import org.game.entity.StaticObjectEntity;
import org.game.event.EquipmentEventManager;
import org.game.helper.MapHelper;
import org.game.helper.PositionHelper;
import org.game.component.mesh.texture.TextureManager;
import org.game.system.renderer.ShaderEnum;
import org.game.system.renderer.ShaderManager;
import org.game.ui.entity.EquipmentEntity;
import org.game.system.BaseSystem;
import org.game.system.CollisionSystem;
import org.game.system.GrowthSystem;
import org.game.system.UiSystem;
import org.game.system.MoveSystem;
import org.game.system.renderer.RenderSystem;
import org.game.event.EventManager;
import org.game.event.EventObserver;
import org.joml.Vector3f;

import java.util.*;

public class GameData {
    private final Map<Component, String> components = new HashMap<>();
    private final Map<Long, Entity> entities = new HashMap<>();
    private final Map<String, BaseSystem> systems = new HashMap<>();
    private final Map<String, EventManager> eventManagers = new HashMap<>();
    private final MeshManager meshManager;
    private final TextureManager textureManager;
    private float[] mapVert;
    private long playerId;
    private ShaderManager shaderManager;
    private final float[][] heightMap;
    private final Long skyId;
    private boolean active;

    public GameData() {
        active = false;
        textureManager = new TextureManager();
        meshManager = new MeshManager(textureManager);
        shaderManager = new ShaderManager();

        //prepareTestData();
        StaticObjectEntity groundMap = new StaticObjectEntity(meshManager, "baseMap3", new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), false);
        entities.put(groundMap.getId(), groundMap);
        groundMap.removeComponent(CollisionComponent.class);

        MeshData groundMeshData = meshManager.getMeshData("baseMap3");
        mapVert = groundMeshData.getVertices();

        heightMap = MapHelper.getHeightMap(mapVert);

        StaticObjectEntity sky = new StaticObjectEntity(meshManager, "background", new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), false);
        sky.removeComponent(CollisionComponent.class);
        skyId = sky.getId();
        entities.put(sky.getId(), sky);

        StaticObjectEntity oldHouse = new StaticObjectEntity(meshManager, "oldhouse", new Vector3f(2.0f, 0.01f, 4.0f),
                new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), false);
        entities.put(oldHouse.getId(), oldHouse);

        StaticObjectEntity palace = new StaticObjectEntity(meshManager, "palace", new Vector3f(15.0f, 0.0f, 14.0f),
                new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), false);
        entities.put(palace.getId(), palace);

        StaticObjectEntity water = new StaticObjectEntity(meshManager, "water", new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), false, new EntityProperties(ShaderEnum.WATER));
        entities.put(water.getId(), water);

        MultipleObjectsEntity grass = new MultipleObjectsEntity(mapVert, meshManager, "grass2",
                new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), 250, false, false);
        entities.put(grass.getId(), grass);


        MultipleObjectsEntity oak = new MultipleObjectsEntity(mapVert, meshManager, "tree2",
                new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), 700, false, true);
        entities.put(oak.getId(), oak);

        MultipleObjectsEntity stones = new MultipleObjectsEntity(mapVert, meshManager, "stones",
                new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), 30, false, true);
        entities.put(stones.getId(), stones);

        LightSourceEntity sun = new LightSourceEntity(meshManager, "sun", new Vector3f(-300.0f, 400.0f, 300.0f),
                new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), false, new Vector3f(1.0f, 1.0f, 1.0f));
        entities.put(sun.getId(), sun);

        EquipmentEntity equipmentEntity = new EquipmentEntity(meshManager, 1L, 100.0f, 200.0f);
        entities.put(equipmentEntity.getId(), equipmentEntity);

        EquipmentEntity equipmentEntity1 = new EquipmentEntity(meshManager, 2L, 300.0f, 200.0f);
        entities.put(equipmentEntity1.getId(), equipmentEntity1);

        //Equipment event manager
        eventManagers.put("equipmentEntity", equipmentEntity.getEventManager());
        eventManagers.put("equipmentEntity1", equipmentEntity1.getEventManager());

        PlayerEntity player = new PlayerEntity(meshManager, false);
        playerId = player.getId();
        entities.put(player.getId(), player);
        addEventManagerObserver(EquipmentEventManager.class, player);

        UiSystem uiSystem = new UiSystem(this);
        systems.put("interfaceSystem", uiSystem);
//        //Wind
//        WindSystem windSystem = new WindSystem(this);
//        systems.put("windSystem", windSystem);
        //Growth
        GrowthSystem growthSystem = new GrowthSystem(this);
        systems.put("growthSystem", growthSystem);
        //Move
        MoveSystem moveSystem = new MoveSystem(this);
        systems.put("moveSystem", moveSystem);
        //Collision
        CollisionSystem collisionSystem = new CollisionSystem(this);
        systems.put("collisionSystem", collisionSystem);
        //Render
        RenderSystem renderSystem = new RenderSystem(this);
        systems.put("renderSystem", renderSystem);
    }

    public void init() {
        systems.forEach((s, systems) -> {
            systems.init();
        });
    }

    public void update(float deltaTime) {
        if (!active) {
            return;
        }
        eventManagers.forEach((s, eventManager) -> {
            eventManager.notifyObservers();
        });
        systems.forEach((s, system) -> {
            system.update(deltaTime);
        });
    }

    public void delete() {
        systems.forEach((s, systems) -> {
            systems.delete();
        });
    }

    public Map<Component, String> getComponents() {
        return components;
    }

    @SafeVarargs
    public final Map<Long, Entity> getEntities(Class<? extends Component>... componentClass) {
        Map<Long, Entity> result = new HashMap<>();
        entities.forEach((id, entity)-> {
            boolean anyMatch = Arrays.stream(componentClass)
                    .allMatch(component -> entity.getComponentList().stream()
                            .anyMatch(component1 -> component.isAssignableFrom(component1.getClass())));
            if (anyMatch) {
                 result.put(entity.getId(), entity);
            }
        });
        return result;
    }

    public void addEventManagerObserver(Class<? extends EventManager> eventManager, EventObserver eventObserver) {
        eventManagers.forEach((s, manager) -> {
            if (manager.getClass().isAssignableFrom(eventManager)) {
                manager.addObserver(eventObserver);
            }
        });
    }

    public Entity getEntity(Long id) {
        return entities.get(id);
    }

    public MeshManager getMeshManager() {
        return meshManager;
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public float[] getMapVert() {
        return mapVert;
    }

    public float[][] getHeightMap() {
        return heightMap;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ShaderManager getShaderManager() {
        return shaderManager;
    }

    public void updateSkyPos(float x, float z) {
        Entity sky = getEntity(skyId);
        sky.getComponent(PositionComponent.class).getPosition().x -= x;
        sky.getComponent(PositionComponent.class).getPosition().z -= z;
    }

    private void prepareTestData() {
        MeshData groundMeshData = meshManager.getMeshData("baseMap3");
        mapVert = groundMeshData.getVertices();

        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                float scale = random.nextFloat();
                float rotationY = random.nextInt(360);
                int offset = random.nextInt(60);
                StaticObjectEntity grass = new StaticObjectEntity(mapVert, meshManager, "grass2", new Vector3f(((i))+offset, 0, ((j))+offset),
                        new Vector3f(0.0f, rotationY, 0.0f), new Vector3f(0.9f, 0.6f+scale, 0.9f), false);
                grass.getComponents(MeshComponent.class).forEach(mesh -> {
                    mesh.setCullFace(false);
                });
                grass.addComponent(new WindComponent());
                grass.removeComponent(CollisionComponent.class);
                entities.put(grass.getId(), grass);
            }
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 5; j++) {
                float scale = random.nextFloat();
                float rotationY = random.nextInt(360);
                int offset = random.nextInt(60);
                StaticObjectEntity oak = new StaticObjectEntity(mapVert, meshManager, "oak", new Vector3f(((i*50))+offset, 0, ((j*40))+offset),
                        new Vector3f(0.0f, rotationY, 0.0f), new Vector3f(0.8f+scale, 0.8f+scale, 0.8f+scale), false);
                oak.getComponents(MeshComponent.class).forEach(mesh -> {
                    mesh.setCullFace(false);
                });
                entities.put(oak.getId(), oak);
            }
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 5; j++) {
                float x = random.nextInt(500);
                float z = random.nextInt(500);
                float y = 0.0f;
                StaticObjectEntity tree2 = new StaticObjectEntity(mapVert, meshManager, "tree2", new Vector3f(x, y, z),
                        new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), false);
                entities.put(tree2.getId(), tree2);
            }
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                float x = random.nextInt(500);
                float z = random.nextInt(500);
                float y = PositionHelper.getPositionY(mapVert, x, z);
                StaticObjectEntity stone = new StaticObjectEntity(mapVert, meshManager, "stones", new Vector3f(x, y, z),
                        new Vector3f(0.0f, random.nextInt(360), 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), false);
                entities.put(stone.getId(), stone);
            }
        }

        StaticObjectEntity fence = new StaticObjectEntity(mapVert, meshManager, "fence", new Vector3f(2.0f, 0.0f, -4.0f),
                new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), false);
        entities.put(fence.getId(), fence);

        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                float x = random.nextInt(1000)-500;
                float z = random.nextInt(1000)-500;
                float y = PositionHelper.getPositionY(mapVert, x, -z);
                StaticObjectEntity flower = new StaticObjectEntity(mapVert, meshManager, "flower", new Vector3f(x, y, z),
                        new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), false);
                entities.put(flower.getId(), flower);
            }
        }
    }
}
