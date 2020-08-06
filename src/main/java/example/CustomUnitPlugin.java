package example;

import java.util.HashMap;
import java.util.Map;

import arc.Events;
import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Time;
import arc.util.Timer.Task;
import mindustry.content.Fx;
import mindustry.Vars;
import mindustry.ai.Pathfinder.PathTarget;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.entities.Effects;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.Player;
import mindustry.entities.type.base.GroundUnit;
import mindustry.entities.units.UnitState;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.EventType.TapEvent;
import mindustry.gen.*;
import mindustry.plugin.Plugin;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Weapon;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.BurstTurret;

import mindustry.type.UnitType;
import static mindustry.Vars.*;


public class CustomUnitPlugin extends Plugin {


  BaseUnit cbaseUnit;


  public CustomUnitPlugin() {

    Events.on(TapEvent.class, event -> {
      Tile tile = event.tile;
      Player player = event.player;
      


      Call.sendMessage(tile.x + ", " + tile.y);    
      Call.sendMessage(tile.getX() + ", " + tile.getY());    
      Call.sendMessage(player.x + ", " + player.y);    
      Call.sendMessage("" + Vars.unitGroup.size()); 

      Units.nearby(player.getTeam(), tile.getX(), tile.getY(), 60f, unit -> {

        if(unit != null) {
          Call.onEffectReliable(Fx.healWave, unit.x, unit.y, 0, Color.white);
          if(unit instanceof BaseUnit) {
            BaseUnit baseUnit = (BaseUnit)unit;
            baseUnit.setState(new CustomUnitState(baseUnit, player));
          }
          //unit.setState(new CustomUnitState(cbaseUnit, player));
        }
        Call.sendMessage("set");  
      });

    });


    /*
    for(BaseUnit unit : Vars.unitGroup.intersect((float)event.tile.x, (float)event.tile.y, 10, 10)) {
      unit.setState(new CustomUnitState(cbaseUnit, player));
      Call.sendMessage("set");  
    }

    });*/
  }

  public void createUnit(Player player) {
    BaseUnit baseUnit = UnitTypes.chaosArray.create(player.getTeam());
    baseUnit.set(player.x, player.y);
    baseUnit.add();
    baseUnit.setState(CustomUnitState.nullState);
    Call.sendMessage(baseUnit.getStartState().toString());    


    cbaseUnit = baseUnit;

  }


  // register commands that player can invoke in-game
  @Override
  public void registerClientCommands(CommandHandler handler) {

    // register a simple reply command
    
    //register a simple reply command
    handler.<Player>register("test", "<text...>", "A simple ping command that test.", (args, player) -> {

      createUnit(player);

    });

    handler.<Player>register("re", "<text...>", "A simple ping command that test.", (args, player) -> {

      cbaseUnit.setState(new CustomUnitState(cbaseUnit, player));
      
    });

    //register a simple reply command
    handler.<Player>register("reply", "<text...>", "A simple ping command that echoes a player's text.", (args, player) -> {
        player.sendMessage("You said: [accent] " + args[0]);
    });

    //register a whisper command which can be used to send other players messages
    handler.<Player>register("whisper", "<player> <text...>", "Whisper text to another player.", (args, player) -> {
      //find player by name
      Player other = Vars.playerGroup.find(p -> p.name.equalsIgnoreCase(args[0]));

      //give error message with scarlet-colored text if player isn't found
      if(other == null){
          player.sendMessage("[scarlet]No player by that name found!");
          return;
      }

      //send the other player a message, using [lightgray] for gray text color and [] to reset color
      other.sendMessage("[lightgray](whisper) " + player.name + ":[] " + args[1]);
    });
  }
}
