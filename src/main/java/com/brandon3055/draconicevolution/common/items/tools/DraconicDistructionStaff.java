package com.brandon3055.draconicevolution.common.items.tools;

import com.brandon3055.draconicevolution.common.ModItems;
import com.brandon3055.draconicevolution.common.items.tools.baseclasses.MiningTool;
import com.brandon3055.draconicevolution.common.items.tools.baseclasses.ToolHandler;
import com.brandon3055.draconicevolution.common.lib.References;
import com.brandon3055.draconicevolution.common.lib.Strings;
import com.brandon3055.draconicevolution.common.utills.IInventoryTool;
import com.brandon3055.draconicevolution.common.utills.ItemConfigField;
import com.brandon3055.draconicevolution.common.utills.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.List;

public class DraconicDistructionStaff extends MiningTool implements IInventoryTool {


	public DraconicDistructionStaff() {
		super(ModItems.DRACONIUM_T3);
		this.setUnlocalizedName(Strings.draconicDStaffName);
		this.setHarvestLevel("pickaxe", 10);
		this.setHarvestLevel("shovel", 10);
		this.setHarvestLevel("axe", 10);
		this.setCapacity(References.DRACONICCAPACITY * 3);
		this.setMaxExtract(References.DRACONICTRANSFER * 3);
		this.setMaxReceive(References.DRACONICTRANSFER * 3);
		this.energyPerOperation = References.ENERGYPERBLOCK;
		ModItems.register(this);
	}

	@Override
	public float func_150893_a(ItemStack stack, Block block) {
		return this.efficiencyOnProperMaterial;
	}


	@Override
	public List<ItemConfigField> getFields(ItemStack stack, int slot) {
		List<ItemConfigField> list = super.getFields(stack, slot);
		list.add(new ItemConfigField(References.INT_ID, slot, References.DIG_AOE).setMinMaxAndIncromente(0, 4, 1).readFromItem(stack, 0).setModifier("AOE"));
		list.add(new ItemConfigField(References.INT_ID, slot, References.DIG_DEPTH).setMinMaxAndIncromente(1, 9, 1).readFromItem(stack, 1));
		list.add(new ItemConfigField(References.INT_ID, slot, References.ATTACK_AOE).setMinMaxAndIncromente(0, 12, 1).readFromItem(stack, 1).setModifier("AOE"));
		list.add(new ItemConfigField(References.BOOLEAN_ID, slot, References.OBLITERATE).readFromItem(stack, false));
		return list;
	}

	@Override
	public String getInventoryName() {
		return StatCollector.translateToLocal("info.de.toolInventoryOblit.txt");
	}

	@Override
	public int getInventorySlots() {
		return 9;
	}

	@Override
	public boolean isEnchantValid(Enchantment enchant) {
		return enchant.type == EnumEnchantmentType.digger || enchant.type == EnumEnchantmentType.weapon;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		//todo Make this awesome
		ToolHandler.AOEAttack(player, entity, stack, 25, ItemNBTHelper.getInteger(stack, References.ATTACK_AOE, 0));
		ToolHandler.damageEntityBasedOnHealth(entity, player, 0.5F);
		return true;
	}
}
