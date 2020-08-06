package example;

import arc.*;
import arc.math.Mathf;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.traits.BuilderTrait;
import mindustry.entities.traits.BuilderTrait.BuildRequest;
import mindustry.entities.type.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.plugin.Plugin;
import mindustry.world.Tile;
import mindustry.world.blocks.BuildBlock;
import mindustry.world.blocks.BuildBlock.BuildEntity;
import static mindustry.Vars.*;

public class BuilderPower extends Plugin {

  // register event handlers and create variables in the constructor
  public BuilderPower() {
    // BuildSelectEvent Version
    /*
    Events.on(BuildSelectEvent.class, event -> {
      if (event.builder != null && event.builder instanceof Player) {
        Tile tile = event.tile;
        BuilderTrait builder = event.builder;
        
        if(!(tile.ent() instanceof BuildEntity)){
          return;
        }
        BuildEntity entity = tile.ent();

        Unit unit = (Unit)builder;
        BuildRequest current = event.builder.buildRequest();
        TileEntity core = unit.getClosestCore();
        if(current.breaking){
          entity.deconstruct(unit, core, 1f);
          Call.sendMessage("breaking");
        }
        else if(entity.construct(unit, core, 1f, current.hasConfig)) {
          Call.sendMessage("building");
          if(current.hasConfig){
              Call.onTileConfig(null, tile, current.config);
          }
        }
        else {
          Call.sendMessage("what?");
        }
      }
    });
    */
    
    // update Version
    Events.on(Trigger.update.getClass(), event -> {
      for (Player player : playerGroup) {

        //nothing to build.
        if(player.buildRequest() == null) continue;

        BuildRequest current = player.buildRequest();

        float finalPlaceDst = state.rules.infiniteResources ? Float.MAX_VALUE : Player.placeDistance;
        if(player.dst(current.tile()) > finalPlaceDst) continue;

        Tile tile = world.tile(current.x, current.y);
        if(!(tile.block() instanceof BuildBlock)) continue;
        if(tile.getTeam() != player.getTeam()) continue;
        Unit unit = (Unit)player;
        TileEntity core = unit.getClosestCore();
        //if there is no core to build with or no build entity, stop building!
        if((core == null && !state.rules.infiniteResources) || !(tile.entity instanceof BuildEntity)){
          continue;
        }

        //otherwise, update it.
        BuildEntity entity = tile.ent();

        if(entity == null){
            return;
        }
        if(current.breaking){
          entity.deconstruct(unit, core, 1f / entity.buildCost * Time.delta() * 10f * state.rules.buildSpeedMultiplier);
        }else{
            if(entity.construct(unit, core, 1f / entity.buildCost * Time.delta() * 10f * state.rules.buildSpeedMultiplier, current.hasConfig)){
                if(current.hasConfig){
                    Call.onTileConfig(null, tile, current.config);
                }
            }
        }
        current.stuck = Mathf.equal(current.progress, entity.progress);
        current.progress = entity.progress;
      }
    });
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
