package com.desastres.disaster.impl;

import com.desastres.disaster.Disaster;
import com.desastres.disaster.DisasterManager;
import com.desastres.entity.DisasterVisualEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Granizada: una nube 3D grande y bolas de hielo 3D que caen sobre la zona. */
public class HailstormDisaster extends Disaster {
    private final int duration;
    private final double radius;
    private double windAngle;

    public HailstormDisaster(ServerLevel level, Vec3 target, int intensity) {
        super(level, target, intensity);
        this.duration = scaled(900 + 400 * intensity);
        this.radius = 26 + 12 * intensity;
        this.windAngle = random.nextDouble() * Math.PI * 2.0D;
    }

    @Override
    protected void tick() {
        if (age >= duration) {
            finish();
            return;
        }
        int cx = Mth.floor(pos.x);
        int cz = Mth.floor(pos.z);
        if (!loaded(cx, cz)) {
            return;
        }
        double cloudY = Math.min(level.getMaxBuildHeight() - 20, surface(cx, cz) + 55);
        showVisual(DisasterVisualEntity.Kind.STORM_CLOUD, pos.x, cloudY, pos.z,
                (float) radius, 14.0F, 0.75F, 0.0D, 0.0D, 0.0D);

        windAngle += (random.nextDouble() - 0.5D) * 0.05D;
        double wx = Math.cos(windAngle) * 0.4D;
        double wz = Math.sin(windAngle) * 0.4D;

        double fade = Math.min(1.0D, Math.min(age / 60.0D, (duration - age) / 60.0D));
        double rate = (0.7D + 0.8D * intensity) * fade;
        int count = (int) rate + (random.nextDouble() < rate - (int) rate ? 1 : 0);
        List<ServerPlayer> players = level.players();
        for (int i = 0; i < count; i++) {
            double x;
            double z;
            if (!players.isEmpty() && random.nextInt(100) < 35) {
                ServerPlayer p = players.get(random.nextInt(players.size()));
                x = p.getX() + (random.nextDouble() - 0.5D) * 24.0D;
                z = p.getZ() + (random.nextDouble() - 0.5D) * 24.0D;
            } else {
                double r = radius * Math.sqrt(random.nextDouble());
                double a = random.nextDouble() * Math.PI * 2.0D;
                x = pos.x + Math.cos(a) * r;
                z = pos.z + Math.sin(a) * r;
            }
            if (!loaded(Mth.floor(x), Mth.floor(z))) {
                continue;
            }
            Vec3 start = new Vec3(x, cloudY - 4.0D, z);
            Vec3 vel = new Vec3(wx, -(1.5D + random.nextDouble()), wz);
            float size = 0.25F + 0.05F * intensity + random.nextFloat() * 0.25F;
            DisasterManager.add(new LavaBombDisaster(level, start, vel, intensity, 0.0F, size, true));
        }
    }
}
