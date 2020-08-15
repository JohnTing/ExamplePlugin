package unitcontrol;

import arc.func.Cons2;
import arc.graphics.Color;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.IntArray;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Effects;
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

public abstract class UnitControler {

  protected Vec2 vec = new Vec2();
  public BaseUnit unit;
  public Player player;
  public Vec2 target = new Vec2();

  public Vec2 getTarget() {
    return this.target;
  }
  
  UnitState rally;
  UnitState attack;
  UnitState retreat;

  abstract public void setCommand(UnitCommand command);


  public BaseUnit getUnit() {
    return this.unit;
  }

  public void setUnit(BaseUnit unit) {
    this.unit = unit;
  }

  public Player getPlayer() {
    return this.player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public void set(BaseUnit unit, Player player) {
    this.unit = unit;
    this.player = player;
  }
}
