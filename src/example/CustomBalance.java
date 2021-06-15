package example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.yaml.snakeyaml.Yaml;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.game.EventType.UnitCreateEvent;
import mindustry.gen.Bullet;
import mindustry.gen.Player;
import mindustry.gen.Statusc;
import mindustry.mod.Plugin;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.Block;


public class CustomBalance {

    public static final String dir = "config/mods/CustomBalance/";
    public static final String errors = dir + "errors/";
    public static final String config = dir + "config.yaml";

    static public HashMap<UnitType, UnitSetting> defaultSetting = new HashMap<UnitType, UnitSetting>();
    static public HashMap<UnitType, UnitSetting> customSetting = new HashMap<UnitType, UnitSetting>();
    public Config cfg = new Config();

    public CustomBalance() {
        load();
    }

    public void load() {
        
        try{
            Config cfg = Util.load(config, Config.class);
            if(cfg == null) {
                return;
            }
            this.cfg = cfg;
            customSetting = cfg.parse();
            
            for (Entry<UnitType, UnitSetting> entry : customSetting.entrySet()) {
                entry.getValue().setUnitType(entry.getKey());
            }

        } catch(IOException e) {
            Log.info("failed to parse config file: " + e.getMessage());
        } catch(Exception e) {
            Log.info("failed to load settings: " + e.getMessage());
        }
    }



    // config represents game configuration
    public static class Config {
        
        public HashMap<String, UnitSetting> unitDef = new HashMap<String, UnitSetting>() {{
            put("dagger", UnitSetting.getUnitType(UnitTypes.dagger));
        }};
    
        @JsonIgnore
        public HashMap<UnitType, UnitSetting> parse() throws Exception {
            HashMap<UnitType, UnitSetting> newSetting = new HashMap<UnitType, UnitSetting>();
            
            for(Entry<String, UnitSetting> entry : unitDef.entrySet()) {
                try {
                    UnitType unitType = (UnitType)Util.getProp(UnitTypes.class, entry.getKey());
                    UnitSetting unitSetting = entry.getValue();
                    newSetting.put(unitType, unitSetting);
                    

                } catch(Exception e) {
                    throw new Exception("UnitType with name '" + entry.getKey() + "' is invalid: " + e.getMessage());
                }
            }
            return newSetting;
        }
    }
    


    public static class UnitWeaponSetting {
        public float damage = -1f;
        public float splashDamage = -1f;
        public float splashDamageRadius = -1f;
        public float buildingDamageMultiplier = -1f;
        public float ammoMultiplier = -1f;

        @Override
        public String toString() {
          StringBuilder sb = new StringBuilder();
          sb.append(getClass().getName());
          sb.append(": ");
          sb.append("\n");
          for (Field f : getClass().getDeclaredFields()) {
            sb.append(f.getName());
            sb.append("=");
            try {
                sb.append(f.get(this));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            sb.append(", ");
            sb.append("\n");
          }
          return sb.toString();
        }

    }
    // js UnitTypes.dagger.weapons.get(0).bullet.damage
    // js UnitTypes.mono.mineTier
    public static class UnitSetting {
        
        // set UnitType
        public float health = -1f;
        public float armor = -1f;
        public float speed = -1f;

        public float boostMultiplier = -1f;
        
        public float buildSpeed = -1f;
        public float mineSpeed = -1f;
        public int mineTier = -1;

        public int itemCapacity = -1;
        public int ammoCapacity = -1;
        public int commandLimit = -1;

        public float crashDamageMultiplier = -1f;

        public HashMap<Integer, UnitWeaponSetting> weapons = new HashMap<Integer, UnitWeaponSetting>();

        static public UnitSetting getUnitType(UnitType unitType) {

            UnitSetting dfSetting = new UnitSetting();

            dfSetting.health = unitType.health;
            dfSetting.armor = unitType.armor;
            dfSetting.speed = unitType.speed;

            dfSetting.boostMultiplier = unitType.boostMultiplier;

            dfSetting.buildSpeed = unitType.buildSpeed;
            dfSetting.mineSpeed = unitType.mineSpeed;
            dfSetting.mineTier = unitType.mineTier;

            dfSetting.itemCapacity = unitType.itemCapacity;
            dfSetting.ammoCapacity = unitType.ammoCapacity;
            dfSetting.commandLimit = unitType.commandLimit;

            dfSetting.crashDamageMultiplier = unitType.crashDamageMultiplier;

            for(int i = 0;i < unitType.weapons.size;i++) {
                BulletType bullet = unitType.weapons.get(i).bullet;
                dfSetting.weapons.put(i, new UnitWeaponSetting(){{
                    damage = bullet.damage;
                    splashDamage = bullet.splashDamage;
                    splashDamageRadius = bullet.splashDamageRadius;
                    buildingDamageMultiplier = bullet.buildingDamageMultiplier;
                    ammoMultiplier = bullet.ammoMultiplier;
                }});
            }
            return dfSetting;
        }

        public void setUnitType(UnitType unitType) {

            if(!defaultSetting.containsKey(unitType)) {
                defaultSetting.put(unitType, getUnitType(unitType));
            }
            if(health > 0) unitType.health = health;
            if(armor > 0) unitType.armor = armor;
            if(speed > 0) unitType.speed = speed;

            if(boostMultiplier > 0) unitType.boostMultiplier = boostMultiplier;

            if(buildSpeed > 0) unitType.buildSpeed = buildSpeed;
            if(mineSpeed > 0) unitType.mineSpeed = mineSpeed;
            if(mineTier > 0) unitType.mineTier = mineTier;

            if(itemCapacity > 0) unitType.itemCapacity = itemCapacity;
            if(ammoCapacity > 0) unitType.ammoCapacity = ammoCapacity;
            if(commandLimit > 0) unitType.commandLimit = commandLimit;
            
            if(crashDamageMultiplier > 0) unitType.crashDamageMultiplier = crashDamageMultiplier;
            
            for (Entry<Integer, UnitWeaponSetting> entry : weapons.entrySet()) {
                int index = entry.getKey();
                UnitWeaponSetting uwSetting = entry.getValue();

                if(index < unitType.weapons.size) {
                    if(uwSetting.damage > 0) 
                        unitType.weapons.get(index).bullet.damage = uwSetting.damage;

                    if(uwSetting.splashDamage > 0) 
                        unitType.weapons.get(index).bullet.splashDamage = uwSetting.splashDamage;

                    if(uwSetting.buildingDamageMultiplier > 0) 
                        unitType.weapons.get(index).bullet.buildingDamageMultiplier = uwSetting.buildingDamageMultiplier;

                    if(uwSetting.ammoMultiplier > 0) 
                        unitType.weapons.get(index).bullet.ammoMultiplier = uwSetting.ammoMultiplier;

                } else {
                    Log.info(unitType.name + "'s " + index + "th weapon does not exist. skip.");
                }
            }
        }

        static public void resetUnitType(UnitType unitType) {
            if(defaultSetting.containsKey(unitType)) {
                defaultSetting.get(unitType).setUnitType(unitType);
            }
        }

        @Override
        public String toString() {
          StringBuilder sb = new StringBuilder();
          sb.append(getClass().getName());
          sb.append(": ");
          sb.append("\n");
          for (Field f : getClass().getDeclaredFields()) {
            sb.append(f.getName());
            sb.append("=");
            try {
                sb.append(f.get(this).toString());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            sb.append(", ");
            sb.append("\n");
          }
          return sb.toString();
        }

    }
    // undone 
    public static class BlockSetting {
        // Block
        int  health = 360;
    }

    public static class Util {

        public static <T> Object getProp(Class<T> target, String prop) throws Exception {
            try{
                return target.getField(prop).get(null);
            }catch(NoSuchFieldException ex){
                throw new Exception(String.format(
                    "%s does not contain '%s', use 'content %s' to see the options",
                    target.getSimpleName(),
                    prop,
                    target.getSimpleName().toLowerCase().replace("type", "")
                ));
            }catch(IllegalAccessException ex){
                throw new RuntimeException(ex);
            }
        }

        public static <T> T load(String filename, Class<T> type) throws IOException{
            ObjectMapper mapper;

            mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();
            makeFullPath(filename);
            File f = new File(filename);
            if(!f.exists()){
                return save(filename, type, null);
            }

            return mapper.readValue(f, type);
        }

        public static <T> T save(String filename, Class<T> type, Object object) throws IOException{

            ObjectMapper mapper;
            mapper = new ObjectMapper(new YAMLFactory());

            makeFullPath(filename);
            File f = new File(filename);
            T obj;
            try{
                if (object == null) {
                    obj = type.getDeclaredConstructor().newInstance();
                } else {
                    obj = type.cast(object);
                }
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
            mapper.writeValue(f, obj);
            return obj;
        }
        
        public static void makeFullPath(String filename) throws IOException{
            File targetFile = new File(filename);
            File parent = targetFile.getParentFile();
            if(!parent.exists() && !parent.mkdirs()){
                throw new IOException("Couldn't create dir: " + parent);
            }
        }
    }
    /*
    public static class Logging {

        static Logger logger = Logger.getLogger("Logging");
        static SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd z");
        static SimpleDateFormat formatterTime = new SimpleDateFormat("[HH-mm-ss-SSS]");
        static String path = errors;
        static {
            try {
                Util.makeFullPath(path);
                FileHandler fileHandler = new FileHandler(errors + formatterDate.format(new Date()) + "%g.log");
                logger.addHandler(fileHandler);
            } catch (SecurityException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }            
        }

    }*/
}


