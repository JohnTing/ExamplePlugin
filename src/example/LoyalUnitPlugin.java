package example;

import arc.*;
import arc.graphics.Color;
import arc.util.*;
import mindustry.*;
import mindustry.ai.formations.patterns.CircleFormation;
import mindustry.ai.types.FormationAI;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;

public class LoyalUnitPlugin extends Plugin {

    // called when game initializes
    @Override
    public void init() {
      // add an action filter for preventing players from doing certain things
      Vars.netServer.admins.addActionFilter(action -> {
        // random example: prevent blast compound depositing
        if (action.type == ActionType.command) {
          Commanderc commander = action.player.unit();
  
          // replace the command function
          if (commander.isCommanding()) {
            commander.clearCommand();
          } else if (action.player.unit().type.commandLimit > 0) {
            commander.commandNearby(new CircleFormation(), (unit) -> !(unit.controller() instanceof FormationAI));         
            Call.effect(Fx.commandSend, action.player.x, action.player.y, 0, Color.white);
          }
          return false;
        }
        return true;
      });
    }
  }
