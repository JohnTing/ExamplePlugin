package customunit;

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
import mindustry.entities.units.UnitState;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.world;

public class GroundUnitState extends CustomUnitState {

  public GroundUnitState(BaseUnit unit, Player player) {
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
    
    circle(100f);
    if(player.isShooting()) {
      attack();
    }
    else {
      unit.rotate(unit.angleTo(player));
    }
  }

  public void set(BaseUnit unit, Player player){
    this.unit = unit;
    this.player = player;
  }

  protected void attack(){
    if(player == null) return;

    float angle = unit.angleTo(player.pointerX, player.pointerY);
    if(Math.abs(angle-unit.rotation) > 30f) {
      unit.rotate(angle);
    }
    else {
      unit.getWeapon().update(unit, player.pointerX, player.pointerY);
    }
  }


  protected void circle(float circleLength){
    if(player == null) return;

    vec.set(player.getX() - unit.x, player.getY() - unit.y);
    if(circleLength < vec.len()) {
      vec.setLength(unit.getType().speed * Time.delta());
      unit.velocity().add(vec);
    }
  }
}
