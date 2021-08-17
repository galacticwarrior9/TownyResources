package io.github.townyadvanced.townyresources.metadata;

import com.gmail.goosius.siegewar.SiegeWar;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Town;

import java.util.*;

/**
 * 
 * @author Goosius
 *
 */
public class TownyResourcesGovernmentMetaDataController {

	@SuppressWarnings("unused")
	private SiegeWar plugin;

	public TownyResourcesGovernmentMetaDataController(SiegeWar plugin) {
		this.plugin = plugin;
	}

    private static String
        discoveredMetadataKey = "townyresources_discovered",  //e.g.   OAK_LOG, SUGAR
        dailyProductionMetadataKey = "townyresources_dailyproduction",  //e.g.   32-OAK_LOG, 32-SUGAR
        availableForCollectionMetadataKey = "townyresources_availableforcollection";  //e.g.  64-OAK_LOG, 64-SUGAR

    public static String getDiscovered(Government government) {
        return MetaDataUtil.getSdf(government, discoveredMetadataKey).replaceAll(" ","");
    }
    
     /**
     * Get the discovered resources of a town
     * 
     * Note that the order is important
     * 0 - level 1 resource
     * 1 - level 2 resource
     * etc.
     * 
     * @param town the town
     * @return the town's discovered resources, as an IMMUTABLE list
     */
    public static List<String> getDiscoveredAsList(Town town) {
        String discoveredString = getDiscovered(town);
        if(discoveredString.isEmpty()) {
            return Collections.emptyList();
        } else {
            String[] discoveredArray = discoveredString.split(",");
            return Arrays.asList(discoveredArray);           
        }
    }
       
       
       /* 
    /**
     * Get the discovered resources of a town
     * 
     * Note that the order is important
     * 0 - level 1 resource
     * 1 - level 2 resource
     * etc.
     * 
     * @param town the town
     * @return the town's discovered resources
    */
    /*
    public static List<ResourceQuantity> getDiscoveredResources(Town town) {
        List<ResourceQuantity> result = new ArrayList<>();
        String resourcesString = getDiscovered(town);
        String resouces;
        int quantity;
        if(!resourcesString.isEmpty()) {
            String[] resourcesQuantitiesArray = resourcesString.split(",");
            for(String resourceQuantityPair: resourcesQuantitiesArray) {
                result.add(new ResourceQuantity(res
            }
            
            return new ArrayList<>();
        } else {
            
            return Arrays.asList(resourcesArray);        
        }
        return result;
    }

    */
    public static void setDiscovered(Government government, List<String> discoveredResources) {
        //Convert materials list to single string
        StringBuilder metadataStringBuilder = new StringBuilder();
        for(int i= 0; i < discoveredResources.size();i++) {
            if(i !=0)
                metadataStringBuilder.append(", "); 
            metadataStringBuilder.append(discoveredResources.get(i));
        }
        setDiscovered(government, metadataStringBuilder.toString());
    }

    public static void setDiscovered(Government government, String discovered) {
        MetaDataUtil.setSdf(government, discoveredMetadataKey, discovered);
    }
    
    public static String getDailyProduction(Government government) {
        return MetaDataUtil.getSdf(government, dailyProductionMetadataKey).replaceAll(" ","");
    }

    public static void setDailyProduction(Government government, List<String> dailyProduction) {
        StringBuilder dailyProductionStringBuilder = new StringBuilder();
        for(int i = 0; i < dailyProduction.size(); i++) {
            //Add comma before all entries except 1st
            if(i != 0) 
                dailyProductionStringBuilder.append(", ");
            dailyProductionStringBuilder.append(dailyProduction.get(i));
        }
        MetaDataUtil.setSdf(government, dailyProductionMetadataKey, dailyProductionStringBuilder.toString());
    }

    public static void setDailyProduction(Government government, String dailyProduction) {
        MetaDataUtil.setSdf(government, dailyProductionMetadataKey, dailyProduction);
    }
    
    public static String getAvailableForCollection(Government government) {
        return MetaDataUtil.getSdf(government, availableForCollectionMetadataKey).replaceAll(" ","");
    }

    public static Map<String, Integer> getDailyProductionAsMap(Government town) {
       return getResourceQuantitiesStringAsMap(getDailyProduction(town));
    }

    public static Map<String, Integer> getAvailableForCollectionAsMap(Government town) {
       return getResourceQuantitiesStringAsMap(getAvailableForCollection(town));
    }

    private static Map<String, Integer> getResourceQuantitiesStringAsMap(String resourceQuantitiesString) {
        Map<String,Integer> result = new HashMap<>();
        if(!resourceQuantitiesString.isEmpty()) {
            String[] resourceQuantitiesArray = resourceQuantitiesString.split(",");
            String[] resourceQuantityPair;
            String resource;
            int amount;
            for(String resourceQuantityString: resourceQuantitiesArray) {
               resourceQuantityPair = resourceQuantityString.split("-");
               amount = Integer.parseInt(resourceQuantityPair[0]); 
               resource = resourceQuantityPair[1];
               result.put(resource, amount);
            }        
        }
        return result;        
    }
    
    public static void setAvailableForCollection(Government government, Map<String, Integer> availableForCollection) {
        setResourceQuantitiesString(government, availableForCollectionMetadataKey, availableForCollection);
    }
    
    public static void setResourceQuantitiesString(Government government, String metadataKey, Map<String, Integer> resourceQuantitiesMap) {
        /* 
         * Build String
         * Note: Do not order these according to discovery
         * (it would not make sense for nations, and would not work reliably for towns)
         */
        StringBuilder resourceQuantitiesStringBuilder = new StringBuilder();
        boolean firstEntry = true;
        String resource;
        int quantity;
        for(Map.Entry<String,Integer> resourceQuantity: resourceQuantitiesMap.entrySet()) {
            if(firstEntry) {
                firstEntry = false;
                resourceQuantitiesStringBuilder.append(", ");
            }
            resource = resourceQuantity.getKey();
            quantity = resourceQuantity.getValue();
            resourceQuantitiesStringBuilder
                .append(resource)
                .append("-")
                .append(quantity);
        }

        //Set the string into metadata
        MetaDataUtil.setSdf(government, resourceQuantitiesStringBuilder.toString() , metadataKey);        
    }

}