package example;

import arc.graphics.Color;
import arc.math.geom.Position;
import mindustry.content.Fx;
import mindustry.entities.Effects;
import mindustry.entities.traits.MoveTrait;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.Player;
import mindustry.entities.type.base.GroundUnit;
import mindustry.entities.units.UnitState;
import mindustry.gen.Call;

public class CustomUnitState implements UnitState {
  static UnitState nullState = new UnitState() {
    public void entered() {
    }
    public void update() {
    }
    public void exited(){
    }
  };

  BaseUnit unit;
  Player player;
  int effectTime;

  public CustomUnitState(BaseUnit unit, Player player) {
    set(unit, player);
  }

  public void entered() {

  }

  public void exited(){

  }

  public void update(){
    effectTime = effectTime < 100 ? effectTime+1 : 0;
    if(effectTime == 0) {
      Call.onEffectReliable(Fx.fire, unit.x, unit.y, 0, Color.white);
    }
    if(unit.dst(player.pointerX, player.pointerY) > 50f) {
      unit.velocity().set(GroundUnit.maxAbsVelocity, 0);
      unit.velocity().setAngle(unit.lastPosition().angleTo(player.pointerX, player.pointerY));
      unit.rotate(unit.angleTo(player.pointerX, player.pointerY));
    }
  }

  public void set(BaseUnit unit, Player player){
    this.unit = unit;
    this.player = player;
  }
}
