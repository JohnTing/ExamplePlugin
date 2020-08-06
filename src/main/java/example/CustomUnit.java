package example;

import java.lang.reflect.Field;

import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.content.Bullets;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.ctype.MappableContent;
import mindustry.entities.type.base.GroundUnit;
import mindustry.entities.type.base.MinerDrone;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

import static mindustry.Vars.*;

public class CustomUnit {

  public CustomUnit() {

  }

  public static UnitType dagger1() {

    UnitType dagger = new UnitType("draug", MinerDrone::new){{
      flying = true;
      drag = 0.01f;
      speed = 0.3f;
      maxVelocity = 1.2f;
      range = 50f;
      health = 80;
      minePower = 0.9f;
      engineSize = 1.8f;
      engineOffset = 5.7f;
      weapon = new Weapon("you have incurred my wrath. prepare to die."){{
          bullet = Bullets.lancerLaser;
      }};
  }};

  return dagger;
  }
}
