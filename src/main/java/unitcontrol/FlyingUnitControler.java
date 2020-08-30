package unitcontrol;

import arc.func.Cons2;
import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.IntArray;
import arc.util.Interval;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.entities.Effects;
import mindustry.entities.Units;
import mindustry.entities.traits.MoveTrait;
import mindustry.entities.traits.TargetTrait;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.Player;
import mindustry.entities.type.Unit;
import mindustry.entities.type.base.GroundUnit;
import mindustry.entities.units.UnitCommand;
import mindustry.entities.units.UnitState;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.world;

public class FlyingUnitControler extends UnitControler {

  UnitState rally = new UnitState() {
    public void entered() {
    }
    public void update() {

      if (unit.getType() == UnitTypes.ghoul) {
        if (player.isShooting()) {
          attack3();
        } else {
          circle();
        }
        return;
      } 

      if (player.isShooting()) {
        rallyAttack();
      } else {
        rallyCircle();
      }
    }
    public void exited() {
    }
  };
  UnitState attack = new UnitState() {
    public void entered() {
    }
    public void update() {

      if (unit.getType() == UnitTypes.ghoul) {
        if (player.isShooting()) {
          attack3();
        } else {
          circle();
        }
        return;
      } 

      if (player.isShooting()) {
        attack();
      } else {
        circle();
      }
    }
    public void exited() {
    }
  };

  UnitState retreat = new UnitState() {
    public void entered() {
    }
    public void update() {
      circle();
    }
    public void exited() {
    }
  };

  public FlyingUnitControler(BaseUnit unit, Player player) {
    set(unit, player);
    setCommand(UnitCommand.rally);
  }

  protected void dosomething() {
    if (player.isShooting()) {
      rally();
    } else {
      circle();
    }
  }
  
  protected void rally() {

    if (unit.getType() == UnitTypes.ghoul) {
      attack3();
    } else {
      rallyAttack();
    }
  }

  protected void rallyAttack() {
    rallyAttack(player.pointerX, player.pointerY, unit.getType().range);
  }

  protected void rallyAttack(float x, float y, float circleLength) {
    
    if(unit.dst(player) > unit.getType().range) {
      rallyCircle();
    }
    else {
      vec.set(x - unit.x, y - unit.y);
      vec.setLength(0.0001f);
      unit.velocity().set(vec);
    }
    unit.getWeapon().update(unit, x, y);
    /*
    vec.set(x - unit.x, y - unit.y);
    vec.setLength(0.0001f);
    unit.velocity().set(vec);
    unit.getWeapon().update(unit, x, y);*/
    /*
    if (player.dst(unit) > circleLength && vec.len() < player.dst(unit)) {
      vec.setLength(0.0001f);
      unit.velocity().set(vec);
    } else {
      vec.setLength(unit.getType().speed * Time.delta());
      unit.velocity().add(vec);
    }*/
    /*
    float angle = unit.angleTo(x, y);
    if (Math.abs(angle - unit.rotation) < 30f) {
      
    }*/
  }

  protected void rallyCircle() {
    rallyCircle(player.getX(), player.getY(), unit.getType().range);
  }
  protected void rallyCircle(float x, float y, float circleLength) {
    circle(x, y, circleLength);

    if(Units.invalidateTarget(target, unit.getTeam(),x , y)){
      target = null;
    }
    if(retarget()) {
      target = null;
      targetClosest();
    }
    if(target != null) {
      //float angle = unit.angleTo(target);
      unit.getWeapon().update(unit, target.getX(), target.getY());
    }
  }

  protected void attack() {
    attack(player.pointerX, player.pointerY, unit.getType().attackLength);
  }

  protected void attack(float x, float y, float circleLength) {

    vec.set(x - unit.x, y - unit.y);
    if (vec.len() < circleLength) {
      vec.setLength(0.0001f);
      unit.velocity().set(vec);
    } else {
      vec.setLength(unit.getType().speed * Time.delta());
      unit.velocity().add(vec);     
    }   

    float angle = unit.angleTo(x, y);
    if (Math.abs(angle - unit.rotation) < 30f) {
      unit.getWeapon().update(unit, x, y);
    }
  }

  protected void attack3() {
    attack3(player.pointerX, player.pointerY, 15f);
  }

  protected void attack3(float x, float y, float circleLength){
    vec.set(x - unit.x, y - unit.y);

    float ang = unit.angleTo(x, y);
    float diff = Angles.angleDist(ang, unit.rotation);

    if(diff > 100f && vec.len() < circleLength){
      vec.setAngle(unit.velocity().angle());
    }else{
      vec.setAngle(Mathf.slerpDelta(unit.velocity().angle(), vec.angle(), 0.44f));
    }

    vec.setLength(unit.getType().speed * Time.delta());

    unit.velocity().add(vec);
    if(unit.dst(x, y) < circleLength + 5f) {
      unit.getWeapon().update(unit, x, y);
    }
  }

  protected void circle() {
    circle(player.getX(), player.getY(), unit.getType().range);
  }
  
  protected void circle(float x, float y, float circleLength) {
    if (player == null)
      return;
  
    vec.set(x - unit.x, y - unit.y);

    if (vec.len() < circleLength) {
      vec.rotate((circleLength - vec.len()) / circleLength * 180f);
    }
    vec.setLength(unit.getType().speed * Time.delta());

    unit.velocity().add(vec);
  }

  public void setCommand(UnitCommand command) {
    unit.setState(command == UnitCommand.retreat ? retreat :
    command == UnitCommand.attack ? attack :
    command == UnitCommand.rally ? rally :
    null);
  }

  TargetTrait target = null;
  protected Interval timer = new Interval(5);
  public void targetClosest() {
    TargetTrait newTarget = Units.closestTarget(unit.getTeam(), unit.getX(), unit.getY(), Math.max(unit.getWeapon().bullet.range(), unit.getType().range), u -> unit.getType().targetAir || !u.isFlying());
    if(newTarget != null){
        target = newTarget;
    }
}

  public boolean retarget() {
    return (timer.get(0, 20));
  }
}
