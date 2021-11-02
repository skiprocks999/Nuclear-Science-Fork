package nuclearscience.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import net.minecraft.world.item.Item.Properties;

public class ItemAntidote extends Item {

    public ItemAntidote(Properties properties) {
	super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
	if (!worldIn.isClientSide) {
	    entityLiving.curePotionEffects(stack);
	}
	if (entityLiving instanceof ServerPlayer) {
	    ServerPlayer serverplayerentity = (ServerPlayer) entityLiving;
	    CriteriaTriggers.CONSUME_ITEM.trigger(serverplayerentity, stack);
	    serverplayerentity.awardStat(Stats.ITEM_USED.get(this));
	}
	if (entityLiving instanceof Player && !((Player) entityLiving).abilities.instabuild) {
	    stack.shrink(1);
	}
	return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
	return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
	return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
	return ItemUtils.useDrink(worldIn, playerIn, handIn);
    }

}
