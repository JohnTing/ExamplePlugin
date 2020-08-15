package customunit;

import arc.func.Cons2;
import arc.graphics.Color;
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

public abstract class CustomUnitState implements UnitState {

  public Vec2 vec = new Vec2();
  public BaseUnit unit;
  public Player player;

  public void entered() {
  }

  public void exited() {
  }

  public void update() {
    unit.velocity().add(vec.trns(unit.angleTo(player), unit.getType().speed * Time.delta()));
  }

  public void set(BaseUnit unit, Player player){
    this.unit = unit;
    this.player = player;
  }
}
