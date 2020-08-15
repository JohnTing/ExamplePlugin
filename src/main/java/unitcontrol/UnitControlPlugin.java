package unitcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import arc.Events;
import arc.graphics.Color;
import arc.util.CommandHandler;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.entities.Effects;
import mindustry.entities.Units;
import mindustry.entities.traits.TargetTrait;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.Player;
import mindustry.entities.type.Unit;
import mindustry.entities.type.base.FlyingUnit;
import mindustry.entities.type.base.GroundUnit;
import mindustry.entities.type.base.HoverUnit;
import mindustry.entities.units.UnitCommand;
import mindustry.game.Team;
import mindustry.game.EventType.BlockDestroyEvent;
import mindustry.game.EventType.TapConfigEvent;
import mindustry.game.EventType.TapEvent;
import mindustry.gen.Call;
import mindustry.plugin.Plugin;
import mindustry.world.Tile;
import mindustry.world.blocks.units.CommandCenter.CommandCenterEntity;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.indexer;
public class UnitControlPlugin extends Plugin {

  // register event handlers and create variables in the constructor
  public UnitControlPlugin() {

    /*
    Map<Player, Unit> playerMap = new HashMap<Player, Unit>();
    Map<Unit, UnitControler> unitMap = new HashMap<Unit, UnitControler>();
*/
    Map<Player, Map<Unit, UnitControler>> controlMap = new HashMap<Player, Map<Unit, UnitControler>>();
    Map<Player, UnitCommand> lastCommand = new HashMap<Player, UnitCommand>();


    Events.on(TapConfigEvent.class, event -> {
      Player player = event.player;
      Tile tile = event.tile;
      int value = event.value;

      if(tile != null && tile.block() == Blocks.commandCenter) {
        UnitCommand command = UnitCommand.attack;
        if (value < UnitCommand.all.length) {
          command = UnitCommand.all[value];
        }
        lastCommand.put(player, command);

        Map<Unit, UnitControler> unitMap = controlMap.get(player);
        if(unitMap != null) {
          if (command == UnitCommand.retreat) {
            unitMap.clear();
          } else {
            List<Unit> delKey = new ArrayList<Unit>();
            for (Map.Entry<Unit, UnitControler> entry : unitMap.entrySet()) {
              if (entry.getKey().isDead()) {
                delKey.add(entry.getKey());
              } else {
                entry.getValue().setCommand(command);
              }
            }
            for (Unit unit : delKey) {
              unitMap.remove(unit);
            }
          }
        }
      }
    });

    Events.on(TapEvent.class, e -> {
      Player player = e.player;
      Tile tile = e.tile;

      UnitCommand command = UnitCommand.rally;
      
      if(lastCommand.get(player) != null) {
        command = lastCommand.get(player);
      }

      if(command == UnitCommand.retreat) {
        return;
      }

      Unit seleteedUnit = Units.closest(player.getTeam(), tile.drawx(), tile.drawy(), 40f, u -> !u.isDead());
      
      if(seleteedUnit instanceof BaseUnit) {
        BaseUnit unit = (BaseUnit)seleteedUnit;

        Call.sendMessage("selete");
        Map<Unit, UnitControler> unitMap = controlMap.get(player);

        if (unitMap == null) {
          unitMap = new HashMap<Unit, UnitControler>();
          controlMap.put(player, unitMap);
        }
        if (!unitMap.containsKey(unit)) {
          UnitControler controler = null;
          if (unit instanceof GroundUnit) {
            controler = new GroundUnitControler(unit, player);
          }
          else if (unit instanceof HoverUnit) {
            controler = new FlyingUnitControler(unit, player);
          }
          else if (unit instanceof FlyingUnit) {
            controler = new FlyingUnitControler(unit, player);
          }
          if(controler != null) {
            unitMap.put(unit, controler);
            Call.onEffect(Fx.dooropenlarge, unit.getX(), unit.getY(), 0, Color.blue);
            controler.setCommand(command);
          }
        } 
      }
    });
  }

  public boolean isCommanded() {
    Team team = Team.sharded;
    return indexer.getAllied(team, BlockFlag.comandCenter).size != 0 && indexer.getAllied(team, BlockFlag.comandCenter).first().entity instanceof CommandCenterEntity;
  }

  // register commands that player can invoke in-game
  @Override
  public void registerClientCommands(CommandHandler handler) {

    // register a simple reply command
    handler.<Player>register("reply", "<text...>", "A simple ping command that echoes a player's text.",
        (args, player) -> {
          player.sendMessage("You said: [accent] " + args[0]);
        });

    // register a whisper command which can be used to send other players messages
    handler.<Player>register("whisper", "<player> <text...>", "Whisper text to another player.", (args, player) -> {
      // find player by name
      Player other = Vars.playerGroup.find(p -> p.name.equalsIgnoreCase(args[0]));

      // give error message with scarlet-colored text if player isn't found
      if (other == null) {
        player.sendMessage("[scarlet]No player by that name found!");
        return;
      }

      // send the other player a message, using [lightgray] for gray text color and []
      // to reset color
      other.sendMessage("[lightgray](whisper) " + player.name + ":[] " + args[1]);
    });
  }
}
