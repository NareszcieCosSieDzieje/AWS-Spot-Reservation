package client;

import com.datastax.oss.driver.api.core.PagingIterable;
import models.entities.AWSSpot;
import models.entities.AZToEC2Mapping;
import models.mappers.InventoryMapper;

import java.util.*;
import java.util.function.Consumer;
import java.lang.reflect.*; //FIXME WYWAL

public class AwsConsoleInterface {

    private InventoryMapper inventoryMapper;
    private final String EXIT_CODE = "X";

    public AwsConsoleInterface(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    public void startLoop() {
        this.optionsMenu("# "); //TODO: USUN
        String response;
        printHeader();
        do {
            printMenu();
            response = getResponse();
            handleResponse(response);
            System.out.println();
        } while (!response.equalsIgnoreCase(EXIT_CODE));
    }

    private void printHeader() {
        System.out.println("=========================================================");
        System.out.println("                 AWS Spot Reservator 1.0                 ");
        System.out.println(" Copyright © by Paweł Kryczka and Krzysztof Charlikowski ");
        System.out.println("=========================================================");
        System.out.println();
    }

    private void printMenu() {
        System.out.println("1. Reserve a spot");
        System.out.println("2. Show reserved spots");
        System.out.println("3. Show instance types");
        System.out.println("4. Run big traffic simulation");
        System.out.println(EXIT_CODE + ". Exit");
    }

    private String getResponse() {
        System.out.print("Your choice: ");
        Scanner reader = new Scanner(System.in);
        return reader.nextLine().strip();
    }

    private <T> T getSelectedItem(ArrayList<T> selectionList, String loopText) {
        Scanner reader = new Scanner(System.in);
        int indexStart = 0;
        int indexEnd = selectionList.size() - 1;
        int chosenIndex = -1;
        final Runnable errorMessage = () -> {
            System.out.println("Index must be a valid integer! The accepted range is [" + indexStart + ", " + indexEnd + "]");
        };

        do {
            System.out.println(loopText);
            try {
                chosenIndex = Integer.parseInt(reader.nextLine().strip());
            } catch(NumberFormatException e) {
                errorMessage.run();
                continue;
            }
            if (chosenIndex < indexStart || chosenIndex > indexEnd) {
                errorMessage.run();
                continue;
            }
            return selectionList.get(chosenIndex);
        } while (true);
    }

    private String optionsMenu(String prompt) {
        PagingIterable<AZToEC2Mapping> azToEC2Mappings = this.inventoryMapper.azToEc2MappingDao().findAll();
        HashSet<String> foundRegionsSet = new HashSet();
        HashSet<String> foundAzsSet = new HashSet();
        HashSet<String> foundInstanceTypesSet = new HashSet();
        Spliterator<AZToEC2Mapping> azToEC2MappingSpliterator = azToEC2Mappings.spliterator();
        azToEC2MappingSpliterator.forEachRemaining( (item) -> {
            foundRegionsSet.add(item.getRegion());
            foundAzsSet.add(item.getAz_name());
            foundInstanceTypesSet.add(item.getInstance_type());
//            System.out.println(item);
        } );

        System.out.print(prompt);

        ArrayList<String> foundAzsList = new ArrayList<>(foundAzsSet);
        ArrayList<String> foundRegionsList = new ArrayList<>(foundRegionsSet);
        ArrayList<String> foundInstanceTypesList = new ArrayList<>(foundInstanceTypesSet);

        ArrayList<ArrayList<String>> listOfLists = new ArrayList<>(Arrays.asList(foundAzsList, foundRegionsList, foundInstanceTypesList));
        HashMap<Integer, String> listPromptMap = new HashMap<>(Map.of(
                0, "Choose region",
                1, "Choose availability zone",
                2, "Choose instance type"));

        HashMap<Integer, String> chosenElements = new HashMap<>();

        for(List<String> list: listOfLists) {
            int listIndex = listOfLists.indexOf(list);
            if (listIndex < 0) {
                System.err.println("Error getting the list index!");
            }
            int idx = 0;
            for (String elem: list) {
                System.out.println(idx + ". " + elem);
                idx += 1;
            }
            String chosenItem = this.getSelectedItem(foundRegionsList, listPromptMap.get(listIndex));
            chosenElements.put(listIndex, chosenItem);
        }

        // TODO: USE 'chosenElements' !!

        return "";
    }

    private void handleResponse(String response) {
        System.out.println("Your choice was: " + response);
    }

    private void reserveSpot(String region, String instance_type, String az_name) {
        AZToEC2Mapping azToEC2Mappings = this.inventoryMapper.azToEc2MappingDao().
                findByRegionAndInstanceTypeAndAzName(region, instance_type, az_name);

    }

    private void showSpots() {
        PagingIterable<AWSSpot> awsSpots = this.inventoryMapper.awsSpotDao().findAll();
        for (AWSSpot awsSpot: awsSpots) {
            System.out.println("spot: " + awsSpot.toString());
        }
    }
}
