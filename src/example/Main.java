package example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import arc.*;
import arc.util.*;
import example.CustomBalance.UnitSetting;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.Damage;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.*;

public class Main extends Plugin{

    CustomBalance customBalance;

    //called when game initializes
    @Override
    public void init(){
        customBalance = new CustomBalance();
    }

    @Override
    public void registerServerCommands(CommandHandler handler){

        handler.register("cb-load", "reloads configuration", (args) -> {
            customBalance.load();
        });
        handler.register("cb-show", "show configuration", (args) -> {
            for (Entry<UnitType, UnitSetting> entey : CustomBalance.customSetting.entrySet()) {
                Log.info(entey.getKey().name + ":\n" + entey.getValue().toString());
            }
        });
    }
}


