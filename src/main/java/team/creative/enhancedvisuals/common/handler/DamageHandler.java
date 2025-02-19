package team.creative.enhancedvisuals.common.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import team.creative.creativecore.CreativeCore;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.CreativeConfig.DecimalRange;
import team.creative.creativecore.common.config.premade.DecimalMinMax;
import team.creative.creativecore.common.config.premade.IntMinMax;
import team.creative.creativecore.common.config.premade.curve.DecimalCurve;
import team.creative.creativecore.common.util.type.Color;
import team.creative.enhancedvisuals.api.VisualHandler;
import team.creative.enhancedvisuals.api.event.FireParticlesEvent;
import team.creative.enhancedvisuals.api.type.VisualType;
import team.creative.enhancedvisuals.api.type.VisualTypeOverlay;
import team.creative.enhancedvisuals.api.type.VisualTypeParticle;
import team.creative.enhancedvisuals.api.type.VisualTypeParticleColored;
import team.creative.enhancedvisuals.client.VisualManager;
import team.creative.enhancedvisuals.common.packet.DamagePacket;

public class DamageHandler extends VisualHandler {
    
    public static final ArrayList<Item> sharpList = new ArrayList<>();
    public static final ArrayList<Item> bluntList = new ArrayList<>();
    public static final ArrayList<Item> pierceList = new ArrayList<>();
    
    public static final Color BLOOD_COLOR = new Color(0.3F, 0.01F, 0.01F, 0.7F);
    
    static {
        sharpList.add(Items.IRON_SWORD);
        sharpList.add(Items.WOODEN_SWORD);
        sharpList.add(Items.STONE_SWORD);
        sharpList.add(Items.DIAMOND_SWORD);
        sharpList.add(Items.GOLDEN_SWORD);
        sharpList.add(Items.IRON_AXE);
        sharpList.add(Items.WOODEN_AXE);
        sharpList.add(Items.STONE_AXE);
        sharpList.add(Items.DIAMOND_AXE);
        sharpList.add(Items.GOLDEN_AXE);
        
        bluntList.add(Items.IRON_PICKAXE);
        bluntList.add(Items.WOODEN_PICKAXE);
        bluntList.add(Items.STONE_PICKAXE);
        bluntList.add(Items.DIAMOND_PICKAXE);
        bluntList.add(Items.GOLDEN_PICKAXE);
        bluntList.add(Items.IRON_SHOVEL);
        bluntList.add(Items.WOODEN_SHOVEL);
        bluntList.add(Items.STONE_SHOVEL);
        bluntList.add(Items.DIAMOND_SHOVEL);
        bluntList.add(Items.GOLDEN_SHOVEL);
        
        pierceList.add(Items.IRON_HOE);
        pierceList.add(Items.WOODEN_HOE);
        pierceList.add(Items.STONE_HOE);
        pierceList.add(Items.DIAMOND_HOE);
        pierceList.add(Items.GOLDEN_HOE);
        pierceList.add(Items.ARROW);
    }
    
    @CreativeConfig
    public VisualType damaged = new VisualTypeOverlay("damaged");
    @CreativeConfig
    @DecimalRange(min = 0, max = 1)
    public float hitEffectIntensity = 0F;
    @CreativeConfig
    public IntMinMax hitDuration = new IntMinMax(1, 10);
    
    @CreativeConfig
    public VisualType splatter = new VisualTypeParticle("splatter");
    @CreativeConfig
    public VisualType impact = new VisualTypeParticle("impact");
    @CreativeConfig
    public VisualType slash = new VisualTypeParticle("slash");
    @CreativeConfig
    public VisualType pierce = new VisualTypeParticle("pierce");
    @CreativeConfig
    public IntMinMax bloodDuration = new IntMinMax(500, 1500);
    
    @CreativeConfig
    public VisualType fire = new VisualTypeParticle("fire");
    @CreativeConfig
    public int fireSplashes = 1;
    @CreativeConfig
    public IntMinMax fireDuration = new IntMinMax(100, 1000);
    
    @CreativeConfig
    public int drownSplashes = 4;
    @CreativeConfig
    public IntMinMax drownDuration = new IntMinMax(10, 15);
    @CreativeConfig
    public VisualType waterDrown = new VisualTypeParticleColored("blob", new Color(0, 0, 255)).setIgnoreWater();
    
    @CreativeConfig
    public int lightningSplashes = 10;
    @CreativeConfig
    public IntMinMax lightningDuration = new IntMinMax(10, 15);
    @CreativeConfig
    public VisualType lightning = new VisualTypeParticleColored("shock", new Color(120, 120, 255)).setIgnoreWater();
    
    @CreativeConfig
    public int freezeSplashes = 4;
    @CreativeConfig
    public IntMinMax freezeDuration = new IntMinMax(10, 15);
    @CreativeConfig
    public VisualType freeze = new VisualTypeParticleColored("ice", new Color(200, 255, 255));
    
    @CreativeConfig
    public int flyIntoWallSplashes = 4;
    @CreativeConfig
    public IntMinMax flyIntoWallDuration = new IntMinMax(10, 15);
    @CreativeConfig
    public VisualType flyIntoWall = new VisualTypeParticleColored("break", new Color(255, 255, 255));
    
    @CreativeConfig
    public int heatSplashes = 4;
    @CreativeConfig
    public IntMinMax heatDuration = new IntMinMax(10, 15);
    @CreativeConfig
    public VisualType heat = new VisualTypeParticleColored("heat", new Color(255, 255, 255)) {
        
        @Override
        public boolean canRotate() {
            return false;
        }
        
    };
    
    @CreativeConfig
    public int effectSplashes = 4;
    @CreativeConfig
    public IntMinMax effectDuration = new IntMinMax(10, 15);
    @CreativeConfig
    public VisualType parasites = new VisualTypeParticleColored("parasite", new Color(0, 126, 0)).setIgnoreWater();
    @CreativeConfig
    public VisualType wither = new VisualTypeParticleColored("wither", 0, new DecimalMinMax(1.5, 2.5), new Color(0, 0, 0)).setIgnoreWater();
    
    @CreativeConfig
    public IntMinMax tunnelDuration = new IntMinMax(10, 15);
    @CreativeConfig
    public VisualType tunnel = new VisualTypeOverlay("tunnel", 0).setIgnoreWater();
    
    @CreativeConfig
    public DecimalCurve healthScaler = new DecimalCurve(0, 3, 12, 1.5);
    
    @CreativeConfig
    public float damageScale = 1;
    
    @CreativeConfig
    public List<String> damageBlackList = new ArrayList<>();
    
    public void clientHurt() {
        if (hitEffectIntensity > 0)
            VisualManager.addVisualFadeOut(damaged, this, new DecimalCurve(VisualManager.RANDOM, hitDuration, hitEffectIntensity * 0.2));
    }
    
    public void playerDamaged(Player player, DamagePacket packet) {
        if (packet.source.equalsIgnoreCase("attacker")) {
            if (packet.attackerClass.contains("arrow"))
                createVisualFromDamageAndDistance(pierce, packet.damage, player, bloodDuration);
            if (packet.stack != null) {
                if (isSharp(packet.stack))
                    createVisualFromDamageAndDistance(slash, packet.damage, player, bloodDuration);
                else if (isBlunt(packet.stack))
                    createVisualFromDamageAndDistance(impact, packet.damage, player, bloodDuration);
                else if (isPierce(packet.stack))
                    createVisualFromDamageAndDistance(pierce, packet.damage, player, bloodDuration);
                else
                    createVisualFromDamageAndDistance(splatter, packet.damage, player, bloodDuration);
            } else if (packet.attackerClass.contains("zombie") || packet.attackerClass.contains("skeleton") || packet.attackerClass.contains("ocelot"))
                createVisualFromDamageAndDistance(slash, packet.damage, player, bloodDuration);
            else if (packet.attackerClass.contains("golem") || packet.attackerClass.contains("player"))
                createVisualFromDamageAndDistance(impact, packet.damage, player, bloodDuration);
            else if (packet.attackerClass.contains("wolf") || packet.attackerClass.contains("spider"))
                createVisualFromDamageAndDistance(pierce, packet.damage, player, bloodDuration);
            else
                createVisualFromDamageAndDistance(splatter, Math.min(20, packet.damage), player, bloodDuration);
        } else if (packet.source.equalsIgnoreCase("cactus"))
            createVisualFromDamageAndDistance(pierce, packet.damage, player, bloodDuration);
        else if (packet.source.equalsIgnoreCase("fall") || packet.source.equalsIgnoreCase("fallingBlock"))
            createVisualFromDamageAndDistance(impact, packet.damage, player, bloodDuration);
        else if (packet.source.equalsIgnoreCase("drown"))
            VisualManager.addParticlesFadeOut(waterDrown, this, drownSplashes, drownDuration, true);
        else if (packet.source.equalsIgnoreCase("hypothermia"))
            VisualManager.addParticlesFadeOut(freeze, this, freezeSplashes, freezeDuration, true);
        else if (packet.source.equalsIgnoreCase("hyperthermia"))
            VisualManager.addParticlesFadeOut(heat, this, heatSplashes, heatDuration, true);
        else if (packet.source.equalsIgnoreCase("wither"))
            VisualManager.addParticlesFadeOut(wither, this, effectSplashes, effectDuration, true);
        else if (packet.source.equalsIgnoreCase("parasites"))
            VisualManager.addParticlesFadeOut(parasites, this, effectSplashes, effectDuration, true);
        else if (packet.source.equalsIgnoreCase("lightningBolt"))
            VisualManager.addParticlesFadeOut(lightning, this, lightningSplashes, lightningDuration, true);
        else if (packet.source.equalsIgnoreCase("flyIntoWall"))
            VisualManager.addParticlesFadeOut(flyIntoWall, this, flyIntoWallSplashes, flyIntoWallDuration, true);
        else if (packet.source.equalsIgnoreCase("dehydration") || packet.source.equalsIgnoreCase("starve"))
            VisualManager.addVisualFadeOut(tunnel, this, tunnelDuration);
        else if (packet.fire || packet.source.equalsIgnoreCase("onFire")) {
            FireParticlesEvent event = new FireParticlesEvent(fireSplashes, fireDuration.min, fireDuration.max);
            CreativeCore.loader().postForge(event);
            VisualManager.addParticlesFadeOut(fire, this, event.getNewFireSplashes(), new IntMinMax(event.getNewFireDurationMin(), event.getNewFireDurationMax()), true,
                new Color(0, 0, 0));
        } else if (!damageBlackList.contains(packet.source))
            createVisualFromDamageAndDistance(splatter, Math.min(20, packet.damage), player, bloodDuration);
        
    }
    
    public void createVisualFromDamageAndDistance(VisualType type, float damage, Player player, IntMinMax duration) {
        if (damage <= 0.0F)
            return;
        
        float health = player.getHealth() - damage;
        double rate = Math.max(0, healthScaler.valueAt(health));
        
        VisualManager.addParticlesFadeOut(type, this, Math.min(5000, (int) (damageScale * damage * rate)), new DecimalCurve(0, 1, duration.next(VisualManager.RANDOM), 0), true,
            BLOOD_COLOR);
    }
    
    private static boolean isSharp(ItemStack item) {
        return sharpList.contains(item.getItem());
    }
    
    private static boolean isBlunt(ItemStack item) {
        return bluntList.contains(item.getItem());
    }
    
    private static boolean isPierce(ItemStack item) {
        return pierceList.contains(item.getItem());
    }
    
}
