package michaelrunzler.fluiddynamics.types;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

/**
 * A derivative of the Forge Fluid Handler type which allows direct access to and modification of fluid totals.
 */
public class FDFluidStorage extends FluidTank
{
    public FDFluidStorage(int capacity) {
        super(capacity);
    }

    public FDFluidStorage(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    public void setFluidInTank(int tankID, FluidStack fluid) {
        this.fluid = fluid;
    }

    public void setFluidAmountInTank(int tankID, int amt){
        if(!this.fluid.isEmpty()) this.fluid.setAmount(amt);
    }
}
