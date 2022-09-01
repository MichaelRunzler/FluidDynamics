package michaelrunzler.fluiddynamics.types;

import net.minecraft.core.Direction;

/**
 * Translates a block's absolute (cardinal) facing into relative facings based upon its placement direction.
 */
public class RelativeFacing
{
    public Direction FRONT;
    public Direction BACK;
    public Direction LEFT;
    public Direction RIGHT;
    public Direction BOTTOM;
    public Direction TOP;

    public RelativeFacing(Direction facing)
    {
        if(facing == Direction.UP || facing == Direction.DOWN) throw new IllegalArgumentException("Unable to transform vertical direction to relative: " + facing.getName());

        this.FRONT = facing;
        this.BACK = facing.getOpposite();
        this.LEFT = facing.getCounterClockWise();
        this.RIGHT = facing.getClockWise();
        this.TOP = Direction.UP;
        this.BOTTOM = Direction.DOWN;
    }
}
