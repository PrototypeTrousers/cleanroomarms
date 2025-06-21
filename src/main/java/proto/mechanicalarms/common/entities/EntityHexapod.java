package proto.mechanicalarms.common.entities;

import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
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

public class EntityHexapod extends EntityMob {

    // We reuse the zombie model which has arms that need to be raised when the zombie is attacking:
    private static final DataParameter<Boolean> ARMS_RAISED = EntityDataManager.createKey(EntityHexapod.class, DataSerializers.BOOLEAN);

    public static final ResourceLocation LOOT = new ResourceLocation(MechanicalArms.MODID, "entities/weird_zombie");


    ModelSegment mainBody = new ModelSegment();
    KinematicChain kinematicChain = new KinematicChain(mainBody);
    //    ModelSegment leftArm = new ModelSegment(mainBody, 3);
    ModelSegment rightArm = new ModelSegment(2);
    KinematicChain rightArmChain = new KinematicChain(kinematicChain, rightArm);
//    ModelSegment leftMidLeg = new ModelSegment(mainBody, 3);
//    ModelSegment rightMidLeg = new ModelSegment(mainBody, 3);
//    ModelSegment leftBackLeg = new ModelSegment(mainBody, 3);
//    ModelSegment rightBackLeg = new ModelSegment(mainBody, 3);

    public EntityHexapod(World worldIn) {
        super(worldIn);
        setSize(1F, 1F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(ARMS_RAISED, Boolean.valueOf(false));
    }

    @Override
    public void onEntityUpdate() {
        //RECHECK THIS
        float f = (float) (Math.sin(System.currentTimeMillis() /100d) ) /2f;
        //kinematicChain.updateFromNewBase(new Vector3f(0,f, 0));
        mainBody.move(0, 1, 0);


        Quaternionf la = new Quaternionf();
        la.rotateY((float) this.getLook(1).x);
        Vector3f ra = new Vector3f(3f, 0, 0);
        Vector3f ra2 = new Vector3f(1.77f + f,  -1f, -1.25f);
        ra.rotate(la);
//        if (!(world.getBlockState(new BlockPos(posX + ra.x, posY + ra.y, posZ + ra.z)) == Blocks.AIR.getDefaultState())) {
//            ra2.y += 1;
//        }
        rightArmChain.doFabrik(ra2);
        super.onEntityUpdate();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        // Here we set various attributes for our mob. Like maximum health, armor, speed, ...
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.13D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
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
    protected boolean isValidLightLevel() {
        return true;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 5;
    }

    public Quaternion getR1() {
        Quaternionf la = rightArm.getCurrentRotation();
        return new Quaternion(la.x, la.y, la.z, la.w);
    }

    public javax.vecmath.Vector3f getTranslation() {
        return new javax.vecmath.Vector3f(mainBody.getBaseVector().x, mainBody.getBaseVector().y, mainBody.getBaseVector().z);
    }


    public static void main(String[] args){
        ModelSegment mainBody = new ModelSegment();
        KinematicChain kinematicChain = new KinematicChain(mainBody);
        ModelSegment rightArm = new ModelSegment(0);
        KinematicChain rightArmChain = new KinematicChain(kinematicChain, rightArm);

        rightArmChain.doFabrik(new Vector3f(1.5f, 0f, 0));

        Quaternionf q = new Quaternionf();
        q.rotationTo(rightArm.originalVector, rightArm.getcurvec());

        Vector3f v = q.getEulerAnglesXYZ(new Vector3f());
        System.out.println(v.x);
        System.out.println(v.y);
        System.out.println(v.z);

        rightArmChain.doFabrik(new Vector3f(2f, 2f, 0));

        q.identity();
        q.rotationTo(rightArm.originalVector, rightArm.getcurvec());

        v = q.getEulerAnglesXYZ(new Vector3f());
        System.out.println(v.x);
        System.out.println(v.y);
        System.out.println(v.z);
    }

    public Quaternion getR2() {
        Quaternionf la = rightArm.children.get(0).getCurrentRotation();
        return new Quaternion(la.x, la.y, la.z, la.w);
    }

    public Quaternion getR3() {
        Quaternionf la = rightArm.children.get(0).children.get(0).getCurrentRotation();
        return new Quaternion(la.x, la.y, la.z, la.w);
    }

    public Quaternion getBodyRotation() {

        Quaternionf la = new Quaternionf();
        //la.rotateY((float) this.getLook(1).x);
        //la.rotateY(this.rotationPitch);
        return new Quaternion(la.x, la.y, la.z, la.w);
    }
}