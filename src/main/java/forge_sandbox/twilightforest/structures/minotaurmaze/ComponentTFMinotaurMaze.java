package forge_sandbox.twilightforest.structures.minotaurmaze;

import forge_sandbox.StructureBoundingBox;
import forge_sandbox.twilightforest.TFFeature;
import forge_sandbox.twilightforest.structures.StructureTFComponentOld;
import forge_sandbox.twilightforest.structures.TFMaze;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import otd.lib.async.AsyncWorldEditor;
import forge_sandbox.twilightforest.structures.StructureTFComponent;

public class ComponentTFMinotaurMaze extends StructureTFComponentOld {

	TFMaze maze;
	int rcoords[];
	private int level;

	public ComponentTFMinotaurMaze() {
		super();
	}

	public ComponentTFMinotaurMaze(TFFeature feature, int index, int x, int y, int z, int entranceX, int entranceZ,
			int level) {
		super(feature, index);
		this.setCoordBaseMode(BlockFace.SOUTH);
		this.level = level;
		this.boundingBox = StructureTFComponentOld.getComponentToAddBoundingBox(x, y, z, -getRadius(), 0, -getRadius(),
				getRadius() * 2, 5, getRadius() * 2, BlockFace.SOUTH);

		// make maze object
		maze = new TFMaze(getMazeSize(), getMazeSize());

		// set the seed to a fixed value based on this maze's x and z
		setFixedMazeSeed();

		// rooms
		int nrooms = 7;
		rcoords = new int[nrooms * 2];

		addRoomsToMaze(entranceX, entranceZ, nrooms);

		// make actual maze
		maze.generateRecursiveBacktracker(0, 0);
	}

	private void addRoomsToMaze(int entranceX, int entranceZ, int nrooms) {
		// make one entrance room always
		rcoords[0] = entranceX;
		rcoords[1] = entranceZ;
		maze.carveRoom1(entranceX, entranceZ);

		// add room coordinates, trying to keep them separate from existing rooms
		for (int i = 1; i < nrooms; i++) {
			int rx, rz;
			do {
				rx = maze.rand.nextInt(getMazeSize() - 2) + 1;
				rz = maze.rand.nextInt(getMazeSize() - 2) + 1;
			} while (isNearRoom(rx, rz, rcoords, i == 1 ? 7 : 4));

			maze.carveRoom1(rx, rz);

			rcoords[i * 2] = rx;
			rcoords[i * 2 + 1] = rz;
		}
	}

	private void setFixedMazeSeed() {
		maze.setSeed(this.boundingBox.minX * 90342903 + this.boundingBox.minY * 90342903 ^ this.boundingBox.minZ);
	}

	public ComponentTFMinotaurMaze(TFFeature feature, int index, int x, int y, int z, int level) {
		this(feature, index, x, y, z, 11, 11, level);
	}

	protected ComponentTFMazeRoom makeRoom(Random random, int i, int dx, int dz) {
		ComponentTFMazeRoom room = null;

		int worldX = boundingBox.minX + dx * 5 - 4;
		int worldY = boundingBox.minY;
		int worldZ = boundingBox.minZ + dz * 5 - 4;

		if (i == 0) {
			// default room
			room = new ComponentTFMazeRoom(getFeatureType(), 3 + i, random, worldX, worldY, worldZ);
		} else if (i == 1) {
			if (this.level == 1) {
				// exit room
				room = new ComponentTFMazeRoomExit(getFeatureType(), 3 + i, random, worldX, worldY, worldZ);
			} else {
				// boss room
				room = new ComponentTFMazeRoomBoss(getFeatureType(), 3 + i, random, worldX, worldY, worldZ);
			}
		} else if (i == 2 || i == 3) {
			if (this.level == 1) {
				// collapsed room
				room = new ComponentTFMazeRoomCollapse(getFeatureType(), 3 + i, random, worldX, worldY, worldZ);
			} else {
				// mush-room
				room = new ComponentTFMazeMushRoom(getFeatureType(), 3 + i, random, worldX, worldY, worldZ);
			}
		} else if (i == 4) {
			if (this.level == 1) {
				// fountain room
				room = new ComponentTFMazeRoomFountain(getFeatureType(), 3 + i, random, worldX, worldY, worldZ);
			} else {
				// vault
				room = new ComponentTFMazeRoomVault(getFeatureType(), 3 + i, random, worldX, worldY, worldZ);

			}
		} else {
			room = new ComponentTFMazeRoomSpawnerChests(getFeatureType(), 3 + i, random, worldX, worldY, worldZ);
		}

		return room;
	}

	/**
	 * Find dead ends and put something there
	 *
	 * @param random
	 * @param list
	 */
	protected void decorateDeadEndsCorridors(Random random, List<StructureTFComponent> list) {
		for (int x = 0; x < maze.width; x++) {
			for (int z = 0; z < maze.depth; z++) {
				StructureTFComponentOld component = null;

				// dead ends
				if (!maze.isWall(x, z, x - 1, z) && maze.isWall(x, z, x + 1, z) && maze.isWall(x, z, x, z - 1)
						&& maze.isWall(x, z, x, z + 1)) {
					component = makeDeadEnd(random, x, z, BlockFace.EAST);
				}
				if (maze.isWall(x, z, x - 1, z) && !maze.isWall(x, z, x + 1, z) && maze.isWall(x, z, x, z - 1)
						&& maze.isWall(x, z, x, z + 1)) {
					component = makeDeadEnd(random, x, z, BlockFace.WEST);
				}
				if (maze.isWall(x, z, x - 1, z) && maze.isWall(x, z, x + 1, z) && !maze.isWall(x, z, x, z - 1)
						&& maze.isWall(x, z, x, z + 1)) {
					component = makeDeadEnd(random, x, z, BlockFace.SOUTH);
				}
				if (maze.isWall(x, z, x - 1, z) && maze.isWall(x, z, x + 1, z) && maze.isWall(x, z, x, z - 1)
						&& !maze.isWall(x, z, x, z + 1)) {
					component = makeDeadEnd(random, x, z, BlockFace.NORTH);
				}

				// corridors
				if (!maze.isWall(x, z, x - 1, z) && !maze.isWall(x, z, x + 1, z) && maze.isWall(x, z, x, z - 1)
						&& maze.isWall(x, z, x, z + 1) && maze.isWall(x - 1, z, x - 1, z - 1)
						&& maze.isWall(x - 1, z, x - 1, z + 1) && maze.isWall(x + 1, z, x + 1, z - 1)
						&& maze.isWall(x + 1, z, x + 1, z + 1)) {
					component = makeCorridor(random, x, z, BlockFace.WEST);
				}
				if (!maze.isWall(x, z, x, z - 1) && !maze.isWall(x, z, x, z + 1) && maze.isWall(x, z, x - 1, z)
						&& maze.isWall(x, z, x + 1, z) && maze.isWall(x, z - 1, x - 1, z - 1)
						&& maze.isWall(x, z - 1, x + 1, z - 1) && maze.isWall(x, z + 1, x - 1, z + 1)
						&& maze.isWall(x, z + 1, x + 1, z + 1)) {
					component = makeCorridor(random, x, z, BlockFace.SOUTH);
				}

				if (component != null) {
					list.add(component);
					component.buildComponent(this, list, random);
				}
			}
		}
	}

	/**
	 * Add a dead end structure at the specified coords
	 */
	protected ComponentTFMazeDeadEnd makeDeadEnd(Random random, int dx, int dz, BlockFace rotation) {
		int worldX = boundingBox.minX + dx * 5 + 1;
		int worldY = boundingBox.minY;
		int worldZ = boundingBox.minZ + dz * 5 + 1;

		int decorationType = random.nextInt(8);

		switch (decorationType) {
		default:
		case 0:
			// blank with fence doorway
			return new ComponentTFMazeDeadEnd(getFeatureType(), 4, worldX, worldY, worldZ, rotation);
		case 1:
			return new ComponentTFMazeDeadEndChest(getFeatureType(), 4, worldX, worldY, worldZ, rotation);
		case 2:
			return random.nextBoolean()
					? new ComponentTFMazeDeadEndTripwireChest(getFeatureType(), 4, worldX, worldY, worldZ, rotation)
					: new ComponentTFMazeDeadEndTrappedChest(getFeatureType(), 4, worldX, worldY, worldZ, rotation);
		case 3:
			return new ComponentTFMazeDeadEndTorches(getFeatureType(), 4, worldX, worldY, worldZ, rotation);
		case 4:
			return new ComponentTFMazeDeadEndFountain(getFeatureType(), 4, worldX, worldY, worldZ, rotation);
		case 5:
			return new ComponentTFMazeDeadEndFountainLava(getFeatureType(), 4, worldX, worldY, worldZ, rotation);
		case 6:
			return new ComponentTFMazeDeadEndPainting(getFeatureType(), 4, worldX, worldY, worldZ, rotation);
		case 7:
			return this.level == 1
					? new ComponentTFMazeDeadEndRoots(getFeatureType(), 4, worldX, worldY, worldZ, rotation)
					: new ComponentTFMazeDeadEndShrooms(getFeatureType(), 4, worldX, worldY, worldZ, rotation);

		}

	}

	protected ComponentTFMazeCorridor makeCorridor(Random random, int dx, int dz, BlockFace rotation) {
		int worldX = boundingBox.minX + dx * 5 + 1;
		int worldY = boundingBox.minY;
		int worldZ = boundingBox.minZ + dz * 5 + 1;

		int decorationType = random.nextInt(5);

		switch (decorationType) {
		default:
		case 0:
			return null;
		case 1:
			return new ComponentTFMazeCorridor(getFeatureType(), 4, worldX, worldY, worldZ, rotation);
		case 2:
			return new ComponentTFMazeCorridorIronFence(getFeatureType(), 4, worldX, worldY, worldZ, rotation);
		case 3:
			return null; // painting
		case 4:
			return this.level == 1
					? new ComponentTFMazeCorridorRoots(getFeatureType(), 4, worldX, worldY, worldZ, rotation)
					: new ComponentTFMazeCorridorShrooms(getFeatureType(), 4, worldX, worldY, worldZ, rotation);
		}

	}

	/**
	 * Initiates construction of the Structure Component picked, at the current
	 * Location of StructGen
	 */
	@Override
	public void buildComponent(StructureTFComponent structurecomponent, List<StructureTFComponent> list,
			Random random) {
		super.buildComponent(structurecomponent, list, random);

		// add a second story
		if (this.level == 1) {
			int centerX = boundingBox.minX + ((boundingBox.maxX - boundingBox.minX) / 2);
			int centerZ = boundingBox.minZ + ((boundingBox.maxZ - boundingBox.minZ) / 2);

			ComponentTFMinotaurMaze maze = new ComponentTFMinotaurMaze(getFeatureType(), 1, centerX,
					boundingBox.minY - 10, centerZ, rcoords[2], rcoords[3], 2);
			list.add(maze);
			maze.buildComponent(this, list, random);
		}

		// add rooms where we have our coordinates
		for (int i = 0; i < rcoords.length / 2; i++) {
			int dx = rcoords[i * 2];
			int dz = rcoords[i * 2 + 1];

			// add the room as a component
			ComponentTFMazeRoom room = makeRoom(random, i, dx, dz);
			list.add(room);
			room.buildComponent(this, list, random);
		}

		// find dead ends and corridors and make components for them
		decorateDeadEndsCorridors(random, list);
	}

	@Override
	public boolean addComponentParts(AsyncWorldEditor world, Random rand, StructureBoundingBox sbb) {

//        IBlockState bedrock = Blocks.BEDROCK.getDefaultState();
		BlockData stone = Bukkit.createBlockData(Material.STONE);

//        // level 2 maze surrounded by bedrock
//        if (level == 2) {
//            fillWithBlocks(world, sbb, 0, -1, 0, getDiameter() + 2, 6, getDiameter() + 2, bedrock, AIR, false);
//        }

		// clear the area
		fillWithAir(world, sbb, 1, 1, 1, getDiameter(), 4, getDiameter());
//        fillWithBlocks(world, sbb, 0, 0, 0, getDiameter(), 0, getDiameter(), TFBlocks.mazestone, Blocks.STONE, false);
//        fillWithBlocks(world, sbb, 0, 5, 0, getDiameter(), 5, getDiameter(), TFBlocks.mazestone, Blocks.STONE, true);
		boolean onlyReplaceCeiling = this.level == 1;
		fillWithBlocks(world, sbb, 1, 5, 1, getDiameter(), 5, getDiameter(), Blocks.maze_stone, stone,
				onlyReplaceCeiling);
		fillWithBlocks(world, sbb, 1, 0, 1, getDiameter(), 0, getDiameter(), Blocks.chiseled_maze_stone, stone, false);

		//
		maze.headBlockState = Blocks.maze_stone;
		maze.wallBlockState = Blocks.maze_stone;
		maze.rootBlockState = Blocks.maze_stone;
		maze.pillarBlockState = Blocks.chiseled_maze_stone;
		maze.wallBlocks = new StructureTFMazeStones();
		maze.torchRarity = 0.05F;
		maze.tall = 2;
		maze.head = 1;
		maze.roots = 1;
		maze.oddBias = 4;

		maze.copyToStructure(world, 1, 2, 1, this, sbb);

		return true;
	}

	public int getMazeSize() {
		return 22;
	}

	public int getRadius() {
		return (int) (getMazeSize() * 2.5);
	}

	public int getDiameter() {
		return getMazeSize() * 5;
	}

	/**
	 * @return true if the specified dx and dz are within 3 of a room specified in
	 *         rcoords
	 */
	protected boolean isNearRoom(int dx, int dz, int[] rcoords, int range) {
		// if proposed coordinates are covering the origin, return true to stop the room
		// from causing the maze to fail
		if (dx == 1 && dz == 1) {
			return true;
		}

		for (int i = 0; i < rcoords.length / 2; i++) {
			int rx = rcoords[i * 2];
			int rz = rcoords[i * 2 + 1];

			if (rx == 0 && rz == 0) {
				continue;
			}

			if (Math.abs(dx - rx) < range && Math.abs(dz - rz) < range) {
				return true;
			}
		}
		return false;
	}

}
