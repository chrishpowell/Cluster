/*******************************************************************************

 ******************************************************************************/
package eu.discoveri.hiercluster;

import java.util.Collection;

/**
 * 
 * @author chrispowell
 */
public class WeightedLinkageStrategy implements LinkageStrategy
{
    @Override
    public Distance calculateDistance(Collection<Distance> distances)
    {
        double sum = 0;
        double weightTotal = 0;
        for( Distance distance: distances )
        {
            weightTotal += distance.getWeight();
            sum += distance.getDistance() * distance.getWeight();
        }

	return new Distance(sum / weightTotal, weightTotal);
    }
}
