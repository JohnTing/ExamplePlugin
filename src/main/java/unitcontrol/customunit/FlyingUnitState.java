package customunit;

import arc.func.Cons2;
import arc.graphics.Color;
import arc.math.Angles;
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
import mindustry.entities.units.UnitState;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.world;

public class FlyingUnitState extends CustomUnitState {
  public Vec2 vec = new Vec2();
  public FlyingUnitState(BaseUnit unit, Player player) {
    set(unit, player);
  }

  public void entered() {
  }

  public void exited() {
  }

  public void update() {
    if(player == null || player.isDead() || unit == null || unit.isDead()) {
      return;
    }
    
    if(player.isShooting()) {
      attack(100f);
    }else {
      circle(100f);
    }
  }

  public void set(BaseUnit unit, Player player){
    this.unit = unit;
    this.player = player;
  }

  protected void attack(float circleLength){
    vec.set(player.pointerX - unit.x, player.pointerY - unit.y);

    float ang = unit.angleTo(player.pointerX, player.pointerY);
    float diff = Angles.angleDist(ang, unit.rotation);

    if(diff > 100f && vec.len() < circleLength){
      vec.setAngle(unit.velocity().angle());
    }else{
      vec.setAngle(Mathf.slerpDelta( unit.velocity().angle(), vec.angle(), 0.44f));
    }
    vec.setLength(unit.getType().speed * Time.delta());
   
    unit.velocity().add(vec);

    float angle = unit.angleTo(player.pointerX, player.pointerY);
    if(Math.abs(angle-unit.rotation) < 30f) {
      unit.getWeapon().update(unit, player.pointerX, player.pointerY);
    }
}

  public void circle(float circleLength){
    if(player == null) return;

    vec.set(player.getX() - unit.x, player.getY() - unit.y);

    if(vec.len() < circleLength){
      vec.rotate((circleLength - vec.len()) / circleLength * 180f);
    }

    vec.setLength(unit.getType().speed * Time.delta());

    unit.velocity().add(vec);
  }
}
