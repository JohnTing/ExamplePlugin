package unitcontrol;

import arc.func.Cons2;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.IntArray;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Effects;
import mindustry.entities.traits.MoveTrait;
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

public class GroundUnitControler extends UnitControler {
 
  UnitState rally = new UnitState(){
    public void entered() {
      if (target.isZero()) {
        target.set(player);
      }
    }
    public void update() {
      circle(target.getX(), target.getY(), 30f, true);
      
      if(player.isShooting()) {
        attack();
        target.set(player.pointerX, player.pointerY);
      }
    }
    public void exited() {
    }
  };

  UnitState attack = new UnitState(){
    public void entered() {
      if (target.isZero()) {
        target.set(player);
        
      }
    }
    public void update() {
      circle(target.getX(), target.getY(), 30f, true);
      
      if(player.isShooting()) {
        attack();
        target.set(player.pointerX, player.pointerY);
      }
    }
    public void exited() {
    }
  };

  UnitState retreat = new UnitState(){
    public void entered() {
    }
    public void update() {
    }
    public void exited() {
    }
  };

  public GroundUnitControler(BaseUnit unit, Player player) {
    set(unit, player);
    setCommand(UnitCommand.rally);
  }

  protected void dosomething() {
    if(player.isShooting()) {
      attack();
    } else {
      circle();
    }
  }

  protected void attack(){
    attack(player.pointerX, player.pointerY);
  }

  protected void attack(float x, float y){
    if(player == null) return;

    float angle = unit.angleTo(x, y);
    if(Math.abs(angle-unit.rotation) > 30f) {
      unit.rotate(angle);
    }
    else {
      unit.getWeapon().update(unit, x, y);
    }
  }

  protected void circle() {
    circle(player.getX(), player.getX(), 50f, true);
  }

  protected void circle(float x, float y, float circleLength, Boolean rotate){
    if(player == null) return;

    vec.set(x - unit.x, y - unit.y);
    if(circleLength < vec.len()) {
      vec.setLength(unit.getType().speed * Time.delta());
      unit.velocity().add(vec);
      if(rotate) {
        unit.rotate(vec.angle());
      }
    }
  }

  public void setCommand(UnitCommand command) {
    unit.setState(command == UnitCommand.retreat ? retreat :
    command == UnitCommand.attack ? attack :
    command == UnitCommand.rally ? rally :
    null);
  }
}
