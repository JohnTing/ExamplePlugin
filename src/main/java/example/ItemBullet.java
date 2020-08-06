package example;

import java.util.HashMap;
import java.util.Map;

import arc.*;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
//import arc.math.Angles;
import arc.util.*;
//import arc.util.Timer.Task;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.traits.ShooterTrait;
import mindustry.entities.type.*;
import mindustry.game.EventType;
import mindustry.game.Team;
//import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.plugin.Plugin;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Mech;
import mindustry.world.Block;
//import mindustry.type.Weapon;
//import mindustry.world.blocks.defense.turrets.BurstTurret;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class ItemBullet extends Plugin {
    // Player's item information will be cleared before UnitDestroyEvent, so it must be recorded
    Map<Integer, ItemStack> PlayerItems;
  
    public Map<Item, BulletType> bulletTable = null;

    public Map<Mech, ObjectMap<Item, BulletType>> mechTabel = null;

    public ObjectMap<Item, BulletType> getTurretAmmo(Block block) {
      if(block != null && block instanceof ItemTurret) {
        return ((ItemTurret)block).ammo;
      }
      return null;
    }

    public void bulletTableInit() {
      if(bulletTable == null || bulletTable.size() == 0) {
        Call.sendMessage("bulletTableInit");

        bulletTable = new HashMap<Item, BulletType>();
        mechTabel = new HashMap<Mech, ObjectMap<Item, BulletType>>();

        ObjectMap<Item, BulletType> tempMap = new ObjectMap<Item, BulletType>();
        
        tempMap.put(Items.copper, Bullets.standardCopper);
        tempMap.put(Items.graphite, Bullets.artilleryDense);
        tempMap.put(Items.silicon, Bullets.standardHoming);
        tempMap.put(Items.thorium, Bullets.standardThorium);

        mechTabel.put(Mechs.dart, getTurretAmmo(Blocks.salvo).merge(getTurretAmmo(Blocks.duo)));
        mechTabel.put(Mechs.glaive, getTurretAmmo(Blocks.scatter).merge(getTurretAmmo(Blocks.cyclone)));
        mechTabel.put(Mechs.alpha, getTurretAmmo(Blocks.salvo));
        mechTabel.put(Mechs.omega, getTurretAmmo(Blocks.swarmer));

        bulletTable = new HashMap<Item, BulletType>();
        
        // special
        bulletTable.put(Items.coal, Bullets.lightning);
        bulletTable.put(Items.sand, Bullets.arc);
        bulletTable.put(Items.phasefabric, Bullets.damageLightning);
        bulletTable.put(Items.sporePod, Bullets.oilShot);
        bulletTable.put(Items.titanium, Bullets.cryoShot);
        
        // scatter & cyclone to glaive
        bulletTable.put(Items.lead, Bullets.flakLead);
        bulletTable.put(Items.scrap, Bullets.flakScrap);
        bulletTable.put(Items.metaglass, Bullets.flakGlass);
        
        // to alpha
        bulletTable.put(Items.plastanium, Bullets.flakPlastic);
        
        // swarmer to omega
        bulletTable.put(Items.pyratite, Bullets.missileIncendiary);
        bulletTable.put(Items.blastCompound, Bullets.missileExplosive);
        bulletTable.put(Items.surgealloy, Bullets.missileSurge);

        // darf
        bulletTable.put(Items.copper, Bullets.standardCopper);
        bulletTable.put(Items.graphite, Bullets.standardDense);
        bulletTable.put(Items.silicon, Bullets.standardHoming);
        bulletTable.put(Items.thorium, Bullets.standardThorium);
        

        
        

      }
    }

    // register event handlers and create variables in the constructor
    public ItemBullet() {
        PlayerItems = new HashMap<Integer, ItemStack>();

        Events.on(EventType.UnitDestroyEvent.class, e -> {
            if (e.unit instanceof Player) {
                Player player = (Player) e.unit;
                Call.sendMessage("player.id " + player.id);

                ItemStack itemStack = PlayerItems.get(player.id);

                if (PlayerItems.containsKey(player.id) && itemStack != null) {

                    Call.sendMessage("PlayerItems " + itemStack);
                    PlayerItems.put(player.id, null);

                    bulletTableInit();
                    BulletType bullet = bulletTable.get(itemStack.item);

                    int times = itemStack.amount;

                    for (int i = 0; i < times; i++) {
                        float angle = 360f * i / times;
                        Call.createBullet(bullet, player.getTeam(), player.getX(), player.getY(), 
                        (player.rotation + angle) % 360, 1f, 1f);
                    }
                    // Bullet.create(Bullets.arc, player, player.getTeam(), player.getX(),
                    // player.getY(), player.rotation);
                } else {
                    Call.sendMessage(PlayerItems.toString());
                }
            }
        });

        Events.on(EventType.WithdrawEvent.class, e -> {
            Call.sendMessage("WithdrawEvent " + e.player.item().amount);
            PlayerItems.put(e.player.id, e.player.item().copy());
        });
        Events.on(EventType.DepositEvent.class, e -> {
            Call.sendMessage("DepositEvent " + e.player.item().amount);
            PlayerItems.put(e.player.id, e.player.item().copy());
        });

        Events.on(EventType.PlayerLeave.class, e -> {
            PlayerItems.remove(e.player.id);
        });
        
        
        Events.on(EventType.Trigger.update.getClass(), e -> {

            for(Player player : Vars.playerGroup) {

                //player shoot
                if(player.getTimer().getTime(0) == 0) {
                    Call.sendMessage("shoot");
                    shootByItem(player, 1);
                }
                if(player.getTimer().getTime(1) == 0) {
                    shootByItem(player, 0);
                }
            }
        });
    }

    // register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("reactors", "List all thorium reactors in the map.", args -> {
            for (int x = 0; x < Vars.world.width(); x++) {
                for (int y = 0; y < Vars.world.height(); y++) {
                    // loop through and log all found reactors
                    if (Vars.world.tile(x, y).block() == Blocks.thoriumReactor) {
                        Log.info("Reactor at {0}, {1}", x, y);
                    }
                }
            }
        });
    }
    
    public void shootByItem(Player player, int useItem) {
        
        if(player != null && player.item() != null && player.item().item != null && player.item().amount > 0) {
            bulletTableInit();

            //mechTabel.get(player.mech).get(player.item().item, null);


            BulletType bullet = bulletTable.get(player.item().item);
            // BulletType bullet = mechTabel.get(player.mech).get(player.item().item, null);

            if(bullet != null) {
                if(!player.mech.turnCursor) {
                    useItem *= 2;
                }

                useItem = Math.min(useItem, player.item().amount);

                if(useItem > 0) {
                    player.item().amount -= useItem;
                    PlayerItems.put(player.id, player.item().copy());
                    player.sendMessage(":" + player.item().amount);
                }

                Team team = player.getTeam();
                float x =  player.getX();
                float y = player.getY();
                float angel = player.angleTo(player.pointerX, player.pointerY);

                ShooterTrait shooterTrait = (ShooterTrait)player;
                shooterTrait.velocity();

                float speed = 1/bullet.speed*player.mech.weapon.bullet.speed;
                
                Vec2 vec = new Vec2();
                vec.set(1, 1);
                vec.setAngle(angel);
                vec.setLength(speed);
                vec.add(shooterTrait.velocity().scl(1/bullet.speed));

                angel = vec.angle();
                speed = vec.len();

                float lifetime = 1/bullet.lifetime*player.mech.weapon.bullet.lifetime;
                Call.createBullet(bullet, team, x, y, angel, speed, lifetime);


                // float speeda = Mathf.cos((float)Math.toRadians(shooterTrait.velocity().angle() - angel));
                // speed += shooterTrait.velocity().len() * speeda / bullet.speed;
                

                
                if(player.mech.turnCursor) {
                    Call.createBullet(bullet, team, x, y, angel, speed, lifetime);
                    Call.createBullet(bullet, team, x, y, angel + 15f, speed, lifetime);
                    Call.createBullet(bullet, team, x, y, angel - 15f, speed, lifetime);
                }
                else {
                    for(int i = 0;i < (4 * useItem);i++) {
                        float angle = 360f * i / (4 * useItem);
                        Call.createBullet(bullet, team, x, y, player.angleTo(player.pointerX, player.pointerY) + angle, 0.05f, 0.6f);
                    }
                }
            } 
            else {
              Call.sendMessage("fail");
                //Call.sendMessage("fail " + player.item().item + " : " + bullet + " : " + Items.copper);
                //Call.sendMessage(bulletTable.toString());
                //Call.sendMessage("" + bulletTable.size());
            }
        }
    }
    /*
    public void callShoot(Player player, float angle) {
        Call.createBullet(Bullets.flakSurge, player.getTeam(), player.getX(), player.getY(), angle, 1f, 1f);
    }
    */

    // register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler) {

        // register a simple reply command
        /*
        handler.<Player>register("shoot", "<text...>", "A simple ping command that test.", (args, player) -> {

            
            Bullet.create(Bullets.arc, player, player.getTeam(), player.getX(), player.getY(), player.rotation);
            player.sendMessage("You said: [accent] " + player.rotation);

        });*/
        /*
        //register a simple reply command
        handler.<Player>register("test", "<text...>", "A simple ping command that test.", (args, player) -> {
            Bullet.create(Bullets.standardThoriumBig, player, player.getTeam(), player.getX(), player.getY(), player.rotation, 1f, 2f);
            Bullet.create(Bullets.arc, player, player.getTeam(), player.getX(), player.getY(), player.rotation);
            player.sendMessage("You said: [accent] " + player.rotation);
            
        });
        */
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
