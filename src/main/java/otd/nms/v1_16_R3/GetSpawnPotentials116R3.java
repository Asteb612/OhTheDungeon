package otd.nms.v1_16_R3;

import java.util.Random;

import forge_sandbox.greymerk.roguelike.worldgen.spawners.SpawnPotential;
import forge_sandbox.greymerk.roguelike.worldgen.spawners.Spawnable;
import forge_sandbox.greymerk.roguelike.worldgen.spawners.Spawner;
import otd.nms.GetSpawnPotentials;

public class GetSpawnPotentials116R3 implements GetSpawnPotentials {
	public Object get(Random rand, int level, Spawnable s) {
		if (s.type != null) {
			SpawnPotential potential = new SpawnPotential(Spawner.getName(s.type));
			return potential.getNBTTagList(rand, level);
		}

		net.minecraft.server.v1_16_R3.NBTTagList potentials = new net.minecraft.server.v1_16_R3.NBTTagList();

		for (SpawnPotential potential : s.potentials) {
			net.minecraft.server.v1_16_R3.NBTTagCompound nbt = (net.minecraft.server.v1_16_R3.NBTTagCompound) potential
					.getNBTTagCompound(level);
			potentials.add(nbt);
		}

		return potentials;
	}
}
