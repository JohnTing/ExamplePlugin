package unitcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import arc.Events;
import arc.graphics.Color;
import arc.util.CommandHandler;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
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
import mindustry.game.EventType.Trigger;
import mindustry.gen.Call;
import mindustry.plugin.Plugin;
import mindustry.world.Tile;
import mindustry.world.blocks.units.CommandCenter.CommandCenterEntity;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.indexer;

public class UnitControlPlugin extends Plugin {

  // register event handlers and create variables in the constructor
  public UnitControlPlugin() {

    Map<Player, Set<Unit>> playersMap = new HashMap<Player, Set<Unit>>();
    Map<Unit, UnitControler> unitsMap = new HashMap<Unit, UnitControler>();

    // Map<Player, Map<Unit, UnitControler>> controlMap = new HashMap<Player,
    // Map<Unit, UnitControler>>();
    Map<Player, UnitCommand> lastCommand = new HashMap<Player, UnitCommand>();

    Interval timer = new Interval(5);

    // Re-control all units that should be controlled,
    // Because in some cases the unit status will be overwritten.
    // (For example, deleting the last commandCenter)
    // This might be what UnitControler should do
    Events.on(Trigger.update.getClass(), event -> {
      if (timer.get(0, 60)) {
        List<Unit> removekey = new ArrayList<Unit>();
        for (Player player : Vars.playerGroup) {
          UnitCommand command = UnitCommand.rally;
          if (lastCommand.get(player) != null) {
            command = lastCommand.get(player);
          }
          Set<Unit> unitSet = playersMap.get(player);
          if (unitSet != null) {
            for (Unit unit : unitSet) {
              if (unit.isDead()) {
                removekey.add(unit);
              } else {
                UnitControler controler = unitsMap.get(unit);
                if (controler != null) {
                  controler.setCommand(command);
                }
              }
            }
          }
        }
        for (Unit unit : removekey) {
          unitsMap.remove(unit);
        }
      }
    });

    Events.on(TapConfigEvent.class, event -> {
      Player player = event.player;
      Tile tile = event.tile;
      int value = event.value;

      if (tile != null && tile.block() == Blocks.commandCenter) {
        UnitCommand command = UnitCommand.attack;
        if (value < UnitCommand.all.length) {
          command = UnitCommand.all[value];
        }
        lastCommand.put(player, command);

        Set<Unit> unitSet = playersMap.get(player);
        if (unitSet != null) {
          if (command == UnitCommand.retreat) {
            unitSet.clear();
          } else {
            List<Unit> delKey = new ArrayList<Unit>();
            for (Unit unit : unitSet) {

              if (unit.isDead()) {
                delKey.add(unit);
              } else {
                unitsMap.get(unit).setCommand(command);
              }
            }
            for (Unit unit : delKey) {
              unitSet.remove(unit);
            }
          }
        }
      }
    });

    Events.on(TapEvent.class, e -> {
      Player player = e.player;
      Tile tile = e.tile;

      UnitCommand command = UnitCommand.rally;

      if (lastCommand.get(player) != null) {
        command = lastCommand.get(player);
      }

      if (command == UnitCommand.retreat) {
        return;
      }

      Unit seleteedUnit = Units.closest(player.getTeam(), tile.drawx(), tile.drawy(), 40f, u -> !u.isDead());

      if (seleteedUnit instanceof BaseUnit) {
        BaseUnit unit = (BaseUnit) seleteedUnit;

        // If the unit has been controlled
        if (unitsMap.containsKey(unit)) {
          return;
        }

        Set<Unit> unitSet = playersMap.get(player);

        if (unitSet == null) {
          unitSet = new HashSet<Unit>();
          playersMap.put(player, unitSet);
        }
        if (!unitSet.contains(unit)) {
          UnitControler controler = null;
          if (unit instanceof GroundUnit) {
            controler = new GroundUnitControler(unit, player);
          } else if (unit instanceof HoverUnit) {
            controler = new FlyingUnitControler(unit, player);
          } else if (unit instanceof FlyingUnit) {
            controler = new FlyingUnitControler(unit, player);
          }
          if (controler != null) {
            unitSet.add(unit);
            unitsMap.put(unit, controler);
            Call.onEffect(Fx.dooropenlarge, unit.getX(), unit.getY(), 0, Color.blue);
            controler.setCommand(command);
          }
        }
      }
    });
  }

  public boolean isCommanded() {
    Team team = Team.sharded;
    return indexer.getAllied(team, BlockFlag.comandCenter).size != 0
        && indexer.getAllied(team, BlockFlag.comandCenter).first().entity instanceof CommandCenterEntity;
  }

  // register commands that player can invoke in-game
  @Override
  public void registerClientCommands(CommandHandler handler) {

    handler.<Player>register("send", "<text...>", "A simple ping command that echoes a player's text.",
        (args, player) -> {
          //if (args[0] == "erad") {
            BaseUnit baseUnit = UnitTypes.eradicator.create(player.getTeam());
            baseUnit.set(player.x, player.y);
            baseUnit.add();
          //}
        });

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
