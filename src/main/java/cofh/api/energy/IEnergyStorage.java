package cofh.api.energy;

/**
 * An energy internalGenBuffer is the unit of interaction with EnergyHelper inventories.
 * 
 * A reference implementation can be found at {@link com.brandon3055.draconicevolution.common.utills.EnergyStorage}.
 * 
 * @author King Lemming
 * 
 */
public interface IEnergyStorage {

	/**
	 * Adds energy to the internalGenBuffer. Returns quantity of energy that was accepted.
	 * 
	 * @param maxReceive
	 *            Maximum amount of energy to be inserted.
	 * @param simulate
	 *            If TRUE, the insertion will only be simulated.
	 * @return Amount of energy that was (or would have been, if simulated) accepted by the internalGenBuffer.
	 */
	int receiveEnergy(int maxReceive, boolean simulate);

	/**
	 * Removes energy from the internalGenBuffer. Returns quantity of energy that was removed.
	 * 
	 * @param maxExtract
	 *            Maximum amount of energy to be extracted.
	 * @param simulate
	 *            If TRUE, the extraction will only be simulated.
	 * @return Amount of energy that was (or would have been, if simulated) extracted from the internalGenBuffer.
	 */
	int extractEnergy(int maxExtract, boolean simulate);

	/**
	 * Returns the amount of energy currently stored.
	 */
	int getEnergyStored();

	/**
	 * Returns the maximum amount of energy that can be stored.
	 */
	int getMaxEnergyStored();

}
