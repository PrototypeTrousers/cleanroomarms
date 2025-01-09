package proto.mechanicalarms.common.block.properties;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import net.minecraft.block.properties.PropertyEnum;

import java.util.Collection;

public class PropertyBeltDirection extends  PropertyEnum<Directions>
{
    protected PropertyBeltDirection(String name, Collection<Directions> values)
    {
        super(name, Directions.class, values);
    }

    /**
     * Create a new PropertyDirection with the given name
     */
    public static PropertyBeltDirection create(String name)
    {
        return create(name, Predicates.alwaysTrue());
    }

    /**
     * Create a new PropertyDirection with all directions that match the given Predicate
     */
    public static PropertyBeltDirection create(String name, Predicate<Directions> filter)
    {
        return create(name, Collections2.filter(Lists.newArrayList(Directions.values()), filter));
    }

    /**
     * Create a new PropertyDirection for the given direction values
     */
    public static PropertyBeltDirection create(String name, Collection<Directions> values)
    {
        return new PropertyBeltDirection(name, values);
    }

}
