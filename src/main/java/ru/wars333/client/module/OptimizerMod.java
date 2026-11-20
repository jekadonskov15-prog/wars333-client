package ru.wars333.client.module;

import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import ru.wars333.client.setting.BoolSetting;
import ru.wars333.client.setting.ModeSetting;

/**
 * Optimizer: снижает настройки графики и нагрузку от самого клиента.
 * При выключении возвращает прежние значения.
 */
public class OptimizerMod extends Mod {
    public final ModeSetting profile = mode("Профиль", 0, "Баланс", "Максимум FPS");
    public final BoolSetting auto = bool("Авто-снижение эффектов при низком FPS", true);

    private boolean saved;
    private int sView, sSim, sBlend;
    private double sEntity;
    private boolean sShadows;
    private Object sParticles;
    private CloudRenderMode sClouds;

    public OptimizerMod() {
        super("Optimizer", Category.SYSTEM, "Снижает нагрузку: настройки графики и эффекты клиента", false);
    }

    @Override
    public String suffix() {
        return profile.get();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onEnable() {
        GameOptions o = mc.options;
        if (o == null) return;
        if (!saved) {
            sView = o.getViewDistance().getValue();
            sSim = o.getSimulationDistance().getValue();
            sBlend = o.getBiomeBlendRadius().getValue();
            sEntity = o.getEntityDistanceScaling().getValue();
            sShadows = o.getEntityShadows().getValue();
            sParticles = o.getParticles().getValue();
            sClouds = o.getCloudRenderMode().getValue();
            saved = true;
        }
        apply();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onDisable() {
        GameOptions o = mc.options;
        if (o == null || !saved) return;
        o.getViewDistance().setValue(sView);
        o.getSimulationDistance().setValue(sSim);
        o.getBiomeBlendRadius().setValue(sBlend);
        o.getEntityDistanceScaling().setValue(sEntity);
        o.getEntityShadows().setValue(sShadows);
        if (sParticles != null) ((SimpleOption) o.getParticles()).setValue(sParticles);
        o.getCloudRenderMode().setValue(sClouds);
        saved = false;
    }

    private void apply() {
        GameOptions o = mc.options;
        if (o == null) return;
        boolean max = "Максимум FPS".equals(profile.get());

        o.getViewDistance().setValue(max ? Math.min(sView, 6) : Math.min(sView, 10));
        o.getSimulationDistance().setValue(max ? 5 : Math.min(sSim, 8));
        o.getBiomeBlendRadius().setValue(max ? 0 : Math.min(sBlend, 1));
        o.getEntityDistanceScaling().setValue(max ? 0.5 : 0.75);
        o.getEntityShadows().setValue(false);
        o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        setParticles(o, max ? "MINIMAL" : "DECREASED");
    }

    /** Множитель плотности визуальных эффектов (его использует другой код проекта). */
    public float visualScale() {
        if (!saved) return 1f;
        return "Максимум FPS".equals(profile.get()) ? 0.5f : 0.75f;
    }

    /** Частицы меняем по имени значения, чтобы не зависеть от названия enum-класса в разных версиях. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setParticles(GameOptions o, String wanted) {
        SimpleOption opt = o.getParticles();
        Object cur = opt.getValue();
        if (!(cur instanceof Enum)) return;
        for (Object e : ((Enum) cur).getDeclaringClass().getEnumConstants()) {
            if (((Enum) e).name().equals(wanted)) {
                opt.setValue(e);
                return;
            }
        }
    }
}
