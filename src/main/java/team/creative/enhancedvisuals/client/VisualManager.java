package team.creative.enhancedvisuals.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.config.premade.IntMinMax;
import team.creative.creativecore.common.config.premade.curve.Curve;
import team.creative.creativecore.common.config.premade.curve.DecimalCurve;
import team.creative.creativecore.common.util.type.Color;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.enhancedvisuals.EnhancedVisuals;
import team.creative.enhancedvisuals.api.Particle;
import team.creative.enhancedvisuals.api.Visual;
import team.creative.enhancedvisuals.api.VisualCategory;
import team.creative.enhancedvisuals.api.VisualHandler;
import team.creative.enhancedvisuals.api.type.VisualType;
import team.creative.enhancedvisuals.client.sound.SoundMuteHandler;
import team.creative.enhancedvisuals.client.sound.TickedSound;
import team.creative.enhancedvisuals.common.event.EVEvents;
import team.creative.enhancedvisuals.common.visual.VisualRegistry;

public class VisualManager {
    
    private static Minecraft mc = Minecraft.getInstance();
    public static final Random RANDOM = new Random();
    private static HashMapList<VisualCategory, Visual> visuals = new HashMapList<>();
    private static List<TickedSound> playing = new ArrayList<>();
    
    public static void onTick(@Nullable Player player) {
        boolean areEyesInWater = player != null && EVEvents.areEyesInWater(player);
        
        synchronized (visuals) {
            for (Iterator<Visual> iterator = visuals.iterator(); iterator.hasNext();) {
                Visual visual = iterator.next();
                
                int factor = 1;
                if (areEyesInWater && visual.isAffectedByWater())
                    factor = EnhancedVisuals.CONFIG.waterSubstractFactor;
                
                for (int i = 0; i < factor; i++) {
                    if (!visual.tick()) {
                        visual.removeFromDisplay();
                        iterator.remove();
                        break;
                    }
                }
            }
            
            for (VisualHandler handler : VisualRegistry.handlers()) {
                if (handler.isEnabled(player))
                    handler.tick(player);
            }
        }
        
        if (player != null && !player.isAlive())
            VisualManager.clearEverything();
        
        if (!playing.isEmpty())
            playing.removeIf(x -> x.isStopped());
    }
    
    public static Collection<Visual> visuals(VisualCategory category) {
        return visuals.get(category);
    }
    
    public static void clearEverything() {
        synchronized (visuals) {
            visuals.removeKey(VisualCategory.particle);
        }
        if (!playing.isEmpty()) {
            for (TickedSound sound : playing)
                sound.stop();
            playing.clear();
            SoundMuteHandler.endMuting();
        }
    }
    
    public static void add(Visual visual) {
        if (!visual.type.disabled) {
            visual.addToDisplay();
            visuals.add(visual.getCategory(), visual);
        }
    }
    
    public static boolean remove(Visual visual) {
        if (visuals.removeValue(visual.getCategory(), visual)) {
            visual.removeFromDisplay();
            return true;
        }
        return false;
    }
    
    public static void playTicking(ResourceLocation location, BlockPos pos, DecimalCurve volume) {
        TickedSound sound;
        if (pos != null)
            sound = new TickedSound(location, SoundSource.MASTER, 1, pos, volume);
        else
            sound = new TickedSound(location, SoundSource.MASTER, 1, volume);
        playing.add(sound);
        Minecraft.getInstance().getSoundManager().play(sound);
    }
    
    public static Visual addVisualFadeOut(VisualType vt, VisualHandler handler, IntMinMax time) {
        return addVisualFadeOut(vt, handler, new DecimalCurve(0, 1, time.next(RANDOM), 0));
    }
    
    public static Visual addVisualFadeOut(VisualType vt, VisualHandler handler, int time) {
        return addVisualFadeOut(vt, handler, new DecimalCurve(0, 1, time, 0));
    }
    
    public static Visual addVisualFadeOut(VisualType vt, VisualHandler handler, Curve curve) {
        Visual v = new Visual(vt, handler, curve, vt.getVariantAmount() > 1 ? RANDOM.nextInt(vt.getVariantAmount()) : 0);
        add(v);
        return v;
    }
    
    public static void addParticlesFadeOut(VisualType vt, VisualHandler handler, int count, IntMinMax time, boolean rotate) {
        addParticlesFadeOut(vt, handler, count, new DecimalCurve(0, 1, time.next(RANDOM), 0), rotate, null);
    }
    
    public static void addParticlesFadeOut(VisualType vt, VisualHandler handler, int count, IntMinMax time, boolean rotate, @Nullable Color color) {
        addParticlesFadeOut(vt, handler, count, new DecimalCurve(0, 1, time.next(RANDOM), 0), rotate, color);
    }
    
    public static void addParticlesFadeOut(VisualType vt, VisualHandler handler, int count, int time, boolean rotate) {
        addParticlesFadeOut(vt, handler, count, new DecimalCurve(0, 1, time, 0), rotate, null);
    }
    
    public static void addParticlesFadeOut(VisualType vt, VisualHandler handler, int count, Curve curve, boolean rotate, @Nullable Color color) {
        if (vt.disabled)
            return;
        for (int i = 0; i < count; i++) {
            int screenWidth = mc.getWindow().getWidth();
            int screenHeight = mc.getWindow().getHeight();
            
            int width = vt.getWidth(screenWidth, screenHeight);
            int height = vt.getHeight(screenWidth, screenHeight);
            
            if (vt.scaleVariants()) {
                double scale = vt.randomScale(RANDOM);
                width *= scale;
                height *= scale;
            }
            
            Particle particle = new Particle(vt, handler, curve, generateOffset(RANDOM, screenWidth, width), generateOffset(RANDOM, screenHeight, height), width, height, vt
                    .canRotate() && rotate ? RANDOM.nextFloat() * 360 : 0, RANDOM.nextInt(vt.getVariantAmount()));
            if (color != null)
                particle.color = color;
            add(particle);
        }
    }
    
    public static Particle addParticle(VisualType vt, VisualHandler handler, boolean rotate, @Nullable Color color) {
        int screenWidth = mc.getWindow().getWidth();
        int screenHeight = mc.getWindow().getHeight();
        
        int width = vt.getWidth(screenWidth, screenHeight);
        int height = vt.getHeight(screenWidth, screenHeight);
        
        if (vt.scaleVariants()) {
            double scale = vt.randomScale(RANDOM);
            width *= scale;
            height *= scale;
        }
        
        Particle particle = new Particle(vt, handler, generateOffset(RANDOM, screenWidth, width), generateOffset(RANDOM, screenHeight, height), width, height, vt
                .canRotate() && rotate ? RANDOM.nextFloat() * 360 : 0, RANDOM.nextInt(vt.getVariantAmount()));
        particle.setOpacityInternal(1);
        if (color != null)
            particle.color = color;
        add(particle);
        return particle;
    }
    
    public static int generateOffset(Random rand, int dimensionLength, int spacingBuffer) {
        int half = dimensionLength / 2;
        float multiplier = (float) (1 - Math.pow(rand.nextDouble(), 2));
        float textureCenterPosition = rand.nextInt(2) == 0 ? half + half * multiplier : half - half * multiplier;
        return (int) (textureCenterPosition - (spacingBuffer / 2.0F));
    }
    
}
