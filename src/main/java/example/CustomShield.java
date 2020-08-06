package example;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.Vars;
import mindustry.Vars.*;
import mindustry.content.Blocks;
import mindustry.entities.EntityGroup;
import mindustry.entities.traits.DrawTrait;
import mindustry.entities.type.BaseEntity;
import mindustry.entities.type.Player;
import mindustry.entities.type.TileEntity;
import mindustry.game.EventType.BuildSelectEvent;
import mindustry.gen.Call;
import mindustry.graphics.Pal;
import mindustry.plugin.Plugin;
import mindustry.world.Tile;

public class CustomShield extends Plugin {
  public CustomShield(){
    //listen for a block selection event
    Events.on(BuildSelectEvent.class, event -> {
      
        if(!event.breaking && event.builder != null && event.builder.buildRequest() != null && event.builder.buildRequest().block == Blocks.thoriumReactor && event.builder instanceof Player){
            //send a message to everyone saying that this player has begun building a reactor
            Call.sendMessage("[scarlet]ALERT![] " + ((Player)event.builder).name + " has begun building a reactor at " + event.tile.x + ", " + event.tile.y);
        }
    });
}

//register commands that run on the server
@Override
public void registerServerCommands(CommandHandler handler){
    handler.register("reactors", "List all thorium reactors in the map.", args -> {
        for(int x = 0; x < Vars.world.width(); x++){
            for(int y = 0; y < Vars.world.height(); y++){
                //loop through and log all found reactors
                if(Vars.world.tile(x, y).block() == Blocks.thoriumReactor){
                    Log.info("Reactor at {0}, {1}", x, y);
                }
            }
        }
    });
}

//register commands that player can invoke in-game
@Override
public void registerClientCommands(CommandHandler handler){

    //register a simple reply command
    handler.<Player>register("test", "<text...>", "A simple ping command that echoes a player's text.", (args, player) -> {
      player.sendMessage("You said: [accent] " + args[0]);
      ShieldEntity entity = new ShieldEntity(player);
      entity.add();

      /*
      ForceEntity entity = new ForceEntity();
      if(entity != null && entity.shield == null){
        entity.shield = new ShieldEntity(player.tileOn());
        entity.shield.add();
      }
      else {
        Call.sendMessage("null");
      }*/
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

  class ForceEntity extends TileEntity{
    ShieldEntity shield;
    boolean broken = true;
    float buildup = 0f;
    float radscl = 0f;
    float hit;
    float warmup;
    float phaseHeat;

    @Override
    public void write(DataOutput stream) throws IOException{
        super.write(stream);
        stream.writeBoolean(broken);
        stream.writeFloat(buildup);
        stream.writeFloat(radscl);
        stream.writeFloat(warmup);
        stream.writeFloat(phaseHeat);
    }

    @Override
    public void read(DataInput stream, byte revision) throws IOException{
        super.read(stream, revision);
        broken = stream.readBoolean();
        buildup = stream.readFloat();
        radscl = stream.readFloat();
        warmup = stream.readFloat();
        phaseHeat = stream.readFloat();
    }
}

public class ShieldEntity extends BaseEntity implements DrawTrait{
    // final ForceEntity entity;
    final Player entity;
    public ShieldEntity(Player entity){
      this.entity = entity;
      set(entity.x, entity.y);
  }

    @Override
    public void update(){
      set(entity.x, entity.y);
      /*
        if(entity.isDead() || !entity.isAdded()){
            remove();
        }*/
    }

    @Override
    public float drawSize(){
        return realRadius(entity) * 2f + 2f;
    }
    public float realRadius(BaseEntity entity) {
      return 80f;
    }

    @Override
    public void draw(){
        Draw.color(Pal.accent);
        Fill.poly(x, y, 6, realRadius(entity));
        Draw.color();
    }

    public void drawOver(){
      /*
        if(entity.hit <= 0f) return;

        Draw.color(Color.white);
        Draw.alpha(entity.hit);
        Fill.poly(x, y, 6, realRadius(entity));
        Draw.color();*/
    }

    public void drawSimple(){
        if(realRadius(entity) < 0.5f) return;

        float rad = realRadius(entity);

        Draw.color(Pal.accent);
        Lines.stroke(1.5f);
        //Draw.alpha(0.09f + 0.08f * entity.hit);
        Fill.poly(x, y, 6, rad);
        Draw.alpha(1f);
        Lines.poly(x, y, 6, rad);
        Draw.reset();
    }

    @Override
    public EntityGroup targetGroup(){
        return Vars.shieldGroup;
    }
  }

}
