package nuclearscience.common.tile;

import electrodynamics.api.electricity.CapabilityElectrodynamic;
import electrodynamics.prefab.tile.GenericTileTicking;
import electrodynamics.prefab.tile.components.ComponentType;
import electrodynamics.prefab.tile.components.type.ComponentElectrodynamic;
import electrodynamics.prefab.tile.components.type.ComponentPacketHandler;
import electrodynamics.prefab.tile.components.type.ComponentTickable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import nuclearscience.DeferredRegisters;
import nuclearscience.common.settings.Constants;

public class TileFusionReactorCore extends GenericTileTicking {
    public int deuterium;
    public int tritium;
    private int timeLeft = 0;

    public TileFusionReactorCore() {
	super(DeferredRegisters.TILE_FUSIONREACTORCORE.get());
	addComponent(new ComponentTickable().tickServer(this::tickServer));
	addComponent(new ComponentPacketHandler().customPacketReader(this::readCustomPacket).customPacketWriter(this::writeCustomPacket));
	addComponent(new ComponentElectrodynamic(this).input(Direction.DOWN).input(Direction.UP)
		.maxJoules(Constants.FUSIONREACTOR_USAGE_PER_TICK * 20.0).voltage(CapabilityElectrodynamic.DEFAULT_VOLTAGE * 4));
    }

    public void tickServer(ComponentTickable tick) {
	ComponentElectrodynamic electro = getComponent(ComponentType.Electrodynamic);
	if (level.getLevelData().getDayTime() % 20 == 0) {
	    this.<ComponentPacketHandler>getComponent(ComponentType.PacketHandler).sendCustomPacket();
	}
	if (tritium > 0 && deuterium > 0 && timeLeft <= 0 && electro.getJoulesStored() > Constants.FUSIONREACTOR_USAGE_PER_TICK) {
	    deuterium -= 1;
	    tritium -= 1;
	    timeLeft = 15 * 20;
	}
	if (timeLeft > 0 && electro.getJoulesStored() > Constants.FUSIONREACTOR_USAGE_PER_TICK) {
	    for (Direction dir : Direction.values()) {
		if (dir != Direction.UP && dir != Direction.DOWN) {
		    BlockPos offset = worldPosition.relative(dir);
		    BlockState state = level.getBlockState(offset);
		    if (state.getBlock() == DeferredRegisters.blockPlasma) {
			BlockEntity tile = level.getBlockEntity(offset);
			if (tile instanceof TilePlasma && ((TilePlasma) tile).ticksExisted > 30) {
			    ((TilePlasma) tile).ticksExisted = 0;
			}
		    } else if (state.getBlock() == Blocks.AIR) {
			level.setBlockAndUpdate(offset, DeferredRegisters.blockPlasma.defaultBlockState());
		    }
		}
	    }
	    electro.joules(electro.getJoulesStored() - Constants.FUSIONREACTOR_USAGE_PER_TICK);
	}
	timeLeft--;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
	writeCustomPacket(compound);
	return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
	super.load(state, compound);
	readCustomPacket(compound);
    }

    public void writeCustomPacket(CompoundTag nbt) {
	nbt.putInt("deuterium", deuterium);
	nbt.putInt("tritium", tritium);
    }

    public void readCustomPacket(CompoundTag nbt) {
	deuterium = nbt.getInt("deuterium");
	tritium = nbt.getInt("tritium");
    }
}
