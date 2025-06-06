package proto.mechanicalarms.common.entities.ai;

import net.minecraft.entity.ai.EntityAIAttackMelee;
import proto.mechanicalarms.common.entities.EntityWeirdZombie;

public class EntityAIWeirdZombieAttack extends EntityAIAttackMelee {
    private int raiseArmTicks;
    private EntityWeirdZombie weirdZombie;

    public EntityAIWeirdZombieAttack(EntityWeirdZombie zombieIn, double speedIn, boolean longMemoryIn) {
        super(zombieIn, speedIn, longMemoryIn);
        this.weirdZombie = zombieIn;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        super.startExecuting();
        this.raiseArmTicks = 0;
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask() {
        super.resetTask();
        this.weirdZombie.setArmsRaised(false);
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask() {
        super.updateTask();
        ++this.raiseArmTicks;

        if (this.raiseArmTicks >= 5 && this.attackTick < 10) {
            this.weirdZombie.setArmsRaised(true);
        } else {
            this.weirdZombie.setArmsRaised(false);
        }
    }
}
