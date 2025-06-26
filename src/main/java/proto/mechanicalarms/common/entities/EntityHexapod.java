package proto.mechanicalarms.common.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import proto.mechanicalarms.MechanicalArms;
import proto.mechanicalarms.client.renderer.entities.KinematicChain;
import proto.mechanicalarms.client.renderer.entities.ModelSegment;
import proto.mechanicalarms.client.renderer.util.Quaternion;

import javax.annotation.Nullable;

public class EntityHexapod extends EntityCreature {

    // We reuse the zombie model which has arms that need to be raised when the zombie is attacking:
    private static final DataParameter<Boolean> ARMS_RAISED = EntityDataManager.createKey(EntityHexapod.class, DataSerializers.BOOLEAN);

    public static final ResourceLocation LOOT = new ResourceLocation(MechanicalArms.MODID, "entities/weird_zombie");


    ModelSegment mainBody = new ModelSegment(ModelSegment.FORWARD, ModelSegment.FORWARD);
    Quaternionf mainBodyRotation = new Quaternionf();
    static final Vector3f FORWARD = new Vector3f(0, 0, -1);
    KinematicChain kinematicChain = new KinematicChain(mainBody, this::getPositionVector, this::getBodyRotation);

    ModelSegment rightFrontArm = ArmFactory.arm(ModelSegment.RIGHT, 3, new Vector3f(0.5f,-0.3f, -0.8f));
    ModelSegment leftFrontArm = ArmFactory.arm(ModelSegment.LEFT, 3, new Vector3f(-0.5f,-0.3f, -0.8f));
    ModelSegment rightMidArm = ArmFactory.arm(ModelSegment.RIGHT, 3, new Vector3f(0.5f,-0.3f, 0f));
    ModelSegment leftMidArm = ArmFactory.arm(ModelSegment.LEFT, 3, new Vector3f(-0.5f,-0.3f, 0f));
    ModelSegment rightRearArm = ArmFactory.arm(ModelSegment.RIGHT, 3, new Vector3f(0.5f,-0.3f, 0.8f));
    ModelSegment leftRearArm = ArmFactory.arm(ModelSegment.LEFT, 3, new Vector3f(-0.5f,-0.3f, 0.8f));

    KinematicChain frontRightArmChain = new KinematicChain(kinematicChain, rightFrontArm);
    KinematicChain frontLeftArmChain = new KinematicChain(kinematicChain, leftFrontArm);
    KinematicChain midRightArmChain = new KinematicChain(kinematicChain, rightMidArm);
    KinematicChain midLeftArmChain = new KinematicChain(kinematicChain, leftMidArm);
    KinematicChain rearRightArmChain = new KinematicChain(kinematicChain, rightRearArm);
    KinematicChain rearLeftArmChain = new KinematicChain(kinematicChain, leftRearArm);

    public EntityHexapod(World worldIn) {
        super(worldIn);
        setSize(1F, 1F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(ARMS_RAISED, Boolean.FALSE);
    }

    @Override
    public void onEntityUpdate() {
        //RECHECK THIS
        float f = (float) (Math.sin(System.currentTimeMillis() / 100d)) / 2f;
        //kinematicChain.updateFromNewBase(new Vector3f(0,f, 0));
        //tmainBody.move(0, 0.5f + f, 0);
        super.onEntityUpdate();
        mainBodyRotation.identity();
        mainBodyRotation.rotateY((float) (Math.toRadians(getPitchYaw().y)));

        Vector3f ra = new Vector3f(2.0f, -mainBody.getBaseVector().y, -2);
        Vector3f ra2 = new Vector3f(ra);
        ra.rotate(mainBodyRotation);

        frontRightArmChain.doFabrik(ra2);
        frontLeftArmChain.doFabrik(new Vector3f(-2.0f, -mainBody.getBaseVector().y, -2));
    }

    @Override
    public void onLivingUpdate() {
        Vector3f target = new Vector3f((int)posX + 2f, (float) posY, (float) posZ);

        if (!(world.getBlockState(new BlockPos(target.x, target.y, target.z)) == Blocks.AIR.getDefaultState())) {
            target.add(0,1,0);
        }

        target.sub((float) this.posX, (float) this.posY + 0.4f, (float) this.posZ);

        //this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, midRightArmChain.endEffectorWorldlyPosition.x, midRightArmChain.endEffectorWorldlyPosition.y, midRightArmChain.endEffectorWorldlyPosition.z, 0,0,0);
        Vector3f endeffector = midRightArmChain.endEffectorPosition;

        float maxDistance = 0.1f;

// Clamp X
        target.x = Math.clamp(target.x, endeffector.x - maxDistance, endeffector.x + maxDistance);

// Clamp Y
        target.y = Math.clamp(target.y, endeffector.y - maxDistance, endeffector.y + maxDistance);

// Clamp Z
        target.z = Math.clamp(target.z, endeffector.z - maxDistance, endeffector.z + maxDistance);


        midRightArmChain.doFabrik(target);

        midLeftArmChain.doFabrik(new Vector3f(-2, -mainBody.getBaseVector().y, 0));
        rearRightArmChain.doFabrik(new Vector3f(2, -mainBody.getBaseVector().y, 0.8f));
        rearLeftArmChain.doFabrik(new Vector3f(-2, -mainBody.getBaseVector().y, 0.8f));
        //moveRelative(0.1f, 0, 0, 1f);
//
        //this.motionX += 0.2f;
        this.move(MoverType.SELF, 0.1f, 0, 0);
        super.onLivingUpdate();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        // Here we set various attributes for our mob. Like maximum health, armor, speed, ...
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.13D);
        //this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
    }

    public void setArmsRaised(boolean armsRaised) {
        this.getDataManager().set(ARMS_RAISED, Boolean.valueOf(armsRaised));
    }

    @SideOnly(Side.CLIENT)
    public boolean isArmsRaised() {
        return this.getDataManager().get(ARMS_RAISED).booleanValue();
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.applyEntityAI();
    }

    private void applyEntityAI() {
        this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[]{EntityPigZombie.class}));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityVillager.class, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityIronGolem.class, true));
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (super.attackEntityAsMob(entityIn)) {
            if (entityIn instanceof EntityLivingBase) {
                // This zombie gives health boost and regeneration when it attacks
                ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, 200));
                ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 5;
    }

    public javax.vecmath.Vector3f getTranslation() {
        return new javax.vecmath.Vector3f(mainBody.getBaseVector().x, mainBody.getBaseVector().y, mainBody.getBaseVector().z);
    }

    public Quaternion getFrontRight(int segment) {
        return getSegmentRotation(frontRightArmChain, segment);
    }

    public Quaternion getFrontLeft(int segment) {
        return getSegmentRotation(frontLeftArmChain, segment);
    }

    public Quaternion getMidRight(int segment) {
        return getSegmentRotation(midRightArmChain, segment);
    }

    public Quaternion getMidLeft(int segment) {
        return getSegmentRotation(midLeftArmChain, segment);
    }

    public Quaternion getRearRight(int segment) {
        return getSegmentRotation(rearRightArmChain, segment);
    }

    public Quaternion getRearLeft(int segment) {
        return getSegmentRotation(rearLeftArmChain, segment);
    }

    public Quaternion getSegmentRotation(KinematicChain chain, int segment) {
        ModelSegment currentSegment = chain.root;
        // Traverse down the chain of children based on the segment index
        for (int i = 0; i < segment; i++) {
            // Check if we've run out of children before reaching the desired segment
            if (currentSegment.children.isEmpty()) {
                // Or return null, or throw a more specific exception like IndexOutOfBoundsException
                throw new IndexOutOfBoundsException("Segment " + segment + " is out of bounds for the right arm chain.");
            }
            currentSegment = currentSegment.children.get(0);
        }

        // Get the current rotation of the final segment
        Quaternionf la = currentSegment.getCurrentRotation(chain.endEffectorPosition);

        // Assuming Quaternion is a class that takes x, y, z, w
        return new Quaternion(la.x, la.y, la.z, la.w);
    }

    public Quaternion getBodyRotation() {
        return new Quaternion(mainBodyRotation.x, mainBodyRotation.y, mainBodyRotation.z, mainBodyRotation.w);
    }

    @Override
    public boolean getCanSpawnHere() {
        return false;
    }

    static class ArmFactory {

        static ModelSegment arm(Vector3f side, int numberSegments, Vector3f rootAttachmentPos) {
            ModelSegment root = new ModelSegment(side, ModelSegment.UP, rootAttachmentPos);
            ModelSegment current = root;
            ModelSegment child;
            for (int i = 1; i < numberSegments; i++) {
                child = new ModelSegment(side, i == numberSegments - 1 ? ModelSegment.DOWN : ModelSegment.UP);
                current.withChild(child);
                current = child;
            }
            return root;
        }
    }
}