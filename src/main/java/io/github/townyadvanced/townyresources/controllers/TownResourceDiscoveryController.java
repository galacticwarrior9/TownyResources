package io.github.townyadvanced.townyresources.controllers;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.townyresources.TownyResources;
import io.github.townyadvanced.townyresources.metadata.TownyResourcesGovernmentMetaDataController;
import io.github.townyadvanced.townyresources.objects.ResourceOfferCategory;
import io.github.townyadvanced.townyresources.settings.TownyResourcesSettings;
import io.github.townyadvanced.townyresources.settings.TownyResourcesTranslation;
import io.github.townyadvanced.townyresources.util.TownyResourcesMessagingUtil;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TownResourceDiscoveryController {
    /**
     * Discover a new resource for a town
     * 
     * After discovery, recalculates town production
     * After discovery, recalculates nation production (if the town has an owner nation)
     * 
     * @param resident the resident who did the survey
     * @param town the town
     * @param surveyLevel the level of the survey
     * @param surveyCost the cost of the survey
     * @param alreadyDiscoveredMaterials list of the town's already-discovered materials
     * @throws TownyException 
     */
    public static void discoverNewResource(Resident resident,
                                            Town town,
                                            int surveyLevel,
                                            double surveyCost,
                                            List<String> alreadyDiscoveredMaterials) throws TownyException{

        //Ensure the resource at this level has not already been discovered
        List<String> discoveredResources = TownyResourcesGovernmentMetaDataController.getDiscoveredAsList(town);
        if(surveyLevel <= discoveredResources.size()) {
            throw new TownyException(TownyResourcesTranslation.of("msg_err_level_x_resource_already_discovered", surveyLevel));
        }

        //Ensure the player can afford this survey
        if (TownyEconomyHandler.isActive() && !resident.getAccount().canPayFromHoldings(surveyCost))
			throw new TownyException(TownyResourcesTranslation.of("msg_err_survey_too_expensive",
                TownyEconomyHandler.getFormattedBalance(surveyCost), resident.getAccount().getHoldingFormattedBalance()));

 		/*
 		 * Generate a list of candidate categories
 		 * This list will be comprised of all resource offer categories, except those of already discovered materials
 		 */
 		List<ResourceOfferCategory> candidateCategories = new ArrayList<>();
        CATEGORY_LOOP:
 		for(ResourceOfferCategory category: TownResourceOffersController.getResourceOfferCategoryList()) { 		    
 		    //Skip category if we have already discovered something in it
 		    for(String material: alreadyDiscoveredMaterials) {
 		        if(category.getMaterialsInCategory().contains(material))
    		        continue CATEGORY_LOOP;
            }
 	        //Add category as a candidate
 	        candidateCategories.add(category);
        }
 		//Ensure there are enough candidates left for a new discovery
        if(candidateCategories.size() < 1)
            throw new TownyException(TownyResourcesTranslation.of("msg_err_not_enough_offers_left"));

        /*
         * Generate a discovery map which will allow us to pick a winning offer
         * The map is in the form <ID><Candidate>
         */
        int discoveryId = 0;
        Map<Integer,ResourceOfferCategory> discoveryMap = new HashMap<>();
        for(ResourceOfferCategory category: candidateCategories) {
            discoveryMap.put(discoveryId, category);
            discoveryId += category.getDiscoveryWeight();    
        }
                
        //Determine which offer has won
        int winningNumber = (int)((Math.random() * discoveryId));
        ResourceOfferCategory winningCategory = null;
        for(Map.Entry<Integer, ResourceOfferCategory> candidate: discoveryMap.entrySet()) {
            if(winningNumber >= candidate.getKey() && winningNumber < candidate.getKey() + candidate.getValue().getDiscoveryWeight()) {
                winningCategory = candidate.getValue();
                break;
            }
        }
        
        //Determine the winning material
        winningNumber = (int)((Math.random() * winningCategory.getMaterialsInCategory().size()));
        String winningMaterial = winningCategory.getMaterialsInCategory().get(winningNumber);

		//Pay for the survey
		resident.getAccount().withdraw(surveyCost, "Cost of resources survey.");

        //Discover the resource
        List<String> discoveredMaterials = new ArrayList<>(alreadyDiscoveredMaterials);
        discoveredMaterials.add(winningMaterial);
        TownyResourcesGovernmentMetaDataController.setDiscovered(town, discoveredMaterials);
        town.save();

        //Recalculate Town Production
        TownResourceProductionController.recalculateProductionForOneTown(town);

        //Recalculate Nation Production
        if(TownyResources.getPlugin().isSiegeWarInstalled() && TownOccupationController.isTownOccupied(town)) {
            TownResourceProductionController.recalculateProductionForOneNation(TownOccupationController.getTownOccupier(town));
        } else if (town.hasNation()) {
            TownResourceProductionController.recalculateProductionForOneNation(town.getNation());
        }
        
         //Send global message
   		int levelOfNewResource = discoveredMaterials.size();
   		double productivityModifierNormalized = (double)TownyResourcesSettings.getProductionPercentagesPerResourceLevel().get(levelOfNewResource-1) / 100;
        int preTaxProduction = (int)((winningCategory.getBaseAmountItems() * productivityModifierNormalized) + 0.5); 
        String categoryName = TownyResourcesMessagingUtil.formatOfferCategoryNameForDisplay(winningCategory);
        String materialName = TownyResourcesMessagingUtil.formatMaterialNameForDisplay(winningMaterial);
		TownyResourcesMessagingUtil.sendGlobalMessage(TownyResourcesTranslation.of("discovery.success", resident.getName(), categoryName, town.getName(), preTaxProduction, materialName));
    }
}
