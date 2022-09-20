package michaelrunzler.fluiddynamics.types;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Indicates that a BE has processing of some type, and thus needs to store progress data.
 */
public interface IProcessingBE
{
    AtomicInteger progress();
    AtomicInteger maxProgress();
}
